package com.wzk.domain.room.dao

import androidx.room.*
import com.wzk.domain.entity.Food

@Dao
interface FoodDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(food: Food)

    @Delete
    suspend fun delete(food: Food)

    @Query("DELETE FROM Food WHERE id =:id")
    suspend fun delete(id: Int)

    @Query("SELECT * FROM Food")
    suspend fun getAll(): List<Food>

    @Query("SELECT * FROM Food WHERE id = :id")
    suspend fun getById(id: Int): Food?
}