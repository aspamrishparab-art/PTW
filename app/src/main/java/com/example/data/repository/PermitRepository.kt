package com.example.data.repository

import com.example.data.local.dao.NotificationDao
import com.example.data.local.dao.PermitDao
import com.example.data.local.entity.NotificationEntity
import com.example.data.local.entity.PermitEntity
import com.example.data.model.DigitalSignature
import com.example.data.model.ExtensionLog
import com.example.data.model.PermitStatus
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow

class PermitRepository(
    private val permitDao: PermitDao,
    private val notificationDao: NotificationDao
) {
    private val gson = Gson()

    val allPermits: Flow<List<PermitEntity>> = permitDao.getAllPermits()
    val activePermits: Flow<List<PermitEntity>> = permitDao.getActivePermits()
    val allNotifications: Flow<List<NotificationEntity>> = notificationDao.getAllNotifications()
    val unreadNotificationsCount: Flow<Int> = notificationDao.getUnreadCount()

    fun getPermitByIdFlow(id: Long): Flow<PermitEntity?> = permitDao.getPermitByIdFlow(id)

    suspend fun getPermitById(id: Long): PermitEntity? = permitDao.getPermitById(id)

    fun searchPermits(query: String): Flow<List<PermitEntity>> = permitDao.searchPermits(query)

    suspend fun insertPermit(permit: PermitEntity): Long {
        val id = permitDao.insertPermit(permit)
        notificationDao.insertNotification(
            NotificationEntity(
                permitId = id,
                permitNumber = permit.permitNumber,
                title = "New Permit Created",
                message = "${permit.category.displayName} permit created by ${permit.issuerName} for ${permit.contractorName} at ${permit.location}.",
                emailRecipientRole = "Department Head"
            )
        )
        return id
    }

    suspend fun updatePermit(permit: PermitEntity) {
        permitDao.updatePermit(permit)
    }

    suspend fun deletePermit(permit: PermitEntity) {
        permitDao.deletePermit(permit)
    }

    suspend fun addSignature(
        permitId: Long,
        signature: DigitalSignature,
        newStatus: PermitStatus? = null
    ) {
        val permit = permitDao.getPermitById(permitId) ?: return
        val listType = object : TypeToken<List<DigitalSignature>>() {}.type
        val currentSignatures: MutableList<DigitalSignature> = try {
            gson.fromJson<List<DigitalSignature>>(permit.signaturesJson, listType)?.toMutableList() ?: mutableListOf()
        } catch (e: Exception) {
            mutableListOf()
        }

        currentSignatures.add(signature)
        val updatedJson = gson.toJson(currentSignatures)
        val updated = permit.copy(
            signaturesJson = updatedJson,
            status = newStatus ?: permit.status
        )
        permitDao.updatePermit(updated)

        notificationDao.insertNotification(
            NotificationEntity(
                permitId = permit.id,
                permitNumber = permit.permitNumber,
                title = "Signature Appended: ${signature.roleName}",
                message = "Signed by ${signature.signerName} (${signature.roleName}). Status is now ${updated.status.displayName}.",
                emailRecipientRole = if (updated.status == PermitStatus.APPROVED) "All Stakeholders" else "Next Approver"
            )
        )
    }

    suspend fun requestExtension(
        permitId: Long,
        additionalHours: Int,
        extensionReason: String,
        approvedBy: String
    ) {
        val permit = permitDao.getPermitById(permitId) ?: return
        val newEndTime = permit.endTime + (additionalHours * 3600 * 1000L)

        val logType = object : TypeToken<List<ExtensionLog>>() {}.type
        val currentLogs: MutableList<ExtensionLog> = try {
            gson.fromJson<List<ExtensionLog>>(permit.extensionLogsJson, logType)?.toMutableList() ?: mutableListOf()
        } catch (e: Exception) {
            mutableListOf()
        }

        currentLogs.add(
            ExtensionLog(
                requestedBy = permit.contractorName,
                approvedBy = approvedBy,
                additionalHours = additionalHours,
                reason = extensionReason,
                extendedAt = System.currentTimeMillis()
            )
        )

        val updated = permit.copy(
            endTime = newEndTime,
            status = PermitStatus.APPROVED, // Reactivate if it was expired
            extensionLogsJson = gson.toJson(currentLogs)
        )
        permitDao.updatePermit(updated)

        notificationDao.insertNotification(
            NotificationEntity(
                permitId = permit.id,
                permitNumber = permit.permitNumber,
                title = "Permit Extended (+${additionalHours}h)",
                message = "Permit extended by $approvedBy. Reason: $extensionReason",
                emailRecipientRole = "Site Supervisor"
            )
        )
    }

    suspend fun closePermit(permitId: Long, closureReason: String) {
        val permit = permitDao.getPermitById(permitId) ?: return
        val updated = permit.copy(
            isClosed = true,
            status = PermitStatus.CLOSED,
            closedAt = System.currentTimeMillis(),
            closureReason = closureReason
        )
        permitDao.updatePermit(updated)

        notificationDao.insertNotification(
            NotificationEntity(
                permitId = permit.id,
                permitNumber = permit.permitNumber,
                title = "Permit Safely Closed",
                message = "Permit closed with work completed & area cleaned. Reason: $closureReason",
                emailRecipientRole = "Safety Officer"
            )
        )
    }

    suspend fun checkAndMarkExpiredPermits() {
        val now = System.currentTimeMillis()
        permitDao.checkAndMarkExpired(now)
    }

    suspend fun markAllNotificationsRead() {
        notificationDao.markAllAsRead()
    }
}
