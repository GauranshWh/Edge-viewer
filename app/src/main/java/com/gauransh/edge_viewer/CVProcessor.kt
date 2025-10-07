package com.gauransh.edge_viewer

import java.nio.ByteBuffer

/**
 * This class holds the JNI (Java Native Interface) method declarations.
 * The actual implementation of these methods is in the native C++ code.
 */
object CVProcessor {

    /**
     * A native method that will receive a camera frame and process it using OpenCV.
     *
     * @param yBuffer The direct ByteBuffer for the Y (luminance) plane.
     * @param uBuffer The direct ByteBuffer for the U (chrominance) plane.
     * @param vBuffer The direct ByteBuffer for the V (chrominance) plane.
     * @param yStride The row stride for the Y plane in bytes.
     * @param uStride The row stride for the U plane in bytes.
     * @param vStride The row stride for the V plane in bytes.
     * @param width The width of the image.
     * @param height The height of the image.
     * @param outputBuffer The direct ByteBuffer where the processed RGBA frame will be written.
     */
    external fun processFrame(
        yBuffer: ByteBuffer,
        uBuffer: ByteBuffer,
        vBuffer: ByteBuffer,
        yStride: Int,
        uStride: Int,
        vStride: Int,
        width: Int,
        height: Int,
        outputBuffer: ByteBuffer
    )

    // Load the native library when this object is initialized.
    init {
        System.loadLibrary("edge_viewer")
    }
}
