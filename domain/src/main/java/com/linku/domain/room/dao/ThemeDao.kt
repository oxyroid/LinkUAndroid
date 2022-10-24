package com.linku.domain.room.dao

import androidx.room.*
import com.linku.domain.entity.Theme
import kotlinx.coroutines.flow.Flow

@Dao
interface ThemeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(theme: Theme): Long

    @Delete
    suspend fun delete(theme: Theme)

    @Query("DELETE FROM Theme WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT * FROM Theme WHERE id = :id")
    suspend fun getById(id: Int): Theme?

    @Query("SELECT * FROM Theme")
    fun observeAll(): Flow<List<Theme>>
}
