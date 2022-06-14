package com.wzk.domain.usecase

import com.wzk.domain.LocalSharedPreference
import com.wzk.domain.entity.Food
import com.wzk.domain.repository.food.FoodRepository
import com.wzk.wrapper.Resource
import com.wzk.wrapper.emitResource
import com.wzk.wrapper.resourceFlow
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

data class FoodUseCases @Inject constructor(
    val fetchListUseCase: FetchFoodsUseCase,
    val findUseCase: FindFoodUseCase,
    val addToCartUseCase: AddToCartUseCase,
    val loadCartUseCase: LoadCartUseCase
)

data class FetchFoodsUseCase(
    private val repository: FoodRepository
) {
    operator fun invoke(): Flow<Resource<List<Food>>> = resourceFlow {
        repository.getAll()
            .handle(::emitResource)
            .catch(::emitResource)
    }
}

data class FindFoodUseCase(
    private val repository: FoodRepository
) {
    operator fun invoke(id: Int): Flow<Resource<Food>> = resourceFlow {
        repository.getById(id)
            .handle(::emitResource)
            .catch(::emitResource)
    }
}

data class AddToCartUseCase(
    private val localSharedPreference: LocalSharedPreference
) {
    operator fun invoke(foodId: Int): Flow<Resource<Int>> = resourceFlow {
        localSharedPreference.addToCart(foodId)
            .handle(::emitResource)
            .catch(::emitResource)
    }
}

data class LoadCartUseCase(
    private val foodRepository: FoodRepository
) {
    suspend operator fun invoke(): Flow<Resource<List<Food>>> = resourceFlow {
        LocalSharedPreference.getCart()
            .map { foodRepository.getById(it) }
            .filter { it.isSuccess }
            .map { it.peek() }
            .also { emitResource(it) }
    }
}