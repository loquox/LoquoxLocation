package com.example.LoquoxLocation

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.LoquoxLocation.data.DatabaseHelper
import com.example.LoquoxLocation.data.Sitio
import kotlinx.coroutines.launch

class SitiosViewModel(applicationContext: Context): AndroidViewModel(applicationContext as Application) {

    val listaSitios = mutableListOf<Sitio>()
    private val dbHelper = DatabaseHelper(applicationContext)

    init {
        cargarSitios()
    }


    fun cargarSitios(){
        viewModelScope.launch {
            val sitios = dbHelper.obtenerSitios()
            listaSitios.clear()
            listaSitios.addAll(sitios)


        }
    }

    fun guardarSitio(titulo: String, foto: String, latitud: String, longitud: String ) {
        viewModelScope.launch {
            dbHelper.insertarSitio(titulo, foto, latitud, longitud)
            cargarSitios()
        }
}
    }