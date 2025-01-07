package com.example.LoquoxLocation.ui.theme

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import androidx.compose.ui.tooling.data.UiToolingDataApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class UbicacionViewModel(application: Application) : AndroidViewModel(application) {





    private val fusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(application)

    private val locationRequest = LocationRequest.create().apply {
        interval = 10000
        fastestInterval = 5000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY

    }

    private val _ubicacion = MutableStateFlow<LatLng?>(null)
    val ubicacion: StateFlow<LatLng?> = _ubicacion

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.locations.lastOrNull()?.let { location ->
                _ubicacion.value = LatLng(location.latitude, location.longitude)
            }
        }
    }

@SuppressLint("MissingPermission")
fun iniciarActualizacionUbicacion() {
    fusedLocationProviderClient.requestLocationUpdates(
        locationRequest,
        locationCallback,
        getApplication<Application>().mainLooper

    )
}

fun deternetActualizacionUbicacion() {
    fusedLocationProviderClient.removeLocationUpdates(locationCallback)

}

override fun onCleared() {
    super.onCleared()
    deternetActualizacionUbicacion()

}





}