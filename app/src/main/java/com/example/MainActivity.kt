package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.JobVisitViewer
import com.example.ui.components.TopNavBar
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppViewModel
import com.example.ui.viewmodel.Screen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val appViewModel: AppViewModel = viewModel()
                MainAppContent(viewModel = appViewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: AppViewModel) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val currentScreen by viewModel.currentScreen.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    // Anti-Cheat App Lifecycle Detection:
    // If user navigates away or minimizes the app while timer is running, invalidate or pause!
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> {
                    viewModel.onAppPausedDuringVisit()
                }
                Lifecycle.Event.ON_RESUME -> {
                    viewModel.onAppResumedDuringVisit()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Permission request launcher for Notification and Background process permissions
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    LaunchedEffect(Unit) {
        val permissionsToRequest = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    // Back button handling: If in sub-screens, navigate back to DASHBOARD
    BackHandler(enabled = currentScreen != Screen.DASHBOARD && currentScreen != Screen.AUTH && currentScreen != Screen.VISIT_JOB) {
        viewModel.navigateTo(Screen.DASHBOARD)
    }

    val showTopNav = currentScreen != Screen.AUTH && currentScreen != Screen.VISIT_JOB

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (showTopNav) {
                TopNavBar(viewModel = viewModel)
            }
        }
    ) { innerPadding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)

        when (currentScreen) {
            Screen.AUTH -> AuthScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
            Screen.DASHBOARD -> DashboardScreen(viewModel = viewModel, modifier = contentModifier)
            Screen.REMOTE_JOB -> RemoteJobScreen(viewModel = viewModel, modifier = contentModifier)
            Screen.DEPOSIT -> DepositScreen(viewModel = viewModel, modifier = contentModifier)
            Screen.WITHDRAWAL -> WithdrawalScreen(viewModel = viewModel, modifier = contentModifier)
            Screen.PROFILE -> ProfileScreen(viewModel = viewModel, modifier = contentModifier)
            Screen.ABOUT -> AboutScreen(viewModel = viewModel, modifier = contentModifier)
            Screen.REFERRAL -> ReferralScreen(viewModel = viewModel, modifier = contentModifier)
            Screen.ADMIN -> AdminScreen(viewModel = viewModel, modifier = contentModifier)
            Screen.VISIT_JOB -> JobVisitViewer(viewModel = viewModel, modifier = Modifier.fillMaxSize())
        }
    }
}
