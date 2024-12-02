package com.example.myapplication.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.util.Log
import android.view.Surface
import android.view.TextureView
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        guideLinesView = findViewById(R.id.guideLinesView)
        guideLinesView.visibility = View.GONE
        textureView =findViewById(R.id.textureView)
        model = LiteModelSsdMobilenetV11Metadata2.newInstance(this)
        imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(300, 300, ResizeOp.ResizeMethod.BILINEAR))
            .build()


        var factory=RearCameraViewModelFactory(applicationContext)
        rearCameraViewModel = ViewModelProvider(this, factory).get(RearCameraViewModel::class.java)


      checkCameraPermission()
      setupTextureView()
       requestPermissions(permission, steeringPermissionCode)

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
                // Handle texture size changes
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

                    // Get the Bitmap from the TextureView
                    val bitmap = textureView.bitmap ?: continue

                    // Process the frame

                    var image = TensorImage.fromBitmap(bitmap)

                    image = imageProcessor.process(image)

                    val outputs = model.process(image)

                    val detectionResult = outputs.detectionResultList

                    // Update UI with results
                    withContext(Dispatchers.Main) {
                        if (detectionResult.isEmpty()) {
                            // Handle empty detection
                        } else {
                            guideLinesView.updateDetections(detectionResult)
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
                                // Call the steeri
                                // ngAngle function whenever the value changes
                                guideLinesView.steeringAngle(it)
                            }
                        }
                    }
                }
            }
        }

    }
}
