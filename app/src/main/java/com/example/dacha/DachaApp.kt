package com.example.dacha

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.RingtoneManager
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

            val channelInstant = NotificationChannel(CHANNEL_ID_ONE_TIME_WORK, "One Time Work Request", importance)
            channelInstant.description  = "One Time Work"
            channelInstant.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            val vibrate = longArrayOf(0, 100, 200, 300)
            channelInstant.enableVibration(true)
            channelInstant.vibrationPattern = vibrate
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = applicationContext.getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(channelInstant)
        }
    }
}