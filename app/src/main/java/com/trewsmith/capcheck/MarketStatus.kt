package com.trewsmith.capcheck

import java.util.*

object MarketStatus {
    fun isMarketOpen(): Boolean {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("America/New_York"))
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
            return false
        }

        val currentTimeInMinutes = hour * 60 + minute
        val marketOpenInMinutes = 9 * 60 + 30
        val marketCloseInMinutes = 16 * 60

        return currentTimeInMinutes in marketOpenInMinutes..marketCloseInMinutes
    }
}
