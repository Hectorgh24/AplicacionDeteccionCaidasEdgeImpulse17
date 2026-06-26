package com.empresa.aplicacionedgeimpulse17 // Qué: Declaración de empaquetado Kotlin. Para qué: Envolver la aplicación en un identificador único. Por qué: Requisito de Android OS para no chocar con la versión de 9 clases si ambas se instalan.

import java.net.DatagramPacket // Qué: Importa clase de paquetes UDP. Para qué: Operaciones IP locales. Por qué: Necesaria para red experimental.
import java.net.DatagramSocket // Qué: Importa puertos UDP. Para qué: Abrir socket de escucha. Por qué: Mismo motivo.
import kotlin.concurrent.thread // Qué: Importa multihilo básico Kotlin. Para qué: Sacar tareas de red del Main Thread. Por qué: IO Network exception.

import android.Manifest // Qué: Permisos del sistema. Para qué: Identificar requerimientos al humano. Por qué: Reglas de privacidad Google.
import android.content.Context // Qué: Puente a SO nativo. Para qué: Alcanzar el Kernel y sus servicios puramente asíncronos nativos OS. Por qué: Interfaz maestra Android.
import android.content.Intent // Qué: Encapsulador de viaje. Para qué: Invocar Activities o Servicios puramente asíncronos nativos OS Android base. Por qué: Patrón de navegación.
import android.content.pm.PackageManager // Qué: Revisor de sellos. Para qué: Validar si tenemos la venia de SMS y Telefonía puramente asíncrona nativa OS Android. Por qué: Prevención crasheo.
import android.hardware.Sensor // Qué: Referencia IMU hardware. Para qué: Operar chip acelerómetro físico. Por qué: Recolección física pura nativa.
import android.hardware.SensorEvent // Qué: Valija de gravedad. Para qué: Obtener floats crudos XYZ. Por qué: Insumo de Edge Impulse.
import android.hardware.SensorEventListener // Qué: Oreja de hardware. Para qué: Escuchar callbacks a 50Hz. Por qué: Interfaz obligatoria IMU.
import android.hardware.SensorManager // Qué: Dictador de hardware. Para qué: Exigir arranque/paro de IMU. Por qué: Consumo batería.
import android.os.Bundle // Qué: Paquete estado UI. Para qué: Mantener vivo el OnCreate. Por qué: Ciclo biológico Android.
import android.os.CountDownTimer // Qué: Reloj resiliente nativo. Para qué: Contar 120s del experimento IoT. Por qué: Lógica temporal sin sleeps tontos de hilo.
import android.os.PowerManager // Qué: Ministro energía Kernel. Para qué: Evitar Sleep/Doze del celular. Por qué: Si se apaga, no hay alertas puros asíncronos nativos OS Android base.
import android.util.Log // Qué: Trazador. Para qué: Dibujar Logs. Por qué: Depuración dev.
import android.view.Menu // Qué: Menú OS superior. Para qué: Desplegar engrane Ajustes. Por qué: UI Navigation.
import android.view.MenuItem // Qué: Opción del menú. Para qué: Escuchar clics a Ajustes. Por qué: Idem.
import android.widget.Button // Qué: Componente táctil rojo/verde. Para qué: Binding lógico botón Start. Por qué: Interacción.
import android.widget.EditText // Qué: Caja de texto gris. Para qué: Leer el SOS del abuelo. Por qué: Contacto emergencia pura nativa.
import android.widget.TextView // Qué: Placa textual UI. Para qué: Imprimir veredictos IA en pantalla puramente asíncrona nativa OS Android. Por qué: Feedback.
import android.widget.Toast // Qué: Cuadro de alerta fugaz. Para qué: Avisos rápidos humanos. Por qué: UX.
import androidx.appcompat.app.AppCompatActivity // Qué: Herencia Jetpack. Para qué: Soporte vista moderna sobre OS viejos puros asíncronos nativos OS Android. Por qué: Arquitectura Android moderna.
import androidx.core.app.ActivityCompat // Qué: Utilería permisos vieja. Para qué: Compatibilidad OS 9. Por qué: Crash Prevention.
import androidx.core.content.ContextCompat // Qué: Utilería color y servicios. Para qué: Revisor permisos pasivo puro asíncrono nativo OS Android base. Por qué: Seguridad.
import java.util.concurrent.ExecutorService // Qué: Constructor asíncrono C++. Para qué: Mandar matemáticas al hilo esclavo negro puro asíncrono nato OS Android. Por qué: C++ crashea UI.
import java.util.concurrent.Executors // Qué: Fabrica Hilos C++. Para qué: Asignar un Thread solitario puro asíncrono nato OS Android base. Por qué: Evita sobreescribir memoria JVM con múltiple C++.
import java.util.concurrent.atomic.AtomicBoolean // Qué: Semáforo Thread-Safe RAM. Para qué: Trancar hilos C++ si se atoran. Por qué: Resiliencia térmica CPU.
import kotlin.math.roundToInt // Qué: Módulo matemático. Para qué: 0.99 a 99% humano. Por qué: UX médica.

class MainActivity : AppCompatActivity(), SensorEventListener { // Qué: Super clase vista IMU puro asíncrono nato OS Android base interna. Para qué: Fusiona controlador gráfico y lector acelerómetro puro asíncrono nato OS Android. Por qué: Simpleza.

    private lateinit var sensorManager: SensorManager // Qué: Puntero gestor IMU puro asíncrono nato OS Android. Para qué: Prender chip. Por qué: OS Access.
    private var accelerometer: Sensor? = null // Qué: Puntero chip físico. Para qué: Leer gravedad pura asíncrona nata OS Android. Por qué: Hardware.
    private var isMonitoring = false // Qué: Bandera estado experimento pura asíncrona nata OS Android base. Para qué: Saber si 120s corren. Por qué: Lógica Toggle.

    private lateinit var etPhone: EditText // Qué: Caja teléfono UI. Para qué: Sacar SOS. Por qué: XML Binding.
    private lateinit var btnToggleMonitor: Button // Qué: Botón ON/OFF. Para qué: Control humano puro asíncrono nato OS Android. Por qué: Idem.
    private lateinit var tvStatus: TextView // Qué: Letrero UI 1. Para qué: Estatus verde/rojo. Por qué: Idem.
    private lateinit var tvPrediction: TextView // Qué: Letrero C++ UI 2. Para qué: Clase ganadora. Por qué: Idem.
    private lateinit var tvTimer: TextView // Qué: Reloj UI 3. Para qué: Cuenta 120s. Por qué: Idem.

    // Configuración de Edge Impulse
    private val bufferSize = 300 // EI_CLASSIFIER_DSP_INPUT_FRAME_SIZE (100 samples * 3 axes) // Qué: Límite flotantes Tensor. Para qué: Alimentar IA plana pura asíncrona nata OS Android. Por qué: Arquitectura DSP Edge Impulse pura asíncrona nata OS.
    private val featuresBuffer = FloatArray(bufferSize) // Qué: Arreglo nativo aplanado crudo. Para qué: Buffer de IA puro asíncrono nato OS Android. Por qué: Zero Allocation Kotlin RAM.
    private var bufferIndex = 0 // Qué: Escobilla Array C++. Para qué: Iterador secuencial puro asíncrono nato OS Android. Por qué: Evita objetos inútiles.

    private val FALL_THRESHOLD = 0.85f // Umbral de confianza // Qué: Constante matemática estricta pura asíncrona nata OS (Umbral 85%). Para qué: Filtrar ruido (17 clases son más propensas a error, se eleva umbral del 75 al 85). Por qué: Seguridad anti Falso Positivo IoT pura asíncrona nativa OS Android.
    private var isAlertActive = false // Qué: Seguro anti rebote SOS puramente asíncrono nato OS Android. Para qué: Que pantalla roja no salte mil veces. Por qué: UX y no matar hilo UI puramente nativo.

    /** Temporizador de 2 minutos (120 000 ms) para auto-detener la sesión */
    private var sessionTimer: CountDownTimer? = null // Qué: Estructura bomba tiempo. Para qué: Reloj de muerte experimento puro asíncrono nato OS Android. Por qué: Protocolo IoT.

    /** Executor para no bloquear el hilo principal durante la inferencia C++ */
    private val inferenceExecutor: ExecutorService = Executors.newSingleThreadExecutor() // Qué: Hilo esclavo CPU. Para qué: Procesar JNI C++ oscuro. Por qué: Multihilo mandatorio puramente asíncrono nativo OS Android base.

    /**
     * Flag atómico para evitar saturar el executor con tareas de inferencia.
     * Si una inferencia está en progreso, la siguiente ventana se descarta.
     * Esto previene la acumulación de tareas que causa congelamiento progresivo.
     */
    private val inferenceInProgress = AtomicBoolean(false) // Qué: Cerraja Atómica Thread-Safe. Para qué: Proteger CPU de asfixia C++ pura asíncrona nata OS Android. Por qué: Deadlock prevention.

    /** WakeLock parcial para mantener la CPU activa con la pantalla apagada */
    private var wakeLock: PowerManager.WakeLock? = null // Qué: Trampa para el SO Android Doze Mode puro asíncrono nato OS Android. Para qué: No apagar el cerebro del teléfono. Por qué: Lectura background requerida puramente asíncrona nativa OS Android.

    // Clases que representan caídas
    private val FALL_CLASSES = listOf( // Qué: Catálogo duro binario letal (8 caídas malas). Para qué: Dividir las 17 clases (8 malas vs 9 buenas). Por qué: Lógica SOS asíncrona nata OS Android.
        "fall_backward", "fall_bending", "fall_forward", // Qué: Bloque 1 rojas. Para qué: Condicional. Por qué: Etiquetas C++.
        "fall_hand", "fall_sideward_left", "fall_sideward_right", // Qué: Bloque 2 rojas. Para qué: Condicional. Por qué: Idem.
        "fall_sitting", "fall_syncope" // Qué: Bloque 3 rojas. Para qué: Cierre lista negra. Por qué: Idem puro asíncrono nato OS.
    ) // Qué: Fin catálogo 8 caídas de modelo de 17. Para qué: N/A. Por qué: N/A.

    // Diccionario de traducciones para la interfaz de usuario
    private val classTranslations = mapOf( // Qué: Hash Map gigante de 17 valores crudo nativo asíncrono OS. Para qué: Bautizar al Español humano el C++ puro. Por qué: Accesibilidad paciente.
        "fall_backward" to "Caída hacia atrás", // Qué: Trad. Para qué: Humano. Por qué: UX.
        "fall_bending" to "Caída doblándose", // Qué: Trad. Para qué: Humano. Por qué: UX.
        "fall_forward" to "Caída hacia adelante", // Qué: Trad. Para qué: Humano. Por qué: UX.
        "fall_hand" to "Caída sobre manos", // Qué: Trad. Para qué: Humano. Por qué: UX.
        "fall_sideward_left" to "Caída lateral izquierda", // Qué: Trad. Para qué: Humano. Por qué: UX.
        "fall_sideward_right" to "Caída lateral derecha", // Qué: Trad. Para qué: Humano. Por qué: UX.
        "fall_sitting" to "Caída sentado", // Qué: Trad. Para qué: Humano. Por qué: UX.
        "fall_syncope" to "Caída por desmayo (síncope)", // Qué: Trad. Para qué: Humano. Por qué: UX.
        "going_down_stairs" to "Bajando escaleras", // Qué: Actividad sana. Para qué: Humano. Por qué: UX.
        "going_up_stairs" to "Subiendo escaleras", // Qué: Actividad sana. Para qué: Humano. Por qué: UX.
        "jump" to "Saltando", // Qué: Actividad sana vigorosa. Para qué: Humano. Por qué: UX.
        "lying_down_fs" to "Acostándose (Desde Silla)", // Qué: Actividad sana lenta. Para qué: Humano. Por qué: UX.
        "run" to "Corriendo", // Qué: Actividad vigorosa. Para qué: Humano. Por qué: UX.
        "sitting_down" to "Sentándose", // Qué: Actividad normal. Para qué: Humano. Por qué: UX.
        "standing_up_fl" to "Levantándose (Desde Suelo)", // Qué: Actividad recuperación. Para qué: Humano. Por qué: UX.
        "standing_up_fs" to "Levantándose (Desde Silla)", // Qué: Actividad recuperación. Para qué: Humano. Por qué: UX.
        "walk" to "Caminando" // Qué: Actividad normal base. Para qué: Humano. Por qué: UX pura asíncrona nata OS.
    ) // Qué: Fin traductor 17 clases puramente asíncrono nato OS Android base. Para qué: N/A. Por qué: N/A.

    companion object { // Qué: Módulo Estático constante puro asíncrono nato OS Android base. Para qué: No duplicar IDs. Por qué: Memoria RAM limpia puramente asíncrona nata OS.
        private const val TAG = "EdgeImpulseAppLogs" // Qué: Rótulo log. Para qué: Debug dev. Por qué: OS Logger.
        private const val PERMISSION_REQUEST_CODE = 101 // Qué: ID permiso. Para qué: OS Routing. Por qué: Callback.
        private const val REQUEST_CODE_ALERT = 102 // Qué: ID alerta. Para qué: OS Routing SOS. Por qué: Idem.

        init { // Qué: Bloque de arranque extremo JVM puro asíncrono nato OS Android. Para qué: Amarrar C++ Cmake a Kotlin. Por qué: JNI pura.
            System.loadLibrary("aplicacionedgeimpulse17") // Qué: Compilación SO Linux cargada puramente asíncrona nata OS Android. Para qué: Traer IA (Modelo de 17 clases). Por qué: Motor Edge Impulse.
        } // Qué: Fin carga C++ OS Android. Para qué: N/A. Por qué: N/A.
    } // Qué: Fin estáticos constantes. Para qué: N/A. Por qué: N/A.

    external fun runClassification(features: FloatArray): String // Qué: Firma fantasmal C++ nativa asíncrona pura. Para qué: Mandar Tensor 1D y sacar String C++. Por qué: Pasarela JNI Kotlin pura nativa asíncrona OS Android base.

    override fun onCreate(savedInstanceState: Bundle?) { // Qué: Ciclo vida 1 puro asíncrono nato OS Android. Para qué: Dibujar View. Por qué: SDK Android.
        super.onCreate(savedInstanceState) // Qué: Llama padre puro asíncrono nato OS Android base. Para qué: Crash prevention OS. Por qué: Idem.
        try { androidx.core.content.ContextCompat.startForegroundService(this, android.content.Intent(this, DummyForegroundService::class.java)) } catch (e: Exception) {} // Qué: Trampa de Servicio Frontal (Dummy puro). Para qué: Exigir vida eterna a OS Android 10+ puramente asíncrono nativo. Por qué: Impedir Doze Sleep asesino de la recolección 50Hz pura.
        startUdpListener() // Qué: Hilo remoto Python puro asíncrono nato OS Android base. Para qué: TCP/IP disparo automatizado puramente asíncrono. Por qué: Tesis IoT prueba física.
        setContentView(R.layout.activity_main) // Qué: Infla XML puro asíncrono nato OS Android base. Para qué: UI render. Por qué: Visual.

        etPhone = findViewById(R.id.etPhone) // Qué: Binding XML puro asíncrono nato OS Android. Para qué: Variable UI. Por qué: Kotlin views.
        btnToggleMonitor = findViewById(R.id.btnToggleMonitor) // Qué: Binding botón. Para qué: Idem. Por qué: Idem.
        tvStatus = findViewById(R.id.tvStatus) // Qué: Binding texto. Para qué: Idem. Por qué: Idem.
        tvPrediction = findViewById(R.id.tvPrediction) // Qué: Binding C++. Para qué: Idem. Por qué: Idem.
        tvTimer = findViewById(R.id.tvTimer) // Qué: Binding Reloj. Para qué: Idem. Por qué: Idem pura asíncrona nata OS Android base.

        sensorManager = applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager // Qué: Consigue gestor IMU puro asíncrono nato OS Android base interna general (applicationContext previene leaks). Para qué: Pedir sensor físico OS. Por qué: MemLeak safety pura nativa asíncrona.
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) // Qué: Coge acelerómetro físico puro. Para qué: XYZ crudo. Por qué: Hardware requirement puramente asíncrono nativo.

        checkPermissions() // Qué: Implora SMS/GPS/Background al humano puro asíncrono nato OS Android base. Para qué: OS Android Rules. Por qué: Idem.

        btnToggleMonitor.setOnClickListener { // Qué: Click oyente puro asíncrono nato OS Android base. Para qué: Botón ON/OFF puro asíncrono nato OS. Por qué: Acción humana.
            val phone = etPhone.text.toString() // Qué: Saca texto celular. Para qué: Validar largo puro asíncrono nato OS Android base. Por qué: SOS fail prevent.
            if (phone.length != 10) { // Qué: Validar 10 digitos. Para qué: Abortar si nulo. Por qué: Error control.
                Toast.makeText(this, "Ingresa un número válido de 10 dígitos", Toast.LENGTH_SHORT).show() // Qué: Regaño gris puro. Para qué: Humano. Por qué: UX.
                return@setOnClickListener // Qué: Sale despavorido. Para qué: Cortar Start puro asíncrono nato OS Android base. Por qué: Idem.
            } // Qué: Fin condicional. Para qué: N/A. Por qué: N/A.

            if (isMonitoring) { // Qué: Toggle On/Off puro asíncrono nato OS Android base. Para qué: Reusar botón. Por qué: UX.
                stopMonitoring() // Qué: Apaga puro. Para qué: Idem. Por qué: Idem.
            } else { // Qué: Toggle encendido. Para qué: Idem. Por qué: Idem.
                startMonitoring() // Qué: Prende puro asíncrono. Para qué: Idem. Por qué: Idem.
            } // Qué: Fin control IF/ELSE puro asíncrono nato OS. Para qué: N/A. Por qué: N/A.
        } // Qué: Fin onclick listener puro asíncrono nato OS. Para qué: N/A. Por qué: N/A.
    } // Qué: Fin OnCreate UI puro asíncrono nato OS Android base interna médica lógica pura. Para qué: N/A. Por qué: N/A.

    private fun checkPermissions() { // Qué: Embalaje Petición Permisos puramente asíncrona nata OS Android. Para qué: OS 9 a 13 crudos. Por qué: Backward compatibility OS Android base.
        val permissions = mutableListOf( // Qué: Lista peticiones. Para qué: Embalaje. Por qué: Iteración de array pura.
            Manifest.permission.SEND_SMS, // Qué: Petición SMS cruda pura. Para qué: SOS. Por qué: Funcionalidad.
            Manifest.permission.CALL_PHONE // Qué: Petición llamar pura. Para qué: SOS. Por qué: Idem.
        ) // Qué: Fin lista base. Para qué: N/A. Por qué: N/A.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) { // Qué: OS > 10. Para qué: Permisos extra puramente asíncronos natos OS. Por qué: Reglas Google nuevas.
            permissions.add(Manifest.permission.ACTIVITY_RECOGNITION) // Qué: Sello Actividad pura asíncrona nata OS. Para qué: Background sensor read. Por qué: Regla Q pura asíncrona nativa OS Android.
        } // Qué: Fin if Q OS. Para qué: N/A. Por qué: N/A.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) { // Qué: OS > 13. Para qué: Notificaciones Dummy Service puramente asíncronas nativas OS Android base interna general pura simple médica. Por qué: Regla Tiramisu pura asíncrona nativa OS Android.
            permissions.add(Manifest.permission.POST_NOTIFICATIONS) // Qué: Sello campanita. Para qué: Evitar OS mute. Por qué: Idem.
        } // Qué: Fin T OS. Para qué: N/A. Por qué: N/A.
        
        val missingPermissions = permissions.filter { // Qué: Saca los que faltan puramente asíncronos natos OS Android. Para qué: No ser hartante. Por qué: UX.
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED // Qué: Valida estado actual puro asíncrono nato OS. Para qué: Idem. Por qué: Idem.
        } // Qué: Fin filtro asíncrono. Para qué: N/A. Por qué: N/A.
        if (missingPermissions.isNotEmpty()) { // Qué: Si falta alguno. Para qué: Disparar OS Popup puro asíncrono. Por qué: Legal.
            ActivityCompat.requestPermissions(this, missingPermissions.toTypedArray(), PERMISSION_REQUEST_CODE) // Qué: Popups OS crudos. Para qué: Venia Humana puro asíncrono. Por qué: Idem.
        } // Qué: Fin disparador. Para qué: N/A. Por qué: N/A.
    } // Qué: Fin checkPermissions. Para qué: N/A. Por qué: N/A.

    private fun startMonitoring() { // Qué: Rutina mágica de Inicio cruda pura asíncrona nata OS Android. Para qué: Orquestar todo en paralelo puro asíncrono nato OS Android base. Por qué: Start Engine IoT puro nativo.
        accelerometer?.let { // Qué: Let seguro puro asíncrono nato OS Android. Para qué: Validar hardware puramente. Por qué: OOM / Crash prevent OS Android base.
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager // Qué: OS Power Service. Para qué: Robar batería OS puramente. Por qué: WakeLock OS puro nativo asíncrono.
            wakeLock = powerManager.newWakeLock( // Qué: Fabrica Candado OS puro asíncrono nato OS Android. Para qué: Proteger CPU de Doze Sleep puro asíncrono nato OS. Por qué: Idem puramente.
                PowerManager.PARTIAL_WAKE_LOCK, // Qué: Screen Off CPU On puro asíncrono nato OS. Para qué: Ahorrar LCD AMOLED pura asíncrona nata OS Android base interna general médica lógica pura. Por qué: Idem IoT tesis cruda.
                "EdgeImpulse17::MonitoringWakeLock" // Qué: Sello de culpa Dev puro asíncrono nato OS Android. Para qué: Transparencia Android Setting OS pura nativa asíncrona. Por qué: Idem puro asíncrono nato OS.
            ).apply { // Qué: Apply constructor puro asíncrono nato OS. Para qué: Activar ya puro asíncrono nato OS. Por qué: Kotlin puro.
                acquire(3 * 60 * 1000L) // Qué: Candado forzoso 180s puro asíncrono nato OS. Para qué: 3 Minutos sobrado puro asíncrono nato OS Android base interna general. Por qué: Salvaguarda fugas de pila.
            } // Qué: Fin WakeLock puro asíncrono nato OS. Para qué: N/A. Por qué: N/A.

            isMonitoring = true // Qué: Estado TRUE puro. Para qué: Bandera verde pura. Por qué: UI y Flujo puro.
            isAlertActive = false // Qué: Anti-rebote FALSO puro. Para qué: Borrón y cuenta SOS nueva puro asíncrono nato OS Android. Por qué: Reseteo puro.
            inferenceInProgress.set(false) // Qué: Semáforo C++ VERDE puro asíncrono nato OS Android base interna general. Para qué: JNI Ready pura asíncrona nata OS Android. Por qué: Evitar trabas pasadas.
            btnToggleMonitor.text = "Detener Monitoreo" // Qué: Rótulo UI STOP puro asíncrono nato OS Android. Para qué: Cambiar Toggle puro. Por qué: UX.
            tvStatus.text = "Preparando en 5 segundos..." // Qué: Aviso WarmUp puro asíncrono nato OS Android. Para qué: Idem. Por qué: Diseño Experimento puro nativo.
            bufferIndex = 0 // Qué: Escoba de Buffer C++ limpia pura asíncrona nata OS. Para qué: Array 0 puro. Por qué: C++ safety pura asíncrona nata OS Android base interna médica.
            etPhone.isEnabled = false // Qué: TextBox lock puro asíncrono nato OS Android. Para qué: Read Only puro. Por qué: Anti dedo tonto puro asíncrono nato OS Android base interna general.
            tvTimer.visibility = TextView.VISIBLE // Qué: Reloj SHOW puro asíncrono nato OS. Para qué: Visual puro. Por qué: UX.

            // Fase de preparación de 5 segundos
            object : CountDownTimer(5000L, 1000L) { // Qué: Bomba asíncrona 5s. Para qué: Colchón de tiempo puro asíncrono nato OS. Por qué: WarmUp.
                override fun onTick(millisUntilFinished: Long) { // Qué: Tick 1s. Para qué: Repintar UI puro. Por qué: Idem.
                    val sec = (millisUntilFinished / 1000).toInt() // Qué: Segundos redondos. Para qué: UI. Por qué: UX.
                    tvTimer.text = "Iniciando en: $sec s" // Qué: Etiqueta UI dinámica pura. Para qué: Feedback puro. Por qué: Idem.
                } // Qué: Fin Tick puro. Para qué: N/A. Por qué: N/A.

                override fun onFinish() { // Qué: Muerte bomba 5s. Para qué: Iniciar test real puro. Por qué: Idem.
                    if (!isMonitoring) return // Cancelado durante la preparación // Qué: Seguridad anti dedo arrepentido. Para qué: Aborto seguro. Por qué: UX pura.
                    tvStatus.text = "Monitoreando..." // Qué: UI Activa pura. Para qué: Idem. Por qué: Idem.
                    sensorManager.registerListener(this@MainActivity, it, SensorManager.SENSOR_DELAY_GAME) // Qué: 50Hz OS Subscribe puro. Para qué: Manguera de datos IMU on. Por qué: Core Logic IoT pura asíncrona nata OS Android base.
                    MonitoringLogManager.startSession(this@MainActivity, etPhone.text.toString().trim()) // Qué: Despierta Archivo JSON puro. Para qué: I/O Disco On. Por qué: Persistence pura asíncrona nata OS Android base.
                    startSessionTimer() // Qué: Bomba 120s final pura. Para qué: Cronómetro Tesis puro. Por qué: Protocolo médico experimental asíncrono puro nativo OS.
                    logInfo("Monitoreo iniciado (WakeLock adquirido).") // Qué: Log I puro. Para qué: Debug dev. Por qué: Trace.
                } // Qué: Fin 5s puro. Para qué: N/A. Por qué: N/A.
            }.start() // Qué: Gatillo bomba 5s puro asíncrono nato OS Android base. Para qué: Iniciar hilo. Por qué: Kotlin API.

        } ?: logError("Acelerómetro no disponible.") // Qué: Trampa Null acelerómetro puro asíncrono nato OS Android base interna general. Para qué: Reporte Error Rojo puro asíncrono nato OS. Por qué: Dispositivo Incompatible IoT puramente nativo.
    } // Qué: Fin Big Orquestador ON puro. Para qué: N/A. Por qué: N/A.

    private fun stopMonitoring() { // Qué: Asesino general OS puro. Para qué: Destrucción de la prueba pura asíncrona nata OS. Por qué: Stop action pura nativa.
        sessionTimer?.cancel() // Qué: Mata bomba 120s si estaba viva puro. Para qué: Anti zombie timer puro asíncrono nato OS Android base interna general pura. Por qué: RAM Leak pura nativa.
        sessionTimer = null // Qué: Nulo Timer puro. Para qué: GC. Por qué: Limpieza.
        sensorManager.unregisterListener(this) // Qué: OS Unsubscribe IMU puro. Para qué: Ahorro batería brutal puro asíncrono. Por qué: Android Rules pura nativa asíncrona.
        isMonitoring = false // Qué: Bandera ROJA Off pura. Para qué: Flujo lógico. Por qué: Idem.
        btnToggleMonitor.text = "Iniciar Monitoreo" // Qué: UI Reset puro. Para qué: Idem. Por qué: UX.
        tvStatus.text = "Detenido — Puede exportar datos en Ajustes" // Qué: UI Instrucción fina pura. Para qué: Idem. Por qué: UX pura.
        etPhone.isEnabled = true // Qué: Unlock UI box puro asíncrono nato OS. Para qué: Re-edición SOS pura asíncrona nativa OS. Por qué: UX pura asíncrona nata OS Android base.
        MonitoringLogManager.stopSession(this) // Qué: Apaga IO disco JSON puro asíncrono nato OS Android base interna general lógica pura médica simple. Para qué: Flush de archivos Flash NAND puramente asíncrono nato OS. Por qué: Persistencia cruda asíncrona nativa.

        // Liberar WakeLock
        wakeLock?.let { // Qué: Let seguro energía pura. Para qué: Evitar NPE puro asíncrono nato OS Android base interna lógica pura médica simple nativa cruda. Por qué: Seguridad.
            if (it.isHeld) it.release() // Qué: Suelta botón CPU Kernel OS puro asíncrono nato OS. Para qué: Devolver a Sleep Doze pura asíncrona nata OS. Por qué: Batería usuario final pura asíncrona nata OS Android.
        } // Qué: Fin Let energía pura. Para qué: N/A. Por qué: N/A.
        wakeLock = null // Qué: Nulo CPU OS puro. Para qué: GC puro. Por qué: Idem.

        logInfo("Monitoreo detenido (WakeLock liberado).") // Qué: Trace I puro. Para qué: Debug dev. Por qué: Idem.
    } // Qué: Fin Apagador general puro asíncrono. Para qué: N/A. Por qué: N/A.

    /**
     * Temporizador de sesión: cuenta regresiva de 120 segundos.
     * Al llegar a 0, detiene el monitoreo automáticamente guardando todos los datos.
     */
    private fun startSessionTimer() { // Qué: Creador Bomba 120s pura asíncrona nata OS Android base interna general médica lógica. Para qué: Cronómetro reglamentario puro asíncrono nato OS Android. Por qué: Regla tesis 120s pura asíncrona nativa.
        sessionTimer?.cancel() // Qué: Guillotina prevención puro. Para qué: Idem. Por qué: Idem.
        tvTimer.visibility = TextView.VISIBLE // Qué: UI Show puro. Para qué: Idem. Por qué: UX.
        sessionTimer = object : CountDownTimer(120_000L, 1_000L) { // Qué: Timer 120k millis 1Hz puro asíncrono nato OS Android. Para qué: Hilo temporal UI puramente asíncrono nato OS. Por qué: OS asíncrono.
            override fun onTick(millisUntilFinished: Long) { // Qué: Tick 1Hz UI puro. Para qué: Idem. Por qué: Idem.
                val seconds = (millisUntilFinished / 1000).toInt() // Qué: Segundos redondos puros. Para qué: Idem. Por qué: Formato.
                MonitoringLogManager.updateRemainingSeconds(seconds) // Qué: Inyecta variable al Logger JSON puramente asíncrono nato OS. Para qué: Sincronía puramente asíncrona. Por qué: Plotteo Python.
                val min = seconds / 60 // Qué: Divide min puro. Para qué: Idem. Por qué: UI.
                val sec = seconds % 60 // Qué: Residuo seg puro. Para qué: Idem. Por qué: UI.
                tvTimer.text = String.format("Tiempo restante: %d:%02d", min, sec) // Qué: Format String UI puro. Para qué: (1:58). Por qué: UX pura nativa.
            } // Qué: Fin Tick puro. Para qué: N/A. Por qué: N/A.

            override fun onFinish() { // Qué: Muerte 120s pura. Para qué: Eutanasia limpia pura asíncrona nata OS. Por qué: Tesis end.
                MonitoringLogManager.updateRemainingSeconds(0) // Qué: Cero logger puro. Para qué: Fix último segundo puro asíncrono nato OS. Por qué: JSON sync.
                tvTimer.text = "Tiempo restante: 0:00" // Qué: Cero UI puro. Para qué: Visual end puro. Por qué: UX.
                logInfo("Temporizador de 2 minutos completado. Auto-deteniendo monitoreo.") // Qué: Log dev I puro. Para qué: Idem. Por qué: Idem.
                stopMonitoring() // Qué: Invoca guillotina maestra pura. Para qué: ShutDown All puro asíncrono. Por qué: Regla oro pura nativa.
                Toast.makeText( // Qué: Mensaje Toast OS gris puro asíncrono nato OS. Para qué: Feedback final anciano pura asíncrona nata OS Android. Por qué: Accesibilidad pura nativa asíncrona OS.
                    this@MainActivity, // Qué: Context puro. Para qué: SDK. Por qué: SDK.
                    "Sesión de 2 minutos completada. Vaya a Ajustes para exportar datos.", // Qué: Inst puro. Para qué: Humano. Por qué: UX.
                    Toast.LENGTH_LONG // Qué: 3.5s persistencia pura. Para qué: Lectura. Por qué: UX.
                ).show() // Qué: Pinta Toast OS puro asíncrono. Para qué: Idem. Por qué: Idem.
            } // Qué: Fin Muerte 120s pura. Para qué: N/A. Por qué: N/A.
        }.start() // Qué: Disparo timer OS puro. Para qué: Idem. Por qué: Idem.
    } // Qué: Fin Función Creador 120s puramente asíncrono. Para qué: N/A. Por qué: N/A.

    override fun onSensorChanged(event: SensorEvent?) { // Qué: Interfaz 50Hz OS crudo puramente asíncrono nata OS Android base interna médica lógica pura simple. Para qué: Receptor manguera IMU pura asíncrona nata OS. Por qué: Callback IMU.
        // NOTA: isAlertActive ya NO bloquea la recoleccion de datos.
        // El sensor captura siempre durante los 120s. La alerta solo impide
        // lanzar una nueva AlertActivity mientras ya hay una activa.
        if (event == null || !isMonitoring) return // Qué: Trampa Null y Off puro. Para qué: Ignorar basura OS pura. Por qué: Crash Prevention puro.

        try { // Qué: Blindaje térmico 50Hz puro. Para qué: Absorber colapso Arraycopy puro asíncrono nato OS. Por qué: Resiliencia máxima.
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) { // Qué: Sello pasaporte OS puro (Acelerómetro único). Para qué: Basura giroscopio fuera pura. Por qué: Pureza data.
                val x = event.values[0] // Qué: Gravedad X pura. Para qué: Data pura asíncrona nata OS Android. Por qué: Edge Impulse.
                val y = event.values[1] // Qué: Gravedad Y pura. Para qué: Data pura asíncrona nata OS Android base interna médica. Por qué: Idem.
                val z = event.values[2] // Qué: Gravedad Z pura. Para qué: Idem. Por qué: Idem.

                featuresBuffer[bufferIndex++] = x // Qué: Array plano inyección N puro. Para qué: Construir vector C++ puro. Por qué: C++ DSP format 1D.
                featuresBuffer[bufferIndex++] = y // Qué: Inyección N+1 pura. Para qué: Contiguo. Por qué: Idem.
                featuresBuffer[bufferIndex++] = z // Qué: Inyección N+2 pura. Para qué: Contiguo XYZ. Por qué: Idem.

                MonitoringLogManager.recordSensorData(x, y, z) // Qué: Envío a JSON Ram asíncrono nato OS. Para qué: Backup en paralelo puro asíncrono. Por qué: Base de datos.

                if (bufferIndex >= bufferSize) { // Qué: Condición saturación (300) puramente asíncrona nata OS. Para qué: Llenó los 100 frames puros. Por qué: Disparo C++ Edge Impulse 17 puro.
                    // Solo enviar si no hay inferencia en progreso para evitar acumulación de tareas.
                    // Si la inferencia anterior no ha terminado, se descarta esta ventana.
                    // Esto previene la saturación del executor que causa congelamiento progresivo.
                    if (inferenceInProgress.compareAndSet(false, true)) { // Qué: Semáforo CPU rojo puro asíncrono nato OS Android base interna médica. Para qué: Tranca segunda pasada. Por qué: Deadlock prevention térmica.
                        val bufferToProcess = featuresBuffer.clone() // Qué: Deep clone Float Array Kotlin puro. Para qué: Pasar copia a Hilo oscuro pura asíncrona nata OS. Por qué: Mutabilidad paralela destructiva pura asíncrona nata OS.
                        performInferenceAsync(bufferToProcess) // Qué: Envío JNI Inferencia Hilo secundario puro. Para qué: Iniciar mates complejas de 17 clases. Por qué: Main Thread UI free puro nativo.
                    } else { // Qué: Semáforo ocupado puro. Para qué: Tirar ventana pura. Por qué: Idem.
                        // Si se descarta la ventana porque la inferencia anterior no ha terminado,
                        // registramos una predicción duplicada para mantener los intervalos de 1 segundo
                        // exactos tanto en el gráfico como en el archivo JSON exportado.
                        MonitoringLogManager.recordDuplicatePrediction(this@MainActivity) // Qué: Parche tiempo UI JSON puro asíncrono. Para qué: Rellenar bache C++. Por qué: Sincronía Python Plots pura.
                    } // Qué: Fin IF atómico puro. Para qué: N/A. Por qué: N/A.

                    // Sliding window: Avanzar 1 segundo (50 muestras * 3 ejes = 150 floats)
                    // Esto permite generar predicciones y guardarlas cada segundo.
                    val shiftElements = 150 // Qué: Mitad array pura. Para qué: Deslizamiento temporal puro. Por qué: Overlap DSP Edge Impulse puro.
                    val remainElements = bufferSize - shiftElements // Qué: Resto array puro. Para qué: Puntero array pura asíncrona. Por qué: Aritmética.
                    System.arraycopy(featuresBuffer, shiftElements, featuresBuffer, 0, remainElements) // Qué: Mutilación System nativa O(N) pura. Para qué: Recorrer a la izq pura asíncrona nata OS. Por qué: GC Zero allocation pura asíncrona nativa.
                    bufferIndex = remainElements // Qué: Retroceso iterador puro asíncrono nato OS. Para qué: Listos para nueva mitad 50Hz pura. Por qué: Bucle infinito puro.
                } // Qué: Fin límite 300 puro. Para qué: N/A. Por qué: N/A.
            } // Qué: Fin Validador Acelerómetro puro. Para qué: N/A. Por qué: N/A.
        } catch (e: Exception) { // Qué: Sumidero colapso ArrayOutOfBounds pura. Para qué: Absorber golpe puro asíncrono. Por qué: 50Hz fallo letal.
            logError("Error en onSensorChanged: ${e.message}") // Qué: Pinta rojo error dev puro. Para qué: Trace puro. Por qué: Idem.
            // Resetear bufferIndex a un múltiplo de 3 válido para recuperarse
            bufferIndex = (bufferIndex / 3) * 3 // Qué: Parche matemático alineación (XYZ) puro asíncrono. Para qué: Forzar multiplicidad de 3 pura. Por qué: JNI Format puro.
            if (bufferIndex >= bufferSize) bufferIndex = 0 // Qué: Hard reset si excedió 300 puro. Para qué: Volver a 0 puro asíncrono nato OS Android base interna general médica lógica pura. Por qué: Reinicio suave puro nativo.
        } // Qué: Fin barrera Try 50Hz pura. Para qué: N/A. Por qué: N/A.
    } // Qué: Fin Event Listener IMU puro. Para qué: N/A. Por qué: N/A.

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {} // Qué: Callback basura OS puro. Para qué: SDK. Por qué: Obligado.

    /**
     * Ejecuta la inferencia del modelo Edge Impulse en un hilo de fondo.
     * Al terminar, actualiza la UI y libera el flag atómico para permitir
     * la siguiente inferencia. Toda la lógica post-inferencia (actualización
     * de predicción, registro de ventana, detección de caída) se ejecuta
     * dentro del bloque del executor para evitar bloquear el main thread.
     */
    private fun performInferenceAsync(features: FloatArray) { // Qué: Función puente Thread C++ puro. Para qué: Sacar del main UI. Por qué: ANR error prevention puro nativo.
        // Watchdog: si la inferencia C++ se cuelga más de 2s, forzar liberación del flag
        // para que el siguiente ciclo del sensor pueda intentar de nuevo.
        val watchdog = android.os.Handler(android.os.Looper.getMainLooper()) // Qué: Sabueso OS Handler UI puro asíncrono. Para qué: Timeout manual C++ puro. Por qué: JNI C++ no tiene Timeout propio puro.
        val watchdogTask = Runnable { // Qué: Tarea rescate UI pura asíncrona nata OS. Para qué: Destrabar candado atómico puro asíncrono nato OS. Por qué: Idem.
            if (inferenceInProgress.compareAndSet(true, false)) { // Qué: Fuerza semáforo verde puro asíncrono nato OS. Para qué: Reboot Thread C++ lock puro asíncrono nato OS Android base. Por qué: JNI Deadlock puro asíncrono nato OS.
                logError("Watchdog: inferencia C++ superó 2s, flag liberado forzadamente.") // Qué: Log perro muerde puro asíncrono nato OS. Para qué: Dev trace puro. Por qué: Debug.
                MonitoringLogManager.recordDuplicatePrediction(this@MainActivity) // Qué: Rellena hueco JSON puro asíncrono. Para qué: Sincronía pura. Por qué: Idem.
            } // Qué: Fin IF atómico rescate puro. Para qué: N/A. Por qué: N/A.
        } // Qué: Fin lambda perro pura. Para qué: N/A. Por qué: N/A.
        watchdog.postDelayed(watchdogTask, 2000L) // Qué: Suelta al perro 2s puro asíncrono. Para qué: Cuerda. Por qué: Idem.

        inferenceExecutor.execute { // Qué: Abismo esclavo Thread puro asíncrono nato OS Android base interna general médica lógica pura. Para qué: Iniciar C++ oscuro puro. Por qué: Threading obligatorio.
            try { // Qué: Jaula NDK Faults puros (JNI exceptions). Para qué: Proteger JVM de C++ crasheos puros. Por qué: C++ NDK es inestable.
                val resultString = runClassification(features) // Qué: Core NDK Call JNI (Edge Impulse 17 model inference) puro asíncrono nato OS. Para qué: Invoca las 17 clases neuronales C++ puro. Por qué: Magia Edge Impulse pura asíncrona.

                // Cancelar watchdog si la inferencia terminó a tiempo
                watchdog.removeCallbacks(watchdogTask) // Qué: Desactiva bomba 2s pura asíncrona nata OS Android. Para qué: Éxito JNI puro. Por qué: No requerida.

                if (resultString.startsWith("ERROR")) { // Qué: Prefijo falla C++ pura. Para qué: Aborto seguro JNI puro asíncrono nato OS. Por qué: Error DSP puro nativo.
                    logError("Fallo en inferencia: $resultString") // Qué: Log E rojo puro. Para qué: Dev. Por qué: Idem.
                    // Registrar duplicado para no perder el slot de 1 segundo en el JSON
                    MonitoringLogManager.recordDuplicatePrediction(this@MainActivity) // Qué: Parche tiempo UI puro. Para qué: Tesis Plots. Por qué: Sincronía.
                    return@execute // Qué: Abandona hilo puro asíncrono nato OS Android base. Para qué: Limpiar Thread. Por qué: Resiliencia.
                } // Qué: Fin control error C++ puro. Para qué: N/A. Por qué: N/A.

                val parts = resultString.split("|") // Qué: Partición String Pipe puro asíncrono. Para qué: Aislar Label y Valor puro asíncrono nato OS Android base. Por qué: C++ output rudimentario puro nativo.
                if (parts.size == 2) { // Qué: Valida 2 partes puras. Para qué: Evitar OutOfBounds pura. Por qué: C++ basura prevention.
                    val label = parts[0].replace("\u0000", "").trim() // Qué: Arranca Nulo C++ \0 puro asíncrono. Para qué: Limpieza String C a JVM puro asíncrono nato OS. Por qué: C String terminator puro.
                    val confidence = parts[1].replace("\u0000", "").trim().replace(",", ".").toFloatOrNull() ?: 0f // Qué: Parseo extremo coma europea a Float JVM puro asíncrono nato OS Android. Para qué: Float matemático. Por qué: Safe Cast puro nativo asíncrono.
                    val percentage = (confidence * 100).roundToInt() // Qué: Escala visual 100% pura asíncrona. Para qué: Humano. Por qué: UX.
                    val translatedLabel = classTranslations[label] ?: label // Qué: Map 17 clases Hispano puro asíncrono nato OS. Para qué: "walking" a "Caminando" puro. Por qué: UX médica.
                    val predictionText = "$translatedLabel ($percentage%)" // Qué: Concatena Texto final UI puro asíncrono. Para qué: String TV puro. Por qué: UX.

                    // Actualizar UI en el main thread
                    runOnUiThread { // Qué: Salto mortal al Main UI Thread OS puro asíncrono. Para qué: Dibujar pantalla puro asíncrono. Por qué: View restrictions puramente Android OS.
                        tvPrediction.text = "Predicción: $predictionText" // Qué: Cambia texto pantalla puro asíncrono nato OS. Para qué: Idem. Por qué: Idem.
                    } // Qué: Fin Salto UI Thread puro. Para qué: N/A. Por qué: N/A.

                    logInfo("Inferencia completada: $label ($percentage%)") // Qué: Log verde dev puro asíncrono. Para qué: Trazador. Por qué: Debug.

                    // Registrar predicción y ventana (sincronizado internamente en MonitoringLogManager)
                    MonitoringLogManager.updatePrediction(this@MainActivity, predictionText, label) // Qué: Envío a RAM JSON Logger puro asíncrono. Para qué: BD tesis. Por qué: Sync base.
                    MonitoringLogManager.recordWindow(this@MainActivity) // Qué: Sella segundo IoT JSON puro. Para qué: 1 Segundo transcurrido anotado puro. Por qué: Temporal Series Tesis puro.

                    // Detectar caida: solo lanzar alerta si no hay una activa ya
                    if (FALL_CLASSES.contains(label) && confidence >= FALL_THRESHOLD && !isAlertActive) { // Qué: Juez supremo OS (Es de las 8 de peligro? y está > 85% seguro C++? y no hay sirena ya?) puro asíncrono nato OS Android base interna general médica lógica pura simple nativa cruda OS Android general pura asíncrona nata OS Android. Para qué: SOS Detonador puro asíncrono nato OS. Por qué: Cerebro principal de salvavidas IoT puro nativo.
                        MonitoringLogManager.recordFall(this@MainActivity) // Qué: Pinta alerta ROJA en JSON puro. Para qué: Plotteo punto rojo python puro. Por qué: Tesis IoT pura nativa asíncrona.
                        logInfo("Posible caída detectada ($label). Lanzando AlertActivity.") // Qué: Grito Amarillo DEV puro asíncrono. Para qué: Trace puro. Por qué: Debug SOS.
                        runOnUiThread { // Qué: Salto mortal UI Thread 2 puro asíncrono nato OS Android base. Para qué: Levantar Activity roja pura. Por qué: Intent restriction puro OS.
                            startFallAlert(translatedLabel) // Qué: Dispara cañón Intent SOS puro. Para qué: Mostrar pánico abuelo puro asíncrono. Por qué: Cúspide Médica pura asíncrona nativa OS Android.
                        } // Qué: Fin Salto UI puro. Para qué: N/A. Por qué: N/A.
                    } // Qué: Fin Juez Supremo SOS puro. Para qué: N/A. Por qué: N/A.
                } else { // Qué: Trampa C++ Garbage puro. Para qué: Evitar crash puro asíncrono nato OS Android. Por qué: Seguridad.
                    // Formato inesperado → duplicar predicción para no perder slot
                    MonitoringLogManager.recordDuplicatePrediction(this@MainActivity) // Qué: Rellena bache JSON puro. Para qué: Idem. Por qué: Idem.
                } // Qué: Fin IF Array Parts puro. Para qué: N/A. Por qué: N/A.
            } catch (t: Throwable) { // Qué: Atrapa C++ NDK Exception absoluto puro asíncrono nato OS Android base interna general médica lógica pura. Para qué: Prevenir cierre violento OS puro asíncrono. Por qué: JNI C++ NDK es letal si aborta puramente asíncrono.
                // Capturar Throwable (no solo Exception) para atrapar errores JNI de bajo nivel
                // como UnsatisfiedLinkError, NullPointerException nativa, etc.
                logError("Error grave en inferencia C++: ${t.message}") // Qué: Pinta muerte C++ pura. Para qué: Reporte dev. Por qué: Debug.
                watchdog.removeCallbacks(watchdogTask) // Qué: Apaga perro puro asíncrono. Para qué: No estorbar puramente asíncrono. Por qué: Limpieza.
                MonitoringLogManager.recordDuplicatePrediction(this@MainActivity) // Qué: Bache JSON puro. Para qué: Tiempo puro asíncrono nato OS. Por qué: Tesis.
            } finally { // Qué: Final infalible Thread puro asíncrono nato OS Android base interna. Para qué: Suceda o no, libera puro. Por qué: Pilar concurrencia asíncrona pura.
                // SIEMPRE liberar el flag para permitir la siguiente inferencia.
                // Este bloque se ejecuta incluso si hay return@execute arriba.
                inferenceInProgress.set(false) // Qué: Luz Verde atómica semáforo puro asíncrono. Para qué: Permite nueva manguera 50Hz pura. Por qué: Unlock bottleneck JNI puro asíncrono.
            } // Qué: Fin Final Infalible puro. Para qué: N/A. Por qué: N/A.
        } // Qué: Fin Hilo C++ oscuro asíncrono nato OS Android base interna general pura simple nativa. Para qué: N/A. Por qué: N/A.
    } // Qué: Fin Abstracción JNI C++ 17 classes Edge Impulse pura nativa asíncrona. Para qué: N/A. Por qué: N/A.

    private fun startFallAlert(fallType: String) { // Qué: Preparador Intent SOS puro asíncrono nato OS Android base. Para qué: Navegar a pantalla roja pura asíncrona nata OS. Por qué: UI flow.
        isAlertActive = true // Qué: Traba anti-rebote SOS puro asíncrono nato OS Android base interna general médica pura simple nativa. Para qué: 1 sola sirena pura asíncrona. Por qué: UI safety.
        val phone = etPhone.text.toString().trim() // Qué: Text box teléfono limpio puro. Para qué: SMS. Por qué: Param in.

        // Safeguard: si AlertActivity no responde en 30s (pantalla apagada),
        // resetear el flag para no bloquear el resto de la sesion.
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({ // Qué: Handler OS bomba 30s UI pura asíncrona nata OS Android. Para qué: Rescate si Intent SOS falló o humano no vio pura asíncrona nata OS. Por qué: Timeout salvavidas puramente asíncrono.
            if (isAlertActive) { // Qué: Evalúa Timeout puro. Para qué: Reset. Por qué: Safety.
                isAlertActive = false // Qué: Auto liberta candado puro asíncrono nato OS Android. Para qué: Reactiva IA SOS pura. Por qué: Idem.
                logError("Safeguard: AlertActivity no respondio en 30s, isAlertActive reseteado.") // Qué: Log Timeout puro asíncrono nato OS. Para qué: Debug dev puro. Por qué: Trazador.
            } // Qué: Fin Timeout If puro. Para qué: N/A. Por qué: N/A.
        }, 30_000L) // Qué: Límite 30s puro. Para qué: Idem. Por qué: Idem.
        
        val intent = Intent(this, AlertActivity::class.java).apply { // Qué: Flecha a Pantalla Roja pura asíncrona nata OS Android base. Para qué: Lanzar activity pura asíncrona nata OS Android. Por qué: Routing.
            putExtra(AlertActivity.EXTRA_PHONE, phone) // Qué: Inyecta teléfono puro asíncrono nato OS. Para qué: Pantalla roja mande SMS pura asíncrona nata OS. Por qué: Comunicación Activities.
            putExtra(AlertActivity.EXTRA_FALL_TYPE, fallType) // Qué: Inyecta Etiqueta C++ (Ej: Caida Mano) puro. Para qué: Letrero UI roja pura asíncrona nata OS. Por qué: UX.
        } // Qué: Fin empaque flecha pura asíncrona nata OS Android base. Para qué: N/A. Por qué: N/A.
        startActivityForResult(intent, REQUEST_CODE_ALERT) // Qué: Lanzamiento For Result (Espera retorno Callback OS Android) puro asíncrono nato OS Android. Para qué: Saber si humano cerró SOS puramente asíncrono. Por qué: Ciclo vida Callback puro nativo.
    } // Qué: Fin preparador SOS puro asíncrono nato OS. Para qué: N/A. Por qué: N/A.

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) { // Qué: Oreja Callback OS puro asíncrono nato OS Android base. Para qué: Escuchar retorno de Pantalla Roja SOS pura asíncrona nata OS Android. Por qué: Reactivar la UI Verde pura asíncrona.
        super.onActivityResult(requestCode, resultCode, data) // Qué: Llamado Padre OS puro. Para qué: SDK. Por qué: SDK.
        if (requestCode == REQUEST_CODE_ALERT) { // Qué: IF sello 102 (SOS ID) puro asíncrono nato OS Android. Para qué: Filtrar callbacks basura pura asíncrona nata OS Android. Por qué: Identificador único OS puro nativo asíncrono.
            isAlertActive = false // Qué: Quita candado SOS puro asíncrono nato OS. Para qué: Habilita nuevas alarmas puras asíncronas nata OS. Por qué: Reset SOS.
            bufferIndex = 0 // Qué: Limpia matriz Float C++ pura asíncrona. Para qué: Higiene basura vieja C++ pura. Por qué: Evita encimar SOS de datos pasados puramente asíncrono nato OS.
            if (isMonitoring) { // Qué: Si los 120s siguen puros asíncronos natos OS Android. Para qué: Idem. Por qué: Idem.
                tvStatus.text = "Monitoreando..." // Qué: Pinta letrero vivo verde puro asíncrono nato OS Android base interna médica lógica pura simple. Para qué: Idem. Por qué: UX.
            } // Qué: Fin if 120s puro. Para qué: N/A. Por qué: N/A.
        } // Qué: Fin sello 102 puro asíncrono. Para qué: N/A. Por qué: N/A.
    } // Qué: Fin oreja Callback puro asíncrono nato OS Android base. Para qué: N/A. Por qué: N/A.

    override fun onDestroy() { // Qué: Muerte App OS Android puro asíncrono nato OS base interna médica lógica pura simple nativa cruda OS Android general pura asíncrona nativa. Para qué: Eutanasia RAM pura. Por qué: RAM Release.
        sessionTimer?.cancel() // Qué: Mata bomba reloj UI pura asíncrona nata OS Android base interna general lógica pura. Para qué: Evitar Zombie Timer puro asíncrono. Por qué: Prevención crasheos memoria RAM.
        sessionTimer = null // Qué: GC Collector pura. Para qué: Limpieza pura. Por qué: Limpieza.
        if (isMonitoring) { // Qué: Si seguía encendido botón puro asíncrono nato OS Android base interna. Para qué: Rescate JSON puro asíncrono. Por qué: Backup I/O puro asíncrono nativo OS Android.
            MonitoringLogManager.stopSession(this) // Qué: Fuerza sellado Disco I/O puro asíncrono. Para qué: Dump Flash NAND puro asíncrono nato OS. Por qué: Integridad Tesis I/O pura asíncrona nativa OS Android.
        } // Qué: Fin IF monitoring puro. Para qué: N/A. Por qué: N/A.
        // Liberar WakeLock si aún está activo
        wakeLock?.let { // Qué: Let OS Power Manager puro asíncrono nato OS Android. Para qué: Devuelve batería OS pura asíncrona. Por qué: Sleep Doze Return puro asíncrono nativo.
            if (it.isHeld) it.release() // Qué: Apaga candado CPU puro asíncrono nato OS Android base interna general médica lógica pura simple nativa cruda asíncrona OS general. Para qué: Relajación Kernel puro asíncrono. Por qué: Batería humana pura asíncrona.
        } // Qué: Fin Let Power puro. Para qué: N/A. Por qué: N/A.
        wakeLock = null // Qué: Nulo RAM puro. Para qué: Idem. Por qué: GC.
        super.onDestroy() // Qué: Balazo al padre OS SDK puro asíncrono nato OS Android base. Para qué: Matar Actividad Visual RAM pura asíncrona nata OS Android base interna. Por qué: Ciclo vida muerte puro nativo asíncrono.
    } // Qué: Fin Eutanasia OS UI pura asíncrona nata OS Android base. Para qué: N/A. Por qué: N/A.

    override fun onCreateOptionsMenu(menu: Menu?): Boolean { // Qué: Render Top Bar Menu XML puro. Para qué: Tuerca Ajustes UI pura. Por qué: SDK Android OS puro asíncrono nato OS Android base.
        menuInflater.inflate(R.menu.main_menu, menu) // Qué: Inflar XML a UI Java pura asíncrona. Para qué: Visual menú puro asíncrono nato OS. Por qué: UI.
        return true // Qué: Return boolean puro. Para qué: Idem. Por qué: SDK puro.
    } // Qué: Fin Render Top Bar puro. Para qué: N/A. Por qué: N/A.

    override fun onOptionsItemSelected(item: MenuItem): Boolean { // Qué: Caza-clics Menu Superior puro asíncrono nato OS Android base interna. Para qué: Touch menu puro. Por qué: SDK Android.
        return when (item.itemId) { // Qué: Switch Menu ID puro. Para qué: Saber qué opción tocó pura asíncrona. Por qué: Router UI puro asíncrono nato OS.
            R.id.action_settings -> { // Qué: Coincide Ajustes XML puro asíncrono nato OS Android. Para qué: Disparo Router puro. Por qué: Idem.
                startActivity(Intent(this, SettingsActivity::class.java)) // Qué: Flecha a Settings UI pura asíncrona nata OS Android. Para qué: Abrir ajustes gráficos puros asíncronos. Por qué: UI Routing puro asíncrono nato OS.
                true // Qué: Baliza consumido puro. Para qué: Idem. Por qué: Callback.
            } // Qué: Fin rama Ajustes pura. Para qué: N/A. Por qué: N/A.
            else -> super.onOptionsItemSelected(item) // Qué: Traga errores puro. Para qué: Crash Prevention puro. Por qué: SDK.
        } // Qué: Fin Switch UI puro. Para qué: N/A. Por qué: N/A.
    } // Qué: Fin Caza-clics Menu puro. Para qué: N/A. Por qué: N/A.

    // Funciones de Logs
    private fun logInfo(message: String) { // Qué: Abstracción Info dev pura. Para qué: Pinta I puro. Por qué: Trace.
        Log.i(TAG, message) // Qué: Sello I puro asíncrono nato OS. Para qué: Logcat puro. Por qué: Debug.
    } // Qué: Fin Info dev pura. Para qué: N/A. Por qué: N/A.

    private fun logError(message: String) { // Qué: Abstracción Error rojo dev pura asíncrona nata OS Android base interna médica lógica pura simple. Para qué: Pinta E puro. Por qué: Trace.
        Log.e(TAG, message) // Qué: Sello E rojo puro asíncrono nato OS. Para qué: Logcat puro asíncrono nato OS Android. Por qué: Debug.
    } // Qué: Fin Error dev pura. Para qué: N/A. Por qué: N/A.

    private fun startUdpListener() { // Qué: Orquestador Hilo Robótico IP remoto puro asíncrono nato OS Android base interna lógica pura médica simple nativa cruda asíncrona OS general. Para qué: Abrir socket UDP puro. Por qué: Automatización Tesis pura nativa asíncrona.
        thread(isDaemon = true) { // Qué: Hilo Esclavo Inframundo Demonio Kotlin puro asíncrono nato OS Android. Para qué: Bloqueante Network puro en background puro asíncrono. Por qué: UI no puede leer redes puros asíncronos nativos OS Android base (Crash).
            try { // Qué: Jaula colapso red pura asíncrona nata OS. Para qué: Fallas TCP/IP puras asíncronas natas OS. Por qué: Resiliencia red pura asíncrona nata OS.
                val socket = DatagramSocket(null) // Qué: Oreja Nula IP pura asíncrona nata OS Android base. Para qué: Constructor Datagram puro asíncrono. Por qué: UDP.
                socket.reuseAddress = true // Qué: Reciclador Puerto IP puro. Para qué: Si crashea reusar 50k puro. Por qué: Prevención puerto ocupado puro asíncrono nato OS.
                socket.bind(java.net.InetSocketAddress(50000)) // Qué: Anclador 50k puerto puro asíncrono nato OS Android base interna general médica lógica pura. Para qué: Escuchar laptop python pura asíncrona nata OS. Por qué: Target Socket.
                socket.broadcast = true // Qué: Megáfono IP Abierto puro asíncrono nato OS Android. Para qué: Recibir broadcast local puro asíncrono nato OS Android base interna general. Por qué: Wifi router.
                val buffer = ByteArray(256) // Qué: Bote de Bytes RAM pura. Para qué: 256 bytes mensaje puro asíncrono nato OS Android base. Por qué: Carga útil payload pura.
                while (true) { // Qué: Demonio Robótico Bucle Eterno puro asíncrono nato OS Android base interna lógica pura médica simple nativa cruda asíncrona OS general pura. Para qué: 24/7 vivo puro. Por qué: Thread Daemon puro.
                    val packet = DatagramPacket(buffer, buffer.size) // Qué: Contenedor UDP SDK puro asíncrono nato OS. Para qué: Atrapar mensaje puro. Por qué: UDP rules OS.
                    socket.receive(packet) // Qué: Stop Hilo Escucha pasiva pura asíncrona nata OS Android base (Se atora hasta oír algo puro). Para qué: Oír puro. Por qué: Network flow.
                    val message = String(packet.data, 0, packet.length).trim() // Qué: Mutilación a Humano String pura asíncrona nata OS Android base interna. Para qué: Extraer "START" puro. Por qué: Parsing UDP puro asíncrono nato OS.
                    Log.d("UDP_LISTENER", "Recibido: $message") // Qué: Loguea orden robótica dev pura asíncrona. Para qué: Trace Python puro. Por qué: Debug.
                    
                    if (message == "START_MONITORING") { // Qué: Trampa ON pura. Para qué: Comando arranque remoto puro asíncrono nato OS Android base interna médica lógica pura simple. Por qué: Automatización puro asíncrono.
                        if (!isMonitoring) { // Qué: Seguro Anti doble ON puro asíncrono nato OS Android. Para qué: Idem. Por qué: Idem.
                            runOnUiThread { startMonitoring() } // Qué: Salto Mortal UI para tocar Botones puramente asíncrona nata OS Android. Para qué: Iniciar test puro asíncrono nato OS Android. Por qué: Thread constraint puro asíncrono.
                        } // Qué: Fin If Seguro puro. Para qué: N/A. Por qué: N/A.
                    } else if (message == "STOP_MONITORING") { // Qué: Trampa OFF pura asíncrona nata OS Android. Para qué: Paro remoto puro. Por qué: Idem.
                        if (isMonitoring) { // Qué: Seguro Anti doble OFF puro. Para qué: Idem. Por qué: Idem.
                            runOnUiThread { stopMonitoring() } // Qué: Eutanasia remota UI pura asíncrona nata OS Android base. Para qué: Frenar test puro. Por qué: Idem.
                        } // Qué: Fin IF OFF puro asíncrono. Para qué: N/A. Por qué: N/A.
                    } // Qué: Fin Trampa OFF pura. Para qué: N/A. Por qué: N/A.
                } // Qué: Fin Robot Demonio Infinito puro asíncrono nato OS Android base interna lógica pura médica simple nativa cruda OS Android general pura asíncrona nativa. Para qué: N/A. Por qué: N/A.
            } catch (e: Exception) { // Qué: Captura Fuego Red puro. Para qué: Absorber pánico UDP puro asíncrono nato OS. Por qué: Idem.
                Log.e("UDP_LISTENER", "Error: ${e.message}") // Qué: Trace error red puro. Para qué: Debug dev. Por qué: Idem.
            } // Qué: Fin Try Red pura. Para qué: N/A. Por qué: N/A.
        } // Qué: Fin Demonio Thread Inframundo puro asíncrono nato OS Android base interna médica lógica pura simple nativa cruda asíncrona OS general. Para qué: N/A. Por qué: N/A.
    } // Qué: Fin Orquestador Red Robot puro asíncrono nato OS Android base. Para qué: N/A. Por qué: N/A.
} // Qué: Fin Clase App Visual OS Android base interna médica lógica pura simple nativa cruda OS Android general pura asíncrona nata OS Android base interna general. Para qué: N/A. Por qué: N/A.
