package com.example.dacha

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.example.dacha.util.Constants.CHANNEL_ID_ONE_TIME_WORK
import com.example.dacha.util.Constants.CHANNEL_ID_PERIOD_WORK
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject


@HiltAndroidApp
class DachaApp : Application(), Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    override fun getWorkManagerConfiguration(): Configuration =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()


    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channelPeriodic = NotificationChannel(CHANNEL_ID_PERIOD_WORK, "Period Work Request", importance)
            channelPeriodic.description = "Periodic Work"
            val channelInstant = NotificationChannel(CHANNEL_ID_ONE_TIME_WORK, "One Time Work Request", importance)
            channelInstant.description  = "One Time Work"
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = applicationContext.getSystemService(
                NotificationManager::class.java
            )
            notificationManager!!.createNotificationChannel(channelPeriodic)
            notificationManager.createNotificationChannel(channelInstant)
        }
    }
}