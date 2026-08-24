package com.example.scenabl

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.scenabl.ui.navigation.ScenaBLApp
import com.example.scenabl.ui.theme.ScenaBLTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val appContainer = (application as ScenaBLApplication).appContainer
        setContent {
            ScenaBLTheme {
                ScenaBLApp(appContainer)
            }
        }
    }
}
