package com.adesoftware.fetchukcoviddata

enum class Metric {
    POSITIVE, DEATH
}

enum class TimeScale(val numDays: Int) {
    WEEK(7),
    MONTH(30),
    MAX(-1)
}