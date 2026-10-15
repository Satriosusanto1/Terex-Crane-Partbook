package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.ReportEditorScreen
import com.example.ui.screens.ReportListScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ReportViewModel

class MainActivity : ComponentActivity() {

    private val reportViewModel: ReportViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        android.widget.Toast.makeText(this, "Created by Satrio Susanto", android.widget.Toast.LENGTH_LONG).show()
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(viewModel = reportViewModel)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(viewModel: ReportViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "report_list"
    ) {
        composable("report_list") {
            ReportListScreen(
                viewModel = viewModel,
                onNavigateToCreate = {
                    navController.navigate("report_editor")
                },
                onNavigateToEdit = { id ->
                    viewModel.loadReport(id)
                    navController.navigate("report_editor")
                }
            )
        }

        composable("report_editor") {
            ReportEditorScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
