package ua.pasinfosc

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import ua.pasinfosc.data.di.appModule
import ua.pasinfosc.data.di.networkModule
import ua.pasinfosc.data.di.repositoriesModule
import ua.pasinfosc.data.di.useCasesModule

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MyApp)
            modules(appModule, networkModule, repositoriesModule, useCasesModule)
        }
    }
}