package com.example.weatherapi

import java.util.concurrent.locks.Condition

data class DayItem(
    val city: String,
    val time: String,
    val condition: String,
    val currentTemp: String,
    val maxTemp: String,
    val minTemp: String,
    val hour: String
)
