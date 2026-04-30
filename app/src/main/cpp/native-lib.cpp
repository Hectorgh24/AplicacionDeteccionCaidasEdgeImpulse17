#include <jni.h>
#include <string>
#include <vector>
#include "edge-impulse-sdk/classifier/ei_run_classifier.h"
#include <android/log.h>

#define TAG "EdgeImpulseNative"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

extern "C" JNIEXPORT jstring JNICALL
Java_com_empresa_aplicacionedgeimpulse17_MainActivity_runClassification(
        JNIEnv *env,
        jobject /* this */,
        jfloatArray features) {

    jfloat *features_array = env->GetFloatArrayElements(features, 0);
    jsize length = env->GetArrayLength(features);

    if (length != EI_CLASSIFIER_DSP_INPUT_FRAME_SIZE) {
        LOGE("Error: El tamaño del array (%d) no coincide con el esperado por el modelo (%d)", length, EI_CLASSIFIER_DSP_INPUT_FRAME_SIZE);
        env->ReleaseFloatArrayElements(features, features_array, 0);
        return env->NewStringUTF("ERROR_SIZE");
    }

    signal_t signal;
    int err = numpy::signal_from_buffer(features_array, length, &signal);
    if (err != 0) {
        LOGE("Error creando la señal: %d", err);
        env->ReleaseFloatArrayElements(features, features_array, 0);
        return env->NewStringUTF("ERROR_SIGNAL");
    }

    ei_impulse_result_t result = { 0 };
    err = run_classifier(&signal, &result, false);

    env->ReleaseFloatArrayElements(features, features_array, 0);

    if (err != EI_IMPULSE_OK) {
        LOGE("Error en la inferencia: %d", err);
        return env->NewStringUTF("ERROR_INFERENCE");
    }

    // Buscar la clase con mayor probabilidad
    float max_value = 0.0;
    std::string max_label = "";

    for (size_t ix = 0; ix < EI_CLASSIFIER_LABEL_COUNT; ix++) {
        LOGI("Clase: %s, Valor: %.5f", result.classification[ix].label, result.classification[ix].value);
        if (result.classification[ix].value > max_value) {
            max_value = result.classification[ix].value;
            max_label = result.classification[ix].label;
        }
    }

    // Retornar la clase dominante y su probabilidad separadas por un pipe (|)
    std::string final_result = max_label + "|" + std::to_string(max_value);
    return env->NewStringUTF(final_result.c_str());
}
