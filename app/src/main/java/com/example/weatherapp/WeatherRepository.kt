package com.example.weatherapp.data.repository

import com.example.weatherapp.data.remote.RetrofitClient
import com.example.weatherapp.data.remote.model.WeatherResponse

class WeatherRepository {

    private val api = RetrofitClient.apiService
    private val apiKey = "fed55e7d1dcf3c6a7f63615d470bc5fa"

    // Fetch weather by city (or city, country)
    suspend fun getWeatherByCity(city: String): WeatherResponse {
        return api.getCurrentWeatherByCity(city, apiKey)
    }
}
