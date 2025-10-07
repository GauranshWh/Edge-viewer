#include <jni.h>
#include <string>

// We need to include the OpenCV headers
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

    // 1. Get direct access to the incoming pixel data
    auto y_data = static_cast<uint8_t*>(env->GetDirectBufferAddress(y_buffer));
    auto u_data = static_cast<uint8_t*>(env->GetDirectBufferAddress(u_buffer));
    auto v_data = static_cast<uint8_t*>(env->GetDirectBufferAddress(v_buffer));
    auto output_data = static_cast<uint8_t*>(env->GetDirectBufferAddress(output_buffer));

    // 2. Wrap the raw YUV data in OpenCV Mats without copying data
    cv::Mat y_mat(height, width, CV_8UC1, y_data, y_stride);
    // The U and V planes are half the size of the Y plane (for YUV_420_888 format)
    cv::Mat u_mat(height / 2, width / 2, CV_8UC1, u_data, u_stride);
    cv::Mat v_mat(height / 2, width / 2, CV_8UC1, v_data, v_stride);

    // 3. Create a temporary Mat for the combined YUV data
    cv::Mat yuv_mat;
    // This is a placeholder for the YUV to RGBA conversion. The actual conversion is more complex.
    // For now, let's just make a grayscale image from the Y plane to prove it works.
    cv::Mat gray_mat = y_mat;

    // 4. Create an RGBA Mat that points to our output buffer
    cv::Mat rgba_mat(height, width, CV_8UC4, output_data);

    // 5. Convert the grayscale image to RGBA and write it to the output buffer
    cv::cvtColor(gray_mat, rgba_mat, cv::COLOR_GRAY2RGBA);

}
