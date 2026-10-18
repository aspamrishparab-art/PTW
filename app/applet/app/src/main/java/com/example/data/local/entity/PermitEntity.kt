package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.PermitCategory
import com.example.data.model.PermitStatus

@Entity(tableName = "permits")
data class PermitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val permitNumber: String, // e.g. "HC-2026-001"
    val category: PermitCategory,
    val title: String,
    val description: String,
    val location: String, // Work site location e.g. "Induction Furnace 3, Bay C"
    val department: String, // e.g. "Foundry Operations"
    val contractorName: String, // Contractor agency e.g. "Shivam Engineering"
    val contractorSupervisor: String,
    val contractorPhone: String,
    val numberOfWorkers: Int = 1,
    val issuerName: String,
    val issuerPhone: String,
    val status: PermitStatus = PermitStatus.PENDING,
    val startTime: Long,
    val endTime: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val checklistJson: String = "[]", // List<ChecklistItem> serialized
    // Multi-tier digital signatures serialized as JSON
    val signaturesJson: String = "[]", // List<DigitalSignature>
    val extensionLogsJson: String = "[]", // List<ExtensionLog>
    val isClosed: Boolean = false,
    val closureReason: String? = null,
    val closedAt: Long? = null,
    val rejectionReason: String? = null
)
