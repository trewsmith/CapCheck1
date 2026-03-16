package com.trewsmith.capcheck.data.model

data class PortfolioHolding(
    val symbol: String = "",
    val name: String = "",
    val shares: Double = 0.0,
    val avgCost: Double = 0.0,
    val currentPrice: Double = 0.0,
    val notes: String = "",
    val lastModified: Long = System.currentTimeMillis()
)
