package com.example.udice.data.remote.supabase

import com.example.udice.BuildConfig

object SupabaseConfig {
    const val BASE_URL = BuildConfig.UDICE_SUPABASE_URL + "/rest/v1"
    const val PUBLISHABLE_KEY = BuildConfig.UDICE_SUPABASE_PUBLISHABLE_KEY
    const val AVATAR_BUCKET = "perfiles-imagenes"

    fun avatarUrl(path: String): String = "${BASE_URL.removeSuffix("/rest/v1")}/storage/v1/object/public/$AVATAR_BUCKET/$path"
}
