package com.wzk.domain.repository.food

import com.wzk.domain.entity.Food
import com.wzk.domain.service.FoodService
import com.wzk.domain.room.dao.FoodDao
import com.wzk.wrapper.Result
import com.wzk.wrapper.sandbox

class FoodRepositoryImpl(
    private val foodDao: FoodDao,
    private val foodService: FoodService
) : FoodRepository {
    override suspend fun getById(id: Int): Result<Food> {
        return sandbox {
            foodService.getById(id).handle(foodDao::insert)
        }
    }

    override suspend fun getAll(): Result<List<Food>> {
        return sandbox {
            foodService.getAll()
                .handle { foods ->
                    foods.forEach { foodDao.insert(it) }
                }
        }
    }

    override suspend fun put(food: Food): Result<Food> {
        return sandbox {
            with(food) { foodService.put(name, price, description, img).handle(foodDao::insert) }
        }
    }
}