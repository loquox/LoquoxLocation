package com.example.LoquoxLocation

import android.Manifest
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.input.KeyboardType
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

    data class Sitio(val latidud: String, val longitud: String)

    private val sitiosViewModel: SitiosViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()

            NavHost(navController = navController,
                startDestination = "locationScreen"){
                composable("locationScreen"){
                    LocationScreen(navController, sitiosViewModel)  }

                composable("listaSitios") {
                    ListaSitios(navController,sitiosViewModel  )

                }
            }
        }
    }
}


@OptIn(ExperimentalPermissionsApi::class)
@Composable

fun LocationScreen(navController: NavHostController,
                   sitiosViewModel: SitiosViewModel = viewModel()) {


    val context = LocalContext.current
    val locationClient = remember {LocationServices.getFusedLocationProviderClient(context)}
    var locationText by remember { mutableStateOf(("Ubicacion desconocida")) }

    val proximidad = 100.0
    var distancia by rememberSaveable { mutableStateOf(0.0) }

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
                latitudPunto,
                longitudPunto,
                sitiosViewModel,
                onCoordenadasConfirmadas = { nuevaLatitud, nuevaLongitud ->
                    latitudPunto = nuevaLatitud
                    longitudPunto = nuevaLongitud
                    confirmarCoordenadas = true
                    sitiosViewModel.listaSitios.add(MainActivity.Sitio(nuevaLatitud, nuevaLongitud)
                    )
                }
            )

        Spacer(modifier = Modifier.padding(16.dp))


        Card(modifier = Modifier.padding(10.dp).fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentSize(Alignment.Center)
            ){
                Column {
                    Text(
                        text = "Ubicacion actual",
                        modifier = Modifier.padding(top = 16.dp),
                        style = MaterialTheme.typography.titleMedium

                    )


                    Text(
                        text = locationText,
                        modifier = Modifier.padding(top = 16.dp)

                    )
                }
        }}

        Spacer(modifier = Modifier.padding(16.dp))

        Card(modifier = Modifier.padding(10.dp).fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentSize(Alignment.Center)
            ){
                Column {

                   MostrarCoordenadas(latitudPunto,longitudPunto)
                }
            }}



        Card(modifier = Modifier.padding(10.dp).fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()

                     ){
                Column {
                    Text(
                        text = "Distancia al Punto",
                        modifier = Modifier.padding(top = 16.dp).
                        align(Alignment.Start),
                        style = MaterialTheme.typography.titleMedium,


                    )

                    Text(
                        text = distancia.toString(),
                        modifier = Modifier.padding(top = 16.dp).
                        align(Alignment.CenterHorizontally)

                    )
                }
            }}

        if (!locationPermissionState.status.isGranted) {
            Button(onClick = {
                locationPermissionState.launchPermissionRequest()
            }) {
                Text(text = "Solicitar Permiso")
            }
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
                locationText =
                    "Latitud: ${location.latitude},   " +
                            "Longitud: ${location.longitude}"
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
                    latitud: String,
                   longitud: String,
                   sitiosViewModel: SitiosViewModel,
                   onCoordenadasConfirmadas: (String, String) -> Unit){

    var latitudlocal by rememberSaveable { mutableStateOf(latitud) }
    var longitudlocal by rememberSaveable { mutableStateOf(longitud) }


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .statusBarsPadding(),
            shape = RoundedCornerShape(16.dp)
            ){
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Introducir Coordenadas",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.padding(16.dp))

            OutlinedTextField(
                value = latitudlocal,
                onValueChange = {
                    if (it.isEmpty() || it.matches(Regex("^-?[0-9]*(\\.[0-9]*)?$"))){
                    latitudlocal = it }
                    },
                label = { Text("Introducir Latitud del punto destino") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions =  KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                )
            )

            Spacer(modifier = Modifier.padding(16.dp))

            OutlinedTextField(
                value = longitudlocal,
                onValueChange = {
                    if (it.isEmpty() || it.matches(Regex("^-?[0-9]*(\\.[0-9]*)?$"))){
                        longitudlocal = it }
                },
                label = { Text("Introducir Longitud del punto destino") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions =  KeyboardOptions(
                    keyboardType = KeyboardType.Number,

                    )
            )

            Spacer(modifier = Modifier.padding(16.dp))

            Button(onClick = {
                onCoordenadasConfirmadas(latitudlocal, longitudlocal)
                latitudlocal = ""
                longitudlocal = ""
            }) {
                Text("Confirmar")
            }

            Button(onClick = {
                navController.navigate("listaSitios")
            }) {
                Text("Revisar la lista de sitios")
            }
        }
    }
}

@Composable
fun MostrarCoordenadas(latitudPunto: Any?, longitudPunto: Any?) {

    Card(modifier = Modifier.padding(10.dp).fillMaxWidth()){
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentSize(Alignment.Center)
        ){
            Column {
                Text(
                    text = "Coordenadas introducidas del Punto",
                    modifier = Modifier.padding(top = 16.dp)
                )

                Text(
                    text = "LatitudPunto: $latitudPunto",
                    modifier = Modifier.padding(top = 16.dp)
                )

                Text(
                    text = "LongitudPunto : $longitudPunto",
                    modifier = Modifier.padding(top = 16.dp)
                )
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














