package com.example.util

import com.example.BuildConfig
import com.example.data.model.FieldServiceReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Calls a trusted server-side AI endpoint. The Gemini credential must live on the server,
 * never in the Android APK. Configure FSR_AI_ENDPOINT at build/deployment time.
 *
 * Expected request JSON: { "action": "...", "text": "...", "context": { ... } }
 * Expected response JSON: { "result": "..." } (also accepts "text" or "output").
 */
object AiAssistantClient {
    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(35, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .callTimeout(45, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false)
            .build()
    }

    sealed class Result {
        data class Success(val text: String) : Result()
        data class Failure(val message: String) : Result()
    }

    suspend fun assist(
        action: String,
        report: FieldServiceReport,
        sourceText: String = ""
    ): Result = withContext(Dispatchers.IO) {
        val endpoint = BuildConfig.FSR_AI_ENDPOINT.trim()
        if (endpoint.isBlank()) {
            return@withContext Result.Failure(
                "Backend AI belum dikonfigurasi. Atur FSR_AI_ENDPOINT ke URL HTTPS server AI yang aman."
            )
        }
        if (!endpoint.startsWith("https://", ignoreCase = true)) {
            return@withContext Result.Failure("Endpoint AI harus menggunakan HTTPS.")
        }

        val context = JSONObject().apply {
            put("unitNo", report.unitNo)
            put("unitBrand", report.unitBrand)
            put("model", report.type)
            put("serialNo", report.serialNo)
            put("reportDate", report.reportDate)
            put("jobDescription", report.jobDescription)
            put("history", report.history)
            put("hourmeterFindings", report.hourmeterFindings)
            put("defect1Findings", report.defect1Findings)
            put("defect2Findings", report.defect2Findings)
            put("completedAction", report.completedAction)
            put("conclusion1", report.conclusion1)
            put("conclusion2", report.conclusion2)
            put("conclusion3", report.conclusion3)
            // Deliberately omit photos, signatures, names, email addresses, and part list by default.
        }
        val payload = JSONObject()
            .put("action", action)
            .put("text", sourceText.take(MAX_INPUT_CHARS))
            .put("context", context)
            .put("language", "id")
            .put("safety", "Do not invent facts, part numbers, OEM limits, measurements, or procedures. Clearly flag uncertainty. Keep unit IDs, serial numbers, fault codes, measurements and units unchanged. Return draft text for human review; do not approve the report.")
        try {
            val request = Request.Builder()
                .url(endpoint)
                .header("Accept", "application/json")
                .post(payload.toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()
            httpClient.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    return@withContext Result.Failure(
                        "Server AI mengembalikan HTTP ${response.code}. Periksa backend lalu coba lagi."
                    )
                }
                val json = JSONObject(body)
                val output = sequenceOf("result", "text", "output")
                    .mapNotNull { key -> json.optString(key).takeIf { it.isNotBlank() && it != "null" } }
                    .firstOrNull()
                    .orEmpty()
                    .take(MAX_OUTPUT_CHARS)
                if (output.isBlank()) {
                    Result.Failure("Respons AI kosong atau format respons tidak sesuai.")
                } else {
                    Result.Success(output)
                }
            }
        } catch (_: java.net.SocketTimeoutException) {
            Result.Failure("Permintaan AI melewati batas waktu. Periksa koneksi dan coba lagi.")
        } catch (_: Exception) {
            Result.Failure("Tidak dapat menghubungi backend AI. Periksa koneksi dan konfigurasi server.")
        }
    }

    private const val MAX_INPUT_CHARS = 6000
    private const val MAX_OUTPUT_CHARS = 12000
    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
}
