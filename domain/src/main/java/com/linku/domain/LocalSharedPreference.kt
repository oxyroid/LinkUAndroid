package com.linku.domain

import com.tencent.mmkv.MMKV
import com.linku.domain.entity.User
import com.linku.domain.room.dao.UserDao
import javax.inject.Inject

class LocalSharedPreference @Inject constructor(
    private val userDao: UserDao
) {
    fun setLocalUser(user: User) = MMKV.defaultMMKV().encode(
        MMKV_USER_ID,
        user.id
    )

    companion object {
        const val MMKV_USER_ID = "mmkv:userId"

        fun getLocalUserId() = MMKV.defaultMMKV().decodeInt(MMKV_USER_ID)
    }
}