package com.example.myapplication.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.view.TextureView.SurfaceTextureListener
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.R
import com.example.myapplication.ml.LiteModelSsdMobilenetV11Metadata2
import com.example.myapplication.viewmodel.RearCameraViewModel
import com.example.myapplication.viewmodel.RearCameraViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp


class MainActivity : AppCompatActivity() {
    val permission = arrayOf("android.car.permission.CAR_VENDOR_EXTENSION")
    val steeringPermissionCode = 200
    private val CAMERA_PERMISSION_CODE = 100
    private lateinit var guideLinesView: GuideLinesView
    private lateinit var model: LiteModelSsdMobilenetV11Metadata2
    private lateinit var imageProcessor: ImageProcessor
    private lateinit var textureView: TextureView
    private lateinit var rearCameraViewModel: RearCameraViewModel
    private lateinit var videoView: TextureView
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var modelView : ModelView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        guideLinesView = findViewById(R.id.guideLinesView)
        guideLinesView.visibility = View.GONE
        textureView =findViewById(R.id.textureView)
        videoView = findViewById(R.id.videoView)

        modelView = findViewById(R.id.model)
        modelView.layout = findViewById(R.id.modelFrameLayout)


        videoView.post {
            val halfHeight = videoView.height / 2
            modelView.layoutParams.height = halfHeight
            modelView.requestLayout()
        }

        model = LiteModelSsdMobilenetV11Metadata2.newInstance(this)
        imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(300, 300, ResizeOp.ResizeMethod.BILINEAR))
            .build()


       var factory= RearCameraViewModelFactory(applicationContext)
       rearCameraViewModel = ViewModelProvider(this, factory).get(RearCameraViewModel::class.java)


      checkCameraPermission()
      setupTextureView()
       requestPermissions(permission, steeringPermissionCode)

        videoView.surfaceTextureListener = object : SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                playVideo(surface)
            }

            override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                stopVideo()
                return true
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
            }
        }

    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
        } else {
            setupTextureView()
        }
    }

    private fun setupTextureView() {
        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, width: Int, height: Int) {

                startCamera(surfaceTexture)
            }

            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                return true
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
if (guideLinesView.visibility == View.GONE)
                guideLinesView.visibility = View.VISIBLE


            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startCamera(surfaceTexture: SurfaceTexture) {
        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = cameraManager.cameraIdList.first { id ->
            val characteristics = cameraManager.getCameraCharacteristics(id)
            characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
        }


        surfaceTexture?.setDefaultBufferSize(textureView.width, textureView.height)
        val surface = Surface(surfaceTexture)


        cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                val previewRequestBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                previewRequestBuilder.addTarget(surface)

                camera.createCaptureSession(listOf(surface), object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        session.setRepeatingRequest(previewRequestBuilder.build(), null, null)
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        Log.e("CameraDebug", "Configuration failed")
                    }
                }, null)
                if (surfaceTexture != null) {
                    processFramesFromTexture(surfaceTexture)
                }
            }

            override fun onDisconnected(camera: CameraDevice) {
                camera.close()
            }

            override fun onError(camera: CameraDevice, error: Int) {
                Log.e("CameraDebug", "Camera error: $error")
            }
        }, null)
    }

    private fun processFramesFromTexture(surfaceTexture: SurfaceTexture) {

        CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                try {

                    val bitmap = textureView.bitmap ?: continue


                    var image = TensorImage.fromBitmap(bitmap)

                    image = imageProcessor.process(image)

                    val outputs = model.process(image)

                    val detectionResult = outputs.detectionResultList

                    // Update UI with results
                    withContext(Dispatchers.Main) {
                        if (detectionResult.isEmpty()) {
                            // Handle empty detection
                        } else {
                            Log.i("TAG", "processFramesFromTexture: we got detections ")
                            Log.i("TAG", "processFramesFromTexture: passing to lines ")

                            guideLinesView.updateDetections(detectionResult)
                            Log.i("TAG", "processFramesFromTexture: passing to image modeling ")

                            modelView.updateDetections(detectionResult)
                            Log.i("TAG", "processFramesFromTexture: out of image modeling ")

                        }
                    }
                } catch (e: Exception) {
                    Log.e("TAG", "Frame processing error", e)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode){
            CAMERA_PERMISSION_CODE ->{
                if (requestCode == CAMERA_PERMISSION_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setupTextureView()
                } else {
                    Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show()
                }
            }
            steeringPermissionCode ->{
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){

                    lifecycleScope.launchWhenStarted {
                        rearCameraViewModel.steeringAngle.collect { rpm ->
                            rpm?.let {
                                guideLinesView.steeringAngle(it)
                            }
                        }
                    }
                }
            }
        }

    }

    private fun playVideo(surface: android.graphics.SurfaceTexture) {
        mediaPlayer = MediaPlayer.create(this, R.raw.carvideo).apply {
            setSurface(Surface(surface))
            isLooping = false
            start()
        }
    }

    private fun stopVideo() {
        if (mediaPlayer != null) {
            mediaPlayer.stop()
            mediaPlayer.release()
        }
    }

}
