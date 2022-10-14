package com.example.dacha.data.repository

import android.content.SharedPreferences
import com.example.dacha.util.SharedPrefConstants
import com.example.dacha.util.UiState
import com.google.firebase.auth.FirebaseAuth

class AuthRepositoryImp(
    val auth: FirebaseAuth,
    val appPreferences: SharedPreferences

): AuthRepository {

    override fun logIn(login: String, password: String, result: (UiState<String>) -> Unit) {
        auth.signInWithEmailAndPassword(login,password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    result.invoke(UiState.Success("Login successfully!"))
                    val user = task.result.user?.uid
                    appPreferences.edit().putString(SharedPrefConstants.USER_SESSION,user).apply()
                }
            }.addOnFailureListener {
                result.invoke(UiState.Failure("Authentication failed, Check email and password"))
            }
    }

    override fun getSession(result: (String?) -> Unit) {
        val userUid = appPreferences.getString(SharedPrefConstants.USER_SESSION,null)
        if (userUid == null){
            result.invoke(null)
        }else{
            result.invoke(userUid)
        }
    }
}