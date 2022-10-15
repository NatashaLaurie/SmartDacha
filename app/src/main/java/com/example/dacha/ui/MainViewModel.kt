package com.example.dacha.ui

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.dacha.data.repository.HeatingRepository
import com.example.dacha.data.repository.SwitcherRepository
import com.example.dacha.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val switcherRepository: SwitcherRepository,
    val heatingRepository: HeatingRepository,
    val localPrefs: SharedPreferences
) : ViewModel() {

    private val _currentTemperature = MutableLiveData<UiState<String>>()
    val currentTemperature: LiveData<UiState<String>>
        get() = _currentTemperature

    private val _requiredTemperature = MutableLiveData<UiState<Pair<String, String>>>()
    val requiredTemperature: LiveData<UiState<Pair<String, String>>>
        get() = _requiredTemperature

    private val _switcherStatus = MutableLiveData<UiState<String>>()
    val switcherStatus: LiveData<UiState<String>>
        get() = _switcherStatus


    fun getCurrentTemperature() {
        _currentTemperature.value = UiState.Loading
        heatingRepository.getCurrentTemperature { _currentTemperature.value = it }
    }

    fun turnOffHeat() {
        switcherRepository.turnOffHeat {
            _switcherStatus.value = it
        }
    }

    fun turnOnHeat() {
        switcherRepository.turnOnHeat {
            _switcherStatus.value = it
        }
    }

    fun setRequireTemperature(requiredTemp: String) {
        heatingRepository.setRequiredTemperature(requiredTemp) { _requiredTemperature.value = it }
    }

}