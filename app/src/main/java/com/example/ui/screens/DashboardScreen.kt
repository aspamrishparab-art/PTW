package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.entity.PermitEntity
import com.example.data.model.PermitCategory
import com.example.data.model.PermitStatus
import com.example.data.model.UserRole
import com.example.ui.components.EmergencyContactBar
import com.example.ui.components.PermitStatusBadge
import com.example.ui.pdf.PermitPdfGenerator
import com.example.ui.viewmodel.PermitViewModel
import com.example.ui.viewmodel.Screen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: PermitViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val allPermits by viewModel.allPermits.collectAsState()
    val filteredPermits by viewModel.filteredPermits.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val unreadCount by viewModel.unreadNotificationsCount.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategoryFilter.collectAsState()
    val selectedStatus by viewModel.selectedStatusFilter.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var showUserMenu by remember { mutableStateOf(false) }

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearToastMessage()
        }
    }

    // Counts for Supervisor KPI
    val activeCount = allPermits.count { it.status == PermitStatus.ACTIVE.name }
    val pendingCount = allPermits.count {
        it.status.startsWith("PENDING_")
    }
    val expiringCount = allPermits.count {
        val now = System.currentTimeMillis()
        it.status == PermitStatus.ACTIVE.name && (it.endTime - now in 1..(2 * 3600 * 1000L))
    }
    val closedCount = allPermits.count { it.status == PermitStatus.CLOSED.name }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.himcast_logo),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(6.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Himgiri Castings",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = "Supervisor Dashboard • ${currentUser?.name ?: "User"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                                maxLines = 1
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    // Switch user menu
                    IconButton(
                        onClick = { showUserMenu = true },
                        modifier = Modifier.testTag("switch_user_action_button")
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = "Switch User")
                    }

                    DropdownMenu(
                        expanded = showUserMenu,
                        onDismissRequest = { showUserMenu = false }
                    ) {
                        Text(
                            text = "Switch Active Factory Role:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                        allUsers.forEach { u ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(u.name, fontWeight = FontWeight.Bold)
                                        Text("${u.role.replace("_", " ")} (${u.department})", fontSize = 11.sp, color = Color.Gray)
                                    }
                                },
                                onClick = {
                                    viewModel.switchUser(u)
                                    showUserMenu = false
                                }
                            )
                        }
                    }

                    // Admin user database management if admin
                    if (currentUser?.role == UserRole.ADMIN.name) {
                        IconButton(
                            onClick = { viewModel.navigateTo(Screen.AdminUserManagement) },
                            modifier = Modifier.testTag("admin_users_button")
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = "User Database")
                        }
                    }

                    // Notifications icon
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.Notifications) },
                        modifier = Modifier.testTag("notifications_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadCount > 0) {
                                    Badge { Text("$unreadCount") }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                        }
                    }

                    // Logout
                    IconButton(
                        onClick = { viewModel.logout() },
                        modifier = Modifier.testTag("logout_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.navigateTo(Screen.CreatePermit) },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier.testTag("create_permit_fab")
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = "New Permit")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New Permit", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Emergency Contact bar at the very top
            EmergencyContactBar(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))

            // Search Bar for Site Supervisor Dashboard:
            // Filters active permits by contractor name, permit ID, or specific work site location
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .testTag("supervisor_search_bar"),
                    placeholder = {
                        Text(
                            text = "Search contractor, permit ID, work site location...",
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.setSearchQuery("") },
                                modifier = Modifier.testTag("clear_search_button")
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))

                    // Site Supervisor KPI Status Cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        KpiCard(
                            title = "Active",
                            count = activeCount,
                            color = Color(0xFF2E7D32),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    viewModel.setStatusFilter(
                                        if (selectedStatus == PermitStatus.ACTIVE.name) null else PermitStatus.ACTIVE.name
                                    )
                                }
                                .testTag("kpi_active_permits")
                        )
                        KpiCard(
                            title = "Approvals",
                            count = pendingCount,
                            color = Color(0xFFE65100),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    viewModel.setStatusFilter(
                                        if (selectedStatus != null && selectedStatus!!.startsWith("PENDING")) null else "PENDING_HOD"
                                    )
                                }
                                .testTag("kpi_pending_approvals")
                        )
                        KpiCard(
                            title = "Expiring",
                            count = expiringCount,
                            color = Color(0xFFC62828),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    viewModel.setStatusFilter(
                                        if (selectedStatus == PermitStatus.EXPIRING_SOON.name) null else PermitStatus.EXPIRING_SOON.name
                                    )
                                }
                                .testTag("kpi_expiring_permits")
                        )
                        KpiCard(
                            title = "Closed",
                            count = closedCount,
                            color = Color(0xFF00695C),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    viewModel.setStatusFilter(
                                        if (selectedStatus == PermitStatus.CLOSED.name) null else PermitStatus.CLOSED.name
                                    )
                                }
                                .testTag("kpi_closed_permits")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Category Filter Scrollable Row
                    Text(
                        text = "Filter by Work Category:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { viewModel.setCategoryFilter(null) },
                            label = { Text("All Types (${allPermits.size})") },
                            modifier = Modifier.testTag("chip_all_categories")
                        )
                        PermitCategory.values().forEach { cat ->
                            val catCount = allPermits.count { it.category == cat.name }
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = {
                                    viewModel.setCategoryFilter(if (selectedCategory == cat) null else cat)
                                },
                                label = { Text("${cat.shortName} ($catCount)") },
                                modifier = Modifier.testTag("chip_category_${cat.name.lowercase()}")
                            )
                        }
                    }

                    // Active filters indicator
                    if (searchQuery.isNotBlank() || selectedCategory != null || selectedStatus != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Showing ${filteredPermits.size} matching permit(s)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            OutlinedButton(
                                onClick = { viewModel.clearFilters() },
                                modifier = Modifier
                                    .height(28.dp)
                                    .testTag("clear_all_filters_button")
                            ) {
                                Text("Reset Filters", fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                }

                if (filteredPermits.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No Work Permits Found",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = if (searchQuery.isNotBlank()) "No permits match contractor, ID, or location: '$searchQuery'"
                                    else "No permits available under selected filters.",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                } else {
                    items(filteredPermits) { permit ->
                        SupervisorPermitCard(
                            permit = permit,
                            onCardClick = {
                                viewModel.navigateTo(Screen.PermitDetail(permit.id))
                            },
                            onDownloadPdf = {
                                PermitPdfGenerator.generateAndSharePermitPdf(context, permit)
                            }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp)) // Padding for FAB
                }
            }
        }
    }
}

@Composable
fun KpiCard(
    title: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$count",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = color
            )
        }
    }
}

@Composable
fun SupervisorPermitCard(
    permit: PermitEntity,
    onCardClick: () -> Unit,
    onDownloadPdf: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
    val cat = try {
        PermitCategory.valueOf(permit.category)
    } catch (e: Exception) {
        PermitCategory.HOT_WORK
    }

    val now = System.currentTimeMillis()
    val isExpiringSoon = permit.status == PermitStatus.ACTIVE.name && (permit.endTime - now in 1..(2 * 3600 * 1000L))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .testTag("permit_card_${permit.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Permit ID + Category Chip + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = permit.permitNumber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = cat.shortName,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                PermitStatusBadge(
                    statusString = if (isExpiringSoon) PermitStatus.EXPIRING_SOON.name else permit.status
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Work Title
            Text(
                text = permit.workTitle,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Work Site Location & Department
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = "Location",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${permit.location} • ${permit.department}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Contractor & Crew Count
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Engineering,
                    contentDescription = "Contractor",
                    tint = Color(0xFF1565C0),
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Contractor: ${permit.contractorName} (${permit.workersCount} Workers)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF0D47A1)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Validity Time Window
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "Time",
                    tint = if (isExpiringSoon) Color(0xFFC62828) else Color(0xFF455A64),
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Valid: ${dateFormat.format(Date(permit.startTime))} - ${dateFormat.format(Date(permit.endTime))}",
                    fontSize = 11.sp,
                    color = if (isExpiringSoon) Color(0xFFC62828) else Color(0xFF546E7A),
                    fontWeight = if (isExpiringSoon) FontWeight.Bold else FontWeight.Normal
                )
            }

            if (permit.isExtended) {
                Text(
                    text = "★ Extension granted: +${permit.extendedHours} hrs",
                    fontSize = 11.sp,
                    color = Color(0xFFC62828),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Row: Download PDF + View Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onDownloadPdf,
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("download_pdf_button_${permit.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = "PDF",
                        modifier = Modifier.size(15.dp),
                        tint = Color(0xFFC62828)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("PTW PDF", fontSize = 12.sp)
                }

                Text(
                    text = "View Workflow & Sign →",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
