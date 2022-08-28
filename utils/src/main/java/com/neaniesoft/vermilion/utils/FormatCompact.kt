package com.neaniesoft.vermilion.utils

import kotlin.time.Duration
import kotlin.time.DurationUnit

fun Duration.formatCompact(): String {
    return when {
        inWholeDays > 0 -> {
            this.toString(DurationUnit.DAYS)
        }
        inWholeHours > 0 -> {
            this.toString(DurationUnit.HOURS)
        }
        inWholeMinutes > 0 -> {
            this.toString(DurationUnit.MINUTES)
        }
        else -> {
            this.toString(DurationUnit.SECONDS)
        }
    }
}
