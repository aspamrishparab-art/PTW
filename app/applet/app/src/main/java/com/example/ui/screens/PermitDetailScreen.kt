package com.example.ui.screens

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ChecklistItem
import com.example.data.model.DigitalSignature
import com.example.data.model.PermitStatus
import com.example.data.model.UserRole
import com.example.ui.components.DigitalSignatureDialog
import com.example.ui.components.EmergencyContactBar
import com.example.ui.components.PermitStatusBadge
import com.example.ui.pdf.PermitPdfGenerator
import com.example.ui.theme.DarkSteelBlue
import com.example.ui.theme.HimcastOrange
import com.example.ui.theme.LightSteelBlue
import com.example.ui.theme.NeutralDark
import com.example.ui.theme.NeutralMedium
import com.example.ui.theme.SafetyGreen
import com.example.ui.theme.SafetyGreenBg
import com.example.ui.theme.SteelBlue
import com.example.ui.viewmodel.PermitViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermitDetailScreen(
    permitId: Long,
    viewModel: PermitViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val allPermits by viewModel.allPermits.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val permit = allPermits.find { it.id == permitId }

    var showSignDialogForRole by remember { mutableStateOf<String?>(null) }
    var showExtensionDialog by remember { mutableStateOf(false) }
    var showCloseDialog by remember { mutableStateOf(false) }

    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    val gson = remember { Gson() }

    if (permit == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Permit not found", color = NeutralDark)
        }
        return
    }

    val sigType = object : TypeToken<List<DigitalSignature>>() {}.type
    val signatures: List<DigitalSignature> = try {
        gson.fromJson(permit.signaturesJson, sigType) ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }

    val checkType = object : TypeToken<List<ChecklistItem>>() {}.type
    val checklists: List<ChecklistItem> = try {
        gson.fromJson(permit.checklistJson, checkType) ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }

    val now = System.currentTimeMillis()
    val isExpired = permit.endTime < now || permit.status == PermitStatus.EXPIRED

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = permit.permitNumber,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = permit.title,
                            fontSize = 11.sp,
                            color = LightSteelBlue,
                            maxLines = 1
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("permit_detail_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { PermitPdfGenerator.generateAndSharePdf(context, permit) },
                        modifier = Modifier.testTag("detail_pdf_export_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "Export PDF",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSteelBlue)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF6F8FC))
                .verticalScroll(rememberScrollState())
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            EmergencyContactBar()

            // Header Banner with Status Badge
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(LightSteelBlue)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = permit.category.displayName,
                                color = DarkSteelBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        PermitStatusBadge(
                            status = if (isExpired && !permit.isClosed && permit.status != PermitStatus.REJECTED) PermitStatus.EXPIRED else permit.status
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = permit.title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeutralDark
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = permit.description,
                        fontSize = 13.sp,
                        color = NeutralMedium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Location & Department
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Place, contentDescription = null, tint = HimcastOrange, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${permit.location} • Dept: ${permit.department}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = NeutralDark
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Contractor Info
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Engineering, contentDescription = null, tint = SteelBlue, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Contractor: ${permit.contractorName} (${permit.contractorSupervisor}, ${permit.contractorPhone})",
                            fontSize = 13.sp,
                            color = NeutralDark
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Validity Window
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF0F4F9))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = SteelBlue, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Start: ${dateFormat.format(Date(permit.startTime))}",
                                fontSize = 11.5.sp,
                                color = NeutralDark
                            )
                            Text(
                                text = "Expires: ${dateFormat.format(Date(permit.endTime))}",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isExpired) Color.Red else DarkSteelBlue
                            )
                        }
                    }
                }
            }

            // Multi-Tier Digital Acceptance & Signatures
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Multi-Tier Digital Approvals",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkSteelBlue
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Signatures required for full authorization & safety compliance",
                        fontSize = 11.sp,
                        color = NeutralMedium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Tier 1: Issuer
                    ApprovalStepItem(
                        tierName = "Site Supervisor (Issuer)",
                        expectedSignerRole = UserRole.SUPERVISOR,
                        matchedSignature = signatures.find { it.roleName.contains("Supervisor", ignoreCase = true) || it.roleName.contains("Issuer", ignoreCase = true) },
                        onSignClick = { showSignDialogForRole = "Site Supervisor (Issuer)" }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Tier 2: Dept Head
                    ApprovalStepItem(
                        tierName = "Department Head",
                        expectedSignerRole = UserRole.DEPT_HEAD,
                        matchedSignature = signatures.find { it.roleName.contains("Department", ignoreCase = true) },
                        onSignClick = { showSignDialogForRole = "Department Head" }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Tier 3: Safety Officer
                    ApprovalStepItem(
                        tierName = "Safety Officer (Final Clearance)",
                        expectedSignerRole = UserRole.SAFETY_OFFICER,
                        matchedSignature = signatures.find { it.roleName.contains("Safety", ignoreCase = true) },
                        onSignClick = { showSignDialogForRole = "Safety Officer" }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Tier 4: Contractor Acceptance
                    ApprovalStepItem(
                        tierName = "Contractor Representative Acceptance",
                        expectedSignerRole = UserRole.CONTRACTOR,
                        matchedSignature = signatures.find { it.roleName.contains("Contractor", ignoreCase = true) },
                        onSignClick = { showSignDialogForRole = "Contractor Representative" }
                    )
                }
            }

            // Safety Checklist Verification
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Verified Precautions (${permit.category.displayName})",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkSteelBlue
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    checklists.forEachIndexed { idx, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (item.checked) Icons.Default.CheckCircle else Icons.Default.HourglassTop,
                                contentDescription = null,
                                tint = if (item.checked) SafetyGreen else NeutralMedium,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${idx + 1}. ${item.question}",
                                fontSize = 12.sp,
                                color = NeutralDark
                            )
                        }
                    }
                }
            }

            // Quick Actions: PDF Export, Extension, Closure
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Permit Actions",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkSteelBlue
                    )

                    // Export PDF Button
                    Button(
                        onClick = { PermitPdfGenerator.generateAndSharePdf(context, permit) },
                        modifier = Modifier.fillMaxWidth().testTag("export_pdf_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = SteelBlue)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                        Text("Download / Share Official PTW (PDF)", fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showExtensionDialog = true },
                            modifier = Modifier.weight(1f).testTag("request_extension_button")
                        ) {
                            Icon(Icons.Default.Update, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                            Text("Request Extension")
                        }

                        if (!permit.isClosed) {
                            Button(
                                onClick = { showCloseDialog = true },
                                modifier = Modifier.weight(1f).testTag("close_permit_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = DarkSteelBlue)
                            ) {
                                Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                                Text("Close Permit")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Digital Signature Modal
    if (showSignDialogForRole != null) {
        val roleTitle = showSignDialogForRole!!
        DigitalSignatureDialog(
            roleTitle = roleTitle,
            signerName = currentUser?.name ?: roleTitle,
            signerPhone = currentUser?.phone ?: "+91 9359610323",
            onDismiss = { showSignDialogForRole = null },
            onSignatureCaptured = { base64, remarks ->
                val isFinal = roleTitle.contains("Safety", ignoreCase = true)
                viewModel.addDigitalSignature(
                    permitId = permit.id,
                    roleTitle = roleTitle,
                    signerName = currentUser?.name ?: roleTitle,
                    signerPhone = currentUser?.phone ?: "+91 9359610323",
                    signatureBase64 = base64,
                    remarks = remarks,
                    isFinalApproval = isFinal
                )
                showSignDialogForRole = null
            }
        )
    }

    // Request Extension Dialog
    if (showExtensionDialog) {
        var addHours by remember { mutableIntStateOf(4) }
        var extReason by remember { mutableStateOf("Refractory cure time extended") }

        Dialog(onDismissRequest = { showExtensionDialog = false }) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Request Permit Extension", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkSteelBlue)

                    Text("Additional Hours: $addHours hrs", fontSize = 13.sp)
                    Slider(
                        value = addHours.toFloat(),
                        onValueChange = { addHours = it.toInt() },
                        valueRange = 1f..12f,
                        steps = 10
                    )

                    OutlinedTextField(
                        value = extReason,
                        onValueChange = { extReason = it },
                        label = { Text("Reason for Extension") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(onClick = { showExtensionDialog = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.requestExtension(permit.id, addHours, extReason)
                                showExtensionDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = HimcastOrange)
                        ) {
                            Text("Confirm Extension")
                        }
                    }
                }
            }
        }
    }

    // Close Permit Dialog
    if (showCloseDialog) {
        var closeReason by remember { mutableStateOf("Work completed satisfactorily, tools removed and area cleaned.") }

        Dialog(onDismissRequest = { showCloseDialog = false }) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Safely Close Permit", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkSteelBlue)
                    Text("Verify all workers evacuated, LOTO locks removed, and housekeeping completed.", fontSize = 12.sp, color = NeutralMedium)

                    OutlinedTextField(
                        value = closeReason,
                        onValueChange = { closeReason = it },
                        label = { Text("Closure Notes / Handover") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(onClick = { showCloseDialog = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.closePermit(permit.id, closeReason)
                                showCloseDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSteelBlue)
                        ) {
                            Text("Confirm Closure")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ApprovalStepItem(
    tierName: String,
    expectedSignerRole: UserRole,
    matchedSignature: DigitalSignature?,
    onSignClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd-MMM HH:mm", Locale.getDefault())

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (matchedSignature != null) SafetyGreenBg else Color(0xFFF8F9FA))
            .border(1.dp, if (matchedSignature != null) SafetyGreen.copy(alpha = 0.3f) else Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = tierName,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (matchedSignature != null) SafetyGreen else NeutralDark
            )

            if (matchedSignature != null) {
                Text(
                    text = "Signed by ${matchedSignature.signerName} • ${dateFormat.format(Date(matchedSignature.timestamp))}",
                    fontSize = 11.sp,
                    color = SafetyGreen
                )
                if (matchedSignature.remarks.isNotBlank()) {
                    Text(
                        text = "Notes: ${matchedSignature.remarks}",
                        fontSize = 11.sp,
                        color = NeutralMedium
                    )
                }
            } else {
                Text(
                    text = "Pending digital authorization",
                    fontSize = 11.sp,
                    color = NeutralMedium
                )
            }
        }

        if (matchedSignature != null) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Signed",
                tint = SafetyGreen,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Button(
                onClick = onSignClick,
                colors = ButtonDefaults.buttonColors(containerColor = SteelBlue),
                modifier = Modifier.testTag("sign_button_${tierName.replace(" ", "_").lowercase()}")
            ) {
                Text("Sign", fontSize = 12.sp)
            }
        }
    }
}
