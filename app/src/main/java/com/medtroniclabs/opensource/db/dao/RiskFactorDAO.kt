package com.medtroniclabs.opensource.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.medtroniclabs.opensource.db.tables.RiskFactorEntity

@Dao
interface RiskFactorDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRiskFactor(riskFactorEntity: RiskFactorEntity)

    /**
     * Fetch screening detail
     */
    @Query("SELECT * FROM RiskFactorEntity")
    suspend fun getAllRiskFactorEntity(): List<RiskFactorEntity>


}