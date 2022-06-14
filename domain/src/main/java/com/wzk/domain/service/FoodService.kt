package com.wzk.domain.service

import com.wzk.domain.entity.Food
import com.wzk.wrapper.Result
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface FoodService {
    @GET("/food/{id}")
    suspend fun getById(@Path("id") id: Int): Result<Food>

    @GET("/food")
    suspend fun getAll(): Result<List<Food>>

    @PUT("/food")
    suspend fun put(
        @Query("name") name: String,
        @Query("price") price: Float,
        @Query("description") description: String?,
        @Query("img") img: String
    ): Result<Food>
}

