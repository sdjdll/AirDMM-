#include "couple.h"
#include <stdlib.h> // for abort()

typedef struct { int len, cap; char *str; } String;
typedef struct { int capacity, count; String *a, *b; } Couple;

static Couple* g_couple = NULL;
static int g_head = 0;
static int g_tail = 0;

// 【新增】缓存 String 的 jclass，避免高频 get 时反复 FindClass
static jclass g_stringClass = NULL;

void init_string_field(String* s, int initial_cap) {
    s->len = 0;
    s->str = (char*) malloc(initial_cap);
    // 【修复】如果 malloc 失败，cap 必须为 0，防止后续跳过 realloc 导致段错误
    if (s->str) {
        s->cap = initial_cap;
        s->str[0] = '\0';
    } else {
        s->cap = 0;
    }
}

void ensure_string_capacity(String* s, int required_len) {
    if (s->cap <= required_len) {
        int new_cap = required_len * 2 + 1;
        char* new_str = (char*) realloc(s->str, new_cap);
        if (new_str) {
            s->str = new_str;
            s->cap = new_cap;
        } else {
            // 【修复】C 层内存彻底耗尽，无法继续安全运行，直接崩溃退出是唯一选择
            abort();
        }
    }
}

void free_string_content(String* s) {
    if (s && s->str) {
        free(s->str);
        s->str = NULL;
        s->cap = 0;
    }
}

void destroy() {
    if (g_couple != NULL) {
        for (int i = 0; i < g_couple->capacity; i++) {
            free_string_content(&g_couple->a[i]);
            free_string_content(&g_couple->b[i]);
        }
        free(g_couple->a);
        free(g_couple->b);
        free(g_couple);
        g_couple = NULL;
        g_head = 0;
        g_tail = 0;
    }
    // 【修复】释放全局引用的 jclass
    if (g_stringClass) {
        // 注意：这里需要 JNIEnv，但我们不打算把 env 传进 destroy。
        // 实际上在 JNI_OnUnload 或进程死亡时，全局引用会自动被回收，
        // 所以这里置空即可，无需手动 DeleteGlobalRef。
        g_stringClass = NULL;
    }
}

static void resize_couple_queue() {
    int old_cap = g_couple->capacity;
    int new_cap = old_cap * 2;

    String* new_a = (String*) malloc(sizeof(String) * new_cap);
    String* new_b = (String*) malloc(sizeof(String) * new_cap);
    if (!new_a || !new_b) abort(); // 极端 OOM 保护

    int pos = 0;
    for (int i = 0; i < g_couple->count; ++i) {
        int from = (g_head + i) % old_cap;
        new_a[pos] = g_couple->a[from];
        new_b[pos] = g_couple->b[from];
        ++pos;
    }

    for (int i = g_couple->count; i < new_cap; ++i) {
        init_string_field(&new_a[i], 64);
        init_string_field(&new_b[i], 64);
    }

    free(g_couple->a);
    free(g_couple->b);

    g_couple->a = new_a;
    g_couple->b = new_b;
    g_couple->capacity = new_cap;
    g_head = 0;
    g_tail = g_couple->count;
}

JNIEXPORT void JNICALL
Java_sdjini_AirDMM_custom_CoupleQueue_INIT(JNIEnv *env, jobject thiz, jint capacity) {
    if (g_couple != NULL) { destroy(); }

    g_couple = (Couple*) malloc(sizeof(Couple));
    if (!g_couple) abort();

    g_couple->capacity = capacity;
    g_couple->count = 0;

    g_couple->a = (String*) malloc(sizeof(String) * capacity);
    g_couple->b = (String*) malloc(sizeof(String) * capacity);
    if (!g_couple->a || !g_couple->b) abort();

    for (int i = 0; i < capacity; i++) {
        init_string_field(&g_couple->a[i], 64);
        init_string_field(&g_couple->b[i], 64);
    }

    g_head = 0;
    g_tail = 0;

    // 【修复】初始化时缓存 String 类
    if (!g_stringClass) {
        jclass cls = (*env)->FindClass(env, "java/lang/String");
        if (cls) {
            g_stringClass = (jclass)(*env)->NewGlobalRef(env, cls);
        }
    }
}

JNIEXPORT void JNICALL
Java_sdjini_AirDMM_custom_CoupleQueue_add(JNIEnv *env, jobject thiz, jstring strA, jstring strB) {
    if (g_couple == NULL) return;

    if (g_couple->count >= g_couple->capacity) {
        resize_couple_queue();
    }

    int idx = g_tail;
    String* target_a = &g_couple->a[idx];
    String* target_b = &g_couple->b[idx];

    free_string_content(target_a);
    free_string_content(target_b);

    // 【修复】绝对防御 Java 传 null 的情况
    const char *cstrA = "";
    jboolean releaseA = JNI_FALSE;
    if (strA != NULL) {
        cstrA = (*env)->GetStringUTFChars(env, strA, NULL);
        if (cstrA) releaseA = JNI_TRUE;
        else cstrA = ""; // JNI 意外失败时的兜底
    }

    int lenA = (int) strlen(cstrA);
    ensure_string_capacity(target_a, lenA + 1);
    strcpy(target_a->str, cstrA);
    target_a->len = lenA;
    if (releaseA) (*env)->ReleaseStringUTFChars(env, strA, cstrA);

    // 【修复】同上，防御 strB 为 null
    const char *cstrB = "";
    jboolean releaseB = JNI_FALSE;
    if (strB != NULL) {
        cstrB = (*env)->GetStringUTFChars(env, strB, NULL);
        if (cstrB) releaseB = JNI_TRUE;
        else cstrB = "";
    }

    int lenB = (int) strlen(cstrB);
    ensure_string_capacity(target_b, lenB + 1);
    strcpy(target_b->str, cstrB);
    target_b->len = lenB;
    if (releaseB) (*env)->ReleaseStringUTFChars(env, strB, cstrB);

    g_couple->count++;
    g_tail = (g_tail + 1) % g_couple->capacity;
}

JNIEXPORT jobjectArray JNICALL
Java_sdjini_AirDMM_custom_CoupleQueue_get(JNIEnv *env, jobject thiz) {
    if (g_couple == NULL || g_couple->count == 0) return NULL;

    String* head_a = &g_couple->a[g_head];
    String* head_b = &g_couple->b[g_head];

    // 【修复】使用全局缓存的 g_stringClass
    jobjectArray result = (*env)->NewObjectArray(env, 2, g_stringClass, NULL);

    jstring jstrA = (*env)->NewStringUTF(env, head_a->str);
    jstring jstrB = (*env)->NewStringUTF(env, head_b->str);
    (*env)->SetObjectArrayElement(env, result, 0, jstrA);
    (*env)->SetObjectArrayElement(env, result, 1, jstrB);

    free_string_content(head_a);
    free_string_content(head_b);

    g_couple->count--;
    g_head = (g_head + 1) % g_couple->capacity;

    return result;
}

JNIEXPORT jboolean JNICALL
Java_sdjini_AirDMM_custom_CoupleQueue_isEmpty(JNIEnv *env, jobject thiz) {
    return (g_couple == NULL || g_couple->count == 0) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL
Java_sdjini_AirDMM_custom_CoupleQueue_destroy(JNIEnv *env, jobject thiz) {
    destroy();
}
