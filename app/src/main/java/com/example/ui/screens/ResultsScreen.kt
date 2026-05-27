package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.AtsApiResponse
import com.example.ui.theme.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ResultsScreen(
    response: AtsApiResponse,
    targetedPosition: String,
    onBackToEditor: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    
    // Active Tab State (0: Türkçe CV, 1: English CV, 2: Analiz Raporu)
    var selectedTabState by remember { mutableStateOf(0) }
    val tabTitles = listOf("Türkçe ATS CV", "English ATS CV", "ATS Analizi")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkTealBackground) // Elegant Dark background
    ) {
        // App bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackToEditor,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, SmoothSlate.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .testTag("back_to_editor_button")
            ) {
                Icon(Icons.Default.Close, contentDescription = "Back", tint = DeepSlate)
            }
            Text(
                text = "ATS Analiz Sonucu",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DeepSlate
            )
            Spacer(modifier = Modifier.width(48.dp)) // Equal space balancing
        }

        // Score Card View
        Card(
            colors = CardDefaults.cardColors(containerColor = RichSlate),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, SmoothSlate.copy(alpha = 0.12f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Circular Radial Gauge
                RadialScoreGauge(score = response.score)

                // Summary analysis information
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val ratingLabel = when {
                        response.score >= 80 -> "Harika! 🌟"
                        response.score >= 60 -> "Geliştirilmeli 📈"
                        else -> "Zayıf / Kritik ⚠️"
                    }
                    val ratingColor = when {
                        response.score >= 80 -> AccentTeal
                        response.score >= 60 -> Color(0xFFF59E0B)
                        else -> Color(0xFFEF4444)
                    }

                    Text(
                        text = ratingLabel,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ratingColor
                    )
                    
                    Text(
                        text = if (targetedPosition.isNotBlank()) targetedPosition else "Genel İş Başvurusu",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SmoothSlate,
                        maxLines = 1
                    )
                    
                    Text(
                        text = "CV'niz başarıyla Türkçe ve İngilizce dillerine çevrildi ve formatlandı. İlgili sekmeleri tıklayarak detaylıca inceleyebilirsiniz.",
                        fontSize = 11.sp,
                        color = SmoothSlate.copy(alpha = 0.7f),
                        lineHeight = 15.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Beautiful TabRow for view switching
        TabRow(
            selectedTabIndex = selectedTabState,
            containerColor = DeepIndigoBackgroundCharcoal,
            contentColor = AccentTeal,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabState]),
                    color = AccentTeal,
                    height = 3.dp
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(8.dp))
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabState == index,
                    onClick = { selectedTabState = index },
                    modifier = Modifier.testTag("results_tab_$index")
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            fontSize = 13.sp,
                            fontWeight = if (selectedTabState == index) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTabState == index) AccentTeal else SmoothSlate
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tab Content Display with simple slide-overs
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(RichSlate)
                .border(1.dp, SmoothSlate.copy(alpha = 0.1f), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
        ) {
            when (selectedTabState) {
                0 -> {
                    // Türkçe ATS CV
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SecondarySlate)
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Türkçe Çıktı", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SmoothSlate)
                            CopyToClipboardButton(response.turkishCv, "Türkçe CV'yi Kopyala")
                        }
                        
                        MarkdownViewer(
                            markdownText = response.turkishCv,
                            modifier = Modifier.weight(1f).testTag("turkish_cv_markdown")
                        )
                    }
                }
                1 -> {
                    // English ATS CV
                    valueForEnTab(response, clipboardManager, context)
                }
                2 -> {
                    // ATS Analizi
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Missing keywords chips
                        Text(
                            text = "Eksik Anahtar Kelimeler & Terimler",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepSlate
                        )
                        Text(
                            text = "Aşağıdaki kelimeler iş tanımında yer alıyor ancak özgeçmişinizde eksik veya zayıf. Kopyalamak için üzerlerine tıklayın:",
                            fontSize = 11.sp,
                            color = SmoothSlate.copy(alpha = 0.7f),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        if (response.missingKeywords.isNotEmpty()) {
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(response.missingKeywords) { keyword ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(RedHighlightBg)
                                            .border(1.dp, RedHighlightBorder, RoundedCornerShape(8.dp))
                                            .clickable {
                                                clipboardManager.setText(AnnotatedString(keyword))
                                                Toast.makeText(context, "'$keyword' panoya kopyalandı!", Toast.LENGTH_SHORT).show()
                                            }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(Icons.Default.Share, contentDescription = "Add", tint = RedHighlightText, modifier = Modifier.size(12.dp))
                                            Text(
                                                text = keyword,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = RedHighlightText
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AccentTeal.copy(alpha = 0.08f))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "✨ Harika! Özgeçmişiniz hedefler ve iş tanımı için kritik tüm anahtar kelimeleri kapsıyor.",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AccentTeal
                                )
                            }
                        }

                        HorizontalDivider(
                            color = SmoothSlate.copy(alpha = 0.1f),
                            modifier = Modifier.padding(vertical = 12.dp)
                        )

                        Text(
                            text = "Geliştirme Tavsiyeleri & Detaylı İnceleme",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepSlate,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        MarkdownViewer(
                            markdownText = response.analysisFeedback,
                            modifier = Modifier.weight(1f).testTag("analysis_viewer")
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun valueForEnTab(
    response: AtsApiResponse,
    clipboardManager: androidx.compose.ui.platform.ClipboardManager,
    context: android.content.Context
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SecondarySlate)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("English Output", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SmoothSlate)
            CopyToClipboardButton(response.englishCv, "English CV'yi Kopyala")
        }
        
        MarkdownViewer(
            markdownText = response.englishCv,
            modifier = Modifier.weight(1f).testTag("english_cv_markdown")
        )
    }
}
