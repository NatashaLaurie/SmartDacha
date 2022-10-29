package com.example.dacha.di

import android.content.Context
import android.content.SharedPreferences
import com.example.dacha.data.repository.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth,
        appPreferences: SharedPreferences
    ): AuthRepository {
        return AuthRepositoryImp(auth, appPreferences)
    }

    @Provides
    @Singleton
    fun providesHeatingRepository(
        database: DatabaseReference,
        appPreferences: SharedPreferences
    ): HeatingRepository {
        return HeatingRepositoryImpl(database, appPreferences)
    }

    @Provides
    @Singleton
    fun providesSwitcherRepository(
        database: DatabaseReference,
        @ApplicationContext appContext: Context
    ): SwitcherRepository {
        return SwitcherRepositoryImpl(database, appContext)
    }
}