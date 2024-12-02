
package com.example.myapplication

import android.car.Car
import android.car.hardware.property.CarPropertyManager
import android.content.Context
import android.util.Log
import kotlin.concurrent.thread


class Steering(var context: Context, private val guideLinesView: GuideLinesView) {
    public val STEERING_PROPERTY: Int = 557842770


    var car: Car? = null
    lateinit var carPropertyManager: CarPropertyManager

    init {
        car = Car.createCar(context)
        if (car == null) {
            Log.e("Steering", "Failed to create Car instance")
        } else {
                    carPropertyManager = car!!.getCarManager(Car.PROPERTY_SERVICE) as CarPropertyManager
                    Log.e("Steering", "CarPropertyManager initialized successfully")

        }
    }

fun readStearing() {

    thread {
        while (stearingRotate) {
            try {
                synchronized(carPropertyManager) {
                    // Get the steering angle property
                    val steeringAngle = carPropertyManager.getProperty(
                        Integer::class.java,  // Use Integer, not Int
                        STEERING_PROPERTY,
                        0
                    )

                    if (steeringAngle != null) {
                        Log.d("Steering", "Raw steering angle: ${steeringAngle.value}")

                        if (steeringAngle.value is Integer) {
                            val rpm = steeringAngle.value as Integer
                            val rpmValue = rpm.toInt()  // Unbox the Integer to int
                            Log.i("KZ", "RPM value polled: $rpmValue")



                            (context as? MainActivity)?.runOnUiThread {
                                guideLinesView.steeringAngle(rpmValue)
                            }
                        } else {
                            Log.e("Steering", "Unexpected value type for steering angle: ${steeringAngle.value::class.java}")
                        }
                    } else {
                        Log.w("Steering", "Received null value for STEERING_PROPERTY")
                    }
                }

                // Sleep to prevent overloading the vehicle system and maintain responsiveness
                Thread.sleep(50)
            } catch (e: Exception) {
                Log.e("Steering", "Error reading steering angle", e)
            }
        }
    }
}
        companion object {
        val permission = arrayOf("android.car.permission.CAR_VENDOR_EXTENSION")
        val steeringPermissionCode = 200
        var stearingRotate = true
    }
}




