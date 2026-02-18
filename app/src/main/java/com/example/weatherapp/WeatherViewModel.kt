package com.example.weatherapp.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.data.local.WeatherDataStore
import com.example.weatherapp.data.remote.model.WeatherResponse
import com.example.weatherapp.data.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WeatherViewModel(
    private val repository: WeatherRepository,
    private val dataStore: WeatherDataStore
) : ViewModel() {

    private val defaultCity = "Manila, PH"

    // Current weather state
    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState

    // Search history
    private val _history = MutableStateFlow<List<WeatherUiState.Success>>(emptyList())
    val history: StateFlow<List<WeatherUiState.Success>> = _history

    init {
        // 1️⃣ Load saved history from DataStore
        viewModelScope.launch {
            dataStore.historyFlow.collect { savedHistory: List<WeatherResponse> ->
                _history.value = savedHistory.map { WeatherUiState.Success(it) }
            }
        }

        // 2️⃣ Load default city weather
        fetchWeatherByCity(defaultCity)
    }

    // Fetch by city
    fun fetchWeatherByCity(city: String) {
        if (city.isBlank()) return

        _uiState.value = WeatherUiState.Loading

        viewModelScope.launch {
            try {
                val response = repository.getWeatherByCity(city.trim())
                val successState = WeatherUiState.Success(response)

                _uiState.value = successState

                // Add newest to top (remove duplicates of same city)
                val updatedHistory = listOf(successState) +
                        _history.value.filter {
                            it.data.name != response.name
                        }

                _history.value = updatedHistory

                // 3️⃣ Save updated history to DataStore
                dataStore.saveHistory(updatedHistory.map { it.data })

            } catch (e: Exception) {
                _uiState.value =
                    WeatherUiState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    // Set selected history item as current weather
    fun setCurrentWeather(weather: WeatherUiState.Success) {
        _uiState.value = weather
    }

    // Clear history permanently
    fun clearHistory() {
        viewModelScope.launch {
            _history.value = emptyList()
            dataStore.saveHistory(emptyList())
        }
    }
}
