package ru.vitaliy.belyaev.wishapp.model.repository.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import ru.vitaliy.belyaev.wishapp.entity.Theme
import ru.vitaliy.belyaev.wishapp.entity.toInt
import ru.vitaliy.belyaev.wishapp.entity.toTheme
import ru.vitaliy.belyaev.wishapp.model.repository.datastore.DataStoreRepository.PreferencesKeys.KEY_POSITIVE_ACTIONS_COUNT
import ru.vitaliy.belyaev.wishapp.model.repository.datastore.DataStoreRepository.PreferencesKeys.KEY_REVIEW_REQUEST_SHOWN_COUNT
import ru.vitaliy.belyaev.wishapp.model.repository.datastore.DataStoreRepository.PreferencesKeys.KEY_THEME

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

    private object PreferencesKeys {
        val KEY_THEME = intPreferencesKey("KEY_THEME")
        val KEY_POSITIVE_ACTIONS_COUNT = intPreferencesKey("KEY_POSITIVE_ACTIONS_COUNT")
        val KEY_REVIEW_REQUEST_SHOWN_COUNT = intPreferencesKey("KEY_REVIEW_REQUEST_SHOWN_COUNT")
    }
}