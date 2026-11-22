package com.example.udice.data.remote.supabase

import com.example.udice.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseHttpClient @Inject constructor(
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    suspend fun get(endpoint: String, queryParams: String = ""): Result<String> = withContext(ioDispatcher) {
        runCatching {
            val fullUrl = if (queryParams.isBlank()) {
                "${SupabaseConfig.BASE_URL}/$endpoint"
            } else {
                "${SupabaseConfig.BASE_URL}/$endpoint?$queryParams"
            }
            val connection = (URL(fullUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10000
                readTimeout = 10000
                setRequestProperty("apikey", SupabaseConfig.PUBLISHABLE_KEY)
                setRequestProperty("Authorization", "Bearer ${SupabaseConfig.PUBLISHABLE_KEY}")
                setRequestProperty("Accept", "application/json")
            }

            val responseCode = connection.responseCode
            val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
            val response = BufferedReader(InputStreamReader(stream ?: connection.inputStream)).use { it.readText() }
            connection.disconnect()

            if (responseCode in 200..299) {
                response
            } else {
                throw Exception("Supabase GET error $responseCode: $response")
            }
        }
    }

    suspend fun post(endpoint: String, jsonBody: String, preferReturn: Boolean = true): Result<String> = withContext(ioDispatcher) {
        runCatching {
            val connection = (URL("${SupabaseConfig.BASE_URL}/$endpoint").openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 10000
                readTimeout = 10000
                doOutput = true
                setRequestProperty("apikey", SupabaseConfig.PUBLISHABLE_KEY)
                setRequestProperty("Authorization", "Bearer ${SupabaseConfig.PUBLISHABLE_KEY}")
                setRequestProperty("Content-Type", "application/json")
                if (preferReturn) {
                    setRequestProperty("Prefer", "return=representation")
                }
            }

            OutputStreamWriter(connection.outputStream).use { it.write(jsonBody) }

            val responseCode = connection.responseCode
            val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
            val response = BufferedReader(InputStreamReader(stream ?: connection.inputStream)).use { it.readText() }
            connection.disconnect()

            if (responseCode in 200..299) {
                response
            } else {
                throw Exception("Supabase POST error $responseCode: $response")
            }
        }
    }

    suspend fun patch(endpoint: String, queryParams: String, jsonBody: String): Result<String> = withContext(ioDispatcher) {
        runCatching {
            val fullUrl = "${SupabaseConfig.BASE_URL}/$endpoint?$queryParams"
            val connection = (URL(fullUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "PATCH"
                connectTimeout = 10000
                readTimeout = 10000
                doOutput = true
                setRequestProperty("apikey", SupabaseConfig.PUBLISHABLE_KEY)
                setRequestProperty("Authorization", "Bearer ${SupabaseConfig.PUBLISHABLE_KEY}")
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Prefer", "return=representation")
            }

            OutputStreamWriter(connection.outputStream).use { it.write(jsonBody) }

            val responseCode = connection.responseCode
            val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
            val response = BufferedReader(InputStreamReader(stream ?: connection.inputStream)).use { it.readText() }
            connection.disconnect()

            if (responseCode in 200..299) {
                response
            } else {
                throw Exception("Supabase PATCH error $responseCode: $response")
            }
        }
    }

    suspend fun delete(endpoint: String, queryParams: String): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            val fullUrl = "${SupabaseConfig.BASE_URL}/$endpoint?$queryParams"
            val connection = (URL(fullUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "DELETE"
                connectTimeout = 10000
                readTimeout = 10000
                setRequestProperty("apikey", SupabaseConfig.PUBLISHABLE_KEY)
                setRequestProperty("Authorization", "Bearer ${SupabaseConfig.PUBLISHABLE_KEY}")
            }

            val responseCode = connection.responseCode
            connection.disconnect()

            if (responseCode !in 200..299) {
                throw Exception("Supabase DELETE error $responseCode")
            }
        }
    }
}
