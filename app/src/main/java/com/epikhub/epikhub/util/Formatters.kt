package com.epikhub.epikhub.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CurrencyFormatter {
    private val numberFormat = NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }

    fun formatMWK(amount: Double): String {
        return "MWK ${numberFormat.format(amount)}"
    }
}

object DateFormatter {
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.US)

    fun formatTimestamp(timestamp: Long): String {
        return if (timestamp > 0) dateFormat.format(Date(timestamp)) else "N/A"
    }
}
