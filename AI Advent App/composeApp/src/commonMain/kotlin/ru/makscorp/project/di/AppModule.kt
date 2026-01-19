package ru.makscorp.project.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import ru.makscorp.project.data.api.AuthApiClient
import ru.makscorp.project.data.api.ChatApiClient
import ru.makscorp.project.data.api.createApiHttpClient
import ru.makscorp.project.data.api.createAuthHttpClient
import ru.makscorp.project.data.repository.AuthRepositoryImpl
import ru.makscorp.project.data.repository.ChatRepositoryImpl
import ru.makscorp.project.data.storage.TokenStorage
import ru.makscorp.project.domain.repository.AuthRepository
import ru.makscorp.project.domain.repository.ChatRepository
import ru.makscorp.project.presentation.chat.ChatViewModel

data class ApiConfig(
    val apiHost: String,
    val authHost: String,
    val authorizationKey: String,
    val model: String,
    val scope: String = "GIGACHAT_API_PERS"
)

fun appModule(config: ApiConfig) = module {
    // HTTP Clients
    single(named("auth")) { createAuthHttpClient() }
    single(named("api")) { createApiHttpClient(baseUrl = config.apiHost) }

    // Auth API Client
    single {
        AuthApiClient(
            httpClient = get(named("auth")),
            authUrl = config.authHost,
            authorizationKey = config.authorizationKey,
            scope = config.scope
        )
    }

    // Auth Repository
    single<AuthRepository> {
        AuthRepositoryImpl(
            authApiClient = get(),
            tokenStorage = get()
        )
    }

    // Chat API Client
    single {
        ChatApiClient(
            httpClient = get(named("api")),
            authRepository = get(),
            model = config.model
        )
    }

    // Chat Repository
    single<ChatRepository> {
        ChatRepositoryImpl(apiClient = get())
    }

    // ViewModel
    viewModelOf(::ChatViewModel)
}
