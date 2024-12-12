package com.example.LoquoxLocation.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.LoquoxLocation.SitiosViewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Composable
fun ListaSitios(navController: NavHostController,
                sitiosViewModel: SitiosViewModel = viewModel()
)

{
    val sitios = sitiosViewModel.listaSitios


    Column(modifier = Modifier.padding(30.dp)) {
        LazyColumn() {
            items(sitios) { sitio ->
                Text(text = "latitud: ${sitio.latidud} longitud: ${sitio.longitud}")
            }
        }
    }

}
