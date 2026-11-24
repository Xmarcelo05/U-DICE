package com.example.udice.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.example.udice.data.remote.supabase.CloudHttpClient
import com.example.udice.data.remote.supabase.SupabaseConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID
import javax.inject.Inject
import org.json.JSONArray
import org.json.JSONObject

data class UploadedPublicationVideo(val videoUrl: String, val thumbnailUrl: String)

class PublicationImageStorage @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val http: CloudHttpClient
) {
    private val publicRoot = SupabaseConfig.BASE_URL.removeSuffix("/rest/v1") + "/storage/v1/object/public"
    private val localDirectory get() = File(context.filesDir, "publication-images")

    suspend fun save(uri: Uri, userId: String, session: SessionRepository): String {
        val mimeType = context.contentResolver.getType(uri)
        val extension = when (mimeType) {
            "image/jpeg" -> "jpg"
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> throw IllegalArgumentException("Selecciona una imagen JPG, PNG o WebP.")
        }
        val bytes = read(uri, 5 * 1024 * 1024, "La imagen debe tener un tamaño de hasta 5 MB.")
        return store(bytes, checkNotNull(mimeType), extension, IMAGE_BUCKET, userId, session)
    }

    suspend fun saveVideo(
        uri: Uri,
        userId: String,
        session: SessionRepository,
        onProgress: (Int) -> Unit
    ): UploadedPublicationVideo {
        require(context.contentResolver.getType(uri) == "video/mp4") { "Selecciona un video MP4." }
        val bytes = read(uri, 20 * 1024 * 1024, "El video debe tener un tamaño de hasta 20 MB.")
        require(bytes.size >= 12 && bytes.copyOfRange(4, 8).decodeToString() == "ftyp") {
            "El archivo no es un video MP4 válido."
        }
        val thumbnail = MediaMetadataRetriever().use { retriever ->
            retriever.setDataSource(context, uri)
            require(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO) == "yes") {
                "El MP4 debe contener una pista de video."
            }
            val frame = retriever.getScaledFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC, 960, 540)
                ?: error("No se pudo obtener la miniatura del video.")
            ByteArrayOutputStream().use { output ->
                try {
                    check(frame.compress(Bitmap.CompressFormat.JPEG, 85, output)) {
                        "No se pudo generar la miniatura del video."
                    }
                    output.toByteArray()
                } finally {
                    frame.recycle()
                }
            }
        }
        val thumbnailUrl = store(thumbnail, "image/jpeg", "jpg", IMAGE_BUCKET, userId, session)
        val video = dataResult { store(bytes, "video/mp4", "mp4", VIDEO_BUCKET, userId, session, onProgress) }
            .getOrElse { error ->
                dataResult { remove(thumbnailUrl, userId, session) }
                throw error
            }
        return UploadedPublicationVideo(video, thumbnailUrl)
    }

    private fun read(uri: Uri, limit: Int, sizeError: String): ByteArray {
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readNBytes(limit + 1) }
            ?: error("No se pudo abrir el archivo seleccionado.")
        require(bytes.isNotEmpty() && bytes.size <= limit) { sizeError }
        return bytes
    }

    private suspend fun store(
        bytes: ByteArray,
        mimeType: String,
        extension: String,
        bucket: String,
        userId: String,
        session: SessionRepository,
        onProgress: ((Int) -> Unit)? = null
    ): String {
        val filename = "${UUID.randomUUID()}.$extension"
        if (session is CloudSessionRepository) {
            val path = "$userId/$filename"
            session.signedRequest { token ->
                http.request(
                    "storage/v1/object/$bucket/$path", "POST", token,
                    image = bytes, imageType = mimeType, onUploadProgress = onProgress
                )
            }
            return "$publicRoot/$bucket/$path"
        }
        localDirectory.mkdirs()
        val file = File(localDirectory, filename)
        file.writeBytes(bytes)
        return Uri.fromFile(file).toString()
    }

    suspend fun remove(url: String, userId: String, session: SessionRepository) {
        if (session is CloudSessionRepository) {
            val bucket = listOf(IMAGE_BUCKET, VIDEO_BUCKET).firstOrNull {
                url.startsWith("$publicRoot/$it/$userId/")
            } ?: return
            val filename = url.removePrefix("$publicRoot/$bucket/$userId/")
            if (!FILE_NAME.matches(filename)) return
            session.signedRequest { token ->
                http.request(
                    "storage/v1/object/$bucket", "DELETE", token,
                    JSONObject().put("prefixes", JSONArray().put("$userId/$filename"))
                )
            }
        } else {
            val path = Uri.parse(url).path ?: return
            val file = File(path)
            if (file.parentFile == localDirectory && FILE_NAME.matches(file.name)) file.delete()
        }
    }

    private companion object {
        const val IMAGE_BUCKET = "publicaciones-imagenes"
        const val VIDEO_BUCKET = "publicaciones-videos"
        val FILE_NAME = Regex("[0-9a-f-]{36}\\.(jpg|png|webp|mp4)")
    }
}
