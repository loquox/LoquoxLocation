package com.example.LoquoxLocation.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.LoquoxLocation.SitiosViewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatLineSpacing
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.LoquoxLocation.R
import com.example.LoquoxLocation.data.Sitio





@SuppressLint("SuspiciousIndentation", "UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ListaSitios(navController: NavHostController,
                sitiosViewModel: SitiosViewModel = viewModel()
)

{


    val sitios by sitiosViewModel.listaSitios.observeAsState(initial = emptyList())

    Scaffold(
        content = { Content(sitios, sitiosViewModel , navController) },
        bottomBar = { BottonBarListaSitios(navController) }
    )




}


@Composable
fun Content(sitios: List<Sitio>, sitiosViewModel: SitiosViewModel, navController: NavHostController) {


    Column(modifier = Modifier.padding(top = 30.dp)) {
        LazyColumn {
            items(sitios) { sitio ->
                Tarjeta(sitio = sitio, sitiosViewModel, navController)
            }
        }

    }


}





@Composable
fun Tarjeta(sitio: Sitio, sitiosViewModel: SitiosViewModel, navController: NavHostController) {
    ElevatedCard (
        modifier = Modifier.
             clickable {
                navController.navigate("descripcionSitio/${sitio.id}")
             }.
        padding(bottom = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp))
    {

        Row(modifier = Modifier.
        fillMaxWidth().
        height(72.dp).
        padding(start = 10.dp),
            verticalAlignment = Alignment.CenterVertically)

        {
            Box(modifier = Modifier.
            background(color = Color.LightGray).
            size(70.dp),
                contentAlignment = Alignment.Center)

            {
              if (sitio.imagen != null){

                   AsyncImage(
                      model = ImageRequest.Builder(LocalContext.current)
                          .data(sitio.imagen)
                          .crossfade(true)
                          .build(),
                        contentDescription = "Imagen del sitio",


                  )
              } else {
                  Image(
                      painter = painterResource(id = R.drawable.ic_launcher_foreground),
                      contentDescription = "Imagen de marcador de posición",

                  )

              }
            }
            Spacer(modifier = Modifier.width(32.dp))
            Column(modifier = Modifier.
            fillMaxWidth().weight(1f) )
            {
                Text(text = "${sitio.titulo}",
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
                Text(text = "latitud: ${sitio.latidud} ",
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
                Text(text = "longitud: ${sitio.longitud}",
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall)

            }
            IconButton(onClick = {sitiosViewModel.borrarSitio(sitio)}) {
                Icon(imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    modifier =Modifier.size(40.dp)
                )
            }

        }
    }

}

@Composable
fun BottonBarListaSitios(navController: NavHostController) {
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





