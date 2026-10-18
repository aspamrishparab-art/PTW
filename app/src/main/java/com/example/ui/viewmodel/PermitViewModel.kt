package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.NotificationEntity
import com.example.data.local.entity.PermitEntity
import com.example.data.local.entity.UserEntity
import com.example.data.model.CategoryChecklists
import com.example.data.model.ChecklistItem
import com.example.data.model.PermitCategory
import com.example.data.model.PermitStatus
import com.example.data.model.UserRole
import com.example.data.repository.PermitRepository
import com.example.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

sealed class Screen {
    object Login : Screen()
    object Dashboard : Screen()
    object CreatePermit : Screen()
    data class PermitDetail(val permitId: Long) : Screen()
    object Notifications : Screen()
    object AdminUserManagement : Screen()
}

class PermitViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application, viewModelScope)
    private val permitRepository = PermitRepository(db.permitDao(), db.notificationDao())
    private val userRepository = UserRepository(db.userDao())

    // Current logged in user
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    // Navigation screen state & back stack
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Login)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()
    private val backStack = mutableListOf<Screen>()

    // Supervisor Dashboard Search & Filter states
    // Search by contractor name, permit ID, or specific work site location
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<PermitCategory?>(null)
    val selectedCategoryFilter: StateFlow<PermitCategory?> = _selectedCategoryFilter.asStateFlow()

    private val _selectedStatusFilter = MutableStateFlow<String?>(null)
    val selectedStatusFilter: StateFlow<String?> = _selectedStatusFilter.asStateFlow()

    // Data streams
    val allPermits: StateFlow<List<PermitEntity>> = permitRepository.allPermits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<UserEntity>> = userRepository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotifications: StateFlow<List<NotificationEntity>> = permitRepository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotificationsCount: StateFlow<Int> = permitRepository.unreadNotificationsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Filtered permits for Supervisor Dashboard
    val filteredPermits: StateFlow<List<PermitEntity>> = combine(
        allPermits,
        _searchQuery,
        _selectedCategoryFilter,
        _selectedStatusFilter
    ) { permits, query, category, status ->
        permits.filter { permit ->
            // Search filter: Contractor name, Permit ID, or specific Work site location
            val matchesQuery = if (query.isBlank()) {
                true
            } else {
                val q = query.trim().lowercase()
                permit.permitNumber.lowercase().contains(q) ||
                permit.contractorName.lowercase().contains(q) ||
                permit.location.lowercase().contains(q) ||
                permit.workTitle.lowercase().contains(q)
            }

            // Category filter
            val matchesCategory = category == null || permit.category == category.name

            // Status filter
            val matchesStatus = status == null || permit.status == status

            matchesQuery && matchesCategory && matchesStatus
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Feedback snackbar or alerts
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    init {
        // Pre-seed and check expired permits
        viewModelScope.launch(Dispatchers.IO) {
            AppDatabase.seedDatabase(db)
            permitRepository.checkAndMarkExpiredPermits()
        }
    }

    // Navigation functions
    fun navigateTo(screen: Screen) {
        backStack.add(_currentScreen.value)
        _currentScreen.value = screen
    }

    fun navigateBack(): Boolean {
        return if (backStack.isNotEmpty()) {
            val prev = backStack.removeAt(backStack.size - 1)
            _currentScreen.value = prev
            true
        } else {
            false
        }
    }

    // Search & Filter Setters
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(category: PermitCategory?) {
        _selectedCategoryFilter.value = category
    }

    fun setStatusFilter(status: String?) {
        _selectedStatusFilter.value = status
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _selectedCategoryFilter.value = null
        _selectedStatusFilter.value = null
    }

    fun clearToastMessage() {
        _toastMessage.value = null
    }

    // Auth & User Management
    fun loginWithMobile(mobile: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val cleanMobile = mobile.trim()
            val user = userRepository.getUserByMobile(cleanMobile)
            if (user != null) {
                if (user.isActive) {
                    _currentUser.value = user
                    _currentScreen.value = Screen.Dashboard
                    onResult(true, "Welcome back, ${user.name} (${user.role})")
                } else {
                    onResult(false, "Account deactivated. Please contact Safety Admin.")
                }
            } else {
                onResult(false, "Mobile number not registered. Admin must add user to the database.")
            }
        }
    }

    fun loginAsAdmin(email: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val cleanEmail = email.trim()
            if (cleanEmail.equals("safety@himcast.in", ignoreCase = true)) {
                val admin = userRepository.getAdminUser()
                if (admin != null) {
                    _currentUser.value = admin
                    _currentScreen.value = Screen.Dashboard
                    onResult(true, "Admin session authenticated successfully")
                } else {
                    val newAdmin = UserEntity(
                        name = "Safety Admin",
                        mobile = "9876543210",
                        email = "Safety@himcast.in",
                        role = UserRole.ADMIN.name,
                        department = "Safety & Admin"
                    )
                    userRepository.insertUser(newAdmin)
                    _currentUser.value = newAdmin
                    _currentScreen.value = Screen.Dashboard
                    onResult(true, "Admin initial session created")
                }
            } else {
                onResult(false, "Only Safety@himcast.in is authorized for admin login.")
            }
        }
    }

    fun switchUser(user: UserEntity) {
        _currentUser.value = user
        _toastMessage.value = "Switched active user to ${user.name} (${user.role})"
    }

    fun logout() {
        _currentUser.value = null
        backStack.clear()
        _currentScreen.value = Screen.Login
    }

    fun addUser(
        name: String,
        mobile: String,
        role: UserRole,
        department: String,
        onComplete: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = userRepository.getUserByMobile(mobile.trim())
            if (existing != null) {
                onComplete(false, "User with mobile $mobile already exists in the database.")
                return@launch
            }
            // All users have standard company email internally without showing it publicly
            val newUser = UserEntity(
                name = name.trim(),
                mobile = mobile.trim(),
                email = "Safety@himcast.in",
                role = role.name,
                department = department.trim()
            )
            userRepository.insertUser(newUser)
            onComplete(true, "User ${newUser.name} registered successfully!")
        }
    }

    // Permit Operations
    fun createPermit(
        category: PermitCategory,
        workTitle: String,
        workDescription: String,
        location: String,
        department: String,
        contractorName: String,
        workersCount: Int,
        durationHours: Int,
        checklist: List<ChecklistItem>,
        specialPrecautions: String,
        issuerSignature: String,
        onSuccess: (Long) -> Unit
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val startTime = System.currentTimeMillis()
            val endTime = startTime + (durationHours * 3600 * 1000L)

            val jsonArray = JSONArray()
            checklist.forEach { item ->
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("question", item.question)
                    put("isChecked", item.isChecked)
                    put("remarks", item.remarks)
                }
                jsonArray.put(obj)
            }

            val newPermit = PermitEntity(
                permitNumber = "HC-PENDING",
                category = category.name,
                workTitle = workTitle.trim(),
                workDescription = workDescription.trim(),
                location = location.trim(),
                department = department.trim(),
                contractorName = contractorName.trim(),
                workersCount = workersCount,
                startTime = startTime,
                endTime = endTime,
                originalEndTime = endTime,
                status = PermitStatus.PENDING_HOD.name,
                emergencyContact = "+91 9359610323",
                issuerId = user.id,
                issuerName = user.name,
                issuerMobile = user.mobile,
                issuerSignature = issuerSignature,
                issuerSignedAt = System.currentTimeMillis(),
                checklistJson = jsonArray.toString(),
                specialPrecautions = specialPrecautions.trim()
            )

            val id = permitRepository.createPermit(newPermit)
            _toastMessage.value = "Permit created! Dispatched for Concern HOD Approval."
            onSuccess(id)
        }
    }

    fun approveByHod(permitId: Long, signature: String, comments: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            permitRepository.approveByHod(permitId, user.name, signature, comments)
            _toastMessage.value = "HOD Approval signed. Forwarded to HR/Security."
        }
    }

    fun approveByHrSecurity(permitId: Long, signature: String, comments: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            permitRepository.approveByHrSecurity(permitId, user.name, signature, comments)
            _toastMessage.value = "HR/Security Gate Clearance signed. Forwarded to Contractor."
        }
    }

    fun acceptByContractor(permitId: Long, signature: String, comments: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            permitRepository.acceptByContractor(permitId, user.name, signature, comments)
            _toastMessage.value = "Contractor Acceptance signed. Forwarded to Production Manager."
        }
    }

    fun approveByProductionMgr(permitId: Long, signature: String, comments: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            permitRepository.approveByProductionMgr(permitId, user.name, signature, comments)
            _toastMessage.value = "Production Line Clearance signed. Forwarded to Safety Officer."
        }
    }

    fun authorizeBySafetyOfficer(permitId: Long, signature: String, comments: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            permitRepository.authorizeBySafetyOfficer(permitId, user.name, signature, comments)
            _toastMessage.value = "★ PERMIT FULLY AUTHORIZED & ISSUED! Work can proceed safely."
        }
    }

    fun requestExtension(permitId: Long, additionalHours: Int, reason: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            permitRepository.requestExtension(permitId, additionalHours, reason, user.name)
            _toastMessage.value = "Permit extended by $additionalHours hours."
        }
    }

    fun requestClosureByIssuer(permitId: Long, signature: String, closureChecklist: List<ChecklistItem>, comments: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val jsonArray = JSONArray()
            closureChecklist.forEach { item ->
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("question", item.question)
                    put("isChecked", item.isChecked)
                }
                jsonArray.put(obj)
            }
            permitRepository.requestClosureByIssuer(permitId, signature, jsonArray.toString(), comments)
            _toastMessage.value = "Closure handover signed by Issuer. Awaiting HOD Closing Approval."
        }
    }

    fun approveClosureByHod(permitId: Long, signature: String, comments: String) {
        viewModelScope.launch(Dispatchers.IO) {
            permitRepository.approveClosureByHod(permitId, signature, comments)
            _toastMessage.value = "Work Permit successfully closed & archived."
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch(Dispatchers.IO) {
            permitRepository.markAllNotificationsRead()
        }
    }
}
