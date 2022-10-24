package com.linku.im.appyx.target

import android.os.Parcelable
import androidx.compose.ui.geometry.Rect
import com.linku.im.screen.introduce.IntroduceEvent
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

sealed class NavTarget : Parcelable {
    @Parcelize
    object Main : NavTarget()

    sealed class ChatTarget(open val cid: Int) : NavTarget() {
        @Parcelize
        data class Messages(override val cid: Int) : ChatTarget(cid)

        @Parcelize
        data class ImageDetail(
            override val cid: Int,
            val url: String,
            val boundaries: @RawValue Rect,
            val aspectRatio: Float = 4 / 3f
        ) : ChatTarget(cid)

        @Parcelize
        data class MemberDetail(override val cid: Int, val mid: Int) : ChatTarget(cid)

        @Parcelize
        data class ChannelDetail(override val cid: Int) : ChatTarget(cid)
    }

    @Parcelize
    data class Introduce(val uid: Int) : NavTarget()

    @Parcelize
    object Sign : NavTarget()

    @Parcelize
    object Query : NavTarget()

    @Parcelize
    data class Edit(val type: IntroduceEvent.Edit.Type?) : NavTarget()

    sealed class Setting : NavTarget() {
        @Parcelize
        object Notification : Setting()

        @Parcelize
        object Safe : Setting()

        @Parcelize
        object DataSource : Setting()

        @Parcelize
        object Theme : Setting()

        @Parcelize
        object Language : Setting()
    }
}
