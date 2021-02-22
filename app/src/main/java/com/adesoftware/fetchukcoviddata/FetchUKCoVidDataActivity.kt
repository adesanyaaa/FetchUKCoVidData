package com.adesoftware.fetchukcoviddata

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class FetchUKCoVidDataActivity : AppCompatActivity() {

    companion object {
        const val BASE_URL = "https://api.coronavirus.data.gov.uk/v1/"
        //https://coronavirus.data.gov.uk/details/developers-guide#get-responses
        const val TAG = "FetchUKCoVidData"
        const val ALL_REGIONS = "England"
    }

    private lateinit var currentlyShownData: List<UKCoVidData>
    private lateinit var adapter: UKCoVidSparkAdapter
    //private lateinit var perStateDailyData: Map<String, List<UKCoVidData>>
    private lateinit var nationalDailyData: List<UKCoVidData>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fetch_uk_covid_data)
        supportActionBar?.title = getString(R.string.description)

        val gson = GsonBuilder().setDateFormat("YYYY-MM-DD'T'HH:mm:ss.000Z").create()
        val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()

        val coVidService = retrofit.create(UKCoVidService::class.java)
        //Fetch the national data
        coVidService.getNationalData().enqueue(object : Callback<List<UKCoVidData>> {

            override fun onFailure(call: Call<List<UKCoVidData>>, t: Throwable) {
                Log.e(TAG, "onFailure $t")
            }

            override fun onResponse(call: Call<List<UKCoVidData>>, response: Response<List<UKCoVidData>>) {
                Log.i(TAG, "onResponse $response")
                val nationalData = response.body()
                if (nationalData == null) {
                    Log.w(TAG, "Did not receive a valid response body")
                    return
                }
                //setUpEventListeners()

                var nationalDailyData = nationalData.reversed()
                Log.i(TAG, "Update graph with national data")
                //Update graph with national data
                //updateDisplayWithData(nationalDailyData)
            }

        })

        //Fetch the regional data
    }


}