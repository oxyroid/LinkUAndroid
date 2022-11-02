package com.linku.data.service.system

import android.content.Context
import android.graphics.BitmapFactory
import com.linku.core.wrapper.Resource
import com.linku.domain.bean.Emoji
import com.linku.domain.service.system.EmojiService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.FileNotFoundException
import javax.inject.Inject

class EmojiServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : EmojiService {
    private val emojis = mutableListOf<Emoji>()
    override fun initialize(): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        try {
            (context.assets.list("emoji") ?: emptyArray()).forEach {
                val name = it.split('.').first()
                val emoji = try {
                    Emoji(
                        unicode = name.lowercase(),
                        emoji = name.toEmoji(),
                        bitmap = context.assets
                            .open("emoji/$it")
                            .use(BitmapFactory::decodeStream)
                    )
                } catch (ignored: FileNotFoundException) {
                    null
                }
                if (emoji != null) {
                    emojis.add(emoji)
                }
            }
        } catch (e: Exception) {
            emit(Resource.Failure(e.message ?: ""))
        } finally {
            emit(Resource.Success(Unit))
        }
    }

    override fun emojis(): List<Emoji> {
        return emojis
    }

    private fun String.toEmoji(): String {
        return String(Character.toChars(this.toInt(16)))
    }
}
