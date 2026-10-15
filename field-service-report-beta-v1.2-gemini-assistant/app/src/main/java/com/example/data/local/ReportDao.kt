package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.FieldServiceReport
import kotlinx.coroutines.flow.Flow

@Dao
interface ReportDao {
    @Query("SELECT * FROM field_service_reports ORDER BY updatedAt DESC")
    fun getAllReports(): Flow<List<FieldServiceReport>>

    @Query("SELECT * FROM field_service_reports WHERE id = :id LIMIT 1")
    suspend fun getReportById(id: Long): FieldServiceReport?

    @Query("SELECT * FROM field_service_reports WHERE isDraft = 1 ORDER BY updatedAt DESC LIMIT 1")
    suspend fun getLatestDraft(): FieldServiceReport?

    @Query("SELECT * FROM field_service_reports WHERE isDraft = 1 ORDER BY updatedAt DESC")
    fun getDraftReports(): Flow<List<FieldServiceReport>>

    @Query("SELECT * FROM field_service_reports WHERE unitNo LIKE '%' || :query || '%' OR customerName LIKE '%' || :query || '%' OR serialNo LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    fun searchReports(query: String): Flow<List<FieldServiceReport>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: FieldServiceReport): Long

    @Update
    suspend fun updateReport(report: FieldServiceReport)

    @Delete
    suspend fun deleteReport(report: FieldServiceReport)

    @Query("DELETE FROM field_service_reports WHERE id = :id")
    suspend fun deleteReportById(id: Long)

    @Query("DELETE FROM field_service_reports WHERE unitNo = :unitNo")
    suspend fun deleteReportsByUnitNo(unitNo: String)

    @Query("DELETE FROM field_service_reports WHERE (unitNo = '' OR unitNo IS NULL OR TRIM(unitNo) = '' OR UPPER(unitNo) = 'UNIT UNTITLED') AND (jobDescription = '' OR jobDescription IS NULL OR TRIM(jobDescription) = '') AND (hourmeterPhotoUri IS NULL OR hourmeterPhotoUri = '') AND (defect1PhotoUri IS NULL OR defect1PhotoUri = '') AND (defect2PhotoUri IS NULL OR defect2PhotoUri = '')")
    suspend fun deleteEmptyDrafts()
}
