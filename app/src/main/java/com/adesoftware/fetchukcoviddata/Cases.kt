package com.adesoftware.fetchukcoviddata


import com.google.gson.annotations.SerializedName

data class Cases(
    val cumulative: Int,
    val daily: Int
)