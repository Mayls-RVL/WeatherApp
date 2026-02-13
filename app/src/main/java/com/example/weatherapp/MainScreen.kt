package com.example.weatherapp.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import com.example.weatherapp.data.remote.model.WeatherResponse
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: WeatherViewModel = WeatherViewModel()) {

    val uiState by viewModel.uiState.collectAsState()
    val history by viewModel.history.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Current Weather", "History")
    var cityQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.fetchWeather(51.5074, -0.1278)
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.WbSunny,
                                contentDescription = "Sun Icon",
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Weather Weather Lang")
                        }
                    }
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = cityQuery,
                        onValueChange = { cityQuery = it },
                        placeholder = { Text("Search a City or Country") },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(50),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                if (cityQuery.isNotBlank()) {
                                    viewModel.fetchWeatherByCity(cityQuery.trim())
                                }
                            }
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (cityQuery.isNotBlank()) {
                                viewModel.fetchWeatherByCity(cityQuery.trim())
                            }
                        }
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {

            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index }
                    ) {
                        Text(title, modifier = Modifier.padding(16.dp))
                    }
                }
            }

            when (selectedTab) {
                0 -> CurrentWeatherScreen(uiState)
                1 -> WeatherHistoryScreen(
                    history = history,
                    onItemClick = { selectedItem ->
                        viewModel.setCurrentWeather(selectedItem)
                        selectedTab = 0
                    }
                )
            }
        }
    }
}

@Composable
fun CurrentWeatherScreen(uiState: WeatherUiState) {
    when (uiState) {
        is WeatherUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is WeatherUiState.Error -> {
            Text(
                text = "Error: ${uiState.message}",
                modifier = Modifier.padding(16.dp)
            )
        }
        is WeatherUiState.Success -> {
            val weather = uiState.data
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                CityTempCard(weather)
                Spacer(modifier = Modifier.height(16.dp))
                WeatherForecastCard(weather)
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SunriseCard(weather, Modifier.weight(1f))
                    SunsetCard(weather, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun CityTempCard(weather: WeatherResponse) {
    val condition = weather.weather.firstOrNull()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Column(
            modifier = Modifier
                .padding(top = 40.dp, start = 32.dp, end = 32.dp, bottom = 32.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // 🌆 City Name
            Text(
                text = "${weather.name}, ${weather.sys.country}",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // 🌤 Weather Icon
            val iconVector = when (condition?.main) {
                "Clear" -> Icons.Default.WbSunny
                "Clouds" -> Icons.Default.Cloud
                "Rain" -> Icons.Default.Umbrella
                "Snow" -> Icons.Default.AcUnit
                else -> Icons.Default.WbSunny
            }

            Icon(
                imageVector = iconVector,
                contentDescription = condition?.main ?: "Weather Icon",
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 🌡 Temperature
            Text(
                text = "${weather.main.temp.toInt()}°C",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}



@Composable
fun WeatherForecastCard(weather: WeatherResponse) {
    val condition = weather.weather.firstOrNull()
    val gradientColors = when (condition?.main) {
        "Rain" -> listOf(Color(0xFF90A4AE), Color(0xFF64B5F6)) // Gray -> Blue
        "Snow" -> listOf(Color(0xFFFFFFFF), Color(0xFFB3E5FC)) // White -> Light Blue
        "Clear" -> listOf(Color(0xFFFFF59D), Color(0xFFFFC107)) // Yellow -> Orange
        "Clouds" -> listOf(Color(0xFFCFD8DC), Color(0xFFB0BEC5)) // Light Gray -> Gray
        else -> listOf(Color(0xFFE0E0E0), Color(0xFFBDBDBD))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(gradientColors)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    condition?.main ?: "N/A",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        shadow = Shadow(Color.Black, Offset(2f, 2f), 4f)
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    condition?.description ?: "",
                    style = MaterialTheme.typography.titleMedium.copy(
                        shadow = Shadow(Color.Black, Offset(2f, 2f), 4f)
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun SunriseCard(weather: WeatherResponse, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFFFF59D), Color(0xFFFFC107))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Sunrise",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge.copy(
                        shadow = Shadow(Color.Black, Offset(2f, 2f), 4f)
                    ),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    formatTime(weather.sys.sunrise),
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        shadow = Shadow(Color.Black, Offset(2f, 2f), 4f)
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun SunsetCard(weather: WeatherResponse, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFFFC107), Color(0xFFFF8A65))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Sunset",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge.copy(
                        shadow = Shadow(Color.Black, Offset(2f, 2f), 4f)
                    ),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    formatTime(weather.sys.sunset),
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        shadow = Shadow(Color.Black, Offset(2f, 2f), 4f)
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun WeatherHistoryScreen(history: List<WeatherUiState.Success>, onItemClick: (WeatherUiState.Success) -> Unit) {
    if (history.isEmpty()) {
        Text("No history yet.", modifier = Modifier.padding(16.dp))
    } else {
        LazyColumn(modifier = Modifier.padding(16.dp)) {
            items(history) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    onClick = { onItemClick(item) }
                ) {
                    WeatherCardContent(item.data)
                }
            }
        }
    }
}

@Composable
fun WeatherCardContent(weather: WeatherResponse) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("${weather.name}, ${weather.sys.country}")
        Text("Temp: ${weather.main.temp}°C")
    }
}

fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp * 1000))
}
