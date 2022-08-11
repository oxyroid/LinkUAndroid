package com.linku.data.service

import android.content.Context
import android.graphics.BitmapFactory
import com.linku.domain.Resource
import com.linku.domain.bean.Emoji
import com.linku.domain.emitResource
import com.linku.domain.resourceFlow
import com.linku.domain.service.EmojiService
import kotlinx.coroutines.flow.Flow

class TwitterEmojiService(
    private val context: Context
) : EmojiService {
    private val cached = mutableListOf<Emoji>()
    override fun initialize(): Flow<Resource<Unit>> = resourceFlow {
        context.assets.open("emoji-test.config")
            .reader()
            .buffered()
            .use { reader ->
                val list = reader.readLines()
                list.onEachIndexed { index, line ->
                    if (line.isNotBlank() && !line.startsWith('#')) {
                        val split = line.split(';', '#')
                        val unicode =
                            split.first().split(' ').first().trim().lowercase()
                        val emoji = split[2].split('E').first().trim()
                        val bitmap = context
                            .assets
                            .open("emoji/$unicode.png")
                            .use { BitmapFactory.decodeStream(it) }
                        Emoji(unicode, emoji, bitmap).also(cached::add)
                    }
                    if (index == list.size - 1) emitResource(Unit)
                }
            }
    }

    override fun emojis(): List<Emoji> = cached
}