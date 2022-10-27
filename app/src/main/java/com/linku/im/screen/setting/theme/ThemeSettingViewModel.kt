package com.linku.im.screen.setting.theme

import androidx.lifecycle.viewModelScope
import com.linku.core.extension.json
import com.linku.core.wrapper.Resource
import com.linku.data.Configurations
import com.linku.data.usecase.ApplicationUseCases
import com.linku.data.usecase.SettingUseCases
import com.linku.domain.entity.Theme
import com.linku.im.LinkUEvent
import com.linku.im.R
import com.linku.im.screen.BaseViewModel
import com.linku.im.screen.setting.SettingEvent
import com.linku.im.screen.setting.SettingState
import com.linku.im.vm
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import javax.inject.Inject

@HiltViewModel
class ThemeSettingViewModel @Inject constructor(
    private val themes: SettingUseCases.Themes,
    private val configurations: Configurations,
    private val applications: ApplicationUseCases,
) : BaseViewModel<SettingState.Themes, SettingEvent.Themes>(SettingState.Themes()) {
    val allTheme: Flow<List<Theme>> = themes.observeAllLocalTheme()
    override fun onEvent(event: SettingEvent.Themes) {
        when (event) {
            SettingEvent.Themes.Init -> {
                writable = readable.copy(
                    currentTheme = if (configurations.isDarkMode) configurations.darkTheme
                    else configurations.lightTheme,
                    defaultLightTheme = configurations.lightTheme,
                    defaultDarkTheme = configurations.darkTheme
                )
            }

            SettingEvent.Themes.ToggleIsDarkMode -> {
                themes.toggleIsDarkMode()
            }

            SettingEvent.Themes.PressedCancel -> {
                writable = readable.copy(
                    currentPressedTheme = -1
                )
            }

            SettingEvent.Themes.DeletePressedTheme -> {
                val id = readable.currentPressedTheme
                if (id == readable.currentTheme) {
                    onMessage(applications.getString(R.string.theme_error_delete_current))
                    return
                }
                if (id <= 3) {
                    onMessage(applications.getString(R.string.theme_error_delete_preset))
                    return
                }
                viewModelScope.launch {
                    themes.deleteById(readable.currentPressedTheme)
                }
            }

            is SettingEvent.Themes.Press -> {
                writable = readable.copy(
                    currentPressedTheme = event.tid
                )
            }

            is SettingEvent.Themes.SelectThemes -> {
                themes.selectThemes(event.tid)
                    .onEach { resource ->
                        writable = when (resource) {
                            Resource.Loading -> readable.copy(
                                loading = true
                            )

                            is Resource.Success -> {
                                val theme = resource.data
                                vm.onEvent(LinkUEvent.OnTheme(theme.id, theme.isDark))
                                readable.copy(
                                    loading = false,
                                    currentTheme = resource.data.id
                                )
                            }

                            is Resource.Failure -> {
                                onMessage(resource.message)
                                readable.copy(
                                    loading = false
                                )
                            }
                        }
                    }
                    .launchIn(viewModelScope)
            }

            is SettingEvent.Themes.WriteThemeToUri -> {
                val tid = event.tid
                viewModelScope.launch {
                    val theme = themes.findById(tid)
                    val text = json.encodeToString(theme)
                    withContext(Dispatchers.Main) {
                        applications.contentResolver().openOutputStream(event.uri)?.use {
                            withContext(Dispatchers.IO) {
                                it.write(text.toByteArray())
                            }
                        }
                    }
                }
            }

            is SettingEvent.Themes.Import -> {
                themes.import(event.uri)
                    .onEach { resource ->
                        when (resource) {
                            Resource.Loading -> {
                                onMessage(applications.getString(R.string.theme_import_loading))
                            }

                            is Resource.Success -> {
                                onMessage(applications.getString(R.string.theme_import_success))
                            }

                            is Resource.Failure -> {
                                onMessage(resource.message)
                            }
                        }
                    }
                    .launchIn(viewModelScope)
            }

            is SettingEvent.Themes.ImportFromClipboard -> {}
        }
    }
}
