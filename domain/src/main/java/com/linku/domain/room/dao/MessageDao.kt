package com.linku.domain.room.dao

import androidx.room.*
import com.linku.domain.entity.Message

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
}