package com.example.dacha.data.repository

import com.example.dacha.util.UiState

interface HeatingRepository {
    fun getCurrentTemperature(result: (UiState<String>) -> Unit)
    fun setRequiredTemperature(requiredTemp: String,  result: (UiState<Pair<String,String>>) -> Unit)
}