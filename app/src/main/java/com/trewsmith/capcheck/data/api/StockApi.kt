package com.trewsmith.capcheck.data.api

import com.trewsmith.capcheck.BuildConfig
import com.trewsmith.capcheck.data.model.StockResponse
import com.trewsmith.capcheck.data.model.TickerSearchResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface StockApi {
    @GET("v2/aggs/ticker/{symbol}/prev")
    suspend fun getQuote(
        @Path("symbol") symbol: String,
        @Query("apiKey") apiKey: String = BuildConfig.MASSIVE_API_KEY
    ): StockResponse

    @GET("v3/reference/tickers")
    suspend fun searchTickers(
        @Query("search") query: String,
        @Query("market") market: String = "stocks",
        @Query("active") active: Boolean = true,
        @Query("limit") limit: Int = 10,
        @Query("apiKey") apiKey: String = BuildConfig.MASSIVE_API_KEY
    ): TickerSearchResponse
}
