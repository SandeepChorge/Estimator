package com.madtitan.estimator

import android.text.format.DateUtils
import java.text.SimpleDateFormat
import java.util.*

fun formatRelativeDate(date: Date): String {
    return when {
        DateUtils.isToday(date.time) -> "Today"
        DateUtils.isToday(date.time + DateUtils.DAY_IN_MILLIS) -> "Yesterday"
        else -> SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date)
    }
}