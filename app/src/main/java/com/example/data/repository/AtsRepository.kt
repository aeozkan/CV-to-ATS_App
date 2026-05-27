package com.example.data.repository

import com.example.BuildConfig
import com.example.data.api.AtsApiResponse
import com.example.data.api.Content
import com.example.data.api.GenerateContentRequest
import com.example.data.api.GenerationConfig
import com.example.data.api.Part
import com.example.data.api.RetrofitClient
import com.example.data.database.AtsReportDao
import com.example.data.database.AtsReportEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AtsRepository(private val reportDao: AtsReportDao) {

    val allReports: Flow<List<AtsReportEntity>> = reportDao.getAllReports()

    suspend fun saveReport(report: AtsReportEntity): Long = withContext(Dispatchers.IO) {
        reportDao.insertReport(report)
    }

    suspend fun deleteReport(id: Long) = withContext(Dispatchers.IO) {
        reportDao.deleteReportById(id)
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        reportDao.deleteAllReports()
    }

    suspend fun generateAtsCv(
        originalCv: String,
        position: String,
        jobDescription: String,
        extraGuidance: String
    ): AtsApiResponse = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            throw IllegalStateException("Lütfen Gemini API anahtarınızı AI Studio Secrets panelinden tanımlayın.")
        }

        val promptText = buildPrompt(originalCv, position, jobDescription, extraGuidance)

        val request = GenerateContentRequest(
            contents = listOf(
                Content(
                    parts = listOf(
                        Part(text = promptText)
                    )
                )
            ),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.2f
            )
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            val rawText = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: throw IllegalStateException("Gemini modelinden boş bir yanıt döndü.")

            parseAtsResponse(rawText)
                ?: throw IllegalStateException("Gemini yanıtı beklenen JSON formatında ayrıştırılamadı.")

        } catch (e: Exception) {
            throw e
        }
    }

    private fun buildPrompt(
        originalCv: String,
        position: String,
        jobDescription: String,
        extraGuidance: String
    ): String {
        val targetPositionSection = if (position.isNotBlank()) "Target Position:\n$position\n" else ""
        val jobDescSection = if (jobDescription.isNotBlank()) "Target Job Description / Requirements:\n$jobDescription\n" else ""
        val guidanceSection = if (extraGuidance.isNotBlank()) "Focus/Directives / Extra Guidance:\n$extraGuidance\n" else ""

        return """
            You are a professional HR Specialist, Recruiter, and ATS (Applicant Tracking System) parser and optimizer engine.
            Your task is to transform the provided CV text into two highly professional, clean, ATS-friendly markdown resumes:
            1. One completely in Turkish (TR)
            2. One completely in English (EN)
            
            Moreover, you will evaluate the original resume against standard industry metrics for the target position or targeted job listing and output a resume analysis.

            Input details:
            $targetPositionSection
            $jobDescSection
            $guidanceSection
            
            Original CV content:
            $originalCv
            
            ATS CONVENTIONS & DESIGN RULES:
            - Layout: Use readable markdown, with clear head levels (e.g. ### Experience, ### Education).
            - Content: Make the CV rich with action verbs (e.g. "managed", "designed", "yönetti", "geliştirdi") and accomplishment-driven bullet points (using the STAR format: Situation, Task, Action, Result).
            - Avoid tables, dynamic visual progress bars for skills, colorful bars, or graphic ratings. List skills as comma-separated items or clean lists.
            - Provide reverse-chronological order for Work Experience and Education.
            - Fill in any typical sections: Contact Details, Summary/Objective, Experience, Education, Technical/Core Skills, Languages, Certifications.
            - Ensure both TR and EN versions are complete and formatted perfectly inside markdown. Do not abbreviate or write placeholders like "[giriş tarihi buraya]". If date is missing in source, use a generic interval or keep what was provided.
            
            EVALUATION & SCORING DETAILS:
            - If a Target Job Description was provided:
              1. Score the resume (0 to 100) based on how well the skills match the job listing.
              2. Extract exact "missingKeywords" (key technologies, skills, languages, or methodologies present in the Job Description but absent or weak in the Original CV).
              3. Provide an insightful "analysisFeedback" in Turkish summarizing missing requirements and actionable advice to make the candidate land an interview.
            - If NO Target Job Description was provided:
              1. Evaluate the resume against standard ATS expectations for the targeted position (or generic modern resume standards if position is empty).
              2. Set a general score (0 to 100).
              3. Identify "missingKeywords" representing important industry words typical for this position that were omitted.
              4. Provide actionable "analysisFeedback" in Turkish explaining how to improve phrasing, structure, and keyword density.
              
            You MUST return ONLY a JSON object and nothing else. No explanation, no wrapper text, just the raw JSON contents matching this structure:
            
            {
              "score": 85,
              "missingKeywords": ["Kotlin", "Dynamic Theming", "CI/CD"],
              "turkishCv": "### TÜRKÇE ATS CV MARKDOWN KODU...",
              "englishCv": "### ENGLISH ATS CV MARKDOWN KODU...",
              "analysisFeedback": "### ATS SKOR LİSTESİ VE GERİ BİLDİRİM KARTI..."
            }
            
            Ensure the JSON is strictly compliant. Wrap both 'turkishCv', 'englishCv' and 'analysisFeedback' properly as valid escaped JSON strings (use \n for linebreaks, escape any double quotes).
        """.trimIndent()
    }

    private fun parseAtsResponse(rawText: String): AtsApiResponse? {
        var cleanText = rawText.trim()
        
        // Strip out triple-backtick markdown blocks if Gemini added them
        if (cleanText.startsWith("```")) {
            val lines = cleanText.lines()
            if (lines.isNotEmpty()) {
                val hasMimeType = lines.first().contains("json", ignoreCase = true)
                val startIdx = if (hasMimeType) 1 else 1
                val endIdx = if (lines.last().startsWith("```")) lines.size - 1 else lines.size
                cleanText = lines.subList(startIdx, endIdx).joinToString("\n").trim()
            }
        }
        
        return try {
            val adapter = RetrofitClient.responseMoshi.adapter(AtsApiResponse::class.java)
            adapter.fromJson(cleanText)
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback: If parsing fails, try manual stripping or regex
            try {
                val startBracket = cleanText.indexOf('{')
                val endBracket = cleanText.lastIndexOf('}')
                if (startBracket != -1 && endBracket != -1 && endBracket > startBracket) {
                    val fallbackText = cleanText.substring(startBracket, endBracket + 1)
                    val adapter = RetrofitClient.responseMoshi.adapter(AtsApiResponse::class.java)
                    adapter.fromJson(fallbackText)
                } else null
            } catch (inner: Exception) {
                inner.printStackTrace()
                null
            }
        }
    }
}
