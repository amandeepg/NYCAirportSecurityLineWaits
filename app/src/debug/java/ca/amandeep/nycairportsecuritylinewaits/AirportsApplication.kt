package ca.amandeep.nycairportsecuritylinewaits

import android.app.Application
import timber.log.Timber

class AirportsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}
