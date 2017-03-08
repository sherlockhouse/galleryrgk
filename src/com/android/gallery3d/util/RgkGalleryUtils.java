/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.gallery3d.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.RemoteException;
import android.util.Log;

import android.app.ActivityThread;
import android.content.pm.IPackageManager;
import android.os.storage.StorageManager;
import com.mediatek.storage.StorageManagerEx;


public class RgkGalleryUtils extends GalleryUtils{
    private static final String TAG = "Gallery2/RgkGalleryUtils";
    
    public static boolean saveBitmap(String path, Bitmap bitmap) {
        return saveBitmap(new File(path), bitmap);
    }


    public static boolean saveBitmap(File f, Bitmap bitmap) {
        if (bitmap == null || bitmap.isRecycled())
            return false;

        FileOutputStream fOut = null;
        try {
            if (f.exists())
                f.createNewFile();

            fOut = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);

            fOut.flush();
            return true;
        } catch (FileNotFoundException e) {
            Log.e(TAG, "", e);
        } catch (IOException e) {
            Log.e(TAG, "", e);
        } catch (Exception e) {
            Log.e(TAG, "", e);
        } finally {
            if (fOut != null) {
                try {
                    fOut.close();
                } catch (IOException e) {
                    Log.e(TAG, "", e);
                }
            }
        }
        return true;
    }
    //A: }
    
    // add Bug_id:JLLBLS-270 liupeng 20150925  (start)
    public static boolean isHaveExternalSDCard() {
        return Environment.getExternalStorageState().
                    equals(android.os.Environment.MEDIA_MOUNTED);

    }
    
    
    public static final int EXCEPTION_LOW_THRESHOLD_BYTES = 10 * 1024 * 1024; // 10MB
    public static boolean isEnoughSpace() {
        File file = new File(StorageManagerEx.getDefaultPath());
        // M BUG_ID:JWYYL-241 by zyn 20141226 {
//        long freeSpace = file.getFreeSpace();
        long freeSpace = file.getUsableSpace();
        // M }
        if (freeSpace > EXCEPTION_LOW_THRESHOLD_BYTES ) {
            return true;
        } else {
            return false;
        }
    }

}
