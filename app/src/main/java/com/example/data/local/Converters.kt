package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.PermitCategory
import com.example.data.model.PermitStatus
import com.example.data.model.UserRole

class Converters {
    @TypeConverter
    fun fromPermitStatus(value: PermitStatus): String = value.name

    @TypeConverter
    fun toPermitStatus(value: String): PermitStatus = PermitStatus.fromString(value)

    @TypeConverter
    fun fromPermitCategory(value: PermitCategory): String = value.name

    @TypeConverter
    fun toPermitCategory(value: String): PermitCategory = PermitCategory.fromString(value)

    @TypeConverter
    fun fromUserRole(value: UserRole): String = value.name

    @TypeConverter
    fun toUserRole(value: String): UserRole = UserRole.fromString(value)
}
