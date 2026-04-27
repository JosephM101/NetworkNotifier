package com.josephm101.networknotifier.helpers

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

fun checkMobileNetworkIsActive(context: Context): Boolean {
    // Get an instance of the ConnectivityManager service
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager

    // Get information about the active network. Returns a Network object.
    // If null, we return `false`.
    val network = connectivityManager.activeNetwork ?: return false

    // Get the capabilities of the active network
    val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

    return when {
        // Check if the current network connection is over mobile data/cellular
        // (will be true even if phone is connected to Wi-Fi but is actively using mobile data as transport, such as when the Wi-Fi connection is "unstable" or "weak")
        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
        else -> false
    }
}