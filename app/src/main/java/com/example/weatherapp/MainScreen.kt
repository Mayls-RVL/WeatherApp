package com.example.weatherapp.ui.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.weatherapp.data.remote.model.WeatherResponse
import com.example.weatherapp.R
import com.example.weatherapp.data.local.WeatherDataStore
import com.example.weatherapp.data.repository.WeatherRepository
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {

    val context = LocalContext.current

    // Proper ViewModel creation
    val viewModel = remember {
        WeatherViewModel(
            repository = WeatherRepository(),
            dataStore = WeatherDataStore(context)
        )
    }

    val uiState by viewModel.uiState.collectAsState()
    val history by viewModel.history.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    var cityQuery by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    val tabs = listOf("Current Weather", "History")

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
                    },
                    actions = {
                        if (selectedTab == 1 && history.isNotEmpty()) {
                            IconButton(onClick = { showDialog = true }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.trash_bin),
                                    contentDescription = "Clear History"
                                )
                            }
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

    // Clear History Dialog
    if (showDialog) {
        Dialog(
            onDismissRequest = { showDialog = false },
            properties = DialogProperties(dismissOnClickOutside = true)
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Clear all history?",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(onClick = { showDialog = false }) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                viewModel.clearHistory()
                                showDialog = false
                            }
                        ) {
                            Text("Clear")
                        }
                    }
                }
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
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Error: ${uiState.message}",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        is WeatherUiState.Success -> {

            val weather = uiState.data
            val condition = weather.weather.firstOrNull()

            // 🌙 Time Calculation
            val utcTime = System.currentTimeMillis() / 1000L
            val cityLocalTime = utcTime + weather.timezone
            val localHour = (cityLocalTime % 86400) / 3600
            val isNight =
                localHour >= 18 || localHour < (weather.sys.sunrise % 86400) / 3600

            // 🌤 Weather Icon
            val iconRes = when (condition?.main) {
                "Clear" -> if (isNight) R.drawable.night else R.drawable.sunny
                "Clouds" -> if (isNight) R.drawable.cloudynight else R.drawable.cloudy
                "Rain" -> R.drawable.rain
                "Snow" -> R.drawable.snow
                "Thunderstorm" -> R.drawable.thunderstorm
                else -> R.drawable.sunny
            }

            // 🎨 Background color for entire screen
            val backgroundColor =
                if (isNight) MaterialTheme.colorScheme.primary
                else Color.White

            val textColor =
                if (isNight) MaterialTheme.colorScheme.onPrimary
                else Color.Black

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor)
                    .padding(16.dp)
            ) {

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "${weather.name}, ${weather.sys.country}",
                        style = MaterialTheme.typography.headlineLarge,
                        color = textColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 32.dp)
                    )


                    Spacer(modifier = Modifier.height(16.dp))

                    // 🌤 Weather Icon
                    Image(
                        painter = painterResource(id = iconRes),
                        contentDescription = condition?.main ?: "Weather Icon",
                        modifier = Modifier.size(120.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 🌡 Temperature
                    Text(
                        text = "${weather.main.temp.toInt()}°C",
                        style = MaterialTheme.typography.displayMedium,
                        color = textColor
                    )

                    Text(
                        text = "Feels like ${weather.main.feels_like.toInt()}°C",
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )


                    Spacer(modifier = Modifier.height(24.dp))

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
}




@Composable
fun WeatherForecastCard(weather: WeatherResponse) {
    val condition = weather.weather.firstOrNull()
    val gradientColors = when (condition?.main) {
        "Rain" -> listOf(Color(0xFF90A4AE), Color(0xFF64B5F6))
        "Snow" -> listOf(Color(0xFFFFFFFF), Color(0xFFB3E5FC))
        "Clear" -> listOf(Color(0xFFFFF59D), Color(0xFFFFC107))
        "Clouds" -> listOf(Color(0xFFCFD8DC), Color(0xFFB0BEC5))
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Condition Column
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = condition?.main ?: "N/A",
                        style = MaterialTheme.typography.titleLarge.copy(
                            shadow = Shadow(Color.Black, Offset(2f, 2f), 4f)
                        ),
                        color = Color.White
                    )
                    Text(
                        text = condition?.description ?: "",
                        style = MaterialTheme.typography.titleMedium.copy(
                            shadow = Shadow(Color.Black, Offset(2f, 2f), 4f)
                        ),
                        color = Color.White
                    )
                }

                // Humidity Column
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Humidity",
                        style = MaterialTheme.typography.titleMedium.copy(
                            shadow = Shadow(Color.Black, Offset(2f, 2f), 4f)
                        ),
                        color = Color.White
                    )
                    Text(
                        text = "${weather.main.humidity}%",
                        style = MaterialTheme.typography.titleLarge.copy(
                            shadow = Shadow(Color.Black, Offset(2f, 2f), 4f)
                        ),
                        color = Color.White
                    )
                }
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
                    formatTime(weather.sys.sunrise, weather.timezone),
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
                    formatTime(weather.sys.sunset, weather.timezone),
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
fun WeatherHistoryScreen(
    history: List<WeatherUiState.Success>,
    onItemClick: (WeatherUiState.Success) -> Unit
) {
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

fun formatTime(timestamp: Long, timezoneOffset: Long): String {
    val date = Date((timestamp + timezoneOffset) * 1000)
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    return sdf.format(date)
}
