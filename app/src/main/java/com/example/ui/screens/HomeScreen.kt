package com.example.ui.screens

import com.example.ui.theme.*
import android.text.format.DateUtils
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.AtsReportEntity
import com.example.ui.viewmodel.AtsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: AtsViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Core inputs
    val originalCvText by viewModel.originalCvText.collectAsState()
    val targetedPosition by viewModel.targetedPosition.collectAsState()
    val jobDescription by viewModel.jobDescription.collectAsState()
    val extraGuidance by viewModel.extraGuidance.collectAsState()
    val savedReports by viewModel.savedReports.collectAsState()
    
    // Toggle state to collapse/expand optional targeted variables for cleaner look
    var areTargetsExpanded by remember { mutableStateOf(false) }

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.loadFileContent(context, it) }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp, top = 8.dp)
    ) {
        
        // Welcome and Intro Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = RichSlate),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "ATS Uyumlu CV Hazırlayıcı 🚀",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Özgeçmişinizi ATS tarayıcılarından geçebilecek biçimde yeniden yapılandırın. Türkçe ve İngilizce çıktıları anında alın.",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // CV Source Selection Row & Input Edit
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Özgeçmiş Metniniz",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepSlate
                    )
                    
                    // File Loader button
                    Button(
                        onClick = { filePickerLauncher.launch("*/*") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SmoothSlate,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Add File", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Dosya Seç", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Beautiful editable multi-line Text Area
                OutlinedTextField(
                    value = originalCvText,
                    onValueChange = { viewModel.updateOriginalCvText(it) },
                    placeholder = {
                        Text(
                            text = "Özgeçmişinizi buraya yapıştırın veya yukarıdan bir PDF ya da metin dosyası (.txt, .md) seçin...",
                            fontSize = 13.sp,
                            color = SmoothSlate.copy(alpha = 0.6f)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .testTag("cv_text_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentTeal,
                        unfocusedBorderColor = SmoothSlate.copy(alpha = 0.4f),
                        focusedContainerColor = RichSlate,
                        unfocusedContainerColor = RichSlate,
                        focusedTextColor = DeepSlate,
                        unfocusedTextColor = DeepSlate
                    ),
                    textStyle = TextStyle(fontSize = 13.sp, lineHeight = 20.sp)
                )
            }
        }

        // Collapsible Targeted Options Section
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = RichSlate),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, SmoothSlate.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { areTargetsExpanded = !areTargetsExpanded }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = "Targets", tint = AccentTeal, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Başvurulacak Pozisyon & İş Tanımı",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepSlate
                            )
                        }
                        Icon(
                            imageVector = if (areTargetsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expand",
                            tint = SmoothSlate
                        )
                    }

                    AnimatedVisibility(visible = areTargetsExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            HorizontalDivider(color = SmoothSlate.copy(alpha = 0.1f))
                            
                            // Target Position Input
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Hedef Pozisyon (İsteğe Bağlı)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SmoothSlate)
                                OutlinedTextField(
                                    value = targetedPosition,
                                    onValueChange = { viewModel.updateTargetedPosition(it) },
                                    placeholder = { Text("E.g., Senior Android Developer", fontSize = 13.sp) },
                                    modifier = Modifier.fillMaxWidth().testTag("position_input"),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AccentTeal,
                                        focusedContainerColor = SecondarySlate,
                                        unfocusedContainerColor = SecondarySlate,
                                        focusedTextColor = DeepSlate,
                                        unfocusedTextColor = DeepSlate
                                    )
                                )
                            }

                            // Job Description Input
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("İş İlanı Açıklaması / Nitelikler (İsteğe Bağlı)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SmoothSlate)
                                OutlinedTextField(
                                    value = jobDescription,
                                    onValueChange = { viewModel.updateJobDescription(it) },
                                    placeholder = { Text("İlan açıklamalarını yapıştırarak eksik anahtar kelimeleri ve uyumluluk raporunu elde edin...", fontSize = 13.sp) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp)
                                        .testTag("job_description_input"),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AccentTeal,
                                        focusedContainerColor = SecondarySlate,
                                        unfocusedContainerColor = SecondarySlate,
                                        focusedTextColor = DeepSlate,
                                        unfocusedTextColor = DeepSlate
                                    )
                                )
                            }

                            // Custom Directives / Extra Guidance
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Özel Direktifler / Ek İstekler (İsteğe Bağlı)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SmoothSlate)
                                OutlinedTextField(
                                    value = extraGuidance,
                                    onValueChange = { viewModel.updateExtraGuidance(it) },
                                    placeholder = { Text("E.g., 'Yapay zeka projelerimi daha fazla vurgula' veya 'Eğitim bilgilerimi kısalt'", fontSize = 13.sp) },
                                    modifier = Modifier.fillMaxWidth().testTag("guidance_input"),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AccentTeal,
                                        focusedContainerColor = SecondarySlate,
                                        unfocusedContainerColor = SecondarySlate,
                                        focusedTextColor = DeepSlate,
                                        unfocusedTextColor = DeepSlate
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Generate and Clear Actions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Clear inputs
                OutlinedButton(
                    onClick = { viewModel.clearInputs() },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                    modifier = Modifier.weight(0.35f).height(50.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Temizle", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                // Core Submit Button
                Button(
                    onClick = { viewModel.generateAtsCv() },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentTeal, contentColor = DeepIndigoAccent),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(0.65f)
                        .height(50.dp)
                        .testTag("generate_cv_button")
                ) {
                    Icon(Icons.Default.Done, contentDescription = "Generate", modifier = Modifier.size(20.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("CV Oluştur & Puanla", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Section: History of Reports
        if (savedReports.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Geçmiş CV Raporlarınız (${savedReports.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepSlate
                )
            }

            items(savedReports) { report ->
                HistoryReportCard(
                    report = report,
                    onClick = { viewModel.handleReportSelection(report) },
                    onDelete = { viewModel.deleteReport(report.id) }
                )
            }
        } else {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Empty History",
                        tint = SmoothSlate.copy(alpha = 0.3f),
                        modifier = Modifier.size(40.dp)
                    )
                    Text(
                        text = "Henüz oluşturulmuş bir rapor bulunmuyor.",
                        fontSize = 13.sp,
                        color = SmoothSlate.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryReportCard(
    report: AtsReportEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val relativeTime = DateUtils.getRelativeTimeSpanString(
        report.timestamp,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS
    )

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = RichSlate),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("history_item_${report.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (report.targetedPosition.isNotBlank()) report.targetedPosition else "Genel ATS İncelemesi",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepSlate,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = relativeTime.toString(),
                    fontSize = 12.sp,
                    color = SmoothSlate.copy(alpha = 0.6f)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Score indicator pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when {
                                report.score >= 80 -> AccentTeal.copy(alpha = 0.15f)
                                report.score >= 60 -> Color(0xFFF59E0B).copy(alpha = 0.15f)
                                else -> Color(0xFFEF4444).copy(alpha = 0.15f)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${report.score} Puan",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            report.score >= 80 -> AccentTeal
                            report.score >= 60 -> Color(0xFFD97706)
                            else -> Color(0xFFDC2626)
                        }
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Delete button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp).testTag("delete_report_${report.id}")
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Raporu Sil",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
