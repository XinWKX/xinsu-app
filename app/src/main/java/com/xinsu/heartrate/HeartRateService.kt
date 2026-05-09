package com.xinsu.heartrate

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder

class HeartRateService : Service() {

    override fun onCreate() {

        super.onCreate()

        createNotification()
    }

    private fun createNotification() {

        val channelId = "heartrate_service"

        if (Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.O
        ) {

            val channel = NotificationChannel(

                channelId,

                "心率监测",

                NotificationManager.IMPORTANCE_LOW
            )

            val manager =
                getSystemService(
                    NotificationManager::class.java
                )

            manager.createNotificationChannel(channel)
        }

        val notification =
            Notification.Builder(
                this,
                channelId
            )

                .setContentTitle("心宿")

                .setContentText("正在后台监测心率")

                .setSmallIcon(
                    android.R.drawable.presence_heart
                )

                .build()

        startForeground(
            1,
            notification
        )
    }

    override fun onBind(
        intent: Intent?
    ): IBinder? = null
}
