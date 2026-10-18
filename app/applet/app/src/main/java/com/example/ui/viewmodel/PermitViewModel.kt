package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.NotificationEntity
import com.example.data.local.entity.PermitEntity
import com.example.data.local.entity.UserEntity
import com.example.data.model.ChecklistItem
import com.example.data.model.DigitalSignature
import com.example.data.model.PermitCategory
import com.example.data.model.PermitStatus
import com.example.data.model.UserRole
import com.example.data.repository.PermitRepository
import com.example.data.repository.UserRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PermitViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val permitRepository = PermitRepository(database.permitDao(), database.notificationDao())
    private val userRepository = UserRepository(database.userDao())
    private val gson = Gson()

    val allUsers: StateFlow<List<UserEntity>> = userRepository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _currentScreen = MutableStateFlow("DASHBOARD")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    private val _selectedPermitId = MutableStateFlow<Long?>(null)
    val selectedPermitId: StateFlow<Long?> = _selectedPermitId.asStateFlow()

    val allPermits: StateFlow<List<PermitEntity>> = permitRepository.allPermits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activePermits: StateFlow<List<PermitEntity>> = permitRepository.activePermits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotifications: StateFlow<List<NotificationEntity>> = permitRepository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotificationsCount: StateFlow<Int> = permitRepository.unreadNotificationsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val searchQuery = MutableStateFlow("")
    val selectedStatusFilter = MutableStateFlow<PermitStatus?>(null)
    val selectedCategoryFilter = MutableStateFlow<PermitCategory?>(null)

    // Filtered permits based on search bar (contractor name, permit ID, work site location) & status filter
    val filteredPermits: StateFlow<List<PermitEntity>> = combine(
        allPermits,
        searchQuery,
        selectedStatusFilter,
        selectedCategoryFilter
    ) { permits, query, status, category ->
        val trimmed = query.trim()
        permits.filter { permit ->
            val matchesQuery = if (trimmed.isBlank()) {
                true
            } else {
                permit.permitNumber.contains(trimmed, ignoreCase = true) ||
                permit.contractorName.contains(trimmed, ignoreCase = true) ||
                permit.location.contains(trimmed, ignoreCase = true) ||
                permit.title.contains(trimmed, ignoreCase = true) ||
                permit.department.contains(trimmed, ignoreCase = true)
            }

            val matchesStatus = if (status == null) true else permit.status == status
            val matchesCategory = if (category == null) true else permit.category == category

            matchesQuery && matchesStatus && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Counts for stats badges on dashboard
    val approvedCount: StateFlow<Int> = allPermits.combine(MutableStateFlow(Unit)) { permits, _ ->
        permits.count { it.status == PermitStatus.APPROVED && !it.isClosed }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val pendingCount: StateFlow<Int> = allPermits.combine(MutableStateFlow(Unit)) { permits, _ ->
        permits.count { it.status == PermitStatus.PENDING && !it.isClosed }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val expiredCount: StateFlow<Int> = allPermits.combine(MutableStateFlow(Unit)) { permits, _ ->
        permits.count { it.status == PermitStatus.EXPIRED && !it.isClosed }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val activeCount: StateFlow<Int> = allPermits.combine(MutableStateFlow(Unit)) { permits, _ ->
        permits.count { !it.isClosed }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        // Initial user setup & periodic expiration check
        viewModelScope.launch {
            permitRepository.checkAndMarkExpiredPermits()
            allUsers.collect { users ->
                if (_currentUser.value == null && users.isNotEmpty()) {
                    _currentUser.value = users.firstOrNull { it.role == UserRole.SUPERVISOR } ?: users.first()
                }
            }
        }
    }

    fun navigateTo(screen: String, permitId: Long? = null) {
        _selectedPermitId.value = permitId
        _currentScreen.value = screen
    }

    fun selectUser(user: UserEntity) {
        _currentUser.value = user
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun setStatusFilter(status: PermitStatus?) {
        selectedStatusFilter.value = status
    }

    fun setCategoryFilter(category: PermitCategory?) {
        selectedCategoryFilter.value = category
    }

    fun createPermit(
        title: String,
        description: String,
        category: PermitCategory,
        location: String,
        department: String,
        contractorName: String,
        contractorSupervisor: String,
        contractorPhone: String,
        numberOfWorkers: Int,
        durationHours: Int,
        checklists: List<ChecklistItem>,
        issuerSignatureBase64: String
    ) {
        viewModelScope.launch {
            val user = _currentUser.value
            val now = System.currentTimeMillis()
            val nextIndex = (allPermits.value.size + 1).toString().padStart(3, '0')
            val permitNumber = "HC-2026-$nextIndex"

            val initialSignatures = if (issuerSignatureBase64.isNotBlank()) {
                listOf(
                    DigitalSignature(
                        roleName = user?.role?.displayName ?: "Site Supervisor (Issuer)",
                        signerName = user?.name ?: "Plant Supervisor",
                        signerPhone = user?.phone ?: "+91 9359610323",
                        signatureBase64 = issuerSignatureBase64,
                        timestamp = now,
                        remarks = "Permit initiated and verified at site"
                    )
                )
            } else {
                emptyList()
            }

            val newPermit = PermitEntity(
                permitNumber = permitNumber,
                category = category,
                title = title,
                description = description,
                location = location,
                department = department,
                contractorName = contractorName,
                contractorSupervisor = contractorSupervisor,
                contractorPhone = contractorPhone,
                numberOfWorkers = numberOfWorkers,
                issuerName = user?.name ?: "Plant Supervisor",
                issuerPhone = user?.phone ?: "+91 9359610323",
                status = PermitStatus.PENDING,
                startTime = now,
                endTime = now + (durationHours * 3600 * 1000L),
                createdAt = now,
                checklistJson = gson.toJson(checklists),
                signaturesJson = gson.toJson(initialSignatures)
            )

            val id = permitRepository.insertPermit(newPermit)
            navigateTo("PERMIT_DETAIL", id)
        }
    }

    fun addDigitalSignature(
        permitId: Long,
        roleTitle: String,
        signerName: String,
        signerPhone: String,
        signatureBase64: String,
        remarks: String,
        isFinalApproval: Boolean = false
    ) {
        viewModelScope.launch {
            val sig = DigitalSignature(
                roleName = roleTitle,
                signerName = signerName,
                signerPhone = signerPhone,
                signatureBase64 = signatureBase64,
                timestamp = System.currentTimeMillis(),
                remarks = remarks
            )
            val newStatus = if (isFinalApproval) PermitStatus.APPROVED else null
            permitRepository.addSignature(permitId, sig, newStatus)
        }
    }

    fun requestExtension(permitId: Long, additionalHours: Int, reason: String) {
        viewModelScope.launch {
            val user = _currentUser.value
            val approver = user?.name ?: "Authorized Safety Officer"
            permitRepository.requestExtension(permitId, additionalHours, reason, approver)
        }
    }

    fun closePermit(permitId: Long, reason: String) {
        viewModelScope.launch {
            permitRepository.closePermit(permitId, reason)
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            permitRepository.markAllNotificationsRead()
        }
    }

    fun addUser(name: String, phone: String, email: String, role: UserRole, department: String) {
        viewModelScope.launch {
            userRepository.insertUser(
                UserEntity(
                    name = name,
                    phone = phone,
                    email = email,
                    role = role,
                    department = department
                )
            )
        }
    }
}
