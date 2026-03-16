package com.trewsmith.capcheck

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trewsmith.capcheck.data.WatchlistRepository
import com.trewsmith.capcheck.data.WatchlistItem
import com.trewsmith.capcheck.data.StockRepository
import com.trewsmith.capcheck.data.model.StockResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Collections

data class WatchlistEntry(
    val symbol: String, 
    val currentPrice: Double? = null,
    val addedAt: Long = System.currentTimeMillis()
)

enum class WatchlistSortOrder {
    A_Z, DATE_ADDED
}

class WatchlistViewModel : ViewModel() {

    private val repository = WatchlistRepository()
    private val stockRepository = StockRepository()

    private val _rawWatchlist = MutableStateFlow<List<WatchlistEntry>>(emptyList())
    
    private val _sortOrder = MutableStateFlow(WatchlistSortOrder.DATE_ADDED)
    val sortOrder: StateFlow<WatchlistSortOrder> = _sortOrder

    val watchlist: StateFlow<List<WatchlistEntry>> = combine(_rawWatchlist, _sortOrder) { list, order ->
        when (order) {
            WatchlistSortOrder.A_Z -> list.sortedBy { it.symbol }
            WatchlistSortOrder.DATE_ADDED -> list.sortedByDescending { it.addedAt }
        }
    }.let { flow ->
        val stateFlow = MutableStateFlow<List<WatchlistEntry>>(emptyList())
        viewModelScope.launch {
            flow.collect { stateFlow.value = it }
        }
        stateFlow
    }

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        loadWatchlist()
    }

    fun setSortOrder(order: WatchlistSortOrder) {
        _sortOrder.value = order
    }

    fun loadWatchlist() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val items = repository.getWatchlist()
                val entries = items.map { item ->
                    val price = stockRepository.getStockPrice(item.symbol)?.close
                    WatchlistEntry(item.symbol, price)
                }
                _rawWatchlist.value = entries
                _error.value = null
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }

    fun reorderWatchlist(from: Int, to: Int) {
        val currentList = _rawWatchlist.value.toMutableList()
        if (from in currentList.indices && to in currentList.indices) {
            Collections.swap(currentList, from, to)
            _rawWatchlist.value = currentList
        }
    }

    fun addToWatchlist(symbol: String, name: String) {
        viewModelScope.launch {
            try {
                repository.addToWatchlist(symbol, name)
                loadWatchlist()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun removeFromWatchlist(symbol: String) {
        viewModelScope.launch {
            try {
                repository.removeFromWatchlist(symbol)
                loadWatchlist()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}
