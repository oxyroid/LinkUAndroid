package com.linku.im.screen.introduce

import com.linku.im.screen.introduce.composable.Property

sealed class IntroduceEvent {
    object FetchIntroduce : IntroduceEvent()
    object VerifiedEmail : IntroduceEvent()
    data class VerifiedEmailCode(val code: String) : IntroduceEvent()
    object CancelVerifiedEmail : IntroduceEvent()
    object SignOut : IntroduceEvent()
    data class Actions(
        val label: String,
        val actions: List<Property.Data.Action>
    ) : IntroduceEvent()
}
