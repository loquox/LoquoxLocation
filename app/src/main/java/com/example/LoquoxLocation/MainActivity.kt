package com.example.LoquoxLocation

import UbicacionViewModelFactory
import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Bundle

import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext


import androidx.compose.ui.tooling.data.UiToolingDataApi
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage

import com.example.LoquoxLocation.screens.DescripcionSitio


import com.example.LoquoxLocation.screens.ListaSitios
import com.example.LoquoxLocation.ui.theme.UbicacionViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState


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

            NavHost(
                navController = navController,
                startDestination = "ViewContainer"
            ) {
                composable("ViewContainer") {

                    ViewContainer(navController, sitiosViewModel)
                }
                composable("listaSitios") {
                    ListaSitios(navController, sitiosViewModel)
                }

                composable(
                    route = "descripcionSitio/{sitioId}",
                    arguments = listOf(navArgument("sitioId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val sitioId = backStackEntry.arguments?.getString("sitioId")
                    DescripcionSitio(navController, sitioId, sitiosViewModel)
                }
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ViewContainer(navController: NavHostController, sitiosViewModel: SitiosViewModel) {
    Scaffold(
        content = { Content(navController, sitiosViewModel) },
        bottomBar = { BottonBar(navController) }
    )
}

@Composable
fun Content(navController: NavHostController, sitiosViewModel: SitiosViewModel) {
    LocationScreen(navController, sitiosViewModel)
}

@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalPermissionsApi::class, UiToolingDataApi::class)
@Composable
fun LocationScreen(
    navController: NavHostController,
    sitiosViewModel: SitiosViewModel = viewModel()
) {

    val context = LocalContext.current

    //variables de los parametros del formulario
    var tituloPunto by rememberSaveable { mutableStateOf("") }
    var descripcionPunto by rememberSaveable { mutableStateOf("") }
    var latitudPunto by rememberSaveable { mutableStateOf("") }
    var longitudPunto by rememberSaveable { mutableStateOf("") }
    var imagenPunto by rememberSaveable { mutableStateOf("") }

    val application = LocalContext.current.applicationContext as Application

    //variables de permisos
    val locationPermissionState = rememberPermissionState(
        permission = Manifest.permission.ACCESS_FINE_LOCATION
    )
    val camaraPermissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    )

    //variables de ubicacion
    val ubicacionViewModel: UbicacionViewModel = viewModel(
        factory = UbicacionViewModelFactory(application)
    )
    val ubicacion = ubicacionViewModel.ubicacion.collectAsState(initial = null).value

    //variables de camara
    val imageUri = rememberSaveable { mutableStateOf<String?>(null) }
    val camaraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            imageUri.value?.let { uri ->
                imagenPunto = uri

            }
        }

    }

    LaunchedEffect(Unit) {
        ubicacionViewModel.iniciarActualizacionUbicacion()
    }

    DisposableEffect(Unit) {
        onDispose {
            ubicacionViewModel.deternetActualizacionUbicacion()
        }
    }


    Column(modifier = Modifier.padding(10.dp)) {

        if (ubicacion != null) {
            IntroducirCoor(
                navController,
                latitudPunto = ubicacion.latitude.toString(),
                longitudPunto = ubicacion.longitude.toString(),
                sitiosViewModel,
                context,
                imageUri,
                camaraLauncher,
                onCoordenadasConfirmadas = { titulo, descripcion, latitud, longitud, image ->
                    tituloPunto = titulo
                    descripcionPunto = descripcion
                    latitudPunto = latitud
                    longitudPunto = longitud
                    imagenPunto = image
                    if (tituloPunto.isNotEmpty() && descripcionPunto.isNotEmpty()) {
                        sitiosViewModel.guardarSitio(
                            tituloPunto,
                            descripcionPunto,
                            latitudPunto,
                            longitudPunto,
                            imagenPunto
                        )
                    } else {
                        Toast.makeText(
                            context,
                            "No hay titulo o descripcion del Punto  ",
                            Toast.LENGTH_SHORT
                        ).show()
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
    context: Context,
    imageUri: MutableState<String?>,
    camaraLauncher: ManagedActivityResultLauncher<Uri, Boolean>,
    onCoordenadasConfirmadas: (String, String, String, String, String) -> Unit
) {

    Log.d("IntroducirCoor", "Recomponiendo con imageUri: ${imageUri.value}")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .statusBarsPadding(),
        shape = RoundedCornerShape(16.dp)
    ) {
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

                Spacer(modifier = Modifier.padding(16.dp))


            }


        }

        Spacer(modifier = Modifier.padding(16.dp))

        Row() {
            Button(onClick = {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.TITLE, "Foto del Sitio")
                    put(MediaStore.Images.Media.DESCRIPTION, "Foto tomada con la camara")
                }
                val uri = context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    values
                )
                imageUri.value = uri.toString()
                if (uri != null) {
                    camaraLauncher.launch(uri)

                }

            }) {
                Text("Tomar Foto")
            }



            imageUri.value?.let { uri ->

                sitiosViewModel.imagenLocal = uri
                imageUri.value = uri


                if (uri.isNotEmpty()) {
                    Log.d("IntroducirCoor", "imageUri: $uri")
                    imageUri.value?.let { uri ->
                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),

                            )
                    }

                } else {
                    Log.d("IntroducirCoor", "imageUri esta vacio")
                }
            }
        }


        Spacer(modifier = Modifier.padding(16.dp))

        Row {
            Button(onClick = {
                onCoordenadasConfirmadas(
                    sitiosViewModel.tituloLocal,
                    sitiosViewModel.descripcionLocal,
                    latitudPunto,
                    longitudPunto,
                    imageUri.value.toString()
                )

                if (sitiosViewModel.tituloLocal.isNotEmpty() && sitiosViewModel.descripcionLocal.isNotEmpty()) {
                    navController.navigate("listaSitios")
                }
                sitiosViewModel.tituloLocal = ""
                sitiosViewModel.descripcionLocal = ""

            }) {
                Text("Guardar Punto")
            }

            Button(modifier = Modifier.padding(start = 10.dp), onClick = {
                sitiosViewModel.tituloLocal = ""
                sitiosViewModel.descripcionLocal = ""
            })
            {
                Text("Borrar")
            }


        }

        Spacer(modifier = Modifier.padding(16.dp))
    }
}


@Composable
fun BottonBar(navController: NavHostController) {
    MaterialTheme {
        NavigationBar {
            NavigationBarItem(
                icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                selected = false,
                onClick = { navController.navigate("ViewContainer") }
            )

            NavigationBarItem(
                icon = {
                    Icon(
                        Icons.Filled.FormatLineSpacing,
                        contentDescription = "Lista de Sitios"
                    )
                },
                selected = false,
                onClick = { navController.navigate("listaSitios") }
            )

        }
    }
}










