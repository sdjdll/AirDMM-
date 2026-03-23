#include <jni.h>
#include <stdlib.h>
#include <string.h>

JNIEXPORT jstring JNICALL
Java_sdjini_AirDMM_service_Notify_stringFormat(JNIEnv *env, jobject thiz, jstring raw) {    if (raw == NULL) {
        return NULL;
    }

    // 1. 获取UTF-8字符串
    // 注意：GetStringUTFChars 返回的是 const char*，直接修改可能导致崩溃或数据损坏
    // 为了安全地进行原地修改，我们需要将其复制一份到可写的内存区域
    const char *rawCStr = (*env)->GetStringUTFChars(env, raw, NULL);
    if (rawCStr == NULL) {
        return NULL; // 内存分配失败
    }

    // 分配内存拷贝字符串，以便修改
    char *array = strdup(rawCStr);
    (*env)->ReleaseStringUTFChars(env, raw, rawCStr); // 尽快释放原始JVM内存

    if (array == NULL) {
        return NULL; // 内存分配失败
    }

    char *a, *b, *c;
    a = b = c = array; // 初始化指针指向头部

    // 主循环
    while (*a != '\0') {
        // --- 步骤 1 ---
        // 向右移动 a，直到遇到“不可见字符”（这里定义为空格）
        // 排除 \t, \n, \0，即遇到 \t \n \0 视为“可见”继续跳过
        while (*a != '\0' && *a != ' ') {
            a++;
        }

        // 如果 a 指向了字符串结束符，说明后面没有空格了，任务完成
        if (*a == '\0') {
            break;
        }

        // --- 步骤 2 ---
        // 此时 a 指向空格。移动 b 到 a，然后向右移动 b 至第一个“可见字符”
        b = a;

        // 这里的“可见字符”指非空格（包括 \t, \n, \0 都视为可见保留）
        while (*b != '\0' && *b == ' ') {
            b++;
        }

        // --- 步骤 3 ---
        // 移动 c 到 a，然后删除 [a, b-1] 范围内的字符（即删除这段空格）
        // “删除”操作通过内存移动实现：将 b 之后的内容搬运到 a 的位置
        c = a;

        // 计算需要搬运的长度：从 b 到结尾（包含 \0）
        size_t len = strlen(b) + 1;

        // 使用 memmove 进行内存搬运（memmove 允许源和目标内存重叠）
        memmove(c, b, len);

        // --- 步骤 4 ---
        // 此时，原先 b 指向的内容已经被搬运到了 a 的位置。
        // 字符串整体缩短了。
        // 你提到的“移动 a 到 b”：
        // 在原地搬运后，逻辑上 a 现在指向了之前 b 对应的字符。
        // 我们不需要显式赋值 a = b (因为 b 的位置已经变了)，
        // 下一次循环会继续检查当前 a 指向的字符（即之前 b 的字符）是否符合条件。
        // 如果是 \t 或 \n，步骤1会跳过；如果是字母，步骤1也会跳过。
        // 如果 a 现在指向 \0，循环将在下一轮顶部结束。
    }

    // 构造新的 Java 字符串返回
    jstring result = (*env)->NewStringUTF(env, array);

    free(array);

    return result;
}