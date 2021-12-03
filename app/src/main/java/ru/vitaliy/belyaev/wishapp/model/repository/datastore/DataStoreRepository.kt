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
import ru.vitaliy.belyaev.wishapp.model.repository.datastore.DataStoreRepository.PreferencesKeys.KEY_THEME

@Singleton
class DataStoreRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    val selectedTheme: Flow<Theme> = dataStore.data
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

    private object PreferencesKeys {
        val KEY_THEME = intPreferencesKey("KEY_THEME")
    }
}