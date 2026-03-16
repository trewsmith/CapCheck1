package com.trewsmith.capcheck

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesManager(context: Context) {

    private val dataStore = context.dataStore

    companion object {
        val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        val SEARCH_HISTORY_KEY = stringSetPreferencesKey("search_history")
        val PORTFOLIO_SORT_KEY = stringPreferencesKey("portfolio_sort")
        val CURRENCY_KEY = stringPreferencesKey("currency")
    }

    val isDarkModeFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[DARK_MODE_KEY] ?: false
    }

    suspend fun setDarkMode(isDarkMode: Boolean) {
        dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = isDarkMode
        }
    }

    val searchHistoryFlow: Flow<List<String>> = dataStore.data.map { preferences ->
        preferences[SEARCH_HISTORY_KEY]?.toList() ?: emptyList()
    }

    suspend fun saveSearchHistory(history: List<String>) {
        dataStore.edit { preferences ->
            preferences[SEARCH_HISTORY_KEY] = history.toSet()
        }
    }

    val portfolioSortFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[PORTFOLIO_SORT_KEY] ?: "BY_SYMBOL"
    }

    suspend fun savePortfolioSort(sortOrder: String) {
        dataStore.edit { preferences ->
            preferences[PORTFOLIO_SORT_KEY] = sortOrder
        }
    }

    val currencyFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[CURRENCY_KEY] ?: "USD"
    }

    suspend fun saveCurrency(currency: String) {
        dataStore.edit { preferences ->
            preferences[CURRENCY_KEY] = currency
        }
    }
}
