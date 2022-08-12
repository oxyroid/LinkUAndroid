package com.linku.domain.room.dao

import androidx.room.*
import com.linku.domain.entity.User

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User)

    @Query("UPDATE User SET avatar = :avatar WHERE id = :uid")
    suspend fun updateAvatar(uid: Int, avatar: String?)

    @Delete
    suspend fun delete(user: User)

    @Query("DELETE FROM User WHERE id =:id")
    suspend fun delete(id: Int)

    @Query("SELECT * FROM User")
    suspend fun getAll(): List<User>

    @Query("SELECT * FROM User WHERE id = :id")
    suspend fun getById(id: Int): User?

    @Query("DELETE FROM USER")
    suspend fun clear()
}