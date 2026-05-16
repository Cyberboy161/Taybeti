package com.nulldata.app

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.nulldata.app.ui.navigation.AppNavGraph
import com.nulldata.app.ui.theme.NulldDataTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // FLAG_SECURE — prevent screenshots and screen recording
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        enableEdgeToEdge()

        val app = application as NulldataApp

        setContent {
            NulldDataTheme(darkTheme = true) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavGraph(repository = app.container.noteRepository)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // Hint to clear decrypted content
        System.gc()
    }

    override fun onStop() {
        super.onStop()
        System.gc()
    }
}
