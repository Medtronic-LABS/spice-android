package com.medtroniclabs.opensource.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.medtroniclabs.opensource.db.tables.LanguageEntity

@Dao
interface LanguageDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLanguage(languageEntity: LanguageEntity)

    /**
     * delete language from db
     */
    @Query("DELETE FROM LanguageEntity")
    suspend fun deleteAllLanguage()


    /**
     * Fetch translation from db
     */
    @Query("SELECT * FROM LanguageEntity")
    suspend fun getAllLanguage(): List<LanguageEntity>


}