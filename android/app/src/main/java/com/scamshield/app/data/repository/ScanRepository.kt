package com.scamshield.app.data.repository

import android.content.Context
import android.util.Log
import com.scamshield.app.data.local.ScamDatabase
import com.scamshield.app.data.local.ScanRecordEntity
import com.scamshield.app.data.model.AnalyzeRequest
import com.scamshield.app.data.network.NetworkClient
import com.scamshield.app.engine.LocalScamFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ScanRepository(private val context: Context) {

    private val db = ScamDatabase.getDatabase(context)
    private val dao = db.scanHistoryDao()

    val allScans: Flow<List<ScanRecordEntity>> = dao.getAllScans()
    val threatsOnly: Flow<List<ScanRecordEntity>> = dao.getThreatsOnly()
    val totalScansCount: Flow<Int> = dao.getTotalScansCount()
    val threatsCount: Flow<Int> = dao.getThreatsCount()
    val recentThreat: Flow<ScanRecordEntity?> = dao.getRecentThreat()

    fun getScanById(id: Long): Flow<ScanRecordEntity?> = dao.getScanById(id)

    suspend fun clearHistory() {
        withContext(Dispatchers.IO) {
            dao.clearAll()
        }
    }

    /**
     * Complete pipeline:
     * 1. Local edge heuristic filtering.
     * 2. If suspicious -> deeper AI backend call.
     * 3. Seamless offline fallback.
     * 4. Privacy-preserving persistence (no raw plaintext message stored).
     */
    suspend fun processIncomingNotification(
        content: String,
        sender: String,
        packageName: String
    ): ScanRecordEntity = withContext(Dispatchers.IO) {
        // Step 1: On-device local filter
        val localResult = LocalScamFilter.evaluateLocally(content)

        var finalScore = localResult.riskScore
        var finalClassification = localResult.classification
        var finalCategory = localResult.category
        var finalIndicators = localResult.indicators
        var finalRecommendation = localResult.recommendation
        var finalWhatToDo = localResult.whatToDo
        var finalWhatNotToDo = localResult.whatNotToDo
        var usedEngine = "local_heuristic"

        // Step 2: If message has suspicious indicators, query backend for deeper AI analysis
        if (localResult.shouldQueryBackend) {
            try {
                val api = NetworkClient.getApiService(context)
                val response = api.analyzeMessage(
                    AnalyzeRequest(
                        content = content,
                        sender = sender,
                        packageName = packageName
                    )
                )

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    finalScore = body.riskScore
                    finalClassification = body.classification
                    finalCategory = body.category
                    finalIndicators = body.indicators
                    finalRecommendation = body.recommendation
                    if (body.whatToDo.isNotEmpty()) finalWhatToDo = body.whatToDo
                    if (body.whatNotToDo.isNotEmpty()) finalWhatNotToDo = body.whatNotToDo
                    usedEngine = body.engine
                } else {
                    Log.w("ScanRepo", "Backend returned error ${response.code()}, falling back to local filter.")
                    usedEngine = "local_fallback"
                }
            } catch (e: Exception) {
                Log.w("ScanRepo", "Backend unreachable: ${e.message}. Using offline local protection.")
                usedEngine = "local_offline"
            }
        }

        // Ensure what_to_do and what_not_to_do are never empty
        if (finalWhatToDo.isEmpty()) {
            finalWhatToDo = LocalScamFilter.generateWhatToDo(finalCategory, finalClassification)
        }
        if (finalWhatNotToDo.isEmpty()) {
            finalWhatNotToDo = LocalScamFilter.generateWhatNotToDo(finalCategory, finalClassification)
        }

        // Step 3: Record creation (stores on-device snippet for the safe messaging inbox display)
        val record = ScanRecordEntity(
            timestamp = System.currentTimeMillis(),
            sourcePackage = packageName,
            senderTitle = sender,
            messageSnippet = content.trim().take(300),
            riskScore = finalScore,
            classification = finalClassification,
            category = finalCategory,
            indicatorsCsv = finalIndicators.joinToString(" • "),
            whatToDoCsv = finalWhatToDo.joinToString(" | "),
            whatNotToDoCsv = finalWhatNotToDo.joinToString(" | "),
            recommendation = finalRecommendation,
            engine = usedEngine,
            isThreat = finalScore >= 70,
            isDismissed = false
        )

        // Step 4: Persist to local database
        val generatedId = dao.insertScan(record)
        return@withContext record.copy(id = generatedId)
    }
}
