#include <jni.h>
#include <string>
#include <algorithm>


int strFilter(const std::string& raw, char* filterArray, int filterSize);
std::string jStr2Str(JNIEnv* env,jstring jStr);
std::string clean_cpp_string(const std::string& str);

extern "C" __attribute__((unused)) JNIEXPORT jboolean JNICALL
Java_sdjini_AirDMM_Notify_filter(JNIEnv* env,jobject,jstring raw,jstring keys) {

    if (raw == nullptr || keys == nullptr) return JNI_FALSE;

    const char *rawChars = env->GetStringUTFChars(raw, nullptr);
    const char *keysChars = env->GetStringUTFChars(keys, nullptr);

    if (rawChars == nullptr || keysChars == nullptr) {
        if (rawChars) env->ReleaseStringUTFChars(raw, rawChars);
        if (keysChars) env->ReleaseStringUTFChars(keys, keysChars);
        return JNI_FALSE;
    }

    std::string rawStr(rawChars);
    std::string keysStr(keysChars);

    std::transform(rawStr.begin(), rawStr.end(), rawStr.begin(), ::tolower);
    std::transform(keysStr.begin(), keysStr.end(), keysStr.begin(), ::tolower);

    env->ReleaseStringUTFChars(raw, rawChars);
    env->ReleaseStringUTFChars(keys, keysChars);

    std::string delimiter = ",";
    size_t start = 0;
    size_t end = keysStr.find(delimiter);

    while (end != std::string::npos) {
        std::string token = keysStr.substr(start, end - start);

        token.erase(0, token.find_first_not_of(" \t\r\n"));
        token.erase(token.find_last_not_of(" \t\r\n") + 1);

        if (!token.empty() && rawStr.find(token) != std::string::npos) return JNI_TRUE;

        start = end + delimiter.length();
        end = keysStr.find(delimiter, start);
    }

    std::string lastToken = keysStr.substr(start);
    lastToken.erase(0, lastToken.find_first_not_of(" \t\r\n"));
    lastToken.erase(lastToken.find_last_not_of(" \t\r\n") + 1);

    if (!lastToken.empty() && rawStr.find(lastToken) != std::string::npos) return JNI_TRUE;

    return JNI_FALSE;
}

extern "C" JNIEXPORT jstring JNICALL
Java_sdjini_AirDMM_Notify_perfectingString(JNIEnv* env, jclass clazz, jstring string) {
    if (string == nullptr) {
        return env->NewStringUTF("");
    }
    std::string cppStr = jStr2Str(env, string);
    std::string cleanStr = clean_cpp_string(cppStr);

    return env->NewStringUTF(cleanStr.c_str());
}

int strFilter(const std::string* raw, const char* filterArray,const int filterSize){
    for (int i = 0;i < filterSize; i++,filterArray++)
        if (raw->find(*filterArray)) return i;
    return -1;
}
std::string clean_cpp_string(const std::string& str) {
    const std::string BLACKLIST_CHARS =
            "\uFEFE\u2028\u2029"
            "\u007f"
            "\u0080\u0081\u0082\u0083\u0084\u0085\u0086\u0087"
            "\u0088\u0089\u008a\u008b\u008c\u008d\u008e\u008f"
            "\u0090\u0091\u0092\u0093\u0094\u0095\u0096\u0097"
            "\u0098\u0099\u009a\u009b\u009c\u009d\u009e\u0099f"
            "\u0008\u000c";
    std::string result;
    result.reserve(str.size());
    for (char c : str)
        if (BLACKLIST_CHARS.find(c) == std::string::npos) result += c;
    return result;
}
std::string jStr2Str(JNIEnv* env, jstring jStr) {
    if (jStr == nullptr) return "";
    jsize len = env->GetStringUTFLength(jStr);
    std::string result(len, '\0');
    env->GetStringUTFRegion(jStr, 0, len, &result[0]);
    return result;
}