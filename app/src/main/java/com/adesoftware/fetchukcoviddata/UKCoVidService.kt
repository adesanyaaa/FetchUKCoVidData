package com.adesoftware.fetchukcoviddata

import retrofit2.Call
import retrofit2.http.GET

interface UKCoVidService {
    @GET("data?filters=areaType=nation;areaName=england")
    fun getNationalData(): Call<List<UKCoVidData>>

    @GET("data?filters=areaType=nation;areaName=england")
    fun getRegionalData(): Call<List<UKCoVidData>>
}