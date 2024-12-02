package com.example.myapplication.viewmodel


import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.Steering
import com.example.myapplication.view.GuideLinesView
import com.example.myapplication.view.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.concurrent.thread

class RearCameraViewModel( context: Context, guideLinesView: GuideLinesView):ViewModel() {

    private val _steeringAngle = MutableStateFlow<Int?>(null)
    val steeringAngle: StateFlow<Int?> get() = _steeringAngle
    val steeringHelper = Steering(context)

    init {
        viewModelScope.launch (Dispatchers.IO){
            startReadingSteering()
        }
    }

    suspend fun startReadingSteering() {
        try {
            val steering=steeringHelper.readStearing()
            _steeringAngle.emit(steering)
        }
        catch (e:Exception)
        {
            e.printStackTrace()
        }
    }



}