package com.wzk.domain.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.wzk.domain.entity.Food
import com.wzk.domain.entity.User
import com.wzk.domain.room.dao.FoodDao
import com.wzk.domain.room.dao.UserDao

@Database(
    entities = [Food::class, User::class],
    version = 1,
    exportSchema = false
)
abstract class MyDatabase : RoomDatabase() {
    abstract fun foodDao(): FoodDao
    abstract fun userDao(): UserDao
}