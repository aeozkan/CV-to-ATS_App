package com.example.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.api.AtsApiResponse
import com.example.data.database.AtsReportEntity
import com.example.data.repository.AtsRepository
import com.example.util.DocumentUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface AtsApiState {
    object Idle : AtsApiState
    data class Loading(val step: String) : AtsApiState
    data class Success(val response: AtsApiResponse) : AtsApiState
    data class Error(val message: String) : AtsApiState
}

class AtsViewModel(private val repository: AtsRepository) : ViewModel() {

    // Input States
    private val _originalCvText = MutableStateFlow("")
    val originalCvText: StateFlow<String> = _originalCvText.asStateFlow()

    private val _targetedPosition = MutableStateFlow("")
    val targetedPosition: StateFlow<String> = _targetedPosition.asStateFlow()

    private val _jobDescription = MutableStateFlow("")
    val jobDescription: StateFlow<String> = _jobDescription.asStateFlow()

    private val _extraGuidance = MutableStateFlow("")
    val extraGuidance: StateFlow<String> = _extraGuidance.asStateFlow()

    // API Execution State
    private val _apiState = MutableStateFlow<AtsApiState>(AtsApiState.Idle)
    val apiState: StateFlow<AtsApiState> = _apiState.asStateFlow()

    // Current displayed report (from History)
    private val _displayedReport = MutableStateFlow<AtsReportEntity?>(null)
    val displayedReport: StateFlow<AtsReportEntity?> = _displayedReport.asStateFlow()

    // Saved Reports History
    val savedReports: StateFlow<List<AtsReportEntity>> = repository.allReports
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun updateOriginalCvText(text: String) {
        _originalCvText.value = text
    }

    fun updateTargetedPosition(text: String) {
        _targetedPosition.value = text
    }

    fun updateJobDescription(text: String) {
        _jobDescription.value = text
    }

    fun updateExtraGuidance(text: String) {
        _extraGuidance.value = text
    }

    fun loadFileContent(context: Context, uri: Uri) {
        viewModelScope.launch {
            _apiState.value = AtsApiState.Loading("Dosya okunuyor...")
            try {
                val extractedText = DocumentUtils.readTextFromUri(context, uri)
                _originalCvText.value = extractedText
                _apiState.value = AtsApiState.Idle
            } catch (e: Exception) {
                _apiState.value = AtsApiState.Error("Dosya okuma hatası: ${e.localizedMessage}")
            }
        }
    }

    fun handleReportSelection(report: AtsReportEntity?) {
        _displayedReport.value = report
        if (report != null) {
            // Load its details into state representation for UI tabs
            val response = AtsApiResponse(
                score = report.score,
                missingKeywords = if (report.missingKeywordsJson.isBlank()) emptyList() else report.missingKeywordsJson.split(", "),
                turkishCv = report.turkishCvMarkdown,
                englishCv = report.englishCvMarkdown,
                analysisFeedback = report.analysisFeedbackMarkdown
            )
            _apiState.value = AtsApiState.Success(response)
        } else {
            // Revert back to editor / inputs
            _apiState.value = AtsApiState.Idle
        }
    }

    fun clearInputs() {
        _originalCvText.value = ""
        _targetedPosition.value = ""
        _jobDescription.value = ""
        _extraGuidance.value = ""
        _displayedReport.value = null
        _apiState.value = AtsApiState.Idle
    }

    fun generateAtsCv() {
        val cvText = _originalCvText.value.trim()
        if (cvText.isBlank()) {
            _apiState.value = AtsApiState.Error("Lütfen önce özgeçmiş metninizi girin veya bir dosya yükleyin.")
            return
        }

        viewModelScope.launch {
            _apiState.value = AtsApiState.Loading("Bağlantı kuruluyor...")
            delay(800)
            _apiState.value = AtsApiState.Loading("Özgeçmiş analiz ediliyor...")
            delay(1000)
            _apiState.value = AtsApiState.Loading("ATS standartlarına uygun Türkçe ve İngilizce CV oluşturuluyor...")

            try {
                val apiResponse = repository.generateAtsCv(
                    originalCv = cvText,
                    position = _targetedPosition.value,
                    jobDescription = _jobDescription.value,
                    extraGuidance = _extraGuidance.value
                )

                _apiState.value = AtsApiState.Loading("Bilgiler yerel veritabanına kaydediliyor...")
                
                // Save report to Room database
                val reportEntity = AtsReportEntity(
                    timestamp = System.currentTimeMillis(),
                    originalCvText = cvText,
                    targetedPosition = _targetedPosition.value,
                    jobDescription = _jobDescription.value,
                    extraGuidance = _extraGuidance.value,
                    score = apiResponse.score,
                    missingKeywordsJson = apiResponse.missingKeywords.joinToString(", "),
                    turkishCvMarkdown = apiResponse.turkishCv,
                    englishCvMarkdown = apiResponse.englishCv,
                    analysisFeedbackMarkdown = apiResponse.analysisFeedback
                )
                
                val insertedId = repository.saveReport(reportEntity)
                _displayedReport.value = reportEntity.copy(id = insertedId)
                
                _apiState.value = AtsApiState.Success(apiResponse)

            } catch (e: Exception) {
                _apiState.value = AtsApiState.Error(e.localizedMessage ?: "Bilinmeyen bir hata oluştu.")
            }
        }
    }

    fun deleteReport(id: Long) {
        viewModelScope.launch {
            repository.deleteReport(id)
            if (_displayedReport.value?.id == id) {
                // Clear active displayed report if it got deleted
                clearInputs()
            }
        }
    }

    class Factory(private val repository: AtsRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AtsViewModel::class.java)) {
                return AtsViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
