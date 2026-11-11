package ru.vitaliy.belyaev.wishapp.data.repository.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import ru.vitaliy.belyaev.wishapp.data.repository.datastore.DataStoreRepository.PreferencesKeys.KEY_IS_BACKUP_WAS_CREATED
import ru.vitaliy.belyaev.wishapp.data.repository.datastore.DataStoreRepository.PreferencesKeys.KEY_IS_RESTORE_FROM_BACKUP_WAS_SUCCEED
import ru.vitaliy.belyaev.wishapp.data.repository.datastore.DataStoreRepository.PreferencesKeys.KEY_POSITIVE_ACTIONS_COUNT
import ru.vitaliy.belyaev.wishapp.data.repository.datastore.DataStoreRepository.PreferencesKeys.KEY_REVIEW_REQUEST_SHOWN_COUNT
import ru.vitaliy.belyaev.wishapp.data.repository.datastore.DataStoreRepository.PreferencesKeys.KEY_SORT_MODE
import ru.vitaliy.belyaev.wishapp.data.repository.datastore.DataStoreRepository.PreferencesKeys.KEY_THEME
import ru.vitaliy.belyaev.wishapp.data.repository.datastore.DataStoreRepository.PreferencesKeys.KEY_UI_LOOK
import ru.vitaliy.belyaev.wishapp.data.repository.datastore.DataStoreRepository.PreferencesKeys.KEY_WALLPAPER_COLORS
import ru.vitaliy.belyaev.wishapp.domain.model.Theme
import ru.vitaliy.belyaev.wishapp.domain.model.UiLook
import ru.vitaliy.belyaev.wishapp.domain.model.storedValue
import ru.vitaliy.belyaev.wishapp.domain.model.toInt
import ru.vitaliy.belyaev.wishapp.domain.model.toTheme
import ru.vitaliy.belyaev.wishapp.domain.model.toWishSortMode
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.WishSortMode

@Singleton
class DataStoreRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    val selectedThemeFlow: Flow<Theme> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map {
            it[KEY_THEME].toTheme()
        }

    suspend fun updateSelectedTheme(theme: Theme) {
        dataStore.edit {
            it[KEY_THEME] = theme.toInt()
        }
    }

    val selectedSortModeFlow: Flow<WishSortMode> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map {
            it[KEY_SORT_MODE].toWishSortMode()
        }

    suspend fun updateSelectedSortMode(sortMode: WishSortMode) {
        dataStore.edit {
            it[KEY_SORT_MODE] = sortMode.toInt()
        }
    }

    val positiveActionsCountFlow: Flow<Int> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map {
            it[KEY_POSITIVE_ACTIONS_COUNT] ?: 0
        }

    suspend fun incrementPositiveActionsCount() {
        dataStore.edit {
            val current = it[KEY_POSITIVE_ACTIONS_COUNT] ?: 0
            it[KEY_POSITIVE_ACTIONS_COUNT] = current + 1
        }
    }

    val reviewRequestShownCountFlow: Flow<Int> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map {
            it[KEY_REVIEW_REQUEST_SHOWN_COUNT] ?: 0
        }

    suspend fun updateReviewRequestShownCount(count: Int) {
        dataStore.edit {
            it[KEY_REVIEW_REQUEST_SHOWN_COUNT] = count
        }
    }

    val isRestoreFromBackupWasSucceed: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map {
            it[KEY_IS_RESTORE_FROM_BACKUP_WAS_SUCCEED] ?: false
        }

    suspend fun updateIsRestoreFromBackupWasSucceed(newValue: Boolean) {
        dataStore.edit {
            it[KEY_IS_RESTORE_FROM_BACKUP_WAS_SUCCEED] = newValue
        }
    }

    val isBackupWasCreated: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map {
            it[KEY_IS_BACKUP_WAS_CREATED] ?: false
        }

    suspend fun updateIsBackupWasCreated(newValue: Boolean) {
        dataStore.edit {
            it[KEY_IS_BACKUP_WAS_CREATED] = newValue
        }
    }

    /** Выбор вида пользователем, `null` если он ещё не выбирал. */
    val uiLookChoiceFlow: Flow<UiLook?> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map {
            UiLook.fromStoredValue(it[KEY_UI_LOOK])
        }

    suspend fun updateUiLookChoice(uiLook: UiLook) {
        dataStore.edit {
            it[KEY_UI_LOOK] = uiLook.storedValue
        }
    }

    /** Настройка «Как обои» нового вида: цвета интерфейса из обоев на Android 12+. */
    val useWallpaperColorsFlow: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map {
            it[KEY_WALLPAPER_COLORS] ?: false
        }

    suspend fun updateUseWallpaperColors(newValue: Boolean) {
        dataStore.edit {
            it[KEY_WALLPAPER_COLORS] = newValue
        }
    }

    /** Когда последний раз предлагали обновить приложение через Google Play, мс. */
    suspend fun getUpdatePromptShownAtMillis(): Long? = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .first()[PreferencesKeys.KEY_UPDATE_PROMPT_SHOWN_AT]

    suspend fun updateUpdatePromptShownAtMillis(millis: Long) {
        dataStore.edit {
            it[PreferencesKeys.KEY_UPDATE_PROMPT_SHOWN_AT] = millis
        }
    }

    private object PreferencesKeys {
        val KEY_THEME = intPreferencesKey("KEY_THEME")
        val KEY_SORT_MODE = intPreferencesKey("KEY_SORT_MODE")
        val KEY_POSITIVE_ACTIONS_COUNT = intPreferencesKey("KEY_POSITIVE_ACTIONS_COUNT")
        val KEY_REVIEW_REQUEST_SHOWN_COUNT = intPreferencesKey("KEY_REVIEW_REQUEST_SHOWN_COUNT")
        val KEY_IS_RESTORE_FROM_BACKUP_WAS_SUCCEED = booleanPreferencesKey("KEY_IS_RESTORE_FROM_BACKUP_WAS_SUCCEED")
        val KEY_IS_BACKUP_WAS_CREATED = booleanPreferencesKey("KEY_IS_BACKUP_WAS_CREATED")
        val KEY_IS_RESTORE_SCREEN_WAS_SHOWN = booleanPreferencesKey("KEY_IS_RESTORE_SCREEN_WAS_SHOWN")
        val KEY_UI_LOOK = stringPreferencesKey("KEY_UI_LOOK")
        val KEY_WALLPAPER_COLORS = booleanPreferencesKey("KEY_WALLPAPER_COLORS")
        val KEY_UPDATE_PROMPT_SHOWN_AT = longPreferencesKey("KEY_UPDATE_PROMPT_SHOWN_AT")
    }
}