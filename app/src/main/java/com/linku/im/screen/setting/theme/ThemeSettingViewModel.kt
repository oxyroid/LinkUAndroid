package com.linku.im.screen.setting.theme

import androidx.lifecycle.viewModelScope
import com.linku.data.usecase.SettingUseCases
import com.linku.data.usecase.SharedPreferenceUseCase
import com.linku.domain.Resource
import com.linku.im.LinkUEvent
import com.linku.im.screen.BaseViewModel
import com.linku.im.screen.setting.SettingEvent
import com.linku.im.screen.setting.SettingState
import com.linku.im.vm
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class ThemeSettingViewModel @Inject constructor(
    private val themes: SettingUseCases.Themes,
    private val sharedPreference: SharedPreferenceUseCase
) : BaseViewModel<SettingState.Themes, SettingEvent.Themes>(SettingState.Themes()) {
    override fun onEvent(event: SettingEvent.Themes) {
        when (event) {
            SettingEvent.Themes.Init -> {
                writable = readable.copy(
                    currentTheme = if (sharedPreference.isDarkMode) sharedPreference.darkTheme
                    else sharedPreference.lightTheme,
                    defaultLightTheme = sharedPreference.lightTheme,
                    defaultDarkTheme = sharedPreference.darkTheme
                )
                themes.loadAllLocalTheme()
                    .onEach { resource ->
                        writable = when (resource) {
                            Resource.Loading -> readable.copy(
                                loading = true
                            )
                            is Resource.Success -> readable.copy(
                                loading = false,
                                themes = resource.data
                            )
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
            SettingEvent.Themes.ToggleIsDarkMode -> {
                themes.toggleIsDarkMode()
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
            is SettingEvent.Themes.Export -> {}
            SettingEvent.Themes.Import -> {}
            is SettingEvent.Themes.ImportFromClipboard -> {}
        }
    }
}