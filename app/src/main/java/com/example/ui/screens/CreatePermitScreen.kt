package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryChecklists
import com.example.data.model.ChecklistItem
import com.example.data.model.PermitCategory
import com.example.ui.components.DigitalSignatureDialog
import com.example.ui.components.EmergencyContactBar
import com.example.ui.viewmodel.PermitViewModel
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePermitScreen(
    viewModel: PermitViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()

    var selectedCategory by remember { mutableStateOf(PermitCategory.HOT_WORK) }
    var workTitle by remember { mutableStateOf("") }
    var workDescription by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("Foundry Operations") }
    var contractorName by remember { mutableStateOf("") }
    var workersCountText by remember { mutableStateOf("4") }
    var durationHours by remember { mutableIntStateOf(8) }
    var specialPrecautions by remember { mutableStateOf("") }
    var issuerSignatureBase64 by remember { mutableStateOf<String?>(null) }
    var showSignatureDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Dynamic category-specific checklist
    val checklistState = remember(selectedCategory) {
        mutableStateListOf<ChecklistItem>().apply {
            addAll(CategoryChecklists.getChecklistForCategory(selectedCategory))
        }
    }

    val departmentsList = listOf(
        "Foundry Operations",
        "Mechanical Maintenance",
        "Electrical & Utility",
        "Machine Shop",
        "Civil & Infrastructure",
        "Pattern Shop",
        "Quality & Metallurgy"
    )
    var deptDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Raise Work Permit (PTW)") },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("create_permit_back_button")
                    ) {
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
                    Spacer(modifier = Modifier.height(4.dp))

                    // Step 1: Select Permit Category
                    Text(
                        text = "1. Select Permit Category",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Detailed checklists dynamically adapt to the selected high-risk activity",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // 2x3 Grid of Categories
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        val categories = PermitCategory.values().toList()
                        for (i in categories.indices step 2) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val cat1 = categories[i]
                                CategoryCard(
                                    category = cat1,
                                    isSelected = selectedCategory == cat1,
                                    onClick = { selectedCategory = cat1 },
                                    modifier = Modifier.weight(1f)
                                )
                                if (i + 1 < categories.size) {
                                    val cat2 = categories[i + 1]
                                    CategoryCard(
                                        category = cat2,
                                        isSelected = selectedCategory == cat2,
                                        onClick = { selectedCategory = cat2 },
                                        modifier = Modifier.weight(1f)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Step 2: Work Details
                    Text(
                        text = "2. Work & Site Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            OutlinedTextField(
                                value = workTitle,
                                onValueChange = { workTitle = it },
                                label = { Text("Work Title *") },
                                placeholder = { Text("e.g. Furnace Shell Gas Cutting & Liner Repair") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("permit_work_title_input")
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = workDescription,
                                onValueChange = { workDescription = it },
                                label = { Text("Detailed Scope of Work *") },
                                placeholder = { Text("Specify tools, machinery, and methods to be used...") },
                                minLines = 2,
                                maxLines = 4,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("permit_work_desc_input")
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = location,
                                onValueChange = { location = it },
                                label = { Text("Plant Work Site Location *") },
                                placeholder = { Text("e.g. Induction Furnace 3 / Bay C") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("permit_location_input")
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Department dropdown
                            ExposedDropdownMenuBox(
                                expanded = deptDropdownExpanded,
                                onExpandedChange = { deptDropdownExpanded = !deptDropdownExpanded }
                            ) {
                                OutlinedTextField(
                                    value = department,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Department / Plant Area *") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = deptDropdownExpanded) },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth()
                                        .testTag("permit_department_dropdown")
                                )
                                ExposedDropdownMenu(
                                    expanded = deptDropdownExpanded,
                                    onDismissRequest = { deptDropdownExpanded = false }
                                ) {
                                    departmentsList.forEach { dept ->
                                        DropdownMenuItem(
                                            text = { Text(dept) },
                                            onClick = {
                                                department = dept
                                                deptDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = contractorName,
                                    onValueChange = { contractorName = it },
                                    label = { Text("Contractor / Agency *") },
                                    placeholder = { Text("e.g. Shivam Engineering") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1.4f)
                                        .testTag("permit_contractor_input")
                                )

                                OutlinedTextField(
                                    value = workersCountText,
                                    onValueChange = { workersCountText = it },
                                    label = { Text("Workers *") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(0.8f)
                                        .testTag("permit_workers_count_input")
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Validity duration selector
                            Text(
                                text = "Permit Validity Duration:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(4, 8, 12, 24).forEach { hours ->
                                    FilterChip(
                                        selected = durationHours == hours,
                                        onClick = { durationHours = hours },
                                        label = { Text("$hours Hrs") },
                                        modifier = Modifier.testTag("duration_${hours}h_chip")
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Step 3: Detailed Safety Checklist
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "3. Detailed Safety Checklist (${selectedCategory.shortName})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "All safety prerequisites must be verified prior to permit submission",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Quick "Check All Complied" helper button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = {
                                for (i in checklistState.indices) {
                                    checklistState[i] = checklistState[i].copy(isChecked = true)
                                }
                            },
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Mark All Complied", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Checklist Items
                itemsIndexed(checklistState) { index, item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (item.isChecked) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    checklistState[index] = item.copy(isChecked = !item.isChecked)
                                }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = item.isChecked,
                                onCheckedChange = { checked ->
                                    checklistState[index] = item.copy(isChecked = checked)
                                },
                                modifier = Modifier.testTag("checklist_item_${item.id}")
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${index + 1}. ${item.question}",
                                fontSize = 12.sp,
                                color = if (item.isChecked) Color(0xFF1B5E20) else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (item.isChecked) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))

                    // Special Precautions
                    OutlinedTextField(
                        value = specialPrecautions,
                        onValueChange = { specialPrecautions = it },
                        label = { Text("Special Precautions / PPE Requirements (Optional)") },
                        placeholder = { Text("e.g. Arc shield, FR suit, standby fire extinguisher...") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Step 4: Digital Signature of Issuer
                    Text(
                        text = "4. Permit Requester / Issuer Digital Signature",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Signing Authority: ${currentUser?.name ?: "Concern Person"} (${currentUser?.role ?: "ISSUER"})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (issuerSignatureBase64 != null) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Signed",
                                        tint = Color(0xFF2E7D32),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "✓ Issuer Digital Signature Captured & Stamped",
                                        color = Color(0xFF2E7D32),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                                Button(
                                    onClick = { showSignatureDialog = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text("Re-sign", fontSize = 12.sp)
                                }
                            } else {
                                Button(
                                    onClick = { showSignatureDialog = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("open_issuer_signature_button")
                                ) {
                                    Icon(Icons.Default.Draw, contentDescription = "Sign")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Tap to Digitally Sign Permit", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Submit Button
                    Button(
                        onClick = {
                            if (workTitle.isBlank()) {
                                errorMessage = "Please enter Work Title."
                                return@Button
                            }
                            if (workDescription.isBlank()) {
                                errorMessage = "Please enter Work Description."
                                return@Button
                            }
                            if (location.isBlank()) {
                                errorMessage = "Please enter Work Site Location."
                                return@Button
                            }
                            if (contractorName.isBlank()) {
                                errorMessage = "Please enter Contractor Name."
                                return@Button
                            }
                            val uncheckedCount = checklistState.count { !it.isChecked }
                            if (uncheckedCount > 0) {
                                errorMessage = "All safety checklist items must be checked before raising permit ($uncheckedCount remaining)."
                                return@Button
                            }
                            if (issuerSignatureBase64 == null) {
                                errorMessage = "Digital signature is required from Permit Issuer before submission."
                                return@Button
                            }

                            errorMessage = null
                            val workers = workersCountText.toIntOrNull() ?: 1

                            viewModel.createPermit(
                                category = selectedCategory,
                                workTitle = workTitle,
                                workDescription = workDescription,
                                location = location,
                                department = department,
                                contractorName = contractorName,
                                workersCount = workers,
                                durationHours = durationHours,
                                checklist = checklistState.toList(),
                                specialPrecautions = specialPrecautions,
                                issuerSignature = issuerSignatureBase64!!
                            ) { permitId ->
                                viewModel.navigateTo(Screen.PermitDetail(permitId))
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("submit_permit_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text("Submit Permit & Initiate Approval Chain", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }

    if (showSignatureDialog) {
        DigitalSignatureDialog(
            signerRoleTitle = "Permit Requester / Issuer",
            signerName = currentUser?.name ?: "Concern Person",
            onDismiss = { showSignatureDialog = false },
            onSignatureConfirmed = { signatureBase64 ->
                issuerSignatureBase64 = signatureBase64
                showSignatureDialog = false
            }
        )
    }
}

@Composable
fun CategoryCard(
    category: PermitCategory,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .testTag("category_select_${category.name.lowercase()}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category.shortName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Text(
                text = category.highRiskLevel,
                fontSize = 10.sp,
                color = Color(0xFFC62828),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
