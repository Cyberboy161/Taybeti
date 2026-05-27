package com.taybeti.app

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.taybeti.app.security.SecurityChecker
import com.taybeti.app.ui.navigation.AppNavGraph
import com.taybeti.app.ui.theme.NulldDataTheme
import com.taybeti.app.util.AppDisguise
import com.taybeti.app.util.LocaleManager

class MainActivity : ComponentActivity() {

    private var isMirroringPaused = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val langCode = LocaleManager.getCurrentLanguage(this)
        LocaleManager.applyLocale(this, langCode)

        // Restore app disguise (icon/name)
        val currentDisguise = AppDisguise.getCurrentDisguise(this)
        AppDisguise.applyDisguise(this, currentDisguise)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        enableEdgeToEdge()

        val app = application as TaybetiApp

        setContent {
            var isDarkTheme by rememberSaveable { mutableStateOf(true) }
            val layoutDirection = if (langCode == "ckb") LayoutDirection.Rtl else LayoutDirection.Ltr
            NulldDataTheme(darkTheme = isDarkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    androidx.compose.runtime.CompositionLocalProvider(
                        LocalLayoutDirection provides layoutDirection
                    ) {
                        AppNavGraph(
                            repository = app.container.noteRepository,
                            isDarkTheme = isDarkTheme,
                            onToggleTheme = { isDarkTheme = it }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (SecurityChecker.isScreenMirroring(this)) {
            if (!isMirroringPaused) {
                isMirroringPaused = true
                Toast.makeText(
                    this,
                    "Screen mirroring detected. App paused for security.",
                    Toast.LENGTH_LONG
                ).show()
            }
            moveTaskToBack(true)
            return
        }

        isMirroringPaused = false

        if (SecurityChecker.isRooted(this)) {
            Toast.makeText(
                this,
                "Warning: Rooted device detected",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onPause() {
        super.onPause()
        System.gc()
    }

    override fun onStop() {
        super.onStop()
        System.gc()
    }
}
