package com.adesoftware.fetchukcoviddata


import com.google.gson.annotations.SerializedName
import java.util.*

data class UKCoVidData(
    val cases: Cases,
    val code: String,
    val date: Date,
    val deaths: Deaths,
    val name: String
)