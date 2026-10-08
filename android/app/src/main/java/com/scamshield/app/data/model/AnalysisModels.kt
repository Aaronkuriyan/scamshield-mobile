package com.scamshield.app.data.model

import com.google.gson.annotations.SerializedName

enum class Classification {
    SAFE,
    SUSPICIOUS,
    SCAM
}

data class AnalyzeRequest(
    @SerializedName("content") val content: String,
    @SerializedName("sender") val sender: String? = null,
    @SerializedName("package_name") val packageName: String? = null,
    @SerializedName("metadata") val metadata: Map<String, String> = emptyMap()
)

data class AnalyzeResponse(
    @SerializedName("risk_score") val riskScore: Int,
    @SerializedName("classification") val classification: String,
    @SerializedName("category") val category: String,
    @SerializedName("confidence") val confidence: Float,
    @SerializedName("indicators") val indicators: List<String> = emptyList(),
    @SerializedName("what_to_do") val whatToDo: List<String> = emptyList(),
    @SerializedName("what_not_to_do") val whatNotToDo: List<String> = emptyList(),
    @SerializedName("recommendation") val recommendation: String,
    @SerializedName("is_safe") val isSafe: Boolean,
    @SerializedName("analyzed_at") val analyzedAt: String,
    @SerializedName("engine") val engine: String = "hybrid"
)

data class HealthResponse(
    @SerializedName("status") val status: String,
    @SerializedName("app") val app: String,
    @SerializedName("version") val version: String,
    @SerializedName("ai_available") val aiAvailable: Boolean,
    @SerializedName("ai_provider") val aiProvider: String,
    @SerializedName("ai_model") val aiModel: String,
    @SerializedName("timestamp") val timestamp: String
)

data class CategoryInfo(
    @SerializedName("category") val category: String,
    @SerializedName("description") val description: String,
    @SerializedName("example") val example: String
)
