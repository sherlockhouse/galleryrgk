filtershow_filters 不用了

两种jni函数定义方法
#### <font color=#bd2424 >定义一个宏展开式</font>
```c++

#define JNIFUNCF(cls, name, vars...) Java_com_android_gallery3d_filtershow_filters_ ## cls ## _ ## name(JNIEnv* env, jobject obj, vars)
```
#### <font color=#bd2424 >在JNI_OnLoad中动态加载</font>
```c

static JNINativeMethod methods[] = {
  {"imageFilterBwFilter", "(Ljava/lang/Object;[BIIIII)Ljava/lang/Object;", (void*)imageFilterBwFilter},
  {"imageFilterKMeans", "(Ljava/lang/Object;[BIILjava/lang/Object;IILjava/lang/Object;IIII)Ljava/lang/Object;",
          (void*)imageFilterKMeans},
  {"getFancyColorEffects", "()[Ljava/lang/Object;", (void*)getFancyColorEffects},
  {"getFancyColorEffectsCount", "()I", (void*)getFancyColorEffectsCount},
  {"getFancyColorEffectImage", "(ILjava/lang/Object;[BIILjava/lang/Object;II)Ljava/lang/Object;",
          (void*)getFancyColorEffectImage},
  {"nativeResizeMask", "([BIIII)[B", (void*)resizeMask},
};

/*
 * Register several native methods for one class.
 */
static int registerNativeMethods(JNIEnv* env, const char* className, JNINativeMethod* gMethods, int numMethods) {
    jclass clazz;

    clazz = env->FindClass(className);
    if (clazz == NULL) {
        ALOGE("Native registration unable to find class '%s'", className);
        return JNI_FALSE;
    }
    if (env->RegisterNatives(clazz, gMethods, numMethods) < 0) {
        ALOGE("RegisterNatives failed for '%s'", className);
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

/*
 * Register native methods for all classes we know about.
 *
 * returns JNI_TRUE on success.
 */
static int registerNatives(JNIEnv* env) {
    if (!registerNativeMethods(env, classPathName, methods, sizeof(methods) / sizeof(methods[0]))) {
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    UnionJNIEnvToVoid uenv;
    uenv.venv = NULL;
    jint result = -1;
    JNIEnv* env = NULL;

    ALOGI("JNI_OnLoad");

    if (vm->GetEnv(&uenv.venv, JNI_VERSION_1_4) != JNI_OK) {
        ALOGE("ERROR: GetEnv failed");
        goto bail;
    }
    env = uenv.env;

    if (registerNatives(env) != JNI_TRUE) {
        ALOGE("ERROR: registerNatives failed");
        goto bail;
    }

    result = JNI_VERSION_1_4;

    bail: return result;
}
```
