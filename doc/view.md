## Fragment vs openGL
***
```java
public class Fragment implements ComponentCallbacks2, OnCreateContextMenuListener {
```

 * The set of callback APIs that are common to all application components
 * ({@link android.app.Activity}, {@link android.app.Service},
 * {@link ContentProvider}, and {@link android.app.Application}).
 *
 * <p class="note"><strong>Note:</strong> You should also implement the {@link
 * ComponentCallbacks2} interface, which provides the {@link
 * ComponentCallbacks2#onTrimMemory} callback to help your app manage its memory usage more
 * effectively.</p>

```java
public interface ComponentCallbacks {
```

 * Interface definition for a callback to be invoked when the context menu
 * for this view is being built.

```java
public interface OnCreateContextMenuListener {
```
