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
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference

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
    val predictionHistory: CopyOnWriteArrayList<PredictionEvent> = CopyOnWriteArrayList(),
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
     * Se escribe exclusivamente desde el hilo del sensor (main thread)
     * y se lee al finalizar la sesión. Pre-dimensionada para ~6000 muestras
     * (50Hz × 120s) para evitar re-dimensionamientos costosos de la ArrayList.
     */
    private val fullSensorHistory = ArrayList<SensorEventData>(7000)

    /**
     * Buffer circular de visualización para el gráfico en tiempo real.
     * Se escribe desde el main thread (sensor) y se publica periódicamente
     * al displaySnapshot atómico. Tamaño fijo para evitar copias costosas.
     */
    private val displaySensorBuffer = ArrayList<SensorEventData>(550)

    /** Contador de throttle para publicar al display solo cada N muestras (~4Hz visual) */
    private var sensorSampleCount = 0
    private const val PUBLISH_EVERY_N = 12 // A 50Hz, publicar cada 12 muestras ≈ 4Hz de refresco
    private const val DISPLAY_BUFFER_MAX = 500
    private const val DISPLAY_BUFFER_TRIM_AT = 600 // Recortar en lote cuando llegue a 600

    /**
     * Snapshot thread-safe del buffer de visualización, leído por SettingsActivity.
     * Usa AtomicReference para evitar copias innecesarias y garantizar visibilidad
     * entre hilos sin sincronización pesada.
     */
    private val displaySnapshotRef = AtomicReference<List<SensorEventData>>(emptyList())

    /** Propiedad de acceso público al snapshot (solo lectura) */
    val displaySnapshot: List<SensorEventData>
        get() = displaySnapshotRef.get()

    /** Segundos restantes del temporizador de sesión (120 = 2 minutos) */
    @Volatile
    var remainingSeconds: Int = 120
        private set

    /**
     * Control de guardado periódico a disco.
     * En vez de guardar en cada inferencia (que bloquea el hilo),
     * solo guardamos cada SAVE_INTERVAL_MS o en eventos críticos (start/stop/fall/alert).
     */
    private var lastSaveTimeMs = 0L
    private const val SAVE_INTERVAL_MS = 10_000L // Guardar a disco cada 10 segundos máximo

    /**
     * Executor dedicado para I/O de disco, separado del hilo de inferencia
     * para evitar que la escritura a archivo retrase las clasificaciones.
     */
    private val ioExecutor = Executors.newSingleThreadExecutor()

    /**
     * Flag atómico para evitar saturar el ioExecutor con tareas de guardado
     * cuando aún no ha terminado la anterior.
     */
    @Volatile
    private var isSaving = false

    fun startSession(context: Context, emergencyNumber: String) {
        // Limpiar buffers de sesión anterior
        fullSensorHistory.clear()
        displaySensorBuffer.clear()
        sensorSampleCount = 0
        displaySnapshotRef.set(emptyList())
        remainingSeconds = 120
        lastSaveTimeMs = System.currentTimeMillis()

        currentSession = MonitoringSessionLog(
            sessionStartMillis = System.currentTimeMillis(),
            emergencyNumber = emergencyNumber
        )
        saveCurrentSessionAsync(context) // Guardar inicio (evento crítico)
    }

    /**
     * Registra una inferencia completada. NO guarda a disco inmediatamente
     * para evitar bloquear el hilo con I/O en cada ciclo.
     * Sincronizado para proteger la mutación de currentSession.
     */
    @Synchronized
    fun recordWindow(context: Context) {
        currentSession?.let {
            currentSession = it.copy(windowsProcessed = it.windowsProcessed + 1)
            saveIfNeeded(context) // Guardado periódico, no en cada llamada
        }
    }

    /** Registra una caída detectada. Guarda inmediatamente (evento crítico). */
    @Synchronized
    fun recordFall(context: Context) {
        currentSession?.let {
            currentSession = it.copy(fallCount = it.fallCount + 1)
            saveCurrentSessionAsync(context) // Evento crítico → guardar inmediato
        }
    }

    /**
     * Registra datos crudos del sensor acelerómetro.
     * OPTIMIZACIÓN CRÍTICA:
     * - ArrayList pre-dimensionada (add es O(1) amortizado sin resize)
     * - Recorte en lote del display buffer (cada 100 extras, no removeAt(0) en cada muestra)
     * - Publicación atómica del snapshot sin crear copias innecesarias constantemente
     * - Sin I/O de disco, sin sincronización pesada
     */
    fun recordSensorData(x: Float, y: Float, z: Float) {
        val session = currentSession ?: return
        val offset = System.currentTimeMillis() - session.sessionStartMillis
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
            // Publicar atómicamente para que SettingsActivity lo lea sin contención
            displaySnapshotRef.set(ArrayList(displaySensorBuffer))
        }
    }

    /**
     * Actualiza la predicción actual.
     * Sincronizado para proteger la mutación concurrente de currentSession
     * desde el hilo de inferencia.
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
    @Synchronized
    fun recordAlert(context: Context) {
        currentSession?.let {
            currentSession = it.copy(alertsTriggered = it.alertsTriggered + 1)
            saveCurrentSessionAsync(context) // Evento crítico → guardar inmediato
        }
    }

    /** Actualiza el contador de tiempo restante (invocado por el CountDownTimer de MainActivity) */
    fun updateRemainingSeconds(seconds: Int) {
        remainingSeconds = seconds
    }

    @Synchronized
    fun stopSession(context: Context) {
        currentSession?.let {
            // Copiar los datos completos del sensor al log de sesión antes de guardar
            it.sensorHistory.clear()
            it.sensorHistory.addAll(fullSensorHistory)
            currentSession = it.copy(sessionEndMillis = System.currentTimeMillis())
            // Guardar final de forma SÍNCRONA para asegurar que todos los datos se persistan
            saveCurrentSessionSync(context)
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
            saveCurrentSessionAsync(context)
        }
    }

    /**
     * Guarda la sesión actual a disco de forma ASÍNCRONA en el hilo de I/O dedicado.
     * Evita bloquear tanto el main thread como el hilo de inferencia.
     * Si ya hay un guardado en curso, se salta esta solicitud para no acumular tareas.
     */
    private fun saveCurrentSessionAsync(context: Context) {
        if (isSaving) return // Ya hay un guardado en progreso, no acumular
        val session = currentSession ?: return
        // Tomar un snapshot de los datos antes de enviar al hilo de I/O
        val jsonString = try {
            session.toJson().toString(2)
        } catch (_: Exception) {
            return
        }
        isSaving = true
        ioExecutor.execute {
            try {
                val file = File(context.filesDir, LOG_FILE_NAME)
                file.writeText(jsonString)
            } catch (_: Exception) {
                // Silenciar errores de I/O para no crashear
            } finally {
                isSaving = false
            }
        }
    }

    /**
     * Guarda la sesión actual a disco de forma SÍNCRONA.
     * Solo se usa en stopSession() para asegurar que los datos finales se persistan.
     */
    private fun saveCurrentSessionSync(context: Context) {
        currentSession?.let {
            lastSaveTimeMs = System.currentTimeMillis()
            val file = File(context.filesDir, LOG_FILE_NAME)
            file.writeText(it.toJson().toString(2))
        }
    }
}
