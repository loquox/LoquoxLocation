package com.example.LoquoxLocation.screens


import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.example.LoquoxLocation.SitiosViewModel
import com.example.LoquoxLocation.data.Sitio

import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker



@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun DescripcionSitio(navController: NavController ,sitioId: String?, sitiosViewModel: SitiosViewModel) {

//    val sitioId = navController.currentBackStackEntry?.arguments?.getString("sitioId")
    val sitio = sitioId?.let { sitiosViewModel.obtenerSitioPorId(it) }

  Scaffold(
      content = { Content(sitio) },
      bottomBar = { BottonBarDescripcionSitio(navController) }
  )

}

@Composable
fun Content(sitio: Sitio?) {
    if (sitio != null) {
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
                    Text(text = "Latitud: ${sitio.latidud}")
                    Text(text = "Longitud: ${sitio.longitud}")
                    OpenStreetMap(sitio)
                }
            }
        }
    }
}


@Composable
fun OpenStreetMap(sitio: Sitio) {

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
                    setZoom(15.0)
                    setCenter(org.osmdroid.util.GeoPoint(sitio.latidud.toDouble(), sitio.longitud.toDouble()))
                }
            }
       },
        modifier = Modifier.
            fillMaxWidth().
            height(300.dp).
            padding(top = 40.dp),

        update = { view ->
            val market = Marker(view)
            market.position = org.osmdroid.util.
            GeoPoint(sitio.latidud.toDouble(), sitio.longitud.toDouble())
            market.title = sitio.titulo
            market.snippet = sitio.descripcion

            view.overlays.add(market)
            market.showInfoWindow()
            view.invalidate()
        }
    )



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




