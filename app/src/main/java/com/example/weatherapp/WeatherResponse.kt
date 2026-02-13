package com.example.weatherapp.data.remote.model

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    val name: String, // City
    val sys: Sys,
    val main: Main,
    val weather: List<Weather>
)

data class Sys(
    val country: String,
    val sunrise: Long,
    val sunset: Long
)

data class Main(
    val temp: Float
)

data class Weather(
    val main: String, // e.g., Rain, Clear, Clouds
    val description: String,
    @SerializedName("icon") val iconCode: String
)
