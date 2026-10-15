package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.model.FieldServiceReport
import com.example.ui.theme.BentoBackground
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoPrimaryDark
import com.example.ui.theme.BentoTilePurple
import com.example.ui.theme.IndustrialAmber
import com.example.ui.theme.IndustrialAmberDark
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.util.EmailSharingHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.max
import kotlin.math.min

@Composable
fun PdfViewerDialog(
    pdfFile: File,
    report: FieldServiceReport,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val renderedPages = remember { mutableStateListOf<Bitmap>() }
    var isLoading by remember { mutableStateOf(true) }

    // Zoom & Pan State
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }

    // Email and WhatsApp dispatch dialog states
    var showEmailDispatchDialog by remember { mutableStateOf(false) }
    var showWhatsAppDispatchDialog by remember { mutableStateOf(false) }

    // Reset zoom helper
    fun resetZoom() {
        zoomScale = 1.0f
        panOffsetX = 0f
        panOffsetY = 0f
    }

    LaunchedEffect(pdfFile) {
        withContext(Dispatchers.IO) {
            try {
                val fileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
                val renderer = PdfRenderer(fileDescriptor)
                val bitmaps = mutableListOf<Bitmap>()
                for (i in 0 until renderer.pageCount) {
                    val page = renderer.openPage(i)
                    // High resolution 2.5x render for crisp reading when zoomed in
                    val width = (page.width * 2.5f).toInt()
                    val height = (page.height * 2.5f).toInt()
                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    bitmap.eraseColor(android.graphics.Color.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    bitmaps.add(bitmap)
                    page.close()
                }
                renderer.close()
                fileDescriptor.close()

                withContext(Dispatchers.Main) {
                    renderedPages.clear()
                    renderedPages.addAll(bitmaps)
                    isLoading = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    isLoading = false
                }
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.97f)
                .fillMaxHeight(0.94f)
                .testTag("pdf_viewer_dialog"),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Top Navigation / Header
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
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.PictureAsPdf,
                                    contentDescription = null,
                                    tint = BentoPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Preview Dokumen PDF",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                            Text(
                                text = "Unit: ${report.unitNo.ifBlank { "N/A" }} • ${renderedPages.size} Halaman (A4)",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate500
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_pdf_preview_btn")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Slate500)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // PDF Pages List Container with Interactive Zoom & Pan Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(BentoBackground, RoundedCornerShape(18.dp))
                        .border(1.dp, Slate200, RoundedCornerShape(18.dp))
                        .clip(RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(color = BentoPrimary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Menyiapkan preview PDF...", color = Slate500, style = MaterialTheme.typography.bodySmall)
                        }
                    } else if (renderedPages.isEmpty()) {
                        Text("Gagal memuat preview dokumen PDF", color = Slate500)
                    } else {
                        BoxWithConstraints(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onDoubleTap = {
                                            if (zoomScale > 1.05f) {
                                                resetZoom()
                                            } else {
                                                zoomScale = 2.0f
                                                panOffsetX = 0f
                                                panOffsetY = 0f
                                            }
                                        }
                                    )
                                }
                                .pointerInput(Unit) {
                                    detectTransformGestures { _, pan, zoom, _ ->
                                        val newScale = (zoomScale * zoom).coerceIn(0.8f, 3.5f)
                                        zoomScale = newScale
                                        if (newScale > 1.0f) {
                                            val maxPanX = 600f * (newScale - 1f)
                                            val maxPanY = 1200f * (newScale - 1f)
                                            panOffsetX = (panOffsetX + pan.x).coerceIn(-maxPanX, maxPanX)
                                            panOffsetY = (panOffsetY + pan.y).coerceIn(-maxPanY, maxPanY)
                                        } else {
                                            panOffsetX = 0f
                                            panOffsetY = 0f
                                        }
                                    }
                                }
                        ) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        scaleX = zoomScale
                                        scaleY = zoomScale
                                        translationX = panOffsetX
                                        translationY = panOffsetY
                                    }
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                itemsIndexed(renderedPages) { index, pageBitmap ->
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "Halaman ${index + 1} dari ${renderedPages.size}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Slate500,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(bottom = 6.dp)
                                        )
                                        Card(
                                            shape = RoundedCornerShape(8.dp),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                            border = androidx.compose.foundation.BorderStroke(0.5.dp, Slate200)
                                        ) {
                                            Image(
                                                bitmap = pageBitmap.asImageBitmap(),
                                                contentDescription = "PDF Page ${index + 1}",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .aspectRatio(595f / 842f)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Floating Zoom Controls Bar (Zoom In, Zoom Out, Reset, Scale Indicator)
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = Color(0xFF1E1B2E).copy(alpha = 0.92f),
                            shadowElevation = 8.dp,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 12.dp)
                                .testTag("pdf_zoom_toolbar")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // Zoom Out (-) Button
                                IconButton(
                                    onClick = {
                                        val newScale = max(0.8f, zoomScale - 0.25f)
                                        zoomScale = newScale
                                        if (newScale <= 1.0f) {
                                            panOffsetX = 0f
                                            panOffsetY = 0f
                                        }
                                    },
                                    enabled = zoomScale > 0.8f,
                                    modifier = Modifier.size(34.dp).testTag("pdf_zoom_out_btn"),
                                    colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Remove,
                                        contentDescription = "Zoom Out",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                // Zoom Percentage Badge (Click to reset to 100%)
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White.copy(alpha = 0.15f),
                                    modifier = Modifier
                                        .clickable { resetZoom() }
                                        .testTag("pdf_zoom_percentage_btn")
                                ) {
                                    Text(
                                        text = "${(zoomScale * 100).toInt()}%",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (zoomScale != 1.0f) IndustrialAmber else Color.White,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }

                                // Zoom In (+) Button
                                IconButton(
                                    onClick = {
                                        val newScale = min(3.5f, zoomScale + 0.25f)
                                        zoomScale = newScale
                                    },
                                    enabled = zoomScale < 3.5f,
                                    modifier = Modifier.size(34.dp).testTag("pdf_zoom_in_btn"),
                                    colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Zoom In",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                // Reset to 100% button
                                if (zoomScale != 1.0f || panOffsetX != 0f || panOffsetY != 0f) {
                                    IconButton(
                                        onClick = { resetZoom() },
                                        modifier = Modifier.size(34.dp).testTag("pdf_zoom_reset_btn"),
                                        colors = IconButtonDefaults.iconButtonColors(contentColor = IndustrialAmber)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.RestartAlt,
                                            contentDescription = "Reset Zoom",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Export & Send Actions
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                showWhatsAppDispatchDialog = true
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF25D366),
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("whatsapp_pdf_btn")
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_whatsapp),
                                contentDescription = "WhatsApp",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share WhatsApp", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Button(
                            onClick = {
                                showEmailDispatchDialog = true
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("email_pdf_btn")
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Kirim Email", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                EmailSharingHelper.openPdfFile(context, pdfFile)
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("open_external_pdf_btn")
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Buka PDF", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                EmailSharingHelper.shareReportPdf(context, pdfFile, report)
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("share_pdf_btn")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Lainnya / Share", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    if (showEmailDispatchDialog) {
        EmailDispatchDialog(
            report = report,
            pdfFile = pdfFile,
            onDismissRequest = { showEmailDispatchDialog = false }
        )
    }

    if (showWhatsAppDispatchDialog) {
        WhatsAppDispatchDialog(
            report = report,
            pdfFile = pdfFile,
            onDismissRequest = { showWhatsAppDispatchDialog = false }
        )
    }
}
