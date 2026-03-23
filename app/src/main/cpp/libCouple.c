#include <jni.h>
#include <stdlib.h>
#include <string.h>

typedef struct {
    int len, cap;
    char *str;
} String;

typedef struct {
    int capacity, count;
    String *a, *b;
} Couple;

static Couple* g_couple = NULL;

void init_string_field(String* s, int initial_cap) {
    s->len = 0;
    s->cap = initial_cap;
    s->str = (char*) malloc(initial_cap);
    if (s->str) s->str[0] = '\0';
}

void ensure_string_capacity(String* s, int required_len) {

    if (s->cap <= required_len) {
        int new_cap = required_len * 2 + 1;
        char* new_str = (char*) realloc(s->str, new_cap);
        if (new_str) {
            s->str = new_str;
            s->cap = new_cap;
        }
    }
}

void free_string_content(String* s) {
    if (s && s->str) {
        free(s->str);
        s->str = NULL;
    }
}

void destroy(){
    if (g_couple != NULL) {

        for (int i = 0; i < g_couple->capacity; i++) {
            free_string_content(&g_couple->a[i]);
            free_string_content(&g_couple->b[i]);
        }

        free(g_couple->a);
        free(g_couple->b);

        free(g_couple);
        g_couple = NULL;
    }
}

JNIEXPORT void JNICALL
Java_sdjini_AirDMM_custom_CoupleQueue_INIT (JNIEnv *env, jobject thiz, jint capacity) {

    if (g_couple != NULL) {
        destroy();
    }

    g_couple = (Couple*) malloc(sizeof(Couple));
    g_couple->capacity = capacity;
    g_couple->count = 0;

    g_couple->a = (String*) malloc(sizeof(String) * capacity);
    g_couple->b = (String*) malloc(sizeof(String) * capacity);

    for (int i = 0; i < capacity; i++) {
        init_string_field(&g_couple->a[i], 64);
        init_string_field(&g_couple->b[i], 64);
    }
}

JNIEXPORT void JNICALL
Java_sdjini_AirDMM_custom_CoupleQueue_add (JNIEnv *env, jobject thiz, jstring strA, jstring strB) {
    if (g_couple == NULL || g_couple->count >= g_couple->capacity) return;

    int idx = g_couple->count;
    String* target_a = &g_couple->a[idx];
    String* target_b = &g_couple->b[idx];

    const char *cstrA = (*env)->GetStringUTFChars(env, strA, NULL);
    if (cstrA) {
        int len = strlen(cstrA);
        ensure_string_capacity(target_a, len + 1);
        strcpy(target_a->str, cstrA);
        target_a->len = len;
        (*env)->ReleaseStringUTFChars(env, strA, cstrA);
    }

    const char *cstrB = (*env)->GetStringUTFChars(env, strB, NULL);
    if (cstrB) {
        int len = strlen(cstrB);
        ensure_string_capacity(target_b, len + 1);
        strcpy(target_b->str, cstrB);
        target_b->len = len;
        (*env)->ReleaseStringUTFChars(env, strB, cstrB);
    }

    g_couple->count++;
}

JNIEXPORT jobjectArray JNICALL
Java_sdjini_AirDMM_custom_CoupleQueue_get (JNIEnv *env, jobject thiz) {

    if (g_couple == NULL || g_couple->count == 0) {
        return NULL;
    }

    String* head_a = &g_couple->a[0];
    String* head_b = &g_couple->b[0];

    jclass stringClass = (*env)->FindClass(env, "java/lang/String");
    jobjectArray result = (*env)->NewObjectArray(env, 2, stringClass, NULL);

    jstring jstrA = (*env)->NewStringUTF(env, head_a->str);
    jstring jstrB = (*env)->NewStringUTF(env, head_b->str);

    (*env)->SetObjectArrayElement(env, result, 0, jstrA);
    (*env)->SetObjectArrayElement(env, result, 1, jstrB);

    int move_size = (g_couple->count - 1) * sizeof(String);

    if (move_size > 0) {
        memmove(g_couple->a, &g_couple->a[1], move_size);
        memmove(g_couple->b, &g_couple->b[1], move_size);
    }

    memset(&g_couple->a[g_couple->count - 1], 0, sizeof(String));
    memset(&g_couple->b[g_couple->count - 1], 0, sizeof(String));

    g_couple->count--;

    return result;
}

JNIEXPORT jboolean JNICALL
Java_sdjini_AirDMM_custom_CoupleQueue_isEmpty (JNIEnv *env, jobject thiz) {
    return (g_couple == NULL || g_couple->count == 0) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL Java_sdjini_AirDMM_custom_CoupleQueue_destroy (JNIEnv *env, jobject thiz) {
    destroy();
}
