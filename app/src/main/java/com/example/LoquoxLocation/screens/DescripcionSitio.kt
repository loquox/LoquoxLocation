package com.example.LoquoxLocation.screens


import UbicacionViewModelFactory
import android.annotation.SuppressLint
import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatLineSpacing
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

import androidx.navigation.NavController
import com.example.LoquoxLocation.SitiosViewModel
import com.example.LoquoxLocation.data.Sitio
import com.example.LoquoxLocation.ui.theme.UbicacionViewModel
import com.google.android.gms.maps.model.LatLng

import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker



@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun DescripcionSitio(navController: NavController ,sitioId: String?, sitiosViewModel: SitiosViewModel) {

//    val sitioId = navController.currentBackStackEntry?.arguments?.getString("sitioId")
    val sitio = sitioId?.let { sitiosViewModel.obtenerSitioPorId(it) }
    val application = LocalContext.current.applicationContext as Application

    val ubicacionViewModel: UbicacionViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = UbicacionViewModelFactory(application)
    )

    LaunchedEffect(Unit) {
        ubicacionViewModel.iniciarActualizacionUbicacion()
    }

    DisposableEffect(Unit) {
        onDispose {
            ubicacionViewModel.deternetActualizacionUbicacion()
        }
    }



  Scaffold(
      content = { Content(sitio, ubicacionViewModel) },
      bottomBar = { BottonBarDescripcionSitio(navController) }
  )

}

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun Content(sitio: Sitio?, ubicacionViewModel: UbicacionViewModel) {
    if (sitio != null) {


        val sitioLat = sitio.latidud.toDoubleOrNull()
        val sitioLon = sitio.longitud.toDoubleOrNull()

        if (sitioLat == null && sitioLon == null) {
            Text(text = "Sitio no encontrado")
            return
        }


        val ubicacion = ubicacionViewModel.ubicacion.collectAsState(initial = null).value

        val distancia = ubicacion?.let { CalcularDistancia(it.latitude,it.longitude, sitio.latidud.toDouble(), sitio.longitud.toDouble()) }
        ElevatedCard(
            modifier = Modifier.
            statusBarsPadding().padding(10.dp),

            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp).
                              fillMaxWidth()
            )
            {
                Column(modifier = Modifier.
                                padding(10.dp).
                                weight(1f)

                ) {
                    Text(
                        text = "Titulo: ${sitio.titulo}",
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(text = "Descripcion: ${sitio.descripcion}")
                    Text(text = "LatitudPunto: ${sitio.latidud}")
                    Text(text = "LongitudPunto: ${sitio.longitud}")
                    Text(text = "Ubicacion actual: ${ubicacionViewModel.ubicacion.value?.latitude} ${ubicacionViewModel.ubicacion.value?.longitude}")
                    OpenStreetMap(sitio, ubicacion)
                    Spacer(modifier = Modifier.height(40.dp))
                    Text(modifier = Modifier.padding(top = 20.dp),
                        text = "Distancia al Punto: ${distancia} metros ")
                }
            }
        }
    }
}


@Composable
fun OpenStreetMap(sitio: Sitio, ubicacion: LatLng?) {

    val context = LocalContext.current
    org.osmdroid.config.Configuration
        .getInstance().load(context,
        android.preference.PreferenceManager.getDefaultSharedPreferences(context))

    AndroidView(
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.apply {
                    // Validamos las coordenadas antes de convertirlas
                    val latitud = sitio.latidud.toDoubleOrNull() ?: 0.0
                    val longitud = sitio.longitud.toDoubleOrNull() ?: 0.0
                    setZoom(15.0)
                    setCenter(org.osmdroid.util.GeoPoint(latitud, longitud))
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(top = 40.dp),

        update = { view ->
            val latitud = sitio.latidud.toDoubleOrNull() ?: 0.0
            val longitud = sitio.longitud.toDoubleOrNull() ?: 0.0

            val market = Marker(view)
            view.overlays.clear()
            market.position = org.osmdroid.util.GeoPoint(latitud, longitud)
            market.title = sitio.titulo
            market.snippet = sitio.descripcion
            view.overlays.add(market)

            ubicacion?.let {
                val markerPosition = org.osmdroid.util.GeoPoint(it.latitude, it.longitude)

                val userMarker = Marker(view)
                userMarker.position = markerPosition
                userMarker.title = "Ubicacion actual"
                view.overlays.add(userMarker)

                view.controller.setCenter(markerPosition)
            }

            market.showInfoWindow()
            view.invalidate()
        }
    )




}



fun CalcularDistancia(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val earthRadius = 6371 // Radio de la Tierra en kilómetros

    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)

    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2)

    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

    val result = earthRadius * c * 1000


    return result // Distancia en metros
}

@Composable
fun BottonBarDescripcionSitio(navController: NavController) {
    MaterialTheme {
        NavigationBar {
            NavigationBarItem(
                icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                selected = false,
                onClick = { navController.navigate("ViewContainer")}
            )

            NavigationBarItem(
                icon = { Icon(Icons.Filled.FormatLineSpacing, contentDescription = "Lista de Sitios") },
                selected = false,
                onClick = {  navController.navigate("listaSitios") }
            )
        }
    }
}








