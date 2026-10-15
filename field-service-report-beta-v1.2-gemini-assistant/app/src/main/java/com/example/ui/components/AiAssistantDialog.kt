package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.model.FieldServiceReport
import com.example.util.AiAssistantClient
import kotlinx.coroutines.launch

private val aiActions = listOf(
    "Perbaiki kalimat temuan",
    "Susun catatan menjadi temuan",
    "Periksa kelengkapan laporan",
    "Ringkas laporan untuk supervisor"
)

private val targetFields = listOf(
    "Defect 1 Findings",
    "Defect 2 Findings",
    "Hourmeter Findings",
    "History",
    "Completed Action",
    "Conclusion 1",
    "Conclusion 2",
    "Conclusion 3"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantDialog(
    report: FieldServiceReport,
    onDismiss: () -> Unit,
    onInsert: (field: String, text: String) -> Unit
) {
    var action by remember { mutableStateOf(aiActions.first()) }
    var sourceText by remember { mutableStateOf(report.defect1Findings) }
    var target by remember { mutableStateOf(targetFields.first()) }
    var targetExpanded by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = { if (!loading) onDismiss() },
        title = { Text("FSR AI Assistant") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Teks laporan yang dipilih dan konteks teknis terpilih akan dikirim ke server AI. Jangan masukkan informasi rahasia. Semua hasil harus diperiksa mekanik.",
                    style = MaterialTheme.typography.bodySmall
                )
                Text("Aksi AI", style = MaterialTheme.typography.labelLarge)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    aiActions.forEach { option ->
                        if (action == option) {
                            Button(onClick = { action = option }, modifier = Modifier.fillMaxWidth()) {
                                Text(option)
                            }
                        } else {
                            TextButton(
                                onClick = {
                                    action = option
                                    result = ""
                                    error = ""
                                    if (option == "Ringkas laporan untuk supervisor") target = "Conclusion 1"
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(option)
                            }
                        }
                    }
                }
                OutlinedTextField(
                    value = sourceText,
                    onValueChange = { sourceText = it },
                    label = { Text("Catatan sumber (opsional)") },
                    placeholder = { Text("Masukkan catatan yang ingin dibantu AI") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 6
                )
                ExposedDropdownMenuBox(
                    expanded = targetExpanded,
                    onExpandedChange = { targetExpanded = !targetExpanded }
                ) {
                    OutlinedTextField(
                        value = target,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Masukkan hasil ke kolom") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = targetExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = targetExpanded,
                        onDismissRequest = { targetExpanded = false }
                    ) {
                        targetFields.forEach { field ->
                            DropdownMenuItem(
                                text = { Text(field) },
                                onClick = {
                                    target = field
                                    targetExpanded = false
                                }
                            )
                        }
                    }
                }
                Button(
                    onClick = {
                        loading = true
                        error = ""
                        result = ""
                        scope.launch {
                            when (val response = AiAssistantClient.assist(action, report, sourceText)) {
                                is AiAssistantClient.Result.Success -> result = response.text
                                is AiAssistantClient.Result.Failure -> error = response.message
                            }
                            loading = false
                        }
                    },
                    enabled = !loading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (loading) {
                        CircularProgressIndicator()
                        Spacer(Modifier.padding(4.dp))
                    }
                    Text(if (loading) "Memproses..." else "Jalankan AI")
                }
                if (error.isNotBlank()) {
                    Text(error, color = MaterialTheme.colorScheme.error)
                }
                if (result.isNotBlank()) {
                    Text(
                        if (action == "Periksa kelengkapan laporan")
                            "Hasil pemeriksaan — rekomendasi saja, tidak akan menimpa kolom laporan"
                        else "Pratinjau hasil — periksa sebelum digunakan",
                        style = MaterialTheme.typography.labelLarge
                    )
                    OutlinedTextField(
                        value = result,
                        onValueChange = { result = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        maxLines = 10
                    )
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (result.isNotBlank() && action != "Periksa kelengkapan laporan") {
                    TextButton(onClick = { onInsert(target, result); onDismiss() }) {
                        Text("Masukkan")
                    }
                }
                TextButton(onClick = onDismiss, enabled = !loading) { Text("Tutup") }
            }
        }
    )
}
