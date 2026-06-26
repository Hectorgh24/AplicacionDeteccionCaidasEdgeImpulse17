package com.empresa.aplicacionedgeimpulse17

import java.net.DatagramPacket
import java.net.DatagramSocket
import kotlin.concurrent.thread

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.CountDownTimer
import android.os.PowerManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var isMonitoring = false

    private lateinit var etPhone: EditText
    private lateinit var btnToggleMonitor: Button
    private lateinit var tvStatus: TextView
    private lateinit var tvPrediction: TextView
    private lateinit var tvTimer: TextView

    // Configuración de Edge Impulse
    private val bufferSize = 300 // EI_CLASSIFIER_DSP_INPUT_FRAME_SIZE (100 samples * 3 axes)
    private val featuresBuffer = FloatArray(bufferSize)
    private var bufferIndex = 0

    private val FALL_THRESHOLD = 0.85f // Umbral de confianza
    private var isAlertActive = false

    /** Temporizador de 2 minutos (120 000 ms) para auto-detener la sesión */
    private var sessionTimer: CountDownTimer? = null

    /** Executor para no bloquear el hilo principal durante la inferencia C++ */
    private val inferenceExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    /**
     * Flag atómico para evitar saturar el executor con tareas de inferencia.
     * Si una inferencia está en progreso, la siguiente ventana se descarta.
     * Esto previene la acumulación de tareas que causa congelamiento progresivo.
     */
    private val inferenceInProgress = AtomicBoolean(false)

    /** WakeLock parcial para mantener la CPU activa con la pantalla apagada */
    private var wakeLock: PowerManager.WakeLock? = null

    // Clases que representan caídas
    private val FALL_CLASSES = listOf(
        "fall_backward", "fall_bending", "fall_forward", 
        "fall_hand", "fall_sideward_left", "fall_sideward_right", 
        "fall_sitting", "fall_syncope"
    )

    // Diccionario de traducciones para la interfaz de usuario
    private val classTranslations = mapOf(
        "fall_backward" to "Caída hacia atrás",
        "fall_bending" to "Caída doblándose",
        "fall_forward" to "Caída hacia adelante",
        "fall_hand" to "Caída sobre manos",
        "fall_sideward_left" to "Caída lateral izquierda",
        "fall_sideward_right" to "Caída lateral derecha",
        "fall_sitting" to "Caída sentado",
        "fall_syncope" to "Caída por desmayo (síncope)",
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

    companion object {
        private const val TAG = "EdgeImpulseAppLogs"
        private const val PERMISSION_REQUEST_CODE = 101
        private const val REQUEST_CODE_ALERT = 102

        init {
            System.loadLibrary("aplicacionedgeimpulse17")
        }
    }

    external fun runClassification(features: FloatArray): String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try { androidx.core.content.ContextCompat.startForegroundService(this, android.content.Intent(this, DummyForegroundService::class.java)) } catch (e: Exception) {}
        startUdpListener()
        setContentView(R.layout.activity_main)

        etPhone = findViewById(R.id.etPhone)
        btnToggleMonitor = findViewById(R.id.btnToggleMonitor)
        tvStatus = findViewById(R.id.tvStatus)
        tvPrediction = findViewById(R.id.tvPrediction)
        tvTimer = findViewById(R.id.tvTimer)

        sensorManager = applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        checkPermissions()

        btnToggleMonitor.setOnClickListener {
            val phone = etPhone.text.toString()
            if (phone.length != 10) {
                Toast.makeText(this, "Ingresa un número válido de 10 dígitos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (isMonitoring) {
                stopMonitoring()
            } else {
                startMonitoring()
            }
        }
    }

    private fun checkPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.SEND_SMS,
            Manifest.permission.CALL_PHONE
        )
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.ACTIVITY_RECOGNITION)
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        
        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missingPermissions.toTypedArray(), PERMISSION_REQUEST_CODE)
        }
    }

    private fun startMonitoring() {
        accelerometer?.let {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "EdgeImpulse17::MonitoringWakeLock"
            ).apply {
                acquire(3 * 60 * 1000L)
            }

            isMonitoring = true
            isAlertActive = false
            inferenceInProgress.set(false)
            btnToggleMonitor.text = "Detener Monitoreo"
            tvStatus.text = "Preparando en 5 segundos..."
            bufferIndex = 0
            etPhone.isEnabled = false
            tvTimer.visibility = TextView.VISIBLE

            // Fase de preparación de 5 segundos
            object : CountDownTimer(5000L, 1000L) {
                override fun onTick(millisUntilFinished: Long) {
                    val sec = (millisUntilFinished / 1000).toInt()
                    tvTimer.text = "Iniciando en: $sec s"
                }

                override fun onFinish() {
                    if (!isMonitoring) return // Cancelado durante la preparación
                    tvStatus.text = "Monitoreando..."
                    sensorManager.registerListener(this@MainActivity, it, SensorManager.SENSOR_DELAY_GAME)
                    MonitoringLogManager.startSession(this@MainActivity, etPhone.text.toString().trim())
                    startSessionTimer()
                    logInfo("Monitoreo iniciado (WakeLock adquirido).")
                }
            }.start()

        } ?: logError("Acelerómetro no disponible.")
    }

    private fun stopMonitoring() {
        sessionTimer?.cancel()
        sessionTimer = null
        sensorManager.unregisterListener(this)
        isMonitoring = false
        btnToggleMonitor.text = "Iniciar Monitoreo"
        tvStatus.text = "Detenido — Puede exportar datos en Ajustes"
        etPhone.isEnabled = true
        MonitoringLogManager.stopSession(this)

        // Liberar WakeLock
        wakeLock?.let {
            if (it.isHeld) it.release()
        }
        wakeLock = null

        logInfo("Monitoreo detenido (WakeLock liberado).")
    }

    /**
     * Temporizador de sesión: cuenta regresiva de 120 segundos.
     * Al llegar a 0, detiene el monitoreo automáticamente guardando todos los datos.
     */
    private fun startSessionTimer() {
        sessionTimer?.cancel()
        tvTimer.visibility = TextView.VISIBLE
        sessionTimer = object : CountDownTimer(120_000L, 1_000L) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = (millisUntilFinished / 1000).toInt()
                MonitoringLogManager.updateRemainingSeconds(seconds)
                val min = seconds / 60
                val sec = seconds % 60
                tvTimer.text = String.format("Tiempo restante: %d:%02d", min, sec)
            }

            override fun onFinish() {
                MonitoringLogManager.updateRemainingSeconds(0)
                tvTimer.text = "Tiempo restante: 0:00"
                logInfo("Temporizador de 2 minutos completado. Auto-deteniendo monitoreo.")
                stopMonitoring()
                Toast.makeText(
                    this@MainActivity,
                    "Sesión de 2 minutos completada. Vaya a Ajustes para exportar datos.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }.start()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        // NOTA: isAlertActive ya NO bloquea la recoleccion de datos.
        // El sensor captura siempre durante los 120s. La alerta solo impide
        // lanzar una nueva AlertActivity mientras ya hay una activa.
        if (event == null || !isMonitoring) return

        try {
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                featuresBuffer[bufferIndex++] = x
                featuresBuffer[bufferIndex++] = y
                featuresBuffer[bufferIndex++] = z

                MonitoringLogManager.recordSensorData(x, y, z)

                if (bufferIndex >= bufferSize) {
                    // Solo enviar si no hay inferencia en progreso para evitar acumulación de tareas.
                    // Si la inferencia anterior no ha terminado, se descarta esta ventana.
                    // Esto previene la saturación del executor que causa congelamiento progresivo.
                    if (inferenceInProgress.compareAndSet(false, true)) {
                        val bufferToProcess = featuresBuffer.clone()
                        performInferenceAsync(bufferToProcess)
                    } else {
                        // Si se descarta la ventana porque la inferencia anterior no ha terminado,
                        // registramos una predicción duplicada para mantener los intervalos de 1 segundo
                        // exactos tanto en el gráfico como en el archivo JSON exportado.
                        MonitoringLogManager.recordDuplicatePrediction(this@MainActivity)
                    }

                    // Sliding window: Avanzar 1 segundo (50 muestras * 3 ejes = 150 floats)
                    // Esto permite generar predicciones y guardarlas cada segundo.
                    val shiftElements = 150
                    val remainElements = bufferSize - shiftElements
                    System.arraycopy(featuresBuffer, shiftElements, featuresBuffer, 0, remainElements)
                    bufferIndex = remainElements
                }
            }
        } catch (e: Exception) {
            logError("Error en onSensorChanged: ${e.message}")
            // Resetear bufferIndex a un múltiplo de 3 válido para recuperarse
            bufferIndex = (bufferIndex / 3) * 3
            if (bufferIndex >= bufferSize) bufferIndex = 0
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    /**
     * Ejecuta la inferencia del modelo Edge Impulse en un hilo de fondo.
     * Al terminar, actualiza la UI y libera el flag atómico para permitir
     * la siguiente inferencia. Toda la lógica post-inferencia (actualización
     * de predicción, registro de ventana, detección de caída) se ejecuta
     * dentro del bloque del executor para evitar bloquear el main thread.
     */
    private fun performInferenceAsync(features: FloatArray) {
        // Watchdog: si la inferencia C++ se cuelga más de 2s, forzar liberación del flag
        // para que el siguiente ciclo del sensor pueda intentar de nuevo.
        val watchdog = android.os.Handler(android.os.Looper.getMainLooper())
        val watchdogTask = Runnable {
            if (inferenceInProgress.compareAndSet(true, false)) {
                logError("Watchdog: inferencia C++ superó 2s, flag liberado forzadamente.")
                MonitoringLogManager.recordDuplicatePrediction(this@MainActivity)
            }
        }
        watchdog.postDelayed(watchdogTask, 2000L)

        inferenceExecutor.execute {
            try {
                val resultString = runClassification(features)

                // Cancelar watchdog si la inferencia terminó a tiempo
                watchdog.removeCallbacks(watchdogTask)

                if (resultString.startsWith("ERROR")) {
                    logError("Fallo en inferencia: $resultString")
                    // Registrar duplicado para no perder el slot de 1 segundo en el JSON
                    MonitoringLogManager.recordDuplicatePrediction(this@MainActivity)
                    return@execute
                }

                val parts = resultString.split("|")
                if (parts.size == 2) {
                    val label = parts[0].replace("\u0000", "").trim()
                    val confidence = parts[1].replace("\u0000", "").trim().replace(",", ".").toFloatOrNull() ?: 0f
                    val percentage = (confidence * 100).roundToInt()
                    val translatedLabel = classTranslations[label] ?: label
                    val predictionText = "$translatedLabel ($percentage%)"

                    // Actualizar UI en el main thread
                    runOnUiThread {
                        tvPrediction.text = "Predicción: $predictionText"
                    }

                    logInfo("Inferencia completada: $label ($percentage%)")

                    // Registrar predicción y ventana (sincronizado internamente en MonitoringLogManager)
                    MonitoringLogManager.updatePrediction(this@MainActivity, predictionText, label)
                    MonitoringLogManager.recordWindow(this@MainActivity)

                    // Detectar caida: solo lanzar alerta si no hay una activa ya
                    if (FALL_CLASSES.contains(label) && confidence >= FALL_THRESHOLD && !isAlertActive) {
                        MonitoringLogManager.recordFall(this@MainActivity)
                        logInfo("Posible caída detectada ($label). Lanzando AlertActivity.")
                        runOnUiThread {
                            startFallAlert(translatedLabel)
                        }
                    }
                } else {
                    // Formato inesperado → duplicar predicción para no perder slot
                    MonitoringLogManager.recordDuplicatePrediction(this@MainActivity)
                }
            } catch (t: Throwable) {
                // Capturar Throwable (no solo Exception) para atrapar errores JNI de bajo nivel
                // como UnsatisfiedLinkError, NullPointerException nativa, etc.
                logError("Error grave en inferencia C++: ${t.message}")
                watchdog.removeCallbacks(watchdogTask)
                MonitoringLogManager.recordDuplicatePrediction(this@MainActivity)
            } finally {
                // SIEMPRE liberar el flag para permitir la siguiente inferencia.
                // Este bloque se ejecuta incluso si hay return@execute arriba.
                inferenceInProgress.set(false)
            }
        }
    }

    private fun startFallAlert(fallType: String) {
        isAlertActive = true
        val phone = etPhone.text.toString().trim()

        // Safeguard: si AlertActivity no responde en 30s (pantalla apagada),
        // resetear el flag para no bloquear el resto de la sesion.
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            if (isAlertActive) {
                isAlertActive = false
                logError("Safeguard: AlertActivity no respondio en 30s, isAlertActive reseteado.")
            }
        }, 30_000L)
        
        val intent = Intent(this, AlertActivity::class.java).apply {
            putExtra(AlertActivity.EXTRA_PHONE, phone)
            putExtra(AlertActivity.EXTRA_FALL_TYPE, fallType)
        }
        startActivityForResult(intent, REQUEST_CODE_ALERT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_ALERT) {
            isAlertActive = false
            bufferIndex = 0
            if (isMonitoring) {
                tvStatus.text = "Monitoreando..."
            }
        }
    }

    override fun onDestroy() {
        sessionTimer?.cancel()
        sessionTimer = null
        if (isMonitoring) {
            MonitoringLogManager.stopSession(this)
        }
        // Liberar WakeLock si aún está activo
        wakeLock?.let {
            if (it.isHeld) it.release()
        }
        wakeLock = null
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Funciones de Logs
    private fun logInfo(message: String) {
        Log.i(TAG, message)
    }

    private fun logError(message: String) {
        Log.e(TAG, message)
    }

    private fun startUdpListener() {
        thread(isDaemon = true) {
            try {
                val socket = DatagramSocket(null)
                socket.reuseAddress = true
                socket.bind(java.net.InetSocketAddress(50000))
                socket.broadcast = true
                val buffer = ByteArray(256)
                while (true) {
                    val packet = DatagramPacket(buffer, buffer.size)
                    socket.receive(packet)
                    val message = String(packet.data, 0, packet.length).trim()
                    Log.d("UDP_LISTENER", "Recibido: $message")
                    
                    if (message == "START_MONITORING") {
                        if (!isMonitoring) {
                            runOnUiThread { startMonitoring() }
                        }
                    } else if (message == "STOP_MONITORING") {
                        if (isMonitoring) {
                            runOnUiThread { stopMonitoring() }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("UDP_LISTENER", "Error: ${e.message}")
            }
        }
    }
}
