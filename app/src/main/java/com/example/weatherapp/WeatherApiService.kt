package com.example.weatherapp.data.remote

import com.example.weatherapp.data.remote.model.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    // 1️⃣ Fetch current weather by latitude and longitude
    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric" // Celsius
    ): WeatherResponse

    // 2️⃣ Fetch current weather by city name (or city, country)
    @GET("weather")
    suspend fun getCurrentWeatherByCity(
        @Query("q") cityName: String,       // e.g., "London" or "London, UK"
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): WeatherResponse
}
