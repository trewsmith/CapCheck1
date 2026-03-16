package com.trewsmith.capcheck

import android.graphics.Color
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate

@Composable
fun PieChartView(data: Map<String, Double>) {
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        factory = { context ->
            PieChart(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                description.isEnabled = false
                isDrawHoleEnabled = true
                setHoleColor(Color.TRANSPARENT)
                legend.isEnabled = true
                legend.textColor = Color.GRAY
                setEntryLabelColor(Color.WHITE)
                setEntryLabelTextSize(12f)
            }
        },
        update = { chart ->
            val entries = data.map { PieEntry(it.value.toFloat(), it.key) }
            val dataSet = PieDataSet(entries, "Allocation").apply {
                colors = ColorTemplate.MATERIAL_COLORS.toList()
                valueTextColor = Color.WHITE
                valueTextSize = 12f
            }
            chart.data = PieData(dataSet)
            chart.invalidate()
        }
    )
}
