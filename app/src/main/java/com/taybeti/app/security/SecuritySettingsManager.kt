package com.taybeti.app.security

import android.content.Context

object SecuritySettingsManager {

    private const val PREFS_NAME = "security_settings"

    // Memory & Data
    private const val KEY_AUTO_CLEAR_CLIPBOARD = "auto_clear_clipboard"
    private const val KEY_SHRED_DELETED_NOTES = "shred_deleted_notes"
    private const val KEY_EXCLUDE_FROM_RECENTS = "exclude_from_recents"
    private const val KEY_DISABLE_LOCKSCREEN_PREVIEW = "disable_lockscreen_preview"

    // App Integrity
    private const val KEY_CHECK_APP_SIGNATURE = "check_app_signature"
    private const val KEY_DETECT_EMULATOR = "detect_emulator"
    private const val KEY_ANTI_DEBUGGING = "anti_debugging"
    private const val KEY_CHECK_SUSPICIOUS_PROCESSES = "check_suspicious_processes"

    // File Security
    private const val KEY_SECURE_FILE_DELETION = "secure_file_deletion"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Memory & Data
    fun getAutoClearClipboard(context: Context): Boolean =
        prefs(context).getBoolean(KEY_AUTO_CLEAR_CLIPBOARD, false)

    fun setAutoClearClipboard(context: Context, value: Boolean) =
        prefs(context).edit().putBoolean(KEY_AUTO_CLEAR_CLIPBOARD, value).apply()

    fun getShredDeletedNotes(context: Context): Boolean =
        prefs(context).getBoolean(KEY_SHRED_DELETED_NOTES, false)

    fun setShredDeletedNotes(context: Context, value: Boolean) =
        prefs(context).edit().putBoolean(KEY_SHRED_DELETED_NOTES, value).apply()

    fun getExcludeFromRecents(context: Context): Boolean =
        prefs(context).getBoolean(KEY_EXCLUDE_FROM_RECENTS, false)

    fun setExcludeFromRecents(context: Context, value: Boolean) =
        prefs(context).edit().putBoolean(KEY_EXCLUDE_FROM_RECENTS, value).apply()

    fun getDisableLockscreenPreview(context: Context): Boolean =
        prefs(context).getBoolean(KEY_DISABLE_LOCKSCREEN_PREVIEW, false)

    fun setDisableLockscreenPreview(context: Context, value: Boolean) =
        prefs(context).edit().putBoolean(KEY_DISABLE_LOCKSCREEN_PREVIEW, value).apply()

    // App Integrity
    fun getCheckAppSignature(context: Context): Boolean =
        prefs(context).getBoolean(KEY_CHECK_APP_SIGNATURE, false)

    fun setCheckAppSignature(context: Context, value: Boolean) =
        prefs(context).edit().putBoolean(KEY_CHECK_APP_SIGNATURE, value).apply()

    fun getDetectEmulator(context: Context): Boolean =
        prefs(context).getBoolean(KEY_DETECT_EMULATOR, false)

    fun setDetectEmulator(context: Context, value: Boolean) =
        prefs(context).edit().putBoolean(KEY_DETECT_EMULATOR, value).apply()

    fun getAntiDebugging(context: Context): Boolean =
        prefs(context).getBoolean(KEY_ANTI_DEBUGGING, false)

    fun setAntiDebugging(context: Context, value: Boolean) =
        prefs(context).edit().putBoolean(KEY_ANTI_DEBUGGING, value).apply()

    fun getCheckSuspiciousProcesses(context: Context): Boolean =
        prefs(context).getBoolean(KEY_CHECK_SUSPICIOUS_PROCESSES, false)

    fun setCheckSuspiciousProcesses(context: Context, value: Boolean) =
        prefs(context).edit().putBoolean(KEY_CHECK_SUSPICIOUS_PROCESSES, value).apply()

    // File Security
    fun getSecureFileDeletion(context: Context): Boolean =
        prefs(context).getBoolean(KEY_SECURE_FILE_DELETION, false)

    fun setSecureFileDeletion(context: Context, value: Boolean) =
        prefs(context).edit().putBoolean(KEY_SECURE_FILE_DELETION, value).apply()
}
