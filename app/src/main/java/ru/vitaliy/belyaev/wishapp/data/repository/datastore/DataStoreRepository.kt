package ru.vitaliy.belyaev.wishapp.data.repository.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import ru.vitaliy.belyaev.wishapp.data.repository.datastore.DataStoreRepository.PreferencesKeys.KEY_IS_BACKUP_WAS_CREATED
import ru.vitaliy.belyaev.wishapp.data.repository.datastore.DataStoreRepository.PreferencesKeys.KEY_IS_RESTORE_FROM_BACKUP_WAS_SUCCEED
import ru.vitaliy.belyaev.wishapp.data.repository.datastore.DataStoreRepository.PreferencesKeys.KEY_POSITIVE_ACTIONS_COUNT
import ru.vitaliy.belyaev.wishapp.data.repository.datastore.DataStoreRepository.PreferencesKeys.KEY_REVIEW_REQUEST_SHOWN_COUNT
import ru.vitaliy.belyaev.wishapp.data.repository.datastore.DataStoreRepository.PreferencesKeys.KEY_THEME
import ru.vitaliy.belyaev.wishapp.domain.model.Theme
import ru.vitaliy.belyaev.wishapp.domain.model.toInt
import ru.vitaliy.belyaev.wishapp.domain.model.toTheme

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

    private object PreferencesKeys {
        val KEY_THEME = intPreferencesKey("KEY_THEME")
        val KEY_POSITIVE_ACTIONS_COUNT = intPreferencesKey("KEY_POSITIVE_ACTIONS_COUNT")
        val KEY_REVIEW_REQUEST_SHOWN_COUNT = intPreferencesKey("KEY_REVIEW_REQUEST_SHOWN_COUNT")
        val KEY_IS_RESTORE_FROM_BACKUP_WAS_SUCCEED = booleanPreferencesKey("KEY_IS_RESTORE_FROM_BACKUP_WAS_SUCCEED")
        val KEY_IS_BACKUP_WAS_CREATED = booleanPreferencesKey("KEY_IS_BACKUP_WAS_CREATED")
        val KEY_IS_RESTORE_SCREEN_WAS_SHOWN = booleanPreferencesKey("KEY_IS_RESTORE_SCREEN_WAS_SHOWN")
    }
}