package com.josephm101.networknotifier

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat

class NetworkMonitoringService : Service() {
    private val TAG = "NetworkMonitoringService"

    lateinit var connectivityManager: ConnectivityManager

    val networkCallback: ConnectivityManager.NetworkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            Log.d(TAG,"networkCallback: onAvailable")
            val mobileNetworkIsActive = checkMobileNetworkIsActive(applicationContext)
            if (mobileNetworkIsActive) {
                Log.d(TAG,"Mobile network is active")
                val mobileDataActiveNotification = NotificationCompat.Builder(applicationContext, mobileDataActiveNotificationChannelConfig.id)
                    .setSmallIcon(R.drawable.baseline_cell_tower_24)
                    .setContentTitle(getString(R.string.mobileDataActiveNotification_Title))
                    .setContentText(getString(R.string.mobileDataActiveNotification_Text))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    //.setCategory(Notification.CATEGORY_ALARM)
                    //.setOngoing(true)
                    .build()

                val notificationManager: NotificationManager =
                    getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(mobileDataActiveNotificationConfig.id, mobileDataActiveNotification)
            } else {
                Log.d(TAG,"Mobile network is not connected/active")
                cancelNotification(mobileDataActiveNotificationConfig)
            }
        }
        override fun onLost(network: Network) {
            Log.d(TAG,"onLost")
            cancelNotification(mobileDataActiveNotificationConfig)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerDefaultNetworkCallback(networkCallback)

        //super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Creating notification channels")
        // On Android SDK versions >= Android 8.0 (Oreo), notification channels must be created and registered, and notifications posted to them.
        // The app will crash if this is not done.
        createNotificationChannels()

        Log.d(TAG, "startForeground()")
        startForeground()

        return START_STICKY
    }

    private fun createNotificationChannels() {
        // Create notification channel for service notification
        val persistentServiceNotificationChannel = NotificationChannel(
            serviceNotificationChannelConfig.id,
            serviceNotificationChannelConfig.name,
            NotificationManager.IMPORTANCE_LOW
        )

        // Create notification channel for "mobile data in use" alert notification
        val mobileDataActiveNotificationChannel = NotificationChannel(
            mobileDataActiveNotificationChannelConfig.id,
            mobileDataActiveNotificationChannelConfig.name,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Mobile data is in use"
        }
        mobileDataActiveNotificationChannel.enableVibration(true)

        // Register notification channels
        val notificationManager =
            getSystemService(NotificationManager::class.java) as NotificationManager
        notificationManager.createNotificationChannel(persistentServiceNotificationChannel)
        notificationManager.createNotificationChannel(mobileDataActiveNotificationChannel)
    }


    // This function is intended for older versions of Android
    private fun startForeground() {
        val notification = NotificationCompat.Builder(applicationContext, serviceNotificationChannelConfig.id)
            .setPriority(NotificationManager.IMPORTANCE_HIGH)
            .build()
        notification.flags = notification.flags or Notification.FLAG_FOREGROUND_SERVICE
        notification.flags = notification.flags or Notification.FLAG_NO_CLEAR
        notification.flags = notification.flags or Notification.FLAG_ONGOING_EVENT
        startForeground(100, notification)
        Log.d(TAG, "Network monitoring foreground service started")

/*
        try {

        } catch (e: Exception) {
            Log.e(TAG, "Foreground service failed to start.")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                && e is ForegroundServiceStartNotAllowedException
            ) {
                // App not in a valid state to start foreground service
                // (e.g. started from bg)
                Log.e(TAG, "Foreground service failed to start.")
            }
        }
 */
    }

    fun cancelNotification(notification: NotificationConfig) {
        val notificationManager: NotificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notification.id)
    }

    override fun onDestroy() {
        Log.d(TAG, "Destroying")

        // Our service is going down; unregister the callback to prevent memory leaks
        connectivityManager.unregisterNetworkCallback(networkCallback)

        super.onDestroy()
    }

    companion object {
        // Notification channel configurations
        val serviceNotificationChannelConfig = NotificationChannelConfig(
            id = "SERVICE_NOTIFICATION",
            name = "Persistent Service Notification"
        )

        val mobileDataActiveNotificationChannelConfig = NotificationChannelConfig(
            id = "MOBILE_DATA_ACTIVE_ALERT",
            name = "Mobile data active"
        )

        // Notification configurations
        val mobileDataActiveNotificationConfig = NotificationConfig(
            channel = mobileDataActiveNotificationChannelConfig,
            id = 10,
        )
    }
}