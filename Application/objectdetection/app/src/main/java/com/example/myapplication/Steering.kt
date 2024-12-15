package com.example.myapplication

import android.car.Car
import android.car.VehiclePropertyIds
import android.car.hardware.CarPropertyValue
import android.car.hardware.property.CarPropertyManager
import android.content.Context
import android.util.Log


class Steering(var context: Context) {
    val STEERING_PROPERTY: Int = 557842770
    val areaId = 0
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
         var  rpmValue:Int=0
        try {
            synchronized(carPropertyManager) {
                // Get the steering angle property

                val steeringAngle: CarPropertyValue<Integer> = carPropertyManager.getProperty(
                    Integer::class.java, STEERING_PROPERTY, areaId
                )

                Log.d("Steering", "Raw steering angle: ${steeringAngle.value}")


                val rpm = steeringAngle.value
                 rpmValue = rpm.toInt()
                Log.i("KZZ", "RPM value polled: $rpmValue")

            }

        } catch (e: Exception) {
            Log.e("Steering", "Error reading steering angle", e)
        }
        return rpmValue
    }
    companion object {
        val permission = arrayOf("android.car.permission.CAR_VENDOR_EXTENSION")
        val steeringPermissionCode = 200
        var stearingRotate = true
    }
}




