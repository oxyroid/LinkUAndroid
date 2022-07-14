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

    @Query("SELECT * FROM Message")
    suspend fun getAll(): List<Message>

    @Query("SELECT * FROM Message WHERE id = :id")
    suspend fun getById(id: Int): Message?


    @Query("SELECT * FROM Message ORDER BY timestamp DESC")
    fun incoming(): Flow<List<Message>>

    @Query("SELECT * FROM Message WHERE cid = :cid ORDER BY timestamp DESC")
    fun observeMessagesByCid(cid: Int): Flow<List<Message>>

    @Query("DELETE FROM Message")
    suspend fun clear()
}