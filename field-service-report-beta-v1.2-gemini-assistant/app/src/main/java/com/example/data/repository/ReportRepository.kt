package com.example.data.repository

import com.example.data.local.ReportDao
import com.example.data.model.FieldServiceReport
import kotlinx.coroutines.flow.Flow

class ReportRepository(private val reportDao: ReportDao) {
    val allReports: Flow<List<FieldServiceReport>> = reportDao.getAllReports()
    val draftReports: Flow<List<FieldServiceReport>> = reportDao.getDraftReports()

    fun searchReports(query: String): Flow<List<FieldServiceReport>> =
        reportDao.searchReports(query)

    suspend fun getReportById(id: Long): FieldServiceReport? =
        reportDao.getReportById(id)

    suspend fun getLatestDraft(): FieldServiceReport? =
        reportDao.getLatestDraft()

    suspend fun saveReport(report: FieldServiceReport): Long {
        val updatedReport = report.copy(updatedAt = System.currentTimeMillis())
        return if (report.id == 0L) {
            reportDao.insertReport(updatedReport)
        } else {
            reportDao.updateReport(updatedReport)
            report.id
        }
    }

    suspend fun deleteReport(report: FieldServiceReport) =
        reportDao.deleteReport(report)

    suspend fun deleteReportById(id: Long) =
        reportDao.deleteReportById(id)

    suspend fun deleteReportsByUnitNo(unitNo: String) =
        reportDao.deleteReportsByUnitNo(unitNo)

    suspend fun deleteEmptyDrafts() =
        reportDao.deleteEmptyDrafts()
}
