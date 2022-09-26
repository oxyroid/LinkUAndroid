package com.linku.data.usecase

import android.content.Context
import android.net.Uri
import com.linku.domain.Resource
import com.linku.domain.bean.frigidity
import com.linku.domain.bean.midNight
import com.linku.domain.bean.seaSalt
import com.linku.domain.entity.local.Theme
import com.linku.domain.entity.local.toTheme
import com.linku.domain.room.dao.ThemeDao
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

sealed class SettingUseCases {
    class Notification() : SettingUseCases() {

    }

    class PrivacySecurity() : SettingUseCases() {

    }

    class DataStorage : SettingUseCases() {

    }

    data class Themes @Inject constructor(
        val installDefaultTheme: InstallDefaultTheme,
        val loadAllLocalTheme: LoadAllLocalTheme,
        val toggleIsDarkMode: ToggleIsDarkMode,
        val selectThemes: SelectThemes,
        val export: Export,
        val import: Import,
        val importFromClipboard: ImportFromClipboard,
        val findById: FindById
    ) : SettingUseCases() {
        data class InstallDefaultTheme @Inject constructor(
            private val themeDao: ThemeDao,
            private val sharedPreference: SharedPreferenceUseCase
        ) : SettingUseCases() {
            operator fun invoke(): Flow<Resource<Unit>> = flow {
                emit(Resource.Loading)
                runCatching {
                    var isAnyDefaultThemeNotInstalled = false
                    if (sharedPreference.lightTheme == Theme.NOT_EXIST_ID) {
                        isAnyDefaultThemeNotInstalled = true
                        val theme: Theme = seaSalt.toTheme()
                        val id = themeDao.insert(theme)
                        sharedPreference.lightTheme = id.toInt()
                    }
                    if (sharedPreference.darkTheme == Theme.NOT_EXIST_ID) {
                        isAnyDefaultThemeNotInstalled = true
                        val theme: Theme = midNight.toTheme()
                        val id = themeDao.insert(theme)
                        sharedPreference.darkTheme = id.toInt()
                    }
                    if (isAnyDefaultThemeNotInstalled) {
                        val theme: Theme = frigidity.toTheme()
                        themeDao.insert(theme)
                    }
                }
                    .onSuccess {
                        emit(Resource.Success(Unit))
                    }
                    .onFailure {
                        emit(Resource.Failure(it.message))
                    }
            }
        }

        data class LoadAllLocalTheme @Inject constructor(
            private val themeDao: ThemeDao
        ) {
            operator fun invoke(): Flow<Resource<List<Theme>>> = flow {
                emit(Resource.Loading)
                runCatching {
                    themeDao.getAll()
                }
                    .onSuccess { emit(Resource.Success(it)) }
                    .onFailure { emit(Resource.Failure(it.message)) }
            }
        }

        data class ToggleIsDarkMode @Inject constructor(
            private val sharedPreferenceUseCase: SharedPreferenceUseCase
        ) {
            operator fun invoke() {
                sharedPreferenceUseCase.isDarkMode = !sharedPreferenceUseCase.isDarkMode
            }
        }

        data class SelectThemes @Inject constructor(
            private val sharedPreferenceUseCase: SharedPreferenceUseCase,
            private val themeDao: ThemeDao
        ) {
            operator fun invoke(tid: Int): Flow<Resource<Theme>> = flow {
                emit(Resource.Loading)
                runCatching {
                    val theme = themeDao.getById(tid)
                    checkNotNull(theme) { "Cannot found Theme!" }
                    val isDark = theme.isDark
                    if (isDark) {
                        sharedPreferenceUseCase.darkTheme = theme.id
                    } else {
                        sharedPreferenceUseCase.lightTheme = theme.id
                    }
                    sharedPreferenceUseCase.isDarkMode = isDark
                    theme
                }
                    .onSuccess { emit(Resource.Success(it)) }
                    .onFailure { emit(Resource.Failure(it.message)) }

            }
        }

        data class Export @Inject constructor(
            private val themeDao: ThemeDao
        ) {
            operator fun invoke(tid: Int): Flow<Resource<Uri>> = flow {
                // TODO
            }
        }

        data class Import @Inject constructor(
            private val themeDao: ThemeDao
        ) {
            operator fun invoke(uri: Uri): Flow<Resource<Theme>> = flow {
                // TODO
            }
        }

        data class ImportFromClipboard @Inject constructor(
            private val themeDao: ThemeDao,
            @ApplicationContext private val context: Context
        ) {
            operator fun invoke(): Flow<Resource<Theme>> = flow {
                // TODO
            }
        }

        data class FindById @Inject constructor(
            private val themeDao: ThemeDao
        ) {
            suspend operator fun invoke(tid: Int): Theme? = themeDao.getById(tid)
        }
    }

    class Language : SettingUseCases() {

    }
}