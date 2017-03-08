
###   StateManager   vs   FragmentManager


demo: Fragment openGL

| 对比项      | StateManager           |FragmentManager  |
| ------------- |:-------------:|:-----:|
|   1. 生命周期    | <font color=#bd2424 >bad</font> | <font color=#3ed01c >good</font> |
| 2. 任务管理   | <font color=#bd2424 >bad</font>      |  <font color=#3ed01c >good</font> |
| 3. view管理| <font color=#bd2424 >null</font>     |     <font color=#3ed01c >good</font> |
***
#### 1. 生命周期
##### StateManager
1. 由Activity控制
2. 只有 **resume  pause destroy **状态

##### FragmentManager
1. 由系统控制，与Activity的生命周期相协调
2. 拥有更丰富的状态，更详细的描述

![alt text][logo]

[logo]:http://3.bp.blogspot.com/-YJSE-iQngrw/U3bLHPnB1YI/AAAAAAAABWs/CD03Kp6O-zM/s1600/fragmentlifecycle.png "Logo Title Text 2"
***
#### 2. 任务管理

##### StateManager
1. 数据结构 Stack
```java
  private Stack<StateEntry> mStack = new Stack<StateEntry>();
```
2. 看得头疼，没文档……

##### FragmentManager

1. 数据结构 arrayList
```java
final class FragmentManagerImpl extends FragmentManager implements LayoutInflater.Factory2 {
  ArrayList<BackStackRecord> mBackStack;
   // Must be accessed while locked.
  ArrayList<BackStackRecord> mBackStackIndices;
  ArrayList<Integer> mAvailBackStackIndices;
  ArrayList<OnBackStackChangedListener> mBackStackChangeListeners;
```

2. 虽然还是看不懂，但至少人家有文档教你怎么用

*** 阶段小结 ***

前面两点已经涵盖了StateManager的功能，但是FragmentManager还有管理view控件的能力


#### 3.view管理
在图库中ActivityState的子类 AlbumPage 等不仅定义了resume等生命周期的实现，同时还持有openGL绘图类
```java
in ActivityState

protected void setContentPane(GLView content) {
    mContentPane = content;
    if (mIntroAnimation != null) {
        mContentPane.setIntroAnimation(mIntroAnimation);
        mIntroAnimation = null;
    }
    mContentPane.setBackgroundColor(getBackgroundColor());
    mActivity.getGLRoot().setContentPane(mContentPane);
}
```

```java
in AbstractGalleryActivity

@Override
public void setContentView(int resId) {
    super.setContentView(resId);
    mGLRootView = (GLRootView) findViewById(R.id.gl_root_view);
    /// M: [FEATURE.ADD] @{
    PhotoPlayFacade.registerMedias(this.getAndroidContext(),
            mGLRootView.getGLIdleExecuter());
    /// @}
}
```

```xml
in main.xml

    <include layout="@layout/gl_root_group"/>
      ----><merge xmlns:android="http://schemas.android.com/apk/res/android">
              <com.android.gallery3d.ui.GLRootView
                      android:id="@+id/gl_root_view"
                      android:layout_width="match_parent"
                      android:layout_height="match_parent"/>
              <View android:id="@+id/gl_root_cover"
                      android:layout_width="match_parent"
                      android:layout_height="match_parent"
                      android:background="@android:color/black"
                      android:visibility="gone"/>
           </merge>
```
指定好新的openGL控件后，就完全交给各个子类实现去控制了


##### Fragment
in main.xml
```xml
<fragment xmlns:android="http://schemas.android.com/apk/res/android"
	android:layout_width="match_parent"
	android:layout_height="match_parent"
  android:name="com.commonsware.android.dfrag.ContentFragment"/>
```


结论：statemanager完全可以用fragmentManager替换
下面就是看图库的openGL绘制框架和Fragment之间的区别

addToBackStack()
