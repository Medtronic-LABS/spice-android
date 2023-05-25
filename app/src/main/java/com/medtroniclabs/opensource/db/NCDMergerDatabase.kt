package com.medtroniclabs.opensource.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.medtroniclabs.opensource.BuildConfig
import com.medtroniclabs.opensource.data.model.UnitMetricEntity
import com.medtroniclabs.opensource.db.dao.LanguageDAO
import com.medtroniclabs.opensource.db.dao.MetaDataDAO
import com.medtroniclabs.opensource.db.dao.RiskFactorDAO
import com.medtroniclabs.opensource.db.dao.ScreeningDAO
import com.medtroniclabs.opensource.db.tables.*
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory


@Database(
    entities = [
        LanguageEntity::class,
        ScreeningEntity::class,
        RiskFactorEntity::class,
        FormEntity::class,
        ConsentEntity::class,
        SiteEntity::class,
        MenuEntity::class,
        CountyEntity::class,
        SubCountyEntity::class,
        CountryEntity::class,
        MentalHealthEntity::class,
        ProgramEntity::class,
        SymptomEntity::class,
        MedicalComplianceEntity::class,
        ComorbidityEntity::class,
        ComplicationEntity::class,
        ComplaintsEntity::class,
        PhysicalExaminationEntity::class,
        LifestyleEntity::class,
        CurrentMedicationEntity::class,
        FrequencyEntity::class,
        DiagnosisEntity::class,
        ShortageReasonEntity::class,
        UnitMetricEntity::class,
        TreatmentPlanEntity::class,
        NutritionLifeStyle::class,
        CulturesEntity::class
    ], version = 1
)
@TypeConverters(Converters::class)
abstract class NCDMergerDatabase : RoomDatabase() {

    abstract fun languageDao(): LanguageDAO
    abstract fun screeningDao(): ScreeningDAO
    abstract fun riskFactorDao(): RiskFactorDAO
    abstract fun metaDataDao(): MetaDataDAO

    companion object {

        private const val DATABASE_NAME = "NCDMergerDatabase"

        @Volatile
        private var INSTANCE: NCDMergerDatabase? = null

        fun getInstance(context: Context): NCDMergerDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context): NCDMergerDatabase {
            val db = Room.databaseBuilder(
                context.applicationContext,
                NCDMergerDatabase::class.java,
                DATABASE_NAME
            )

            if (!BuildConfig.DEBUG) {
                db.openHelperFactory(addEncryption())
            }
            return db.build()
        }

        private fun addEncryption(): SupportFactory {
            val password = DefinedParams.DBKEY.toCharArray()
            val passphrase: ByteArray = SQLiteDatabase.getBytes(password)
            return SupportFactory(passphrase)
        }
    }

}