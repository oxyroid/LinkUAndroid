package com.linku.domain.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.linku.domain.entity.Message
import com.linku.domain.entity.User
import com.linku.domain.room.dao.MessageDao
import com.linku.domain.room.dao.UserDao

@Database(
    entities = [User::class, Message::class],
    version = 1,
    exportSchema = false
)
abstract class ULinkDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun messageDao(): MessageDao
}