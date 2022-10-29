package com.example.dacha.data.repository

import android.content.Context
import android.util.Log
import com.example.dacha.util.FireBaseFields
import com.example.dacha.util.UiState
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SwitcherRepositoryImpl(
    private val database: DatabaseReference,
    @ApplicationContext val context: Context
) : SwitcherRepository {

    override fun turnOnHeat(requiredTemp: String, result: (UiState<Pair<String, String>>) -> Unit) {
        database.child(FireBaseFields.REQUIRED_TEMPERATURE)
            .setValue(requiredTemp.dropLast(2).toInt())
            .addOnSuccessListener {
                database.child(FireBaseFields.SWITCHER_HEAT).setValue(1)
                result.invoke(
                    UiState.Success(Pair(requiredTemp, "Температура установлена"))
                )
            }
            .addOnFailureListener {
                result.invoke(
                    UiState.Failure(
                        it.localizedMessage
                    )
                )
            }

    }

    override suspend fun fetchSwitcherStatus(result: (UiState<Pair<String, String>>) -> Unit) {
        val data = try {
            getSwitcherStatus()
        } catch (e: Exception) {
            result.invoke(
                UiState.Failure(e.message)
            )
            Log.e("Fetch switcher status", e.message, e)
        }

        val requiredTemperature = try {
            getReqTemperature()
        } catch (e: Exception) {
            result.invoke(
                UiState.Failure(e.message)
            )
            Log.e("Fetch temperature status", e.message, e)
        }

        result.invoke(UiState.Success(Pair(data.toString(), "${requiredTemperature.toString()}°C")))

    }

    private suspend fun getSwitcherStatus() = withContext(Dispatchers.IO) {
        database.child(FireBaseFields.SWITCHER_HEAT)
            .get()
            .await()
            .value
    }

    private suspend fun getReqTemperature() = withContext(Dispatchers.IO) {
        database.child(FireBaseFields.REQUIRED_TEMPERATURE)
            .get()
            .await()
            .value
    }


    override fun turnOffHeat() {
        database.child(FireBaseFields.SWITCHER_HEAT)
            .setValue(0)

    }
}