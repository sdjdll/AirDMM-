#include <jni.h>
#include <string>
#include <algorithm>

bool isSpecialInvisibleControlChar(char c);

extern "C" JNIEXPORT jboolean JNICALL
Java_sdjini_AirDMM_Notify_filter(JNIEnv* env, jobject, jstring raw, jstring keys) {

    if (raw == nullptr || keys == nullptr) return JNI_FALSE;

    const char *rawChars = env->GetStringUTFChars(raw, nullptr);
    if (rawChars == nullptr) {
        return JNI_FALSE;
    }

    const char *keysChars = env->GetStringUTFChars(keys, nullptr);
    if (keysChars == nullptr) {
        env->ReleaseStringUTFChars(raw, rawChars);
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
Java_sdjini_AirDMM_Notify_perfectingString(JNIEnv *env,jclass,jstring input) {
    if (input == nullptr) return nullptr;
    const char *nativeStr = env->GetStringUTFChars(input, nullptr);
    jsize len = env->GetStringUTFLength(input);

    if (nativeStr == nullptr || len == 0) {
        env->ReleaseStringUTFChars(input, nativeStr);
        return input;
    }

    std::string result;
    result.reserve(len);

    bool lastWasSpecial = false;

    for (int i = 0; i < len; ) {
        unsigned char c = static_cast<unsigned char>(nativeStr[i]);

        if (c > 127) {
            int charLen = 0;
            if ((c & 0xE0) == 0xC0) charLen = 2;
            else if ((c & 0xF0) == 0xE0) charLen = 3;
            else if ((c & 0xF8) == 0xF0) charLen = 4;
            else charLen = 1;

            if (i + charLen <= len) {
                result.append(nativeStr + i, charLen);
                i += charLen;
                lastWasSpecial = false;
            } else {
                i++;
            }
        } else {
            if (isSpecialInvisibleControlChar(c)) {
                if (!lastWasSpecial) {
                    result.append(" ");
                    lastWasSpecial = true;
                }
            } else {
                result.push_back(c);
                lastWasSpecial = false;
            }
            i++;
        }
    }

    env->ReleaseStringUTFChars(input, nativeStr);
    return env->NewStringUTF(result.c_str());
}

bool isSpecialInvisibleControlChar(char c) {
    if (c < 0 || c > 31) {
        return false;
    }
    if (c == 9 || c == 10 || c == 13) {
        return false;
    }

    return true;
}