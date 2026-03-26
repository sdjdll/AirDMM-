#include <jni.h>
#include <stdlib.h>

#define DEFAULT_ADD 1;
#define DEFAULT_SUB 1;

static jfieldID lengthFid,sizeFid,elementsFid;
//static jint length,size;


JNIEXPORT void JNICALL
Java_sdjini_AirDMM_custom_Queue_INIT(JNIEnv* env,jobject thiz){
    lengthFid = (*env)->GetFieldID(env, thiz, "length", "I"),
    sizeFid = (*env)->GetFieldID(env, thiz, "size", "I"),
    elementsFid = (*env)->GetFieldID(env, thiz, "elements", "[Lsdjini/AirDMM/custom/Element;");
}
///Working
JNIEXPORT jboolean JNICALL
Java_sdjini_AirDMM_custom_Queue_add(JNIEnv* env,jobject thiz,jobject element){
    jclass clazz = (*env)->GetObjectClass(env, thiz);

    jint length = (*env)->GetIntField(env, thiz, lengthFid),
    size = (*env)->GetIntField(env, thiz, sizeFid);
    jobjectArray old = (jobjectArray)(*env)->GetObjectField(env, thiz, elementsFid);

    size += size <= length ? 0 : DEFAULT_ADD;

    return JNI_TRUE;
}

JNIEXPORT jobject JNICALL
Java_sdjini_AirDMM_custom_Queue_get(JNIEnv* env, jobject thiz){
    jobjectArray old = (jobjectArray)(*env)->GetObjectField(env, thiz, elementsFid);
}