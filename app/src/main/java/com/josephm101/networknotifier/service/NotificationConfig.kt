package com.josephm101.networknotifier.service

import com.josephm101.networknotifier.notifications.NotificationChannelConfig

data class NotificationConfig(
    var notificationId: Int,
    var notificationChannelConfig: NotificationChannelConfig,
)