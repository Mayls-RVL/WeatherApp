package com.example.weatherapp.data.repository

import com.example.weatherapp.data.remote.RetrofitClient
import com.example.weatherapp.data.remote.model.WeatherResponse

class WeatherRepository {

    private val api = RetrofitClient.apiService
    private val apiKey = "fed55e7d1dcf3c6a7f63615d470bc5fa" // Replace with your OpenWeather API key

    // Fetch weather by latitude and longitude
    suspend fun getCurrentWeather(lat: Double, lon: Double): WeatherResponse {
        return api.getCurrentWeather(lat, lon, apiKey)
    }

    // Fetch weather by city (or city, country)
    suspend fun getWeatherByCity(city: String): WeatherResponse {
        return api.getCurrentWeatherByCity(city, apiKey)
    }
}
