package org.eos.mynoti.data.remote

import org.eos.mynoti.data.remote.dto.AnalyzeNotificationRequest
import org.eos.mynoti.data.remote.dto.AnalyzeNotificationResponse
import org.eos.mynoti.data.remote.dto.BatchAnalyzeRequest
import org.eos.mynoti.data.remote.dto.BatchAnalyzeResponse
import org.eos.mynoti.data.remote.dto.HealthResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @POST("api/v1/notifications/analyze")
    suspend fun analyze(
        @Body request: AnalyzeNotificationRequest
    ): AnalyzeNotificationResponse

    @POST("api/v1/notifications/analyze/batch")
    suspend fun analyzeBatch(
        @Body request: BatchAnalyzeRequest
    ): BatchAnalyzeResponse

    @GET("api/v1/health")
    suspend fun health(): HealthResponse
}
