package com.empresa.aplicacionedgeimpulse17

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.json.JSONArray

data class PredictionEvent(
    val timeSeconds: Int,
    val className: String
)

data class SensorEventData(
    val timeOffsetMillis: Long,
    val x: Float,
    val y: Float,
    val z: Float
)

data class MonitoringSessionLog(
    val sessionStartMillis: Long,
    val sessionEndMillis: Long? = null,
    val windowsProcessed: Int = 0,
    val fallCount: Int = 0,
    val alertsTriggered: Int = 0,
    val emergencyNumber: String = "",
    val currentPrediction: String = "Inactivo",
    val predictionHistory: MutableList<PredictionEvent> = mutableListOf(),
    @Transient val sensorHistory: MutableList<SensorEventData> = mutableListOf()
) {
    val durationSeconds: Long
        get() = if (sessionEndMillis != null) {
            (sessionEndMillis - sessionStartMillis) / 1000
        } else {
            (System.currentTimeMillis() - sessionStartMillis) / 1000
        }

    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("sessionStartMillis", sessionStartMillis)
            put("sessionStartIso", isoFormat(sessionStartMillis))
            put("sessionEndMillis", sessionEndMillis ?: JSONObject.NULL)
            put("sessionEndIso", sessionEndMillis?.let { isoFormat(it) } ?: JSONObject.NULL)
            put("durationSeconds", durationSeconds)
            put("windowsProcessed", windowsProcessed)
            put("fallCount", fallCount)
            put("alertsTriggered", alertsTriggered)
            put("emergencyNumber", emergencyNumber)
            put("currentPrediction", currentPrediction)
            
            val historyArray = JSONArray()
            predictionHistory.forEach { event ->
                val eventObj = JSONObject()
                eventObj.put("timeSeconds", event.timeSeconds)
                eventObj.put("className", event.className)
                historyArray.put(eventObj)
            }
            put("predictionHistory", historyArray)
        }
    }

    companion object {
        private fun isoFormat(timestamp: Long): String {
            val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            return formatter.format(Date(timestamp))
        }
    }
}

object MonitoringLogManager {
    private const val LOG_FILE_NAME = "monitoring_log.json"
    private const val EXPORT_FILE_NAME = "datos-monitoreo-edge-impulse-17-clases.json"
    private var currentSession: MonitoringSessionLog? = null

    fun startSession(context: Context, emergencyNumber: String) {
        currentSession = MonitoringSessionLog(
            sessionStartMillis = System.currentTimeMillis(),
            emergencyNumber = emergencyNumber
        )
        saveCurrentSession(context)
    }

    fun recordWindow(context: Context) {
        currentSession?.let {
            currentSession = it.copy(windowsProcessed = it.windowsProcessed + 1)
            saveCurrentSession(context)
        }
    }

    fun recordFall(context: Context) {
        currentSession?.let {
            currentSession = it.copy(fallCount = it.fallCount + 1)
            saveCurrentSession(context)
        }
    }

    fun recordSensorData(x: Float, y: Float, z: Float) {
        currentSession?.let {
            val offset = System.currentTimeMillis() - it.sessionStartMillis
            it.sensorHistory.add(SensorEventData(offset, x, y, z))
            // Limit to last 500 points (approx 10 seconds at 50Hz) to save memory
            if (it.sensorHistory.size > 500) {
                it.sensorHistory.removeAt(0)
            }
        }
    }

    fun updatePrediction(context: Context, prediction: String, className: String) {
        currentSession?.let {
            it.predictionHistory.add(PredictionEvent(it.durationSeconds.toInt(), className))
            currentSession = it.copy(currentPrediction = prediction)
            saveCurrentSession(context)
        }
    }

    fun recordAlert(context: Context) {
        currentSession?.let {
            currentSession = it.copy(alertsTriggered = it.alertsTriggered + 1)
            saveCurrentSession(context)
        }
    }

    fun stopSession(context: Context) {
        currentSession?.let {
            currentSession = it.copy(sessionEndMillis = System.currentTimeMillis())
            saveCurrentSession(context)
        }
    }

    fun getCurrentSession(): MonitoringSessionLog? = currentSession

    fun loadLastSession(context: Context): MonitoringSessionLog? {
        val file = File(context.filesDir, LOG_FILE_NAME)
        if (!file.exists()) return null
        return try {
            val json = JSONObject(file.readText())
            MonitoringSessionLog(
                sessionStartMillis = json.optLong("sessionStartMillis"),
                sessionEndMillis = if (json.isNull("sessionEndMillis")) null else json.optLong("sessionEndMillis"),
                windowsProcessed = json.optInt("windowsProcessed"),
                fallCount = json.optInt("fallCount"),
                alertsTriggered = json.optInt("alertsTriggered"),
                emergencyNumber = json.optString("emergencyNumber"),
                currentPrediction = json.optString("currentPrediction", "Inactivo"),
                predictionHistory = mutableListOf<PredictionEvent>().apply {
                    val arr = json.optJSONArray("predictionHistory")
                    if (arr != null) {
                        for (i in 0 until arr.length()) {
                            val obj = arr.optJSONObject(i)
                            if (obj != null) {
                                add(PredictionEvent(obj.optInt("timeSeconds"), obj.optString("className")))
                            }
                        }
                    }
                }
            )
        } catch (_: Exception) {
            null
        }
    }

    fun exportReportToDownloads(context: Context): String? {
        val session = currentSession ?: loadLastSession(context) ?: return null
        val jsonData = session.toJson().toString(2).toByteArray()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, EXPORT_FILE_NAME)
                put(MediaStore.Downloads.MIME_TYPE, "application/json")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                put(MediaStore.Downloads.IS_PENDING, 1)
            }

            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: return null
            resolver.openOutputStream(uri)?.use { it.write(jsonData) } ?: return null
            values.clear()
            values.put(MediaStore.Downloads.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
            "Download/$EXPORT_FILE_NAME"
        } else {
            val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadDir.exists()) {
                downloadDir.mkdirs()
            }
            val file = File(downloadDir, EXPORT_FILE_NAME)
            FileOutputStream(file).use { it.write(jsonData) }
            file.absolutePath
        }
    }

    private fun saveCurrentSession(context: Context) {
        currentSession?.let {
            val file = File(context.filesDir, LOG_FILE_NAME)
            file.writeText(it.toJson().toString(2))
        }
    }
}
