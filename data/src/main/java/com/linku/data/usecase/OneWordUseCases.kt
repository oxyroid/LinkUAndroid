package com.linku.data.usecase

import com.linku.domain.Resource
import com.linku.domain.emitResource
import com.linku.domain.resourceFlow
import com.linku.domain.service.OneWordService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

data class OneWordUseCases @Inject constructor(
    val hitokotoUseCase: HitokotoUseCase,
    val neteaseUseCase: NeteaseUseCase,
)

data class HitokotoUseCase(
    private val service: OneWordService
) {
    operator fun invoke(): Flow<Resource<OneWordService.Hitokoto>> = resourceFlow {
        try {
            val hitokoto = service.hitokoto()
            emitResource(hitokoto)
        } catch (e: Exception) {
            e.printStackTrace()
            emitResource("Hitokoto Error","ThreePartUseCases")
        }
    }
}

data class NeteaseUseCase(
    private val service: OneWordService
) {
    operator fun invoke(): Flow<Resource<String>> = resourceFlow {
        try {
            val netease = service.netease().firstOrNull()
            checkNotNull(netease)
            emitResource(netease.wangyiyunreping)
        } catch (e: Exception) {
            e.printStackTrace()
            emitResource("Netease Error","ThreePartUseCases")
        }
    }
}
