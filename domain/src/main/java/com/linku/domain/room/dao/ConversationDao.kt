package com.linku.domain.room.dao

import androidx.room.*
import com.linku.domain.entity.Conversation
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(conversation: Conversation)

    @Delete
    suspend fun delete(conversation: Conversation)

    @Query("DELETE FROM Conversation WHERE id =:id")
    suspend fun delete(id: Int)

    @Query("SELECT * FROM Conversation ORDER BY updatedAt")
    suspend fun getAll(): List<Conversation>

    @Query("SELECT * FROM Conversation WHERE id = :id")
    suspend fun getById(id: Int): Conversation?

    @Query("SELECT * FROM Conversation WHERE id = :cid")
    fun observeConversation(cid: Int): Flow<Conversation>

    @Query("SELECT * FROM Conversation ORDER BY updatedAt")
    fun observeConversations(): Flow<List<Conversation>>

    @Query("DELETE FROM Conversation")
    suspend fun clear()
}