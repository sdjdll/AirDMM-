#include <jni.h>
#include <stdlib.h>
#include <string.h>

#ifndef _Included_sdjini_AirDMM_custom_CoupleQueue
#ifdef __cplusplus
#endif

JNIEXPORT void JNICALL Java_sdjini_AirDMM_custom_CoupleQueue_INIT
        (JNIEnv *, jobject, jint);

JNIEXPORT void JNICALL Java_sdjini_AirDMM_custom_CoupleQueue_add
        (JNIEnv *, jobject, jstring, jstring);

JNIEXPORT jstring JNICALL Java_sdjini_AirDMM_custom_CoupleQueue_get
        (JNIEnv *, jobject);

JNIEXPORT jboolean JNICALL Java_sdjini_AirDMM_custom_CoupleQueue_isEmpty
        (JNIEnv *, jobject);

JNIEXPORT void JNICALL Java_sdjini_AirDMM_custom_CoupleQueue_destroy
        (JNIEnv *, jobject);

#ifdef __cplusplus
#endif
#endif