package com.linku.im.screen.sign.composable

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.linku.im.extension.ifTrue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginTextField(
    value: String,
    onValueChange: (String) -> Unit,
    @StringRes titleRes: Int,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    type: KeyboardType = KeyboardType.Text,
    focus: Boolean = false
) {
    val focusRequester = remember(::FocusRequester)
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(id = titleRes)) },
        modifier = modifier
            .padding(horizontal = 48.dp)
            .also {
                focus.ifTrue {
                    it.focusRequester(focusRequester)
                }
            },
        singleLine = true,
        textStyle = TextStyle(
            fontWeight = FontWeight.Bold
        ),
        enabled = enabled,
        keyboardOptions = KeyboardOptions(
            keyboardType = type
        )
    )
}