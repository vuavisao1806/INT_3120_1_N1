package com.example.locationpins.utils

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