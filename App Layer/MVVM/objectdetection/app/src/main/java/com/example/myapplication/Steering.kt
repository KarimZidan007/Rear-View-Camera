package com.example.myapplication

import android.car.Car
import android.car.VehiclePropertyIds
import android.car.hardware.property.CarPropertyManager
import android.content.Context
import android.util.Log


class Steering(var context: Context) {
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

    fun readStearing():Int {
        var rpmValue =0
     //   thread {
            while (stearingRotate) {
                try {
                    synchronized(carPropertyManager) {
                        val steeringAngle = carPropertyManager.getProperty(
                            Integer::class.java,
                            VehiclePropertyIds.STEERING_PROPERTY,
                            0
                        )

                        if (steeringAngle != null) {
                            Log.d("Steering", "Raw steering angle: ${steeringAngle.value}")

                            if (steeringAngle.value is Integer) {
                                val rpm = steeringAngle.value as Integer
                                 rpmValue = rpm.toInt()
                                Log.i("KZ", "RPM value polled: $rpmValue")

                            } else {
                                Log.e("Steering", "Unexpected value type for steering angle: ${steeringAngle.value::class.java}")
                            }
                        } else {
                            Log.w("Steering", "Received null value for STEERING_PROPERTY")
                        }
                    }

                    Thread.sleep(50)
                } catch (e: Exception) {
                    Log.e("Steering", "Error reading steering angle", e)
                }
            }
       // }
        return rpmValue
    }
    companion object {
        val permission = arrayOf("android.car.permission.CAR_VENDOR_EXTENSION")
        val steeringPermissionCode = 200
        var stearingRotate = true
    }
}




