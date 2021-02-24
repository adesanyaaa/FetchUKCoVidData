package com.adesoftware.fetchukcoviddata

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.google.gson.GsonBuilder
import com.robinhood.ticker.TickerUtils
import kotlinx.android.synthetic.main.activity_fetch_uk_covid_data.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*


class FetchUKCoVidDataActivity : AppCompatActivity() {

    companion object {
        const val BASE_URL = "https://api.coronavirus.data.gov.uk/v1/"
        //https://coronavirus.data.gov.uk/details/developers-guide#get-responses
        const val TAG = "FetchUKCoVidData"
        const val ALL_REGIONS = "England"
    }

    private lateinit var currentlyShownData: List<UKCoVidData>
    private lateinit var adapter: UKCoVidSparkAdapter
    private lateinit var perRegionalDailyData: Map<String, List<UKCoVidData>>
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
                setUpEventListeners()

                var nationalDailyData = nationalData.reversed()
                Log.i(TAG, "Update graph with national data")
                //Update graph with national data
                updateDisplayWithData(nationalDailyData)
            }

        })

        //Fetch the regional data
        coVidService.getRegionalData().enqueue(object : Callback<List<UKCoVidData>> {

            override fun onFailure(call: Call<List<UKCoVidData>>, t: Throwable) {
                Log.e(TAG, "onFailure $t")
            }

            override fun onResponse(
                    call: Call<List<UKCoVidData>>,
                    response: Response<List<UKCoVidData>>
            ) {
                Log.i(TAG, "onResponse $response")
                val regionalData = response.body()
                if (regionalData == null) {
                    Log.w(TAG, "Did not receive a valid response body")
                    return
                }

                perRegionalDailyData = regionalData
                        .filter { it.cases != null }
                        .map { //// State data may have negative deltas, which don't make sense to graph
                            UKCoVidData(
                                    //it.date,
                                    it.cases
                                    //it.deaths as Date
                            )
                        }
                        .reversed()
                        .groupBy { return  }
                Log.i(TAG, "Update spinner with state names")
                //Update spinner with state names
                updateSpinnerWithRegionalData(perRegionalDailyData.keys)
            }
        })
    }

    private fun updateSpinnerWithRegionalData(stateNames: Set<String>) {
        val stateAbberviationList = stateNames.toMutableList()
        stateAbberviationList.sort()
        stateAbberviationList.add(0, ALL_REGIONS)

        //Add state list as data source for the spinner
        spinnerSelect.attachDataSource(stateAbberviationList)
        spinnerSelect.setOnSpinnerItemSelectedListener { parent, view, position, id ->
            val selectedRegion = parent.getItemAtPosition(position) as String
            val selectedData = perRegionalDailyData[selectedRegion] ?: nationalDailyData
            updateDisplayWithData(selectedData)
        }
    }

    private fun setUpEventListeners() {
        //Add a listener for the user scrubbing on the chart
        sparkView.isScrubEnabled = true
        sparkView.setScrubListener { itemData ->
            if (itemData is UKCoVidData) {
                updateInfoForDate(itemData)
            }
        }
        tickerView.setCharacterLists(TickerUtils.provideNumberList())

        //Respond to radio button selected events
        radioGroupTimeSelection.setOnCheckedChangeListener { group, checkedId ->
            adapter.daysAgo = when (checkedId) {
                R.id.radioButtonWeek -> TimeScale.WEEK
                R.id.radioButtonMonth -> TimeScale.MONTH
                else -> TimeScale.MAX
            }
            //Display the last day of the metric
            updateInfoForDate(currentlyShownData.last())
            adapter.notifyDataSetChanged()
        }

        radioGroupMetricSelection.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.radioButtonPositive -> updateDisplayMetric(Metric.POSITIVE)
                R.id.radioButtonDeath -> updateDisplayMetric(Metric.DEATH)
            }
        }

    }

    private fun updateDisplayMetric(metric: Metric) {
        //Update the color of the chart
        val colorRes = when (metric) {
            Metric.POSITIVE -> R.color.colorPositive
            Metric.DEATH-> R.color.colorDeath
        }

        @ColorInt val colorInt = ContextCompat.getColor(this, colorRes)
        sparkView.lineColor = colorInt
        tickerView.setTextColor(colorInt)


        //Update the metric on the adapter
        adapter.metric = metric
        adapter.notifyDataSetChanged()

        //Reset number and date shown in the bottom text views
        updateInfoForDate(currentlyShownData.last())
    }

    private fun updateDisplayWithData(dailyData: List<UKCoVidData>) {
        currentlyShownData = dailyData
        //Create a new SparkAdapter with the data
        adapter = UKCoVidSparkAdapter(dailyData)
        sparkView.adapter = adapter
        //Update radio buttons to select the positive cases and max time by default
        radioButtonPositive.isChecked = true
        radioButtonMax.isChecked = true
        //Display metric for most recent date
        updateDisplayMetric(Metric.POSITIVE)
    }

    private fun updateInfoForDate(coVidData: UKCoVidData) {
        val numCases = when (adapter.metric) {
            Metric.POSITIVE -> coVidData.cases
            Metric.DEATH -> coVidData.deathIncrease
        }
        tickerView.text = NumberFormat.getInstance().format(numCases)
        val outputDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.UK)
        tvDateLabel.text = outputDateFormat.format(coVidData.cases)
    }

}