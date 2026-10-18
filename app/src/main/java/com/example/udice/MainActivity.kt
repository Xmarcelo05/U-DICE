package com.example.udice

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.udice.navigation.AppNavigation
import com.example.udice.ui.theme.LightBlueGray
import com.example.udice.ui.theme.UDICETheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UDICETheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = LightBlueGray
                ) {
                    AppNavigation()
                }
            }
        }
    }
}