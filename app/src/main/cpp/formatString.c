#include <jni.h>
#include <stdlib.h>
#include <string.h>

int is_invisible(char c);

JNIEXPORT jstring JNICALL
Java_sdjini_AirDMM_service_Notify_stringFormat(JNIEnv *env, jobject thiz, jstring raw) {
    if (raw == NULL) return NULL;

    const char *src = (*env)->GetStringUTFChars(env, raw, NULL);
    if (src == NULL) return NULL;

    size_t len = strlen(src);
    char *dest = (char *)malloc(len + 1);
    if (dest == NULL) {
        (*env)->ReleaseStringUTFChars(env, raw, src);
        return NULL;
    }

    const char *a = src;
    char *c = dest;

    while (*a != '\0') {
        while (*a != '\0' && !is_invisible(*a)) *c++ = *a++;


        if (*a == '\0') break;

        const char *b = a;
        while (*b != '\0' && is_invisible(*b)) b++;

        if(b>a) *c++ = ' ';

        a = b;
    }

    *c = '\0';

    jstring result = (*env)->NewStringUTF(env, dest);

    free(dest);
    (*env)->ReleaseStringUTFChars(env, raw, src);

    return result;
}

int is_invisible(char c) {
    if (c == '\t' || c == '\n' || c == '\0') return 0;

    unsigned char uc = (unsigned char)c;
    if (uc <= 32 || uc == 127) return 1;

    return 0;
}