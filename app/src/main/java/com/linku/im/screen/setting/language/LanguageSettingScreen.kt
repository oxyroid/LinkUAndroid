package com.linku.im.screen.setting.language

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.*
import com.linku.im.R
import com.linku.im.screen.setting.BasicSettingScreen
import com.linku.im.ui.theme.LocalTheme

@OptIn(ExperimentalTextApi::class)
@Composable
fun LanguageSettingScreen(
    modifier: Modifier = Modifier
) {
    BasicSettingScreen(
        title = stringResource(R.string.profile_settings_language),
        content = {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                val theme = LocalTheme.current
                val uriHandler = LocalUriHandler.current
                val text = buildAnnotatedString {
                    withStyle(
                        SpanStyle(color = LocalContentColor.current)
                    ) {
                        append("点击参与")
                        withAnnotation(
                            tag = "URL",
                            annotation = "https://github.com/thxbrop/LinkU-Android/discussions/15#discussion-4582264",
                        ) {
                            withStyle(
                                SpanStyle(color = theme.primary)
                            ) {
                                append("国际化")
                            }
                        }
                        append("建设")
                    }

                }
                ClickableText(
                    text = text,
                    onClick = { position ->
                        text.getStringAnnotations("URL", position, position)
                            .firstOrNull()
                            ?.let {
                                uriHandler.openUri(it.item)
                            }
                    }
                )
            }
        },
        modifier = modifier
    )
}
