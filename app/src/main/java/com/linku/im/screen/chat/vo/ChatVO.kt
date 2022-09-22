package com.linku.im.screen.chat.vo

import androidx.annotation.Keep
import com.linku.data.usecase.UserUseCases
import com.linku.domain.Strategy
import com.linku.domain.entity.Message
import com.linku.im.Constants
import com.linku.im.extension.ifTrue
import com.linku.im.screen.chat.composable.BubbleConfig

@Keep
data class MessageVO(
    val message: Message,
    val config: BubbleConfig
)

sealed class ChatVO {
    data class Data(
        val message: Message,
        val other: Boolean
    ) : ChatVO()
    data class TimeSeparator(val timestamp: Long) : ChatVO()
    data class AvatarSeparator(
        val avatar: String
    ) : ChatVO()
}

infix fun ChatVO?.calculateTimeSeparator(
    after: ChatVO?
): ChatVO.TimeSeparator? = when (val before = this) {
    is ChatVO.Data -> {
        val duration = Constants.CHAT_LABEL_MIN_DURATION
        when (after) {
            is ChatVO.Data ->
                (after.message.timestamp - before.message.timestamp >= duration).ifTrue {
                    ChatVO.TimeSeparator(after.message.timestamp)
                }
            else -> null
        }
    }
    is ChatVO.TimeSeparator -> null
    is ChatVO.AvatarSeparator -> null
    null -> when (after) {
        is ChatVO.Data -> ChatVO.TimeSeparator(after.message.timestamp)
        else -> null
    }
}

/**
 * If after is already [ChatVO.AvatarSeparator] => null.
 * If before is not data => null.
 * If before is data and after is [ChatVO.TimeSeparator] => separator.
 * If before and after are data and before is other but after is not other => separator.
 * If before and after are data and before is not other => null.
 */

context(UserUseCases) suspend infix fun ChatVO?.calculateAvatarSeparator(
    after: ChatVO?
): ChatVO.AvatarSeparator?  {
    val before = this
    suspend fun separator(
        uid: Int
    ): ChatVO.AvatarSeparator = ChatVO.AvatarSeparator(
        findUser(uid, Strategy.Memory).let { it?.avatar.orEmpty() },
    )
    return when {
        after is ChatVO.AvatarSeparator -> null
        before is ChatVO.Data -> when(after) {
            is ChatVO.Data ->
                if (before.other && !after.other) separator(before.message.uid)
                else null
            is ChatVO.TimeSeparator -> separator(before.message.uid)
            else -> null
        }
        else -> null
    }
}