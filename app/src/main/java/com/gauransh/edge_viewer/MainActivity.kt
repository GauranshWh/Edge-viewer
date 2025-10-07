package com.gauransh.edge_viewer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.gauransh.edge_viewer.databinding.ActivityMainBinding
import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var cameraService: CameraService
    private var outputBuffer: ByteBuffer? = null
    private lateinit var mainRenderer: MainRenderer

    // For FPS calculation
    private var frameCount = 0
    private var lastFpsTimestamp = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainRenderer = MainRenderer()

        binding.glSurfaceView.apply {
            setEGLContextClientVersion(2)
            setRenderer(mainRenderer)
        }

        cameraService = CameraService(this) { width, height, yBuffer, uBuffer, vBuffer, yStride, uStride, vStride ->
            if (outputBuffer == null) {
                outputBuffer = ByteBuffer.allocateDirect(width * height * 4)
            }

            CVProcessor.processFrame(
                yBuffer, uBuffer, vBuffer,
                yStride, uStride, vStride,
                width, height,
                outputBuffer!!
            )

            mainRenderer.updateFrame(width, height, outputBuffer!!)
            binding.glSurfaceView.requestRender()

            calculateFps(width, height)
        }

        if (allPermissionsGranted()) {
            startCameraWithViewSize()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
    }

    private fun calculateFps(width: Int, height: Int) {
        frameCount++
        val currentTime = System.nanoTime()
        if (lastFpsTimestamp == 0L) {
            lastFpsTimestamp = currentTime
        }
        val elapsedTime = currentTime - lastFpsTimestamp
        if (elapsedTime >= TimeUnit.SECONDS.toNanos(1)) {
            val fps = frameCount / (elapsedTime / 1_000_000_000.0)
            val statsText = "$width x $height @ ${"%.1f".format(fps)} FPS"

            runOnUiThread {
                binding.statsTextView.text = statsText
            }

            frameCount = 0
            lastFpsTimestamp = currentTime
        }
    }

    private fun startCameraWithViewSize() {
        binding.glSurfaceView.post {
            cameraService.startCamera(binding.glSurfaceView.width, binding.glSurfaceView.height)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCameraWithViewSize()
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    external fun stringFromJNI(): String

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

        init {
            System.loadLibrary("edge_viewer")
        }
    }
}