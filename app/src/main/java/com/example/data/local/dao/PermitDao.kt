package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.PermitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PermitDao {
    @Query("SELECT * FROM permits ORDER BY createdAt DESC")
    fun getAllPermits(): Flow<List<PermitEntity>>

    @Query("SELECT * FROM permits WHERE isClosed = 0 ORDER BY createdAt DESC")
    fun getActivePermits(): Flow<List<PermitEntity>>

    @Query("SELECT * FROM permits WHERE id = :id LIMIT 1")
    suspend fun getPermitById(id: Long): PermitEntity?

    @Query("SELECT * FROM permits WHERE id = :id LIMIT 1")
    fun getPermitByIdFlow(id: Long): Flow<PermitEntity?>

    @Query("""
        SELECT * FROM permits 
        WHERE (contractorName LIKE '%' || :query || '%' 
           OR permitNumber LIKE '%' || :query || '%' 
           OR location LIKE '%' || :query || '%' 
           OR title LIKE '%' || :query || '%')
        ORDER BY createdAt DESC
    """)
    fun searchPermits(query: String): Flow<List<PermitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPermit(permit: PermitEntity): Long

    @Update
    suspend fun updatePermit(permit: PermitEntity)

    @Delete
    suspend fun deletePermit(permit: PermitEntity)

    @Query("UPDATE permits SET status = 'EXPIRED' WHERE endTime < :now AND status = 'APPROVED' AND isClosed = 0")
    suspend fun checkAndMarkExpired(now: Long): Int
}
