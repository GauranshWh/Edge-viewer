package com.gauransh.edge_viewer

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface

class CameraService(
    context: Context,
    private val frameListener: (width: Int, height: Int, yBuffer: java.nio.ByteBuffer, uBuffer: java.nio.ByteBuffer, vBuffer: java.nio.ByteBuffer, yStride: Int, uStride: Int, vStride: Int) -> Unit
) {

    private val cameraManager: CameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null

    private val cameraThread = HandlerThread("CameraThread").apply { start() }
    private val cameraHandler = Handler(cameraThread.looper)

    private lateinit var imageReader: ImageReader

    @SuppressLint("MissingPermission")
    fun startCamera(width: Int, height: Int) {
        // Create an ImageReader to capture frames
        imageReader = ImageReader.newInstance(width, height, ImageFormat.YUV_420_888, 2)
        imageReader.setOnImageAvailableListener({ reader ->
            val image = reader.acquireLatestImage() ?: return@setOnImageAvailableListener
            // Send the raw plane data to our listener (MainActivity)
            val planes = image.planes
            frameListener(
                image.width, image.height,
                planes[0].buffer, planes[1].buffer, planes[2].buffer,
                planes[0].rowStride, planes[1].rowStride, planes[2].rowStride
            )
            image.close()
        }, cameraHandler)

        openCamera()
    }

    @SuppressLint("MissingPermission")
    private fun openCamera() {
        try {
            val cameraId = cameraManager.cameraIdList[0] // Default to the rear camera
            cameraManager.openCamera(cameraId, deviceStateCallback, cameraHandler)
        } catch (e: CameraAccessException) {
            Log.e(TAG, "Cannot access the camera.", e)
        }
    }

    private val deviceStateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            cameraDevice = camera
            createCaptureSession()
        }
        override fun onDisconnected(camera: CameraDevice) {
            camera.close()
            cameraDevice = null
        }
        override fun onError(camera: CameraDevice, error: Int) {
            camera.close()
            cameraDevice = null
            Log.e(TAG, "Camera device error: $error")
        }
    }

    private fun createCaptureSession() {
        try {
            // The target for the camera is now the ImageReader's surface
            val surface = imageReader.surface
            val captureRequestBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            captureRequestBuilder.addTarget(surface)

            cameraDevice?.createCaptureSession(listOf(surface), object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    captureSession = session
                    captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
                    session.setRepeatingRequest(captureRequestBuilder.build(), null, cameraHandler)
                }
                override fun onConfigureFailed(session: CameraCaptureSession) {
                    Log.e(TAG, "Failed to configure camera session.")
                }
            }, cameraHandler)
        } catch (e: CameraAccessException) {
            Log.e(TAG, "Failed to create capture session.", e)
        }
    }

    fun closeCamera() {
        captureSession?.close()
        cameraDevice?.close()
        imageReader.close()
    }

    companion object {
        private const val TAG = "CameraService"
    }
}