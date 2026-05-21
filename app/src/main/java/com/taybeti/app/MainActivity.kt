package com.taybeti.app

import android.os.Bundle
import android.view.WindowManager
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
import com.taybeti.app.ui.navigation.AppNavGraph
import com.taybeti.app.ui.theme.NulldDataTheme
import com.taybeti.app.util.LocaleManager

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val langCode = LocaleManager.getCurrentLanguage(this)
        LocaleManager.applyLocale(this, langCode)

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

    override fun onPause() {
        super.onPause()
        System.gc()
    }

    override fun onStop() {
        super.onStop()
        System.gc()
    }
}
