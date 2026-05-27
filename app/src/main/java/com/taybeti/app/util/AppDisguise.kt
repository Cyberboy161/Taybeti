package com.taybeti.app.util

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

object AppDisguise {
    private const val PREF_NAME = "app_disguise"
    private const val KEY_ACTIVE = "active_alias"

    val options = listOf(
        "MainActivityTaybeti" to "Taybeti Notesharing",
        "MainActivityCalc" to "Calculator",
        "MainActivityNotes" to "Notes",
        "MainActivityClock" to "Clock",
        "MainActivitySettings" to "Settings"
    )

    fun getCurrentDisguise(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_ACTIVE, "MainActivityTaybeti") ?: "MainActivityTaybeti"
    }

    fun applyDisguise(context: Context, aliasSuffix: String) {
        val pm = context.packageManager
        val packageName = context.packageName

        options.forEach { (suffix, _) ->
            val comp = ComponentName(packageName, "$packageName.$suffix")
            val state = if (suffix == aliasSuffix)
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            else
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            pm.setComponentEnabledSetting(comp, state, PackageManager.DONT_KILL_APP)
        }

        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_ACTIVE, aliasSuffix).apply()
    }
}
