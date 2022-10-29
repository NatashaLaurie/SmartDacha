package com.example.dacha.data.repository

import com.example.dacha.util.UiState

interface SwitcherRepository {
    fun turnOnHeat(requiredTemp: String, result: (UiState<Pair<String, String>>) -> Unit)
    fun turnOffHeat()
    suspend fun fetchSwitcherStatus(result: (UiState<Pair<String, String>>) -> Unit)
}