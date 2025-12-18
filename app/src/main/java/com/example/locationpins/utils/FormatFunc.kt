package com.example.locationpins.utils

import android.util.Log
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Format số thành dạng rút gọn (k, m)
 * Ví dụ: 1234 -> "1.2k", 1234567 -> "1.2m"
 */
internal fun formatCount(count: Int): String {
    return when {
        count >= 1_000_000 -> {
            val millions = count / 1_000_000.0
            if (millions >= 10) {
                "${(millions).toInt()}m"
            } else {
                String.format("%.1fm", millions)
            }
        }
        count >= 1_000 -> {
            val thousands = count / 1_000.0
            if (thousands >= 10) {
                "${(thousands).toInt()}k"
            } else {
                String.format("%.1fk", thousands)
            }
        }
        else -> count.toString()
    }
}

internal fun formatTimeForPost(createdAtTime: String): String {
    val currentTime = ZonedDateTime.now()
    Log.i("Time", currentTime.toString())
    val createdAtLocalTime = runCatching {
        LocalDateTime.parse(createdAtTime.trim(), DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }.getOrNull() ?: return "vừa xong"

    Log.i("Time after", createdAtLocalTime.toString())

    val createAtLocalTimeZone = createdAtLocalTime
        .atOffset(ZoneOffset.UTC)
        .atZoneSameInstant(currentTime.zone)
    Log.i("Time final", createAtLocalTimeZone.toString())
    val diff = Duration.between(createAtLocalTimeZone, currentTime)

    if (diff.isNegative) {
        return "vừa xong"
    }

    val seconds = diff.seconds
    if (seconds < 60) {
        return "vừa xong"
    }

    val minutes = seconds / 60
    if (minutes < 60) {
        return "${minutes} phút trước"
    }

    val hours = minutes / 60
    if (hours < 24) {
        return "${hours} giờ trước"
    }

    val yearDiff = currentTime.year - createAtLocalTimeZone.year
    if (yearDiff >= 1) {
        return "${yearDiff} năm trước"
    }

    val monthDiff = currentTime.monthValue - createAtLocalTimeZone.monthValue
    if (monthDiff >= 1) {
        return "${monthDiff} tháng trước"
    }

    val daysDiff = ChronoUnit.DAYS.between(createAtLocalTimeZone.toLocalDate(), currentTime.toLocalDate())
    return "${daysDiff} ngày trước"
}

internal fun formatTimeForAchievement(createdAtTime: String): String {
    val currentTime = ZonedDateTime.now()
    val createdAtLocalTime = runCatching {
        LocalDateTime.parse(createdAtTime.trim(), DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }.getOrNull() ?: return "Unknown"

    val createAtLocalTimeZone = createdAtLocalTime
        .atOffset(ZoneOffset.UTC)
        .atZoneSameInstant(currentTime.zone)

    return createAtLocalTimeZone.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
}
