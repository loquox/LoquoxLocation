package com.example.LoquoxLocation.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.LoquoxLocation.MainActivity
import com.example.LoquoxLocation.R
import com.example.LoquoxLocation.data.Sitio


@SuppressLint("SuspiciousIndentation")
@Composable
fun ListaSitios(navController: NavHostController,
                sitiosViewModel: SitiosViewModel = viewModel()
)

{
  val sitios = sitiosViewModel.listaSitios
    Column(modifier = Modifier.padding(top = 30.dp)) {
        LazyColumn() {
            items(sitios) { sitio ->
                tarjeta(sitio)
            }
        }
    }
    }




@Composable
fun tarjeta(sitio: Sitio) {
    Card(modifier = Modifier.
            padding(bottom = 4.dp))
    {
        Column(modifier = Modifier.padding(10.dp)) {
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
                    Image(painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = null)
                }
                Spacer(modifier = Modifier.width(32.dp))
                 Column(modifier = Modifier.fillMaxWidth()) {
                     Text(text = "${sitio.titulo}",
                         style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
                     Text(text = "latitud: ${sitio.latidud} longitud: ${sitio.longitud}",
                         style = androidx.compose.material3.MaterialTheme.typography.bodySmall)

                 }
            }
        }
    }
}
