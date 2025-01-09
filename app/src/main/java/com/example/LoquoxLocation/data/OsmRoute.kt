package com.example.LoquoxLocation.data

import okhttp3.Route

data class OSMRouteResponse(
    val routes: List<Route>

)

data class Route(
    val geometry: String,
)