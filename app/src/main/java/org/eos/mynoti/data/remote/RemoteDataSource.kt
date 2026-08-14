package org.eos.mynoti.data.remote

import org.eos.mynoti.BuildConfig
import org.eos.mynoti.data.remote.dto.AnalyzeNotificationRequest
import org.eos.mynoti.data.remote.dto.AnalyzeNotificationResponse
import org.eos.mynoti.data.remote.dto.BatchAnalyzeRequest
import org.eos.mynoti.data.remote.dto.BatchAnalyzeResponse
import org.eos.mynoti.data.remote.dto.HealthResponse

class RemoteDataSource(
    private val apiService: ApiService
) {
    suspend fun analyze(request: AnalyzeNotificationRequest): AnalyzeNotificationResponse {
        return apiService.analyze(request)
    }

    suspend fun analyzeBatch(request: BatchAnalyzeRequest): BatchAnalyzeResponse {
        return apiService.analyzeBatch(request)
    }

    suspend fun health(): HealthResponse {
        return apiService.health()
    }
}

object NetworkModule {
    fun createApiService(): ApiService {
        val logging = okhttp3.logging.HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                okhttp3.logging.HttpLoggingInterceptor.Level.BASIC
            } else {
                okhttp3.logging.HttpLoggingInterceptor.Level.NONE
            }
        }
        val client = okhttp3.OkHttpClient.Builder()
            .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(90, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header(ApiConfig.API_KEY_HEADER, ApiConfig.apiKey)
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(logging)
            .build()

        return retrofit2.Retrofit.Builder()
            .baseUrl(ApiConfig.baseUrl)
            .client(client)
            .addConverterFactory(
                retrofit2.converter.gson.GsonConverterFactory.create(
                    com.google.gson.GsonBuilder().create()
                )
            )
            .build()
            .create(ApiService::class.java)
    }
}
