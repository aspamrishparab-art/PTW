package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.EmergencyContactBar
import com.example.ui.theme.DarkSteelBlue
import com.example.ui.theme.HimcastOrange
import com.example.ui.theme.LightSteelBlue
import com.example.ui.theme.NeutralDark
import com.example.ui.theme.NeutralMedium
import com.example.ui.theme.SteelBlue
import com.example.ui.viewmodel.PermitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: PermitViewModel,
    onBack: () -> Unit
) {
    val allUsers by viewModel.allUsers.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var mobileInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("Safety@himcast.in") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sign In & User Personas", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("login_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSteelBlue)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF6F8FC))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            EmergencyContactBar()

            // Company Logo & Title
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.himcast_logo),
                        contentDescription = "Logo",
                        modifier = Modifier.size(52.dp).clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text("Himgiri Castings Pvt. Ltd.", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkSteelBlue)
                        Text("Permit-To-Work Authorization Portal", fontSize = 12.sp, color = NeutralMedium)
                    }
                }
            }

            // Quick Role Switcher (For Testing & Multi-User Operations)
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Quick Persona Switcher",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkSteelBlue
                    )
                    Text(
                        text = "Switch active role instantly to test multi-tier approval flows:",
                        fontSize = 12.sp,
                        color = NeutralMedium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    allUsers.forEach { user ->
                        val isSelected = currentUser?.id == user.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) LightSteelBlue else Color(0xFFF8F9FA))
                                .clickable {
                                    viewModel.selectUser(user)
                                    onBack()
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = if (isSelected) SteelBlue else NeutralMedium,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "${user.name} (${user.role.displayName})",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (isSelected) DarkSteelBlue else NeutralDark
                                    )
                                    Text(
                                        text = "Phone: ${user.phone} • ${user.department}",
                                        fontSize = 11.sp,
                                        color = NeutralMedium
                                    )
                                }
                            }

                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = "Active", tint = SteelBlue)
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }

            // Mobile Sign-In Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Individual Mobile Number Sign-In",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkSteelBlue
                    )

                    OutlinedTextField(
                        value = mobileInput,
                        onValueChange = { mobileInput = it },
                        label = { Text("Registered Mobile Number (e.g. 9822012345)") },
                        modifier = Modifier.fillMaxWidth().testTag("input_mobile_login"),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            val found = allUsers.find { it.phone == mobileInput.trim() }
                            if (found != null) {
                                viewModel.selectUser(found)
                                onBack()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SteelBlue),
                        modifier = Modifier.fillMaxWidth().testTag("mobile_login_button")
                    ) {
                        Text("Sign In with Mobile")
                    }
                }
            }

            // Admin Login Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Safety Admin Portal Access",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkSteelBlue
                    )

                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Admin Email") },
                        modifier = Modifier.fillMaxWidth().testTag("input_admin_email"),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            val found = allUsers.find { it.email.equals(emailInput.trim(), ignoreCase = true) }
                            if (found != null) {
                                viewModel.selectUser(found)
                                onBack()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HimcastOrange),
                        modifier = Modifier.fillMaxWidth().testTag("admin_login_button")
                    ) {
                        Text("Sign In as Safety Admin")
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
