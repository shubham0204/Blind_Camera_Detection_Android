package com.shubham0204.ml.blindcameradetection

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class UIViewModel() : ViewModel() {

    val isCameraBlinded = MutableLiveData<Boolean>()
    val colorStdDev = MutableLiveData<String>()

}