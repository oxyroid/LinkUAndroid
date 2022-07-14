package com.linku.domain.service

import retrofit2.http.GET

interface OneWordService {
    @GET("https://v1.hitokoto.cn/")
    suspend fun hitokoto(): Hitokoto

    @GET("https://v.api.aa1.cn/api/api-wenan-wangyiyunreping/index.php?aa1=json")
    suspend fun netease(): List<Netease>

    data class Hitokoto(
        val commit_from: String,
        val created_at: String,
        val creator: String,
        val creator_uid: Int,
        val from: String,
        val from_who: Any,
        val hitokoto: String,
        val id: Int,
        val length: Int,
        val reviewer: Int,
        val type: String,
        val uuid: String
    )

    data class Netease(
        val wangyiyunreping: String
    )
}