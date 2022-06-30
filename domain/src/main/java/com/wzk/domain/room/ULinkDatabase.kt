package com.wzk.domain.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.wzk.domain.entity.User
import com.wzk.domain.room.dao.UserDao

@Database(
    entities = [User::class],
    version = 1,
    exportSchema = false
)
abstract class ULinkDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}