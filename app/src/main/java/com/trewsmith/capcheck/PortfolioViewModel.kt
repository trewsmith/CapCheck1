package com.trewsmith.capcheck

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trewsmith.capcheck.data.PortfolioRepository
import com.trewsmith.capcheck.data.StockRepository
import com.trewsmith.capcheck.data.model.PortfolioHolding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

enum class SortOrder {
    BY_SYMBOL, BY_GAIN_LOSS, BY_VALUE
}

class PortfolioViewModel(private val preferencesManager: PreferencesManager? = null) : ViewModel() {

    private val repository = PortfolioRepository()
    private val stockRepository = StockRepository()

    private val _holdings = MutableStateFlow<List<PortfolioHolding>>(emptyList())
    
    private val _sortOrder = MutableStateFlow(SortOrder.BY_SYMBOL)
    val sortOrder: StateFlow<SortOrder> = _sortOrder

    val holdings: StateFlow<List<PortfolioHolding>> = combine(_holdings, _sortOrder) { holdings, order ->
        when (order) {
            SortOrder.BY_SYMBOL -> holdings.sortedBy { it.symbol }
            SortOrder.BY_GAIN_LOSS -> holdings.sortedByDescending { (it.currentPrice - it.avgCost) * it.shares }
            SortOrder.BY_VALUE -> holdings.sortedByDescending { it.currentPrice * it.shares }
        }
    }.let { flow ->
        val stateFlow = MutableStateFlow<List<PortfolioHolding>>(emptyList())
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
        loadHoldings()
        viewModelScope.launch {
            preferencesManager?.portfolioSortFlow?.collect { savedSort ->
                _sortOrder.value = try { SortOrder.valueOf(savedSort) } catch (e: Exception) { SortOrder.BY_SYMBOL }
            }
        }
    }

    fun loadHoldings() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val baseHoldings = repository.getHoldings()
                val updatedHoldings = baseHoldings.map { holding ->
                    val price = stockRepository.getStockPrice(holding.symbol)?.close ?: 0.0
                    holding.copy(currentPrice = price)
                }
                _holdings.value = updatedHoldings
                _error.value = null
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
        viewModelScope.launch {
            preferencesManager?.savePortfolioSort(order.name)
        }
    }

    fun addHolding(symbol: String, name: String, shares: Double, avgCost: Double) {
        viewModelScope.launch {
            try {
                val holding = PortfolioHolding(symbol, name, shares, avgCost)
                repository.addHolding(holding)
                loadHoldings()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun editHolding(holding: PortfolioHolding) {
        viewModelScope.launch {
            try {
                repository.addHolding(holding)
                loadHoldings()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun removeHolding(symbol: String) {
        viewModelScope.launch {
            try {
                repository.removeHolding(symbol)
                loadHoldings()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}
