package com.trewsmith.capcheck

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class PortfolioViewModelFactory(private val preferencesManager: PreferencesManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PortfolioViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PortfolioViewModel(preferencesManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
