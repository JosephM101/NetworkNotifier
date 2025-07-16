package com.josephm101.networknotifier

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import android.Manifest
import android.net.Uri
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.josephm101.networknotifier.ui.theme.NetworkNotifierTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        startService(Intent(this, NetworkMonitoringService::class.java))
        
        setContent {
            val notificationPermissionState = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)

            NetworkNotifierTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when {
                        notificationPermissionState.status.isGranted -> MobileDataCheckTest(
                            modifier = Modifier.padding(innerPadding),
                            context = this
                        )
                        else -> {
                            NotificationPermissionView(this, notificationPermissionState)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MobileDataCheckTest(modifier: Modifier = Modifier, context: Context) {
    //context.startService(Intent(context, NetworkMonitoringService::class.java))
    Row(horizontalArrangement = Arrangement.Center, modifier = modifier.fillMaxSize()) {
        Column(verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxHeight()) {
            Button(
                onClick = {
                    checkConn(context)
                },
                content = {
                    Text("Check connectivity")
                },
                modifier = modifier
            )
        }
    }
}

fun checkConn(context: Context) {
    val toastLength = Toast.LENGTH_SHORT
    val mobileActive = checkMobileNetworkIsActive(context)
    if (mobileActive) {
        Toast.makeText(context, "Mobile network active", toastLength).show()
    } else {
        Toast.makeText(context, "Mobile network not active/connected", toastLength)
            .show()
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun NotificationPermissionView(context: Context, notificationPermissionState: PermissionState) {
    LaunchedEffect(Unit) {
        notificationPermissionState.launchPermissionRequest()
    }
    Box(Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)) {
        Column(Modifier.padding(vertical = 120.dp, horizontal = 16.dp)) {
            Icon(
                Icons.Rounded.Notifications,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(8.dp))
            Text("Notification permission required", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(4.dp))
            Text("This is required in order for Network Notifier to send you alerts.")
        }
        val context = LocalContext.current
        Button(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            onClick = {
                val intent =
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                context.startActivity(intent)
            }) {
            Text("Go to settings")
        }
    }
}