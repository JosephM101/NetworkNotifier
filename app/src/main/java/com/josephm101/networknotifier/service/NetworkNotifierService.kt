package com.josephm101.networknotifier.service

import android.app.Service
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.os.IBinder
import android.util.Log
import com.josephm101.networknotifier.helpers.checkMobileNetworkIsActive
import com.josephm101.networknotifier.notifications.AppNotificationChannels
import com.josephm101.networknotifier.notifications.AppNotifications

class NetworkNotifierService : Service() {
    private val LogTag = "NetworkNotifierService"

    lateinit var connectivityManager: ConnectivityManager

    val networkCallback: ConnectivityManager.NetworkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            Log.d(LogTag,"networkCallback: onAvailable")
            val mobileNetworkIsActive = checkMobileNetworkIsActive(applicationContext)
            if (mobileNetworkIsActive) {
                Log.d(LogTag,"Mobile network is active")
                AppNotifications.MobileDataActiveNotification.post(applicationContext)
            } else {
                Log.d(LogTag,"Mobile network is not connected/active")
                AppNotifications.MobileDataActiveNotification.cancel(applicationContext)
            }
        }
        override fun onLost(network: Network) {
            Log.d(LogTag,"onLost")
            AppNotifications.MobileDataActiveNotification.cancel(applicationContext)
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
        Log.d(LogTag, "Creating notification channels")
        // On Android SDK versions >= Android 8.0 (Oreo), notification channels must be created and registered, and notifications posted to them.
        // The app will crash if this is not done.
        AppNotificationChannels.createNotificationChannels(applicationContext)

        Log.d(LogTag, "startForeground()")
        //startForeground()

        startForeground(AppNotifications.PersistentServiceNotification.notificationId, AppNotifications.PersistentServiceNotification.asNotification(applicationContext))
        Log.d(LogTag, "NetworkNotifier service started")

        return START_STICKY
    }

    /*
    private fun createNotificationChannels() {
        // Create notification channel for service notification
        val persistentServiceNotificationChannel = NotificationChannel(
            serviceNotificationChannelConfig.channelId,
            serviceNotificationChannelConfig.channelDisplayName,
            NotificationManager.IMPORTANCE_LOW
        )

        // Create notification channel for "mobile data in use" alert notification
        val mobileDataActiveNotificationChannel = NotificationChannel(
            mobileDataActiveNotificationChannelConfig.channelId,
            mobileDataActiveNotificationChannelConfig.channelDisplayName,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = getString(R.string.notificationChannel_service_description)
        }
        mobileDataActiveNotificationChannel.enableVibration(true)

        // Register notification channels
        val notificationManager =
            getSystemService(NotificationManager::class.java) as NotificationManager
        notificationManager.createNotificationChannel(persistentServiceNotificationChannel)
        notificationManager.createNotificationChannel(mobileDataActiveNotificationChannel)
    }
     */

    override fun onDestroy() {
        Log.d(LogTag, "Destroying")

        // Our service is going down; unregister the callback to prevent memory leaks
        connectivityManager.unregisterNetworkCallback(networkCallback)

        super.onDestroy()
    }
}