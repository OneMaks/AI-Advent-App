package ru.makscorp.project

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import ru.makscorp.project.di.ApiConfig
import ru.makscorp.project.di.androidModule
import ru.makscorp.project.di.appModule

class ChatApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val config = ApiConfig(
            apiHost = BuildConfig.AI_API_HOST,
            authHost = BuildConfig.AI_AUTH_HOST,
            authorizationKey = BuildConfig.AI_AUTH_KEY,
            scope = BuildConfig.AI_SCOPE
        )

        startKoin {
            androidLogger()
            androidContext(this@ChatApplication)
            modules(androidModule, appModule(config))
        }
    }
}
