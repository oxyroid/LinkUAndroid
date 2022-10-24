package com.linku.data.service

import android.content.Context
import android.graphics.BitmapFactory
import com.linku.domain.wrapper.Resource
import com.linku.domain.bean.Emoji
import com.linku.domain.service.EmojiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.FileNotFoundException

class TwitterEmojiService(
    private val context: Context
) : EmojiService {
    private val cached = mutableListOf<Emoji>()
    override fun initialize(): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        try {
            context.assets.open("emoji-test.config").reader().buffered().use { reader ->
                val list = reader.readLines()
                list.onEach { line ->
                    if (line.isNotBlank() && !line.startsWith('#')) {
                        val split = line.split(';', '#')
                        val unicode = split.first().split(' ').first().trim().lowercase()
                        val emoji = split[2].split('E').first().trim()
                        val bitmap = context.assets.open("emoji/$unicode.png")
                            .use { BitmapFactory.decodeStream(it) }
                        Emoji(unicode, emoji, bitmap).also(cached::add)
                    }
                }
            }
        } catch (ignored: FileNotFoundException) {

        } catch (e: Exception) {
            emit(Resource.Failure(e.message ?: ""))
        }

        emit(Resource.Success(Unit))
    }

    override fun emojis(): List<Emoji> = cached
}
