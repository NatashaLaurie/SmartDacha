package com.example.dacha.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.dacha.data.repository.AuthRepository
import com.example.dacha.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    val repository: AuthRepository
) : ViewModel() {
    private val _login = MutableLiveData<UiState<String>>()
    val login: LiveData<UiState<String>>
        get() = _login

    fun login(
        email: String,
        password: String
    ) {
        _login.value = UiState.Loading
        repository.logIn(
            email,
            password
        ) {
            _login.value = it
        }
    }

    fun getSession(result: (String?) -> Unit) {
        repository.getSession(result)
    }
}