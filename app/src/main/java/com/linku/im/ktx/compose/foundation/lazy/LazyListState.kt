package com.linku.im.ktx.compose.foundation.lazy

import androidx.compose.foundation.lazy.LazyListState

val LazyListState.isAtTop
    get() = firstVisibleItemIndex == 0 && firstVisibleItemScrollOffset == 0

val LazyListState.isScrolled
    get() = firstVisibleItemIndex > 0 || firstVisibleItemScrollOffset > 0

