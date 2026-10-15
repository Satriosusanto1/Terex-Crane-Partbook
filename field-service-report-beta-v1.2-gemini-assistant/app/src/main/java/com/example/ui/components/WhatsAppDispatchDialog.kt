package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.FieldServiceReport
import com.example.R
import com.example.ui.theme.BentoBackground
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoPrimaryDark
import com.example.ui.theme.BentoTileBlue
import com.example.ui.theme.BentoTilePurple
import com.example.ui.theme.IndustrialAmber
import com.example.ui.theme.SafetyGreen
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.bento
import com.example.util.EmailSharingHelper
import com.example.util.WhatsAppAuthorizedGroup
import java.io.File

private val WhatsAppBrandGreen = Color(0xFF25D366)
private val WhatsAppDarkGreen = Color(0xFF075E54)
private val WhatsAppLightBg = Color(0xFFE8F8F0)

/**
 * Dedicated Modal Dialog allowing the Service Engineer to share the generated PDF report
 * strictly to the 2 authorized operational WhatsApp groups:
 * 1. PT Amman Non Liebherr Operation
 * 2. PT Amman Liebherr Operation
 */
@Composable
fun WhatsAppDispatchDialog(
    report: FieldServiceReport,
    pdfFile: File,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val bento = MaterialTheme.bento
    val suggestedGroup = remember(report) {
        WhatsAppAuthorizedGroup.getSuggestedGroup(report)
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = bento.surface),
            border = BorderStroke(1.dp, bento.cardBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 20.dp)
                .testTag("whatsapp_dispatch_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
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
                            color = WhatsAppLightBg,
                            border = BorderStroke(1.dp, WhatsAppBrandGreen.copy(alpha = 0.4f)),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_whatsapp),
                                    contentDescription = null,
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Share ke WhatsApp Group",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = bento.textPrimary
                            )
                            Text(
                                text = "Pilih grup resmi tujuan operasional PT Amman",
                                style = MaterialTheme.typography.bodySmall,
                                color = bento.textSecondary,
                                fontSize = 11.5.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = bento.textSecondary
                        )
                    }
                }

                HorizontalDivider(color = bento.cardBorder)

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Operational Notice
                    item {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = WhatsAppLightBg,
                            border = BorderStroke(1.dp, WhatsAppBrandGreen.copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = WhatsAppDarkGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = "Grup Khusus Operasional Resmi",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = WhatsAppDarkGreen
                                    )
                                    Text(
                                        text = "Laporan FSR diformat khusus dan hanya ditujukan untuk 2 grup operasional di bawah ini.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Slate700,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }

                    // Authorized Group 1: PT Amman Non Liebherr Operation
                    item {
                        WhatsAppGroupSelectionCard(
                            group = WhatsAppAuthorizedGroup.PT_AMMAN_NON_LIEBHERR,
                            isSuggested = suggestedGroup == WhatsAppAuthorizedGroup.PT_AMMAN_NON_LIEBHERR,
                            suggestedReason = "Unit Non-Liebherr (${report.unitBrand.ifBlank { "General" }})",
                            onSelect = {
                                EmailSharingHelper.shareReportByWhatsApp(
                                    context = context,
                                    report = report,
                                    pdfFile = pdfFile,
                                    targetGroup = WhatsAppAuthorizedGroup.PT_AMMAN_NON_LIEBHERR
                                )
                                onDismissRequest()
                            }
                        )
                    }

                    // Authorized Group 2: PT Amman Liebherr Operation
                    item {
                        WhatsAppGroupSelectionCard(
                            group = WhatsAppAuthorizedGroup.PT_AMMAN_LIEBHERR,
                            isSuggested = suggestedGroup == WhatsAppAuthorizedGroup.PT_AMMAN_LIEBHERR,
                            suggestedReason = "Unit Liebherr (${report.unitBrand} ${report.type})",
                            onSelect = {
                                EmailSharingHelper.shareReportByWhatsApp(
                                    context = context,
                                    report = report,
                                    pdfFile = pdfFile,
                                    targetGroup = WhatsAppAuthorizedGroup.PT_AMMAN_LIEBHERR
                                )
                                onDismissRequest()
                            }
                        )
                    }

                    // Summary of Report being shared
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = bento.tileBlue,
                            border = BorderStroke(1.dp, bento.cardBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Dokumen yang Dikirim",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoPrimaryDark
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.AttachFile,
                                            contentDescription = null,
                                            tint = BentoPrimary,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = pdfFile.name,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = BentoPrimary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Unit: ${report.unitNo.ifBlank { "-" }} • ${report.unitBrand} ${report.type}".trim(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = bento.textPrimary,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = report.reportDate.ifBlank { "Hari ini" },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = bento.textSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Bottom Action: Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismissRequest,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Batal", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun WhatsAppGroupSelectionCard(
    group: WhatsAppAuthorizedGroup,
    isSuggested: Boolean,
    suggestedReason: String,
    onSelect: () -> Unit
) {
    val bento = MaterialTheme.bento

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (isSuggested) WhatsAppLightBg.copy(alpha = 0.6f) else bento.surface,
        border = BorderStroke(
            if (isSuggested) 1.5.dp else 1.dp,
            if (isSuggested) WhatsAppBrandGreen else bento.cardBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("wa_group_${group.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
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
                        shape = CircleShape,
                        color = if (isSuggested) WhatsAppBrandGreen else Color(0xFFE2E8F0),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Groups,
                                contentDescription = null,
                                tint = if (isSuggested) Color.White else Slate700,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = group.groupName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = bento.textPrimary,
                            fontSize = 13.5.sp
                        )
                        Text(
                            text = group.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = bento.textSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                if (isSuggested) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = WhatsAppBrandGreen,
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = "Sesuai",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Text(
                text = group.description,
                style = MaterialTheme.typography.bodySmall,
                color = Slate600,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )

            Button(
                onClick = onSelect,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = WhatsAppBrandGreen,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_whatsapp),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Kirim ke ${group.groupName}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}
