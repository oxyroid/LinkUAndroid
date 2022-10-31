package com.linku.domain.room.dao

import androidx.room.*
import com.linku.domain.entity.Message
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: Message)

    @Delete
    suspend fun delete(message: Message)

    @Query("DELETE FROM Message WHERE id =:id")
    suspend fun delete(id: Int)

    @Query("SELECT * FROM Message WHERE uuid = :uuid LIMIT 1")
    suspend fun findByUUID(uuid: String): Message?

    @Query("UPDATE Message SET uuid = '', id = :id, cid = :cid, timestamp = :timestamp,content = :content,sendState = 1 WHERE uuid = :uuid")
    suspend fun levelStagingMessage(
        uuid: String,
        id: Int,
        cid: Int,
        timestamp: Long,
        content: String
    )

    @Query("UPDATE MESSAGE SET sendState = 2 WHERE uuid = :uuid")
    suspend fun failedStagingMessage(uuid: String)

    @Query("SELECT * FROM Message ORDER BY timestamp DESC")
    suspend fun getAll(): List<Message>

    @Query("SELECT * FROM Message WHERE cid = :cid ORDER BY timestamp DESC")
    fun getAllByCid(cid: Int): List<Message>

    @Query("SELECT * FROM Message WHERE id = :id")
    suspend fun getById(id: Int): Message?

    @Query("SELECT * FROM Message ORDER BY timestamp DESC")
    fun incoming(): Flow<List<Message>>

    @Query("SELECT * FROM Message WHERE cid = :cid ORDER BY timestamp DESC")
    fun incoming(cid: Int): Flow<List<Message>>

    @Query("DELETE FROM Message")
    suspend fun clear()

    @Query("DELETE FROM Message WHERE sendState = 0 OR sendState = 2")
    suspend fun clearStagingMessages()

    @Query("SELECT * FROM Message ORDER BY timestamp DESC LIMIT 1")
    fun getLatestMessage(): Message?

    @Query("SELECT * FROM Message WHERE cid = :cid ORDER BY timestamp DESC LIMIT 1")
    fun observeLatestMessageByCid(cid: Int): Flow<Message>

    @Query("SELECT * FROM Message WHERE type = :type")
    suspend fun findByType(type: Message.Type): List<Message>
}
