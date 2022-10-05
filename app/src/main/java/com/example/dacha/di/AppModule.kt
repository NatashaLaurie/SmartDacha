package com.example.dacha.di

import com.example.dacha.ConnectivityObserver
import com.example.dacha.NetworkConnectivityObserver
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
abstract class AppModule {

    @Binds
    abstract fun bindConnectivityObserver(
        connectivityObserver: NetworkConnectivityObserver
    ): ConnectivityObserver
}