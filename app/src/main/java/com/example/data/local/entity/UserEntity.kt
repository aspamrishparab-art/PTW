package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.UserRole

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String, // Individual login via mobile number
    val email: String, // Individual or common email Safety@himcast.in
    val role: UserRole,
    val department: String,
    val isActive: Boolean = true
)
