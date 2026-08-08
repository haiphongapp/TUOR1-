package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.PackingViewModel
import androidx.compose.material.icons.filled.Home
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.RecordScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.PackingRecorderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PackingRecorderTheme {
                PackingAppMainScreen()
            }
        }
    }
}

@Composable
fun PackingAppMainScreen() {
    val viewModel: PackingViewModel = viewModel()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val userMessage by viewModel.userMessage.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val lang = settings.language
    var selectedTab by remember { mutableIntStateOf(0) }

    // Toast / Snackbar feedback
    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(tonalElevation = 8.dp) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(imageVector = Icons.Default.Home, contentDescription = "Home")
                    },
                    label = {
                        Text(
                            text = com.example.util.LanguageUtils.getTranslation("nav_home", lang),
                            fontSize = 11.sp,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(imageVector = Icons.Default.Videocam, contentDescription = "Record")
                    },
                    label = {
                        Text(
                            text = com.example.util.LanguageUtils.getTranslation("nav_record", lang),
                            fontSize = 11.sp,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(imageVector = Icons.Default.History, contentDescription = "History")
                    },
                    label = {
                        Text(
                            text = com.example.util.LanguageUtils.getTranslation("nav_history", lang),
                            fontSize = 11.sp,
                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )

                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
                    },
                    label = {
                        Text(
                            text = com.example.util.LanguageUtils.getTranslation("nav_settings", lang),
                            fontSize = 11.sp,
                            fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }
    ) { innerPadding ->
        BoxModifier(
            selectedTab = selectedTab,
            viewModel = viewModel,
            onNavigateToTab = { tabIndex -> selectedTab = tabIndex },
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun BoxModifier(
    selectedTab: Int,
    viewModel: PackingViewModel,
    onNavigateToTab: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    when (selectedTab) {
        0 -> HomeScreen(
            viewModel = viewModel,
            onNavigateToRecord = { onNavigateToTab(1) },
            onNavigateToHistory = { onNavigateToTab(2) },
            onNavigateToSettings = { onNavigateToTab(3) },
            modifier = modifier
        )
        1 -> RecordScreen(viewModel = viewModel, modifier = modifier)
        2 -> HistoryScreen(
            viewModel = viewModel,
            onNavigateToRecordTab = { onNavigateToTab(1) },
            modifier = modifier
        )
        3 -> SettingsScreen(viewModel = viewModel, modifier = modifier)
    }
}
