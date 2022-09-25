package com.linku.im.ktx.compose.layout

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable

typealias Content = @Composable () -> Unit
typealias BoxContent = @Composable BoxScope.() -> Unit
typealias RowContent = @Composable RowScope.() -> Unit
typealias ColumnContent = @Composable ColumnScope.() -> Unit

context(BoxScope)
fun Content.toBoxContent(): BoxContent = { this@toBoxContent.invoke() }

context(RowScope)
fun Content.toRowContent(): RowContent = { this@toRowContent.invoke() }

context(ColumnScope)
fun Content.toColumnContent(): ColumnContent = { this@toColumnContent.invoke() }