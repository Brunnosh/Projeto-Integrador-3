package com.safelocker.safelocker.ui.maps

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MapsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "test"
    }
    val text: LiveData<String> = _text
}