package com.example.myapplication

import android.car.Car
import android.car.hardware.CarPropertyValue
import android.car.hardware.property.CarPropertyManager
import android.car.hardware.property.CarPropertyManager.CarPropertyEventCallback
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.os.Bundle
import android.util.Log
import android.view.TextureView
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.image.ImageProcessor

class MainActivity : AppCompatActivity() {

    private val CAMERA_PERMISSION_CODE = 100
    private lateinit var guideLinesView: GuideLinesView
    private lateinit var interpreter: Interpreter
    private lateinit var imageProcessor: ImageProcessor
    private lateinit var textureView: TextureView
    private lateinit var steering: Steering
    public val STEERING_PROPERTY: Int = 557842770
    val areaId = 0x01000000
    private var mCar: Car? = null

    private var mPropertyCallback: CarPropertyEventCallback = object : CarPropertyEventCallback {
        override fun onChangeEvent(carPropertyValue: CarPropertyValue<*>) {
            Log.d("MainActivity", "onChangeEvent triggered")
            if (carPropertyValue.propertyId == STEERING_PROPERTY) {
                try {
                    val rpmValue = carPropertyValue.value as Int
                    Log.i("KZ", "RPM value polled: $rpmValue")
                    Log.d("MainActivity", "Fuel tank updated to: $rpmValue%")
                } catch (e: java.lang.Exception) {
                    Log.e("MainActivity", "Error processing FuelTank property value", e)
                }
            }
        }

        override fun onErrorEvent(propertyId: Int, zone: Int) {
            Log.e("MainActivity", "Car Property error event: Property ID $propertyId, Zone $zone")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate called")
        setContentView(R.layout.activity_main)

        guideLinesView = findViewById(R.id.guideLinesView)
        guideLinesView.visibility = View.GONE
        textureView = findViewById(R.id.textureView)

        Log.d("MainActivity", "guideLinesView and textureView initialized")
        steering = Steering(applicationContext, guideLinesView)
        ActivityCompat.requestPermissions(
            this,
            arrayOf("android.car.permission.CAR_INFO", "android.car.permission.CAR_CONTROL","android.car.permission.CAR_VENDOR_EXTENSION","android.car.permission.READ_CAR_PROPERTY"),200)
        Log.d("MainActivity", "Steering object initialized")
    }

    private fun initializeCarPropertyManager() {
        Log.d("MainActivity", "Initializing Car Property Manager")
        mCar = Car.createCar(applicationContext)
        if (mCar == null) {
            Log.e("MainActivity", "Failed to create Car instance")
        } else {
            val carPropertyManager = mCar!!.getCarManager(CarPropertyManager::class.java)
            Log.d("MainActivity", "Car manager created, registering callback")
            carPropertyManager.registerCallback(mPropertyCallback, STEERING_PROPERTY, CarPropertyManager.SENSOR_RATE_ONCHANGE)
            Log.d("MainActivity", "Car Property Callback registered")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d("MainActivity", "onRequestPermissionsResult called with requestCode: $requestCode")

        when (requestCode) {
            Steering.steeringPermissionCode -> {
                Log.d("MainActivity", "Steering permission request result received")
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("MainActivity", "Car Property Permission granted")
                    initializeCarPropertyManager()
                    Toast.makeText(this, "Car Property Permission granted", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("MainActivity", "Car Property Permission denied")
                    Toast.makeText(this, "Permission to access car property is required", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MainActivity", "onDestroy called")
    }
}
