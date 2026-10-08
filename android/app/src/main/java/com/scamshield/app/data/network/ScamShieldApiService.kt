package com.scamshield.app.data.network

import com.scamshield.app.data.model.AnalyzeRequest
import com.scamshield.app.data.model.AnalyzeResponse
import com.scamshield.app.data.model.CategoryInfo
import com.scamshield.app.data.model.HealthResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ScamShieldApiService {

    @POST("api/analyze")
    suspend fun analyzeMessage(
        @Body request: AnalyzeRequest
    ): Response<AnalyzeResponse>

    @GET("api/health")
    suspend fun checkHealth(): Response<HealthResponse>

    @GET("api/categories")
    suspend fun getCategories(): Response<List<CategoryInfo>>
}
