package com.josephm101.networknotifier

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.josephm101.networknotifier.helpers.VersionHelper
import com.josephm101.networknotifier.helpers.checkMobileNetworkIsActive
import com.josephm101.networknotifier.preferences.AppPreferences
import com.josephm101.networknotifier.service.NetworkNotifierService
import com.josephm101.networknotifier.ui.components.CenteredFooterText
import com.josephm101.networknotifier.ui.components.card.CardConstants.cardDefaultBodyTextStyle
import com.josephm101.networknotifier.ui.components.card.CustomCard.CustomCardWithTitleAndIconAndContent
import com.josephm101.networknotifier.ui.components.card.SwitchCard
import com.josephm101.networknotifier.ui.theme.NetworkNotifierTheme

class MainActivity : ComponentActivity() {
    private val loopHandler = Handler(Looper.getMainLooper())
    private lateinit var appVersion: String
    private var appVersionCode: Long = 0

    private lateinit var appPreferences: AppPreferences

    fun startNetworkNotifierService() {
        startForegroundService(Intent(applicationContext, NetworkNotifierService::class.java))
    }

    private fun notImplementedToast() {
        Toast.makeText(applicationContext, "Not implemented", Toast.LENGTH_SHORT).show()
    }

    @OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // (Try to) get app version information
        try {
            val pInfo: PackageInfo = VersionHelper.getAppPackageInfo(applicationContext)
            appVersion = pInfo.versionName?.ifBlank { null } ?: "unknown"
            appVersionCode = VersionHelper.getAppVersionCode(pInfo)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            appVersion = "unknown (failed to retrieve)"
        }

        appPreferences = AppPreferences(applicationContext)

        setContent {
            NetworkNotifierTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(stringResource(id = R.string.app_name))
                            }
                        )
                    }
                ) { innerPadding ->
                    /*
                    when {
                        notificationPermissionState.status.isGranted -> MobileDataCheckTest(
                            modifier = Modifier.padding(innerPadding),
                            context = this
                        )
                        else -> {
                            NotificationPermissionView(this, notificationPermissionState)
                        }
                    }
                     */
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                    ) {
                        MainView()
                    }
                }
            }
        }
    }

    @Composable
    fun MainView() {
        Column(
            modifier = Modifier
                // .padding(8.dp)
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            PostNotificationPermissionsCard()
            if (!permissionToIgnoreBatteryOptimizationsIsGranted()) {
                BatteryOptimizationPermissionCard()
            }
            ServiceEnabledCard()

            // Draw footer
            Column(
                modifier = Modifier.padding(
                    top = 16.dp,
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 8.dp
                )
            ) {
                CenteredFooterText(stringResource(R.string.app_name))
                CenteredFooterText(
                    stringResource(
                        R.string.ui_footer_appVersionString,
                        appVersion,
                        appVersionCode
                    )
                )
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

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun PostNotificationPermissionsCard() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val showRationaleDialog = remember { mutableStateOf(false) }

            // Check the status of the POST_NOTIFICATIONS permission
            val hasNotificationPermission = remember {
                mutableStateOf(
                    ContextCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                )
            }

            // Request notification permissions
            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
                onResult = { isGranted ->
                    if (!isGranted) {
                        if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                            showRationaleDialog.value = true
                        }
                    } else {
                        hasNotificationPermission.value = true
                    }
                }
            )

            when {
                hasNotificationPermission.value -> {
                    Log.d("PostNotificationPermissions", "Permissions granted! Starting service...")
                    startNetworkNotifierService() // Start service
                }
            }

            AnimatedVisibility(
                visible = !hasNotificationPermission.value,
                enter = fadeIn() + slideInHorizontally(),
                exit = fadeOut() + slideOutHorizontally()
            ) {
                CustomCardWithTitleAndIconAndContent(
                    title = stringResource(R.string.ui_notificationPermissionsRequired_dialogTitle),
                    iconResId = R.drawable.baseline_notifications_24,
                    //cardContainerColor = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = stringResource(R.string.ui_notificationPermissionsRequired_dialogBody),
                        style = cardDefaultBodyTextStyle
                    )
                    Spacer(modifier = Modifier.size(width = 0.dp, height = 12.dp))
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                intent.data = "package:$packageName".toUri()
                                startActivity(intent)
                            } else {
                                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.request_permissions),
                            modifier = Modifier
                                .basicMarquee(velocity = 50.dp)
                        )
                    }

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            intent.data = "package:$packageName".toUri()
                            startActivity(intent)
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.open_android_settings_for_this_app),
                            modifier = Modifier
                                .basicMarquee(velocity = 50.dp)
                        )
                    }
                }
            }

            when {
                showRationaleDialog.value -> {
                    AlertDialog(
                        onDismissRequest = {
                            showRationaleDialog.value = false
                        },
                        title = {
                            Text(text = stringResource(R.string.ui_notificationPermissionsRationale_dialogTitle))
                        },
                        text = {
                            Text(stringResource(R.string.ui_notificationPermissionsRationale_dialogBody))
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showRationaleDialog.value = false
                                    val intent = Intent("android.settings.APP_NOTIFICATION_SETTINGS")
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    intent.putExtra("android.provider.extra.APP_PACKAGE", packageName)

                                    //val intent =
                                    //    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                    //intent.data = "package:$packageName".toUri()

                                    startActivity(intent)
                                },
                            ) {
                                Text(stringResource(R.string.open_settings))
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    showRationaleDialog.value = false
                                }
                            ) {
                                Text(stringResource(R.string.cancel))
                            }
                        }
                    )
                }
            }

            // Set up a loop to check the status of the permission
            val loopDuration: Long = 1000 // ms
            val notificationPermissionsGrantedTimerCheck = object : Runnable {
                override fun run() {
                    if (!hasNotificationPermission.value) {
                        if (ContextCompat.checkSelfPermission(
                                applicationContext,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            hasNotificationPermission.value = true
                        } else {
                            // Restart the timer
                            loopHandler.postDelayed(this, loopDuration)
                        }
                    }
                }
            }
            // Start the timer
            loopHandler.postDelayed(notificationPermissionsGrantedTimerCheck, loopDuration)
        }
        else {
            startNetworkNotifierService()
        }
    }

    @Composable
    fun ServiceEnabledCard() {
        /// TODO: If switch is turned off, stop the service if it is running.
        val status = remember { mutableStateOf(appPreferences.serviceEnabled) }
        SwitchCard(
            title = "Enable Service",
            description = "Enable the Network Notifier service",
            status.value,
            onCheckedChange = { newValue ->
                status.value = newValue
                appPreferences.serviceEnabled = newValue
            }
        )
    }

    @OptIn(ExperimentalFoundationApi::class)
    //@Preview
    @Composable
    fun BatteryOptimizationPermissionCard() {
        val hidden = remember { mutableStateOf(false) }
        AnimatedVisibility(
            visible = !hidden.value,
            enter = fadeIn() + slideInHorizontally(),
            exit = fadeOut() + slideOutHorizontally()
        ) {
            CustomCardWithTitleAndIconAndContent(
                title = stringResource(R.string.ui_batteryOptimizationsCard_title),
                iconResId = R.drawable.baseline_battery_saver_24
            ) {
                Text(
                    text = stringResource(R.string.ui_batteryOptimizationsCard_description),
                    style = cardDefaultBodyTextStyle
                )
                Spacer(modifier = Modifier.size(width = 0.dp, height = 12.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        requestPermissionToIgnoreBatteryOptimization()
                    }
                ) {
                    Text(
                        text = "Disable Battery Optimization",
                        modifier = Modifier
                            .basicMarquee(velocity = 50.dp)
                    )
                }
            }
        }

        // Set up a loop to periodically check the status of the permission
        val loopDuration: Long = 500 // ms
        val batteryOptimizationGrantedTimerCheck = object : Runnable {
            override fun run() {
                if (permissionToIgnoreBatteryOptimizationsIsGranted()) {
                    hidden.value = true
                } else {
                    // If permissions haven't been granted yet, start the timer again
                    loopHandler.postDelayed(this, loopDuration)
                }
            }
        }
        loopHandler.postDelayed(batteryOptimizationGrantedTimerCheck, loopDuration)
    }

    @SuppressLint("BatteryLife")
    private fun requestPermissionToIgnoreBatteryOptimization() {
        startActivity(
            Intent(
                Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                "package:$packageName".toUri()
            )
        )
    }

    private fun permissionToIgnoreBatteryOptimizationsIsGranted(): Boolean {
        val packageName = applicationContext.packageName
        val powerManager = applicationContext.getSystemService(POWER_SERVICE) as PowerManager

        return powerManager.isIgnoringBatteryOptimizations(packageName)
    }
}