package com.empresa.aplicacionedgeimpulse17

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var isMonitoring = false

    private lateinit var etPhone: EditText
    private lateinit var btnToggleMonitor: Button
    private lateinit var tvStatus: TextView
    private lateinit var tvPrediction: TextView

    // Configuración de Edge Impulse
    private val bufferSize = 300 // EI_CLASSIFIER_DSP_INPUT_FRAME_SIZE (100 samples * 3 axes)
    private val featuresBuffer = FloatArray(bufferSize)
    private var bufferIndex = 0

    // Temporizador de 5 segundos
    private var fallTimer: CountDownTimer? = null
    private var isTimerRunning = false
    private val FALL_THRESHOLD = 0.85f // Umbral de confianza
    
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
        "lying_down_fs" to "Acostándose (FS)",
        "run" to "Corriendo",
        "sitting_down" to "Sentándose",
        "standing_up_fl" to "Levantándose (FL)",
        "standing_up_fs" to "Levantándose (FS)",
        "walk" to "Caminando"
    )

    companion object {
        private const val TAG = "EdgeImpulseAppLogs"

        init {
            System.loadLibrary("aplicacionedgeimpulse17")
        }
    }

    external fun runClassification(features: FloatArray): String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etPhone = findViewById(R.id.etPhone)
        btnToggleMonitor = findViewById(R.id.btnToggleMonitor)
        tvStatus = findViewById(R.id.tvStatus)
        tvPrediction = findViewById(R.id.tvPrediction)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        btnToggleMonitor.setOnClickListener {
            val phone = etPhone.text.toString()
            if (phone.isEmpty()) {
                Toast.makeText(this, "Ingresa un número válido para WhatsApp", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (isMonitoring) {
                stopMonitoring()
            } else {
                startMonitoring()
            }
        }
    }

    private fun startMonitoring() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
            isMonitoring = true
            btnToggleMonitor.text = "Detener Monitoreo"
            tvStatus.text = "Monitoreando..."
            bufferIndex = 0
            logInfo("Monitoreo iniciado.")
        } ?: logError("Acelerómetro no disponible.")
    }

    private fun stopMonitoring() {
        sensorManager.unregisterListener(this)
        isMonitoring = false
        btnToggleMonitor.text = "Iniciar Monitoreo"
        tvStatus.text = "Detenido"
        cancelFallTimer()
        logInfo("Monitoreo detenido.")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || !isMonitoring) return

        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            featuresBuffer[bufferIndex++] = event.values[0]
            featuresBuffer[bufferIndex++] = event.values[1]
            featuresBuffer[bufferIndex++] = event.values[2]

            if (bufferIndex >= bufferSize) {
                bufferIndex = 0
                performInference()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun performInference() {
        val resultString = runClassification(featuresBuffer)

        if (resultString.startsWith("ERROR")) {
            logError("Fallo en inferencia: $resultString")
            return
        }

        val parts = resultString.split("|")
        if (parts.size == 2) {
            val label = parts[0]
            val confidence = parts[1].toFloatOrNull() ?: 0f
            val percentage = (confidence * 100).roundToInt()
            val translatedLabel = classTranslations[label] ?: label

            runOnUiThread {
                tvPrediction.text = "Predicción: $translatedLabel ($percentage%)"
            }

            logInfo("Inferencia completada: $label ($percentage%)")

            if (FALL_CLASSES.contains(label) && confidence >= FALL_THRESHOLD) {
                if (!isTimerRunning) {
                    logInfo("Posible caída detectada ($label). Iniciando temporizador.")
                    startFallTimer(translatedLabel)
                }
            }
        }
    }

    private fun startFallTimer(fallType: String) {
        isTimerRunning = true
        runOnUiThread { tvStatus.text = "¡Alerta! $fallType en 5s..." }

        fallTimer = object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val sec = millisUntilFinished / 1000
                logInfo("Temporizador de caída: ${sec}s restantes.")
                runOnUiThread { tvStatus.text = "¡Alerta en ${sec}s!" }
            }

            override fun onFinish() {
                isTimerRunning = false
                logInfo("Temporizador finalizado. Ejecutando protocolo de emergencia.")
                runOnUiThread { tvStatus.text = "Abriendo WhatsApp..." }
                executeEmergencyProtocol(fallType)
            }
        }.start()
    }

    private fun cancelFallTimer() {
        fallTimer?.cancel()
        isTimerRunning = false
        runOnUiThread { if (isMonitoring) tvStatus.text = "Monitoreando..." }
        logInfo("Temporizador de caída cancelado.")
    }

    private fun executeEmergencyProtocol(fallType: String) {
        val phone = etPhone.text.toString().replace("+", "").replace(" ", "")
        val message = "🚨 *SOS: ALERTA DE EMERGENCIA* 🚨\nSufrí una posible caída o accidente ($fallType). Por favor, comunícate conmigo de inmediato o envía ayuda."
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://api.whatsapp.com/send?phone=$phone&text=${Uri.encode(message)}")
            startActivity(intent)
            logInfo("Abriendo WhatsApp con mensaje de emergencia hacia $phone.")
        } catch (e: Exception) {
            logError("Error al abrir WhatsApp: ${e.message}")
            Toast.makeText(this, "Error al abrir WhatsApp. ¿Está instalado?", Toast.LENGTH_LONG).show()
        }
        
        // Reiniciar estado
        if(isMonitoring) {
            runOnUiThread { tvStatus.text = "Monitoreando..." }
        }
    }

    // Funciones de Logs
    private fun logInfo(message: String) {
        Log.i(TAG, message)
    }

    private fun logError(message: String) {
        Log.e(TAG, message)
    }
}