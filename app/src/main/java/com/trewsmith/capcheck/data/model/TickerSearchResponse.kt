package com.trewsmith.capcheck.data.model

import com.google.gson.annotations.SerializedName

data class TickerSearchResponse(
    @SerializedName("results")
    val results: List<TickerResult>?,
    @SerializedName("status")
    val status: String
)

data class TickerResult(
    @SerializedName("ticker")
    val ticker: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("market")
    val market: String,
    @SerializedName("primary_exchange")
    val exchange: String?
)