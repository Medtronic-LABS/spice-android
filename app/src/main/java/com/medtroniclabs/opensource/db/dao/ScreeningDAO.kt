package com.medtroniclabs.opensource.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.medtroniclabs.opensource.db.tables.ScreeningEntity

@Dao
interface ScreeningDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScreening(screeningEntity: ScreeningEntity): Long

    /**
     * Fetch screening detail
     */
    @Query("SELECT * FROM ScreeningEntity")
    suspend fun getAllScreening(): List<ScreeningEntity>


    /**
     * delete screening entries from db
     */
    @Query("DELETE FROM ScreeningEntity")
    suspend fun deleteAllScreening()

    @Query("SELECT count(id) FROM ScreeningEntity WHERE userId=:userId AND createdAt BETWEEN :startDate AND :endDate")
    suspend fun getScreenedPatientCount(
        startDate: Long,
        endDate: Long,
        userId: Long
    ): Long

    @Query("SELECT count(id) FROM ScreeningEntity WHERE userId=:userId AND isReferred = :isReferred AND createdAt BETWEEN :startDate AND :endDate")
    suspend fun getScreenedPatientReferredCount(
        startDate: Long,
        endDate: Long,
        userId: Long,
        isReferred: Boolean
    ): Long

    @Query("DELETE FROM ScreeningEntity WHERE uploadStatus = 1 AND createdAt < :dateTime")
    suspend fun deleteUploadedScreeningRecords(dateTime: Long)

    @Query("SELECT * FROM ScreeningEntity WHERE uploadStatus = :uploadStatus ORDER BY createdAt DESC")
    suspend fun getAllScreeningRecords(uploadStatus: Boolean): List<ScreeningEntity>

    @Query("UPDATE ScreeningEntity SET uploadStatus = :uploadStatus WHERE id = :id")
    suspend fun updateScreeningRecordById(id: Long, uploadStatus: Boolean)

    @Query("UPDATE ScreeningEntity SET generalDetails = :generalDetails WHERE id = :id")
    suspend fun updateGeneralDetailsById(id: Long, generalDetails: String)

    @Query("SELECT * FROM ScreeningEntity where id=:id")
    suspend fun getScreeningRecordById(id: Long): ScreeningEntity

    @Query("SELECT COUNT(screeningDetails) FROM screeningentity WHERE uploadStatus=0")
    suspend fun getUnSyncedDataCount(): Long
}