package com.safelocker.safelocker.ui.cartao

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class cartaoViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "test"
    }
    val text: LiveData<String> = _text
}