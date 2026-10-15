package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.BentoBackground
import com.example.ui.theme.BentoLavenderBorder
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoPrimaryDark
import com.example.ui.theme.BentoTilePurple
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.util.ImageUtils

data class PathPoint(
    val path: Path,
    val strokeWidth: Float = 6f,
    val color: Color = Color(0xFF1C1B1F)
)

@Composable
fun SignaturePadDialog(
    title: String = "Tanda Tangan Digital",
    signerName: String = "",
    onDismiss: () -> Unit,
    onSignatureSaved: (uriString: String) -> Unit
) {
    val context = LocalContext.current
    val paths = remember { mutableStateListOf<PathPoint>() }
    var currentPath by remember { mutableStateOf<Path?>(null) }
    var lastPoint by remember { mutableStateOf<Offset?>(null) }

    var selectedColor by remember { mutableStateOf(Color(0xFF1C1B1F)) }
    var selectedStrokeWidth by remember { mutableFloatStateOf(6f) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(12.dp)
                .testTag("signature_dialog_card"),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = BentoTilePurple,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Draw, contentDescription = null, tint = BentoPrimary, modifier = Modifier.size(20.dp))
                            }
                        }
                        Column {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                            if (signerName.isNotBlank()) {
                                Text(
                                    text = "Atas nama: $signerName",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate500
                                )
                            }
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_signature_btn")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Slate500)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Pen Controls: Color & Stroke Width
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Ink Colors
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tinta:",
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate500,
                            fontWeight = FontWeight.Medium
                        )
                        val inkColors = listOf(
                            Color(0xFF1C1B1F) to "Hitam",
                            Color(0xFF003366) to "Navy",
                            Color(0xFF1A56DB) to "Biru"
                        )
                        inkColors.forEach { (color, _) ->
                            val isChosen = selectedColor == color
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(color, CircleShape)
                                    .border(
                                        width = if (isChosen) 2.5.dp else 1.dp,
                                        color = if (isChosen) BentoPrimary else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColor = color }
                            )
                        }
                    }

                    // Stroke Thickness
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Garis:",
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate500,
                            fontWeight = FontWeight.Medium
                        )
                        val thicknesses = listOf(4f to "Tipis", 6f to "Sedang", 9f to "Tebal")
                        thicknesses.forEach { (width, label) ->
                            val isChosen = selectedStrokeWidth == width
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isChosen) BentoTilePurple else Color.Transparent,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isChosen) BentoLavenderBorder else Slate200
                                ),
                                modifier = Modifier.clickable { selectedStrokeWidth = width }
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isChosen) BentoPrimaryDark else Slate700,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Canvas Area for drawing with Bézier smoothing
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .background(Color(0xFFFAFAFD), RoundedCornerShape(18.dp))
                        .border(1.5.dp, Color(0xFFD0BCFF), RoundedCornerShape(18.dp))
                        .testTag("signature_canvas_box")
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(selectedColor, selectedStrokeWidth) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        val p = Path().apply {
                                            moveTo(offset.x, offset.y)
                                        }
                                        currentPath = p
                                        lastPoint = offset
                                    },
                                    onDrag = { change, _ ->
                                        change.consume()
                                        val prev = lastPoint
                                        val current = change.position
                                        if (prev != null) {
                                            // Smooth Bézier curve interpolation
                                            val midX = (prev.x + current.x) / 2
                                            val midY = (prev.y + current.y) / 2
                                            currentPath?.quadraticTo(prev.x, prev.y, midX, midY)
                                            currentPath = Path().apply {
                                                currentPath?.let { addPath(it) }
                                            }
                                        } else {
                                            currentPath?.lineTo(current.x, current.y)
                                        }
                                        lastPoint = current
                                    },
                                    onDragEnd = {
                                        currentPath?.let {
                                            paths.add(PathPoint(it, selectedStrokeWidth, selectedColor))
                                            currentPath = null
                                            lastPoint = null
                                        }
                                    },
                                    onDragCancel = {
                                        currentPath = null
                                        lastPoint = null
                                    }
                                )
                            }
                    ) {
                        // Draw guideline baseline
                        drawLine(
                            color = Color(0xFFD0BCFF).copy(alpha = 0.6f),
                            start = Offset(40f, size.height * 0.78f),
                            end = Offset(size.width - 40f, size.height * 0.78f),
                            strokeWidth = 1.5f,
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                        )

                        // Draw committed paths
                        paths.forEach { pathPoint ->
                            drawPath(
                                path = pathPoint.path,
                                color = pathPoint.color,
                                style = Stroke(
                                    width = pathPoint.strokeWidth,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }

                        // Draw current active stroke
                        currentPath?.let {
                            drawPath(
                                path = it,
                                color = selectedColor,
                                style = Stroke(
                                    width = selectedStrokeWidth,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                    }

                    if (paths.isEmpty() && currentPath == null) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Draw,
                                contentDescription = null,
                                tint = Slate300,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Goreskan tanda tangan tulisan tangan di sini",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = Slate500
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action buttons: Undo, Clear, Save
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            if (paths.isNotEmpty()) {
                                paths.removeAt(paths.size - 1)
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        enabled = paths.isNotEmpty(),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("signature_undo_btn")
                    ) {
                        Icon(Icons.Default.Undo, contentDescription = "Undo", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Batal", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            paths.clear()
                            currentPath = null
                            lastPoint = null
                        },
                        shape = RoundedCornerShape(14.dp),
                        enabled = paths.isNotEmpty(),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("signature_clear_btn")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            if (paths.isNotEmpty()) {
                                val bitmap = createSignatureBitmap(paths, 700, 350)
                                val savedUri = ImageUtils.saveBitmapToInternalStorage(context, bitmap, "signature")
                                onSignatureSaved(savedUri)
                                onDismiss()
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        enabled = paths.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary),
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("signature_save_btn")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Simpan", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Simpan TTd", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

private fun createSignatureBitmap(paths: List<PathPoint>, width: Int, height: Int): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // White background for crisp rendering in PDF document
    canvas.drawColor(android.graphics.Color.WHITE)

    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    paths.forEach { pathPoint ->
        paint.color = when (pathPoint.color) {
            Color(0xFF003366) -> android.graphics.Color.rgb(0, 51, 102)
            Color(0xFF1A56DB) -> android.graphics.Color.rgb(26, 86, 219)
            else -> android.graphics.Color.BLACK
        }
        paint.strokeWidth = pathPoint.strokeWidth * 1.3f
        canvas.drawPath(pathPoint.path.asAndroidPath(), paint)
    }

    return bitmap
}

