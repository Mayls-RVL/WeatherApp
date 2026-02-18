package com.example.weatherapp.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.weatherapp.data.remote.model.WeatherResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "weather_prefs")

class WeatherDataStore(private val context: Context) {

    private val HISTORY_KEY = stringPreferencesKey("weather_history")
    private val gson = Gson()

    suspend fun saveHistory(history: List<WeatherResponse>) {
        val json = gson.toJson(history)
        context.dataStore.edit { preferences ->
            preferences[HISTORY_KEY] = json
        }
    }

    val historyFlow: Flow<List<WeatherResponse>> =
        context.dataStore.data.map { preferences ->
            val json = preferences[HISTORY_KEY] ?: return@map emptyList()
            val type = object : TypeToken<List<WeatherResponse>>() {}.type
            gson.fromJson(json, type)
        }

}
