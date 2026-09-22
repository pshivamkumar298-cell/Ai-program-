package com.example.ui.tools

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.api.GeminiService
import com.example.ui.theme.CyanSpark
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.PurpleRadiance

data class SampleVisualAsset(
    val id: String,
    val title: String,
    val category: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val samplePrompt: String,
    val generateBitmap: () -> Bitmap
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultimodalSheet(
    onDismiss: () -> Unit,
    onSelectImage: (base64: String, prompt: String, previewName: String) -> Unit,
    onLaunchPhotoPicker: () -> Unit
) {
    val sampleAssets = remember {
        listOf(
            SampleVisualAsset(
                id = "calc_derivation",
                title = "Calculus & Geometry Diagram",
                category = "Math & STEM",
                icon = Icons.Default.SquareFoot,
                samplePrompt = "Analyze this geometric curve diagram. Derive the equation of the tangent line at the peak and calculate the enclosed area.",
                generateBitmap = {
                    createDiagramBitmap("f(x) = -x² + 4", "Tangent Line at (0,4)")
                }
            ),
            SampleVisualAsset(
                id = "system_arch",
                title = "Microservices Architecture",
                category = "Software & Tech",
                icon = Icons.Default.DeviceHub,
                samplePrompt = "Review this system architecture blueprint. Identify single points of failure and suggest a resilient event-driven upgrade.",
                generateBitmap = {
                    createArchitectureBitmap()
                }
            ),
            SampleVisualAsset(
                id = "financial_chart",
                title = "Quarterly Revenue Breakdown",
                category = "Business & Data",
                icon = Icons.Default.BarChart,
                samplePrompt = "Analyze this quarterly growth chart. Calculate YoY growth rate, identify top drivers, and summarize trade-offs.",
                generateBitmap = {
                    createChartBitmap()
                }
            ),
            SampleVisualAsset(
                id = "physics_kinetics",
                title = "Physics Projectile Motion",
                category = "Academic Tutor",
                icon = Icons.Default.Speed,
                samplePrompt = "Solve this projectile motion problem: Initial velocity 25 m/s at 35°. Calculate maximum altitude and range.",
                generateBitmap = {
                    createPhysicsBitmap()
                }
            )
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ImageSearch,
                        contentDescription = "Multimodal",
                        tint = CyanSpark,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Multimodal Visual Analysis",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Text(
                text = "Attach photos, diagrams, equations, or choose a technical sample for immediate multimodal reasoning:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Device Gallery Picker button
            OutlinedButton(
                onClick = {
                    onLaunchPhotoPicker()
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pick Image from Device Gallery")
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Preset Technical Diagrams & Charts:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                sampleAssets.forEach { asset ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .clickable {
                                val bmp = asset.generateBitmap()
                                val base64 = GeminiService.bitmapToBase64(bmp)
                                onSelectImage(base64, asset.samplePrompt, asset.title)
                                onDismiss()
                            },
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = ElectricIndigo.copy(alpha = 0.15f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = asset.icon,
                                        contentDescription = null,
                                        tint = ElectricIndigo,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = asset.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = asset.category,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private fun createDiagramBitmap(label: String, sublabel: String): Bitmap {
    val bitmap = Bitmap.createBitmap(400, 300, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(AndroidColor.rgb(15, 23, 42))

    val gridPaint = Paint().apply {
        color = AndroidColor.argb(40, 255, 255, 255)
        strokeWidth = 1f
    }
    for (x in 0..400 step 40) canvas.drawLine(x.toFloat(), 0f, x.toFloat(), 300f, gridPaint)
    for (y in 0..300 step 40) canvas.drawLine(0f, y.toFloat(), 400f, y.toFloat(), gridPaint)

    val curvePaint = Paint().apply {
        color = AndroidColor.rgb(56, 189, 248)
        strokeWidth = 4f
        isAntiAlias = true
        style = Paint.Style.STROKE
    }
    val path = android.graphics.Path()
    path.moveTo(40f, 260f)
    path.quadTo(200f, 40f, 360f, 260f)
    canvas.drawPath(path, curvePaint)

    val textPaint = Paint().apply {
        color = AndroidColor.WHITE
        textSize = 20f
        isAntiAlias = true
    }
    canvas.drawText(label, 60f, 60f, textPaint)
    textPaint.textSize = 14f
    textPaint.color = AndroidColor.LTGRAY
    canvas.drawText(sublabel, 60f, 85f, textPaint)

    return bitmap
}

private fun createArchitectureBitmap(): Bitmap {
    val bitmap = Bitmap.createBitmap(400, 300, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(AndroidColor.rgb(17, 24, 39))

    val boxPaint = Paint().apply {
        color = AndroidColor.rgb(99, 102, 241)
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }
    val textPaint = Paint().apply {
        color = AndroidColor.WHITE
        textSize = 15f
        isAntiAlias = true
    }

    canvas.drawRoundRect(40f, 40f, 160f, 100f, 12f, 12f, boxPaint)
    canvas.drawText("API Gateway", 55f, 75f, textPaint)

    canvas.drawRoundRect(240f, 40f, 360f, 100f, 12f, 12f, boxPaint)
    canvas.drawText("Auth Service", 255f, 75f, textPaint)

    canvas.drawRoundRect(140f, 180f, 260f, 240f, 12f, 12f, boxPaint)
    canvas.drawText("Event Bus", 160f, 215f, textPaint)

    val linePaint = Paint().apply {
        color = AndroidColor.rgb(56, 189, 248)
        strokeWidth = 2f
    }
    canvas.drawLine(100f, 100f, 170f, 180f, linePaint)
    canvas.drawLine(300f, 100f, 230f, 180f, linePaint)

    return bitmap
}

private fun createChartBitmap(): Bitmap {
    val bitmap = Bitmap.createBitmap(400, 300, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(AndroidColor.rgb(15, 23, 42))

    val barPaint = Paint().apply {
        color = AndroidColor.rgb(168, 85, 247)
        style = Paint.Style.FILL
    }
    val textPaint = Paint().apply {
        color = AndroidColor.WHITE
        textSize = 14f
        isAntiAlias = true
    }

    val heights = listOf(80f, 130f, 175f, 220f)
    val labels = listOf("Q1: $1.2M", "Q2: $1.9M", "Q3: $2.6M", "Q4: $3.4M")
    heights.forEachIndexed { i, h ->
        val left = 50f + i * 85f
        canvas.drawRoundRect(left, 260f - h, left + 50f, 260f, 8f, 8f, barPaint)
        canvas.drawText(labels[i], left - 5f, 280f, textPaint)
    }

    textPaint.textSize = 18f
    canvas.drawText("Annual SaaS Revenue Velocity", 50f, 40f, textPaint)
    return bitmap
}

private fun createPhysicsBitmap(): Bitmap {
    val bitmap = Bitmap.createBitmap(400, 300, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(AndroidColor.rgb(15, 23, 42))

    val trajPaint = Paint().apply {
        color = AndroidColor.rgb(245, 158, 11)
        strokeWidth = 3f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }
    val path = android.graphics.Path()
    path.moveTo(30f, 250f)
    path.cubicTo(120f, 60f, 280f, 60f, 370f, 250f)
    canvas.drawPath(path, trajPaint)

    val textPaint = Paint().apply {
        color = AndroidColor.WHITE
        textSize = 16f
        isAntiAlias = true
    }
    canvas.drawText("v₀ = 25 m/s, θ = 35°", 40f, 50f, textPaint)
    textPaint.textSize = 13f
    textPaint.color = AndroidColor.LTGRAY
    canvas.drawText("Apex: h_max = (v₀ sinθ)² / 2g", 40f, 80f, textPaint)

    return bitmap
}
