package com.josephm101.networknotifier.preferences

import android.content.Context
import hu.autsoft.krate.SimpleKrate
import hu.autsoft.krate.booleanPref
import hu.autsoft.krate.default.withDefault

// Uses Krate library. See: https://github.com/ZenitechSoftware/Krate

class AppPreferences(context: Context) : SimpleKrate(context) {
    var serviceEnabled by booleanPref().withDefault(true)
}