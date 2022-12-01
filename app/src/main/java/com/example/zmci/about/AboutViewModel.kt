package com.example.zmci.about

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AboutViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "An android application that monitors violations of construction workers. Developed by ZMCI."
    }
    val text: LiveData<String> = _text
}