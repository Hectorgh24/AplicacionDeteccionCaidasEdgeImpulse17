package com.empresa.aplicacionedgeimpulse17

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.util.TypedValue
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.ScatterChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.ScatterData
import com.github.mikephil.charting.data.ScatterDataSet
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import android.graphics.Color

class SettingsActivity : AppCompatActivity() {

    private lateinit var tvSessionStart: TextView
    private lateinit var tvDuration: TextView
    private lateinit var tvWindows: TextView
    private lateinit var tvFalls: TextView
    private lateinit var tvAlerts: TextView
    private lateinit var tvEmergencyNumber: TextView
    private lateinit var tvPrediction: TextView
    private lateinit var tvRemainingTime: TextView
    private lateinit var btnExportReport: Button
    private lateinit var timelineChart: ScatterChart
    private lateinit var sensorChart: LineChart

    private val classList = listOf(
        "fall_backward", "fall_bending", "fall_forward", 
        "fall_hand", "fall_sideward_left", "fall_sideward_right", 
        "fall_sitting", "fall_syncope", "going_down_stairs", 
        "going_up_stairs", "jump", "lying_down_fs", 
        "run", "sitting_down", "standing_up_fl", 
        "standing_up_fs", "walk"
    )

    private val classTranslationsChart = mapOf(
        "fall_backward" to "Caída hacia atrás",
        "fall_bending" to "Caída doblándose",
        "fall_forward" to "Caída hacia adelante",
        "fall_hand" to "Caída sobre manos",
        "fall_sideward_left" to "Caída lateral izq.",
        "fall_sideward_right" to "Caída lateral der.",
        "fall_sitting" to "Caída sentado",
        "fall_syncope" to "Caída desmayo",
        "going_down_stairs" to "Bajando escaleras",
        "going_up_stairs" to "Subiendo escaleras",
        "jump" to "Saltando",
        "lying_down_fs" to "Acostándose (Desde Silla)",
        "run" to "Corriendo",
        "sitting_down" to "Sentándose",
        "standing_up_fl" to "Levantándose (Desde Suelo)",
        "standing_up_fs" to "Levantándose (Desde Silla)",
        "walk" to "Caminando"
    )

    private val refreshHandler = Handler(Looper.getMainLooper())
    private val refreshTask = object : Runnable {
        override fun run() {
            renderSession()
            refreshHandler.postDelayed(this, 2000L) // Refrescar cada 2 segundos para no saturar el main thread
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        tvSessionStart = findViewById(R.id.tvSessionStart)
        tvDuration = findViewById(R.id.tvDuration)
        tvWindows = findViewById(R.id.tvWindows)
        tvFalls = findViewById(R.id.tvFalls)
        tvAlerts = findViewById(R.id.tvAlerts)
        tvEmergencyNumber = findViewById(R.id.tvEmergencyNumber)
        tvPrediction = findViewById(R.id.tvPredictionLog)
        tvRemainingTime = findViewById(R.id.tvRemainingTime)
        btnExportReport = findViewById(R.id.btnExportReport)
        timelineChart = findViewById(R.id.timelineChart)
        sensorChart = findViewById(R.id.sensorChart)

        setupChart()
        setupSensorChart()

        btnExportReport.setOnClickListener {
            val path = MonitoringLogManager.exportReportToDownloads(this)
            if (path != null) {
                Toast.makeText(this, "Reporte guardado en: $path", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "No hay datos para exportar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        renderSession()
        refreshHandler.post(refreshTask)
    }

    override fun onStop() {
        refreshHandler.removeCallbacks(refreshTask)
        super.onStop()
    }

    private fun setupSensorChart() {
        val typedValue = TypedValue()
        theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, true)
        val textColorPrimary = ContextCompat.getColor(this, typedValue.resourceId)

        sensorChart.description.isEnabled = false
        sensorChart.legend.textColor = textColorPrimary
        sensorChart.axisRight.isEnabled = false
        
        sensorChart.isDragEnabled = true
        sensorChart.setScaleEnabled(true)
        sensorChart.setPinchZoom(true)

        // Desactivar hardware acceleration en el chart para reducir presión de GPU/memoria
        sensorChart.setHardwareAccelerationEnabled(false)
        
        val xAxis = sensorChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(true)
        xAxis.granularity = 1f
        xAxis.textColor = textColorPrimary
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return "${value.toInt()}s"
            }
        }
        
        val yAxis = sensorChart.axisLeft
        yAxis.setDrawGridLines(true)
        yAxis.textColor = textColorPrimary
        yAxis.axisMinimum = -25f
        yAxis.axisMaximum = 25f
    }

    private fun setupChart() {
        val typedValue = TypedValue()
        theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, true)
        val textColorPrimary = ContextCompat.getColor(this, typedValue.resourceId)

        timelineChart.description.isEnabled = false
        timelineChart.legend.isEnabled = false
        timelineChart.axisRight.isEnabled = false
        timelineChart.extraLeftOffset = 65f
        timelineChart.extraBottomOffset = 10f
        
        // Enable scrolling and zooming
        timelineChart.isDragEnabled = true
        timelineChart.setScaleEnabled(true)
        timelineChart.setPinchZoom(true)

        // Desactivar hardware acceleration para reducir presión de memoria
        timelineChart.setHardwareAccelerationEnabled(false)
        
        val xAxis = timelineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(true)
        xAxis.granularity = 1f
        xAxis.textColor = textColorPrimary
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return "${value.toInt()}s"
            }
        }
        
        val yAxis = timelineChart.axisLeft
        yAxis.granularity = 1f
        yAxis.setDrawGridLines(true)
        yAxis.axisMinimum = -0.5f
        yAxis.axisMaximum = classList.size - 0.5f
        yAxis.labelCount = classList.size
        yAxis.textSize = 10f
        yAxis.textColor = textColorPrimary
        
        yAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val index = value.toInt()
                if (index in classList.indices) {
                    val key = classList[index]
                    return classTranslationsChart[key] ?: key
                }
                return ""
            }
        }
    }

    private fun renderSession() {
        val session = MonitoringLogManager.getCurrentSession() ?: MonitoringLogManager.loadLastSession(this)
        if (session == null) {
            tvSessionStart.text = "Fecha de inicio: -"
            tvDuration.text = "Duración (segundos): 0"
            tvWindows.text = "Inferencias realizadas: 0"
            tvFalls.text = "Cantidad de caídas detectadas: 0"
            tvAlerts.text = "Alertas enviadas: 0"
            tvEmergencyNumber.text = "Número de emergencia: -"
            tvPrediction.text = "Última predicción: Inactivo"
            tvRemainingTime.text = "Tiempo restante: -"
            timelineChart.clear()
            sensorChart.clear()
            return
        }

        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val startDate = formatter.format(Date(session.sessionStartMillis))
        tvSessionStart.text = "Fecha de inicio: $startDate"
        tvDuration.text = "Duración (segundos): ${session.durationSeconds}"
        tvWindows.text = "Inferencias realizadas: ${session.windowsProcessed}"
        tvFalls.text = "Cantidad de caídas detectadas: ${session.fallCount}"
        tvAlerts.text = "Alertas enviadas: ${session.alertsTriggered}"
        tvEmergencyNumber.text = "Número de emergencia: ${session.emergencyNumber.ifBlank { "-" }}"
        tvPrediction.text = "Última predicción: ${session.currentPrediction}"

        // Mostrar tiempo restante de la sesión activa
        val remaining = MonitoringLogManager.remainingSeconds
        if (session.sessionEndMillis == null) {
            val min = remaining / 60
            val sec = remaining % 60
            tvRemainingTime.text = "Tiempo restante: ${String.format("%d:%02d", min, sec)}"
        } else {
            tvRemainingTime.text = "Sesión finalizada"
        }

        updateChart(session)
        updateSensorChart()
    }

    /**
     * Actualiza el gráfico del acelerómetro usando el snapshot del display buffer
     * (ya throttleado a ~2Hz por MonitoringLogManager).
     * OPTIMIZACIÓN: Diezmado adaptativo para limitar a ~100 puntos por eje,
     * reduciendo drásticamente el costo de renderizado del chart.
     */
    private fun updateSensorChart() {
        // Usar el snapshot throttleado si hay sesión activa, o sensorHistory de sesión guardada
        val sensorData = MonitoringLogManager.displaySnapshot.ifEmpty {
            MonitoringLogManager.getCurrentSession()?.sensorHistory
                ?: MonitoringLogManager.loadLastSession(this)?.sensorHistory
                ?: emptyList()
        }

        if (sensorData.isEmpty()) {
            sensorChart.clear()
            return
        }

        // Diezmado adaptativo: si hay más de MAX_CHART_POINTS, tomar 1 de cada N
        val maxChartPoints = 100
        val step = maxOf(1, sensorData.size / maxChartPoints)

        val entriesX = ArrayList<Entry>(maxChartPoints + 1)
        val entriesY = ArrayList<Entry>(maxChartPoints + 1)
        val entriesZ = ArrayList<Entry>(maxChartPoints + 1)

        var i = 0
        while (i < sensorData.size) {
            val ev = sensorData[i]
            val t = ev.timeOffsetMillis / 1000f
            entriesX.add(Entry(t, ev.x))
            entriesY.add(Entry(t, ev.y))
            entriesZ.add(Entry(t, ev.z))
            i += step
        }
        // Siempre incluir el último punto para que el gráfico llegue al tiempo actual
        val last = sensorData.last()
        val lastT = last.timeOffsetMillis / 1000f
        if (entriesX.isEmpty() || entriesX.last().x != lastT) {
            entriesX.add(Entry(lastT, last.x))
            entriesY.add(Entry(lastT, last.y))
            entriesZ.add(Entry(lastT, last.z))
        }

        if (sensorChart.data != null && sensorChart.data.dataSetCount == 3) {
            val dataSetX = sensorChart.data.getDataSetByIndex(0) as LineDataSet
            val dataSetY = sensorChart.data.getDataSetByIndex(1) as LineDataSet
            val dataSetZ = sensorChart.data.getDataSetByIndex(2) as LineDataSet
            
            dataSetX.values = entriesX
            dataSetY.values = entriesY
            dataSetZ.values = entriesZ
            
            sensorChart.data.notifyDataChanged()
            sensorChart.notifyDataSetChanged()
        } else {
            val dataSetX = LineDataSet(entriesX, "Eje X").apply {
                color = Color.RED
                setDrawCircles(false)
                setDrawValues(false)
                lineWidth = 1.5f
            }
            val dataSetY = LineDataSet(entriesY, "Eje Y").apply {
                color = Color.GREEN
                setDrawCircles(false)
                setDrawValues(false)
                lineWidth = 1.5f
            }
            val dataSetZ = LineDataSet(entriesZ, "Eje Z").apply {
                color = Color.BLUE
                setDrawCircles(false)
                setDrawValues(false)
                lineWidth = 1.5f
            }
            
            val lineData = LineData(listOf<ILineDataSet>(dataSetX, dataSetY, dataSetZ))
            sensorChart.data = lineData
        }
        
        val maxX = entriesX.last().x
        
        sensorChart.setVisibleXRangeMaximum(10f)
        if (maxX > 10f) {
            sensorChart.moveViewToX(maxX - 10f)
        } else {
            sensorChart.invalidate()
        }
    }

    private fun updateChart(session: MonitoringSessionLog) {
        val entries = ArrayList<Entry>()
        for (event in session.predictionHistory) {
            val yIndex = classList.indexOf(event.className).toFloat()
            if (yIndex >= 0f) {
                entries.add(Entry(event.timeSeconds.toFloat(), yIndex))
            }
        }
        
        if (entries.isEmpty()) {
            timelineChart.clear()
            return
        }

        if (timelineChart.data != null && timelineChart.data.dataSetCount > 0) {
            val dataSet = timelineChart.data.getDataSetByIndex(0) as ScatterDataSet
            dataSet.values = entries
            timelineChart.data.notifyDataChanged()
            timelineChart.notifyDataSetChanged()
        } else {
            val dataSet = ScatterDataSet(entries, "Predicciones")
            val typedValue = TypedValue()
            theme.resolveAttribute(androidx.appcompat.R.attr.colorAccent, typedValue, true)
            val colorAccent = ContextCompat.getColor(this, typedValue.resourceId)
            dataSet.color = colorAccent
            dataSet.setScatterShape(ScatterChart.ScatterShape.CIRCLE)
            dataSet.scatterShapeSize = 12f
            dataSet.setDrawValues(false)
            
            val scatterData = ScatterData(dataSet)
            timelineChart.data = scatterData
        }
        
        val maxX = maxOf(30f, session.durationSeconds.toFloat() + 2f)
        timelineChart.xAxis.axisMinimum = 0f
        timelineChart.xAxis.axisMaximum = maxX
        
        // Limit visible X range to create a scrollable slider effect
        timelineChart.setVisibleXRangeMaximum(25f)
        if (session.durationSeconds > 25) {
            // moveViewToX sets the LEFT edge of the viewport
            timelineChart.moveViewToX(session.durationSeconds.toFloat() - 25f)
        } else {
            timelineChart.invalidate()
        }
    }
}
