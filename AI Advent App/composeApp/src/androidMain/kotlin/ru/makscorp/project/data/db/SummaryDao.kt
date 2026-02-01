package ru.makscorp.project.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SummaryDao {

    @Query("SELECT * FROM summaries ORDER BY createdAt ASC")
    fun getAllSummaries(): Flow<List<SummaryEntity>>

    @Query("SELECT * FROM summaries ORDER BY createdAt ASC")
    suspend fun getAllSummariesList(): List<SummaryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSummary(summary: SummaryEntity)

    @Query("DELETE FROM summaries WHERE id = :summaryId")
    suspend fun deleteSummary(summaryId: String)

    @Query("DELETE FROM summaries")
    suspend fun deleteAllSummaries()
}
