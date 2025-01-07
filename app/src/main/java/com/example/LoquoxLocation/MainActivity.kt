package com.example.LoquoxLocation

import UbicacionViewModelFactory
import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
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
import androidx.compose.runtime.collectAsState
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
import androidx.navigation.NavType

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.LoquoxLocation.screens.DescripcionSitio


import com.example.LoquoxLocation.screens.ListaSitios
import com.example.LoquoxLocation.ui.theme.UbicacionViewModel
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
                    ListaSitios(navController,sitiosViewModel)
                }

                composable(
                    route = "descripcionSitio/{sitioId}",
                    arguments = listOf(navArgument("sitioId") { type = NavType.StringType })
                ){ backStackEntry ->
                    val sitioId = backStackEntry.arguments?.getString("sitioId")
                    DescripcionSitio(navController,sitioId, sitiosViewModel)
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

@SuppressLint("StateFlowValueCalledInComposition")
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
    val application = LocalContext.current.applicationContext as Application


    val ubicacionViewModel: UbicacionViewModel = viewModel(
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


    val locationPermissionState = rememberPermissionState(
        permission = Manifest.permission.ACCESS_FINE_LOCATION
    )

    var showDialog by remember { mutableStateOf(false) }
    var confirmarCoordenadas by rememberSaveable { mutableStateOf(false) }

    val ubicacion = ubicacionViewModel.ubicacion.collectAsState(initial = null).value



    Column(modifier = Modifier.padding(10.dp)) {

        if (ubicacion != null) {
            IntroducirCoor(
                navController,
                latitudPunto = ubicacion.latitude.toString(),
                longitudPunto = ubicacion.longitude.toString(),
                sitiosViewModel,
                onCoordenadasConfirmadas = { titulo, descripcion, latitud, longitud ->
                    tituloPunto = titulo
                    descripcionPunto = descripcion
                    latitudPunto = latitud
                    longitudPunto = longitud
                    confirmarCoordenadas = true
                    if(tituloPunto.isNotEmpty() && descripcionPunto.isNotEmpty()) {
                        sitiosViewModel.guardarSitio(
                            tituloPunto,
                            descripcionPunto,
                            latitudPunto,
                            longitudPunto
                        )
                    }else {
                        Toast.makeText(context, "No hay titulo o descripcion del Punto  ", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }

    if (!locationPermissionState.status.isGranted) {
        Button(onClick = {
            locationPermissionState.launchPermissionRequest()
        }) {
            Text(text = "Solicitar Permiso")
        }
    }
}


@Composable
fun IntroducirCoor(
                    navController: NavHostController,
                    latitudPunto: String,
                    longitudPunto: String,
                    sitiosViewModel: SitiosViewModel,
                   onCoordenadasConfirmadas: (String, String, String , String ) -> Unit){

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
                maxLines = 1,
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
                    onCoordenadasConfirmadas(sitiosViewModel.tituloLocal, sitiosViewModel.descripcionLocal, latitudPunto, longitudPunto)

                    if (sitiosViewModel.tituloLocal.isNotEmpty() && sitiosViewModel.descripcionLocal.isNotEmpty()) {
                        navController.navigate("listaSitios")
                    }
                    sitiosViewModel.tituloLocal=""
                    sitiosViewModel.descripcionLocal=""

                }) {
                    Text("Guardar Punto")
                }

                Button(modifier = Modifier.
                    padding(start = 10.dp)
                    , onClick = {
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










