package com.example.myapplication

import android.car.Car
import android.car.VehiclePropertyIds
import android.car.hardware.CarPropertyValue
import android.car.hardware.property.CarPropertyManager
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import kotlin.concurrent.thread

class Steering(var context : Context) {
     var car :Car
     var  carPropertyManager  : CarPropertyManager
    private lateinit var guideLinesView: GuideLinesView
    init {
         car = Car.createCar(context)
        carPropertyManager = car.getCarManager(Car.PROPERTY_SERVICE) as CarPropertyManager
    }

    companion object{
        val permission = arrayOf("android.car.permission.CAR_VENDOR_EXTENSION")
        val steeringPermissionCode = 200
        val stearingRotate = true
    }

    fun readStearing(){
       thread {
           while(stearingRotate){
               try {
                   val steeringAngle: CarPropertyValue<*>? = carPropertyManager.getProperty(
                       Int::class.java,
                       VehiclePropertyIds.STEERING_PROPERTY,
                       0
                   )
                   if (steeringAngle != null) {
                       val rpm = steeringAngle.value as Int
                       Toast.makeText(context, "Stearing Angle is ${rpm}", Toast.LENGTH_SHORT).show()
                       Log.e("steering" , "steering rotate")
                       guideLinesView.steeringAngle(rpm)
                   }
                   Thread.sleep(300)
               } catch (e: Exception) {
                   e.printStackTrace()
               }

           }
       }.start()
    }


}