package com.josephm101.networknotifier.notifications

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import androidx.core.app.NotificationCompat
import com.josephm101.networknotifier.R

enum class AppNotifications(val notificationChannel: AppNotificationChannels, val notificationId: Int) {
    PersistentServiceNotification(
        notificationChannel = AppNotificationChannels.ServiceNotificationChannel,
        notificationId = 1
    ) {
        override fun asNotification(context: Context): Notification {
            return NotificationCompat.Builder(context, AppNotificationChannels.ServiceNotificationChannel.channelId)
                .setSmallIcon(R.drawable.outline_wifi_notification_24)
                .setContentTitle("Network Notifier service is running")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setOngoing(true)
                .build()
        }
        override fun post(context: Context) {
            val notificationManager: NotificationManager =
                context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(notificationId, asNotification(context))
        }

        override fun cancel(context: Context) {
            val notificationManager: NotificationManager =
                context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(notificationId)
        }
    },
    MobileDataActiveNotification(
        notificationChannel = AppNotificationChannels.MobileDataActiveNotificationChannel,
        notificationId = 10
    ) {
        override fun asNotification(context: Context): Notification {
            return NotificationCompat.Builder(context, notificationChannel.channelId)
                .setSmallIcon(R.drawable.baseline_cell_tower_24)
                .setContentTitle(context.getString(R.string.mobileDataActiveNotification_title))
                .setContentText(context.getString(R.string.mobileDataActiveNotification_text))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                //.setCategory(Notification.CATEGORY_ALARM)
                //.setOngoing(true)
                .build()
        }
        override fun post(context: Context) {
            val notificationManager: NotificationManager =
                context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(notificationId, asNotification(context))
        }

        override fun cancel(context: Context) {
            val notificationManager: NotificationManager =
                context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(notificationId)
        }
    };

    abstract fun post(context: Context)
    abstract fun cancel(context: Context)
    abstract fun asNotification(context: Context): Notification
}