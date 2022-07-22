package com.linku.im.screen.query

sealed class QueryEvent {
    object Query : QueryEvent()
    data class OnText(val text: String) : QueryEvent()
    object ToggleIncludeDescription : QueryEvent()
}
