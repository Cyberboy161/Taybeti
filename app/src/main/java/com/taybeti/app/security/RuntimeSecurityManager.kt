package com.taybeti.app.security

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.os.Build
import android.provider.Settings
import android.view.WindowManager
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import java.io.File

object RuntimeSecurityManager {

    private var securityChecksEnabled = true
    private var isScreenMirroring = false
    private var onSecurityViolation: ((String) -> Unit)? = null

    fun initialize(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        onViolation: (String) -> Unit
    ) {
        onSecurityViolation = onViolation

        lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                if (!securityChecksEnabled) return
                runAllChecks(context)
            }
        })
    }

    fun enableChecks(enabled: Boolean) {
        securityChecksEnabled = enabled
    }

    private fun runAllChecks(context: Context) {
        val violations = mutableListOf<String>()

        if (isDeviceRooted()) {
            violations.add("ROOT_DETECTED")
        }

        if (isUsbDebuggingEnabled(context)) {
            violations.add("USB_DEBUGGING_ENABLED")
        }

        if (hasScreenOverlay(context)) {
            violations.add("SCREEN_OVERLAY_DETECTED")
        }

        val suspiciousAccessibility = getSuspiciousAccessibilityServices(context)
        if (suspiciousAccessibility.isNotEmpty()) {
            violations.add("SUSPICIOUS_ACCESSIBILITY: ${suspiciousAccessibility.joinToString(", ")}")
        }

        isScreenMirroring = isScreenMirroringActive(context)
        if (isScreenMirroring) {
            violations.add("SCREEN_MIRRORING_DETECTED")
        }

        if (violations.isNotEmpty()) {
            onSecurityViolation?.invoke(violations.joinToString("; "))
        }
    }

    // ─── Root Detection ───

    fun isDeviceRooted(): Boolean {
        return checkRootMethod1() || checkRootMethod2() || checkRootMethod3()
    }

    private fun checkRootMethod1(): Boolean {
        val buildTags = Build.TAGS
        return buildTags != null && buildTags.contains("test-keys")
    }

    private fun checkRootMethod2(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/system/xbin/daemonsu",
            "/system/etc/init.d/99SuperSUDaemon",
            "/system/bin/.ext/.su",
            "/system/etc/.has_su_daemon",
            "/system/etc/.install_su_daemon",
            "/system/xbin/su",
            "/system/bin/su",
            "/system/app/Superuser",
            "/sbin/su",
            "/vendor/bin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/data/local/su",
            "/su/bin/su",
            "/system/bin/failsafe/su",
            "/magisk"
        )
        return paths.any { File(it).exists() }
    }

    private fun checkRootMethod3(): Boolean {
        return try {
            Runtime.getRuntime().exec("su").waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }

    // ─── USB Debugging Detection ───

    fun isUsbDebuggingEnabled(context: Context): Boolean {
        return try {
            Settings.Secure.getInt(
                context.contentResolver,
                Settings.Global.ADB_ENABLED,
                0
            ) == 1
        } catch (e: Exception) {
            try {
                Settings.Secure.getInt(
                    context.contentResolver,
                    Settings.Secure.ADB_ENABLED,
                    0
                ) == 1
            } catch (e2: Exception) {
                false
            }
        }
    }

    fun isDevelopmentModeEnabled(context: Context): Boolean {
        return try {
            Settings.Secure.getInt(
                context.contentResolver,
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
                0
            ) == 1
        } catch (e: Exception) {
            false
        }
    }

    // ─── Screen Overlay Detection ───

    fun hasScreenOverlay(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            !Settings.canDrawOverlays(context)
        } else {
            false
        }
    }

    fun canDrawOverOtherApps(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    // ─── Accessibility Service Check ───

    fun getSuspiciousAccessibilityServices(context: Context): List<String> {
        val suspiciousPackages = setOf(
            "com.teamviewer.quicksupport.market",
            "com.teamviewer.host.market",
            "com.splashtop.streamer.addon",
            "com.realvnc.viewer.android",
            "com.microsoft.rdc.androidx",
            "com.anydesk.anydeskandroid",
            "com.screenovate.mobile",
            "com.bomberoid.screenrecorder",
            "com.koushikdutta.vysor",
            "com.develper.accessibility"
        )

        val accessibilityManager = context.getSystemService(
            Context.ACCESSIBILITY_SERVICE
        ) as? android.view.accessibility.AccessibilityManager

        val enabledServices = accessibilityManager?.getEnabledAccessibilityServiceList(
            AccessibilityServiceInfo.FEEDBACK_ALL_MASK
        ) ?: return emptyList()

        return enabledServices
            .mapNotNull { it.resolveInfo?.serviceInfo?.packageName }
            .filter { suspiciousPackages.contains(it) }
            .distinct()
    }

    fun getEnabledAccessibilityServices(context: Context): List<String> {
        val accessibilityManager = context.getSystemService(
            Context.ACCESSIBILITY_SERVICE
        ) as? android.view.accessibility.AccessibilityManager

        val enabledServices = accessibilityManager?.getEnabledAccessibilityServiceList(
            AccessibilityServiceInfo.FEEDBACK_ALL_MASK
        ) ?: return emptyList()

        return enabledServices.mapNotNull {
            val info = it.resolveInfo?.serviceInfo
            if (info != null) {
                val label = info.loadLabel(context.packageManager)
                "$label (${info.packageName})"
            } else null
        }
    }

    // ─── Screen Mirroring / Casting Detection ───

    fun isScreenMirroringActive(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val displayManager = context.getSystemService(
                Context.DISPLAY_SERVICE
            ) as? DisplayManager

            displayManager?.displays?.forEach { display ->
                if (display.displayId != android.view.Display.DEFAULT_DISPLAY) {
                    return true
                }
            }
        }

        val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Check if FLAG_SECURE is being respected
            // If screen recording is active, this might not work perfectly
            // but we can check for virtual displays
        }

        // Check for known screen recording/casting packages running
        val castingPackages = setOf(
            "com.google.android.apps.chromecast.app",
            "com.sec.chromecast",
            "com.samsung.android.app.mirrorlink",
            "com.microsoft.xboxone.smartglass"
        )

        val pm = context.packageManager
        castingPackages.forEach { pkg ->
            try {
                val appInfo = pm.getApplicationInfo(pkg, 0)
                if (isAppRunning(context, pkg)) {
                    return true
                }
            } catch (e: PackageManager.NameNotFoundException) {
                // Not installed
            }
        }

        return false
    }

    private fun isAppRunning(context: Context, packageName: String): Boolean {
        val activityManager = context.getSystemService(
            Context.ACTIVITY_SERVICE
        ) as? android.app.ActivityManager

        val processes = activityManager?.getRunningAppProcesses() ?: return false
        return processes.any { it.processName == packageName }
    }

    // ─── Emulator Detection ───

    fun isRunningOnEmulator(): Boolean {
        return (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
            || Build.FINGERPRINT.startsWith("generic")
            || Build.FINGERPRINT.startsWith("unknown")
            || Build.HARDWARE.contains("goldfish")
            || Build.HARDWARE.contains("ranchu")
            || Build.MODEL.contains("google_sdk")
            || Build.MODEL.contains("Emulator")
            || Build.MODEL.contains("Android SDK")
            || Build.BOARD.lowercase().contains("nox")
            || Build.BOOTLOADER.lowercase().contains("nox")
            || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
            || "google_sdk" == Build.PRODUCT
    }

    // ─── Debugger Detection ───

    fun isDebuggerConnected(): Boolean {
        return android.os.Debug.isDebuggerConnected()
    }

    // ─── Combined Security Report ───

    data class SecurityReport(
        val isRooted: Boolean,
        val isUsbDebuggingEnabled: Boolean,
        val isDevelopmentMode: Boolean,
        val hasScreenOverlay: Boolean,
        val suspiciousAccessibilityServices: List<String>,
        val isScreenMirroring: Boolean,
        val isEmulator: Boolean,
        val isDebuggerConnected: Boolean
    )

    fun generateSecurityReport(context: Context): SecurityReport {
        return SecurityReport(
            isRooted = isDeviceRooted(),
            isUsbDebuggingEnabled = isUsbDebuggingEnabled(context),
            isDevelopmentMode = isDevelopmentModeEnabled(context),
            hasScreenOverlay = hasScreenOverlay(context),
            suspiciousAccessibilityServices = getSuspiciousAccessibilityServices(context),
            isScreenMirroring = isScreenMirroringActive(context),
            isEmulator = isRunningOnEmulator(),
            isDebuggerConnected = isDebuggerConnected()
        )
    }
}
