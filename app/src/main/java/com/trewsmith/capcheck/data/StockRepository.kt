package com.trewsmith.capcheck.data

import com.trewsmith.capcheck.data.api.RetrofitInstance
import com.trewsmith.capcheck.data.model.StockResult
import com.trewsmith.capcheck.data.model.TickerResult

class StockRepository {

    private val stockApi = RetrofitInstance.api

    suspend fun getStockPrice(symbol: String): StockResult? {
        return try {
            val response = stockApi.getQuote(symbol)
            response.results?.firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun searchTickers(query: String, market: String = "stocks"): List<TickerResult> {
        return try {
            val response = stockApi.searchTickers(query, market)
            response.results ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
