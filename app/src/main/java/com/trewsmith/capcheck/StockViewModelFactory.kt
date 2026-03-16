package com.trewsmith.capcheck

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class StockViewModelFactory(private val preferencesManager: PreferencesManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StockViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StockViewModel(preferencesManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
