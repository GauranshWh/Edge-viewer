package com.gauransh.edge_viewer

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import android.view.TextureView

/**
 * Manages all camera operations in a separate class to keep MainActivity clean.
 * This class handles opening the camera, creating a preview session, and closing the camera.
 */
class CameraService(private val context: Context, private val textureView: TextureView) {

    private val cameraManager: CameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null

    // A background thread to prevent blocking the UI while doing camera work.
    private val cameraThread = HandlerThread("CameraThread").apply { start() }
    private val cameraHandler = Handler(cameraThread.looper)

    /**
     * Starts the camera process. It waits for the TextureView to be ready, then opens the camera.
     */
    fun startCamera() {
        if (textureView.isAvailable) {
            openCamera()
        } else {
            textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                    openCamera()
                }
                override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}
                override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean = true
                override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
            }
        }
    }

    /**
     * Opens a connection to the first available camera.
     * The result is handled by the deviceStateCallback.
     */
    @SuppressLint("MissingPermission")
    private fun openCamera() {
        try {
            val cameraId = cameraManager.cameraIdList[0] // Default to the rear camera
            cameraManager.openCamera(cameraId, deviceStateCallback, cameraHandler)
        } catch (e: CameraAccessException) {
            Log.e(TAG, "Cannot access the camera.", e)
        }
    }

    /**
     * Callback for camera device state changes (opened, disconnected, error).
     */
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

    /**
     * Creates a new camera capture session for the preview.
     */
    private fun createCaptureSession() {
        try {
            val texture = textureView.surfaceTexture ?: return
            // We'll use a standard preview size. This can be configured more dynamically later.
            texture.setDefaultBufferSize(1920, 1080)
            val surface = Surface(texture)

            // Create a request builder for a repeating preview request.
            val captureRequestBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            captureRequestBuilder.addTarget(surface)

            // Create the session. The result is handled by the session state callback.
            cameraDevice?.createCaptureSession(listOf(surface), object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    captureSession = session
                    captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
                    try {
                        // Start the continuous preview.
                        session.setRepeatingRequest(captureRequestBuilder.build(), null, cameraHandler)
                    } catch (e: CameraAccessException) {
                        Log.e(TAG, "Failed to start camera preview.", e)
                    }
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {
                    Log.e(TAG, "Failed to configure camera session.")
                }
            }, cameraHandler)
        } catch (e: CameraAccessException) {
            Log.e(TAG, "Failed to create capture session.", e)
        }
    }

    /**
     * Closes camera resources to release them for other apps.
     */
    fun closeCamera() {
        captureSession?.close()
        captureSession = null
        cameraDevice?.close()
        cameraDevice = null
    }

    companion object {
        private const val TAG = "CameraService"
    }
}

