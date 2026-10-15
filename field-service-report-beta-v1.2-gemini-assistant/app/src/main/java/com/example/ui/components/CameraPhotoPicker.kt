package com.example.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoPrimaryDark
import com.example.ui.theme.IndustrialAmber
import com.example.ui.theme.IndustrialAmberDark
import com.example.ui.theme.SafetyRed
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.appTextFieldColors
import com.example.ui.theme.bento
import com.example.util.ImageUtils
import java.io.File

@Composable
fun InspectionPhotoCard(
    title: String,
    subtitle: String,
    photoUri: String?,
    findingsText: String = "",
    onPhotoChanged: (newUri: String?) -> Unit,
    onFindingsChanged: ((newText: String) -> Unit)? = null,
    showFindingsInput: Boolean = true,
    tagPrefix: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val bento = MaterialTheme.bento
    var showCameraXDialog by remember { mutableStateOf(false) }
    var showPhotoEditorDialog by remember { mutableStateOf(false) }
    var showFocusedPopup by remember { mutableStateOf(false) }

    // Legacy fallback gallery picker (works universally on all Android versions)
    val legacyGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedUri = ImageUtils.saveUriToInternalStorage(context, uri, tagPrefix)
            if (savedUri != null) {
                onPhotoChanged(savedUri)
            }
        }
    }

    // Modern Photo Picker launcher (Android 13+ with backward compat)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedUri = ImageUtils.saveUriToInternalStorage(context, uri, tagPrefix)
            if (savedUri != null) {
                onPhotoChanged(savedUri)
            }
        }
    }

    val openGallerySafely = {
        try {
            galleryLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        } catch (e: Exception) {
            // Fallback for older Android versions or devices without PhotoPicker
            legacyGalleryLauncher.launch("image/*")
        }
    }

    // Photo Editor & Annotation Dialog
    if (showPhotoEditorDialog && !photoUri.isNullOrBlank()) {
        InspectionPhotoEditorDialog(
            photoUriString = photoUri,
            title = "Edit & Anotasi: $title",
            onDismissRequest = { showPhotoEditorDialog = false },
            onPhotoSaved = { newUriString ->
                onPhotoChanged(newUriString)
            }
        )
    }

    // CameraX Capture Dialog
    if (showCameraXDialog) {
        CameraXInspectionCaptureDialog(
            inspectionTitle = title,
            tagPrefix = tagPrefix,
            onDismissRequest = { showCameraXDialog = false },
            onPhotoCaptured = { savedUri ->
                showCameraXDialog = false
                onPhotoChanged(savedUri)
            }
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("${tagPrefix}_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = bento.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, bento.cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = if (bento.isDark) 0.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header: Title & Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = bento.textPrimary
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = bento.textSecondary
                    )
                }

                if (!photoUri.isNullOrBlank()) {
                    IconButton(
                        onClick = { onPhotoChanged(null) },
                        colors = IconButtonDefaults.iconButtonColors(contentColor = SafetyRed),
                        modifier = Modifier.testTag("${tagPrefix}_delete_btn")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus Foto")
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Photo Preview or Capture Area
            if (!photoUri.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, bento.cardBorderSubtle, RoundedCornerShape(16.dp))
                ) {
                    AsyncImage(
                        model = photoUri,
                        contentDescription = title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Re-take overlay buttons
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(10.dp)
                            .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = { showPhotoEditorDialog = true },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                Icons.Default.Draw,
                                contentDescription = "Edit Foto (Panah, Kotak, Teks, Crop)",
                                tint = IndustrialAmber,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = { showCameraXDialog = true },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = "Ambil Ulang Kamera (CameraX)",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = { openGallerySafely() },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                Icons.Default.AddPhotoAlternate,
                                contentDescription = "Pilih Ulang Galeri",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Edit & Anotasi Button under Photo
                FilledTonalButton(
                    onClick = { showPhotoEditorDialog = true },
                    shape = RoundedCornerShape(14.dp),
                    colors = androidx.compose.material3.ButtonDefaults.filledTonalButtonColors(
                        containerColor = bento.tilePurple,
                        contentColor = bento.tilePurpleText
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("${tagPrefix}_edit_photo_btn")
                ) {
                    Icon(
                        Icons.Default.Draw,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = bento.tilePurpleText
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Edit Foto (Crop, Panah, Kotak & Teks)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = bento.tilePurpleText
                    )
                }
            } else {
                // Empty photo prompt with 2 action buttons
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(bento.tilePurple, RoundedCornerShape(16.dp))
                        .border(1.5.dp, bento.cardBorder, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = bento.primaryAccent,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tambahkan Foto $title",
                            style = MaterialTheme.typography.bodyMedium,
                            color = bento.textPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FilledTonalButton(
                                onClick = { showCameraXDialog = true },
                                shape = RoundedCornerShape(14.dp),
                                colors = androidx.compose.material3.ButtonDefaults.filledTonalButtonColors(
                                    containerColor = bento.primaryAccent,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier.testTag("${tagPrefix}_camera_btn")
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Kamera HD", fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            OutlinedButton(
                                onClick = { openGallerySafely() },
                                shape = RoundedCornerShape(14.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, bento.cardBorder),
                                modifier = Modifier.testTag("${tagPrefix}_gallery_btn")
                            ) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp), tint = bento.primaryAccent)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Galeri", fontWeight = FontWeight.Bold, color = bento.textPrimary)
                            }
                        }
                    }
                }
            }

            // Findings and Recommendations Text (Conditional)
            if (showFindingsInput && onFindingsChanged != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = findingsText,
                        onValueChange = onFindingsChanged,
                        readOnly = true,
                        shape = RoundedCornerShape(16.dp),
                        label = { Text("Findings and Recommendations") },
                        placeholder = { Text("Ketuk 1x atau 2x untuk mengetik temuan & rekomendasi...") },
                        trailingIcon = {
                            IconButton(
                                onClick = { showFocusedPopup = true },
                                modifier = Modifier.testTag("${tagPrefix}_popup_findings_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EditNote,
                                    contentDescription = "Buka di Pop-up Nyaman (Klik 1x / 2x)",
                                    tint = Color(0xFF6750A4),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        },
                        colors = appTextFieldColors(),
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("${tagPrefix}_findings_input"),
                        minLines = 3,
                        maxLines = 6
                    )

                    // Transparent overlay for 1x and 2x tap triggers
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .pointerInput(findingsText) {
                                detectTapGestures(
                                    onTap = { showFocusedPopup = true },
                                    onDoubleTap = { showFocusedPopup = true }
                                )
                            }
                    )
                }
            }
        }
    }

    if (showFocusedPopup && onFindingsChanged != null) {
        FocusedInputDialog(
            config = PopupFieldConfig(
                title = "Temuan $title",
                subtitle = "Tuliskan deskripsi temuan kerusakan, kondisi komponen, atau rekomendasi perbaikan dalam pop-up bebas halangan keyboard.",
                initialValue = findingsText,
                placeholder = "Deskripsikan temuan inspeksi secara detail...",
                isMultiline = true,
                helperChips = listOf(
                    "Ditemukan kebocoran oli hidrolik",
                    "Kondisi komponen aus / crack",
                    "Kabel kelistrikan terkelupas / short",
                    "Tekanan hidrolik abnormal",
                    "Perlu penggantian seal kit",
                    "Perlu kalibrasi sensor LMI"
                ),
                onConfirm = { onFindingsChanged(it) }
            ),
            onDismissRequest = { showFocusedPopup = false }
        )
    }
}
