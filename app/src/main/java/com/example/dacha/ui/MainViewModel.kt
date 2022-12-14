package com.example.dacha.ui

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.example.dacha.data.repository.HeatingRepository
import com.example.dacha.data.repository.SwitcherRepository
import com.example.dacha.util.UiState
import com.example.dacha.worker.FirebaseWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val switcherRepository: SwitcherRepository,
    private val heatingRepository: HeatingRepository,
    val localPrefs: SharedPreferences,
    application: Application
) : ViewModel() {
    private val workManager = WorkManager.getInstance(application)

    private val _currentTemperature = MutableLiveData<UiState<Float>>()
    val currentTemperature: LiveData<UiState<Float>>
        get() = _currentTemperature

    private val requiredTemperature = MutableLiveData<UiState<Pair<String, String>>>()

    private val _switcherStatus = MutableLiveData<UiState<Pair<String, String>>>()
    val switcherStatus: LiveData<UiState<Pair<String, String>>>
        get() = _switcherStatus


init {
    fetchSwitcherStatus()
}
    fun getCurrentTemperature() {
        _currentTemperature.value = UiState.Loading
        heatingRepository.getCurrentTemperature { _currentTemperature.value = it }
    }

    fun turnOffHeat() {
        switcherRepository.turnOffHeat()
    }

    fun turnOnHeat(requiredTemp: String) {
        switcherRepository.turnOnHeat(requiredTemp){ requiredTemperature.value = it }
    }

    fun setRequireTemperature(requiredTemp: String) {
        heatingRepository.setRequiredTemperature(requiredTemp) { requiredTemperature.value = it }
    }

    private fun fetchSwitcherStatus() = viewModelScope.launch {
        _switcherStatus.value = UiState.Loading
        switcherRepository.fetchSwitcherStatus { _switcherStatus.value = it }
    }


    fun startCheckingStatus() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val workRequest =
            OneTimeWorkRequestBuilder<FirebaseWorker>()
                .setConstraints(constraints)
                //.setInitialDelay(1, TimeUnit.MINUTES)
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()
        workManager.enqueue(workRequest)
    }

    fun stopCheckingStatus() {
        workManager.cancelAllWork()
    }

}