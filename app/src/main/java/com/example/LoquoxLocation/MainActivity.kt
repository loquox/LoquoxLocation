package com.example.LoquoxLocation

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatLineSpacing
import androidx.compose.material.icons.filled.Home


import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner

import androidx.compose.ui.tooling.data.UiToolingDataApi
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.example.LoquoxLocation.screens.ListaSitios
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices



class MainActivity : ComponentActivity() {

//    data class Sitio(val latidud: String, val longitud: String)

    private val sitiosViewModel: SitiosViewModel by viewModels {
        SitiosViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()

            NavHost(navController = navController,
                startDestination = "ViewContainer"){
                composable("ViewContainer"){

                    ViewContainer(navController, sitiosViewModel)
                    }
                composable("listaSitios") {
                    ListaSitios(navController,sitiosViewModel  )
                }
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ViewContainer(navController: NavHostController, sitiosViewModel: SitiosViewModel){
   Scaffold(
       content = { Content(navController, sitiosViewModel) },
       bottomBar = { BottonBar(navController) }
   )
}

@Composable
fun Content(navController: NavHostController,sitiosViewModel: SitiosViewModel){
    LocationScreen(navController, sitiosViewModel)
}

@OptIn(ExperimentalPermissionsApi::class, UiToolingDataApi::class)
@Composable
fun LocationScreen(navController: NavHostController,
                   sitiosViewModel: SitiosViewModel = viewModel()) {

    val context = LocalContext.current
    val locationClient = remember {LocationServices.getFusedLocationProviderClient(context)}
    var locationText by remember { mutableStateOf(("Ubicacion desconocida")) }

    val proximidad = 100.0
    var distancia by rememberSaveable { mutableStateOf(0.0) }

    var tituloPunto by rememberSaveable { mutableStateOf("") }
    var descripcionPunto by rememberSaveable { mutableStateOf("") }
    var latitudPunto by rememberSaveable { mutableStateOf("") }
    var longitudPunto by rememberSaveable { mutableStateOf("") }


    val locationPermissionState = rememberPermissionState(
        permission = Manifest.permission.ACCESS_FINE_LOCATION
    )

    val lifecycleOwner = LocalLifecycleOwner.current
    var showDialog by remember { mutableStateOf(false) }
    var confirmarCoordenadas by rememberSaveable { mutableStateOf(false) }


    Column(modifier = Modifier.padding(10.dp)) {

            IntroducirCoor(
                navController,
                tituloPunto,
                descripcionPunto,
                latitudPunto = latitudPunto,
                longitudPunto = longitudPunto,
                sitiosViewModel,
                onCoordenadasConfirmadas = { titulo, descripcion->
                    tituloPunto = titulo
                    descripcionPunto = descripcion
                    confirmarCoordenadas = true
                    if(tituloPunto.isNotEmpty() && descripcionPunto.isNotEmpty()) {
                        sitiosViewModel.guardarSitio(
                            tituloPunto,
                            descripcionPunto,
                            latitudPunto,
                            longitudPunto
                        )
                    }else {
                        Toast.makeText(context, "No hay titulo ni descripcion del Punto  ", Toast.LENGTH_SHORT).show()
                    }
                }
            )
    }

    if (!locationPermissionState.status.isGranted) {
        Button(onClick = {
            locationPermissionState.launchPermissionRequest()
        }) {
            Text(text = "Solicitar Permiso")
        }
    }


    val locationRequest = LocationRequest.create().apply {
        interval = 10000
        fastestInterval = 5000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    val locationCallback = object : LocationCallback() {
        override  fun onLocationResult(locationResult: LocationResult) {
            for (location in locationResult.locations) {
                latitudPunto = location.latitude.toString()
                longitudPunto = location.longitude.toString()



                val latitudPuntoDouble = latitudPunto.toDoubleOrNull()
                val longitudPuntoDouble = longitudPunto.toDoubleOrNull()
//                val latitudPuntoDouble = 43.337788498781876
//                val longitudPuntoDouble = -1.7802331596025087


               if (latitudPuntoDouble != null && longitudPuntoDouble != null
                   && confirmarCoordenadas ) {
                   distancia = CalcularDistancia(
                       location.latitude,
                       location.longitude,
                       latitudPuntoDouble,
                       longitudPuntoDouble
                   )
               }
                if (distancia <= proximidad && confirmarCoordenadas) {
                    Toast.makeText(context, "Estas a$distancia ", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LaunchedEffect(locationPermissionState.status) {
        if (locationPermissionState.status.isGranted) {
            locationClient.requestLocationUpdates(locationRequest,
               locationCallback, Looper.getMainLooper())
        }

    }

    DisposableEffect(lifecycleOwner)
    {
        onDispose {
            locationClient.removeLocationUpdates(locationCallback)
        }
    }
}


@Composable
fun IntroducirCoor(
                    navController: NavHostController,
                    titulo: String,
                    descripcion: String,
                    latitudPunto: String,
                    longitudPunto: String,
                    sitiosViewModel: SitiosViewModel,
                   onCoordenadasConfirmadas: (String, String) -> Unit){
    var titulolocal by rememberSaveable { mutableStateOf(titulo) }
    var descripcionlocal by rememberSaveable { mutableStateOf(descripcion) }



    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .statusBarsPadding(),
            shape = RoundedCornerShape(16.dp)
            ){
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Introducir Datos del Punto",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.padding(16.dp))

            OutlinedTextField(
                value = sitiosViewModel.tituloLocal,
                onValueChange = { sitiosViewModel.tituloLocal = it },
                label = { Text("Introducir titulo del punto destino") },
                modifier = Modifier.fillMaxWidth()
            )


            Spacer(modifier = Modifier.padding(16.dp))

            OutlinedTextField(
                value = sitiosViewModel.descripcionLocal,
                onValueChange = { sitiosViewModel.descripcionLocal = it },
                label = { Text("Introducir descripcion del punto destino") },
                modifier = Modifier.fillMaxWidth()
            )

            Column {
                Text(
                    text = "Coordenadas introducidas del Punto",
                    modifier = Modifier.padding(top = 16.dp)
                )

                Text(
                    text = "LatitudPunto: ${latitudPunto} ",
                    modifier = Modifier.padding(top = 16.dp)
                )

                Text(
                    text = "LongitudPunto : ${longitudPunto}",
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            Spacer(modifier = Modifier.padding(16.dp))

            Row {
                Button(onClick = {
                    onCoordenadasConfirmadas(sitiosViewModel.tituloLocal, sitiosViewModel.descripcionLocal)
                    titulolocal = ""
                    descripcionlocal = ""
                    if (latitudPunto.isNotEmpty() && longitudPunto.isNotEmpty()) {
                        navController.navigate("listaSitios")
                    }

                }) {
                    Text("Guardar Punto")
                }

                Button(onClick = {
                    sitiosViewModel.tituloLocal=""
                    sitiosViewModel.descripcionLocal=""
                })
                {
                    Text("Borrar")
                }



            }
        }
    }
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
fun BottonBar(navController: NavHostController) {
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










