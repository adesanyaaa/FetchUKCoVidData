package com.adesoftware.fetchukcoviddata

import android.graphics.RectF
import com.robinhood.spark.SparkAdapter

class UKCoVidSparkAdapter(private val dailyData: List<UKCoVidData>): SparkAdapter() {

    var metric = Metric.CASE
    var daysAgo = TimeScale.MAX

    override fun getItem(index: Int) = dailyData[index]

    override fun getCount() = dailyData.size

    override fun getY(index: Int): Float {
        val chosenDayData = dailyData[index]
        return when(metric) {
            //Metric.CASE -> chosenDayData.deathIncrease
            //Metric.DEATH -> chosenDayData.deathIncrease.toFloat()

        }
    }

    override fun getDataBounds(): RectF {
        val bounds = super.getDataBounds()
        if(daysAgo != TimeScale.MAX) {
            bounds.left = count - daysAgo.numDays.toFloat()
        }
        return bounds
    }
}