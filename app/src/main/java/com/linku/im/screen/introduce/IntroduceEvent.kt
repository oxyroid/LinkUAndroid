package com.linku.im.screen.introduce

import android.net.Uri
import android.os.Parcelable
import com.linku.im.screen.introduce.composable.Property
import kotlinx.parcelize.Parcelize

sealed class IntroduceEvent {
    data class FetchIntroduce(val uid: Int) : IntroduceEvent()
    object ToggleLogMode : IntroduceEvent()
    object VerifiedEmail : IntroduceEvent()
    data class VerifiedEmailCode(val code: String) : IntroduceEvent()
    object CancelVerifiedEmail : IntroduceEvent()
    object SignOut : IntroduceEvent()
    object AvatarClicked : IntroduceEvent()
    object FriendShipAction : IntroduceEvent()

    data class UpdateAvatar(val uri: Uri?) : IntroduceEvent()

    data class Actions(
        val label: String,
        val actions: List<Property.Data.Action>
    ) : IntroduceEvent()

    data class Edit(val type: Type) : IntroduceEvent() {
        sealed class Type(val code: Int) : Parcelable {
            @Parcelize
            object Name : Type(0)

            @Parcelize
            object NickName : Type(1)

            @Parcelize
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
