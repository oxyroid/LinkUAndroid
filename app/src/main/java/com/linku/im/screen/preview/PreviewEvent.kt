package com.linku.im.screen.preview

import com.linku.domain.entity.Message

sealed class PreviewEvent {
    data class GetImageUrl(val mid: Int) : PreviewEvent()
}
