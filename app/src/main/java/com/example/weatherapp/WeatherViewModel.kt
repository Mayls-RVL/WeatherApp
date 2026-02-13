package com.example.weatherapp.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.data.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WeatherViewModel(
    private val repository: WeatherRepository = WeatherRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState

    private val _history = MutableStateFlow<List<WeatherUiState.Success>>(emptyList())
    val history: StateFlow<List<WeatherUiState.Success>> = _history

    // Fetch by coordinates
    fun fetchWeather(lat: Double, lon: Double) {
        _uiState.value = WeatherUiState.Loading
        viewModelScope.launch {
            try {
                val response = repository.getCurrentWeather(lat, lon)
                val successState = WeatherUiState.Success(response)
                _uiState.value = successState
                _history.value = _history.value + successState
            } catch (e: Exception) {
                _uiState.value = WeatherUiState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    // Fetch by city or "City, Country"
    fun fetchWeatherByCity(city: String) {
        if (city.isBlank()) return

        _uiState.value = WeatherUiState.Loading
        viewModelScope.launch {
            try {
                val response = repository.getWeatherByCity(city.trim())
                val successState = WeatherUiState.Success(response)
                _uiState.value = successState
                _history.value = _history.value + successState
            } catch (e: Exception) {
                _uiState.value = WeatherUiState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    // ⭐ NEW: Set selected history item as current weather
    fun setCurrentWeather(weather: WeatherUiState.Success) {
        _uiState.value = weather
    }
}
