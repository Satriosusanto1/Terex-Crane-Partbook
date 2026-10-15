package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.data.model.UnitDatabase
import com.example.data.model.UnitCatalogItem
import com.example.ui.components.FocusedInputDialog
import com.example.ui.components.AiAssistantDialog
import com.example.ui.components.PopupFieldConfig
import com.example.ui.components.SmartFocusTextField
import android.app.DatePickerDialog
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.FieldServiceReport
import com.example.data.model.PartItem
import com.example.ui.components.InspectionPhotoCard
import com.example.ui.components.PdfViewerDialog
import com.example.ui.components.SignaturePadDialog
import com.example.ui.components.WhatsAppDispatchDialog
import kotlinx.coroutines.launch
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
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.appTextFieldColors
import com.example.ui.theme.bento
import com.example.ui.viewmodel.ReportViewModel
import com.example.util.EmailSharingHelper
import com.example.util.PdfReportGenerator
import com.example.util.TechnicalTranslator
import java.io.File

data class StepNodeData(
    val stepIndex: Int,
    val shortLabel: String,
    val isDone: Boolean,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportEditorScreen(
    viewModel: ReportViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val bento = MaterialTheme.bento

    val report by viewModel.currentReport.collectAsStateWithLifecycle()
    val previewPdfFile by viewModel.previewPdfFile.collectAsStateWithLifecycle()
    val isGeneratingPdf by viewModel.isGeneratingPdf.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()
    val autoSaveStatus by viewModel.autoSaveStatus.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val lifecycleOwner = LocalLifecycleOwner.current

    // Auto-save on lifecycle pause/stop (app minimized or closed) and component dispose
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                viewModel.saveDraftImmediate()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.saveDraftImmediate()
        }
    }

    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAiAssistant by remember { mutableStateOf(false) }

    // Active signature pad dialog state
    var signatureDialogTarget by remember { mutableStateOf<String?>(null) }
    
    // Active focused pop-up input dialog state (prevents keyboard obstruction)
    var activePopupField by remember { mutableStateOf<PopupFieldConfig?>(null) }

    // WhatsApp Group Dispatch Dialog state
    var whatsAppDispatchPair by remember { mutableStateOf<Pair<FieldServiceReport, File>?>(null) }

    // Validation checks for sequential 5-step progression
    val isStep1Done = report.customerName.isNotBlank() &&
            report.projectSite.isNotBlank() &&
            report.unitNo.isNotBlank() &&
            report.jobDescription.isNotBlank()

    val isStep2Done = !report.hourmeterPhotoUri.isNullOrBlank() ||
            report.history.isNotBlank() ||
            !report.defect1PhotoUri.isNullOrBlank() ||
            report.defect1Findings.isNotBlank()

    // Step 3 (Spare parts) is optional / always valid once reached
    val isStep3Done = true

    val isStep4Done = report.completedAction.isNotBlank()

    val isStep5Done = report.preparedByName.isNotBlank() &&
            !report.preparedBySignatureUri.isNullOrBlank()

    val stepTitles = listOf(
        "Info Unit & Customer",
        "Foto & Temuan",
        "Spare Parts",
        "Tindakan & Kesimpulan",
        "Tanda Tangan & Email"
    )

    fun tryNavigateToTab(targetTab: Int) {
        if (targetTab <= selectedTab) {
            // Always allow going back to previous steps
            selectedTab = targetTab
            return
        }

        // Validate sequentially
        if (targetTab >= 1 && !isStep1Done) {
            val missing = mutableListOf<String>()
            if (report.customerName.isBlank()) missing.add("Customer Name")
            if (report.projectSite.isBlank()) missing.add("Project Site")
            if (report.unitNo.isBlank()) missing.add("Unit No.")
            if (report.jobDescription.isBlank()) missing.add("Job Description")
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Harap lengkapi Langkah 1 (Info Unit): ${missing.joinToString(", ")}")
            }
            return
        }

        if (targetTab >= 2 && !isStep2Done) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Harap lengkapi Langkah 2 (Foto Hourmeter/Temuan) terlebih dahulu.")
            }
            return
        }

        if (targetTab >= 4 && !isStep4Done) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Harap isi Completed Action / Tindakan di Langkah 4 terlebih dahulu.")
            }
            return
        }

        selectedTab = targetTab
    }

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
                color = bento.surface,
                shadowElevation = if (bento.isDark) 0.dp else 1.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, bento.cardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        IconButton(
                            onClick = {
                                viewModel.saveDraftImmediate()
                                onNavigateBack()
                            },
                            modifier = Modifier.testTag("editor_back_btn")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = bento.textPrimary)
                        }

                        Column {
                            Text(
                                text = if (report.id == 0L) "Buat Laporan Baru" else "Edit Laporan FSR",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = bento.textPrimary
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(SafetyGreen, CircleShape)
                                )
                                Text(
                                    text = autoSaveStatus,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = bento.textSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { showAiAssistant = true },
                            modifier = Modifier.testTag("editor_ai_assistant_btn")
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "AI Assistant", tint = bento.primaryAccent)
                        }

                        IconButton(
                            onClick = {
                                viewModel.saveReport(isDraft = true)
                            },
                            modifier = Modifier.testTag("editor_quick_save_btn")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = "Simpan", tint = bento.primaryAccent)
                        }

                        IconButton(
                            onClick = {
                                viewModel.generatePdfAndPreview(context)
                            },
                            modifier = Modifier.testTag("editor_quick_pdf_btn")
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = "Preview PDF", tint = bento.primaryAccent)
                        }
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                color = bento.surface,
                shadowElevation = if (bento.isDark) 0.dp else 8.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, bento.cardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.saveReport(isDraft = false)
                        },
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, bento.cardBorder),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("editor_bottom_save_btn")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp), tint = bento.primaryAccent)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Simpan", fontWeight = FontWeight.Bold, color = bento.textPrimary)
                    }

                    FilledTonalButton(
                        onClick = {
                            viewModel.generatePdfAndPreview(context)
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = bento.tilePurple,
                            contentColor = bento.tilePurpleText
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("editor_bottom_pdf_btn")
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PDF", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            viewModel.saveReport(isDraft = false) {
                                viewModel.generatePdfAndPreview(context) { file ->
                                    EmailSharingHelper.sendReportByEmail(context, viewModel.currentReport.value, file)
                                }
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = bento.primaryAccent, contentColor = Color.White),
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("editor_bottom_email_btn")
                    ) {
                        Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Kirim Email", fontWeight = FontWeight.Bold)
                    }
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
            // Visual Step-by-Step Progress Indicator (5-Step Stepper Header)
            Surface(
                color = bento.surface,
                shadowElevation = if (bento.isDark) 0.dp else 3.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, bento.cardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    // Header text & status chip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "LANGKAH ${selectedTab + 1} DARI 5",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = bento.tilePurpleText,
                                fontSize = 11.sp,
                                letterSpacing = 0.6.sp
                            )
                            Text(
                                text = stepTitles[selectedTab],
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = bento.textPrimary,
                                fontSize = 14.sp
                            )
                        }

                        val stepStatus = when (selectedTab) {
                            0 -> if (isStep1Done) "Langkah 1 Selesai ✓" else "Wajib Dilengkapi"
                            1 -> if (isStep2Done) "Langkah 2 Selesai ✓" else "Perlu Foto/Temuan"
                            2 -> if (viewModel.currentPartsList.isNotEmpty()) "${viewModel.currentPartsList.size} Part Ditambahkan ✓" else "Opsional / Dapat Dilewati"
                            3 -> if (isStep4Done) "Langkah 4 Selesai ✓" else "Wajib Diisi"
                            else -> if (isStep5Done) "Siap Dikirim ✓" else "Perlu Tanda Tangan"
                        }
                        val isCurrentStepDone = when (selectedTab) {
                            0 -> isStep1Done
                            1 -> isStep2Done
                            2 -> true
                            3 -> isStep4Done
                            else -> isStep5Done
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isCurrentStepDone) bento.tileGreen else bento.tileAmber,
                            border = androidx.compose.foundation.BorderStroke(
                                0.5.dp,
                                if (isCurrentStepDone) bento.tileGreenText.copy(alpha = 0.3f) else bento.tileAmberText.copy(alpha = 0.3f)
                            )
                        ) {
                            Text(
                                text = stepStatus,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrentStepDone) bento.tileGreenText else bento.tileAmberText,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Animated Progress Bar
                    val animatedProgress by animateFloatAsState(
                        targetValue = (selectedTab + 1) / 5f,
                        animationSpec = tween(durationMillis = 350),
                        label = "StepProgressAnimation"
                    )
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = bento.primaryAccent,
                        trackColor = bento.cardBorderSubtle,
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 5-Step Connected Nodes Rail with centered track line
                    val stepItems = listOf(
                        StepNodeData(0, "1. Unit", isStep1Done, Icons.Default.DirectionsCar),
                        StepNodeData(1, "2. Foto", isStep2Done, Icons.Default.CameraAlt),
                        StepNodeData(2, "3. Parts", isStep3Done, Icons.Default.Build),
                        StepNodeData(3, "4. Tindakan", isStep4Done, Icons.Default.TaskAlt),
                        StepNodeData(4, "5. TTd", isStep5Done, Icons.Default.Draw)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                    ) {
                        // Background Connecting Track Line (aligned through center of 34.dp circles)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                                .align(Alignment.TopCenter)
                                .padding(top = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (i in 0 until 4) {
                                val isTrackActive = i < selectedTab
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(2.5.dp)
                                        .background(
                                            if (isTrackActive) bento.primaryAccent else bento.cardBorderSubtle,
                                            shape = RoundedCornerShape(1.dp)
                                        )
                                )
                            }
                        }

                        // 5 Interactive Step Columns
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            stepItems.forEachIndexed { index, step ->
                                val isSelected = selectedTab == index
                                val isUnlocked = index == 0 ||
                                        (index == 1 && isStep1Done) ||
                                        (index == 2 && isStep1Done && isStep2Done) ||
                                        (index == 3 && isStep1Done && isStep2Done) ||
                                        (index == 4 && isStep1Done && isStep2Done && isStep4Done)

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { tryNavigateToTab(index) }
                                        .testTag("progress_step_$index")
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = when {
                                            isSelected -> bento.primaryAccent
                                            step.isDone -> bento.tilePurple
                                            isUnlocked -> bento.surface
                                            else -> bento.subtleSurface
                                        },
                                        shadowElevation = if (isSelected) 3.dp else 0.dp,
                                        border = androidx.compose.foundation.BorderStroke(
                                            width = if (isSelected) 2.5.dp else 1.5.dp,
                                            color = when {
                                                isSelected -> bento.primaryAccent
                                                step.isDone -> bento.cardBorder
                                                isUnlocked -> bento.cardBorder
                                                else -> bento.cardBorderSubtle
                                            }
                                        ),
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            when {
                                                step.isDone && !isSelected -> {
                                                    Icon(
                                                        Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = SafetyGreen,
                                                        modifier = Modifier.size(17.dp)
                                                    )
                                                }
                                                !isUnlocked -> {
                                                    Icon(
                                                        Icons.Default.Lock,
                                                        contentDescription = null,
                                                        tint = bento.textMuted,
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                }
                                                else -> {
                                                    Text(
                                                        text = "${index + 1}",
                                                        fontWeight = FontWeight.ExtraBold,
                                                        fontSize = 13.sp,
                                                        color = if (isSelected) Color.White else bento.tilePurpleText
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(5.dp))

                                    Text(
                                        text = step.shortLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                        fontSize = 10.sp,
                                        color = when {
                                            isSelected -> bento.tilePurpleText
                                            step.isDone -> bento.textPrimary
                                            isUnlocked -> bento.textSecondary
                                            else -> bento.textMuted
                                        },
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Tab Content with Sequential 5-Step Navigation
            when (selectedTab) {
                0 -> TabUnitAndCustomer(
                    viewModel = viewModel,
                    isCompleted = isStep1Done,
                    onNextStep = { tryNavigateToTab(1) },
                    onOpenPopup = { activePopupField = it }
                )
                1 -> TabPhotosAndFindings(
                    viewModel = viewModel,
                    isCompleted = isStep2Done,
                    onPreviousStep = { tryNavigateToTab(0) },
                    onNextStep = { tryNavigateToTab(2) },
                    onOpenPopup = { activePopupField = it }
                )
                2 -> TabPartsRequired(
                    viewModel = viewModel,
                    isCompleted = isStep3Done,
                    onPreviousStep = { tryNavigateToTab(1) },
                    onNextStep = { tryNavigateToTab(3) },
                    onOpenPopup = { activePopupField = it }
                )
                3 -> TabActionAndConclusions(
                    viewModel = viewModel,
                    isCompleted = isStep4Done,
                    onPreviousStep = { tryNavigateToTab(2) },
                    onNextStep = { tryNavigateToTab(4) },
                    onOpenPopup = { activePopupField = it }
                )
                4 -> TabSignaturesAndEmail(
                    viewModel = viewModel,
                    isCompleted = isStep5Done,
                    onPreviousStep = { tryNavigateToTab(3) },
                    onOpenSignatureDialog = { target -> signatureDialogTarget = target },
                    onOpenPopup = { activePopupField = it },
                    onOpenWhatsAppDispatch = { r, f -> whatsAppDispatchPair = Pair(r, f) },
                    onSaveAndPreview = {
                        viewModel.saveReport(isDraft = false) {
                            viewModel.generatePdfAndPreview(context)
                        }
                    }
                )
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

    // Focused Pop-Up Input Dialog (Unobstructed by Soft Keyboard)
    activePopupField?.let { config ->
        FocusedInputDialog(
            config = config,
            onDismissRequest = { activePopupField = null }
        )
    }

    // Signature Pad Dialogs
    signatureDialogTarget?.let { target ->
        SignaturePadDialog(
            title = "Tanda Tangan Digital - Mekanik yang menginspeksi",
            signerName = report.preparedByName,
            onDismiss = { signatureDialogTarget = null },
            onSignatureSaved = { savedUri ->
                viewModel.updateCurrentReport { it.copy(preparedBySignatureUri = savedUri) }
                signatureDialogTarget = null
            }
        )
    }

    // PDF Viewer Dialog
    previewPdfFile?.let { file ->
        PdfViewerDialog(
            pdfFile = file,
            report = report,
            onDismiss = { viewModel.dismissPdfPreview() }
        )
    }

    // Optional AI assistant. Results are previewed and only inserted after explicit user action.
    if (showAiAssistant) {
        AiAssistantDialog(
            report = report,
            onDismiss = { showAiAssistant = false },
            onInsert = { field, text ->
                viewModel.updateCurrentReport { current ->
                    when (field) {
                        "Defect 1 Findings" -> current.copy(defect1Findings = text)
                        "Defect 2 Findings" -> current.copy(defect2Findings = text)
                        "Hourmeter Findings" -> current.copy(hourmeterFindings = text)
                        "History" -> current.copy(history = text)
                        "Completed Action" -> current.copy(completedAction = text)
                        "Conclusion 1" -> current.copy(conclusion1 = text)
                        "Conclusion 2" -> current.copy(conclusion2 = text)
                        "Conclusion 3" -> current.copy(conclusion3 = text)
                        else -> current
                    }
                }
            }
        )
    }
}

@Composable
fun TabUnitAndCustomer(
    viewModel: ReportViewModel,
    isCompleted: Boolean,
    onNextStep: () -> Unit,
    onOpenPopup: (PopupFieldConfig) -> Unit = {}
) {
    val report by viewModel.currentReport.collectAsStateWithLifecycle()
    var showUnitDatabaseDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }

    // Automatically populate reportDate with current system date if empty
    LaunchedEffect(report.reportDate) {
        if (report.reportDate.isBlank()) {
            val todayStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            viewModel.updateCurrentReport { r -> r.copy(reportDate = todayStr) }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            // Auto-Save Status Banner
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = BentoTileGreen,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SafetyGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Auto-Save Aktif ke Database Room",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF166534)
                        )
                        Text(
                            text = "Semua isian Info Unit & Customer tersimpan seketika di SQLite lokal, tidak akan hilang jika aplikasi tertutup.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate700,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = BentoTileBlue,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("📍", fontSize = 14.sp)
                            }
                        }
                        Column {
                            Text(
                                text = "CUSTOMER & SITE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = BentoPrimaryDark,
                                fontSize = 10.sp,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Informasi Customer & Lokasi Proyek",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        }
                    }

                    SmartFocusTextField(
                        value = report.customerName,
                        onValueChange = { viewModel.updateCurrentReport { r -> r.copy(customerName = it) } },
                        label = "Customer Name *",
                        placeholder = "PT. Amman Mineral",
                        helperChips = listOf("PT. AMMAN MINERAL", "PT. BUKIT ASAM", "PT. FREEPORT INDONESIA", "PT. ADARO ENERGY", "PT. VALE INDONESIA"),
                        testTag = "input_customer_name",
                        modifier = Modifier.fillMaxWidth(),
                        onOpenPopup = onOpenPopup
                    )

                    SmartFocusTextField(
                        value = report.projectSite,
                        onValueChange = { viewModel.updateCurrentReport { r -> r.copy(projectSite = it) } },
                        label = "Project Site *",
                        placeholder = "Batu Hijau",
                        helperChips = listOf("Batu Hijau", "Grasberg", "Tanjung Enim", "Tabang Site", "Sorowako"),
                        testTag = "input_project_site",
                        modifier = Modifier.fillMaxWidth(),
                        onOpenPopup = onOpenPopup
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SmartFocusTextField(
                            value = report.contactPerson,
                            onValueChange = { viewModel.updateCurrentReport { r -> r.copy(contactPerson = it) } },
                            label = "Contact Person",
                            placeholder = "Bpk. Hendra",
                            testTag = "input_contact_person",
                            modifier = Modifier.weight(1f),
                            onOpenPopup = onOpenPopup
                        )

                        OutlinedTextField(
                            value = report.reportDate,
                            onValueChange = { viewModel.updateCurrentReport { r -> r.copy(reportDate = it) } },
                            shape = RoundedCornerShape(16.dp),
                            label = { Text("Date of Inspection *") },
                            placeholder = { Text("DD/MM/YYYY") },
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        val dateParts = report.reportDate.split("/", "-")
                                        var initialYear = calendar.get(Calendar.YEAR)
                                        var initialMonth = calendar.get(Calendar.MONTH)
                                        var initialDay = calendar.get(Calendar.DAY_OF_MONTH)
                                        if (dateParts.size == 3) {
                                            dateParts[0].toIntOrNull()?.let { d -> if (d in 1..31) initialDay = d }
                                            dateParts[1].toIntOrNull()?.let { m -> if (m in 1..12) initialMonth = m - 1 }
                                            dateParts[2].toIntOrNull()?.let { y -> if (y > 2000) initialYear = y }
                                        }
                                        DatePickerDialog(
                                            context,
                                            { _, year, monthOfYear, dayOfMonth ->
                                                val formattedDay = String.format(Locale.getDefault(), "%02d", dayOfMonth)
                                                val formattedMonth = String.format(Locale.getDefault(), "%02d", monthOfYear + 1)
                                                val selectedDate = "$formattedDay/$formattedMonth/$year"
                                                viewModel.updateCurrentReport { r -> r.copy(reportDate = selectedDate) }
                                            },
                                            initialYear,
                                            initialMonth,
                                            initialDay
                                        ).show()
                                    },
                                    modifier = Modifier.testTag("btn_pick_date")
                                ) {
                                    Icon(
                                        Icons.Default.DateRange,
                                        contentDescription = "Pilih Tanggal Inspeksi",
                                        tint = BentoPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            colors = appTextFieldColors(),
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black),
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("input_report_date")
                        )
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
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
                                shape = RoundedCornerShape(10.dp),
                                color = BentoTileAmber,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_mobile_crane),
                                        contentDescription = "Mobile Crane",
                                        tint = Color.Unspecified,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "UNIT SPECIFICATIONS",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoPrimaryDark,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "Spesifikasi Unit",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                            }
                        }

                        // Quick Unit Database button
                        OutlinedButton(
                            onClick = { showUnitDatabaseDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = BentoPrimary)
                        ) {
                            Icon(Icons.Default.DirectionsCar, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Database Unit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Matching Unit Badge or Suggestions
                    val matchedCatalog = remember(report.unitNo) {
                        UnitDatabase.findByUnitNo(report.unitNo)
                    }

                    if (matchedCatalog != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFE8F5E9),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF81C784)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
                                Text(
                                    text = "✓ Unit Terdaftar: ${matchedCatalog.unitNo} • Model: ${matchedCatalog.model} • SN: ${matchedCatalog.serialNo}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF1B5E20),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    } else if (report.unitNo.isNotBlank()) {
                        val suggestions = remember(report.unitNo) {
                            UnitDatabase.searchMatching(report.unitNo).take(4)
                        }
                        if (suggestions.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Saran Unit dari Database (Ketuk untuk pilih):",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Slate500,
                                    fontSize = 11.sp
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    suggestions.forEach { item ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = BentoTileBlue,
                                            border = androidx.compose.foundation.BorderStroke(1.dp, BentoPrimary.copy(alpha = 0.3f)),
                                            modifier = Modifier.clickable {
                                                viewModel.selectUnitCatalogItem(item)
                                            }
                                        ) {
                                            Text(
                                                text = "${item.unitNo} (${item.model})",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = BentoPrimaryDark,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SmartFocusTextField(
                            value = report.unitNo,
                            onValueChange = { viewModel.updateUnitNo(it) },
                            label = "Unit No. *",
                            placeholder = "MC041, EX026",
                            helperChips = listOf("MC041", "MC048", "MC050", "EX026", "RT012", "CC015"),
                            testTag = "input_unit_no",
                            modifier = Modifier.weight(1f),
                            onOpenPopup = onOpenPopup
                        )
                        SmartFocusTextField(
                            value = report.unitBrand,
                            onValueChange = { viewModel.updateCurrentReport { r -> r.copy(unitBrand = it) } },
                            label = "Unit Brand",
                            placeholder = "LIEBHERR",
                            helperChips = listOf("LIEBHERR", "TADANO", "KOBELCO", "KATO", "SANY", "TEREX", "HITACHI", "KOMATSU", "CATERPILLAR"),
                            testTag = "input_unit_brand",
                            modifier = Modifier.weight(1f),
                            onOpenPopup = onOpenPopup
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SmartFocusTextField(
                            value = report.type,
                            onValueChange = { viewModel.updateCurrentReport { r -> r.copy(type = it) } },
                            label = "Type / Model",
                            placeholder = "Mobile and Crawler Crane",
                            helperChips = listOf("Mobile Crane", "Crawler Crane", "All Terrain Crane", "Rough Terrain Crane", "Hydraulic Excavator"),
                            testTag = "input_unit_type",
                            modifier = Modifier.weight(1f),
                            onOpenPopup = onOpenPopup
                        )
                        SmartFocusTextField(
                            value = report.serialNo,
                            onValueChange = { viewModel.updateCurrentReport { r -> r.copy(serialNo = it) } },
                            label = "Serial No.",
                            placeholder = "LH-LR1300-982",
                            testTag = "input_serial_no",
                            modifier = Modifier.weight(1f),
                            onOpenPopup = onOpenPopup
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SmartFocusTextField(
                            value = report.superstructureHourmeter,
                            onValueChange = { viewModel.updateCurrentReport { r -> r.copy(superstructureHourmeter = it) } },
                            label = "Superstructure HM",
                            placeholder = "12,450.5 hrs",
                            testTag = "input_superstructure_hm",
                            modifier = Modifier.weight(1f),
                            onOpenPopup = onOpenPopup
                        )
                        SmartFocusTextField(
                            value = report.chassisHourmeter,
                            onValueChange = { viewModel.updateCurrentReport { r -> r.copy(chassisHourmeter = it) } },
                            label = "Chassis HM",
                            placeholder = "12,450.5 hrs",
                            testTag = "input_chassis_hm",
                            modifier = Modifier.weight(1f),
                            onOpenPopup = onOpenPopup
                        )
                    }

                    SmartFocusTextField(
                        value = report.odometer,
                        onValueChange = { viewModel.updateCurrentReport { r -> r.copy(odometer = it) } },
                        label = "Odometer",
                        placeholder = "e.g. N/A or 45,200 km",
                        testTag = "input_odometer",
                        modifier = Modifier.fillMaxWidth(),
                        onOpenPopup = onOpenPopup
                    )

                    SmartFocusTextField(
                        value = report.jobDescription,
                        onValueChange = { viewModel.updateCurrentReport { r -> r.copy(jobDescription = it) } },
                        label = "Job Description *",
                        placeholder = "Deskripsi pekerjaan inspeksi / problem breakdown...",
                        singleLine = false,
                        minLines = 2,
                        maxLines = 4,
                        showEnglishBadge = true,
                        helperChips = listOf(
                            "Periodic Service Maintenance (2000 HM)",
                            "Troubleshooting Main Winch Hydraulic Leak",
                            "Annual Safety & Load Moment Indicator Inspection",
                            "Overhaul Engine & Transmission Check",
                            "Boom Extension & Wire Rope Condition Inspection"
                        ),
                        testTag = "input_job_description",
                        modifier = Modifier.fillMaxWidth(),
                        onOpenPopup = onOpenPopup
                    )
                }
            }
        }

        // Sequential Step 1 Navigation Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = if (isCompleted) BentoTileGreen else BentoTilePurple),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isCompleted) Color(0xFF86EFAC) else BentoLavenderBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            if (isCompleted) Icons.Default.CheckCircle else Icons.Default.Info,
                            contentDescription = null,
                            tint = if (isCompleted) SafetyGreen else BentoPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (isCompleted) "Langkah 1 Lengkap & Siap" else "Lengkapi Informasi Unit (*)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isCompleted) Color(0xFF166534) else BentoPrimaryDark
                        )
                    }

                    if (!isCompleted) {
                        Text(
                            text = "Harap lengkapi Customer Name, Project Site, Unit No., dan Job Description untuk melanjutkan ke Langkah 2.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate700
                        )
                    }

                    Button(
                        onClick = onNextStep,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCompleted) BentoPrimary else Slate800
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("step1_next_btn")
                    ) {
                        Text("Lanjut ke Langkah 2: Foto & Temuan", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }

    if (showUnitDatabaseDialog) {
        UnitCatalogPickerDialog(
            onDismissRequest = { showUnitDatabaseDialog = false },
            onSelectUnit = { unitItem ->
                viewModel.selectUnitCatalogItem(unitItem)
            }
        )
    }
}

@Composable
fun TabPhotosAndFindings(
    viewModel: ReportViewModel,
    isCompleted: Boolean,
    onPreviousStep: () -> Unit,
    onNextStep: () -> Unit,
    onOpenPopup: (PopupFieldConfig) -> Unit = {}
) {
    val report by viewModel.currentReport.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = BentoLavenderCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, BentoLavenderBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("📸", fontSize = 18.sp)
                        }
                    }
                    Column {
                        Text(
                            text = "FOTO TEMUAN & REKOMENDASI",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = BentoPrimaryDark,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Dokumentasi Kamera Langsung",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = BentoPrimaryDark
                        )
                        Text(
                            text = "Gunakan kamera HP atau galeri untuk lampiran resmi PDF.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate700
                        )
                    }
                }
            }
        }

        // 1. Foto Hourmeter (Foto Saja)
        item {
            InspectionPhotoCard(
                title = "1. Foto Hourmeter",
                subtitle = "Bukti foto pembacaan display hourmeter / dashboard",
                photoUri = report.hourmeterPhotoUri,
                showFindingsInput = false,
                onPhotoChanged = { uri -> viewModel.updateCurrentReport { it.copy(hourmeterPhotoUri = uri) } },
                tagPrefix = "photo_hourmeter"
            )
        }

        // 2. Kolom History (Riwayat Unit / Problem Breakdown)
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = BentoTileBlue,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("📋", fontSize = 14.sp)
                                }
                            }
                            Column {
                                Text(
                                    text = "UNIT HISTORY",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoPrimaryDark,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "History / Riwayat Unit",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                            }
                        }
                    }

                    Text(
                        text = "Riwayat kerusakan, jam operasi, atau catatan servis unit sebelumnya.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500
                    )

                    SmartFocusTextField(
                        value = report.history,
                        onValueChange = { text -> viewModel.updateCurrentReport { it.copy(history = text) } },
                        label = "History Unit / Problem Breakdown",
                        placeholder = "Contoh: Riwayat kebocoran oli pada silinder boom saat operasi lifting, perbaikan terakhir pada 4500 HM...",
                        singleLine = false,
                        minLines = 3,
                        maxLines = 6,
                        helperChips = listOf(
                            "Riwayat kebocoran oli silinder boom saat operasi lifting",
                            "Unit mengalami overheat mesin saat beban puncak di siang hari",
                            "Penggantian wire rope hoist utama terakhir pada 4.000 HM",
                            "Troubleshooting sistem transmisi slip pada gear 2 dan 3",
                            "Inspeksi berkala 2000 hours service maintenance"
                        ),
                        testTag = "input_history",
                        modifier = Modifier.fillMaxWidth(),
                        onOpenPopup = onOpenPopup
                    )

                    // Realtime Live English Translation Preview for PDF
                    if (report.history.isNotBlank()) {
                        val translatedPreview = remember(report.history) {
                            TechnicalTranslator.translateToEnglish(report.history)
                        }
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Slate100,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text("🇬🇧", fontSize = 12.sp)
                                        Text(
                                            text = "Preview Terjemahan Bahasa Inggris di PDF:",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Slate700,
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                Text(
                                    text = translatedPreview,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate800,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Foto Defect 1 (Foto + Finding and Recommendation)
        item {
            InspectionPhotoCard(
                title = "2. Foto Defect 1",
                subtitle = "Temuan kerusakan / keausan komponen utama",
                photoUri = report.defect1PhotoUri,
                findingsText = report.defect1Findings,
                showFindingsInput = true,
                onPhotoChanged = { uri -> viewModel.updateCurrentReport { it.copy(defect1PhotoUri = uri) } },
                onFindingsChanged = { text -> viewModel.updateCurrentReport { it.copy(defect1Findings = text) } },
                tagPrefix = "photo_defect1"
            )
        }

        // 4. Foto Defect 2 (Foto + Finding and Recommendation)
        item {
            InspectionPhotoCard(
                title = "3. Foto Defect 2",
                subtitle = "Temuan kerusakan / temuan inspeksi pendukung",
                photoUri = report.defect2PhotoUri,
                findingsText = report.defect2Findings,
                showFindingsInput = true,
                onPhotoChanged = { uri -> viewModel.updateCurrentReport { it.copy(defect2PhotoUri = uri) } },
                onFindingsChanged = { text -> viewModel.updateCurrentReport { it.copy(defect2Findings = text) } },
                tagPrefix = "photo_defect2"
            )
        }

        // Sequential Step 2 Navigation Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = if (isCompleted) BentoTileGreen else BentoTilePurple),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isCompleted) Color(0xFF86EFAC) else BentoLavenderBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            if (isCompleted) Icons.Default.CheckCircle else Icons.Default.Info,
                            contentDescription = null,
                            tint = if (isCompleted) SafetyGreen else BentoPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (isCompleted) "Dokumentasi Foto Siap" else "Lengkapi Foto Hourmeter / Temuan",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isCompleted) Color(0xFF166534) else BentoPrimaryDark
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onPreviousStep,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("step2_prev_btn")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Kembali", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onNextStep,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isCompleted) BentoPrimary else Slate800
                            ),
                            modifier = Modifier
                                .weight(1.5f)
                                .height(48.dp)
                                .testTag("step2_next_btn")
                        ) {
                            Text("Lanjut ke Step 3 (Parts)", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TabPartsRequired(
    viewModel: ReportViewModel,
    isCompleted: Boolean,
    onPreviousStep: () -> Unit,
    onNextStep: () -> Unit,
    onOpenPopup: (PopupFieldConfig) -> Unit = {}
) {
    val partsList = viewModel.currentPartsList

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Card
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = BentoLavenderCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, BentoLavenderBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("⚙️", fontSize = 18.sp)
                        }
                    }
                    Column {
                        Text(
                            text = "LANGKAH 3: SPARE PARTS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = BentoPrimaryDark,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Kebutuhan Spare Part (Part Required)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = BentoPrimaryDark
                        )
                        Text(
                            text = "Catat daftar suku cadang yang dibutuhkan untuk penggantian atau perbaikan unit.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate700
                        )
                    }
                }
            }
        }

        // Section: Part Required
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "PARTS REQUIREMENT",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = BentoPrimaryDark,
                                fontSize = 10.sp,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Part Required :",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        }

                        FilledTonalButton(
                            onClick = { viewModel.addPartItem() },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = BentoTilePurple,
                                contentColor = BentoPrimaryDark
                            ),
                            modifier = Modifier.testTag("add_part_row_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tambah Part", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (partsList.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = BentoBackground,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Belum ada spare part yang ditambahkan.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = Slate700
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Bagian ini bersifat opsional. Klik 'Tambah Part' jika ada kebutuhan part, atau langsung klik 'Lanjut ke Step 4'.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate500
                                )
                            }
                        }
                    } else {
                        partsList.forEachIndexed { index, item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SmartFocusTextField(
                                    value = item.partNumber,
                                    onValueChange = { viewModel.updatePartItem(index, item.copy(partNumber = it)) },
                                    label = "P/N",
                                    placeholder = "Part No.",
                                    testTag = "part_number_$index",
                                    modifier = Modifier.weight(1.3f),
                                    onOpenPopup = onOpenPopup
                                )

                                SmartFocusTextField(
                                    value = item.partDescription,
                                    onValueChange = { viewModel.updatePartItem(index, item.copy(partDescription = it)) },
                                    label = "Description",
                                    placeholder = "Deskripsi",
                                    testTag = "part_desc_$index",
                                    modifier = Modifier.weight(2f),
                                    onOpenPopup = onOpenPopup
                                )

                                SmartFocusTextField(
                                    value = item.qty,
                                    onValueChange = { viewModel.updatePartItem(index, item.copy(qty = it)) },
                                    label = "Qty",
                                    placeholder = "1",
                                    keyboardType = KeyboardType.Number,
                                    testTag = "part_qty_$index",
                                    modifier = Modifier.weight(0.9f),
                                    onOpenPopup = onOpenPopup
                                )

                                IconButton(
                                    onClick = { viewModel.removePartItem(index) },
                                    modifier = Modifier.testTag("delete_part_$index")
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = SafetyRed)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Sequential Step 3 Navigation Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = BentoTilePurple),
                border = androidx.compose.foundation.BorderStroke(1.dp, BentoLavenderBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = BentoPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (partsList.isNotEmpty()) "${partsList.size} Part Tercatat • Lanjut Tindakan" else "Spare Part Opsional • Lanjut Tindakan",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = BentoPrimaryDark
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onPreviousStep,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("step3_prev_btn")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Kembali", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onNextStep,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BentoPrimary
                            ),
                            modifier = Modifier
                                .weight(1.5f)
                                .height(48.dp)
                                .testTag("step3_next_btn")
                        ) {
                            Text("Lanjut ke Step 4 (Tindakan)", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TabActionAndConclusions(
    viewModel: ReportViewModel,
    isCompleted: Boolean,
    onPreviousStep: () -> Unit,
    onNextStep: () -> Unit,
    onOpenPopup: (PopupFieldConfig) -> Unit = {}
) {
    val report by viewModel.currentReport.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Card
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = BentoLavenderCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, BentoLavenderBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("📋", fontSize = 18.sp)
                        }
                    }
                    Column {
                        Text(
                            text = "LANGKAH 4: TINDAKAN & KESIMPULAN",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = BentoPrimaryDark,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Completed Action & Conclusion",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = BentoPrimaryDark
                        )
                        Text(
                            text = "Rincikan tindakan yang telah dilaksanakan dan kesimpulan status unit.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate700
                        )
                    }
                }
            }
        }

        // Section: Completed Action:
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "MAINTENANCE ACTION",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = BentoPrimaryDark,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Completed Action: *",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Text(
                        text = "Tindakan perbaikan/pengecekan yang telah diselesaikan oleh mekanik",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500
                    )

                    SmartFocusTextField(
                        value = report.completedAction,
                        onValueChange = { viewModel.updateCurrentReport { r -> r.copy(completedAction = it) } },
                        label = "Completed Action *",
                        placeholder = "Tuliskan tindakan yang telah dilakukan...\n1. ...\n2. ...",
                        singleLine = false,
                        minLines = 4,
                        maxLines = 8,
                        helperChips = listOf(
                            "1. Replaced damaged hydraulic high pressure seal kit\n2. Torqued all boom connection bolts to manufacturer specs\n3. Functional testing without load: Normal",
                            "1. Performed thorough cleaning and inspection\n2. Adjusted cable tension and limit switch\n3. Load test passed successfully"
                        ),
                        testTag = "input_completed_action",
                        modifier = Modifier.fillMaxWidth(),
                        onOpenPopup = onOpenPopup
                    )
                }
            }
        }

        // Section: Conclusion:
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "FINAL REMARKS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = BentoPrimaryDark,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Conclusion:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Text(
                        text = "Kesimpulan status kelayakan unit dan instruksi tindak lanjut",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500
                    )

                    SmartFocusTextField(
                        value = report.conclusion1,
                        onValueChange = { viewModel.updateCurrentReport { r -> r.copy(conclusion1 = it) } },
                        label = "1. Kesimpulan 1",
                        placeholder = "e.g. Unit layak operasi dengan catatan...",
                        singleLine = false,
                        helperChips = listOf(
                            "Unit in good and safe operational condition.",
                            "Unit layak operasi penuh (Ready for Operation).",
                            "Unit temporarily down pending spare parts arrival."
                        ),
                        testTag = "input_conclusion_1",
                        modifier = Modifier.fillMaxWidth(),
                        onOpenPopup = onOpenPopup
                    )

                    SmartFocusTextField(
                        value = report.conclusion2,
                        onValueChange = { viewModel.updateCurrentReport { r -> r.copy(conclusion2 = it) } },
                        label = "2. Kesimpulan 2",
                        placeholder = "e.g. Jadwalkan pergantian part...",
                        singleLine = false,
                        helperChips = listOf(
                            "Schedule periodic inspection after 250 operating hours.",
                            "Monitoring hose fitting daily during pre-start check.",
                            "Replace filter element on next routine maintenance."
                        ),
                        testTag = "input_conclusion_2",
                        modifier = Modifier.fillMaxWidth(),
                        onOpenPopup = onOpenPopup
                    )

                    SmartFocusTextField(
                        value = report.conclusion3,
                        onValueChange = { viewModel.updateCurrentReport { r -> r.copy(conclusion3 = it) } },
                        label = "3. Kesimpulan 3",
                        placeholder = "e.g. Monitoring berkala setiap shift...",
                        singleLine = false,
                        helperChips = listOf(
                            "Operator must perform daily safety inspection.",
                            "Follow standard operating procedures and load chart."
                        ),
                        testTag = "input_conclusion_3",
                        modifier = Modifier.fillMaxWidth(),
                        onOpenPopup = onOpenPopup
                    )
                }
            }
        }

        // Sequential Step 4 Navigation Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = if (isCompleted) BentoTileGreen else BentoTilePurple),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isCompleted) Color(0xFF86EFAC) else BentoLavenderBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            if (isCompleted) Icons.Default.CheckCircle else Icons.Default.Info,
                            contentDescription = null,
                            tint = if (isCompleted) SafetyGreen else BentoPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (isCompleted) "Tindakan Selesai Diisi" else "Wajib: Isi Completed Action",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isCompleted) Color(0xFF166534) else BentoPrimaryDark
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onPreviousStep,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("step4_prev_btn")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Kembali", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onNextStep,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isCompleted) BentoPrimary else Slate800
                            ),
                            modifier = Modifier
                                .weight(1.5f)
                                .height(48.dp)
                                .testTag("step4_next_btn")
                        ) {
                            Text("Lanjut ke Step 5 (TTd)", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TabSignaturesAndEmail(
    viewModel: ReportViewModel,
    isCompleted: Boolean,
    onPreviousStep: () -> Unit,
    onOpenSignatureDialog: (target: String) -> Unit,
    onOpenPopup: (PopupFieldConfig) -> Unit = {},
    onOpenWhatsAppDispatch: (FieldServiceReport, File) -> Unit = { _, _ -> },
    onSaveAndPreview: () -> Unit
) {
    val report by viewModel.currentReport.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = BentoLavenderCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, BentoLavenderBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("✍️", fontSize = 18.sp)
                        }
                    }
                    Column {
                        Text(
                            text = "ONLINE DIGITAL SIGNATURES",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = BentoPrimaryDark,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Tanda Tangan Digital Mekanik & Email",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = BentoPrimaryDark
                        )
                        Text(
                            text = "Goreskan tanda tangan digital mekanik yang melakukan inspeksi unit.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate700
                        )
                    }
                }
            }
        }

        // 1. Prepared By (Mekanik yang menginspeksi) - Digital Signature
        item {
            SignatureCard(
                title = "Prepared By,",
                roleDescription = "Mekanik yang menginspeksi (Tanda Tangan Digital)",
                name = report.preparedByName,
                position = report.preparedByPosition,
                signatureUri = report.preparedBySignatureUri,
                onNameChange = { viewModel.updateCurrentReport { r -> r.copy(preparedByName = it) } },
                onPositionChange = { viewModel.updateCurrentReport { r -> r.copy(preparedByPosition = it) } },
                onSignClicked = { onOpenSignatureDialog("prepared") },
                onClearSignature = { viewModel.updateCurrentReport { r -> r.copy(preparedBySignatureUri = null) } },
                tagPrefix = "sig_prepared",
                onOpenPopup = onOpenPopup
            )
        }

        // Email Pengiriman Setting
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = BentoTilePurple,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("✉️", fontSize = 14.sp)
                            }
                        }
                        Column {
                            Text(
                                text = "EMAIL DISPATCH",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = BentoPrimaryDark,
                                fontSize = 10.sp,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Tujuan Email Pengiriman",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        }
                    }

                    // List of Default TO Recipients
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFEFF6FF),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = BentoPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Daftar Email Penerima Tertuju (TO):",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoPrimaryDark,
                                    fontSize = 12.sp
                                )
                            }
                            EmailSharingHelper.DEFAULT_TO_RECIPIENTS.forEach { email ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("•", color = BentoPrimary, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = email,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Slate800,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    // List of Default CC Recipients
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFFFF7ED),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFED7AA)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("📋", fontSize = 14.sp)
                                Text(
                                    text = "Daftar Tembusan (CC):",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC2410C),
                                    fontSize = 12.sp
                                )
                            }
                            EmailSharingHelper.DEFAULT_CC_RECIPIENTS.forEach { email ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("•", color = Color(0xFFEA580C), fontWeight = FontWeight.Bold)
                                    Text(
                                        text = email,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Slate800,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    SmartFocusTextField(
                        value = report.recipientEmail,
                        onValueChange = { viewModel.updateCurrentReport { r -> r.copy(recipientEmail = it) } },
                        label = "Email Tambahan (Opsional)",
                        placeholder = "e.g. tambahan@ptnim.co.id",
                        testTag = "input_recipient_email",
                        modifier = Modifier.fillMaxWidth(),
                        onOpenPopup = onOpenPopup
                    )

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = BentoBackground,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Aplikasi akan otomatis mengisi seluruh alamat email TO dan CC di atas beserta melampirkan file PDF FSR resmi 2-halaman ke aplikasi email Anda (Gmail/Outlook).",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500,
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    // Action Buttons for Instant Sharing
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.saveReport(isDraft = false) {
                                    viewModel.generatePdfAndPreview(context) { pdfFile ->
                                        onOpenWhatsAppDispatch(report, pdfFile)
                                    }
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF25D366),
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("share_whatsapp_direct_btn")
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_whatsapp),
                                contentDescription = "WhatsApp",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Share WhatsApp", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Button(
                            onClick = {
                                viewModel.saveReport(isDraft = false) {
                                    viewModel.generatePdfAndPreview(context) { pdfFile ->
                                        EmailSharingHelper.sendReportByEmail(context, report, pdfFile)
                                    }
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("send_email_direct_btn")
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Kirim Email", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Sequential Step 5 Finalize Navigation Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = BentoTilePurple),
                border = androidx.compose.foundation.BorderStroke(1.dp, BentoLavenderBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            if (isCompleted) Icons.Default.CheckCircle else Icons.Default.Info,
                            contentDescription = null,
                            tint = if (isCompleted) SafetyGreen else BentoPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (isCompleted) "Semua 5 Langkah Pengisian Selesai!" else "Langkah 5: Tanda Tangan & Selesai",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = BentoPrimaryDark
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(
                            onClick = onPreviousStep,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(0.85f)
                                .height(46.dp)
                                .testTag("step5_prev_btn")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("Kembali", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.saveReport(isDraft = false) {
                                    viewModel.generatePdfAndPreview(context) { pdfFile ->
                                        onOpenWhatsAppDispatch(report, pdfFile)
                                    }
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF1E7E34)
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF25D366)),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("step5_whatsapp_btn")
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_whatsapp),
                                contentDescription = null,
                                tint = Color.Unspecified,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("WhatsApp", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.saveReport(isDraft = false) {
                                    viewModel.generatePdfAndPreview(context) { pdfFile ->
                                        EmailSharingHelper.sendReportByEmail(context, report, pdfFile)
                                    }
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(0.95f)
                                .height(46.dp)
                                .testTag("step5_email_btn")
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(14.dp), tint = BentoPrimary)
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Email", fontWeight = FontWeight.Bold, color = BentoPrimaryDark, fontSize = 11.sp)
                        }

                        Button(
                            onClick = onSaveAndPreview,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary),
                            modifier = Modifier
                                .weight(1.1f)
                                .height(46.dp)
                                .testTag("step5_finish_btn")
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Simpan & PDF", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SignatureCard(
    title: String,
    roleDescription: String,
    name: String,
    position: String,
    signatureUri: String?,
    onNameChange: (String) -> Unit,
    onPositionChange: (String) -> Unit,
    onSignClicked: () -> Unit,
    onClearSignature: () -> Unit,
    tagPrefix: String,
    isTitleEditable: Boolean = false,
    customTitleValue: String = "",
    onCustomTitleChange: (String) -> Unit = {},
    onOpenPopup: (PopupFieldConfig) -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isTitleEditable) {
                SmartFocusTextField(
                    value = customTitleValue,
                    onValueChange = onCustomTitleChange,
                    label = "Header Judul Pihak Ke-3",
                    placeholder = "Acknowledge By PT. AMNT",
                    modifier = Modifier.fillMaxWidth(),
                    onOpenPopup = onOpenPopup
                )
            } else {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BentoPrimaryDark
                )
            }

            Text(
                text = roleDescription,
                style = MaterialTheme.typography.bodySmall,
                color = Slate500
            )

            // Signature drawing preview box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(Color(0xFFFCFDFD), RoundedCornerShape(16.dp))
                    .border(1.5.dp, Color(0xFFD0BCFF), RoundedCornerShape(16.dp))
                    .clickable { onSignClicked() }
                    .testTag("${tagPrefix}_canvas_preview"),
                contentAlignment = Alignment.Center
            ) {
                if (!signatureUri.isNullOrBlank()) {
                    AsyncImage(
                        model = signatureUri,
                        contentDescription = title,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    )

                    // Retake & Clear overlay
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                    ) {
                        IconButton(
                            onClick = onSignClicked,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Ubah TTd", tint = BentoPrimary, modifier = Modifier.size(18.dp))
                        }
                        IconButton(
                            onClick = onClearSignature,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Hapus TTd", tint = SafetyRed, modifier = Modifier.size(18.dp))
                        }
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Draw,
                            contentDescription = null,
                            tint = BentoPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Ketuk untuk tanda tangan online",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = BentoPrimary
                        )
                    }
                }
            }

            // Name & Position text fields
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SmartFocusTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = "Name",
                    placeholder = "Nama Lengkap",
                    helperChips = listOf("Satrio Susanto", "Rudi Hartono", "Ahmad Fauzi"),
                    testTag = "${tagPrefix}_name_input",
                    modifier = Modifier.weight(1f),
                    onOpenPopup = onOpenPopup
                )

                SmartFocusTextField(
                    value = position,
                    onValueChange = onPositionChange,
                    label = "Position",
                    placeholder = "Jabatan",
                    helperChips = listOf("Service Mechanic", "Senior Technician", "Field Engineer", "Hydraulic Specialist"),
                    testTag = "${tagPrefix}_pos_input",
                    modifier = Modifier.weight(1f),
                    onOpenPopup = onOpenPopup
                )
            }
        }
    }
}

@Composable
fun UnitCatalogPickerDialog(
    onDismissRequest: () -> Unit,
    onSelectUnit: (UnitCatalogItem) -> Unit
) {
    val bento = MaterialTheme.bento
    var searchQuery by remember { mutableStateOf("") }
    val filteredList = remember(searchQuery) {
        UnitDatabase.searchMatching(searchQuery)
    }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = bento.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, bento.cardBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = if (bento.isDark) 0.dp else 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 580.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Database Unit",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = bento.textPrimary
                        )
                        Text(
                            text = "Pilih unit untuk otomatis mengisi Model & Serial No.",
                            style = MaterialTheme.typography.bodySmall,
                            color = bento.textSecondary
                        )
                    }
                    IconButton(onClick = onDismissRequest) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = bento.textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    shape = RoundedCornerShape(14.dp),
                    placeholder = { Text("Cari Unit No, Model, atau Serial...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = bento.textMuted) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Hapus", tint = bento.textMuted)
                            }
                        }
                    },
                    colors = appTextFieldColors(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredList.size) { index ->
                        val item = filteredList[index]
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = bento.subtleSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, bento.cardBorderSubtle),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectUnit(item)
                                    onDismissRequest()
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = item.unitNo,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = bento.tilePurpleText
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = bento.tileAmber
                                        ) {
                                            Text(
                                                text = item.brand,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = bento.tileAmberText,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Model: ${item.model} • SN: ${item.serialNo}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = bento.textSecondary
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = bento.primaryAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

