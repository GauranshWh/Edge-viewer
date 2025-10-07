#include <jni.h>
#include <string>

#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>

extern "C" JNIEXPORT void JNICALL
Java_com_gauransh_edge_1viewer_CVProcessor_processFrame(
        JNIEnv* env,
        jobject /* this */,
        jobject y_buffer,
        jobject u_buffer,
        jobject v_buffer,
        jint y_stride,
        jint u_stride,
        jint v_stride,
        jint width,
        jint height,
        jobject output_buffer) {

    auto y_data = static_cast<uint8_t*>(env->GetDirectBufferAddress(y_buffer));
    auto output_data = static_cast<uint8_t*>(env->GetDirectBufferAddress(output_buffer));

    // Wrap the Y plane (grayscale) in a Mat
    cv::Mat gray_mat(height, width, CV_8UC1, y_data, y_stride);

    // *** CHANGE IS HERE: Apply Canny edge detection ***
    // The numbers 50 and 150 are the low and high thresholds for the edge detection.
    cv::Canny(gray_mat, gray_mat, 50, 150);

    // Create an RGBA Mat that points to our output buffer
    cv::Mat rgba_mat(height, width, CV_8UC4, output_data);

    // Convert the single-channel Canny output (black and white) to a 4-channel RGBA image
    cv::cvtColor(gray_mat, rgba_mat, cv::COLOR_GRAY2RGBA);
}