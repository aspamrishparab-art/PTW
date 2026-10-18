package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.UserRole
import com.example.ui.components.DigitalSignaturePad
import com.example.ui.components.EmergencyContactBar
import com.example.ui.viewmodel.PermitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignatureSignOffScreen(
    permitId: Long,
    roleToSign: UserRole,
    viewModel: PermitViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val allPermits by viewModel.allPermits.collectAsState()

    val permit = allPermits.find { it.id == permitId }

    BackHandler {
        viewModel.navigateBack()
    }

    var comments by remember { mutableStateOf("") }
    var contractorAcceptedTerms by remember { mutableStateOf(roleToSign != UserRole.CONTRACTOR) }

    if (permit == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Work permit not found.")
        }
        return
    }

    val signerName = currentUser?.name ?: roleToSign.displayName

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Sign-off: ${roleToSign.displayName}", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(6.dp))
                EmergencyContactBar()
            }

            // Permit Summary Card with Himcast Logo
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.himcast_logo),
                                contentDescription = "HIMCAST Logo",
                                modifier = Modifier
                                    .height(36.dp)
                                    .width(110.dp),
                                contentScale = ContentScale.Fit
                            )
                            Text(
                                text = permit.permitNumber,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = permit.workTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "📍 Location: ${permit.location} • Contractor: ${permit.contractorName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Specific Role Instructions / Contractor Declaration
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (roleToSign == UserRole.CONTRACTOR) Color(0xFFFFF8E1) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (roleToSign == UserRole.CONTRACTOR) Icons.Default.Info else Icons.Default.Shield,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (roleToSign) {
                                    UserRole.CONTRACTOR -> "Contractor Work Permit Acceptance Undertaking"
                                    UserRole.DEPT_HEAD -> "Department Head Operational Review"
                                    UserRole.HR_SECURITY -> "HR & Plant Security Clearance"
                                    UserRole.PRODUCTION_MGR -> "Production Line Clearance"
                                    UserRole.SAFETY_OFFICER -> "Safety Officer Final Authorization"
                                    else -> "Authorization Sign-off"
                                },
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = when (roleToSign) {
                                UserRole.CONTRACTOR -> "As the Contractor Representative, I have physically inspected the site, verified safety precautions, and confirm that all workers are equipped with approved PPE and will abide by Himgiri Castings Pvt. Ltd. safety guidelines."
                                UserRole.DEPT_HEAD -> "I verify that the work will not endanger adjacent foundry equipment and that necessary department isolations are in place."
                                UserRole.HR_SECURITY -> "Gate pass verified for contractor workmen, attendance logged, and physical/medical fitness cleared."
                                UserRole.PRODUCTION_MGR -> "Plant production schedule coordinated. Electrical and process interlocking confirmed safe."
                                UserRole.SAFETY_OFFICER -> "Site inspected, all 5 preliminary signatures verified, firefighting apparatus standby, permit officially authorized."
                                else -> "I certify the safety verification of this work permit."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (roleToSign == UserRole.CONTRACTOR) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = contractorAcceptedTerms,
                                    onCheckedChange = { contractorAcceptedTerms = it },
                                    modifier = Modifier.testTag("contractor_terms_checkbox")
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "I accept the safety conditions & assume responsibility for contractor workforce compliance.",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE65100)
                                )
                            }
                        }
                    }
                }
            }

            // Comments / Notes
            item {
                OutlinedTextField(
                    value = comments,
                    onValueChange = { comments = it },
                    label = { Text("Signer Remarks / Review Notes") },
                    placeholder = {
                        Text(
                            when (roleToSign) {
                                UserRole.CONTRACTOR -> "e.g. 4 certified welders deployed with double lanyard harnesses"
                                UserRole.DEPT_HEAD -> "e.g. Induction furnace #2 cold and isolated"
                                UserRole.HR_SECURITY -> "e.g. 4 gate passes cleared & safety helmet verified"
                                UserRole.PRODUCTION_MGR -> "e.g. Overhead crane movement restricted near bay 2"
                                UserRole.SAFETY_OFFICER -> "e.g. Atmospheric test LEL 0%, fire watch standby"
                                else -> "Notes..."
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }

            // Digital Signature Pad
            item {
                DigitalSignaturePad(
                    signerRoleTitle = roleToSign.displayName,
                    signerName = signerName,
                    onSignatureConfirmed = { signatureHash ->
                        if (!contractorAcceptedTerms) {
                            Toast.makeText(context, "Please check and accept the safety undertaking checkbox first", Toast.LENGTH_SHORT).show()
                            return@DigitalSignaturePad
                        }

                        when (roleToSign) {
                            UserRole.DEPT_HEAD -> {
                                viewModel.approveHod(permit.id, signatureHash, comments.ifBlank { "Department approved" }) {
                                    Toast.makeText(context, "HOD approved! Forwarded to HR/Security.", Toast.LENGTH_SHORT).show()
                                    viewModel.navigateBack()
                                }
                            }
                            UserRole.HR_SECURITY -> {
                                viewModel.approveHrSecurity(permit.id, signatureHash, comments.ifBlank { "Gate pass & fitness cleared" }) {
                                    Toast.makeText(context, "HR & Security cleared! Forwarded to Production Mgr.", Toast.LENGTH_SHORT).show()
                                    viewModel.navigateBack()
                                }
                            }
                            UserRole.PRODUCTION_MGR -> {
                                viewModel.approveProductionMgr(permit.id, signatureHash, comments.ifBlank { "Production clearance granted" }) {
                                    Toast.makeText(context, "Production cleared! Awaiting Contractor Acceptance.", Toast.LENGTH_SHORT).show()
                                    viewModel.navigateBack()
                                }
                            }
                            UserRole.CONTRACTOR -> {
                                viewModel.acceptContractor(permit.id, signerName, signatureHash, comments.ifBlank { "Accepted all safety conditions" }) {
                                    Toast.makeText(context, "Contractor accepted! Forwarded to Safety Officer.", Toast.LENGTH_SHORT).show()
                                    viewModel.navigateBack()
                                }
                            }
                            UserRole.SAFETY_OFFICER -> {
                                viewModel.authorizeSafetyOfficer(permit.id, signatureHash, comments.ifBlank { "Site verified & officially issued" }) {
                                    Toast.makeText(context, "Permit officially authorized & active! PDF generated.", Toast.LENGTH_LONG).show()
                                    viewModel.navigateBack()
                                }
                            }
                            else -> {
                                viewModel.navigateBack()
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
