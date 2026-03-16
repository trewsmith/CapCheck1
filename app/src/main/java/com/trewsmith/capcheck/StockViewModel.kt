package com.trewsmith.capcheck

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trewsmith.capcheck.data.StockRepository
import com.trewsmith.capcheck.data.model.StockResult
import com.trewsmith.capcheck.data.model.TickerResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StockViewModel(private val preferencesManager: PreferencesManager? = null) : ViewModel() {

    private val repository = StockRepository()

    private val _price = MutableStateFlow("Loading...")
    val price: StateFlow<String> = _price

    private val _searchResults = MutableStateFlow<List<TickerResult>>(emptyList())
    val searchResults: StateFlow<List<TickerResult>> = _searchResults

    private val _selectedStock = MutableStateFlow<StockResult?>(null)
    val selectedStock: StateFlow<StockResult?> = _selectedStock

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())
    val searchHistory: StateFlow<List<String>> = _searchHistory

    private val _marketFilter = MutableStateFlow("stocks")
    val marketFilter: StateFlow<String> = _marketFilter

    private val _bookmarks = MutableStateFlow<Set<String>>(emptySet())
    val bookmarks: StateFlow<Set<String>> = _bookmarks

    private val _priceAlerts = MutableStateFlow<Map<String, Double>>(emptyMap())
    val priceAlerts: StateFlow<Map<String, Double>> = _priceAlerts

    private var searchJob: Job? = null

    init {
        fetchStockPrice()
        viewModelScope.launch {
            preferencesManager?.searchHistoryFlow?.collect {
                _searchHistory.value = it
            }
        }
    }

    fun setMarketFilter(market: String) {
        _marketFilter.value = market
    }

    fun clearSearch() {
        _searchResults.value = emptyList()
    }

    fun clearHistory() {
        _searchHistory.value = emptyList()
        viewModelScope.launch {
            preferencesManager?.saveSearchHistory(emptyList())
        }
    }

    fun toggleBookmark(symbol: String) {
        val current = _bookmarks.value
        _bookmarks.value = if (current.contains(symbol)) current - symbol else current + symbol
    }

    fun setPriceAlert(symbol: String, price: Double) {
        val current = _priceAlerts.value.toMutableMap()
        current[symbol] = price
        _priceAlerts.value = current
    }

    fun removePriceAlert(symbol: String) {
        val current = _priceAlerts.value.toMutableMap()
        current.remove(symbol)
        _priceAlerts.value = current
    }

    fun fetchStockPrice(symbol: String = "AAPL") {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = repository.getStockPrice(symbol)
                _selectedStock.value = result
                _price.value = result?.close?.toString() ?: "No data"
                _isLoading.value = false
                
                if (result != null) {
                    val currentHistory = _searchHistory.value.toMutableList()
                    currentHistory.remove(symbol)
                    currentHistory.add(0, symbol)
                    if (currentHistory.size > 10) {
                        currentHistory.removeAt(currentHistory.size - 1)
                    }
                    _searchHistory.value = currentHistory
                    preferencesManager?.saveSearchHistory(currentHistory)
                }
            } catch (e: Exception) {
                _isLoading.value = false
                ErrorHandler.emitError(e.message ?: "Failed to fetch stock price")
            }
        }
    }

    fun searchTickers(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            _isLoading.value = true
            try {
                _searchResults.value = repository.searchTickers(query, _marketFilter.value)
                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
                ErrorHandler.emitError(e.message ?: "Search failed")
            }
        }
    }
}
