package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.screens.GroceryHomeScreen
import com.example.ui.theme.GharKiListTheme
import com.example.ui.viewmodel.GroceryViewModel
import com.example.ui.viewmodel.GroceryViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: GroceryViewModel by viewModels {
        val app = application as GharKiListApp
        GroceryViewModelFactory(app.repository, app.voiceSpeechManager)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GharKiListTheme {
                GroceryHomeScreen(viewModel = viewModel)
            }
        }
    }
}
