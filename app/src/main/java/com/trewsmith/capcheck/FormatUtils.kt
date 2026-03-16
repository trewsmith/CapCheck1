package com.trewsmith.capcheck

import java.util.Locale

object FormatUtils {
    fun formatPrice(value: Double): String {
        return "$" + String.format(Locale.US, "%.2f", value)
    }

    fun formatVolume(value: Double): String {
        return when {
            value >= 1_000_000_000 -> String.format(Locale.US, "%.1fB", value / 1_000_000_000)
            value >= 1_000_000 -> String.format(Locale.US, "%.1fM", value / 1_000_000)
            value >= 1_000 -> String.format(Locale.US, "%.1fK", value / 1_000)
            else -> String.format(Locale.US, "%.0f", value)
        }
    }

    fun formatPercent(value: Double): String {
        return String.format(Locale.US, "%.2f%%", value)
    }

    fun formatShares(value: Double): String {
        return if (value == value.toLong().toDouble()) {
            String.format(Locale.US, "%d", value.toLong())
        } else {
            String.format(Locale.US, "%.4f", value).trimEnd('0').trimEnd('.')
        }
    }
}
