package com.example.udice.data.remote.supabase

import com.example.udice.di.IoDispatcher
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.json.JSONObject

internal class CloudRequestException(val status: Int, message: String) : IOException(message)

class CloudHttpClient @Inject constructor(
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun request(
        path: String,
        method: String = "GET",
        token: String? = null,
        body: JSONObject? = null,
        returnRow: Boolean = false,
        image: ByteArray? = null,
        imageType: String? = null
    ): String = withContext(ioDispatcher) {
        val root = SupabaseConfig.BASE_URL.removeSuffix("/rest/v1")
        val connection = (URL("$root/$path").openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 10_000
            readTimeout = 10_000
            instanceFollowRedirects = false
            setRequestProperty("apikey", SupabaseConfig.PUBLISHABLE_KEY)
            if (returnRow) setRequestProperty("Prefer", "return=representation")
            setRequestProperty("Accept", "application/json")
            if (token != null) setRequestProperty("Authorization", "Bearer $token")
        }
        try {
            if (image != null) {
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", checkNotNull(imageType))
                connection.setFixedLengthStreamingMode(image.size)
                connection.outputStream.use { it.write(image) }
            } else if (body != null) {
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json")
                connection.outputStream.bufferedWriter(Charsets.UTF_8).use { it.write(body.toString()) }
            }

            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val response = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (status !in 200..299) {
                val message = runCatching {
                    val error = JSONObject(response)
                    listOf("message", "msg", "error_description", "error")
                        .firstNotNullOfOrNull { error.optString(it).takeIf(String::isNotBlank) }
                }.getOrNull()
                throw CloudRequestException(status, message ?: "Supabase respondió con un error ($status).")
            }
            response
        } finally {
            connection.disconnect()
        }
    }
}
