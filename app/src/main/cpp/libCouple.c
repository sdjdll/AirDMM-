#include "couple.h"

typedef struct {
    int len, cap;
    char *str;
} String;

typedef struct {
    int capacity;
    int initial_capacity;
    int count;
    String *a_start, *a_end;
    String *b_start, *b_end;
    String *head_a, *head_b;
    String *tail_a, *tail_b;
} Couple;

static Couple* g_couple = NULL;
static jclass global_string_class = NULL;

// ================= 底层字符串工具 =================

void init_string_field(String* s, int initial_cap) {
    s->len = 0;
    s->cap = initial_cap;
    s->str = (char*) malloc(initial_cap);
    if (s->str) s->str[0] = '\0';
}

int ensure_string_capacity(String* s, int required_len) {
    if (s->cap <= required_len) {
        int new_cap = required_len * 2 + 1;
        char* new_str = (char*) realloc(s->str, new_cap);
        if (!new_str) return 0;
        s->str = new_str;
        s->cap = new_cap;
    }
    return 1;
}

void free_string_content(String* s) {
    if (s && s->str) {
        free(s->str);
        s->str = NULL;
    }
}

void clear_string_content(String* s) {
    if (s && s->str) {
        s->len = 0;
        s->str[0] = '\0';
    }
}

// ================= 内部销毁与引擎 =================

void destroy_c_internal() {
    if (g_couple != NULL) {
        String *p_a = g_couple->a_start;
        String *p_b = g_couple->b_start;
        while (p_a != g_couple->a_end) {
            free_string_content(p_a);
            free_string_content(p_b);
            p_a++;
            p_b++;
        }
        free(g_couple->a_start);
        free(g_couple->b_start);
        free(g_couple);
        g_couple = NULL;
    }
}

int resize_couple(int new_cap) {
    String *new_a = (String*) malloc(sizeof(String) * new_cap);
    String *new_b = (String*) malloc(sizeof(String) * new_cap);
    if (!new_a || !new_b) {
        if (new_a) free(new_a);
        if (new_b) free(new_b);
        return 0;
    }

    int old_head_idx = g_couple->head_a - g_couple->a_start;

    for (int i = 0; i < g_couple->count; i++) {
        int src_idx = (old_head_idx + i) % g_couple->capacity;

        new_a[i] = g_couple->a_start[src_idx];
        g_couple->a_start[src_idx].str = NULL;

        new_b[i] = g_couple->b_start[src_idx];
        g_couple->b_start[src_idx].str = NULL;
    }

    for (int i = g_couple->count; i < new_cap; i++) {
        init_string_field(&new_a[i], 64);
        init_string_field(&new_b[i], 64);
    }

    for (int i = 0; i < g_couple->capacity; i++) {
        free_string_content(&g_couple->a_start[i]);
        free_string_content(&g_couple->b_start[i]);
    }

    free(g_couple->a_start);
    free(g_couple->b_start);

    g_couple->a_start = new_a; g_couple->a_end = new_a + new_cap;
    g_couple->b_start = new_b; g_couple->b_end = new_b + new_cap;
    g_couple->head_a = new_a; g_couple->head_b = new_b;
    g_couple->tail_a = new_a + g_couple->count; g_couple->tail_b = new_b + g_couple->count;
    g_couple->capacity = new_cap;

    return 1;
}

// ================= JNI 接口实现 =================

JNIEXPORT void JNICALL Java_sdjini_AirDMM_custom_CoupleQueue_INIT
        (JNIEnv *env, jobject thiz, jint capacity) {
    if (g_couple != NULL) {
        destroy_c_internal();
    }

    g_couple = (Couple*) malloc(sizeof(Couple));
    g_couple->initial_capacity = capacity;
    g_couple->capacity = capacity;
    g_couple->count = 0;

    g_couple->a_start = (String*) malloc(sizeof(String) * capacity);
    g_couple->b_start = (String*) malloc(sizeof(String) * capacity);

    g_couple->a_end = g_couple->a_start + capacity;
    g_couple->b_end = g_couple->b_start + capacity;

    g_couple->head_a = g_couple->a_start;
    g_couple->head_b = g_couple->b_start;
    g_couple->tail_a = g_couple->a_start;
    g_couple->tail_b = g_couple->b_start;

    for (int i = 0; i < capacity; i++) {
        init_string_field(&g_couple->a_start[i], 64);
        init_string_field(&g_couple->b_start[i], 64);
    }

    if (!global_string_class) {
        jclass local_class = (*env)->FindClass(env, "java/lang/String");
        if (local_class) {
            global_string_class = (*env)->NewGlobalRef(env, local_class);
            (*env)->DeleteLocalRef(env, local_class);
        }
    }
}

JNIEXPORT void JNICALL Java_sdjini_AirDMM_custom_CoupleQueue_add
        (JNIEnv *env, jobject thiz, jstring strA, jstring strB) {
    if (g_couple == NULL) return;

    if (g_couple->count >= g_couple->capacity) {
        if (!resize_couple(g_couple->capacity * 2)) return;
    }

    String* target_a = g_couple->tail_a;
    String* target_b = g_couple->tail_b;

    int lenA = (*env)->GetStringUTFLength(env, strA);
    const char *cstrA = (*env)->GetStringUTFChars(env, strA, NULL);
    int lenB = (*env)->GetStringUTFLength(env, strB);
    const char *cstrB = (*env)->GetStringUTFChars(env, strB, NULL);

    if (!cstrA || !cstrB ||
        !target_a->str || !target_b->str ||
        !ensure_string_capacity(target_a, lenA + 1) ||
        !ensure_string_capacity(target_b, lenB + 1)) {

        if (cstrA) (*env)->ReleaseStringUTFChars(env, strA, cstrA);
        if (cstrB) (*env)->ReleaseStringUTFChars(env, strB, cstrB);
        return;
    }

    memcpy(target_a->str, cstrA, lenA + 1);
    target_a->len = lenA;
    (*env)->ReleaseStringUTFChars(env, strA, cstrA);

    memcpy(target_b->str, cstrB, lenB + 1);
    target_b->len = lenB;
    (*env)->ReleaseStringUTFChars(env, strB, cstrB);

    g_couple->tail_a++;
    g_couple->tail_b++;
    if (g_couple->tail_a == g_couple->a_end) {
        g_couple->tail_a = g_couple->a_start;
        g_couple->tail_b = g_couple->b_start;
    }
    g_couple->count++;
}

JNIEXPORT jobjectArray JNICALL Java_sdjini_AirDMM_custom_CoupleQueue_get
        (JNIEnv *env, jobject thiz) {
    if (g_couple == NULL || g_couple->count == 0) return NULL;

    String* head_a = g_couple->head_a;
    String* head_b = g_couple->head_b;

    if (!head_a->str || !head_b->str) {
        clear_string_content(head_a);
        clear_string_content(head_b);
        g_couple->head_a++; g_couple->head_b++;
        if (g_couple->head_a == g_couple->a_end) {
            g_couple->head_a = g_couple->a_start;
            g_couple->head_b = g_couple->b_start;
        }
        g_couple->count--;
        return NULL;
    }

    jobjectArray result = (*env)->NewObjectArray(env, 2, global_string_class, NULL);
    if (!result) return NULL;

    jstring jstrA = (*env)->NewStringUTF(env, head_a->str);
    jstring jstrB = (*env)->NewStringUTF(env, head_b->str);
    (*env)->SetObjectArrayElement(env, result, 0, jstrA);
    (*env)->SetObjectArrayElement(env, result, 1, jstrB);

    clear_string_content(head_a);
    clear_string_content(head_b);

    g_couple->head_a++;
    g_couple->head_b++;
    if (g_couple->head_a == g_couple->a_end) {
        g_couple->head_a = g_couple->a_start;
        g_couple->head_b = g_couple->b_start;
    }
    g_couple->count--;

    if (g_couple->capacity > g_couple->initial_capacity &&
        g_couple->count < g_couple->capacity / 4) {
        resize_couple(g_couple->capacity / 2);
    }

    return result;
}

JNIEXPORT jboolean JNICALL Java_sdjini_AirDMM_custom_CoupleQueue_isEmpty
        (JNIEnv *env, jobject thiz) {
    return (g_couple == NULL || g_couple->count == 0) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL Java_sdjini_AirDMM_custom_CoupleQueue_destroy
        (JNIEnv *env, jobject thiz) {
    if (global_string_class) {
        (*env)->DeleteGlobalRef(env, global_string_class);
        global_string_class = NULL;
    }
    destroy_c_internal();
}
