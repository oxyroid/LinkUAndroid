package com.wzk.domain

import com.tencent.mmkv.MMKV
import com.wzk.domain.common.Constants
import com.wzk.domain.entity.User
import com.wzk.domain.room.dao.UserDao
import com.wzk.wrapper.Result
import javax.inject.Inject

class LocalSharedPreference @Inject constructor(
    private val userDao: UserDao
) {
    fun setLocalUser(user: User) = MMKV.defaultMMKV().encode(
        if (isMockMode) MMKV_MOCK_USER_ID else MMKV_USER_ID,
        user.id
    )

    fun addToCart(foodId: Int): Result<Int> {
        val key = if (isMockMode) MMKV_MOCK_CART else MMKV_CART
        val set: MutableSet<String> = MMKV.defaultMMKV().decodeStringSet(key) ?: mutableSetOf()
        if (set.contains(foodId.toString()))
            return Result(
                code = 999,
                message = "?"
            )
        set.add(foodId.toString())
        MMKV.defaultMMKV().encode(key, set)
        return Result(data = foodId)
    }


    suspend fun getLocalUser() = getLocalUserId().let { userDao.getById(it) }

    companion object {
        const val MMKV_USER_ID = "mmkv:userId"
        const val MMKV_MOCK_USER_ID = "mmkv:userId-mock"
        const val MMKV_CART = "mmkv:cart"
        const val MMKV_MOCK_CART = "mmkv:cart-mock"
        private const val isMockMode = Constants.MOCK_MODE

        fun getLocalUserId() = MMKV.defaultMMKV().decodeInt(
            if (isMockMode) MMKV_MOCK_USER_ID else MMKV_USER_ID
        )

        fun getCart(): List<Int> = run {
            val key = if (isMockMode) MMKV_MOCK_CART else MMKV_CART
            val set = MMKV.defaultMMKV().decodeStringSet(key) ?: emptySet()
            set.map { it.toInt() }
        }
    }
}