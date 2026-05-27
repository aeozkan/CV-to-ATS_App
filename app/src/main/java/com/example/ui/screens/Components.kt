package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.ui.theme.*

// Central Colors for the Elegant Dark Theme
val DeepSlate = TextPrimary            // On Surface Light Grey #E6E1E5 for text
val RichSlate = SurfaceDarkCard        // M3 Card Surface Background #2B2930 for cards
val SmoothSlate = TextSecondary        // On Surface Variant Smooth Grey #938F99 for borders/placeholders
val AccentTeal = PrimaryTeal           // Elegant Light Purple Accent #D0BCFF
val SoftTeal = DeepIndigoAccent        // Deep Indigo Accent #381E72 for highlights/tints
val DarkTealBackground = SecondarySlate // Dark Slate Base Background #1C1B1F for main layouts


// Deeply customizable radial gauge to show ATS Resume Score
@Composable
fun RadialScoreGauge(
    score: Int,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    strokeWidth: Dp = 12.dp
) {
    val animatedScore = remember { Animatable(0f) }
    LaunchedEffect(score) {
        animatedScore.animateTo(
            targetValue = score.toFloat(),
            animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
        )
    }

    val progress = animatedScore.value / 100f
    
    // Determine dynamic score colors
    val scoreColor = when {
        score >= 80 -> AccentTeal
        score >= 60 -> Color(0xFFF59E0B) // Amber/Orange
        else -> Color(0xFFEF4444)        // Red
    }
    
    val trackColor = scoreColor.copy(alpha = 0.15f)

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokeWidthPx = strokeWidth.toPx()
            val arcSize = size.toPx() - strokeWidthPx
            val topLeftOffset = strokeWidthPx / 2
            
            // Draw Background Track Circle
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(topLeftOffset, topLeftOffset),
                size = Size(arcSize, arcSize),
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )
            
            // Draw Active Score Progress Arc starting from top (-90 degrees)
            drawArc(
                color = scoreColor,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                topLeft = Offset(topLeftOffset, topLeftOffset),
                size = Size(arcSize, arcSize),
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${animatedScore.value.toInt()}",
                fontSize = 32.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.ExtraBold,
                color = DeepSlate,
                textAlign = TextAlign.Center
            )
            Text(
                text = "ATS PUANI",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SmoothSlate,
                letterSpacing = 1.sp
            )
        }
    }
}

// Custom Markdown Renderer that translates headers and lists into Compose UI elements
@Composable
fun MarkdownViewer(
    markdownText: String,
    modifier: Modifier = Modifier
) {
    val lines = remember(markdownText) { markdownText.split("\n") }
    
    SelectionContainer {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(lines) { line ->
                val trimmed = line.trim()
                when {
                    trimmed.startsWith("#### ") -> {
                        Text(
                            text = trimmed.removePrefix("#### ").replace("**", ""),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepSlate,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                    }
                    trimmed.startsWith("### ") -> {
                        Text(
                            text = trimmed.removePrefix("### ").replace("**", ""),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = DeepSlate,
                            modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
                        )
                    }
                    trimmed.startsWith("## ") -> {
                        Text(
                            text = trimmed.removePrefix("## ").replace("**", ""),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = DeepSlate,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                        HorizontalDivider(color = SmoothSlate.copy(alpha = 0.2f), thickness = 1.dp)
                    }
                    trimmed.startsWith("# ") -> {
                        Text(
                            text = trimmed.removePrefix("# ").replace("**", ""),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = DeepSlate,
                            modifier = Modifier.padding(top = 20.dp, bottom = 10.dp)
                        )
                        HorizontalDivider(color = AccentTeal, thickness = 2.dp, modifier = Modifier.padding(bottom = 8.dp))
                    }
                    trimmed.startsWith("* ") || trimmed.startsWith("- ") -> {
                        val pureText = trimmed.removePrefix("* ").removePrefix("- ")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 8.dp, top = 2.dp, bottom = 2.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "• ",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentTeal
                            )
                            Text(
                                text = parseInlineBold(pureText),
                                fontSize = 14.sp,
                                color = TextPrimary,
                                lineHeight = 20.sp
                            )
                        }
                    }
                    trimmed.startsWith("> ") -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(AccentTeal.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                                .padding(12.dp)
                        ) {
                            Spacer(
                                modifier = Modifier
                                    .width(4.dp)
                                    .fillMaxHeight()
                                    .background(AccentTeal)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = trimmed.removePrefix("> "),
                                fontSize = 13.sp,
                                color = DeepSlate,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    trimmed.isBlank() -> {
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    else -> {
                        Text(
                            text = parseInlineBold(trimmed),
                            fontSize = 14.sp,
                            color = TextPrimary,
                            lineHeight = 22.sp,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

// Convert **text** inside string into AnnotatedString with bold styles
fun parseInlineBold(text: String): AnnotatedString {
    val builder = AnnotatedString.Builder()
    val parts = text.split("**")
    for (i in parts.indices) {
        if (i % 2 == 1) {
            builder.pushStyle(androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold, color = DeepSlate))
            builder.append(parts[i])
            builder.pop()
        } else {
            builder.append(parts[i])
        }
    }
    return builder.toAnnotatedString()
}

// Clipboard copy action button
@Composable
fun CopyToClipboardButton(
    textToCopy: String,
    label: String = "Panoya Kopyala",
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    var copied by remember { mutableStateOf(false) }

    LaunchedEffect(copied) {
        if (copied) {
            delay(2000)
            copied = false
        }
    }

    Button(
        onClick = {
            if (textToCopy.isNotBlank()) {
                clipboardManager.setText(AnnotatedString(textToCopy))
                Toast.makeText(context, "$label kopyalandı!", Toast.LENGTH_SHORT).show()
                copied = true
            }
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = if (copied) Color(0xFF0F766E) else AccentTeal,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.height(48.dp)
    ) {
        if (copied) {
            Icon(Icons.Default.Done, contentDescription = "Done", modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Kopyalandı!", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        } else {
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}
