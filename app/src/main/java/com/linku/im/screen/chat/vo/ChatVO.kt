package com.linku.im.screen.chat.vo

import com.linku.data.usecase.UserUseCases
import com.linku.domain.Strategy
import com.linku.im.Constants
import com.linku.im.extension.ifTrue

sealed class ChatVO {
    data class Message(
        val message: com.linku.domain.entity.Message,
        val other: Boolean
    ) : ChatVO()

    data class TimeSeparator(val timestamp: Long) : ChatVO()
    data class UserSeparator(
        val uid: Int,
        val avatar: String
    ) : ChatVO()
}

infix fun ChatVO?.calculateTimeSeparator(
    after: ChatVO?
): ChatVO.TimeSeparator? = when (val before = this) {
    is ChatVO.Message -> {
        val duration = Constants.CHAT_LABEL_MIN_DURATION
        when (after) {
            is ChatVO.Message ->
                (after.message.timestamp - before.message.timestamp >= duration).ifTrue {
                    ChatVO.TimeSeparator(after.message.timestamp)
                }
            else -> null
        }
    }
    is ChatVO.TimeSeparator -> null
    is ChatVO.UserSeparator -> null
    null -> when (after) {
        is ChatVO.Message -> ChatVO.TimeSeparator(after.message.timestamp)
        else -> null
    }
}

/**
 * If after is already [ChatVO.UserSeparator] => null.
 * If before is not data => null.
 * If before is data and after is [ChatVO.TimeSeparator] => separator.
 * If before and after are data and before is other but after is not other => separator.
 * If before and after are data and before is not other => null.
 */

context(UserUseCases) suspend infix fun ChatVO?.calculateAvatarSeparator(
    after: ChatVO?
): ChatVO.UserSeparator? {
    val before = this
    suspend fun separator(
        uid: Int
    ): ChatVO.UserSeparator = ChatVO.UserSeparator(
        uid,
        findUser(uid, Strategy.Memory).let { it?.avatar.orEmpty() },
    )
    return when {
        after is ChatVO.UserSeparator -> null
        before is ChatVO.Message -> when (after) {
            is ChatVO.Message ->
                if (before.other && !after.other) separator(before.message.uid)
                else null
            is ChatVO.TimeSeparator -> separator(before.message.uid)
            else -> null
        }
        else -> null
    }
}