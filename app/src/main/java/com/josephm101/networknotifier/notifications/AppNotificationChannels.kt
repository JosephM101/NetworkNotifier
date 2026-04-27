package com.josephm101.networknotifier.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

enum class AppNotificationChannels(val channelId: String, val channelDisplayName: String, val channelDescription: String, val channelImportance: Int, val enableVibration: Boolean) {
    ServiceNotificationChannel(
        channelId = "NN_SERVICE_NOTIFICATION",
        channelDisplayName = "Persistent Service Notification",
        channelDescription = "Keepalive for service",
        channelImportance = NotificationManager.IMPORTANCE_LOW,
        enableVibration = false
    ),

    MobileDataActiveNotificationChannel(
        channelId = "NN_MOBILE_DATA_ACTIVE_ALERT",
        channelDisplayName = "Mobile data active",
        channelDescription = "Send notifications when your device switches to a mobile data connection",
        channelImportance = NotificationManager.IMPORTANCE_HIGH,
        enableVibration = true
    );

    companion object {
        fun createNotificationChannels(context: Context) {
            val notificationManager =
                context.getSystemService(NotificationManager::class.java) as NotificationManager
            AppNotificationChannels.entries.forEach { channel ->
                // Create channel
                val nChannel = NotificationChannel(
                    channel.channelId,
                    channel.channelDisplayName,
                    channel.channelImportance
                ).apply {
                    description = channel.channelDescription
                }
                nChannel.enableVibration(channel.enableVibration)

                notificationManager.createNotificationChannel(nChannel)
            }
        }
    }
}