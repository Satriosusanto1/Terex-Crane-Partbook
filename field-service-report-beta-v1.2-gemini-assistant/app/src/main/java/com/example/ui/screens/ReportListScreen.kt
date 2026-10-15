package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.FieldServiceReport
import com.example.ui.components.EmailDispatchDialog
import com.example.ui.components.PdfViewerDialog
import com.example.ui.components.WhatsAppDispatchDialog
import com.example.ui.theme.BentoBackground
import com.example.ui.theme.BentoLavenderBorder
import com.example.ui.theme.BentoLavenderCard
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoPrimaryContainer
import com.example.ui.theme.BentoPrimaryDark
import com.example.ui.theme.BentoTileAmber
import com.example.ui.theme.BentoTileBlue
import com.example.ui.theme.BentoTileGreen
import com.example.ui.theme.BentoTilePink
import com.example.ui.theme.BentoTilePurple
import com.example.ui.theme.IndustrialAmber
import com.example.ui.theme.IndustrialAmberDark
import com.example.ui.theme.SafetyGreen
import com.example.ui.theme.SafetyRed
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate50
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.bento
import com.example.ui.viewmodel.ReportViewModel
import com.example.util.EmailSharingHelper
import com.example.util.PdfReportGenerator
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportListScreen(
    viewModel: ReportViewModel,
    onNavigateToCreate: () -> Unit,
    onNavigateToEdit: (Long) -> Unit
) {
    val context = LocalContext.current
    val bento = MaterialTheme.bento
    val reports by viewModel.reports.collectAsStateWithLifecycle()
    val latestDraft by viewModel.latestDraft.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val previewPdfFile by viewModel.previewPdfFile.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var reportToDelete by remember { mutableStateOf<FieldServiceReport?>(null) }
    var activePreviewReport by remember { mutableStateOf<FieldServiceReport?>(null) }
    var emailDispatchPair by remember { mutableStateOf<Pair<FieldServiceReport, File>?>(null) }
    var whatsAppDispatchPair by remember { mutableStateOf<Pair<FieldServiceReport, File>?>(null) }

    val pendingCount = reports.count { !it.isSynced }
    val syncedCount = reports.count { it.isSynced }

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = bento.background,
        topBar = {
            Surface(
                color = bento.surfaceHeader,
                shadowElevation = if (bento.isDark) 0.dp else 2.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, bento.cardBorderSubtle)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = bento.tilePurple,
                            border = androidx.compose.foundation.BorderStroke(1.dp, bento.cardBorder),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_mobile_crane),
                                    contentDescription = "Mobile Crane Logo",
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Field Service Report",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = bento.textPrimary
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = bento.tilePurple
                                ) {
                                    Text(
                                        text = "PRO",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp,
                                        color = bento.tilePurpleText,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Heavy Equipment Inspection & Maintenance",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = bento.textSecondary
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Sync button
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (pendingCount > 0) bento.tileAmber else bento.tileGreen,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (pendingCount > 0) bento.tileAmberText.copy(alpha = 0.35f) else bento.tileGreenText.copy(alpha = 0.35f)
                            ),
                            modifier = Modifier
                                .clickable {
                                    if (pendingCount > 0) {
                                        viewModel.syncAllPendingReports()
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .background(
                                            if (pendingCount > 0) bento.tileAmberText else bento.tileGreenText,
                                            CircleShape
                                        )
                                )
                                Icon(
                                    imageVector = if (pendingCount > 0) Icons.Default.Sync else Icons.Default.CloudDone,
                                    contentDescription = if (pendingCount > 0) "Sinkronkan Laporan" else "Tersinkron",
                                    tint = if (pendingCount > 0) bento.tileAmberText else bento.tileGreenText,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = if (pendingCount > 0) "$pendingCount" else "OK",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (pendingCount > 0) bento.tileAmberText else bento.tileGreenText,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.initNewReport()
                    onNavigateToCreate()
                },
                containerColor = bento.primaryAccent,
                contentColor = if (bento.isDark) Color.Black else Color.White,
                shape = RoundedCornerShape(24.dp),
                elevation = androidx.compose.material3.FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 10.dp
                ),
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .testTag("create_new_report_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Buat Laporan Baru", modifier = Modifier.size(20.dp))
                    Text("Buat Laporan", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(bento.background)
        ) {
            // Search Bento Container
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = bento.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, bento.cardBorder),
                shadowElevation = if (bento.isDark) 0.dp else 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    placeholder = { Text("Cari No. Unit, Customer, Serial...", color = bento.textMuted, fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Cari", tint = bento.primaryAccent)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Hapus pencarian", tint = bento.textMuted)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(22.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = bento.textPrimary,
                        unfocusedTextColor = bento.textPrimary,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = bento.primaryAccent
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(color = bento.textPrimary, fontSize = 14.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("report_search_input")
                )
            }

            // Bento Quick Stats Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Stat 1: Total Reports
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    color = bento.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, bento.cardBorder),
                    shadowElevation = if (bento.isDark) 0.dp else 1.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = bento.tilePurple,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("📋", fontSize = 18.sp)
                            }
                        }
                        Column {
                            Text(
                                text = "TOTAL LAPORAN",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = bento.textMuted
                            )
                            Text(
                                text = "${reports.size} Unit",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = bento.textPrimary
                            )
                        }
                    }
                }

                // Stat 2: Local Data Sync Status
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    color = if (pendingCount > 0) bento.tileAmber else bento.tileGreen,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (pendingCount > 0) bento.tileAmberText.copy(alpha = 0.35f) else bento.tileGreenText.copy(alpha = 0.35f)
                    ),
                    shadowElevation = if (bento.isDark) 0.dp else 1.dp,
                    onClick = {
                        if (pendingCount > 0) {
                            viewModel.syncAllPendingReports()
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = bento.surface,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (pendingCount > 0) Icons.Default.CloudQueue else Icons.Default.CloudDone,
                                    contentDescription = null,
                                    tint = if (pendingCount > 0) bento.tileAmberText else bento.tileGreenText,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "STATUS SINKRON",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (pendingCount > 0) bento.tileAmberText else bento.tileGreenText
                            )
                            Text(
                                text = if (pendingCount > 0) "$pendingCount Pending" else "All Synced ($syncedCount)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (pendingCount > 0) bento.tileAmberText else bento.tileGreenText
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Auto-Saved Draft Recovery Banner
            if (latestDraft != null && searchQuery.isEmpty()) {
                val draft = latestDraft!!
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = bento.tileAmber,
                    border = androidx.compose.foundation.BorderStroke(1.dp, bento.tileAmberText.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .testTag("draft_recovery_card")
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(bento.tileAmberText, CircleShape)
                                )
                                Text(
                                    text = "DRAFT OTOMATIS TERSIMPAN",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = bento.tileAmberText,
                                    fontSize = 11.sp
                                )
                            }
                            Text(
                                text = "Auto-Saved",
                                style = MaterialTheme.typography.labelSmall,
                                color = bento.tileAmberText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (draft.unitNo.isNotBlank()) "Unit: ${draft.unitNo} (${draft.unitBrand} ${draft.type})".trim() else "Laporan Baru Sedang Diisi",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = bento.textPrimary
                                )
                                if (draft.jobDescription.isNotBlank()) {
                                    Text(
                                        text = draft.jobDescription,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = bento.textSecondary,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = { onNavigateToEdit(draft.id) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = bento.tileAmberText,
                                        contentColor = if (bento.isDark) Color.Black else Color.White
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.height(36.dp).testTag("btn_resume_draft")
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Lanjutkan", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            if (reports.isEmpty()) {
                // Empty State
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(28.dp),
                        color = bento.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, bento.cardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = bento.tilePurple,
                                modifier = Modifier.size(72.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_mobile_crane),
                                        contentDescription = "Mobile Crane",
                                        tint = Color.Unspecified,
                                        modifier = Modifier.size(48.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (searchQuery.isNotEmpty()) "Laporan tidak ditemukan" else "Belum Ada Laporan Inspeksi",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = bento.textPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (searchQuery.isNotEmpty()) "Coba kata kunci lain untuk mencari laporan" else "Tekan tombol '+ Buat Laporan' untuk membuat field service report dengan gaya Bento Grid.",
                                style = MaterialTheme.typography.bodySmall,
                                color = bento.textSecondary,
                                modifier = Modifier.padding(horizontal = 16.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 92.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(reports, key = { it.id }) { report ->
                        ReportItemCard(
                            report = report,
                            onEdit = { onNavigateToEdit(report.id) },
                            onPreviewPdf = {
                                activePreviewReport = report
                                val file = PdfReportGenerator.generateReportPdf(context, report)
                                viewModel.updateCurrentReport { report }
                                viewModel.generatePdfAndPreview(context)
                            },
                            onSendWhatsApp = {
                                val file = PdfReportGenerator.generateReportPdf(context, report)
                                whatsAppDispatchPair = Pair(report, file)
                            },
                            onSendEmail = {
                                val file = PdfReportGenerator.generateReportPdf(context, report)
                                emailDispatchPair = Pair(report, file)
                            },
                            onDelete = { reportToDelete = report },
                            onToggleSync = { viewModel.toggleReportSyncStatus(report) }
                        )
                    }
                }
            }
        }
    }

    // WhatsApp Group Dispatch Dialog
    whatsAppDispatchPair?.let { pair ->
        WhatsAppDispatchDialog(
            report = pair.first,
            pdfFile = pair.second,
            onDismissRequest = { whatsAppDispatchPair = null }
        )
    }

    // Email Dispatch Dialog
    emailDispatchPair?.let { pair ->
        EmailDispatchDialog(
            report = pair.first,
            pdfFile = pair.second,
            onDismissRequest = { emailDispatchPair = null }
        )
    }

    // Delete Confirmation Dialog
    reportToDelete?.let { report ->
        AlertDialog(
            onDismissRequest = { reportToDelete = null },
            shape = RoundedCornerShape(24.dp),
            containerColor = bento.surface,
            title = { Text("Hapus Laporan?", fontWeight = FontWeight.Bold, color = bento.textPrimary) },
            text = { Text("Apakah Anda yakin ingin menghapus laporan unit ${report.unitNo} (${report.customerName})?", color = bento.textSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteReport(report)
                        reportToDelete = null
                    },
                    modifier = Modifier.testTag("confirm_delete_btn")
                ) {
                    Text("Hapus", color = SafetyRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { reportToDelete = null }) {
                    Text("Batal", color = bento.textSecondary)
                }
            }
        )
    }

    // PDF Preview Dialog
    previewPdfFile?.let { file ->
        PdfViewerDialog(
            pdfFile = file,
            report = activePreviewReport ?: FieldServiceReport(),
            onDismiss = { viewModel.dismissPdfPreview() }
        )
    }
}


@Composable
fun ReportItemCard(
    report: FieldServiceReport,
    onEdit: () -> Unit,
    onPreviewPdf: () -> Unit,
    onSendWhatsApp: () -> Unit,
    onSendEmail: () -> Unit,
    onDelete: () -> Unit,
    onToggleSync: () -> Unit = {}
) {
    val bento = MaterialTheme.bento
    val photosCount = listOfNotNull(report.hourmeterPhotoUri, report.defect1PhotoUri, report.defect2PhotoUri).size
    val signaturesCount = listOfNotNull(report.preparedBySignatureUri, report.acknowledgeBySignatureUri, report.clientAcknowledgeSignatureUri).size

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() }
            .testTag("report_item_${report.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = bento.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (bento.isDark) 0.dp else 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, bento.cardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Bento Header Tile Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = bento.tilePurple,
                        border = androidx.compose.foundation.BorderStroke(1.dp, bento.cardBorder),
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_mobile_crane),
                                contentDescription = "Mobile Crane",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "INFORMASI UNIT",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = bento.tilePurpleText,
                            fontSize = 9.sp
                        )
                        Text(
                            text = report.unitNo.ifBlank { "UNIT UNTITLED" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = bento.textPrimary
                        )
                        Text(
                            text = "${report.unitBrand} ${report.type}".trim().ifBlank { "Alat Berat" },
                            style = MaterialTheme.typography.bodySmall,
                            color = bento.textSecondary
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Small Status Indicator: Synced vs Pending Local Data
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (report.isSynced) bento.tileGreen else bento.tileAmber,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (report.isSynced) bento.tileGreenText.copy(alpha = 0.35f) else bento.tileAmberText.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier
                            .clickable { onToggleSync() }
                            .testTag("report_sync_indicator_${report.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(
                                        if (report.isSynced) bento.tileGreenText else bento.tileAmberText,
                                        CircleShape
                                    )
                            )
                            Icon(
                                imageVector = if (report.isSynced) Icons.Default.CloudDone else Icons.Default.CloudQueue,
                                contentDescription = if (report.isSynced) "Data Tersinkron" else "Pending Sinkronisasi",
                                tint = if (report.isSynced) bento.tileGreenText else bento.tileAmberText,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = if (report.isSynced) "SYNCED" else "PENDING",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (report.isSynced) bento.tileGreenText else bento.tileAmberText,
                                fontSize = 9.sp
                            )
                        }
                    }

                    // Draft or Selesai status badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (report.isDraft) bento.tileAmber else bento.tileGreen,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (report.isDraft) bento.tileAmberText.copy(alpha = 0.25f) else bento.tileGreenText.copy(alpha = 0.25f)
                        )
                    ) {
                        Text(
                            text = if (report.isDraft) "DRAFT" else "SELESAI",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (report.isDraft) bento.tileAmberText else bento.tileGreenText,
                            fontSize = 9.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bento Sub-tile: Customer & Findings preview
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = bento.subtleSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, bento.cardBorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = report.customerName.ifBlank { "Customer Belum Diisi" },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = bento.textPrimary
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = bento.surface,
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, bento.cardBorder)
                        ) {
                            Text(
                                text = report.reportDate.ifBlank { "-" },
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = bento.textSecondary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (report.projectSite.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = bento.primaryAccent, modifier = Modifier.size(13.dp))
                            Text(
                                text = report.projectSite,
                                style = MaterialTheme.typography.bodySmall,
                                color = bento.textSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    if (report.jobDescription.isNotBlank()) {
                        Text(
                            text = report.jobDescription,
                            style = MaterialTheme.typography.bodySmall,
                            color = bento.textSecondary,
                            maxLines = 2,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bento mini metric tiles (3 balanced columns)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Photos Badge
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = if (photosCount > 0) bento.tilePurple else bento.subtleSurface,
                    border = androidx.compose.foundation.BorderStroke(
                        0.5.dp,
                        if (photosCount > 0) bento.cardBorder else Color.Transparent
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = null,
                            tint = if (photosCount > 0) bento.primaryAccent else bento.textMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$photosCount/3 Foto",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (photosCount > 0) bento.tilePurpleText else bento.textMuted,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }

                // Signatures Badge
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = if (signaturesCount >= 2) bento.tileGreen else if (signaturesCount == 1) bento.tileAmber else bento.subtleSurface,
                    border = androidx.compose.foundation.BorderStroke(
                        0.5.dp,
                        if (signaturesCount >= 2) bento.tileGreenText.copy(alpha = 0.3f) else if (signaturesCount == 1) bento.tileAmberText.copy(alpha = 0.25f) else Color.Transparent
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Draw,
                            contentDescription = null,
                            tint = if (signaturesCount >= 2) bento.tileGreenText else if (signaturesCount == 1) bento.tileAmberText else bento.textMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$signaturesCount/3 TTd",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (signaturesCount >= 2) bento.tileGreenText else if (signaturesCount == 1) bento.tileAmberText else bento.textMuted,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }

                // HM Badge
                val hasHm = report.superstructureHourmeter.isNotBlank()
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = if (hasHm) bento.tileAmber else bento.subtleSurface,
                    border = androidx.compose.foundation.BorderStroke(
                        0.5.dp,
                        if (hasHm) bento.tileAmberText.copy(alpha = 0.25f) else Color.Transparent
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("⏱️", fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (hasHm) "${report.superstructureHourmeter} HM" else "- HM",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (hasHm) bento.tileAmberText else bento.textMuted,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = bento.cardBorderSubtle)
            Spacer(modifier = Modifier.height(10.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        onClick = onPreviewPdf,
                        shape = RoundedCornerShape(12.dp),
                        color = bento.tilePurple,
                        border = androidx.compose.foundation.BorderStroke(1.dp, bento.cardBorder),
                        modifier = Modifier.testTag("preview_pdf_${report.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = bento.primaryAccent, modifier = Modifier.size(15.dp))
                            Text("PDF", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = bento.tilePurpleText, fontSize = 11.sp)
                        }
                    }

                    Surface(
                        onClick = onSendWhatsApp,
                        shape = RoundedCornerShape(12.dp),
                        color = bento.tileGreen,
                        border = androidx.compose.foundation.BorderStroke(1.dp, bento.tileGreenText.copy(alpha = 0.3f)),
                        modifier = Modifier.testTag("whatsapp_report_${report.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_whatsapp),
                                contentDescription = "WhatsApp",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(15.dp)
                            )
                            Text("WA", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = bento.tileGreenText, fontSize = 11.sp)
                        }
                    }

                    Surface(
                        onClick = onSendEmail,
                        shape = RoundedCornerShape(12.dp),
                        color = bento.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, bento.cardBorder),
                        modifier = Modifier.testTag("email_report_${report.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null, tint = bento.textSecondary, modifier = Modifier.size(14.dp))
                            Text("Email", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = bento.textPrimary, fontSize = 11.sp)
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("edit_report_${report.id}")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Laporan", tint = bento.primaryAccent, modifier = Modifier.size(18.dp))
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("delete_report_${report.id}")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus Laporan", tint = SafetyRed, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

