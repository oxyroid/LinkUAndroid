package com.linku.data.usecase

import android.content.Context
import android.net.Uri
import com.linku.core.extension.json
import com.linku.core.wrapper.Resource
import com.linku.data.Configurations
import com.linku.data.R
import com.linku.domain.bean.frigidity
import com.linku.domain.bean.midNight
import com.linku.domain.bean.seaSalt
import com.linku.domain.entity.Theme
import com.linku.domain.entity.toTheme
import com.linku.domain.room.dao.ThemeDao
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import javax.inject.Inject

sealed interface SettingUseCases {
    class Notification() : SettingUseCases {

    }

    class PrivacySecurity() : SettingUseCases {

    }

    class DataStorage : SettingUseCases {

    }

    data class Themes @Inject constructor(
        val installDefaultTheme: InstallDefaultThemeUseCase,
        val observeAllLocalTheme: ObserveAllLocalThemeUseCase,
        val toggleIsDarkMode: ToggleIsDarkModeUseCase,
        val selectThemes: SelectThemesUseCase,
        val import: ImportUseCase,
        val importFromClipboard: ImportFromClipboardUseCase,
        val findById: FindByIdUseCase,
        val deleteById: DeleteByIdUseCase
    ) : SettingUseCases {
        data class InstallDefaultThemeUseCase @Inject constructor(
            private val themeDao: ThemeDao,
            private val configurations: Configurations
        ) : SettingUseCases {
            operator fun invoke(): Flow<Resource<Unit>> = flow {
                emit(Resource.Loading)
                runCatching {
                    var isAnyDefaultThemeNotInstalled = false
                    if (configurations.lightTheme == Theme.NOT_EXIST_ID) {
                        isAnyDefaultThemeNotInstalled = true
                        val theme: Theme = seaSalt.toTheme()
                        val id = themeDao.insert(theme)
                        configurations.lightTheme = id.toInt()
                    }
                    if (configurations.darkTheme == Theme.NOT_EXIST_ID) {
                        isAnyDefaultThemeNotInstalled = true
                        val theme: Theme = midNight.toTheme()
                        val id = themeDao.insert(theme)
                        configurations.darkTheme = id.toInt()
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

        data class ObserveAllLocalThemeUseCase @Inject constructor(
            private val themeDao: ThemeDao
        ) {
            operator fun invoke(): Flow<List<Theme>> = themeDao.observeAll()
        }

        data class ToggleIsDarkModeUseCase @Inject constructor(
            private val configurations: Configurations
        ) {
            operator fun invoke() {
                configurations.isDarkMode = !configurations.isDarkMode
            }
        }

        data class SelectThemesUseCase @Inject constructor(
            private val configurations: Configurations,
            private val themeDao: ThemeDao
        ) {
            operator fun invoke(tid: Int): Flow<Resource<Theme>> = flow {
                emit(Resource.Loading)
                runCatching {
                    val theme = themeDao.getById(tid)
                    checkNotNull(theme) { "Cannot found Theme!" }
                    val isDark = theme.isDark
                    if (isDark) {
                        configurations.darkTheme = theme.id
                    } else {
                        configurations.lightTheme = theme.id
                    }
                    configurations.isDarkMode = isDark
                    theme
                }
                    .onSuccess { emit(Resource.Success(it)) }
                    .onFailure { emit(Resource.Failure(it.message)) }

            }
        }


        data class ImportUseCase @Inject constructor(
            private val themeDao: ThemeDao,
            private val applications: ApplicationUseCases
        ) {
            operator fun invoke(uri: Uri): Flow<Resource<Theme>> = flow {
                emit(Resource.Loading)
                try {
                    applications.contentResolver().openInputStream(uri).use {
                        if (it == null) {
                            emit(Resource.Failure(applications.getString(R.string.error_import)))
                            return@flow
                        }
                        val s = it.readBytes().decodeToString()
                        val theme: Theme = json.decodeFromString<Theme>(s).copy(
                            id = 0
                        )
                        themeDao.insert(theme)
                        emit(Resource.Success(theme))
                    }
                } catch (e: SerializationException) {
                    emit(Resource.Failure(applications.getString(R.string.error_import)))
                } catch (e: IllegalArgumentException) {
                    emit(Resource.Failure(applications.getString(R.string.error_import)))
                }
            }
        }

        data class ImportFromClipboardUseCase @Inject constructor(
            private val themeDao: ThemeDao,
            @ApplicationContext private val context: Context
        ) {
            operator fun invoke(): Flow<Resource<Theme>> = flow {
                // TODO
            }
        }

        data class FindByIdUseCase @Inject constructor(
            private val themeDao: ThemeDao
        ) {
            suspend operator fun invoke(tid: Int): Theme? = themeDao.getById(tid)
        }

        data class DeleteByIdUseCase @Inject constructor(
            private val themeDao: ThemeDao
        ) {
            suspend operator fun invoke(tid: Int) = themeDao.deleteById(tid)
        }
    }

    class Language : SettingUseCases {

    }
}
