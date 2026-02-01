package ru.makscorp.project.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import ru.makscorp.project.data.storage.ContextStorage
import ru.makscorp.project.data.storage.SettingsStorage
import ru.makscorp.project.data.storage.TokenStorage

val androidModule = module {
    single { TokenStorage(androidContext()) }
    single { SettingsStorage(androidContext()) }
    single { ContextStorage(androidContext()) }
}
