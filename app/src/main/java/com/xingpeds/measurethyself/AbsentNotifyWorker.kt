package com.xingpeds.measurethyself

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlinx.datetime.Clock

@ExperimentalTime
class AbsentNotifyWorker(val context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {
    val snoozeTime = Duration.days(1)
    companion object {
        val workName: String = "myName"
    }
    val workName = "absentWork"
    val mostRecentComp
        get() =
            PersistJsonSource(context)
                .load()
                .flatten()
                .sortedByDescending { comp -> comp.timeStamp }
                .first()
    override fun doWork(): Result {
        Log.d("ugh", "work triggered")
        // get the data
        val data = PersistJsonSource(context = context).load()
        // compute last completion
        val comps: List<Completion> = data.flatMap { task -> task }
        if (comps.isEmpty()) {
            // remind user to use app
        } else {
            // if more than 24 hours notify user
            val sorted = comps.sortedByDescending { comp -> comp.timeStamp }
            val now = Clock.System.now()
            val distance = now - sorted.first().timeStamp
            if (distance > snoozeTime) {
                // notify user its been a while
                Notifier(context = context).showNotif(distance)
            }
        }

        return Result.success()
    }
}

class Notifier(val context: Context) {
    val CHANNEL_ID = "AN ID"
    val myintent = Intent(context, MainActivity::class.java)
    val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, myintent, 0)
    val builder =
        NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Missing measurements?")
            .setSmallIcon(com.xingpeds.measurethyself.R.drawable.ic_baseline_playlist_add_check_24)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            // Set the intent that will fire when the user taps the notification
            .setAutoCancel(true)
    @ExperimentalTime
    val showNotif: (duration: Duration) -> Unit = {
        builder.setContentText("its been ${it.toString()} Since your last completion")
        with(NotificationManagerCompat.from(context)) {
            // notificationId is a unique int for each notification that you must define

            notify(Random.nextInt(), builder.build())
        }
    }
    private val channelName = "absent reminder"
    private val notificationChannelDescription =
        "remind the user to keep using the app after a period of absence"
    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel =
                NotificationChannel(CHANNEL_ID, channelName, importance).apply {
                    description = notificationChannelDescription
                }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
