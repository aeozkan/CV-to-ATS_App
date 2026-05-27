package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ats_reports")
data class AtsReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val originalCvText: String,
    val targetedPosition: String,
    val jobDescription: String,
    val extraGuidance: String,
    val score: Int,
    val missingKeywordsJson: String, // Comma-separated or serialized JSON list of keywords
    val turkishCvMarkdown: String,
    val englishCvMarkdown: String,
    val analysisFeedbackMarkdown: String
)
