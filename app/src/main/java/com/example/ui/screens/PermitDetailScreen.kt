package com.example.ui.screens

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreTime
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import com.example.data.local.entity.PermitEntity
import com.example.data.model.CategoryChecklists
import com.example.data.model.ChecklistItem
import com.example.data.model.PermitCategory
import com.example.data.model.PermitStatus
import com.example.data.model.UserRole
import com.example.ui.components.DigitalSignatureDialog
import com.example.ui.components.EmergencyContactBar
import com.example.ui.components.PermitStatusBadge
import com.example.ui.pdf.PermitPdfGenerator
import com.example.ui.viewmodel.PermitViewModel
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermitDetailScreen(
    permitId: Long,
    viewModel: PermitViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val allPermits by viewModel.allPermits.collectAsState()
    val permit = allPermits.find { it.id == permitId }

    var showSignDialogForStep by remember { mutableStateOf<String?>(null) }
    var approvalComments by remember { mutableStateOf("") }
    var showExtensionDialog by remember { mutableStateOf(false) }
    var showClosureDialog by remember { mutableStateOf(false) }

    if (permit == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Permit not found.")
        }
        return
    }

    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val now = System.currentTimeMillis()
    val isExpiringSoon = permit.status == PermitStatus.ACTIVE.name && (permit.endTime - now in 1..(2 * 3600 * 1000L))
    val isExpired = permit.status == PermitStatus.ACTIVE.name && permit.endTime <= now

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(permit.permitNumber, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(permit.workTitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f), maxLines = 1)
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("permit_detail_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            PermitPdfGenerator.generateAndSharePermitPdf(context, permit)
                        },
                        modifier = Modifier.testTag("detail_download_pdf_button")
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF", tint = Color(0xFFFFCC80))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            EmergencyContactBar(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(6.dp))

                    // Expiring alert banner if active and near deadline
                    if (isExpiringSoon || isExpired) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFC62828))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (isExpired) "PERMIT EXPIRED" else "PERMIT EXPIRING SOON",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFC62828),
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = if (isExpired) "Valid working hours have elapsed. Request permit extension or proceed to permit closure."
                                        else "Less than 2 hours remaining on this permit. Site team must request an extension or complete closure handover.",
                                        fontSize = 11.sp,
                                        color = Color(0xFFB71C1C)
                                    )
                                }
                            }
                        }
                    }

                    // Key Summary Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = permit.category.replace("_", " "),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                PermitStatusBadge(
                                    statusString = if (isExpiringSoon) PermitStatus.EXPIRING_SOON.name else permit.status
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = permit.workTitle,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = permit.workDescription,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(10.dp))

                            // Grid of details
                            DetailRow("Plant Location", permit.location)
                            DetailRow("Department", permit.department)
                            DetailRow("Contractor Agency", "${permit.contractorName} (${permit.workersCount} workers)")
                            DetailRow("Valid Time Window", "${dateFormat.format(Date(permit.startTime))} to ${dateFormat.format(Date(permit.endTime))}")
                            DetailRow("Issuer / Raised By", "${permit.issuerName} (${permit.issuerMobile})")

                            if (permit.isExtended) {
                                DetailRow("Extension Granted", "+${permit.extendedHours} Hrs by ${permit.extensionApprovedBy ?: "Safety"}")
                                DetailRow("Extension Reason", permit.extensionReason ?: "Operational need")
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Download Official PTW button
                            Button(
                                onClick = {
                                    PermitPdfGenerator.generateAndSharePermitPdf(context, permit)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("download_official_ptw_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF0D2033)
                                )
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF", tint = Color(0xFFFFB74D))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Download / Share Official PTW (PDF)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Extension Action Button (if Active or Expiring)
                    if (permit.status == PermitStatus.ACTIVE.name || isExpiringSoon || isExpired) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showExtensionDialog = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("request_extension_button")
                            ) {
                                Icon(Icons.Default.MoreTime, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Request Extension", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            // Permit Closing Option: Concern person of permit issuer will close the permit
                            Button(
                                onClick = { showClosureDialog = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("request_closure_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF00695C)
                                )
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Close Permit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Approval Chain Title
                    Text(
                        text = "Real-Time Document Tracking & Digital Signatures",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Automated 6-tier approval workflow required for safety compliance",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // 6 Approval Steps in Order:
                // 1. Issuer (Signed)
                // 2. Department Head
                // 3. HR or Security
                // 4. Contractor Acceptance
                // 5. Production Manager
                // 6. Safety Officer (Final)
                item {
                    // Step 1: Issuer
                    ApprovalStepCard(
                        stepNumber = 1,
                        roleTitle = "Permit Requester / Issuer",
                        signerName = permit.issuerName,
                        signatureBase64 = permit.issuerSignature,
                        signedAt = permit.issuerSignedAt,
                        comments = "Work permit initiated with safety checks completed",
                        isCompleted = true,
                        isCurrentPending = false,
                        canSign = false,
                        onSignClick = {}
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Step 2: Concern Department Head
                    val isPendingHod = permit.status == PermitStatus.PENDING_HOD.name
                    val canSignHod = isPendingHod && (currentUser?.role == UserRole.DEPT_HEAD.name || currentUser?.role == UserRole.ADMIN.name)
                    ApprovalStepCard(
                        stepNumber = 2,
                        roleTitle = "Concern Department Head (HOD)",
                        signerName = permit.hodName,
                        signatureBase64 = permit.hodSignature,
                        signedAt = permit.hodSignedAt,
                        comments = permit.hodComments,
                        isCompleted = permit.hodSignature != null,
                        isCurrentPending = isPendingHod,
                        canSign = canSignHod,
                        onSignClick = { showSignDialogForStep = "HOD" }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Step 3: HR / Security Officer
                    val isPendingHr = permit.status == PermitStatus.PENDING_HR_SECURITY.name
                    val canSignHr = isPendingHr && (currentUser?.role == UserRole.HR_SECURITY.name || currentUser?.role == UserRole.ADMIN.name)
                    ApprovalStepCard(
                        stepNumber = 3,
                        roleTitle = "HR / Security Officer",
                        signerName = permit.hrSecurityName,
                        signatureBase64 = permit.hrSecuritySignature,
                        signedAt = permit.hrSecuritySignedAt,
                        comments = permit.hrSecurityComments,
                        isCompleted = permit.hrSecuritySignature != null,
                        isCurrentPending = isPendingHr,
                        canSign = canSignHr,
                        onSignClick = { showSignDialogForStep = "HR_SECURITY" }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Step 4: Contractor Acceptance
                    val isPendingContractor = permit.status == PermitStatus.PENDING_CONTRACTOR_ACCEPTANCE.name
                    val canSignContractor = isPendingContractor && (currentUser?.role == UserRole.CONTRACTOR.name || currentUser?.role == UserRole.ADMIN.name)
                    ApprovalStepCard(
                        stepNumber = 4,
                        roleTitle = "Contractor Safety Acceptance",
                        signerName = permit.contractorSignerName ?: permit.contractorName,
                        signatureBase64 = permit.contractorSignature,
                        signedAt = permit.contractorSignedAt,
                        comments = permit.contractorComments,
                        isCompleted = permit.contractorSignature != null,
                        isCurrentPending = isPendingContractor,
                        canSign = canSignContractor,
                        onSignClick = { showSignDialogForStep = "CONTRACTOR" }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Step 5: Production Manager
                    val isPendingProd = permit.status == PermitStatus.PENDING_PRODUCTION_MGR.name
                    val canSignProd = isPendingProd && (currentUser?.role == UserRole.PRODUCTION_MGR.name || currentUser?.role == UserRole.ADMIN.name)
                    ApprovalStepCard(
                        stepNumber = 5,
                        roleTitle = "Production Manager",
                        signerName = permit.productionMgrName,
                        signatureBase64 = permit.productionMgrSignature,
                        signedAt = permit.productionMgrSignedAt,
                        comments = permit.productionMgrComments,
                        isCompleted = permit.productionMgrSignature != null,
                        isCurrentPending = isPendingProd,
                        canSign = canSignProd,
                        onSignClick = { showSignDialogForStep = "PRODUCTION_MGR" }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Step 6: Safety Officer (Final Authorization)
                    val isPendingSafety = permit.status == PermitStatus.PENDING_SAFETY_OFFICER.name
                    val canSignSafety = isPendingSafety && (currentUser?.role == UserRole.SAFETY_OFFICER.name || currentUser?.role == UserRole.ADMIN.name)
                    ApprovalStepCard(
                        stepNumber = 6,
                        roleTitle = "Safety Officer (Final Authorization)",
                        signerName = permit.safetyOfficerName,
                        signatureBase64 = permit.safetyOfficerSignature,
                        signedAt = permit.safetyOfficerSignedAt,
                        comments = permit.safetyOfficerComments,
                        isCompleted = permit.safetyOfficerSignature != null,
                        isCurrentPending = isPendingSafety,
                        canSign = canSignSafety,
                        onSignClick = { showSignDialogForStep = "SAFETY_OFFICER" }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Closure Section if requested or closed
                    if (permit.closureRequestedAt != null || permit.status == PermitStatus.CLOSED.name) {
                        ClosureTrackingCard(
                            permit = permit,
                            currentUser = currentUser,
                            onHodApproveClosure = { sig, comm ->
                                viewModel.approveClosureByHod(permit.id, sig, comm)
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Safety Checklist Inspection section
                    Text(
                        text = "Verified Safety Checklist",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Render Checklist Items
                val checklist = parseChecklistItems(permit.checklistJson)
                itemsIndexed(checklist) { idx, item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${idx + 1}. ${item.question}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }

    // Modal Signature Dialog
    if (showSignDialogForStep != null) {
        val roleTitle = when (showSignDialogForStep) {
            "HOD" -> "Concern Department Head"
            "HR_SECURITY" -> "HR / Security Officer"
            "CONTRACTOR" -> "Contractor Representative"
            "PRODUCTION_MGR" -> "Production Manager"
            "SAFETY_OFFICER" -> "Safety Officer (Final Authorization)"
            else -> "Authorized Signer"
        }

        DigitalSignatureDialog(
            signerRoleTitle = roleTitle,
            signerName = currentUser?.name ?: "Authorized Officer",
            onDismiss = { showSignDialogForStep = null },
            onSignatureConfirmed = { signatureBase64 ->
                when (showSignDialogForStep) {
                    "HOD" -> viewModel.approveByHod(permit.id, signatureBase64, approvalComments)
                    "HR_SECURITY" -> viewModel.approveByHrSecurity(permit.id, signatureBase64, approvalComments)
                    "CONTRACTOR" -> viewModel.acceptByContractor(permit.id, signatureBase64, approvalComments)
                    "PRODUCTION_MGR" -> viewModel.approveByProductionMgr(permit.id, signatureBase64, approvalComments)
                    "SAFETY_OFFICER" -> viewModel.authorizeBySafetyOfficer(permit.id, signatureBase64, approvalComments)
                }
                showSignDialogForStep = null
                approvalComments = ""
            }
        )
    }

    // Extension Dialog
    if (showExtensionDialog) {
        ExtensionDialog(
            onDismiss = { showExtensionDialog = false },
            onConfirm = { hours, reason ->
                viewModel.requestExtension(permit.id, hours, reason)
                showExtensionDialog = false
            }
        )
    }

    // Permit Closure Dialog
    if (showClosureDialog) {
        ClosureDialog(
            userName = currentUser?.name ?: "Issuer",
            onDismiss = { showClosureDialog = false },
            onConfirm = { signature, checklist, comments ->
                viewModel.requestClosureByIssuer(permit.id, signature, checklist, comments)
                showClosureDialog = false
            }
        )
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun ApprovalStepCard(
    stepNumber: Int,
    roleTitle: String,
    signerName: String?,
    signatureBase64: String?,
    signedAt: Long?,
    comments: String?,
    isCompleted: Boolean,
    isCurrentPending: Boolean,
    canSign: Boolean,
    onSignClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("approval_step_$stepNumber"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) Color(0xFFF1F8E9)
            else if (isCurrentPending) Color(0xFFFFF8E1)
            else MaterialTheme.colorScheme.surface
        ),
        border = if (isCurrentPending) androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFA000)) else null
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(
                                if (isCompleted) Color(0xFF2E7D32)
                                else if (isCurrentPending) Color(0xFFE65100)
                                else Color(0xFFB0BEC5)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        } else {
                            Text("$stepNumber", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = roleTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (signerName != null) {
                            Text(
                                text = "Signer: $signerName",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (isCompleted) {
                    Text("✓ Signed", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                } else if (isCurrentPending) {
                    Text("Pending Approval", color = Color(0xFFE65100), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                } else {
                    Text("Upcoming", color = Color.Gray, fontSize = 11.sp)
                }
            }

            if (isCompleted && signatureBase64 != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Digital signature preview
                    val bitmap = remember(signatureBase64) {
                        try {
                            val bytes = Base64.decode(signatureBase64, Base64.DEFAULT)
                            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        } catch (e: Exception) {
                            null
                        }
                    }

                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Signature",
                            modifier = Modifier
                                .height(38.dp)
                                .width(90.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .border(1.dp, Color(0xFFCFD8DC), RoundedCornerShape(4.dp))
                                .background(Color.White)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Digitally Verified",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                        if (signedAt != null) {
                            Text(
                                text = dateFormat.format(Date(signedAt)),
                                fontSize = 10.sp,
                                color = Color(0xFF546E7A)
                            )
                        }
                    }
                }
            }

            if (canSign) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onSignClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("sign_step_button_$stepNumber"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.Draw, contentDescription = "Sign")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Sign & Authorize This Step", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun ClosureTrackingCard(
    permit: PermitEntity,
    currentUser: com.example.data.local.entity.UserEntity?,
    onHodApproveClosure: (sig: String, comm: String) -> Unit
) {
    var showHodClosureSignDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (permit.status == PermitStatus.CLOSED.name) Color(0xFFE0F2F1) else Color(0xFFFFF3E0)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "Permit Closing Workflow",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = if (permit.status == PermitStatus.CLOSED.name) Color(0xFF004D40) else Color(0xFFE65100)
            )
            Text(
                text = "Concern person of permit issuer initiates closing -> Concern Department Head grants final closure approval",
                fontSize = 11.sp,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Issuer Closure
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Issuer Handover & Housekeeping: Signed", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(6.dp))

            // HOD Closure
            if (permit.closureHodSignature != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("HOD Closing Authorization: Safely Closed & Archived", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            } else {
                Text(
                    text = "Awaiting HOD Closing Sign-off",
                    fontSize = 12.sp,
                    color = Color(0xFFE65100),
                    fontWeight = FontWeight.Bold
                )

                if (currentUser?.role == UserRole.DEPT_HEAD.name || currentUser?.role == UserRole.ADMIN.name) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { showHodClosureSignDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .testTag("hod_closure_sign_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00695C))
                    ) {
                        Icon(Icons.Default.Draw, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("HOD Sign & Authorize Final Closure", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }

    if (showHodClosureSignDialog) {
        DigitalSignatureDialog(
            signerRoleTitle = "Concern Department Head (Closure)",
            signerName = currentUser?.name ?: "HOD",
            onDismiss = { showHodClosureSignDialog = false },
            onSignatureConfirmed = { signature ->
                onHodApproveClosure(signature, "Site inspected, machinery clear, LOTO removed. Closure approved.")
                showHodClosureSignDialog = false
            }
        )
    }
}

@Composable
fun ExtensionDialog(
    onDismiss: () -> Unit,
    onConfirm: (hours: Int, reason: String) -> Unit
) {
    var selectedHours by remember { mutableIntStateOf(4) }
    var reason by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Request Work Permit Extension",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Extend validity window for ongoing safety-cleared operations",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Additional Hours:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(2, 4, 8, 12).forEach { h ->
                        FilterChip(
                            selected = selectedHours == h,
                            onClick = { selectedHours = h },
                            label = { Text("+$h Hrs") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Operational Reason for Extension *") },
                    placeholder = { Text("e.g. Additional welding & slag grinding required...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (reason.isNotBlank()) onConfirm(selectedHours, reason)
                        },
                        enabled = reason.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("Grant Extension", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ClosureDialog(
    userName: String,
    onDismiss: () -> Unit,
    onConfirm: (signature: String, checklist: List<ChecklistItem>, comments: String) -> Unit
) {
    val closureItems = remember {
        mutableStateListOf<ChecklistItem>().apply {
            addAll(CategoryChecklists.getClosureChecklist())
        }
    }
    var comments by remember { mutableStateOf("Work completed cleanly. Area safe.") }
    var signatureBase64 by remember { mutableStateOf<String?>(null) }
    var showSignatureDialog by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            LazyColumn(modifier = Modifier.padding(16.dp)) {
                item {
                    Text(
                        text = "Permit Closure & Handover (Issuer)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00695C)
                    )
                    Text(
                        text = "Confirm all post-work safety measures before handing over to HOD:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                itemsIndexed(closureItems) { idx, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { closureItems[idx] = item.copy(isChecked = !item.isChecked) }
                            .padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = item.isChecked,
                            onCheckedChange = { closureItems[idx] = item.copy(isChecked = it) }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(item.question, fontSize = 11.sp)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(10.dp))

                    if (signatureBase64 != null) {
                        Text("✓ Issuer Signature Captured", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    } else {
                        Button(
                            onClick = { showSignatureDialog = true },
                            modifier = Modifier.fillMaxWidth().height(42.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00695C))
                        ) {
                            Icon(Icons.Default.Draw, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Issuer Sign Closure Handover", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(onClick = onDismiss) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (signatureBase64 != null) {
                                    onConfirm(signatureBase64!!, closureItems.toList(), comments)
                                }
                            },
                            enabled = signatureBase64 != null && closureItems.all { it.isChecked },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00695C))
                        ) {
                            Text("Submit Closure", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showSignatureDialog) {
        DigitalSignatureDialog(
            signerRoleTitle = "Permit Requester / Issuer (Closure Handover)",
            signerName = userName,
            onDismiss = { showSignatureDialog = false },
            onSignatureConfirmed = { sig ->
                signatureBase64 = sig
                showSignatureDialog = false
            }
        )
    }
}

private fun parseChecklistItems(jsonStr: String): List<ChecklistItem> {
    val list = mutableListOf<ChecklistItem>()
    try {
        val arr = JSONArray(jsonStr)
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            list.add(
                ChecklistItem(
                    id = obj.optString("id", "chk_$i"),
                    question = obj.optString("question", "Safety compliance verified"),
                    isChecked = obj.optBoolean("isChecked", true),
                    remarks = obj.optString("remarks", "")
                )
            )
        }
    } catch (_: Exception) {}
    return list
}
