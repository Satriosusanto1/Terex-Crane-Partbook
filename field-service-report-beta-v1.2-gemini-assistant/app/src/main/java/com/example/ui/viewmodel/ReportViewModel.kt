package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.FieldServiceReport
import com.example.data.model.PartItem
import com.example.data.model.parsePartsJson
import com.example.data.model.toJsonString
import com.example.data.repository.ReportRepository
import com.example.util.PdfReportGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReportViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ReportRepository = ReportRepository(AppDatabase.getDatabase(application).reportDao())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()


    val reports: StateFlow<List<FieldServiceReport>>

    private val _currentReport = MutableStateFlow(FieldServiceReport())
    val currentReport: StateFlow<FieldServiceReport> = _currentReport.asStateFlow()

    val currentPartsList = mutableStateListOf<PartItem>()

    private val _previewPdfFile = MutableStateFlow<File?>(null)
    val previewPdfFile: StateFlow<File?> = _previewPdfFile.asStateFlow()

    private val _isGeneratingPdf = MutableStateFlow(false)
    val isGeneratingPdf: StateFlow<Boolean> = _isGeneratingPdf.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    private val _autoSaveStatus = MutableStateFlow("Auto-Save Aktif")
    val autoSaveStatus: StateFlow<String> = _autoSaveStatus.asStateFlow()

    private var autoSaveJob: Job? = null

    val latestDraft: StateFlow<FieldServiceReport?> = repository.draftReports
        .flatMapLatest { drafts ->
            val validDraft = drafts.firstOrNull { draft ->
                draft.unitNo.isNotBlank() || draft.jobDescription.isNotBlank() || !draft.hourmeterPhotoUri.isNullOrBlank()
            }
            kotlinx.coroutines.flow.flowOf(validDraft)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    init {
        reports = _searchQuery
            .debounce(200)
            .distinctUntilChanged()
            .flatMapLatest { query ->
                if (query.isBlank()) {
                    repository.allReports
                } else {
                    repository.searchReports(query)
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        // Clean up legacy sample and empty untitled ghost drafts on app start
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteReportsByUnitNo("CR-8012")
            repository.deleteEmptyDrafts()
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun initNewReport() {
        val today = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        _currentReport.value = FieldServiceReport(
            companyHeader = "LIEBHERR",
            reportTitle = "FIELD SERVICE REPORT",
            customerName = "PT. Amman Mineral",
            projectSite = "Batu Hijau",
            unitBrand = "LIEBHERR",
            type = "Mobile and Crawler Crane",
            reportDate = today,
            preparedByPosition = "",
            acknowledgeByPosition = "Supervisor Mechanic / Technical Expert",
            clientAcknowledgeTitle = "Acknowledge By PT. AMNT",
            clientAcknowledgePosition = "Site Supervisor",
            partsRequiredJson = "[]",
            isDraft = true
        )
        currentPartsList.clear()
        currentPartsList.add(PartItem())
        _autoSaveStatus.value = "Draft Baru (Auto-Save Aktif)"
    }

    fun loadReport(id: Long) {
        viewModelScope.launch {
            val report = repository.getReportById(id)
            if (report != null) {
                _currentReport.value = report
                currentPartsList.clear()
                val parts = parsePartsJson(report.partsRequiredJson)
                if (parts.isEmpty()) {
                    currentPartsList.add(PartItem())
                } else {
                    currentPartsList.addAll(parts)
                }
                _autoSaveStatus.value = if (report.isDraft) "Draft Tersimpan di Room" else "Laporan Final (Tersimpan)"
            }
        }
    }

    fun restoreLatestDraft(onSuccess: (FieldServiceReport) -> Unit = {}) {
        viewModelScope.launch {
            val latestDraft = repository.getLatestDraft()
            if (latestDraft != null) {
                _currentReport.value = latestDraft
                currentPartsList.clear()
                val parts = parsePartsJson(latestDraft.partsRequiredJson)
                if (parts.isEmpty()) {
                    currentPartsList.add(PartItem())
                } else {
                    currentPartsList.addAll(parts)
                }
                _autoSaveStatus.value = "Draft Dipulihkan dari Room"
                onSuccess(latestDraft)
            }
        }
    }

    fun updateCurrentReport(update: (FieldServiceReport) -> FieldServiceReport) {
        _currentReport.value = update(_currentReport.value)
        triggerDebouncedAutoSave()
    }

    fun updateUnitNo(unitNo: String) {
        val matched = com.example.data.model.UnitDatabase.findByUnitNo(unitNo)
        _currentReport.value = if (matched != null) {
            _currentReport.value.copy(
                unitNo = unitNo,
                type = matched.model,
                serialNo = matched.serialNo,
                unitBrand = matched.brand
            )
        } else {
            _currentReport.value.copy(unitNo = unitNo)
        }
        triggerDebouncedAutoSave()
    }

    fun selectUnitCatalogItem(item: com.example.data.model.UnitCatalogItem) {
        _currentReport.value = _currentReport.value.copy(
            unitNo = item.unitNo,
            type = item.model,
            serialNo = item.serialNo,
            unitBrand = item.brand
        )
        triggerDebouncedAutoSave()
    }

    private fun triggerDebouncedAutoSave() {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            _autoSaveStatus.value = "Menyimpan..."
            delay(500)
            saveDraftImmediateInternal(isPeriodic = false)
        }
    }

    private suspend fun saveDraftImmediateInternal(isPeriodic: Boolean = false) {
        val report = _currentReport.value
        // Only persist if the user has entered meaningful inspection content
        val hasMeaningfulContent = report.unitNo.isNotBlank() ||
                report.jobDescription.isNotBlank() ||
                report.superstructureHourmeter.isNotBlank() ||
                report.serialNo.isNotBlank() ||
                report.completedAction.isNotBlank() ||
                !report.hourmeterPhotoUri.isNullOrBlank() ||
                !report.defect1PhotoUri.isNullOrBlank() ||
                !report.defect2PhotoUri.isNullOrBlank() ||
                report.preparedByName.isNotBlank() ||
                currentPartsList.any { it.partNumber.isNotBlank() || it.partDescription.isNotBlank() }

        if (report.id == 0L && !hasMeaningfulContent) {
            _autoSaveStatus.value = "Auto-Save Siap"
            return
        }

        val partsJson = currentPartsList.filter {
            it.partNumber.isNotBlank() || it.partDescription.isNotBlank() || it.qty.isNotBlank()
        }.toJsonString()

        val draftReport = report.copy(
            partsRequiredJson = partsJson,
            isDraft = if (report.id == 0L) true else report.isDraft,
            updatedAt = System.currentTimeMillis()
        )

        val savedId = repository.saveReport(draftReport)
        if (_currentReport.value.id == 0L) {
            _currentReport.value = _currentReport.value.copy(id = savedId)
        }
        val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        _autoSaveStatus.value = "Otomatis tersimpan ($timeStr)"
    }

    fun saveDraftImmediate() {
        viewModelScope.launch {
            saveDraftImmediateInternal(isPeriodic = false)
        }
    }

    // Parts Required table methods
    fun addPartItem() {
        currentPartsList.add(PartItem())
        triggerDebouncedAutoSave()
    }

    fun updatePartItem(index: Int, part: PartItem) {
        if (index in currentPartsList.indices) {
            currentPartsList[index] = part
            triggerDebouncedAutoSave()
        }
    }

    fun removePartItem(index: Int) {
        if (index in currentPartsList.indices) {
            currentPartsList.removeAt(index)
            triggerDebouncedAutoSave()
        }
    }

    fun saveReport(isDraft: Boolean, onSaved: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val partsJson = currentPartsList.filter {
                it.partNumber.isNotBlank() || it.partDescription.isNotBlank() || it.qty.isNotBlank()
            }.toJsonString()

            val updatedReport = _currentReport.value.copy(
                partsRequiredJson = partsJson,
                isDraft = isDraft
            )

            val savedId = repository.saveReport(updatedReport)
            _currentReport.value = updatedReport.copy(id = savedId)
            _userMessage.value = if (isDraft) "Draft laporan tersimpan" else "Laporan FSR berhasil disimpan"
            onSaved(savedId)
        }
    }

    fun generatePdfAndPreview(context: Context, onReady: (File) -> Unit = {}) {
        viewModelScope.launch {
            _isGeneratingPdf.value = true
            try {
                // Ensure latest parts are synced into current report
                val partsJson = currentPartsList.filter {
                    it.partNumber.isNotBlank() || it.partDescription.isNotBlank() || it.qty.isNotBlank()
                }.toJsonString()

                val reportToExport = _currentReport.value.copy(partsRequiredJson = partsJson)

                // Pre-translate findings, actions, and conclusions online (with instant offline technical engine fallback)
                withContext(Dispatchers.IO) {
                    val texts = listOf(
                        reportToExport.history,
                        reportToExport.defect1Findings,
                        reportToExport.defect2Findings,
                        reportToExport.hourmeterFindings,
                        reportToExport.completedAction,
                        reportToExport.conclusion1,
                        reportToExport.conclusion2,
                        reportToExport.conclusion3
                    )
                    texts.filter { it.isNotBlank() }.forEach { text ->
                        try {
                            com.example.util.OnlineTranslationManager.translateToEnglish(text)
                        } catch (_: Exception) {}
                    }
                }

                val file = withContext(Dispatchers.IO) {
                    PdfReportGenerator.generateReportPdf(context, reportToExport)
                }

                _previewPdfFile.value = file
                onReady(file)
            } catch (e: Exception) {
                e.printStackTrace()
                _userMessage.value = "Gagal membuat PDF: ${e.localizedMessage}"
            } finally {
                _isGeneratingPdf.value = false
            }
        }
    }

    fun dismissPdfPreview() {
        _previewPdfFile.value = null
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun deleteReport(report: FieldServiceReport) {
        viewModelScope.launch {
            repository.deleteReport(report)
            _userMessage.value = "Laporan Unit ${report.unitNo} telah dihapus"
        }
    }

    fun toggleReportSyncStatus(report: FieldServiceReport) {
        viewModelScope.launch {
            val newSyncState = !report.isSynced
            val updated = report.copy(isSynced = newSyncState, updatedAt = System.currentTimeMillis())
            repository.saveReport(updated)
            _userMessage.value = if (newSyncState) {
                "Laporan Unit ${report.unitNo.ifBlank { "Unit" }}: Tersinkronisasi"
            } else {
                "Laporan Unit ${report.unitNo.ifBlank { "Unit" }}: Pending Sync (Data Lokal)"
            }
        }
    }

    fun syncAllPendingReports() {
        viewModelScope.launch {
            val pendingList = reports.value.filter { !it.isSynced }
            if (pendingList.isEmpty()) {
                _userMessage.value = "Semua laporan lokal sudah tersinkronisasi"
                return@launch
            }
            pendingList.forEach { report ->
                repository.saveReport(report.copy(isSynced = true, updatedAt = System.currentTimeMillis()))
            }
            _userMessage.value = "${pendingList.size} laporan berhasil disinkronkan"
        }
    }
}
