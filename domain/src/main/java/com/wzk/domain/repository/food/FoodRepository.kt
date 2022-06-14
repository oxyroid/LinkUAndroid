package com.wzk.domain.repository.food

import com.wzk.domain.entity.Food
import com.wzk.wrapper.Result

interface FoodRepository {
    suspend fun getById(id: Int): Result<Food>
    suspend fun getAll(): Result<List<Food>>
    suspend fun put(food: Food): Result<Food>
}