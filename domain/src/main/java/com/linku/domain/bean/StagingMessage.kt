package com.linku.domain.bean

import android.net.Uri
import java.util.UUID

sealed class StagingMessage {
    data class Text(
        val cid: Int,
        val uid: Int,
        val text: String,
        val reply: Int?
    ) : StagingMessage()

    data class Image(
        val cid: Int,
        val uid: Int,
        val uri: Uri,
        val reply: Int?
    ) : StagingMessage()

    data class Graphics(
        val cid: Int,
        val uid: Int,
        val text: String,
        val uri: Uri,
        val reply: Int?
    ) : StagingMessage()

    val uuid: String by lazy { UUID.randomUUID().toString() }
}