package com.example.dacha.data.repository

import com.example.dacha.util.UiState

interface SwitcherRepository {
    fun turnOnHeat(result: (UiState<String>) -> Unit)
    fun turnOffHeat(result: (UiState<String>) -> Unit)
}