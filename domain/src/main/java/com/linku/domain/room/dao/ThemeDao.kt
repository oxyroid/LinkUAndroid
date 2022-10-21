package com.linku.domain.room.dao

import androidx.room.*
import com.linku.domain.entity.local.Theme
import kotlinx.coroutines.flow.Flow

@Dao
interface ThemeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(theme: Theme): Long

    @Delete
    suspend fun delete(theme: Theme)

    @Query("SELECT * FROM Theme WHERE id = :id")
    suspend fun getById(id: Int): Theme?

    @Query("SELECT * FROM Theme")
    fun observeAll(): Flow<List<Theme>>
}
