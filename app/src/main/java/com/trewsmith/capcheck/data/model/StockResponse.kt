package com.trewsmith.capcheck.data.model

import com.google.gson.annotations.SerializedName

data class StockResponse(
    @SerializedName("results")
    val results: List<StockResult>?,
    @SerializedName("status")
    val status: String
)

data class StockResult(
    @SerializedName("T")
    val symbol: String?,
    @SerializedName("c")
    val close: Double,
    @SerializedName("o")
    val open: Double,
    @SerializedName("h")
    val high: Double,
    @SerializedName("l")
    val low: Double,
    @SerializedName("v")
    val volume: Double
)