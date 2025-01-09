package com.example.LoquoxLocation

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.LoquoxLocation.data.DatabaseHelper
import com.example.LoquoxLocation.data.Sitio
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch

class SitiosViewModel(applicationContext: Context): AndroidViewModel(applicationContext as Application) {

    val _listaSitios = MutableLiveData<List<Sitio>>()
    private val dbHelper = DatabaseHelper(applicationContext)
    val listaSitios: LiveData<List<Sitio>> get() = _listaSitios
    var tituloLocal by mutableStateOf("")
    var descripcionLocal by mutableStateOf("")
    var imagenLocal by mutableStateOf("")



    init {
        cargarSitios()
    }

    fun cargarSitios(){
        viewModelScope.launch {
            val sitios = dbHelper.obtenerSitios()
            _listaSitios.postValue(sitios)
        }
    }

    fun guardarSitio(titulo: String, descripcion: String, latitud: String, longitud: String, imagen: String ) {
        viewModelScope.launch {
            dbHelper.insertarSitio(titulo, descripcion, latitud, longitud, imagen)
            cargarSitios()
            tituloLocal = titulo
            descripcionLocal = descripcion
            imagenLocal = imagen
        }
}


    fun borrarSitio(sitio: Sitio) {
        viewModelScope.launch {
            dbHelper.borrarSitio(sitio)
            cargarSitios()
        }

    }

    fun obtenerSitioPorId(sitioId: String?): Sitio? {

        return sitioId?.let { dbHelper.obtenerSitioPorId(sitioId) }

    }

    fun actualizarImagen(uri: String) {
        imagenLocal = uri
    }




}