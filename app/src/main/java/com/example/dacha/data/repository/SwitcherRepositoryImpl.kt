package com.example.dacha.data.repository

import android.content.SharedPreferences
import com.example.dacha.util.FireBaseFields
import com.example.dacha.util.SharedPrefConstants
import com.example.dacha.util.UiState
import com.google.firebase.database.DatabaseReference

class SwitcherRepositoryImpl(
    val database: DatabaseReference,
    val appPreferences: SharedPreferences
) : SwitcherRepository {
    override fun turnOnHeat(result: (UiState<String>) -> Unit) {
        database.child(FireBaseFields.SWITCHER_HEAT)
            .setValue(1)
            .addOnSuccessListener {
                result.invoke(
                    UiState.Success("Отопление включено!")
                )
                appPreferences.edit().putBoolean(SharedPrefConstants.SWITCHER_STATUS, true).apply()

            }
            .addOnFailureListener {
                result.invoke(
                    UiState.Failure(
                        it.localizedMessage
                    )
                )
            }
    }

    override fun turnOffHeat(result: (UiState<String>) -> Unit) {
        database.child(FireBaseFields.SWITCHER_HEAT)
            .setValue(0)
            .addOnSuccessListener {
                result.invoke(
                    UiState.Success("Отопление выключено!")
                )
                appPreferences.edit().putBoolean(SharedPrefConstants.SWITCHER_STATUS, false)
                    .apply()
            }
            .addOnFailureListener {
                result.invoke(
                    UiState.Failure(
                        it.localizedMessage
                    )
                )
            }
    }


}