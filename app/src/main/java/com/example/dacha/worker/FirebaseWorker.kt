package com.example.dacha.worker

import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.dacha.MainActivity
import com.example.dacha.R
import com.example.dacha.util.Constants
import com.google.firebase.database.DatabaseReference
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

@HiltWorker
class FirebaseWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters,
    val database: DatabaseReference
) : CoroutineWorker(context, workerParameters) {
    private val TAG = "FirebaseWorker"
    private val docPath = "readyStatus"

    private suspend fun getData() = withContext(Dispatchers.IO) {
        database.child(docPath)
            .get()
            .await()
            .value
    }

    override suspend fun doWork(): Result {
        val data = try {
            getData()
        } catch (e: Exception) {
            val errMsg = "Error message"
            Log.e(TAG, errMsg, e)
            return Result.retry()
        }
        if (data as Boolean) {
            showNotification()
        } else return Result.retry()
        Log.d(TAG, "$data")
        return Result.success()
    }


    private fun showNotification() {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = TaskStackBuilder.create(applicationContext).run {
            // Add the intent, which inflates the back stack
            addNextIntentWithParentStack(intent)
            // Get the PendingIntent containing the entire back stack
            getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        }


        val notification =
            NotificationCompat.Builder(applicationContext, Constants.CHANNEL_ID_ONE_TIME_WORK)
                .apply {
                    setContentIntent(pendingIntent)
                }
        notification.setContentTitle("ONE TIME WORK")
        notification.setContentText(Constants.ONETIME_WORK_DESCRIPTION)
        notification.priority = NotificationCompat.PRIORITY_HIGH
        notification.setCategory(NotificationCompat.CATEGORY_ALARM)
        notification.setSmallIcon(R.drawable.ic_launcher_background)
        val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        notification.setSound(sound)
        val vibrate = longArrayOf(0, 100, 200, 300)
        notification.setVibrate(vibrate)

        with(NotificationManagerCompat.from(applicationContext)) {
            notify(2, notification.build())
        }
    }

}
