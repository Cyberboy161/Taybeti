package com.taybeti.app.security

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import java.io.File

object AppIntegrityChecker {

    private val EXPECTED_SIGNATURE = "YOUR_EXPECTED_SIGNATURE_HERE"

    fun checkAppSignature(context: Context): Boolean {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNING_CERTIFICATES
            )
            val signatures = packageInfo.signingInfo?.apkContentsSigners
            signatures?.isNotEmpty() == true
        } catch (_: Exception) {
            false
        }
    }

    fun detectEmulator(): Boolean {
        val indicators = listOf(
            Build.MANUFACTURER.equals("Genymotion", ignoreCase = true),
            Build.MANUFACTURER.equals("unknown", ignoreCase = true),
            Build.BRAND.equals("generic", ignoreCase = true),
            Build.DEVICE.contains("generic", ignoreCase = true),
            Build.MODEL.contains("Emulator", ignoreCase = true),
            Build.MODEL.contains("Android SDK", ignoreCase = true),
            Build.HARDWARE.contains("goldfish", ignoreCase = true),
            Build.HARDWARE.contains("ranchu", ignoreCase = true),
            Build.FINGERPRINT.contains("generic/sdk/generic", ignoreCase = true),
            File("/dev/socket/qemud").exists(),
            File("/dev/qemu_pipe").exists(),
            File("/system/lib/libc_malloc_debug_qemu.so").exists(),
            File("/sys/qemu_trace").exists(),
            File("/system/bin/qemu-props").exists()
        )

        return indicators.count { it } >= 3
    }

    fun detectDebugging(): Boolean {
        return android.os.Debug.isDebuggerConnected() ||
               android.os.Debug.waitingForDebugger() ||
               System.getProperty("java.class.path")?.contains("xposed") == true ||
               System.getProperty("java.class.path")?.contains("frida") == true ||
               detectFridaServer() ||
               detectXposedModules()
    }

    private fun detectFridaServer(): Boolean {
        val fridaProcesses = listOf("frida-server", "frida-agent", "frida-helper")
        return try {
            val process = Runtime.getRuntime().exec("ps")
            val output = process.inputStream.bufferedReader().readText()
            process.waitFor()
            fridaProcesses.any { output.contains(it) }
        } catch (_: Exception) {
            false
        }
    }

    private fun detectXposedModules(): Boolean {
        return try {
            val clazz = Class.forName("de.robv.android.xposed.XposedBridge")
            val classLoader = clazz.classLoader
            classLoader != null && classLoader.toString().contains("xposed")
        } catch (_: ClassNotFoundException) {
            false
        } catch (_: Exception) {
            false
        }
    }

    fun checkSuspiciousProcesses(): List<String> {
        val suspicious = mutableListOf<String>()
        val knownSuspicious = listOf(
            "frida-server", "frida-agent", "frida-helper",
            "xposed", "substrate", "cydia",
            "sshd", "dropbear",
            "tcpdump", "strace", "ltrace",
            "gdb", "gdbserver"
        )

        try {
            val process = Runtime.getRuntime().exec("ps")
            val output = process.inputStream.bufferedReader().readText()
            process.waitFor()

            for (line in output.lines()) {
                for (suspiciousProcess in knownSuspicious) {
                    if (line.contains(suspiciousProcess, ignoreCase = true)) {
                        suspicious.add(line.trim())
                    }
                }
            }
        } catch (_: Exception) {
        }

        return suspicious
    }

    fun runAllChecks(context: Context): List<String> {
        val warnings = mutableListOf<String>()

        if (!checkAppSignature(context)) {
            warnings.add("App signature verification failed")
        }

        if (detectEmulator()) {
            warnings.add("Running on an emulator")
        }

        if (detectDebugging()) {
            warnings.add("Debugger or hooking framework detected")
        }

        val suspiciousProcesses = checkSuspiciousProcesses()
        if (suspiciousProcesses.isNotEmpty()) {
            warnings.add("Suspicious processes detected: ${suspiciousProcesses.size}")
        }

        return warnings
    }
}
