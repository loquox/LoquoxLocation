package com.example.LoquoxLocation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import android.app.Application
import androidx.lifecycle.AndroidViewModel


class SitiosViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SitiosViewModel::class.java)) {
            return SitiosViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


