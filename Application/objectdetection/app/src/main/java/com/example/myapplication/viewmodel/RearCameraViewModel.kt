package com.example.myapplication.viewmodel


import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.Steering

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RearCameraViewModel(  context: Context):ViewModel() {

    private val _steeringAngle = MutableStateFlow<Int?>(null)
    val steeringAngle: StateFlow<Int?> get() = _steeringAngle
    val steeringHelper = Steering(context)

    init {
        viewModelScope.launch (Dispatchers.IO){
            startReadingSteering()
        }
    }

    suspend fun startReadingSteering() {
        while (Steering.stearingRotate) {
            try {
                val steeringAngle = steeringHelper.readStearing()
                Log.i("KZ", "Steering: ${steeringAngle}")
                _steeringAngle.emit(steeringAngle)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}



class RearCameraViewModelFactory(private var context: Context): ViewModelProvider.Factory{
    public override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RearCameraViewModel(context) as T
    }


}