package com.example.dacha.data.repository

import android.content.SharedPreferences
import com.example.dacha.util.FireBaseFields
import com.example.dacha.util.SharedPrefConstants
import com.example.dacha.util.UiState
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

class HeatingRepositoryImpl(
    val database: DatabaseReference,
    val appPreferences: SharedPreferences
) : HeatingRepository {

    override fun getCurrentTemperature(result: (UiState<Float>) -> Unit) {
        database.child(FireBaseFields.CURRENT_TEMPERATURE)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val temperature = snapshot.getValue(Float::class.java)
                    if (temperature == null) {
                        result.invoke(
                            UiState.Loading
                        )
                    } else {
                        result.invoke(UiState.Success(temperature))
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    result.invoke(
                        UiState.Failure(error.message)
                    )
                }
            })
    }

    override fun setRequiredTemperature(
        requiredTemp: String,
        result: (UiState<Pair<String, String>>) -> Unit
    ) {
        database.child(FireBaseFields.REQUIRED_TEMPERATURE)
            .setValue(requiredTemp.dropLast(2).toInt())
            .addOnSuccessListener {
                result.invoke(
                    UiState.Success(Pair(requiredTemp, "Температура установлена"))
                )
                appPreferences.edit().putString(SharedPrefConstants.REQUIRED_TEMPERATURE, requiredTemp).apply()
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