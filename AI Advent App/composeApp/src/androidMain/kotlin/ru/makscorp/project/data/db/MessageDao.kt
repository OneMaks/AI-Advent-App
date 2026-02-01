package ru.makscorp.project.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Query("SELECT * FROM messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE isCompressed = 0 ORDER BY timestamp ASC")
    fun getUncompressedMessages(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE isCompressed = 1 ORDER BY timestamp ASC")
    fun getCompressedMessages(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE isCompressed = 0 ORDER BY timestamp ASC")
    suspend fun getUncompressedMessagesList(): List<MessageEntity>

    @Query("SELECT * FROM messages ORDER BY timestamp ASC")
    suspend fun getAllMessagesList(): List<MessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    @Update
    suspend fun updateMessage(message: MessageEntity)

    @Query("UPDATE messages SET isCompressed = 1, summaryId = :summaryId WHERE id IN (:messageIds)")
    suspend fun markMessagesAsCompressed(messageIds: List<String>, summaryId: String)

    @Query("DELETE FROM messages WHERE id = :messageId")
    suspend fun deleteMessage(messageId: String)

    @Query("DELETE FROM messages")
    suspend fun deleteAllMessages()

    @Query("SELECT COUNT(*) FROM messages WHERE isCompressed = 0")
    suspend fun getUncompressedMessagesCount(): Int
}
