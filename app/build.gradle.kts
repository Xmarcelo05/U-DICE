import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
}

val useSupabase = providers.gradleProperty("udiceSupabase").map { it.toBooleanStrict() }.orElse(false)

val localProperties = Properties().apply {
    providers.fileContents(rootProject.layout.projectDirectory.file("local.properties")).asText.orNull
        ?.reader()?.use { load(it) }
}
val supabaseUrl = localProperties.getProperty("UDICE_SUPABASE_URL", "").trim().trimEnd('/').removeSuffix("/rest/v1")
val supabaseKey = localProperties.getProperty("UDICE_SUPABASE_PUBLISHABLE_KEY", "").trim()

if (useSupabase.get()) {
    require(supabaseUrl.matches(Regex("https://[A-Za-z0-9.-]+")) &&
        supabaseKey.matches(Regex("sb_publishable_[A-Za-z0-9_-]+"))
    ) {
        "Configura UDICE_SUPABASE_URL y UDICE_SUPABASE_PUBLISHABLE_KEY en local.properties. Consulta el README."
    }
}

android {
    namespace = "com.example.udice"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.udice"
        minSdk = 35
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
        buildConfigField("boolean", "UDICE_SUPABASE", useSupabase.get().toString())

        buildConfigField("String", "UDICE_SUPABASE_URL", "\"$supabaseUrl\"")
        buildConfigField("String", "UDICE_SUPABASE_PUBLISHABLE_KEY", "\"$supabaseKey\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Room
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.ui)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
