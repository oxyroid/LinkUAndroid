package com.linku.domain.service

import androidx.annotation.Keep
import com.linku.domain.BuildConfig
import kotlinx.serialization.Serializable
import retrofit2.http.GET

interface OneWordService {
    @GET(BuildConfig.HITOKOTO_URL)
    suspend fun hitokoto(): Hitokoto
}

@Serializable
@Keep
data class Hitokoto(
    val from: String,
    val hitokoto: String,
    val uuid: String
)