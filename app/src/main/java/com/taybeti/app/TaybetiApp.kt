package com.taybeti.app

import android.app.Application
import android.content.ComponentCallbacks2
import com.taybeti.app.di.AppContainer
import com.taybeti.app.security.SecureMemory

class TaybetiApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW) {
            // Clear sensitive data on memory pressure
            SecureMemory.clear(ByteArray(0)) // GC hint
            System.gc()
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        System.gc()
    }
}
