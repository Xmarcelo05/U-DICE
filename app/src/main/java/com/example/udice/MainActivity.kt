package com.example.udice

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.udice.ui.auth.AuthStateHolder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val auth: AuthStateHolder by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) handleConfirmation(intent)
        enableEdgeToEdge()
        setContent {
            UDiceApp(auth)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleConfirmation(intent)
    }

    private fun handleConfirmation(intent: Intent) {
        val uri = intent.data ?: return
        if (intent.action != Intent.ACTION_VIEW || uri.scheme != "com.example.udice" ||
            uri.host != "confirm-email" || !uri.path.isNullOrEmpty()
        ) return

        val failed = uri.getQueryParameter("error") != null ||
            uri.encodedFragment.orEmpty().split('&').any { it.startsWith("error=") }
        auth.onEmailConfirmationLink(failed)
    }
}
