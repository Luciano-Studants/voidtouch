package com.voidtouch.launcher

import android.app.Application

class VoidApp : Application() {
    override fun onCreate() {
        super.onCreate()
        CrashGuard.install(this)
    }
}
