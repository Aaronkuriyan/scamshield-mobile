package com.scamshield.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_history")
data class ScanRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val sourcePackage: String = "SMS",
    val senderTitle: String = "Unknown",
    val riskScore: Int,
    val classification: String,
    val category: String,
    val indicatorsCsv: String = "",
    val recommendation: String,
    val engine: String = "hybrid",
    val isThreat: Boolean = false,
    val isDismissed: Boolean = false
)
