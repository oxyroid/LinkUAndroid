package com.linku.im.screen.setting.theme

import androidx.lifecycle.viewModelScope
import com.linku.data.usecase.ApplicationUseCases
import com.linku.data.usecase.SettingUseCases
import com.linku.data.usecase.SharedPreferenceUseCase
import com.linku.domain.Resource
import com.linku.domain.entity.local.Theme
import com.linku.im.LinkUEvent
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
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class ThemeSettingViewModel @Inject constructor(
    private val themes: SettingUseCases.Themes,
    private val sharedPreference: SharedPreferenceUseCase,
    private val applications: ApplicationUseCases,
    private val json: Json
) : BaseViewModel<SettingState.Themes, SettingEvent.Themes>(SettingState.Themes()) {
    val allTheme: Flow<List<Theme>> = themes.observeAllLocalTheme()
    override fun onEvent(event: SettingEvent.Themes) {
        when (event) {
            SettingEvent.Themes.Init -> {
                writable = readable.copy(
                    currentTheme = if (sharedPreference.isDarkMode) sharedPreference.darkTheme
                    else sharedPreference.lightTheme,
                    defaultLightTheme = sharedPreference.lightTheme,
                    defaultDarkTheme = sharedPreference.darkTheme
                )
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
                                onMessage("Loading")
                            }
                            is Resource.Success -> {
                                onMessage("Success")
                            }
                            is Resource.Failure -> {
                                onMessage("Failed")
                            }
                        }
                    }
                    .launchIn(viewModelScope)
            }
            is SettingEvent.Themes.ImportFromClipboard -> {}
        }
    }
}
