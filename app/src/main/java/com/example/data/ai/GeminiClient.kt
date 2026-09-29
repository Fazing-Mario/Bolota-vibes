package com.example.data.ai

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    fun isApiKeyConfigured(): Boolean {
        val key = BuildConfig.GEMINI_API_KEY
        return key.isNotBlank() && key != "MY_GEMINI_API_KEY"
    }

    suspend fun generateBolotaResponse(
        systemPrompt: String,
        conversationHistory: List<Pair<String, String>>, // role ("user" | "model"), text
        userMessage: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (!isApiKeyConfigured()) {
            return@withContext Result.failure(IllegalStateException("Chave do Gemini não configurada"))
        }

        try {
            val root = JSONObject()

            // System instruction
            val sysContent = JSONObject()
            val sysParts = JSONArray()
            sysParts.put(JSONObject().put("text", systemPrompt))
            sysContent.put("parts", sysParts)
            root.put("systemInstruction", sysContent)

            // Contents array
            val contents = JSONArray()
            // Add previous history (last 10 turns to avoid exceeding context)
            val recentTurns = conversationHistory.takeLast(10)
            for ((role, text) in recentTurns) {
                val turnObj = JSONObject()
                turnObj.put("role", if (role == "user") "user" else "model")
                val parts = JSONArray()
                parts.put(JSONObject().put("text", text))
                turnObj.put("parts", parts)
                contents.put(turnObj)
            }

            // Current message
            val currentTurn = JSONObject()
            currentTurn.put("role", "user")
            val currentParts = JSONArray()
            currentParts.put(JSONObject().put("text", userMessage))
            currentTurn.put("parts", currentParts)
            contents.put(currentTurn)

            root.put("contents", contents)

            // Generation config
            val genConfig = JSONObject()
            genConfig.put("temperature", 0.7)
            genConfig.put("topP", 0.95)
            root.put("generationConfig", genConfig)

            val requestBody = root.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    Exception("Erro na API Gemini (${response.code}): $responseBody")
                )
            }

            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val firstCandidate = candidates.getJSONObject(0)
                val content = firstCandidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    val reply = parts.getJSONObject(0).optString("text", "")
                    if (reply.isNotBlank()) {
                        return@withContext Result.success(reply.trim())
                    }
                }
            }

            Result.failure(Exception("Resposta vazia da IA"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generateWeeklySummary(
        systemPrompt: String,
        weeklyData: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (!isApiKeyConfigured()) {
            return@withContext Result.failure(IllegalStateException("Chave da API ausente"))
        }

        try {
            val root = JSONObject()
            val sysContent = JSONObject()
            val sysParts = JSONArray().put(JSONObject().put("text", systemPrompt))
            sysContent.put("parts", sysParts)
            root.put("systemInstruction", sysContent)

            val contents = JSONArray()
            val turn = JSONObject().put("role", "user")
            val turnParts = JSONArray().put(
                JSONObject().put(
                    "text",
                    "Analise minha semana com base nestes dados:\n$weeklyData\n\nEscreva um resumo acolhedor porém direto com: o que foi bem, o padrão de atenção (seja sincero) e uma meta pequena para a próxima semana. Máximo 4 frases curtas."
                )
            )
            turn.put("parts", turnParts)
            contents.put(turn)
            root.put("contents", contents)

            val genConfig = JSONObject().put("temperature", 0.6)
            root.put("generationConfig", genConfig)

            val requestBody = root.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Erro HTTP ${response.code}"))
            }

            val jsonResponse = JSONObject(responseBody)
            val text = jsonResponse.optJSONArray("candidates")
                ?.optJSONObject(0)
                ?.optJSONObject("content")
                ?.optJSONArray("parts")
                ?.optJSONObject(0)
                ?.optString("text")

            if (!text.isNullOrBlank()) {
                Result.success(text.trim())
            } else {
                Result.failure(Exception("Sem resposta"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
