package com.linku.im.screen.introduce

import com.linku.im.screen.introduce.composable.Property

sealed class IntroduceEvent {
    object FetchIntroduce : IntroduceEvent()
    object ToggleLogMode : IntroduceEvent()
    object VerifiedEmail : IntroduceEvent()
    data class VerifiedEmailCode(val code: String) : IntroduceEvent()
    object CancelVerifiedEmail : IntroduceEvent()
    object SignOut : IntroduceEvent()
    data class Actions(
        val label: String,
        val actions: List<Property.Data.Action>
    ) : IntroduceEvent()
    data class Edit(val type: Type) : IntroduceEvent() {
        sealed class Type(private val code: Int) {
            object Name : Type(0)
            object NickName : Type(1)
            object Description : Type(2)

            override fun toString(): String = code.toString()

            companion object {
                fun parse(code: Int): Type? = when (code) {
                    0 -> Name
                    1 -> NickName
                    2 -> Description
                    else -> null
                }
            }
        }
    }
}
