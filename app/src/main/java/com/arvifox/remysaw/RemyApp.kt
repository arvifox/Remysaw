package com.arvifox.remysaw

import android.app.Application
import timber.log.Timber
import java.io.File

class RemyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        initLogger()
    }

    private val path: String by lazy {
        applicationContext.externalMediaDirs.firstOrNull()?.absolutePath + File.separator + "logs.txt"
    }

    private fun initLogger() {
        if (BuildConfig.DEBUG) {
            Timber.plant(object : Timber.DebugTree() {
                override fun createStackElementTag(element: StackTraceElement): String {
                    return "REMY_LOG: ${Thread.currentThread().name}"
                }
            })
            try {
                Runtime.getRuntime()
                    .exec("logcat -f $path -v threadtime -r ${1024 * 4} -n 5")
            } catch (t: Throwable) {
                Timber.e(t)
            }
            Timber.d("logger has been started")
        }
    }
}