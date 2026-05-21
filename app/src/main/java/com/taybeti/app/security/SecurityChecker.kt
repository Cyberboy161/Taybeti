package com.taybeti.app.security

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.provider.Settings
import android.view.Display
import android.view.accessibility.AccessibilityManager
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

data class SecurityStatus(
    val isRooted: Boolean = false,
    val isUsbDebuggingEnabled: Boolean = false,
    val hasScreenOverlay: Boolean = false,
    val hasSuspiciousAccessibilityServices: Boolean = false,
    val isScreenMirroring: Boolean = false,
    val suspiciousServices: List<String> = emptyList()
)

object SecurityChecker {

    private val ROOT_INDICATORS = listOf(
        "/system/app/Superuser.apk",
        "/system/xbin/which",
        "/system/xbin/su",
        "/system/bin/su",
        "/sbin/su",
        "/vendor/bin/su",
        "/cache/su",
        "/data/su",
        "/data/local/xbin/su",
        "/data/local/bin/su",
        "/system/sd/xbin/su",
        "/system/bin/failsafe/su",
        "/data/local/su",
        "/su/bin/su"
    )

    private val SUSPICIOUS_PACKAGES = listOf(
        "eu.chainfire.supersu",
        "com.koushikdutta.superuser",
        "com.thirdparty.superuser",
        "com.noshufou.android.su",
        "com.yellowes.su",
        "com.topjohnwu.magisk",
        "com.kingroot.kinguser",
        "com.kingo.root",
        "com.smedialink.oneclickroot",
        "com.zhiqupk.root.global",
        "com.alephzain.framaroot",
        "com.devadvance.rootcloak",
        "com.devadvance.rootcloakplus"
    )

    private val SUSPICIOUS_ACCESSIBILITY_SERVICES = listOf(
        "com.koushikdutta.rom",
        "com.devadvance.rootcloak",
        "com.devadvance.rootcloakplus",
        "eu.chainfire.supersu",
        "com.kingroot.kinguser",
        "com.kingo.root",
        "com.saurik.substrate",
        "de.robv.android.xposed.installer",
        "org.meowcat.edxposed.manager",
        "com.topjohnwu.magisk"
    )

    fun checkSecurity(context: Context): SecurityStatus {
        return SecurityStatus(
            isRooted = isRooted(context),
            isUsbDebuggingEnabled = isUsbDebuggingEnabled(context),
            hasScreenOverlay = hasScreenOverlay(context),
            hasSuspiciousAccessibilityServices = checkAccessibilityServices(context).first,
            suspiciousServices = checkAccessibilityServices(context).second,
            isScreenMirroring = isScreenMirroring(context)
        )
    }

    fun isRooted(context: Context): Boolean {
        if (Build.TAGS?.contains("test-keys") == true) return true

        for (path in ROOT_INDICATORS) {
            if (File(path).exists()) return true
        }

        for (pkg in SUSPICIOUS_PACKAGES) {
            try {
                context.packageManager.getPackageInfo(pkg, 0)
                return true
            } catch (_: Exception) {
            }
        }

        return try {
            val process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su"))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val result = reader.readLine()
            reader.close()
            result != null
        } catch (_: Exception) {
            false
        }
    }

    fun isUsbDebuggingEnabled(context: Context): Boolean {
        return try {
            Settings.Secure.getInt(
                context.contentResolver,
                Settings.Secure.ADB_ENABLED,
                0
            ) == 1
        } catch (_: Exception) {
            false
        }
    }

    fun hasScreenOverlay(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return false
        return try {
            Settings.canDrawOverlays(context).not().not() &&
                Settings.canDrawOverlays(context)
        } catch (_: Exception) {
            false
        }
    }

    fun checkAccessibilityServices(context: Context): Pair<Boolean, List<String>> {
        val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
            ?: return false to emptyList()

        val enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(
            AccessibilityServiceInfo.FEEDBACK_ALL_MASK
        )

        val suspicious = enabledServices
            .map { it.resolveInfo.serviceInfo.packageName }
            .filter { pkg ->
                SUSPICIOUS_ACCESSIBILITY_SERVICES.any { suspicious -> pkg.contains(suspicious) }
            }

        return suspicious.isNotEmpty() to suspicious
    }

    fun isScreenMirroring(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return false

        return try {
            val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
            val displays = displayManager.displays
            displays.any { display ->
                display.displayId != Display.DEFAULT_DISPLAY
            }
        } catch (_: Exception) {
            false
        }
    }

    fun getSecurityWarnings(context: Context): List<String> {
        val status = checkSecurity(context)
        val warnings = mutableListOf<String>()

        if (status.isRooted) {
            warnings.add("Root detected — device security is compromised")
        }
        if (status.isUsbDebuggingEnabled) {
            warnings.add("USB debugging enabled — ADB can access device data")
        }
        if (status.hasScreenOverlay) {
            warnings.add("Screen overlay detected — other apps may be drawing over Taybeti")
        }
        if (status.hasSuspiciousAccessibilityServices) {
            warnings.add("Suspicious accessibility services: ${status.suspiciousServices.joinToString(", ")}")
        }
        if (status.isScreenMirroring) {
            warnings.add("Screen mirroring/casting detected — your screen may be visible elsewhere")
        }

        return warnings
    }
}
