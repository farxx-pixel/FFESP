#include <jni.h>

extern "C" JNIEXPORT jfloatArray JNICALL
Java_com_esp_ffindia_NativeBridge_worldToScreen(
        JNIEnv* env, jclass,
        jfloatArray matrixArr,
        jfloat x, jfloat y, jfloat z,
        jint width, jint height) {

    jfloat* m = env->GetFloatArrayElements(matrixArr, nullptr);
    float w = m[3]*x + m[7]*y + m[11]*z + m[15];
    if (w < 0.1f) {
        env->ReleaseFloatArrayElements(matrixArr, m, 0);
        return nullptr;
    }
    float inv = 1.0f / w;
    float sx = (m[0]*x + m[4]*y + m[8]*z + m[12]) * inv;
    float sy = (m[1]*x + m[5]*y + m[9]*z + m[13]) * inv;

    jfloatArray result = env->NewFloatArray(2);
    jfloat out[2] = {
        (width / 2.0f) * (1.0f + sx),
        (height / 2.0f) * (1.0f - sy)
    };
    env->SetFloatArrayRegion(result, 0, 2, out);
    env->ReleaseFloatArrayElements(matrixArr, m, 0);
    return result;
}
