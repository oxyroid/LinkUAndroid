package com.linku.im.screen.query

import androidx.compose.ui.text.input.TextFieldValue

sealed class QueryEvent {
    object Query : QueryEvent()
    data class OnText(val text: TextFieldValue) : QueryEvent()
    object ToggleIncludeDescription : QueryEvent()
}
