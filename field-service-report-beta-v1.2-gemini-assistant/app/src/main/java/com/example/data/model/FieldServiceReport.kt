package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun getCurrentInspectionDate(): String {
    return try {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    } catch (_: Exception) {
        ""
    }
}

@Entity(tableName = "field_service_reports")
data class FieldServiceReport(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // Header & Company
    val companyHeader: String = "LIEBHERR",
    val reportTitle: String = "FIELD SERVICE REPORT",
    
    // Customer & Unit Information (Page 1 Table)
    val customerName: String = "PT. Amman Mineral",
    val projectSite: String = "Batu Hijau",
    val contactPerson: String = "",
    val unitNo: String = "",
    val unitBrand: String = "LIEBHERR",
    val type: String = "Mobile and Crawler Crane",
    val serialNo: String = "",
    val superstructureHourmeter: String = "",
    val chassisHourmeter: String = "",
    val odometer: String = "",
    val jobDescription: String = "",
    val reportDate: String = getCurrentInspectionDate(),
    
    // Detail Pictures & Findings (Page 1 Table)
    val history: String = "",
    val hourmeterPhotoUri: String? = null,
    val hourmeterFindings: String = "",
    val defect1PhotoUri: String? = null,
    val defect1Findings: String = "",
    val defect2PhotoUri: String? = null,
    val defect2Findings: String = "",
    
    // Page 2: Part Required (JSON array string of PartItem)
    val partsRequiredJson: String = "[]",
    
    // Page 2: Completed Action
    val completedAction: String = "",
    
    // Page 2: Conclusions
    val conclusion1: String = "",
    val conclusion2: String = "",
    val conclusion3: String = "",
    
    // Page 2: Signatures (Signatures stored as local file URIs)
    val preparedBySignatureUri: String? = null,
    val preparedByName: String = "",
    val preparedByPosition: String = "",
    
    val acknowledgeBySignatureUri: String? = null,
    val acknowledgeByName: String = "",
    val acknowledgeByPosition: String = "",
    
    val clientAcknowledgeTitle: String = "Acknowledge By PT. AMNT",
    val clientAcknowledgeSignatureUri: String? = null,
    val clientAcknowledgeName: String = "",
    val clientAcknowledgePosition: String = "",
    
    // Recipient email for quick sharing
    val recipientEmail: String = "",
    
    // Metadata
    val isDraft: Boolean = false,
    val isSynced: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class PartItem(
    val partNumber: String = "",
    val partDescription: String = "",
    val qty: String = ""
)

fun parsePartsJson(json: String): List<PartItem> {
    val list = mutableListOf<PartItem>()
    try {
        val array = JSONArray(json)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                PartItem(
                    partNumber = obj.optString("partNumber", ""),
                    partDescription = obj.optString("partDescription", ""),
                    qty = obj.optString("qty", "")
                )
            )
        }
    } catch (_: Exception) {
    }
    return list
}

fun List<PartItem>.toJsonString(): String {
    val array = JSONArray()
    for (item in this) {
        val obj = JSONObject()
        obj.put("partNumber", item.partNumber)
        obj.put("partDescription", item.partDescription)
        obj.put("qty", item.qty)
        array.put(obj)
    }
    return array.toString()
}
