package com.example.data.model

enum class PermitStatus(val displayName: String) {
    PENDING("Pending"),
    APPROVED("Approved"),
    EXPIRED("Expired"),
    REJECTED("Rejected"),
    CLOSED("Closed");

    companion object {
        fun fromString(value: String): PermitStatus {
            return entries.find { it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) } ?: PENDING
        }
    }
}

enum class PermitCategory(val displayName: String, val iconDescription: String) {
    HOT_WORK("Hot Work", "Welding, gas cutting, brazing, grinding"),
    HEIGHT_WORK("Height Work", "Scaffolding, roof work, ladders > 1.8m"),
    CONFINED_SPACE("Confined Space", "Furnace interior, pits, silos, ducts"),
    ELECTRICAL("Electrical Work", "HT/LT panels, transformer maintenance, cabling"),
    COLD_WORK("Cold Work", "General civil, pipe fitting, structural maintenance"),
    EXCAVATION("Excavation", "Trench digging, foundation earthwork > 1.2m");

    companion object {
        fun fromString(value: String): PermitCategory {
            return entries.find { it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) } ?: HOT_WORK
        }
    }
}

enum class UserRole(val displayName: String) {
    SUPERVISOR("Site Supervisor"),
    SAFETY_OFFICER("Safety Officer"),
    DEPT_HEAD("Department Head"),
    PLANT_HEAD("Plant Head"),
    CONTRACTOR("Contractor Representative"),
    ADMIN("Safety Admin");

    companion object {
        fun fromString(value: String): UserRole {
            return entries.find { it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) } ?: SUPERVISOR
        }
    }
}

data class ChecklistItem(
    val id: String,
    val question: String,
    val checked: Boolean = false,
    val remarks: String = ""
)

data class DigitalSignature(
    val roleName: String,
    val signerName: String,
    val signerPhone: String,
    val signatureBase64: String,
    val timestamp: Long,
    val remarks: String = ""
)

data class ExtensionLog(
    val requestedBy: String,
    val approvedBy: String,
    val additionalHours: Int,
    val reason: String,
    val extendedAt: Long
)
