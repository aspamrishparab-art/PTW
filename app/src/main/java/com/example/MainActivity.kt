package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.screens.AdminUserScreen
import com.example.ui.screens.CreatePermitScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.NotificationScreen
import com.example.ui.screens.PermitDashboardScreen
import com.example.ui.screens.PermitDetailScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.PermitViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: PermitViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PermitAppNavigation(viewModel)
                }
            }
        }
    }
}

@Composable
fun PermitAppNavigation(viewModel: PermitViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val selectedPermitId by viewModel.selectedPermitId.collectAsState()

    // Handle back navigation for secondary screens
    if (currentScreen != "DASHBOARD") {
        BackHandler {
            viewModel.navigateTo("DASHBOARD")
        }
    }

    when (currentScreen) {
        "DASHBOARD" -> {
            PermitDashboardScreen(
                viewModel = viewModel,
                onNavigateToCreate = { viewModel.navigateTo("CREATE_PERMIT") },
                onNavigateToDetail = { id -> viewModel.navigateTo("PERMIT_DETAIL", id) },
                onNavigateToLogin = { viewModel.navigateTo("LOGIN") },
                onNavigateToAdmin = { viewModel.navigateTo("ADMIN_USERS") },
                onNavigateToNotifications = { viewModel.navigateTo("NOTIFICATIONS") }
            )
        }
        "CREATE_PERMIT" -> {
            CreatePermitScreen(
                viewModel = viewModel,
                onBack = { viewModel.navigateTo("DASHBOARD") }
            )
        }
        "PERMIT_DETAIL" -> {
            PermitDetailScreen(
                permitId = selectedPermitId ?: 1L,
                viewModel = viewModel,
                onBack = { viewModel.navigateTo("DASHBOARD") }
            )
        }
        "LOGIN" -> {
            LoginScreen(
                viewModel = viewModel,
                onBack = { viewModel.navigateTo("DASHBOARD") }
            )
        }
        "ADMIN_USERS" -> {
            AdminUserScreen(
                viewModel = viewModel,
                onBack = { viewModel.navigateTo("DASHBOARD") }
            )
        }
        "NOTIFICATIONS" -> {
            NotificationScreen(
                viewModel = viewModel,
                onBack = { viewModel.navigateTo("DASHBOARD") },
                onNavigateToDetail = { id -> viewModel.navigateTo("PERMIT_DETAIL", id) }
            )
        }
    }
}
