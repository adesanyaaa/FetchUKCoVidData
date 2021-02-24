package com.adesoftware.fetchukcoviddata


import com.google.gson.annotations.SerializedName
import java.util.*

data class UKCoVidData(
        @SerializedName("newCasesByPublishDate")
        val cases: Date
) {
    val deathIncrease: Any
        get() {
            return deaths
        }
}