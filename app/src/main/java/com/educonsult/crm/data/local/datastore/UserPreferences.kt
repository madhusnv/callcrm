package com.educonsult.crm.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_preferences"
)

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    val isLoggedIn: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_LOGGED_IN] ?: false
    }

    val userId: Flow<String?> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.USER_ID]
    }

    val organizationId: Flow<String?> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ORGANIZATION_ID]
    }

    val branchId: Flow<String?> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.BRANCH_ID]
    }

    suspend fun setLoggedIn(isLoggedIn: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_LOGGED_IN] = isLoggedIn
        }
    }

    suspend fun setUserId(userId: String?) {
        dataStore.edit { preferences ->
            if (userId != null) {
                preferences[PreferencesKeys.USER_ID] = userId
            } else {
                preferences.remove(PreferencesKeys.USER_ID)
            }
        }
    }

    suspend fun setOrganizationId(organizationId: String?) {
        dataStore.edit { preferences ->
            if (organizationId != null) {
                preferences[PreferencesKeys.ORGANIZATION_ID] = organizationId
            } else {
                preferences.remove(PreferencesKeys.ORGANIZATION_ID)
            }
        }
    }

    suspend fun setBranchId(branchId: String?) {
        dataStore.edit { preferences ->
            if (branchId != null) {
                preferences[PreferencesKeys.BRANCH_ID] = branchId
            } else {
                preferences.remove(PreferencesKeys.BRANCH_ID)
            }
        }
    }

    suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    private object PreferencesKeys {
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val USER_ID = stringPreferencesKey("user_id")
        val ORGANIZATION_ID = stringPreferencesKey("organization_id")
        val BRANCH_ID = stringPreferencesKey("branch_id")
    }
}
