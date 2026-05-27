package com.example.data.api

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AtsApiResponse(
    val score: Int,
    val missingKeywords: List<String>,
    val turkishCv: String,
    val englishCv: String,
    val analysisFeedback: String
)
