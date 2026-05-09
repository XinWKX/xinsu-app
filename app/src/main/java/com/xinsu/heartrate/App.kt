package com.xinsu.heartrate

import android.app.Application

class App : Application() {

    override fun onCreate() {

        super.onCreate()

        Thread.setDefaultUncaughtExceptionHandler(

            CrashHandler(this)
        )
    }
}
