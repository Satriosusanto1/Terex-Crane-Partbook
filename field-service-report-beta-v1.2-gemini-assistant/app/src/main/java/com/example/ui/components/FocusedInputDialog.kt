package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.BentoBackground
import com.example.ui.theme.BentoLavenderBorder
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoPrimaryDark
import com.example.ui.theme.BentoTileBlue
import com.example.ui.theme.BentoTileGreen
import com.example.ui.theme.BentoTilePurple
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.appTextFieldColors
import com.example.util.TechnicalTranslator
import kotlinx.coroutines.delay

/**
 * Configuration for the focused popup input dialog.
 */
data class PopupFieldConfig(
    val title: String,
    val subtitle: String? = null,
    val initialValue: String,
    val placeholder: String = "",
    val isMultiline: Boolean = true,
    val helperChips: List<String> = emptyList(),
    val keyboardType: KeyboardType = KeyboardType.Text,
    val icon: ImageVector = Icons.Default.EditNote,
    val onConfirm: (String) -> Unit
)

/**
 * Dedicated Full-Visibility Top-Up / Popup Input Dialog.
 *
 * Placed comfortably in the upper half of the screen with `imePadding()`,
 * ensuring that soft keyboards NEVER obstruct the input text, cursor,
 * or controls.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FocusedInputDialog(
    config: PopupFieldConfig,
    onDismissRequest: () -> Unit
) {
    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = config.initialValue,
                selection = TextRange(config.initialValue.length)
            )
        )
    }

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()
    val chipsScrollState = rememberScrollState()

    DisposableEffect(Unit) {
        onDispose {
            try {
                focusManager.clearFocus(force = true)
                keyboardController?.hide()
            } catch (_: Exception) {
                // Safe fallback
            }
        }
    }

    // Request auto-focus and auto-show keyboard on open
    LaunchedEffect(Unit) {
        try {
            delay(100)
            focusRequester.requestFocus()
            keyboardController?.show()
        } catch (_: Exception) {
            // Safe fallback
        }
    }

    val charCount = textFieldValue.text.length
    val wordCount = remember(textFieldValue.text) {
        textFieldValue.text.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }.size
    }

    val handleDismiss = {
        try {
            focusManager.clearFocus(force = true)
            keyboardController?.hide()
        } catch (_: Exception) {
            // Safe fallback
        }
        onDismissRequest()
    }

    Dialog(
        onDismissRequest = handleDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
                .windowInsetsPadding(WindowInsets.statusBars)
                .imePadding()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                border = BorderStroke(1.5.dp, BentoLavenderBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 620.dp)
                    .testTag("focused_input_dialog")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = BentoTilePurple,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = config.icon,
                                        contentDescription = null,
                                        tint = BentoPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Column {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = BentoTileBlue
                                ) {
                                    Text(
                                        text = "POP-UP INPUT NYAMAN",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoPrimaryDark,
                                        fontSize = 9.sp,
                                        letterSpacing = 0.5.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = config.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900,
                                    maxLines = 1
                                )
                            }
                        }

                        IconButton(
                            onClick = handleDismiss,
                            modifier = Modifier
                                .size(36.dp)
                                .background(Slate100, CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Tutup",
                                tint = Slate700,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    if (!config.subtitle.isNullOrBlank()) {
                        Text(
                            text = config.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500,
                            lineHeight = 16.sp
                        )
                    }

                    // Helper Toolbar: Paste, Bullet, Clear
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Slate100,
                            border = BorderStroke(1.dp, Slate200),
                            modifier = Modifier.clickable {
                                val clip = clipboardManager.getText()?.text
                                if (!clip.isNullOrBlank()) {
                                    val current = textFieldValue.text
                                    val newText = if (current.isBlank()) clip else "$current $clip"
                                    textFieldValue = TextFieldValue(
                                        text = newText,
                                        selection = TextRange(newText.length)
                                    )
                                }
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.ContentPaste,
                                    contentDescription = null,
                                    tint = BentoPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    "Tempel",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = BentoPrimaryDark
                                )
                            }
                        }

                        if (config.isMultiline) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Slate100,
                                border = BorderStroke(1.dp, Slate200),
                                modifier = Modifier.clickable {
                                    val current = textFieldValue.text
                                    val newText = if (current.isBlank()) "• " else if (current.endsWith("\n")) "$current• " else "$current\n• "
                                    textFieldValue = TextFieldValue(
                                        text = newText,
                                        selection = TextRange(newText.length)
                                    )
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.FormatListBulleted,
                                        contentDescription = null,
                                        tint = BentoPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        "Bullet",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = BentoPrimaryDark
                                    )
                                }
                            }
                        }

                        if (textFieldValue.text.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Slate100,
                                border = BorderStroke(1.dp, Slate200),
                                modifier = Modifier.clickable {
                                    textFieldValue = TextFieldValue(text = "", selection = TextRange(0))
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = null,
                                        tint = Slate500,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        "Hapus",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Slate700
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Word / Char count badge
                        Text(
                            text = "$charCount huruf • $wordCount kata",
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate400,
                            fontSize = 10.sp
                        )
                    }

                    // Quick Preset Chips (if provided)
                    if (config.helperChips.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(chipsScrollState),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            config.helperChips.forEach { chipText ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = BentoTilePurple,
                                    border = BorderStroke(1.dp, BentoLavenderBorder),
                                    modifier = Modifier.clickable {
                                        val current = textFieldValue.text
                                        val newText = if (current.isBlank()) {
                                            chipText
                                        } else if (config.isMultiline) {
                                            "$current\n$chipText"
                                        } else {
                                            chipText
                                        }
                                        textFieldValue = TextFieldValue(
                                            text = newText,
                                            selection = TextRange(newText.length)
                                        )
                                    }
                                ) {
                                    Text(
                                        text = "+ $chipText",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = BentoPrimaryDark,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Main Spacious Text Field (Auto-focused, High contrast, Anti-Keyboard Block)
                    OutlinedTextField(
                        value = textFieldValue,
                        onValueChange = {
                            textFieldValue = it
                            config.onConfirm(it.text)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .testTag("focused_popup_input_field"),
                        shape = RoundedCornerShape(16.dp),
                        placeholder = {
                            Text(
                                text = config.placeholder.ifBlank { "Ketik di sini dengan leluasa..." },
                                color = Slate400,
                                fontSize = 14.sp
                            )
                        },
                        colors = appTextFieldColors(),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = Color.Black,
                            fontSize = 15.sp,
                            lineHeight = 22.sp
                        ),
                        singleLine = !config.isMultiline,
                        minLines = if (config.isMultiline) 4 else 1,
                        maxLines = if (config.isMultiline) 8 else 1,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = config.keyboardType,
                            imeAction = if (config.isMultiline) ImeAction.Default else ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                config.onConfirm(textFieldValue.text)
                                handleDismiss()
                            }
                        )
                    )

                    // Action Buttons: Cancel and Confirm/Save
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = handleDismiss,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Text(
                                text = "Batal",
                                color = Slate700,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Button(
                            onClick = {
                                config.onConfirm(textFieldValue.text)
                                handleDismiss()
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BentoPrimary,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1.5f)
                                .height(46.dp)
                                .testTag("focused_popup_save_btn")
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Simpan & Selesai",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Reusable Form Text Field with built-in Automatic "Pop-up / Top-up Zoom"
 * Responds to 1 tap (single click) or 2 taps (double click) anywhere on the field,
 * opening an elevated focused dialog with soft keyboard auto-focused at the top of the screen.
 */
@Composable
fun SmartFocusTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text,
    showEnglishBadge: Boolean = false,
    helperChips: List<String> = emptyList(),
    icon: ImageVector = Icons.Default.EditNote,
    testTag: String = "",
    onOpenPopup: (PopupFieldConfig) -> Unit
) {
    val haptic = LocalHapticFeedback.current

    val openPopup: () -> Unit = {
        try {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        } catch (_: Exception) {
            // Haptic optional
        }
        onOpenPopup(
            PopupFieldConfig(
                title = label.replace("*", "").trim(),
                subtitle = if (singleLine) "Ketik satu baris dalam jendela pop-up nyaman bebas halangan keyboard" else "Ketik paragraf leluasa dalam jendela pop-up nyaman bebas halangan keyboard",
                initialValue = value,
                placeholder = placeholder,
                isMultiline = !singleLine,
                helperChips = helperChips,
                keyboardType = keyboardType,
                icon = icon,
                onConfirm = onValueChange
            )
        )
    }

    Box(
        modifier = modifier
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .then(if (testTag.isNotBlank()) Modifier.testTag(testTag) else Modifier),
            shape = RoundedCornerShape(16.dp),
            label = { Text(label) },
            placeholder = { Text(if (placeholder.isNotBlank()) placeholder else "Ketuk 1x atau 2x untuk mengetik...") },
            trailingIcon = {
                IconButton(
                    onClick = openPopup,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag(if (testTag.isNotBlank()) "${testTag}_popup_btn" else "popup_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = "Buka Pop-up Nyaman (Klik 1x / 2x)",
                        tint = BentoPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            },
            colors = appTextFieldColors(),
            textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black),
            singleLine = singleLine,
            minLines = minLines,
            maxLines = maxLines
        )

        // Full transparent gesture overlay catching single click (1x) AND double click (2x)
        Box(
            modifier = Modifier
                .matchParentSize()
                .pointerInput(value, label) {
                    detectTapGestures(
                        onTap = { openPopup() },
                        onDoubleTap = { openPopup() }
                    )
                }
        )
    }
}
