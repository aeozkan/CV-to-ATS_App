package com.example.ui.screens

import com.example.ui.theme.*
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.AtsApiState
import com.example.ui.viewmodel.AtsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(
    viewModel: AtsViewModel,
    modifier: Modifier = Modifier
) {
    val apiState by viewModel.apiState.collectAsState()
    val position by viewModel.targetedPosition.collectAsState()
    
    var errorToShow by remember { mutableStateOf<String?>(null) }

    // Sync state errors to dismissable overlay value
    LaunchedEffect(apiState) {
        if (apiState is AtsApiState.Error) {
            errorToShow = (apiState as AtsApiState.Error).message
        } else if (apiState is AtsApiState.Loading || apiState is AtsApiState.Idle) {
            errorToShow = null
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "ATS Özgeçmiş Sihirbazı",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(AccentTeal)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "AI",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkTealBackground
                ),
                modifier = Modifier.testTag("app_top_bar")
            )
        },
        containerColor = DarkTealBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            
            // Switch screen based on current state
            when (val state = apiState) {
                is AtsApiState.Success -> {
                    ResultsScreen(
                        response = state.response,
                        targetedPosition = position,
                        onBackToEditor = { viewModel.handleReportSelection(null) }
                    )
                }
                is AtsApiState.Loading -> {
                    AtsLoadingView(step = state.step)
                }
                else -> {
                    // Show standard Input/Home Screen (Idle or Error fallback)
                    HomeScreen(viewModel = viewModel)
                }
            }

            // Beautiful top error notification banner
            AnimatedVisibility(
                visible = errorToShow != null,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut(),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .testTag("error_banner")
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = RedHighlightBg),
                    border = BorderStroke(1.dp, RedHighlightBorder),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = RedHighlightText,
                            modifier = Modifier.size(24.dp)
                        )
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Bir Sorun Oluştu",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = RedHighlightText
                            )
                            Text(
                                text = errorToShow ?: "",
                                fontSize = 12.sp,
                                color = RedHighlightText.copy(alpha = 0.9f),
                                lineHeight = 16.sp
                            )
                        }

                        IconButton(
                            onClick = { errorToShow = null },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = RedHighlightText,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AtsLoadingView(step: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkTealBackground)
            .padding(24.dp)
            .testTag("loading_view"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = AccentTeal,
            strokeWidth = 4.dp,
            modifier = Modifier.size(60.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Yapay Zeka Çalışıyor...",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = DeepSlate,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(10.dp))
        
        Text(
            text = step,
            fontSize = 14.sp,
            color = SmoothSlate,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(0.85f),
            lineHeight = 22.sp
        )
    }
}
