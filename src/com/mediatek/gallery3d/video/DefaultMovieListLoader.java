package com.mediatek.gallery3d.video;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;


/**
 * Movie list loader class. It will load videos from MediaProvider database.
 * If MoviePlayer starting activity doesn't set any thing, default OrderBy will be used.
 * Default OrderBy: MediaStore.Video.Media.DATE_TAKEN + " DESC, " + MediaStore.Video.Media._ID + " DESC ";
 */
public class DefaultMovieListLoader implements IMovieListLoader {
    private static final String TAG = "Gallery2/VideoPlayer/DefaultMovieListLoader";

    private MovieListFetcherTask mListTask;
    /**
     * Load all video list or not.[boolean]
     * "yes" means load all videos in all storages.
     * "false" means load videos located in current video's folder.
     */
    String EXTRA_ALL_VIDEO_FOLDER = "mediatek.intent.extra.ALL_VIDEO_FOLDER";
   /**
     * Video list order by column name.[String]
     */
    String EXTRA_ORDERBY = "mediatek.intent.extra.VIDEO_LIST_ORDERBY";

    @Override
    public void fillVideoList(Context context, Intent intent, LoaderListener l, IMovieItem item) {
        boolean fetechAll = false;
        if (intent.hasExtra(EXTRA_ALL_VIDEO_FOLDER)) {
            fetechAll = intent.getBooleanExtra(EXTRA_ALL_VIDEO_FOLDER, false);
        }
        //default order by
        String orderBy = MediaStore.Video.Media.DATE_TAKEN + " DESC, " + MediaStore.Video.Media._ID + " DESC ";
        if (intent.hasExtra(EXTRA_ORDERBY)) {
            orderBy = intent.getStringExtra(EXTRA_ORDERBY);
        }
        cancelList();
        mListTask = new MovieListFetcherTask(context, fetechAll, l, orderBy);
        mListTask.execute(item);
        Log.v(TAG, "fillVideoList() fetechAll=" + fetechAll + ", orderBy=" + orderBy);
    }

    @Override
    public void cancelList() {
        if (mListTask != null) {
            mListTask.cancel(true);
        }
    }

    private class MovieListFetcherTask extends AsyncTask<IMovieItem, Void, IMovieList> {
        private static final String TAG = "Gallery2/VideoPlayer/MovieListFetcherTask";
        private static final boolean LOG = true;

        private final Context mContext;
        private final ContentResolver mCr;
        private final LoaderListener mFetecherListener;
        private final boolean mFetechAll;
        private final String mOrderBy;

        public MovieListFetcherTask(Context context, boolean fetechAll, LoaderListener l, String orderBy) {
            mContext = context;
            mCr = context.getContentResolver();
            mFetecherListener = l;
            mFetechAll = fetechAll;
            mOrderBy = orderBy;
            if (LOG) {
                Log.v(TAG, "MovieListFetcherTask() fetechAll=" + fetechAll + ", orderBy=" + orderBy);
            }
        }

        @Override
        protected void onPostExecute(IMovieList params) {
            if (LOG) {
                Log.v(TAG, "onPostExecute() isCancelled()=" + isCancelled());
            }
            if (isCancelled()) {
                return;
            }
            if (mFetecherListener != null) {
                mFetecherListener.onListLoaded(params);
            }
        }

        @Override
        protected IMovieList doInBackground(IMovieItem... params) {
            if (LOG) {
                Log.v(TAG, "doInBackground() begin");
            }
            if (params[0] == null) {
                return null;
            }
            IMovieList movieList = null;
            Uri uri = params[0].getUri();
            String mime = params[0].getMimeType();
            if (mFetechAll) { //get all list
                if (MovieUtils.isLocalFile(uri, mime)) {
                    String uristr = String.valueOf(uri);
                    if (uristr.toLowerCase().startsWith("content://media")) {
                        //from gallery, gallery3D, videoplayer
                        long curId = Long.parseLong(uri.getPathSegments().get(3));
                        movieList = fillUriList(null, null, curId, params[0]);
                    }
                }
            } else { //get current list
                if (MovieUtils.isLocalFile(uri, mime)) {
                    String uristr = String.valueOf(uri);
                    if (uristr.toLowerCase().startsWith("content://media")) {
                        Cursor cursor = null;
                        try {
                            cursor = mCr.query(uri,
                                new String[]{MediaStore.Video.Media._ID,
                                    MediaStore.Video.Media.BUCKET_ID},
                                null, null, null);
                        long bucketId = -1;
                        long curId = -1;
                        if (cursor != null) {
                            if (cursor.moveToFirst()) {
                                curId = cursor.getInt(0);
                                bucketId = cursor.getLong(1);
                            }
                        }
                        movieList = fillUriList(MediaStore.Video.Media.BUCKET_ID + "=? ",
                                new String[]{String.valueOf(bucketId)}, curId, params[0]);
                        } catch (final SQLiteException e) {
                            Log.e(TAG, "query movie list fail", e);
                        } catch (IndexOutOfBoundsException e) {
                            // For some invalid video file,
                            // the path segments size of uri is less than 4
                            Log.e(TAG, "query movie list fail", e);
                        } finally {
                            if (cursor != null) {
                                cursor.close();
                            }
                        }
                    } else if (uristr.toLowerCase().startsWith("file://")) {
                        String data = Uri.decode(uri.toString());
                        if (data == null) {
                            return null;
                        }
                        data = data.replaceAll("'", "''");
                        String where = "_data LIKE '%" + data.replaceFirst("file:///", "") + "'";
                        Cursor cursor = null;
                        try {
                            cursor = mCr.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                                new String[]{"_id", MediaStore.Video.Media.BUCKET_ID},
                                where, null, null);
                        long bucketId = -1;
                        long curId = -1;
                        if (cursor != null) {
                            if (cursor.moveToFirst()) {
                                curId = cursor.getLong(0);
                                bucketId = cursor.getLong(1);
                            }
                        }
                        movieList = fillUriList(MediaStore.Video.Media.BUCKET_ID + "=? ",
                                new String[]{String.valueOf(bucketId)}, curId, params[0]);
                        } catch (final SQLiteException e) {
                            e.printStackTrace();
                        } finally {
                            if (cursor != null) {
                                cursor.close();
                            }
                        }
                    }
                }
            }
            if (LOG) {
                Log.v(TAG, "doInBackground() done return " + movieList);
            }
            return movieList;
        }

        private IMovieList fillUriList(String where, String[] whereArgs, long curId, IMovieItem current) {
            IMovieList movieList = null;
            Cursor cursor = null;
            try {
                cursor = mCr.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        new String[]{"_id", "mime_type", OpenableColumns.DISPLAY_NAME},
                        where,
                        whereArgs,
                        mOrderBy);
                boolean find = false;
                if (cursor != null && cursor.getCount() > 0) {
                    movieList = new DefaultMovieList();
                    while (cursor.moveToNext()) {
                        long id = cursor.getLong(0);
                        if (!find && id == curId) {
                            find = true;
                            movieList.add(current);
                            continue;
                        }
                        Uri uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);
                        String mimeType = cursor.getString(1);
                        String title = cursor.getString(2);
                        movieList.add(new DefaultMovieItem(mContext, uri, mimeType, title));
                    }
                }
            } catch (final SQLiteException e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return movieList;
        }
    }
}
