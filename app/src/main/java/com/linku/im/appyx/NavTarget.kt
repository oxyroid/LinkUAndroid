package com.linku.im.appyx

import android.os.Parcelable
import com.linku.im.screen.introduce.IntroduceEvent
import kotlinx.parcelize.Parcelize

sealed class NavTarget: Parcelable {
    @Parcelize
    object Main : NavTarget()

    @Parcelize
    data class Chat(val cid: Int) : NavTarget()

    @Parcelize
    data class Introduce(val uid: Int) : NavTarget()

    @Parcelize
    object Sign : NavTarget()

    @Parcelize
    object Create : NavTarget()

    @Parcelize
    object Query : NavTarget()

    @Parcelize
    data class Edit(val type: IntroduceEvent.Edit.Type?) : NavTarget()
}
