package com.safelocker.safelocker.ui.locacao

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class locacaoViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "test"
    }
    val text: LiveData<String> = _text
}
