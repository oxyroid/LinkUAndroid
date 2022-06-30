package com.linku.domain.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.linku.domain.entity.User
import com.linku.domain.room.dao.UserDao

@Database(
    entities = [User::class],
    version = 1,
    exportSchema = false
)
abstract class ULinkDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}