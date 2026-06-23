package com.kiladarbar.di

import com.google.gson.GsonBuilder
import com.kiladarbar.BuildConfig
import com.kiladarbar.data.local.SessionManager
import com.kiladarbar.data.remote.ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier @Retention(AnnotationRetention.BINARY) annotation class AuthenticatedClient
@Qualifier @Retention(AnnotationRetention.BINARY) annotation class UnauthenticatedClient

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private fun loggingInterceptor() = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                else HttpLoggingInterceptor.Level.NONE
    }

    /** Unauthenticated client — for token refresh calls (avoids circular dependency) */
    @Provides @Singleton @UnauthenticatedClient
    fun provideUnauthenticatedClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor())
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

    /** Authenticated client — attaches Bearer token and auto-refreshes on 401 */
    @Provides @Singleton @AuthenticatedClient
    fun provideAuthenticatedClient(
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator,
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .authenticator(tokenAuthenticator)
            .addInterceptor(loggingInterceptor())
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

    @Provides @Singleton
    fun provideRetrofit(@AuthenticatedClient okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(
                GsonConverterFactory.create(
                    GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create()
                )
            )
            .build()

    @Provides @Singleton
    fun provideTokenAuthenticator(
        sessionManager: SessionManager,
        @UnauthenticatedClient client: OkHttpClient,
    ): TokenAuthenticator = TokenAuthenticator(sessionManager, client, BuildConfig.BASE_URL)

    @Provides @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService =
        retrofit.create(ApiService::class.java)
}

/**
 * Authenticator that handles 401 Unauthorized by attempting a token refresh.
 * Called automatically by OkHttp when a request receives a 401 response.
 */
class TokenAuthenticator(
    private val sessionManager: SessionManager,
    private val client: OkHttpClient,
    private val baseUrl: String,
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.request.header("X-Retry-Auth") != null) return null

        val refreshToken = runBlocking { sessionManager.refreshToken.first() } ?: return null

        val refreshResponse = runBlocking {
            client.newCall(
                Request.Builder()
                    .url("${baseUrl}v1/auth/refresh-token")
                    .post(
                        okhttp3.RequestBody.create(
                            "application/json; charset=utf-8".toMediaType(),
                            """{"refreshToken":"$refreshToken"}"""
                        )
                    )
                    .build()
            ).execute()
        }

        if (!refreshResponse.isSuccessful) {
            runBlocking { sessionManager.clearSession() }
            return null
        }

        val body  = refreshResponse.body?.string() ?: return null
        val regex = """"accessToken"\s*:\s*"([^"]+)"""".toRegex()
        val newToken = regex.find(body)?.groupValues?.get(1) ?: return null

        runBlocking { sessionManager.saveAccessToken(newToken) }

        return response.request.newBuilder()
            .header("Authorization", "Bearer $newToken")
            .header("X-Retry-Auth", "true")
            .build()
    }
}
