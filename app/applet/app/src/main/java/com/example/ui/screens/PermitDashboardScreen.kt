package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import com.example.ui.components.EmergencyContactBar
import com.example.ui.components.PermitStatusBadge
import com.example.ui.pdf.PermitPdfGenerator
import com.example.ui.theme.AlertAmber
import com.example.ui.theme.AlertAmberBg
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DangerRedBg
import com.example.ui.theme.DarkSteelBlue
import com.example.ui.theme.HimcastOrange
import com.example.ui.theme.LightSteelBlue
import com.example.ui.theme.NeutralDark
import com.example.ui.theme.NeutralLight
import com.example.ui.theme.NeutralMedium
import com.example.ui.theme.SafetyGreen
import com.example.ui.theme.SafetyGreenBg
import com.example.ui.theme.SteelBlue
import com.example.ui.viewmodel.PermitViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermitDashboardScreen(
    viewModel: PermitViewModel,
    onNavigateToCreate: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onNavigateToNotifications: () -> Unit
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val filteredPermits by viewModel.filteredPermits.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedStatus by viewModel.selectedStatusFilter.collectAsState()
    val selectedCategory by viewModel.selectedCategoryFilter.collectAsState()

    val approvedCount by viewModel.approvedCount.collectAsState()
    val pendingCount by viewModel.pendingCount.collectAsState()
    val expiredCount by viewModel.expiredCount.collectAsState()
    val activeCount by viewModel.activeCount.collectAsState()
    val unreadNotifs by viewModel.unreadNotificationsCount.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.himcast_logo),
                            contentDescription = "Himcast Logo",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(6.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Himgiri Castings",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Safety PTW System",
                                fontSize = 11.sp,
                                color = LightSteelBlue
                            )
                        }
                    }
                },
                actions = {
                    // Persona Pill
                    Box(
                        modifier = Modifier
                            .testTag("user_persona_pill")
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .clickable { onNavigateToLogin() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "User Profile",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = currentUser?.role?.displayName ?: "Sign In",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Notification Bell
                    IconButton(
                        onClick = onNavigateToNotifications,
                        modifier = Modifier.testTag("notifications_icon_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadNotifs > 0) {
                                    Badge(containerColor = HimcastOrange) {
                                        Text(text = unreadNotifs.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = Color.White
                            )
                        }
                    }

                    // Admin icon
                    IconButton(
                        onClick = onNavigateToAdmin,
                        modifier = Modifier.testTag("admin_users_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = "User Management",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSteelBlue)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreate,
                containerColor = HimcastOrange,
                contentColor = Color.White,
                modifier = Modifier.testTag("create_permit_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Create Permit")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "New Permit", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF6F8FC))
        ) {
            // Emergency Contact Strip
            EmergencyContactBar(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp))

            // Supervisor Dashboard Search Bar
            SupervisorSearchBar(
                query = searchQuery,
                onQueryChanged = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp)
            )

            // Status Badges & Counters Row (Approved, Pending, Expired, All)
            StatusBadgesRow(
                approvedCount = approvedCount,
                pendingCount = pendingCount,
                expiredCount = expiredCount,
                allCount = activeCount,
                selectedStatus = selectedStatus,
                onStatusSelected = { viewModel.setStatusFilter(it) },
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            )

            // Category Filter Chips
            CategoryFilterRow(
                selectedCategory = selectedCategory,
                onCategorySelected = { viewModel.setCategoryFilter(it) },
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
            )

            // Active Permits List Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Active Safety Permits (${filteredPermits.size})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeutralDark
                )

                if (selectedStatus != null || selectedCategory != null || searchQuery.isNotBlank()) {
                    Text(
                        text = "Clear Filters",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SteelBlue,
                        modifier = Modifier
                            .clickable {
                                viewModel.setSearchQuery("")
                                viewModel.setStatusFilter(null)
                                viewModel.setCategoryFilter(null)
                            }
                            .padding(4.dp)
                    )
                }
            }

            // Permits Lazy Column
            if (filteredPermits.isEmpty()) {
                EmptyPermitsState(
                    hasFilter = searchQuery.isNotBlank() || selectedStatus != null || selectedCategory != null,
                    onReset = {
                        viewModel.setSearchQuery("")
                        viewModel.setStatusFilter(null)
                        viewModel.setCategoryFilter(null)
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("permits_list"),
                    contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = 88.dp, top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredPermits, key = { it.id }) { permit ->
                        PermitCard(
                            permit = permit,
                            onCardClick = { onNavigateToDetail(permit.id) },
                            onPdfClick = {
                                PermitPdfGenerator.generateAndSharePdf(context, permit)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SupervisorSearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        placeholder = {
            Text(
                text = "Search by contractor name, permit ID, work site location...",
                fontSize = 12.sp,
                color = NeutralMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = SteelBlue,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChanged("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear Search",
                        tint = NeutralMedium,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedIndicatorColor = SteelBlue,
            unfocusedIndicatorColor = Color(0xFFD0D7DE)
        ),
        modifier = modifier.testTag("supervisor_search_bar")
    )
}

@Composable
fun StatusBadgesRow(
    approvedCount: Int,
    pendingCount: Int,
    expiredCount: Int,
    allCount: Int,
    selectedStatus: PermitStatus?,
    onStatusSelected: (PermitStatus?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Approved Badge Card
        StatusFilterPill(
            label = "Approved",
            count = approvedCount,
            isSelected = selectedStatus == PermitStatus.APPROVED,
            activeBgColor = SafetyGreenBg,
            activeTextColor = SafetyGreen,
            icon = Icons.Default.CheckCircle,
            onClick = {
                onStatusSelected(if (selectedStatus == PermitStatus.APPROVED) null else PermitStatus.APPROVED)
            },
            modifier = Modifier.weight(1f).testTag("filter_badge_approved")
        )

        // Pending Badge Card
        StatusFilterPill(
            label = "Pending",
            count = pendingCount,
            isSelected = selectedStatus == PermitStatus.PENDING,
            activeBgColor = AlertAmberBg,
            activeTextColor = AlertAmber,
            icon = Icons.Default.HourglassTop,
            onClick = {
                onStatusSelected(if (selectedStatus == PermitStatus.PENDING) null else PermitStatus.PENDING)
            },
            modifier = Modifier.weight(1f).testTag("filter_badge_pending")
        )

        // Expired Badge Card
        StatusFilterPill(
            label = "Expired",
            count = expiredCount,
            isSelected = selectedStatus == PermitStatus.EXPIRED,
            activeBgColor = DangerRedBg,
            activeTextColor = DangerRed,
            icon = Icons.Default.Error,
            onClick = {
                onStatusSelected(if (selectedStatus == PermitStatus.EXPIRED) null else PermitStatus.EXPIRED)
            },
            modifier = Modifier.weight(1f).testTag("filter_badge_expired")
        )

        // All Active Card
        StatusFilterPill(
            label = "All",
            count = allCount,
            isSelected = selectedStatus == null,
            activeBgColor = LightSteelBlue,
            activeTextColor = DarkSteelBlue,
            icon = Icons.Default.FilterList,
            onClick = { onStatusSelected(null) },
            modifier = Modifier.weight(0.9f).testTag("filter_badge_all")
        )
    }
}

@Composable
fun StatusFilterPill(
    label: String,
    count: Int,
    isSelected: Boolean,
    activeBgColor: Color,
    activeTextColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) activeBgColor else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 1.dp),
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isSelected) activeTextColor else NeutralMedium,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) activeTextColor else NeutralDark
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = count.toString(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) activeTextColor else NeutralDark
            )
        }
    }
}

@Composable
fun CategoryFilterRow(
    selectedCategory: PermitCategory?,
    onCategorySelected: (PermitCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item {
            CategoryChip(
                label = "All Categories",
                isSelected = selectedCategory == null,
                onClick = { onCategorySelected(null) }
            )
        }
        items(PermitCategory.entries) { category ->
            CategoryChip(
                label = category.displayName,
                isSelected = selectedCategory == category,
                onClick = {
                    onCategorySelected(if (selectedCategory == category) null else category)
                }
            )
        }
    }
}

@Composable
fun CategoryChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) SteelBlue else Color.White)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else NeutralMedium
        )
    }
}

@Composable
fun PermitCard(
    permit: PermitEntity,
    onCardClick: () -> Unit,
    onPdfClick: () -> Unit
) {
    val now = System.currentTimeMillis()
    val isExpired = permit.endTime < now || permit.status == PermitStatus.EXPIRED
    val remainingMillis = permit.endTime - now
    val remainingHours = (remainingMillis / (3600 * 1000L)).toInt()
    val remainingMins = ((remainingMillis % (3600 * 1000L)) / (60 * 1000L)).toInt()

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("permit_card_${permit.id}")
            .clickable { onCardClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: Permit Number + Category Tag + Prominent Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkSteelBlue)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = permit.permitNumber,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(LightSteelBlue)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = permit.category.displayName,
                            color = DarkSteelBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Dedicated Status Badge for Approved, Pending, Expired
                PermitStatusBadge(
                    status = if (isExpired && !permit.isClosed && permit.status != PermitStatus.REJECTED) PermitStatus.EXPIRED else permit.status
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Work Title
            Text(
                text = permit.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = NeutralDark,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Work Site Location
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = "Work Site Location",
                    tint = HimcastOrange,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${permit.location} (${permit.department})",
                    fontSize = 12.sp,
                    color = NeutralDark,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Contractor Name
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Engineering,
                    contentDescription = "Contractor",
                    tint = SteelBlue,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Contractor: ${permit.contractorName} • Supv: ${permit.contractorSupervisor}",
                    fontSize = 12.sp,
                    color = NeutralMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer with Validity Timer and PDF action
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF8F9FA))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Time",
                        tint = if (isExpired) DangerRed else SteelBlue,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (permit.isClosed) {
                            "Closed / Completed"
                        } else if (isExpired) {
                            "Validity Expired"
                        } else {
                            "Valid: ${remainingHours}h ${remainingMins}m remaining"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isExpired) DangerRed else NeutralDark
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onPdfClick,
                        modifier = Modifier
                            .size(30.dp)
                            .testTag("download_pdf_${permit.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "Export PDF",
                            tint = SteelBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "View Details →",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SteelBlue,
                        modifier = Modifier.clickable { onCardClick() }
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyPermitsState(
    hasFilter: Boolean,
    onReset: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = Color.LightGray,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = if (hasFilter) "No permits found matching your filter" else "No active safety permits",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = NeutralMedium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (hasFilter) "Try searching by contractor name, permit ID, or work site location" else "Create a new permit using the '+' button below",
            fontSize = 12.sp,
            color = Color.Gray
        )
        if (hasFilter) {
            Spacer(modifier = Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(SteelBlue)
                    .clickable { onReset() }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(text = "Reset All Filters", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
