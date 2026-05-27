package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.data.database.AppDatabase
import com.example.data.repository.AtsRepository
import com.example.ui.screens.MainApp
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AtsViewModel

class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Room DB, Dao, and Repository
        val database = AppDatabase.getDatabase(applicationContext)
        val dao = database.atsReportDao()
        val repository = AtsRepository(dao)
        
        // Instantiate our ViewModel
        val viewModel: AtsViewModel by viewModels {
            AtsViewModel.Factory(repository)
        }
        
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainApp(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
