package com.example.udice.data.remote.supabase

import com.example.udice.BuildConfig

object SupabaseConfig {
    const val BASE_URL = BuildConfig.UDICE_SUPABASE_URL + "/rest/v1"
    const val PUBLISHABLE_KEY = BuildConfig.UDICE_SUPABASE_PUBLISHABLE_KEY
}
