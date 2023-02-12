package ca.amandeep.nycairportsecuritylinewaits

import android.app.Application
import timber.log.Timber
import timber.log.Timber.DebugTree

class AirportsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }
    }
}
