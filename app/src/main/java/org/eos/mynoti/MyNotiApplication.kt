package org.eos.mynoti

import android.app.Application
import org.eos.mynoti.di.AppContainer

class MyNotiApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
