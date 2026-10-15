package com.example.util

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.net.URLEncoder
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * Online and Offline Hybrid Translation Engine for Field Service Reports.
 *
 * Automatically translates Indonesian mechanical inspection reports to professional English.
 * Prioritizes high-accuracy online translation when network is available, and seamlessly
 * falls back to the specialized on-device [TechnicalTranslator] dictionary when offline in remote sites.
 */
object OnlineTranslationManager {

    private const val TAG = "OnlineTranslationManager"
    private val translationCache = ConcurrentHashMap<String, String>()

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(4, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(4, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false)
            .build()
    }

    /**
     * Translates the given Indonesian text to Technical English using Online Google Engine
     * with instant fallback to on-device [TechnicalTranslator].
     */
    suspend fun translateToEnglish(text: String): String = withContext(Dispatchers.IO) {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return@withContext ""

        // Check in-memory cache first
        translationCache[trimmed]?.let { return@withContext it }

        // Attempt online translation
        val onlineResult = tryOnlineTranslation(trimmed)
        val finalResult = if (!onlineResult.isNullOrBlank()) {
            // Post-process with technical heavy equipment phrasing and acronyms
            TechnicalTranslator.enhanceOnlineTranslation(onlineResult)
        } else {
            // Offline fallback
            TechnicalTranslator.translateToEnglish(trimmed)
        }

        translationCache[trimmed] = finalResult
        finalResult
    }

    /**
     * Synchronous translation method (uses cache or offline technical engine).
     */
    fun translateToEnglishSync(text: String): String {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return ""
        translationCache[trimmed]?.let { return it }
        val offlineResult = TechnicalTranslator.translateToEnglish(trimmed)
        translationCache[trimmed] = offlineResult
        return offlineResult
    }

    private fun tryOnlineTranslation(sourceText: String): String? {
        return try {
            val encodedQuery = URLEncoder.encode(sourceText, "UTF-8")
            val url = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=id&tl=en&dt=t&q=$encodedQuery"

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Android; Mobile; rv:100.0)")
                .get()
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                response.close()
                return null
            }

            val bodyString = response.body?.string() ?: return null
            response.close()

            parseGoogleTranslateResponse(bodyString)
        } catch (e: Exception) {
            Log.d(TAG, "Online translation unavailable, switching to local technical engine: ${e.message}")
            null
        }
    }

    private fun parseGoogleTranslateResponse(jsonString: String): String? {
        return try {
            val jsonArray = JSONArray(jsonString)
            val sentencesArray = jsonArray.optJSONArray(0) ?: return null
            val resultBuilder = StringBuilder()

            for (i in 0 until sentencesArray.length()) {
                val sentenceObj = sentencesArray.optJSONArray(i)
                if (sentenceObj != null && sentenceObj.length() > 0) {
                    val partText = sentenceObj.optString(0, "")
                    resultBuilder.append(partText)
                }
            }

            val result = resultBuilder.toString().trim()
            if (result.isNotBlank()) result else null
        } catch (e: Exception) {
            null
        }
    }
}
