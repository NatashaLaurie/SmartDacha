package com.example.dacha.data.repository

import com.example.dacha.util.UiState

interface AuthRepository {
    fun logIn(login: String, password: String, result: (UiState<String>) -> Unit)
    fun getSession(result: (String?) -> Unit)
}