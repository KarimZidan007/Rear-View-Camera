package com.example.myapplication.view

import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.os.Bundle
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.R
import com.example.myapplication.ml.LiteModelSsdMobilenetV11Metadata2
import com.example.myapplication.viewmodel.RearCameraViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.support.image.TensorImage

import org.tensorflow.lite.support.image.ImageProcessor
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
//        model = LiteModelSsdMobilenetV11Metadata2.newInstance(this)
//        imageProcessor = ImageProcessor.Builder()
//            .add(ResizeOp(300, 300, ResizeOp.ResizeMethod.BILINEAR))
//            .build()

        rearCameraViewModel = RearCameraViewModel(this,guideLinesView)

        checkCameraPermission()
        setupTextureView()
        // this line is to check permission for VHAL add by hamed
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
                processFramesFromTexture(surfaceTexture)
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
                         //   guideLinesView.updateDetections(detectionResult)
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
                                // Call the steeringAngle function whenever the value changes
                                guideLinesView.steeringAngle(it)
                            }
                        }
                    }
                }
            }
        }

    }
}
