package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint as AndroidPaint
import android.graphics.Path as AndroidPath
import android.graphics.RectF as AndroidRectF
import android.graphics.Typeface
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FormatColorText
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.Rectangle
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoPrimaryDark
import com.example.ui.theme.IndustrialAmber
import com.example.ui.theme.SafetyGreen
import com.example.ui.theme.SafetyRed
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.util.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * Annotation & Shape Models
 * Coordinates are normalized in [0f..1f] relative to the image bounds for perfect scaling & cropping.
 */
sealed class PhotoElement {
    data class Arrow(
        val start: Offset, // Normalized (0..1)
        val end: Offset,   // Normalized (0..1)
        val color: Color,
        val strokeWidth: Float
    ) : PhotoElement()

    data class Box(
        val rect: Rect,    // Normalized (0..1)
        val color: Color,
        val strokeWidth: Float
    ) : PhotoElement()

    data class TextLabel(
        val text: String,
        val position: Offset, // Normalized (0..1)
        val color: Color,
        val isWarningBadge: Boolean = true
    ) : PhotoElement()
}

enum class ToolMode {
    ARROW,
    BOX,
    TEXT,
    CROP
}

enum class CropAspectRatio(val label: String, val ratio: Float?) {
    FREE("Bebas", null),
    SQUARE("1:1", 1f),
    STANDARD("4:3", 4f / 3f),
    WIDE("16:9", 16f / 9f)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InspectionPhotoEditorDialog(
    photoUriString: String,
    title: String = "Edit & Anotasi Foto",
    onDismissRequest: () -> Unit,
    onPhotoSaved: (newUriString: String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var activeBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoadingBitmap by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    // Tool State
    var selectedTool by remember { mutableStateOf(ToolMode.ARROW) }
    var selectedColor by remember { mutableStateOf(SafetyRed) }
    var selectedStrokeWidth by remember { mutableFloatStateOf(5f) }

    // Annotations stack
    val elements = remember { mutableStateListOf<PhotoElement>() }

    // Active drag tracking for preview
    var dragStartNormalized by remember { mutableStateOf<Offset?>(null) }
    var dragCurrentNormalized by remember { mutableStateOf<Offset?>(null) }

    // Text Input Dialog state
    var showTextInputDialog by remember { mutableStateOf(false) }
    var pendingTextPosition by remember { mutableStateOf(Offset(0.5f, 0.5f)) }
    var textInputContent by remember { mutableStateOf("") }

    // Crop Tool State (Normalized 0..1)
    var cropRectNormalized by remember { mutableStateOf(Rect(0.05f, 0.05f, 0.95f, 0.95f)) }
    var selectedCropRatio by remember { mutableStateOf(CropAspectRatio.FREE) }
    var activeCropHandle by remember { mutableStateOf<String?>(null) }

    // Colors palette
    val paletteColors = listOf(
        SafetyRed,                  // Red (Critical / Defect)
        Color(0xFFF59E0B),          // Amber (Warning / Inspection)
        SafetyGreen,                // Green (Pass / Normal)
        Color(0xFF0284C7),          // Cyan / Blue (Info)
        Color(0xFFFFFFFF),          // White
        Color(0xFF0F172A)           // Black
    )

    // Load original bitmap
    LaunchedEffect(photoUriString) {
        isLoadingBitmap = true
        withContext(Dispatchers.IO) {
            val bmp = ImageUtils.loadScaledBitmap(context, photoUriString, 2048, 2048)
            activeBitmap = bmp
            isLoadingBitmap = false
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF09080E)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // ================= TOP HEADER BAR =================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = onDismissRequest,
                            colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Tutup")
                        }
                        Column {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Garis Panah • Kotak • Teks • Crop",
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate400,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Undo Button
                        IconButton(
                            onClick = {
                                if (elements.isNotEmpty()) {
                                    elements.removeAt(elements.lastIndex)
                                }
                            },
                            enabled = elements.isNotEmpty(),
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = Color.White,
                                disabledContentColor = Color.White.copy(alpha = 0.25f)
                            )
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo Anotasi")
                        }

                        // Clear All Button
                        if (elements.isNotEmpty()) {
                            IconButton(
                                onClick = { elements.clear() },
                                colors = IconButtonDefaults.iconButtonColors(contentColor = SafetyRed)
                            ) {
                                Icon(Icons.Default.DeleteSweep, contentDescription = "Hapus Semua")
                            }
                        }

                        // Save Button
                        Button(
                            onClick = {
                                val currentBmp = activeBitmap ?: return@Button
                                isSaving = true
                                scope.launch(Dispatchers.IO) {
                                    try {
                                        val outputUri = renderAndSaveEditedBitmap(
                                            context = context,
                                            baseBitmap = currentBmp,
                                            elements = elements.toList()
                                        )
                                        withContext(Dispatchers.Main) {
                                            isSaving = false
                                            if (outputUri != null) {
                                                Toast.makeText(context, "Foto berhasil diedit & disimpan!", Toast.LENGTH_SHORT).show()
                                                onPhotoSaved(outputUri)
                                                onDismissRequest()
                                            } else {
                                                Toast.makeText(context, "Gagal menyimpan foto", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        withContext(Dispatchers.Main) {
                                            isSaving = false
                                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            },
                            enabled = !isSaving && activeBitmap != null,
                            colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("save_edited_photo_btn")
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Simpan", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // ================= MAIN INTERACTIVE CANVAS AREA =================
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoadingBitmap || activeBitmap == null) {
                        CircularProgressIndicator(color = BentoPrimary)
                    } else {
                        val bmp = activeBitmap!!
                        BoxWithConstraints(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            val containerWidth = constraints.maxWidth.toFloat()
                            val containerHeight = constraints.maxHeight.toFloat()

                            // Calculate aspect fit dimensions
                            val imgWidth = bmp.width.toFloat()
                            val imgHeight = bmp.height.toFloat()
                            val imgAspect = imgWidth / imgHeight
                            val containerAspect = containerWidth / containerHeight

                            val displayWidth: Float
                            val displayHeight: Float

                            if (imgAspect > containerAspect) {
                                displayWidth = containerWidth
                                displayHeight = containerWidth / imgAspect
                            } else {
                                displayHeight = containerHeight
                                displayWidth = containerHeight * imgAspect
                            }

                            val displayLeft = (containerWidth - displayWidth) / 2f
                            val displayTop = (containerHeight - displayHeight) / 2f
                            val imageRectInContainer = Rect(displayLeft, displayTop, displayLeft + displayWidth, displayTop + displayHeight)

                            // Interactive Canvas
                            Canvas(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pointerInput(selectedTool, selectedCropRatio, activeBitmap) {
                                        when (selectedTool) {
                                            ToolMode.CROP -> {
                                                detectDragGestures(
                                                    onDragStart = { offset ->
                                                        // Check which crop handle is touched
                                                        activeCropHandle = findCropHandle(offset, imageRectInContainer, cropRectNormalized)
                                                    },
                                                    onDragEnd = {
                                                        activeCropHandle = null
                                                    },
                                                    onDragCancel = {
                                                        activeCropHandle = null
                                                    },
                                                    onDrag = { change, dragAmount ->
                                                        change.consume()
                                                        val handle = activeCropHandle ?: return@detectDragGestures
                                                        val normDx = dragAmount.x / imageRectInContainer.width
                                                        val normDy = dragAmount.y / imageRectInContainer.height
                                                        cropRectNormalized = updateCropRect(
                                                            current = cropRectNormalized,
                                                            handle = handle,
                                                            dx = normDx,
                                                            dy = normDy,
                                                            aspectRatio = selectedCropRatio.ratio
                                                        )
                                                    }
                                                )
                                            }
                                            ToolMode.ARROW, ToolMode.BOX -> {
                                                detectDragGestures(
                                                    onDragStart = { offset ->
                                                        val norm = toNormalized(offset, imageRectInContainer)
                                                        dragStartNormalized = norm
                                                        dragCurrentNormalized = norm
                                                    },
                                                    onDragEnd = {
                                                        val start = dragStartNormalized
                                                        val end = dragCurrentNormalized
                                                        if (start != null && end != null && (start - end).getDistance() > 0.02f) {
                                                            if (selectedTool == ToolMode.ARROW) {
                                                                elements.add(
                                                                    PhotoElement.Arrow(
                                                                        start = start,
                                                                        end = end,
                                                                        color = selectedColor,
                                                                        strokeWidth = selectedStrokeWidth
                                                                    )
                                                                )
                                                            } else if (selectedTool == ToolMode.BOX) {
                                                                val minX = min(start.x, end.x)
                                                                val maxX = max(start.x, end.x)
                                                                val minY = min(start.y, end.y)
                                                                val maxY = max(start.y, end.y)
                                                                elements.add(
                                                                    PhotoElement.Box(
                                                                        rect = Rect(minX, minY, maxX, maxY),
                                                                        color = selectedColor,
                                                                        strokeWidth = selectedStrokeWidth
                                                                    )
                                                                )
                                                            }
                                                        }
                                                        dragStartNormalized = null
                                                        dragCurrentNormalized = null
                                                    },
                                                    onDragCancel = {
                                                        dragStartNormalized = null
                                                        dragCurrentNormalized = null
                                                    },
                                                    onDrag = { change, _ ->
                                                        change.consume()
                                                        dragCurrentNormalized = toNormalized(change.position, imageRectInContainer)
                                                    }
                                                )
                                            }
                                            ToolMode.TEXT -> {
                                                detectTapGestures { offset ->
                                                    pendingTextPosition = toNormalized(offset, imageRectInContainer)
                                                    textInputContent = ""
                                                    showTextInputDialog = true
                                                }
                                            }
                                        }
                                    }
                            ) {
                                // 1. Draw base bitmap
                                drawImage(
                                    image = bmp.asImageBitmap(),
                                    srcOffset = IntOffset.Zero,
                                    srcSize = IntSize(bmp.width, bmp.height),
                                    dstOffset = IntOffset(imageRectInContainer.left.toInt(), imageRectInContainer.top.toInt()),
                                    dstSize = IntSize(imageRectInContainer.width.toInt(), imageRectInContainer.height.toInt())
                                )

                                // 2. Draw existing elements
                                elements.forEach { element ->
                                    drawPhotoElement(element, imageRectInContainer)
                                }

                                // 3. Draw active drag preview
                                val start = dragStartNormalized
                                val end = dragCurrentNormalized
                                if (start != null && end != null) {
                                    val startPx = fromNormalized(start, imageRectInContainer)
                                    val endPx = fromNormalized(end, imageRectInContainer)

                                    if (selectedTool == ToolMode.ARROW) {
                                        drawArrowOnCanvas(startPx, endPx, selectedColor, selectedStrokeWidth.dp.toPx())
                                    } else if (selectedTool == ToolMode.BOX) {
                                        val rect = Rect(
                                            min(startPx.x, endPx.x),
                                            min(startPx.y, endPx.y),
                                            max(startPx.x, endPx.x),
                                            max(startPx.y, endPx.y)
                                        )
                                        drawRect(
                                            color = selectedColor,
                                            topLeft = rect.topLeft,
                                            size = rect.size,
                                            style = Stroke(width = selectedStrokeWidth.dp.toPx(), join = StrokeJoin.Round)
                                        )
                                    }
                                }

                                // 4. Draw Crop Overlay (if in CROP mode)
                                if (selectedTool == ToolMode.CROP) {
                                    drawCropOverlay(imageRectInContainer, cropRectNormalized)
                                }
                            }
                        }
                    }
                }

                // ================= BOTTOM EDITING TOOLBAR =================
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF13111C),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Sub-toolbar based on active tool
                        if (selectedTool == ToolMode.CROP) {
                            // Crop Controls: Aspect Ratios + Apply Crop Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Rasio:",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Slate400,
                                        fontWeight = FontWeight.Bold
                                    )
                                    CropAspectRatio.entries.forEach { ratioItem ->
                                        val isSelected = selectedCropRatio == ratioItem
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isSelected) IndustrialAmber else Color(0xFF232030),
                                            modifier = Modifier.clickable {
                                                selectedCropRatio = ratioItem
                                                // Adjust current crop rect to ratio if fixed
                                                if (ratioItem.ratio != null) {
                                                    val r = ratioItem.ratio
                                                    val w = cropRectNormalized.width
                                                    val newH = (w / r).coerceIn(0.1f, 0.9f)
                                                    cropRectNormalized = Rect(
                                                        cropRectNormalized.left,
                                                        cropRectNormalized.top,
                                                        cropRectNormalized.left + w,
                                                        (cropRectNormalized.top + newH).coerceAtMost(1f)
                                                    )
                                                }
                                            }
                                        ) {
                                            Text(
                                                text = ratioItem.label,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color.Black else Color.White,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }

                                Button(
                                    onClick = {
                                        val bmp = activeBitmap ?: return@Button
                                        val cropped = applyCropToBitmap(bmp, cropRectNormalized)
                                        if (cropped != null) {
                                            activeBitmap = cropped
                                            // Reset crop rect for next crop
                                            cropRectNormalized = Rect(0.05f, 0.05f, 0.95f, 0.95f)
                                            selectedTool = ToolMode.ARROW
                                            Toast.makeText(context, "Foto berhasil dipotong (Cropped)", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = IndustrialAmber, contentColor = Color.Black),
                                    modifier = Modifier.testTag("apply_crop_btn")
                                ) {
                                    Icon(Icons.Default.Crop, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Potong", fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            // Annotation Controls: Color Palette + Stroke Width
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Color Circles
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    paletteColors.forEach { color ->
                                        val isSelected = selectedColor == color
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(color)
                                                .border(
                                                    width = if (isSelected) 3.dp else 1.dp,
                                                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.3f),
                                                    shape = CircleShape
                                                )
                                                .clickable { selectedColor = color },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(CircleShape)
                                                        .background(if (color == Color.White) Color.Black else Color.White)
                                                )
                                            }
                                        }
                                    }
                                }

                                // Stroke width picker (Thin / Medium / Thick)
                                if (selectedTool != ToolMode.TEXT) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        listOf(3f to "Halus", 6f to "Sedang", 10f to "Tebal").forEach { (widthVal, label) ->
                                            val isSelected = selectedStrokeWidth == widthVal
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (isSelected) BentoPrimary else Color(0xFF232030),
                                                modifier = Modifier.clickable { selectedStrokeWidth = widthVal }
                                            ) {
                                                Text(
                                                    text = label,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) Color.White else Slate400,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Main Tool Mode Switcher Tabs
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1E1B2E), RoundedCornerShape(16.dp))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ToolTabItem(
                                icon = Icons.AutoMirrored.Filled.ArrowForward,
                                label = "Panah",
                                isSelected = selectedTool == ToolMode.ARROW,
                                onClick = { selectedTool = ToolMode.ARROW }
                            )

                            ToolTabItem(
                                icon = Icons.Default.Rectangle,
                                label = "Kotak",
                                isSelected = selectedTool == ToolMode.BOX,
                                onClick = { selectedTool = ToolMode.BOX }
                            )

                            ToolTabItem(
                                icon = Icons.Default.TextFields,
                                label = "Teks",
                                isSelected = selectedTool == ToolMode.TEXT,
                                onClick = {
                                    selectedTool = ToolMode.TEXT
                                    pendingTextPosition = Offset(0.5f, 0.5f)
                                    textInputContent = ""
                                    showTextInputDialog = true
                                }
                            )

                            ToolTabItem(
                                icon = Icons.Default.Crop,
                                label = "Crop",
                                isSelected = selectedTool == ToolMode.CROP,
                                onClick = { selectedTool = ToolMode.CROP }
                            )
                        }
                    }
                }
            }
        }
    }

    // ================= TEXT INPUT & SUGGESTION DIALOG =================
    if (showTextInputDialog) {
        val quickChips = listOf(
            "CRACK / RETAK",
            "LEAKAGE / BOCOR",
            "AUS / WORN",
            "LOOSE BOLT",
            "OVERHEAT",
            "DAMAGED",
            "NORMAL / GOOD",
            "HM METER"
        )

        AlertDialog(
            onDismissRequest = { showTextInputDialog = false },
            title = {
                Text("Tambah Label / Catatan Teks", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Ketik keterangan atau pilih label inspeksi cepat di bawah:",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate700
                    )

                    OutlinedTextField(
                        value = textInputContent,
                        onValueChange = { textInputContent = it },
                        placeholder = { Text("e.g. Kebocoran oli pada seal...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = false,
                        maxLines = 3
                    )

                    Text(
                        text = "Label Cepat:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Slate800
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quickChips.forEach { chipText ->
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFFEFF6FF),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
                                modifier = Modifier.clickable {
                                    textInputContent = chipText
                                }
                            ) {
                                Text(
                                    text = chipText,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = BentoPrimaryDark,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (textInputContent.isNotBlank()) {
                            elements.add(
                                PhotoElement.TextLabel(
                                    text = textInputContent.trim(),
                                    position = pendingTextPosition,
                                    color = selectedColor
                                )
                            )
                        }
                        showTextInputDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Tambahkan", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTextInputDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun ToolTabItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) BentoPrimary else Color.Transparent,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) Color.White else Slate400,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else Slate400
            )
        }
    }
}

// ================= DRAWING HELPERS =================

private fun DrawScope.drawPhotoElement(element: PhotoElement, imageRect: Rect) {
    when (element) {
        is PhotoElement.Arrow -> {
            val startPx = fromNormalized(element.start, imageRect)
            val endPx = fromNormalized(element.end, imageRect)
            drawArrowOnCanvas(startPx, endPx, element.color, element.strokeWidth.dp.toPx())
        }
        is PhotoElement.Box -> {
            val topLeft = fromNormalized(element.rect.topLeft, imageRect)
            val bottomRight = fromNormalized(element.rect.bottomRight, imageRect)
            val rectPx = Rect(topLeft, bottomRight)
            drawRect(
                color = element.color,
                topLeft = rectPx.topLeft,
                size = rectPx.size,
                style = Stroke(width = element.strokeWidth.dp.toPx(), join = StrokeJoin.Round)
            )
        }
        is PhotoElement.TextLabel -> {
            val posPx = fromNormalized(element.position, imageRect)
            drawTextBadge(element.text, posPx, element.color)
        }
    }
}

private fun DrawScope.drawArrowOnCanvas(start: Offset, end: Offset, color: Color, strokeWidthPx: Float) {
    // Draw shaft
    drawLine(
        color = color,
        start = start,
        end = end,
        strokeWidth = strokeWidthPx,
        cap = StrokeCap.Round
    )

    // Calculate Arrowhead
    val angle = atan2((end.y - start.y).toDouble(), (end.x - start.x).toDouble())
    val arrowLength = max(strokeWidthPx * 3.5f, 24f)
    val arrowAngle = Math.toRadians(28.0)

    val x1 = end.x - (arrowLength * cos(angle - arrowAngle)).toFloat()
    val y1 = end.y - (arrowLength * sin(angle - arrowAngle)).toFloat()
    val x2 = end.x - (arrowLength * cos(angle + arrowAngle)).toFloat()
    val y2 = end.y - (arrowLength * sin(angle + arrowAngle)).toFloat()

    val path = Path().apply {
        moveTo(end.x, end.y)
        lineTo(x1, y1)
        lineTo(x2, y2)
        close()
    }

    drawPath(path = path, color = color, style = Fill)
}

private fun DrawScope.drawTextBadge(text: String, position: Offset, color: Color) {
    val paint = AndroidPaint().apply {
        this.color = android.graphics.Color.WHITE
        textSize = 34f
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        isAntiAlias = true
    }

    val textBounds = android.graphics.Rect()
    paint.getTextBounds(text, 0, text.length, textBounds)

    val padX = 18f
    val padY = 12f
    val badgeWidth = textBounds.width() + (padX * 2)
    val badgeHeight = textBounds.height() + (padY * 2)

    val left = position.x - (badgeWidth / 2f)
    val top = position.y - (badgeHeight / 2f)
    val badgeRect = Rect(left, top, left + badgeWidth, top + badgeHeight)

    // Draw background badge with drop shadow
    drawRoundRect(
        color = Color.Black.copy(alpha = 0.75f),
        topLeft = badgeRect.topLeft,
        size = badgeRect.size,
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
    )

    // Draw colored border indicator
    drawRoundRect(
        color = color,
        topLeft = badgeRect.topLeft,
        size = badgeRect.size,
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f),
        style = Stroke(width = 3.dp.toPx())
    )

    // Draw text
    drawContext.canvas.nativeCanvas.drawText(
        text,
        left + padX - textBounds.left,
        top + padY - textBounds.top,
        paint
    )
}

private fun DrawScope.drawCropOverlay(imageRect: Rect, cropNorm: Rect) {
    val cropPx = Rect(
        imageRect.left + (cropNorm.left * imageRect.width),
        imageRect.top + (cropNorm.top * imageRect.height),
        imageRect.left + (cropNorm.right * imageRect.width),
        imageRect.top + (cropNorm.bottom * imageRect.height)
    )

    // Dim outside crop rect
    // Top
    drawRect(Color.Black.copy(alpha = 0.6f), topLeft = imageRect.topLeft, size = Size(imageRect.width, cropPx.top - imageRect.top))
    // Bottom
    drawRect(Color.Black.copy(alpha = 0.6f), topLeft = Offset(imageRect.left, cropPx.bottom), size = Size(imageRect.width, imageRect.bottom - cropPx.bottom))
    // Left
    drawRect(Color.Black.copy(alpha = 0.6f), topLeft = Offset(imageRect.left, cropPx.top), size = Size(cropPx.left - imageRect.left, cropPx.height))
    // Right
    drawRect(Color.Black.copy(alpha = 0.6f), topLeft = Offset(cropPx.right, cropPx.top), size = Size(imageRect.right - cropPx.right, cropPx.height))

    // Crop border
    drawRect(
        color = Color.White,
        topLeft = cropPx.topLeft,
        size = cropPx.size,
        style = Stroke(width = 2.dp.toPx())
    )

    // Rule of thirds grid lines
    val gridStroke = 1.dp.toPx()
    val thirdW = cropPx.width / 3f
    val thirdH = cropPx.height / 3f
    val gridColor = Color.White.copy(alpha = 0.35f)

    drawLine(gridColor, Offset(cropPx.left + thirdW, cropPx.top), Offset(cropPx.left + thirdW, cropPx.bottom), strokeWidth = gridStroke)
    drawLine(gridColor, Offset(cropPx.left + 2 * thirdW, cropPx.top), Offset(cropPx.left + 2 * thirdW, cropPx.bottom), strokeWidth = gridStroke)
    drawLine(gridColor, Offset(cropPx.left, cropPx.top + thirdH), Offset(cropPx.right, cropPx.top + thirdH), strokeWidth = gridStroke)
    drawLine(gridColor, Offset(cropPx.left, cropPx.top + 2 * thirdH), Offset(cropPx.right, cropPx.top + 2 * thirdH), strokeWidth = gridStroke)

    // Corner Handles
    val handleLen = 22f
    val handleStroke = 4.dp.toPx()
    val handleColor = IndustrialAmber

    // Top-Left
    drawLine(handleColor, Offset(cropPx.left, cropPx.top), Offset(cropPx.left + handleLen, cropPx.top), strokeWidth = handleStroke)
    drawLine(handleColor, Offset(cropPx.left, cropPx.top), Offset(cropPx.left, cropPx.top + handleLen), strokeWidth = handleStroke)

    // Top-Right
    drawLine(handleColor, Offset(cropPx.right, cropPx.top), Offset(cropPx.right - handleLen, cropPx.top), strokeWidth = handleStroke)
    drawLine(handleColor, Offset(cropPx.right, cropPx.top), Offset(cropPx.right, cropPx.top + handleLen), strokeWidth = handleStroke)

    // Bottom-Left
    drawLine(handleColor, Offset(cropPx.left, cropPx.bottom), Offset(cropPx.left + handleLen, cropPx.bottom), strokeWidth = handleStroke)
    drawLine(handleColor, Offset(cropPx.left, cropPx.bottom), Offset(cropPx.left, cropPx.bottom - handleLen), strokeWidth = handleStroke)

    // Bottom-Right
    drawLine(handleColor, Offset(cropPx.right, cropPx.bottom), Offset(cropPx.right - handleLen, cropPx.bottom), strokeWidth = handleStroke)
    drawLine(handleColor, Offset(cropPx.right, cropPx.bottom), Offset(cropPx.right, cropPx.bottom - handleLen), strokeWidth = handleStroke)
}

// ================= COORDINATE & CROP LOGIC =================

private fun toNormalized(offset: Offset, imageRect: Rect): Offset {
    val normX = ((offset.x - imageRect.left) / imageRect.width).coerceIn(0f, 1f)
    val normY = ((offset.y - imageRect.top) / imageRect.height).coerceIn(0f, 1f)
    return Offset(normX, normY)
}

private fun fromNormalized(norm: Offset, imageRect: Rect): Offset {
    return Offset(
        imageRect.left + (norm.x * imageRect.width),
        imageRect.top + (norm.y * imageRect.height)
    )
}

private fun findCropHandle(touchOffset: Offset, imageRect: Rect, cropNorm: Rect): String {
    val cropPx = Rect(
        imageRect.left + (cropNorm.left * imageRect.width),
        imageRect.top + (cropNorm.top * imageRect.height),
        imageRect.left + (cropNorm.right * imageRect.width),
        imageRect.top + (cropNorm.bottom * imageRect.height)
    )
    val threshold = 48.dp.value

    return when {
        (touchOffset - cropPx.topLeft).getDistance() < threshold -> "TOP_LEFT"
        (touchOffset - cropPx.topRight).getDistance() < threshold -> "TOP_RIGHT"
        (touchOffset - cropPx.bottomLeft).getDistance() < threshold -> "BOTTOM_LEFT"
        (touchOffset - cropPx.bottomRight).getDistance() < threshold -> "BOTTOM_RIGHT"
        cropPx.contains(touchOffset) -> "MOVE"
        else -> "MOVE"
    }
}

private fun updateCropRect(
    current: Rect,
    handle: String,
    dx: Float,
    dy: Float,
    aspectRatio: Float?
): Rect {
    var left = current.left
    var top = current.top
    var right = current.right
    var bottom = current.bottom

    when (handle) {
        "TOP_LEFT" -> {
            left = (left + dx).coerceIn(0f, right - 0.1f)
            top = (top + dy).coerceIn(0f, bottom - 0.1f)
        }
        "TOP_RIGHT" -> {
            right = (right + dx).coerceIn(left + 0.1f, 1f)
            top = (top + dy).coerceIn(0f, bottom - 0.1f)
        }
        "BOTTOM_LEFT" -> {
            left = (left + dx).coerceIn(0f, right - 0.1f)
            bottom = (bottom + dy).coerceIn(top + 0.1f, 1f)
        }
        "BOTTOM_RIGHT" -> {
            right = (right + dx).coerceIn(left + 0.1f, 1f)
            bottom = (bottom + dy).coerceIn(top + 0.1f, 1f)
        }
        "MOVE" -> {
            val width = right - left
            val height = bottom - top
            left = (left + dx).coerceIn(0f, 1f - width)
            top = (top + dy).coerceIn(0f, 1f - height)
            right = left + width
            bottom = top + height
        }
    }

    if (aspectRatio != null && handle != "MOVE") {
        val width = right - left
        val desiredHeight = (width / aspectRatio).coerceIn(0.1f, 1f - top)
        bottom = top + desiredHeight
    }

    return Rect(left, top, right, bottom)
}

private fun applyCropToBitmap(original: Bitmap, cropNorm: Rect): Bitmap? {
    return try {
        val cropX = (cropNorm.left * original.width).toInt().coerceIn(0, original.width - 1)
        val cropY = (cropNorm.top * original.height).toInt().coerceIn(0, original.height - 1)
        val cropW = (cropNorm.width * original.width).toInt().coerceIn(1, original.width - cropX)
        val cropH = (cropNorm.height * original.height).toInt().coerceIn(1, original.height - cropY)

        Bitmap.createBitmap(original, cropX, cropY, cropW, cropH)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * Renders all annotations onto the base bitmap at full resolution and saves it to internal storage.
 */
private suspend fun renderAndSaveEditedBitmap(
    context: Context,
    baseBitmap: Bitmap,
    elements: List<PhotoElement>
): String? = withContext(Dispatchers.IO) {
    try {
        val mutableBitmap = baseBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = AndroidCanvas(mutableBitmap)
        val bmpWidth = mutableBitmap.width.toFloat()
        val bmpHeight = mutableBitmap.height.toFloat()

        elements.forEach { element ->
            when (element) {
                is PhotoElement.Arrow -> {
                    val startX = element.start.x * bmpWidth
                    val startY = element.start.y * bmpHeight
                    val endX = element.end.x * bmpWidth
                    val endY = element.end.y * bmpHeight
                    val strokePx = max(element.strokeWidth * (bmpWidth / 400f), 8f)

                    val paint = AndroidPaint().apply {
                        color = element.color.toArgb()
                        strokeWidth = strokePx
                        style = AndroidPaint.Style.STROKE
                        strokeCap = AndroidPaint.Cap.ROUND
                        isAntiAlias = true
                    }
                    canvas.drawLine(startX, startY, endX, endY, paint)

                    // Arrowhead
                    val angle = atan2((endY - startY).toDouble(), (endX - startX).toDouble())
                    val arrowLength = strokePx * 3.5f
                    val arrowAngle = Math.toRadians(28.0)

                    val x1 = endX - (arrowLength * cos(angle - arrowAngle)).toFloat()
                    val y1 = endY - (arrowLength * sin(angle - arrowAngle)).toFloat()
                    val x2 = endX - (arrowLength * cos(angle + arrowAngle)).toFloat()
                    val y2 = endY - (arrowLength * sin(angle + arrowAngle)).toFloat()

                    val headPath = AndroidPath().apply {
                        moveTo(endX, endY)
                        lineTo(x1, y1)
                        lineTo(x2, y2)
                        close()
                    }
                    val fillPaint = AndroidPaint().apply {
                        color = element.color.toArgb()
                        style = AndroidPaint.Style.FILL
                        isAntiAlias = true
                    }
                    canvas.drawPath(headPath, fillPaint)
                }

                is PhotoElement.Box -> {
                    val left = element.rect.left * bmpWidth
                    val top = element.rect.top * bmpHeight
                    val right = element.rect.right * bmpWidth
                    val bottom = element.rect.bottom * bmpHeight
                    val strokePx = max(element.strokeWidth * (bmpWidth / 400f), 8f)

                    val paint = AndroidPaint().apply {
                        color = element.color.toArgb()
                        strokeWidth = strokePx
                        style = AndroidPaint.Style.STROKE
                        strokeJoin = AndroidPaint.Join.ROUND
                        isAntiAlias = true
                    }
                    canvas.drawRect(left, top, right, bottom, paint)
                }

                is PhotoElement.TextLabel -> {
                    val centerX = element.position.x * bmpWidth
                    val centerY = element.position.y * bmpHeight
                    val fontSizePx = max(bmpWidth * 0.038f, 36f)

                    val textPaint = AndroidPaint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = fontSizePx
                        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                        isAntiAlias = true
                    }

                    val bounds = android.graphics.Rect()
                    textPaint.getTextBounds(element.text, 0, element.text.length, bounds)

                    val padX = fontSizePx * 0.45f
                    val padY = fontSizePx * 0.35f
                    val badgeW = bounds.width() + (padX * 2)
                    val badgeH = bounds.height() + (padY * 2)

                    val badgeLeft = centerX - (badgeW / 2f)
                    val badgeTop = centerY - (badgeH / 2f)
                    val badgeRect = AndroidRectF(badgeLeft, badgeTop, badgeLeft + badgeW, badgeTop + badgeH)

                    // Draw dark background pill
                    val bgPaint = AndroidPaint().apply {
                        color = android.graphics.Color.argb(200, 15, 23, 42)
                        style = AndroidPaint.Style.FILL
                        isAntiAlias = true
                    }
                    canvas.drawRoundRect(badgeRect, 16f, 16f, bgPaint)

                    // Draw border
                    val borderPaint = AndroidPaint().apply {
                        color = element.color.toArgb()
                        style = AndroidPaint.Style.STROKE
                        strokeWidth = max(fontSizePx * 0.08f, 4f)
                        isAntiAlias = true
                    }
                    canvas.drawRoundRect(badgeRect, 16f, 16f, borderPaint)

                    // Draw text
                    canvas.drawText(
                        element.text,
                        badgeLeft + padX - bounds.left,
                        badgeTop + padY - bounds.top,
                        textPaint
                    )
                }
            }
        }

        ImageUtils.saveBitmapToInternalStorage(context, mutableBitmap, "edited_inspection")
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
