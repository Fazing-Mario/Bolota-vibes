package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notification.BolotaNotificationHelper
import com.example.ui.screens.EstatisticasScreen
import com.example.ui.screens.HojeScreen
import com.example.ui.screens.PetChatScreen
import com.example.ui.screens.SaudeScreen
import com.example.ui.theme.BolotaAccentDark
import com.example.ui.theme.BolotaTheme
import com.example.ui.viewmodel.BolotaViewModel

enum class BolotaTab {
    HOJE,
    ESTATISTICAS,
    SAUDE,
    PET
}

class MainActivity : ComponentActivity() {
    private val viewModel: BolotaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create notification channel for daily habit reminders
        BolotaNotificationHelper.createNotificationChannel(this)

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val context = LocalContext.current

            // Notification permission launcher for Android 13+ (API 33+)
            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) {
                    viewModel.updateReminderPreferences(
                        enabled = true,
                        hour = uiState.reminderHour,
                        minute = uiState.reminderMinute
                    )
                }
            }

            // Check and request notification permission if reminders are enabled
            LaunchedEffect(uiState.remindersEnabled) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    uiState.remindersEnabled &&
                    !BolotaNotificationHelper.canSendNotifications(context)
                ) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            // Show Toast when requested
            LaunchedEffect(uiState.toastMessage) {
                uiState.toastMessage?.let { msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    viewModel.clearToast()
                }
            }

            val isSystemDark = isSystemInDarkTheme()
            val isDark = when (uiState.themeMode) {
                "claro" -> false
                "escuro" -> true
                else -> isSystemDark
            }

            BolotaTheme(darkTheme = isDark) {
                var currentTab by remember { mutableStateOf(BolotaTab.HOJE) }

                BackHandler(enabled = currentTab != BolotaTab.HOJE) {
                    currentTab = BolotaTab.HOJE
                }

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("main_scaffold"),
                    topBar = {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = uiState.petName,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Sync indicator pill
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(BolotaAccentDark)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "salvo no aparelho",
                                        fontSize = 11.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // Theme toggle button
                                IconButton(
                                    onClick = {
                                        val newTheme = if (isDark) "claro" else "escuro"
                                        viewModel.updateSettings(
                                            petName = uiState.petName,
                                            theme = newTheme,
                                            screenGoal = uiState.screenGoal,
                                            sleepGoal = uiState.sleepGoal
                                        )
                                    },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.surface)
                                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                                ) {
                                    Icon(
                                        imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                        contentDescription = "Trocar tema",
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    },
                    bottomBar = {
                        NavigationBar(
                            modifier = Modifier
                                .testTag("bottom_nav_bar")
                                .navigationBarsPadding(),
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 6.dp
                        ) {
                            NavigationBarItem(
                                selected = currentTab == BolotaTab.HOJE,
                                onClick = { currentTab = BolotaTab.HOJE },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Hoje"
                                    )
                                },
                                label = { Text("Hoje", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold) },
                                modifier = Modifier.testTag("nav_tab_hoje")
                            )

                            NavigationBarItem(
                                selected = currentTab == BolotaTab.ESTATISTICAS,
                                onClick = { currentTab = BolotaTab.ESTATISTICAS },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.TrendingUp,
                                        contentDescription = "Tendências"
                                    )
                                },
                                label = { Text("Tendências", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold) },
                                modifier = Modifier.testTag("nav_tab_estatisticas")
                            )

                            NavigationBarItem(
                                selected = currentTab == BolotaTab.SAUDE,
                                onClick = { currentTab = BolotaTab.SAUDE },
                                icon = {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ShowChart,
                                        contentDescription = "Saúde"
                                    )
                                },
                                label = { Text("Saúde", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold) },
                                modifier = Modifier.testTag("nav_tab_saude")
                            )

                            NavigationBarItem(
                                selected = currentTab == BolotaTab.PET,
                                onClick = { currentTab = BolotaTab.PET },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Face,
                                        contentDescription = uiState.petName
                                    )
                                },
                                label = { Text(uiState.petName, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold) },
                                modifier = Modifier.testTag("nav_tab_pet")
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentTab) {
                            BolotaTab.HOJE -> HojeScreen(
                                uiState = uiState,
                                viewModel = viewModel,
                                onNavigateToChat = { currentTab = BolotaTab.PET }
                            )
                            BolotaTab.ESTATISTICAS -> EstatisticasScreen(
                                uiState = uiState
                            )
                            BolotaTab.SAUDE -> SaudeScreen(
                                uiState = uiState,
                                viewModel = viewModel
                            )
                            BolotaTab.PET -> PetChatScreen(
                                uiState = uiState,
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }
}
