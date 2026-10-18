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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChecklistItem
import com.example.data.model.PermitCategory
import com.example.ui.components.DigitalSignatureDialog
import com.example.ui.theme.DarkSteelBlue
import com.example.ui.theme.HimcastOrange
import com.example.ui.theme.LightSteelBlue
import com.example.ui.theme.NeutralDark
import com.example.ui.theme.NeutralMedium
import com.example.ui.theme.SafetyGreen
import com.example.ui.theme.SteelBlue
import com.example.ui.viewmodel.PermitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePermitScreen(
    viewModel: PermitViewModel,
    onBack: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()

    var selectedCategory by remember { mutableStateOf(PermitCategory.HOT_WORK) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("Foundry Operations") }
    var contractorName by remember { mutableStateOf("") }
    var contractorSupervisor by remember { mutableStateOf("") }
    var contractorPhone by remember { mutableStateOf("") }
    var numberOfWorkers by remember { mutableIntStateOf(3) }
    var durationHours by remember { mutableIntStateOf(8) }

    var showSignatureDialog by remember { mutableStateOf(false) }
    var signatureBase64 by remember { mutableStateOf("") }
    var signatureRemarks by remember { mutableStateOf("") }

    // Dynamic checklists based on category
    val checklists = remember(selectedCategory) {
        mutableStateListOf<ChecklistItem>().apply {
            addAll(getChecklistForCategory(selectedCategory))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Create Safety Permit-To-Work",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("create_permit_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Section 1: Permit Category Selector
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "1. Select Permit Category",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkSteelBlue
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        PermitCategory.entries.chunked(2).forEach { rowCategories ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowCategories.forEach { category ->
                                    val isSelected = selectedCategory == category
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) SteelBlue else Color(0xFFF0F4F9))
                                            .border(
                                                width = if (isSelected) 1.5.dp else 0.dp,
                                                color = if (isSelected) DarkSteelBlue else Color.Transparent,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable { selectedCategory = category }
                                            .padding(vertical = 10.dp, horizontal = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = category.displayName,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.White else NeutralDark
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Section 2: Work Scope & Location
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "2. Work Site & Scope",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkSteelBlue
                    )

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Work Title (e.g. Furnace Coil Repair)") },
                        modifier = Modifier.fillMaxWidth().testTag("input_work_title"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Detailed Scope of Work") },
                        modifier = Modifier.fillMaxWidth().testTag("input_work_desc"),
                        maxLines = 3
                    )

                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Work Site Location (e.g. Induction Furnace 3, Bay C)") },
                        modifier = Modifier.fillMaxWidth().testTag("input_work_location"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = department,
                        onValueChange = { department = it },
                        label = { Text("Plant Department") },
                        modifier = Modifier.fillMaxWidth().testTag("input_work_department"),
                        singleLine = true
                    )
                }
            }

            // Section 3: Contractor Information
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "3. Contractor Details",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkSteelBlue
                    )

                    OutlinedTextField(
                        value = contractorName,
                        onValueChange = { contractorName = it },
                        label = { Text("Contractor Agency Name") },
                        modifier = Modifier.fillMaxWidth().testTag("input_contractor_name"),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = contractorSupervisor,
                            onValueChange = { contractorSupervisor = it },
                            label = { Text("Contractor Supervisor") },
                            modifier = Modifier.weight(1f).testTag("input_contractor_supervisor"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = contractorPhone,
                            onValueChange = { contractorPhone = it },
                            label = { Text("Supervisor Mobile") },
                            modifier = Modifier.weight(1f).testTag("input_contractor_phone"),
                            singleLine = true
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Number of Workers: $numberOfWorkers", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Row {
                            OutlinedButton(onClick = { if (numberOfWorkers > 1) numberOfWorkers-- }) { Text("-") }
                            Spacer(modifier = Modifier.width(6.dp))
                            OutlinedButton(onClick = { numberOfWorkers++ }) { Text("+") }
                        }
                    }

                    Column {
                        Text(text = "Permit Validity Duration: $durationHours Hours", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Slider(
                            value = durationHours.toFloat(),
                            onValueChange = { durationHours = it.toInt() },
                            valueRange = 1f..24f,
                            steps = 22
                        )
                    }
                }
            }

            // Section 4: Category Checklist
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "4. ${selectedCategory.displayName} Safety Checklist",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkSteelBlue
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    checklists.forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    checklists[index] = item.copy(checked = !item.checked)
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = item.checked,
                                onCheckedChange = { checked ->
                                    checklists[index] = item.copy(checked = checked)
                                },
                                colors = CheckboxDefaults.colors(checkedColor = SteelBlue)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = item.question,
                                fontSize = 12.5.sp,
                                color = NeutralDark,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Section 5: Issuer Digital Signature
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "5. Site Supervisor Digital Signature",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkSteelBlue
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Issuer: ${currentUser?.name ?: "Plant Supervisor"} (${currentUser?.phone ?: "+91 9359610323"})",
                        fontSize = 12.sp,
                        color = NeutralMedium
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (signatureBase64.isNotBlank()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE8F5E9))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = SafetyGreen)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Digital Signature Captured", color = SafetyGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Text(
                                text = "Re-sign",
                                color = SteelBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.clickable { showSignatureDialog = true }
                            )
                        }
                    } else {
                        OutlinedButton(
                            onClick = { showSignatureDialog = true },
                            modifier = Modifier.fillMaxWidth().testTag("open_signature_pad_button")
                        ) {
                            Icon(Icons.Default.Create, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                            Text("Sign Digital Permit Authorization")
                        }
                    }
                }
            }

            // Create Permit Submit Button
            Button(
                onClick = {
                    viewModel.createPermit(
                        title = title.ifBlank { "${selectedCategory.displayName} Job" },
                        description = description.ifBlank { "Standard maintenance procedure" },
                        category = selectedCategory,
                        location = location.ifBlank { "Plant Area 1" },
                        department = department,
                        contractorName = contractorName.ifBlank { "Internal Maintenance" },
                        contractorSupervisor = contractorSupervisor.ifBlank { "On-duty supervisor" },
                        contractorPhone = contractorPhone.ifBlank { "+91 9359610323" },
                        numberOfWorkers = numberOfWorkers,
                        durationHours = durationHours,
                        checklists = checklists.toList(),
                        issuerSignatureBase64 = signatureBase64
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_create_permit_button"),
                colors = ButtonDefaults.buttonColors(containerColor = HimcastOrange)
            ) {
                Text(
                    text = "Submit & Issue Permit-To-Work",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showSignatureDialog) {
        DigitalSignatureDialog(
            roleTitle = currentUser?.role?.displayName ?: "Site Supervisor",
            signerName = currentUser?.name ?: "Site Supervisor",
            signerPhone = currentUser?.phone ?: "+91 9359610323",
            onDismiss = { showSignatureDialog = false },
            onSignatureCaptured = { base64, remarks ->
                signatureBase64 = base64
                signatureRemarks = remarks
                showSignatureDialog = false
            }
        )
    }
}

private fun getChecklistForCategory(category: PermitCategory): List<ChecklistItem> {
    return when (category) {
        PermitCategory.HOT_WORK -> listOf(
            ChecklistItem("1", "Surrounding combustible materials cleared (min 10m radius)", true),
            ChecklistItem("2", "Fire extinguishers (CO2 + DCP) inspected and placed at site", true),
            ChecklistItem("3", "Gas cylinders kept upright, trolley-mounted with flashback arrestors", true),
            ChecklistItem("4", "PPE (leather apron, welding helmet, safety boots) issued", true),
            ChecklistItem("5", "Trained fire watcher assigned at location", true)
        )
        PermitCategory.HEIGHT_WORK -> listOf(
            ChecklistItem("1", "Full body harness inspected with dual lanyards & shock absorber", true),
            ChecklistItem("2", "Scaffolding tagged GREEN (inspected by competent engineer)", true),
            ChecklistItem("3", "Lifeline securely anchored to rigid overhead beam", true),
            ChecklistItem("4", "Hard hat with chin strap and fall arrester worn", true),
            ChecklistItem("5", "Barricading and danger caution boards placed at floor", true)
        )
        PermitCategory.CONFINED_SPACE -> listOf(
            ChecklistItem("1", "Atmospheric gas testing done (O2 between 19.5% and 23.5%)", true),
            ChecklistItem("2", "Forced air ventilation blower running continuously", true),
            ChecklistItem("3", "24V safety handlamp and intrinsically safe torch provided", true),
            ChecklistItem("4", "Dedicated standby person stationed at vessel entry", true),
            ChecklistItem("5", "LOTO isolation verified on all pipes and electrical drives", true)
        )
        PermitCategory.ELECTRICAL -> listOf(
            ChecklistItem("1", "Circuit breaker racked out and physical LOTO padlock applied", true),
            ChecklistItem("2", "Voltage tester used to verify zero potential / de-energized state", true),
            ChecklistItem("3", "Earth discharge stick applied before touching busbars", true),
            ChecklistItem("4", "11kV insulated rubber gloves and arc flash face shield used", true),
            ChecklistItem("5", "Warning tags placed on feeder cubicle", true)
        )
        PermitCategory.COLD_WORK -> listOf(
            ChecklistItem("1", "Non-sparking hand tools inspected and ready", true),
            ChecklistItem("2", "Pipe line drained, depressurized and tagged", true),
            ChecklistItem("3", "Eyewash station and safety shower location verified", true),
            ChecklistItem("4", "Standard PPE (safety shoes, helmet, hand gloves) checked", true)
        )
        PermitCategory.EXCAVATION -> listOf(
            ChecklistItem("1", "Underground utility cables / gas line scanning completed", true),
            ChecklistItem("2", "Trench shoring / sloping provided for depth > 1.2 meters", true),
            ChecklistItem("3", "Safe ladder access / egress provided every 7.5 meters", true),
            ChecklistItem("4", "Spoil heap placed min 1 meter away from edge", true)
        )
    }
}
