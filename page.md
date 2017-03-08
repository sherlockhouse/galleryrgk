###Page in gallery3d

各个state所执行的任务

| state      | AbstractActivity           |AlbumPage |
| ------------- |:-------------:|:----------:|
|   1. resume    | <font color=#bd2424 >restoreFilter();PhotoPlayFacade，mGLRootView, datamanager,statemanager.resume() </font> | <font color=#3ed01c >mResumeEffect,setContentPane,mAlbumDataAdapter,mAlbumView</font> |
| 2. pause    | <font color=#bd2424 >GalleryBitmapPool，MediaItem.getBytesBufferPool().clear();</font>      |  <font color=#3ed01c >mAlbumView,mActionModeHandler,mAlbumDataAdapter,</font> |
| 3. stop| <font color=#bd2424 >mGLRootView, datamanager.statemanager.pause()</font>     |     <font color=#3ed01c >null</font> |
| 4. destroy| <font color=#bd2424 >mGLRootView,removeFilter(),statemanager.destroy()</font>     |     <font color=#3ed01c >mAlbumDataAdapter,mActionModeHandler</font> |

1. 前面由讲到statemanager可以完全用fragmentManager配合系统来管理，
2. filter部分逻辑很清楚，在resume和destroy中管理即可
3. 剩下的dataManager，mGLRootView,mAlbumView,mAlbumDataAdapter,mActionModeHandler,不能简单地做resume,pause,stop的区分。
需要理解内部逻辑，并提炼出pause和stop。原来的设计中没有stop。


```java
in AbstractGalleryActivity

 @Override
 protected void onResume() {

     super.onResume();
     /// M: [FEATURE.ADD] @{
     restoreFilter();
     PhotoPlayFacade.registerMedias(this.getAndroidContext(),
             mGLRootView.getGLIdleExecuter());
     /// @}
     mGLRootView.lockRenderThread();

     try {
         getStateManager().resume();
         getDataManager().resume();
     } finally {
         mGLRootView.unlockRenderThread();
     }
     mGLRootView.onResume();
     /// M: [BUG.ADD] fix abnormal screen @{
     mGLRootView.setVisibility(View.VISIBLE);

 }

 @Override
 protected void onPause() {

     super.onPause();

     GalleryBitmapPool.getInstance().clear();
     MediaItem.getBytesBufferPool().clear();

 }

 @Override
 protected void onStop() {

     super.onStop();
     mGLRootView.onPause();
     mGLRootView.lockRenderThread();
     try {
         getStateManager().pause();
         getDataManager().pause();
     } finally {
         mGLRootView.unlockRenderThread();
     }

 }

 @Override
 protected void onDestroy() {

     super.onDestroy();
     mGLRootView.lockRenderThread();
     try {
         getStateManager().destroy();
     } finally {
         mGLRootView.unlockRenderThread();
     }
     removeFilter();
 }

in  AlbumPage

@Override
protected void onResume() {
    super.onResume();
    mIsActive = true;

    mResumeEffect = mActivity.getTransitionStore().get(KEY_RESUME_ANIMATION);
    if (mResumeEffect != null) {
        mAlbumView.setSlotFilter(mResumeEffect);
        mResumeEffect.setPositionProvider(mPositionProvider);
        mResumeEffect.start();
    }

    setContentPane(mRootPane);
    // Set the reload bit here to prevent it exit this page in clearLoadingBit().
    setLoadingBit(BIT_LOADING_RELOAD);
    mLoadingFailed = false;

    mAlbumDataAdapter.resume();

    mAlbumView.resume();
    mAlbumView.setPressedIndex(-1);
    mActionModeHandler.resume();
    if (!mInitialSynced) {
        setLoadingBit(BIT_LOADING_SYNC);
        mSyncTask = mMediaSet.requestSync(this);
    }
    /// M: [BUG.ADD] leave selection mode when plug out sdcard @{
    mActivity.setEjectListener(this);
    /// @}

}

@Override
protected void onPause() {
    super.onPause();
    mIsActive = false;


    mAlbumView.setSlotFilter(null);
    mActionModeHandler.pause();
    mAlbumDataAdapter.pause();
    mAlbumView.pause();
    DetailsHelper.pause();

    mActivity.getGalleryActionBar().removeAlbumModeListener();
    /// @}
    if (mSyncTask != null) {
        mSyncTask.cancel();
        mSyncTask = null;
        clearLoadingBit(BIT_LOADING_SYNC);
    }
    /// M: [BUG.ADD] leave selection mode when plug out sdcard @{
    mActivity.setEjectListener(null);
    /// @}


}

@Override
protected void onDestroy() {
    super.onDestroy();
    if (mAlbumDataAdapter != null) {
        mAlbumDataAdapter.setLoadingListener(null);
    }
    mActionModeHandler.destroy();

}
```
