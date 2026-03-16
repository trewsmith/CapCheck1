package com.trewsmith.capcheck

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TermsViewModel : ViewModel() {
    private val _favoritedTerms = MutableStateFlow<Set<String>>(emptySet())
    val favoritedTerms: StateFlow<Set<String>> = _favoritedTerms

    private val _viewedTerms = MutableStateFlow<Set<String>>(emptySet())
    val viewedTerms: StateFlow<Set<String>> = _viewedTerms

    fun toggleFavorite(term: String) {
        val current = _favoritedTerms.value
        if (current.contains(term)) {
            _favoritedTerms.value = current - term
        } else {
            _favoritedTerms.value = current + term
        }
    }

    fun markViewed(term: String) {
        _viewedTerms.value = _viewedTerms.value + term
    }
}
