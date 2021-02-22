package com.adesoftware.fetchukcoviddata


import com.google.gson.annotations.SerializedName

data class Deaths(
    val cumulative: Int,
    val daily: Int
)