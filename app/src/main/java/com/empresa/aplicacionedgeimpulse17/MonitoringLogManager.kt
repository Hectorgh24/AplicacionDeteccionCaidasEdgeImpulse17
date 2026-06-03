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
import java.util.concurrent.CopyOnWriteArrayList

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
    val predictionHistory: MutableList<PredictionEvent> = CopyOnWriteArrayList(),
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
            // Iterar sobre una copia defensiva para evitar ConcurrentModificationException
            ArrayList(predictionHistory).forEach { event ->
                val eventObj = JSONObject()
                eventObj.put("timeSeconds", event.timeSeconds)
                eventObj.put("className", event.className)
                historyArray.put(eventObj)
            }
            put("predictionHistory", historyArray)

            // Incluir datos completos del acelerómetro para reconstrucción de gráfico por Python
            val sensorArray = JSONArray()
            sensorHistory.forEach { data ->
                val sensorObj = JSONObject()
                sensorObj.put("timeOffsetMillis", data.timeOffsetMillis)
                sensorObj.put("x", data.x.toDouble())
                sensorObj.put("y", data.y.toDouble())
                sensorObj.put("z", data.z.toDouble())
                sensorArray.put(sensorObj)
            }
            put("sensorHistory", sensorArray)
        }
    }

    companion object {
        private fun isoFormat(timestamp: Long): String {
            val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            return formatter.format(Date(timestamp))
        }

        fun fromJson(json: JSONObject): MonitoringSessionLog {
            val sensorList = mutableListOf<SensorEventData>()
            val sensorArr = json.optJSONArray("sensorHistory")
            if (sensorArr != null) {
                for (i in 0 until sensorArr.length()) {
                    val obj = sensorArr.optJSONObject(i)
                    if (obj != null) {
                        sensorList.add(
                            SensorEventData(
                                obj.optLong("timeOffsetMillis"),
                                obj.optDouble("x", 0.0).toFloat(),
                                obj.optDouble("y", 0.0).toFloat(),
                                obj.optDouble("z", 0.0).toFloat()
                            )
                        )
                    }
                }
            }

            return MonitoringSessionLog(
                sessionStartMillis = json.optLong("sessionStartMillis"),
                sessionEndMillis = if (json.isNull("sessionEndMillis")) null else json.optLong("sessionEndMillis"),
                windowsProcessed = json.optInt("windowsProcessed"),
                fallCount = json.optInt("fallCount"),
                alertsTriggered = json.optInt("alertsTriggered"),
                emergencyNumber = json.optString("emergencyNumber"),
                currentPrediction = json.optString("currentPrediction", "Inactivo"),
                predictionHistory = CopyOnWriteArrayList<PredictionEvent>().apply {
                    val arr = json.optJSONArray("predictionHistory")
                    if (arr != null) {
                        for (i in 0 until arr.length()) {
                            val obj = arr.optJSONObject(i)
                            if (obj != null) {
                                add(PredictionEvent(obj.optInt("timeSeconds"), obj.optString("className")))
                            }
                        }
                    }
                },
                sensorHistory = sensorList
            )
        }
    }
}

object MonitoringLogManager {
    private const val LOG_FILE_NAME = "monitoring_log.json"
    private const val EXPORT_FILE_NAME = "datos-monitoreo-edgeimpulse17-clases.json"

    @Volatile
    private var currentSession: MonitoringSessionLog? = null

    /**
     * Lista completa de datos del sensor para guardar en el JSON final.
     * Usa ArrayList simple — solo se escribe desde el hilo del sensor (main thread)
     * y se lee al finalizar la sesión (también main thread). CopyOnWriteArrayList
     * causaba lag progresivo severo porque cada add() copia todo el array interno.
     */
    private val fullSensorHistory = ArrayList<SensorEventData>()

    /**
     * Buffer de visualización para el gráfico en tiempo real.
     * Se escribe un dato por muestra del sensor y se recorta en lote cuando excede
     * el límite. Usar removeAt(0) en cada muestra era O(n) por desplazamiento;
     * ahora se recorta en bloque cada 100 muestras extra.
     */
    private val displaySensorBuffer = ArrayList<SensorEventData>()

    /** Contador de throttle para publicar al display solo cada N muestras (~4Hz visual) */
    private var sensorSampleCount = 0
    private const val PUBLISH_EVERY_N = 12 // A 50Hz, publicar cada 12 muestras ≈ 4Hz de refresco
    private const val DISPLAY_BUFFER_MAX = 500
    private const val DISPLAY_BUFFER_TRIM_AT = 600 // Recortar en lote cuando llegue a 600

    /** Snapshot thread-safe del buffer de visualización, leído por SettingsActivity */
    @Volatile
    var displaySnapshot: List<SensorEventData> = emptyList()
        private set

    /** Segundos restantes del temporizador de sesión (120 = 2 minutos) */
    @Volatile
    var remainingSeconds: Int = 120
        private set

    /**
     * Control de guardado periódico a disco.
     * En vez de guardar en cada inferencia (que bloquea el main thread con I/O),
     * solo guardamos cada SAVE_INTERVAL_MS o en eventos críticos (start/stop/fall/alert).
     */
    private var lastSaveTimeMs = 0L
    private const val SAVE_INTERVAL_MS = 10_000L // Guardar a disco cada 10 segundos máximo

    fun startSession(context: Context, emergencyNumber: String) {
        // Limpiar buffers de sesión anterior
        fullSensorHistory.clear()
        displaySensorBuffer.clear()
        sensorSampleCount = 0
        displaySnapshot = emptyList()
        remainingSeconds = 120
        lastSaveTimeMs = System.currentTimeMillis()

        currentSession = MonitoringSessionLog(
            sessionStartMillis = System.currentTimeMillis(),
            emergencyNumber = emergencyNumber
        )
        saveCurrentSession(context) // Guardar inicio (evento crítico)
    }

    /**
     * Registra una inferencia completada. NO guarda a disco inmediatamente
     * para evitar bloquear el main thread con I/O en cada ciclo.
     */
    fun recordWindow(context: Context) {
        currentSession?.let {
            currentSession = it.copy(windowsProcessed = it.windowsProcessed + 1)
            saveIfNeeded(context) // Guardado periódico, no en cada llamada
        }
    }

    /** Registra una caída detectada. Guarda inmediatamente (evento crítico). */
    fun recordFall(context: Context) {
        currentSession?.let {
            currentSession = it.copy(fallCount = it.fallCount + 1)
            saveCurrentSession(context) // Evento crítico → guardar inmediato
        }
    }

    /**
     * Registra datos crudos del sensor acelerómetro.
     * OPTIMIZACIÓN CRÍTICA vs versión anterior:
     * - ArrayList en vez de CopyOnWriteArrayList (add es O(1) amortizado, no O(n))
     * - Recorte en lote del display buffer (cada 100 extras, no removeAt(0) en cada muestra)
     * - Sin I/O de disco
     */
    fun recordSensorData(x: Float, y: Float, z: Float) {
        currentSession?.let {
            val offset = System.currentTimeMillis() - it.sessionStartMillis
            val data = SensorEventData(offset, x, y, z)

            // Guardar en historial completo (para exportación a JSON/Python)
            fullSensorHistory.add(data)

            // Guardar en buffer de visualización
            displaySensorBuffer.add(data)

            // Recorte en lote: solo cuando excede TRIM_AT, recortar a MAX de una sola vez
            // Esto evita el costoso removeAt(0) en cada muestra individual
            if (displaySensorBuffer.size >= DISPLAY_BUFFER_TRIM_AT) {
                val fromIndex = displaySensorBuffer.size - DISPLAY_BUFFER_MAX
                val trimmed = ArrayList(displaySensorBuffer.subList(fromIndex, displaySensorBuffer.size))
                displaySensorBuffer.clear()
                displaySensorBuffer.addAll(trimmed)
            }

            // Throttle: publicar snapshot solo cada N muestras para refresco suave
            sensorSampleCount++
            if (sensorSampleCount >= PUBLISH_EVERY_N) {
                sensorSampleCount = 0
                displaySnapshot = ArrayList(displaySensorBuffer)
            }
        }
    }

    /**
     * Actualiza la predicción actual. NO guarda a disco inmediatamente
     * para evitar I/O excesivo en el main thread.
     */
    @Synchronized
    fun updatePrediction(context: Context, prediction: String, className: String) {
        currentSession?.let {
            it.predictionHistory.add(PredictionEvent(it.durationSeconds.toInt(), className))
            currentSession = it.copy(currentPrediction = prediction)
            saveIfNeeded(context) // Guardado periódico
        }
    }

    /** Registra una alerta enviada. Guarda inmediatamente (evento crítico). */
    fun recordAlert(context: Context) {
        currentSession?.let {
            currentSession = it.copy(alertsTriggered = it.alertsTriggered + 1)
            saveCurrentSession(context) // Evento crítico → guardar inmediato
        }
    }

    /** Actualiza el contador de tiempo restante (invocado por el CountDownTimer de MainActivity) */
    fun updateRemainingSeconds(seconds: Int) {
        remainingSeconds = seconds
    }

    fun stopSession(context: Context) {
        currentSession?.let {
            // Copiar los datos completos del sensor al log de sesión antes de guardar
            it.sensorHistory.clear()
            it.sensorHistory.addAll(fullSensorHistory)
            currentSession = it.copy(sessionEndMillis = System.currentTimeMillis())
            saveCurrentSession(context) // Evento crítico → guardar inmediato con todos los datos
        }
    }

    fun getCurrentSession(): MonitoringSessionLog? = currentSession

    fun loadLastSession(context: Context): MonitoringSessionLog? {
        val file = File(context.filesDir, LOG_FILE_NAME)
        if (!file.exists()) return null
        return try {
            val json = JSONObject(file.readText())
            MonitoringSessionLog.fromJson(json)
        } catch (_: Exception) {
            null
        }
    }

    fun exportReportToDownloads(context: Context): String? {
        val session = currentSession ?: loadLastSession(context) ?: return null

        // Si la sesión activa no tiene datos de sensor copiados aún, inyectarlos
        if (session.sensorHistory.isEmpty() && fullSensorHistory.isNotEmpty()) {
            session.sensorHistory.addAll(fullSensorHistory)
        }

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

    /** Guarda a disco solo si ha pasado el intervalo mínimo desde el último guardado. */
    private fun saveIfNeeded(context: Context) {
        val now = System.currentTimeMillis()
        if (now - lastSaveTimeMs >= SAVE_INTERVAL_MS) {
            lastSaveTimeMs = now
            saveCurrentSession(context)
        }
    }

    private fun saveCurrentSession(context: Context) {
        currentSession?.let {
            lastSaveTimeMs = System.currentTimeMillis()
            val file = File(context.filesDir, LOG_FILE_NAME)
            file.writeText(it.toJson().toString(2))
        }
    }
}
