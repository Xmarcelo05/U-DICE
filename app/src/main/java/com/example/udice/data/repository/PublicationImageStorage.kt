package com.example.udice.data.repository

import android.content.Context
import android.net.Uri
import com.example.udice.data.remote.supabase.CloudHttpClient
import com.example.udice.data.remote.supabase.SupabaseConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import org.json.JSONArray
import org.json.JSONObject

class PublicationImageStorage @Inject constructor(
    @ApplicationContext private val context: Context,
    private val http: CloudHttpClient
) {
    private val root = SupabaseConfig.BASE_URL.removeSuffix("/rest/v1")
    private val publicPrefix = "$root/storage/v1/object/public/publicaciones-imagenes/"
    private val localDirectory get() = File(context.filesDir, "publication-images")

    suspend fun save(uri: Uri, userId: String, session: SessionRepository): String {
        val resolver = context.contentResolver
        val mimeType = resolver.getType(uri)
        val extension = when (mimeType) {
            "image/jpeg" -> "jpg"
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> throw IllegalArgumentException("Selecciona una imagen JPG, PNG o WebP.")
        }
        val bytes = resolver.openInputStream(uri)?.use { it.readNBytes(MAX_BYTES + 1) }
            ?: error("No se pudo abrir la imagen seleccionada.")
        require(bytes.isNotEmpty() && bytes.size <= MAX_BYTES) { "La imagen debe tener un tamaño de hasta 5 MB." }
        val filename = "${UUID.randomUUID()}.$extension"

        if (session is CloudSessionRepository) {
            val path = "$userId/$filename"
            session.signedRequest { token ->
                http.request(
                    "storage/v1/object/publicaciones-imagenes/$path", "POST", token,
                    image = bytes, imageType = mimeType
                )
            }
            return "$publicPrefix$path"
        }

        localDirectory.mkdirs()
        val file = File(localDirectory, filename)
        file.writeBytes(bytes)
        return Uri.fromFile(file).toString()
    }

    suspend fun remove(imageUrl: String, userId: String, session: SessionRepository) {
        if (session is CloudSessionRepository) {
            val ownerPrefix = "$publicPrefix$userId/"
            if (!imageUrl.startsWith(ownerPrefix)) return
            val filename = imageUrl.removePrefix(ownerPrefix)
            if (!FILE_NAME.matches(filename)) return
            session.signedRequest { token ->
                http.request(
                    "storage/v1/object/publicaciones-imagenes", "DELETE", token,
                    JSONObject().put("prefixes", JSONArray().put("$userId/$filename"))
                )
            }
        } else {
            val path = Uri.parse(imageUrl).path ?: return
            val file = File(path)
            if (file.parentFile == localDirectory && FILE_NAME.matches(file.name)) file.delete()
        }
    }

    private companion object {
        const val MAX_BYTES = 5 * 1024 * 1024
        val FILE_NAME = Regex("[0-9a-f-]{36}\\.(jpg|png|webp)")
    }
}
