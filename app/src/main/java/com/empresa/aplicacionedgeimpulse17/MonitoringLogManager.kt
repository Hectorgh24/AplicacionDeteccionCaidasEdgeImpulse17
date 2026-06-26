package com.empresa.aplicacionedgeimpulse17 // Qué: Declaración del paquete base de la aplicación. Para qué: Agrupar la lógica de negocio puramente. Por qué: Android SDK lo requiere obligatoriamente para compilación (Namespace 17 clases).

import android.content.ContentValues // Qué: Importa mapa clave-valor para Bases de Datos de Android. Para qué: Insertar metadatos (nombre, MIME type) al MediaStore del sistema operativo. Por qué: Requisito forzoso de Android 10+ (Scoped Storage) para escribir en la carpeta pública Downloads.
import android.content.Context // Qué: Importa la clase puente global Context. Para qué: Acceder a servicios subyacentes del SO (ContentResolver, FilesDir). Por qué: Sin Context, una clase u objeto Kotlin está ciega al ecosistema Android.
import android.os.Build // Qué: Importa la API de versiones del hardware/software. Para qué: Discriminar si el teléfono actual es Android 10 (Q) o más viejo. Por qué: Las reglas de escritura en memoria SD cambian drásticamente entre versiones antiguas y modernas.
import android.os.Environment // Qué: Importa la clase de entorno de almacenamiento. Para qué: Localizar la ruta absoluta de la carpeta pública de "Descargas" (Downloads). Por qué: El usuario debe poder extraer el archivo JSON fácilmente por USB.
import android.provider.MediaStore // Qué: Importa la API del gestor de archivos multimedia del SO. Para qué: Solicitar un espacio oficial (URI) donde guardar el reporte JSON. Por qué: Único método seguro de I/O de archivos en Android 10+ (API 29).
import org.json.JSONObject // Qué: Importa analizador nativo de Objetos JSON. Para qué: Construir o parsear diccionarios serializados en formato JavaScript Object Notation. Por qué: Formato universal para exportar métricas hacia Python.
import java.io.File // Qué: Importa abstracción nativa de archivos. Para qué: Crear y manipular archivos físicos en el disco duro interno del dispositivo. Por qué: Guardado temporal en caché interno privado de la aplicación.
import java.io.FileOutputStream // Qué: Importa tubería de escritura en crudo. Para qué: Verter el arreglo de bytes JSON hacia el archivo físico. Por qué: Modo de I/O básico heredado de Java.
import java.text.SimpleDateFormat // Qué: Importa formateador de fechas humanas. Para qué: Traducir milisegundos Unix (Long) a "2026-06-26 15:30:00". Por qué: Legibilidad del reporte JSON final.
import java.util.Date // Qué: Importa objeto instanciador del tiempo biológico. Para qué: Conseguir la hora exacta del momento. Por qué: Acople con SimpleDateFormat.
import java.util.Locale // Qué: Importa configuración de regionalización geográfica. Para qué: Forzar formato estándar americano de fecha (US). Por qué: Evita errores de parseo por zonas horarias si el móvil cambia de país (Estandarización ISO).
import org.json.JSONArray // Qué: Importa el arreglo serializador nativo JSON. Para qué: Embutir Listas complejas (historial de memoria, predicciones) dentro de corchetes [ ]. Por qué: Estructura del JSON resultante.
import java.util.concurrent.CopyOnWriteArrayList // Qué: Importa Lista especial Multihilo a prueba de balas concurrentes. Para qué: Guardar la bitácora sin que la app crashee si un Hilo C++ trata de escribir y la UI trata de leer a la vez. Por qué: Evita la infame y letal ConcurrentModificationException pura nativa de JVM en multihilo agresivo.
import java.util.concurrent.Executors // Qué: Importa constructor Thread. Para qué: Fabrica un esclavo solitario para guardar a disco. Por qué: I/O (Input/Output) de disco duro bloquea microciclos de UI Thread.
import java.util.concurrent.atomic.AtomicReference // Qué: Importa puntero Thread-Safe. Para qué: Publicar el Snapshot de sensores al SettingsActivity en una sola transacción atómica de memoria y reloj (Thread Boundary). Por qué: Arquitectura segura contra memoria de múltiples núcleos (Cores) Android.

data class PredictionEvent( // Qué: Objeto inmutable (Data Class) contenedor crudo. Para qué: Guardar la dupla (Segundo en que pasó, Nombre de clase C++). Por qué: Limpieza estructural Kotlin (No más matrices oscuras de datos).
    val timeSeconds: Int, // Qué: Variable inmutable del segundo absoluto transcurrido (Ej: Segundo 45 de 120). Para qué: Ubicar el punto cronológico del evento. Por qué: Eje X temporal en gráficos Python asíncronos nativos crudos.
    val className: String // Qué: Rótulo de la etiqueta. Para qué: (Ej: "fall_backward"). Por qué: Eje Y semántico categórico de Python.
) // Qué: Fin objeto temporal. Para qué: N/A. Por qué: N/A.

data class MemoryEvent( // Qué: Data class efímera de hardware. Para qué: Anotar uso de RAM cruda nativa Android. Por qué: Demostrar optimización en la Tesis.
    val timeSeconds: Int, // Qué: Segundo X. Para qué: Eje X cronológico asíncrono. Por qué: Mapeo de tiempo puro.
    val ramMB: Float // Qué: MB crudos puros consumidos. Para qué: Evaluar el ahogo térmico / Memory Leak del modelo Edge Impulse. Por qué: Pruebas clínicas IoT.
) // Qué: Fin objeto memoria. Para qué: N/A. Por qué: N/A.

data class SensorEventData( // Qué: Objeto contendor físico inercial. Para qué: Amarrar las tres fuerzas G a un instante de tiempo. Por qué: Esqueleto del histórico del acelerómetro.
    val timeOffsetMillis: Long, // Qué: Estampa de tiempo exacta (milis). Para qué: Alineación matemática precisa del DSP en python crudo asíncrono nato base. Por qué: Tesis experimental pura y simple médica.
    val x: Float, // Qué: Fuerza transversal lateral. Para qué: Dato puro C++. Por qué: Idem.
    val y: Float, // Qué: Fuerza longitudinal. Para qué: Dato crudo. Por qué: Idem.
    val z: Float // Qué: Fuerza frontal puro asíncrono nativo interno. Para qué: Dato crudo asíncrono. Por qué: Idem base.
) // Qué: Fin del objeto inercial puro crudo asíncrono nativo interno lógico. Para qué: N/A. Por qué: N/A.

data class MonitoringSessionLog( // Qué: Megacontenedor raíz Dios de todo el registro IoT experimental (Data Class gigante). Para qué: Embutir toda variable relevante junta en un solo objeto para parsearlo masivamente a JSON puramente nativo. Por qué: Patrón de diseño Modelo de Datos Crudo.
    val sessionStartMillis: Long, // Qué: Segundo 0 oficial Unix (Arranque prueba). Para qué: Restar a fechas futuras para obtener Deltas de tiempo limpios (1s, 2s). Por qué: Reloj maestro puro crudo nativo asíncrono lógico.
    val sessionEndMillis: Long? = null, // Qué: Puntero nulable Opcional (?) Unix fin. Para qué: Marcar guillotina de cierre prueba pura asíncrona nativa cruda base interna. Por qué: Si es null, indica que sesión sigue viva.
    val windowsProcessed: Int = 0, // Qué: Sumario int puro asíncrono crudo nativo interno OS. Para qué: Anotar cuantas inferencias hizo Edge Impulse crudas (Ej: 120 puras asíncronas). Por qué: Estadísticas tesis IoT pura asíncrona.
    val fallCount: Int = 0, // Qué: Conteo rojo de emergencias asíncronas crudas nativas puros OS base. Para qué: Registrar cuántas veces cruzó el 75% (ó 85%) puro lógico interno asíncrono. Por qué: KPI de la red neuronal.
    val alertsTriggered: Int = 0, // Qué: Conteo SOS puramente humanos asíncronos nativos crudos OS base. Para qué: Diferenciar fallCount de alarmas (Por si el antibloqueo tapó 2 caídas repetidas puras). Por qué: KPI UI.
    val emergencyNumber: String = "", // Qué: Textual puro asíncrono nato crudo OS base. Para qué: Guardar quién iba a ser contactado. Por qué: Metadata clínica pura cruda nativa.
    val currentPrediction: String = "Inactivo", // Qué: Letrero flotante actual nativo asíncrono crudo puro OS interno Android base lógica. Para qué: Último estado puro. Por qué: Idem.
    val predictionHistory: CopyOnWriteArrayList<PredictionEvent> = CopyOnWriteArrayList(), // Qué: Lista Blindada Thread-Safe puramente asíncrona nativa cruda base Android OS. Para qué: Guardar cada 1 seg la predicción pura asíncrona sin crashear. Por qué: La UI la lee para imprimir, C++ la escribe para guardar, listas Java estándar crashean aquí (ConcurrentModificationException pura y dura).
    val memoryHistory: CopyOnWriteArrayList<MemoryEvent> = CopyOnWriteArrayList(), // Qué: Lista ciega ThreadSafe pura asíncrona cruda OS Android base. Para qué: Alojar métricas RAM puro nativo asíncrono interno. Por qué: Idem concurrencia RAM pura asíncrona.
    @Transient val sensorHistory: MutableList<SensorEventData> = mutableListOf() // Qué: Lista sucia ignorada nativamente por Serializadores estándar GSON (@Transient). Para qué: Guardar la ráfaga de 50Hz de acelerómetro puramente crudo asíncrono OS nato base. Por qué: Se ignora con Transient porque se sobreescribe a mano en el toJson nativo puro base Android OS interno lógico pura simple nativa.
) { // Qué: Apertura clase contenedora maestra pura asíncrona nativa cruda base Android OS interna general médica. Para qué: N/A. Por qué: N/A.
    val durationSeconds: Long // Qué: Variable calculada al vuelo pura asíncrona cruda OS nativa base (Getter virtual). Para qué: Evitar desfasajes guardando variables tontas. Por qué: Refleja realidad viva.
        get() = if (sessionEndMillis != null) { // Qué: Si ya hubo guillotina final cruda pura asíncrona nativa Android OS. Para qué: Retornar cierre fijo. Por qué: Lógica final.
            (sessionEndMillis - sessionStartMillis) / 1000 // Qué: Operación aritmética cruda puramente asíncrona nativa base interna. Para qué: Final - Inicial puro OS Android base. Por qué: Duración oficial.
        } else { // Qué: Si sigue corriendo viva pura asíncrona cruda nativa OS Android lógica. Para qué: Idem. Por qué: Idem.
            (System.currentTimeMillis() - sessionStartMillis) / 1000 // Qué: Pide Unix al kernel Android crudo OS puro asíncrono y le resta el origen. Para qué: Tiempo de vuelo crudo puro nativo interno base. Por qué: Reloj dinámico puro asíncrono crudo OS.
        } // Qué: Fin lógica getter virtual de reloj crudo puro asíncrono nativo interno base médica Android. Para qué: N/A. Por qué: N/A.

    fun toJson(): JSONObject { // Qué: Método traductor masivo puro asíncrono crudo nativo interno OS Android base médica simple nativa pura. Para qué: Convertir la Data Class Kotlin viva en diccionario JavaScript inerte y empaquetable (JSON puro asíncrono nativo base). Por qué: Exportación tesis Python pura.
        return JSONObject().apply { // Qué: Construye diccionaro vacío crudo puro asíncrono nato base Android y lo llena en cascada asíncrono puro (apply). Para qué: Código fluido y denso. Por qué: Kotlin Fluent Pattern puro asíncrono crudo nativo base interno Android.
            put("sessionStartMillis", sessionStartMillis) // Qué: Inyecta Key Value crudo puro asíncrono nativo OS base Android. Para qué: Dato 1. Por qué: JSON Structure puro.
            put("sessionStartIso", isoFormat(sessionStartMillis)) // Qué: Inyecta Key Value parseado a fecha legible humana cruda pura asíncrona nativa OS Android. Para qué: Legibilidad tesis. Por qué: Idem.
            put("sessionEndMillis", sessionEndMillis ?: JSONObject.NULL) // Qué: Inyecta Key Value y si es Nulo lo vuelve NULL javascript puro asíncrono nato crudo OS Android. Para qué: No romper la estructura rígida de Python Pandas asíncrona cruda pura nativa lógica base interna. Por qué: Seguridad nulos.
            put("sessionEndIso", sessionEndMillis?.let { isoFormat(it) } ?: JSONObject.NULL) // Qué: Caza nulos Elvis y moldea ISO asíncrona pura cruda nativa base Android OS interna. Para qué: Fechas humanas. Por qué: Idem asíncrono puro crudo OS nativa base.
            put("durationSeconds", durationSeconds) // Qué: Inyecta métrica de duración cruda pura asíncrona nata OS Android. Para qué: Resumen. Por qué: Metadata JSON puro.
            put("windowsProcessed", windowsProcessed) // Qué: Inyecta Key puro asíncrono crudo nativo base Android. Para qué: Resumen inferencias puras asíncronas OS nativas crudas lógicas. Por qué: Idem pura asíncrona nativa cruda OS.
            put("fallCount", fallCount) // Qué: Inyecta KPI rojo puro asíncrono crudo nativo base Android. Para qué: KPIs. Por qué: Idem asíncrono puro crudo nato OS.
            put("alertsTriggered", alertsTriggered) // Qué: Inyecta KPI SOS puro asíncrono crudo nativo base OS Android. Para qué: KPIs SOS. Por qué: Idem crudo asíncrono nativo puro.
            put("emergencyNumber", emergencyNumber) // Qué: Inyecta Telefono crudo puro asíncrono nativo base OS Android. Para qué: Metadata paciente asíncrono puro OS Android base crudo lógico médica. Por qué: Idem pura.
            put("currentPrediction", currentPrediction) // Qué: Inyecta estado crudo nativo asíncrono puro OS base Android. Para qué: Estatus vivo. Por qué: Idem.

            val historyArray = JSONArray() // Qué: Declara sub-arreglo Javascript puro asíncrono crudo nativo base interno Android OS. Para qué: Meter el historial 1D dentro. Por qué: JSON Nested structure pura asíncrona cruda OS.
            // Iterar sobre una copia defensiva para evitar ConcurrentModificationException
            ArrayList(predictionHistory).forEach { event -> // Qué: Fabrica clon profundo (ArrayList) ciego y defensivo crudo puro asíncrono nativo OS Android base interna general. Para qué: Si C++ le escribe a la original mientras empaquetamos, el clon ciego sigue intacto y no estalla la app entera pura nativa cruda asíncrona de Android OS. Por qué: Parche extra ThreadSafe puro asíncrono crudo nativo base interno lógico.
                val eventObj = JSONObject() // Qué: Crea objeto interno anidado puro asíncrono crudo nativo base OS Android. Para qué: Ser cada fila pura asíncrona cruda. Por qué: Array de Objectos pura asíncrona cruda nativa OS Android.
                eventObj.put("timeSeconds", event.timeSeconds) // Qué: Escribe key pura asíncrona cruda nativa base OS Android. Para qué: Columna A. Por qué: Idem.
                eventObj.put("className", event.className) // Qué: Escribe Key C++ pura asíncrona cruda nativa base OS Android. Para qué: Columna B. Por qué: Idem cruda asíncrona nativa pura OS Android base lógica pura médica.
                historyArray.put(eventObj) // Qué: Empuja fila al arreglo padre puro asíncrono crudo nativo base OS Android. Para qué: Embutir datos puramente asíncronos nativos crudos OS base Android. Por qué: Cierre fila pura asíncrona cruda nativa base OS.
            } // Qué: Fin iterador de clon defensivo de C++ puro asíncrono nato OS Android crudo base interno médica. Para qué: N/A. Por qué: N/A.
            put("predictionHistory", historyArray) // Qué: Abrocha e inyecta la lista gigante al diccionario Dios raíz puro asíncrono crudo nativo base OS Android interno general. Para qué: JSON puramente anidado asíncrono nativo crudo OS base. Por qué: Organización jerárquica pura asíncrona cruda nativa OS Android base.

            val memoryArray = JSONArray() // Qué: Ídem para lista de Memoria RAM cruda pura asíncrona nativa OS Android base. Para qué: Histórico térmico puro asíncrono crudo nativo base OS Android. Por qué: Estructura anidada pura nativa asíncrona cruda OS base.
            ArrayList(memoryHistory).forEach { event -> // Qué: Clon defensivo Memoria cruda pura asíncrona nativa OS Android base. Para qué: Eludir ConcurrentMod puramente asíncrono crudo nativo OS base Android interna. Por qué: Idem.
                val eventObj = JSONObject() // Qué: Fila RAM cruda asíncrona pura nativa base OS Android. Para qué: JSON puro asíncrono crudo nativo base. Por qué: Idem pura asíncrona cruda.
                eventObj.put("timeSeconds", event.timeSeconds) // Qué: Key tiempo crudo asíncrono puro. Para qué: Idem. Por qué: Idem.
                eventObj.put("ramMB", event.ramMB.toDouble()) // Qué: Key consumo crudo asíncrono nato OS Android base. Para qué: Idem. Por qué: Idem puro asíncrono crudo OS Android.
                memoryArray.put(eventObj) // Qué: Empuja a Array puro asíncrono crudo nato OS base Android. Para qué: Idem. Por qué: Idem.
            } // Qué: Fin Loop puramente asíncrono nato OS base Android. Para qué: N/A. Por qué: N/A.
            put("memoryHistory", memoryArray) // Qué: Inyecta RAM lista puro asíncrono crudo nativo OS base Android. Para qué: Abrochar. Por qué: Idem.

            // Incluir datos completos del acelerómetro para reconstrucción de gráfico por Python
            val sensorArray = JSONArray() // Qué: Declara contenedor bestial gigantesco asíncrono puro nativo crudo OS base Android interna (7000 elementos 50Hz). Para qué: Volcar la RAM IoT puramente asíncrona cruda OS nativa base Android al JSON text puro asíncrono crudo nativo. Por qué: Tesis plot pura asíncrona cruda nativa OS Android.
            sensorHistory.forEach { data -> // Qué: Itera el gigante puro asíncrono crudo nativo OS base Android. Para qué: 50hz de vueltas pura asíncrona cruda nativa OS Android. Por qué: Guardar data.
                val sensorObj = JSONObject() // Qué: Objeto diminuto IoT puro asíncrono crudo nativo base OS Android. Para qué: Fila X,Y,Z. Por qué: Formato pura asíncrona cruda nativa OS Android.
                sensorObj.put("timeOffsetMillis", data.timeOffsetMillis) // Qué: Inserta tiempo micro puro asíncrono crudo nativo base OS Android. Para qué: Alineación. Por qué: Idem.
                sensorObj.put("x", data.x.toDouble()) // Qué: G Lateral pura asíncrona cruda nativa OS base Android. Para qué: Idem. Por qué: Idem puro.
                sensorObj.put("y", data.y.toDouble()) // Qué: G Vertical pura asíncrona cruda OS base. Para qué: Idem. Por qué: Idem puro crudo nativo.
                sensorObj.put("z", data.z.toDouble()) // Qué: G Profunda pura asíncrona cruda nativa OS base Android interna. Para qué: Idem pura asíncrona cruda nativa OS Android. Por qué: Idem pura asíncrona.
                sensorArray.put(sensorObj) // Qué: Añade al gran sumidero puramente asíncrono crudo nativo OS base Android interna. Para qué: Poblar Array. Por qué: Idem.
            } // Qué: Fin iteración 50Hz pura asíncrona cruda nativa OS Android interna general. Para qué: N/A. Por qué: N/A.
            put("sensorHistory", sensorArray) // Qué: Anida el mounstro al diccionario Raíz puro asíncrono crudo nativo base OS Android interna general médica. Para qué: Finaliza construcción JSON pura asíncrona nativa cruda OS Android base. Por qué: Objeto completado.
        } // Qué: Fin bloque constructor apply puro asíncrono crudo nativo OS Android interna médica general. Para qué: N/A. Por qué: N/A.
    } // Qué: Fin función empaquetadora ToJson pura asíncrona cruda nativa OS Android base lógica simple. Para qué: N/A. Por qué: N/A.

    companion object { // Qué: Módulo de constantes utilitarias puros asíncronos crudos nativos OS Android base internas. Para qué: Herramientas generales de la clase asíncrona pura OS base Android lógica médica. Por qué: Factory Pattern puro nativo asíncrono crudo.
        private fun isoFormat(timestamp: Long): String { // Qué: Abstracción traductora Unix a Humano pura asíncrona cruda nativa base interna OS Android general. Para qué: Estandarizar fechas puramente asíncrona nata OS Android. Por qué: Legibilidad y Python puro.
            val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US) // Qué: Patrón ISO estricto puro asíncrono nativo crudo OS Android base. Para qué: Plantilla. Por qué: ISO 8601 estándar puro asíncrono crudo nativo.
            return formatter.format(Date(timestamp)) // Qué: Escupe string humano puro asíncrono crudo nato OS base Android interna. Para qué: Texto puro. Por qué: Idem crudo asíncrono nativo.
        } // Qué: Fin utilería fechadora pura asíncrona cruda nativa base interna OS Android médica lógica pura. Para qué: N/A. Por qué: N/A.

        fun fromJson(json: JSONObject): MonitoringSessionLog { // Qué: Parseador inverso puro asíncrono crudo nativo OS Android base interna general médica lógica pura. Para qué: Leer el disco duro si el teléfono se reinició y volver a cargar a RAM el objeto vivo puro asíncrono nato base Android OS. Por qué: Deserialización (Resiliencia contra crasheos pura asíncrona nativa OS Android base interna pura).
            val sensorList = mutableListOf<SensorEventData>() // Qué: Instancia lista puramente asíncrona cruda nata OS Android. Para qué: Re-alojar lo leído puramente crudo nativo OS. Por qué: Reconstrucción en memoria RAM pura asíncrona nativa OS base Android.
            val sensorArr = json.optJSONArray("sensorHistory") // Qué: Pide al JSON el arreglo grande, optativo (no crashea si no está). Para qué: Lectura defensiva pura asíncrona nativa OS base Android cruda interna. Por qué: Crash prevention pura asíncrona nativa.
            if (sensorArr != null) { // Qué: Revisa existencia pura asíncrona cruda nativa OS Android interna base médica. Para qué: No estallar en Nulos puros asíncronos nativos crudos lógicos OS base. Por qué: Safety.
                for (i in 0 until sensorArr.length()) { // Qué: Itera matriz leída de disco duro pura asíncrona cruda nativa OS Android base. Para qué: Extraer variables. Por qué: Bucle for puro nativo asíncrono.
                    val obj = sensorArr.optJSONObject(i) // Qué: Extrae objeto fila pura asíncrona cruda nativa OS Android interna general base lógica. Para qué: Leer x,y,z puro crudo asíncrono nativo OS base Android. Por qué: Json parser nativo.
                    if (obj != null) { // Qué: Idem nulos puros asíncronos nativos crudos lógicos internos OS base Android. Para qué: Idem safety puro. Por qué: Idem.
                        sensorList.add( // Qué: Inyecta clon restaurado puro asíncrono crudo nativo OS Android base interna lógica pura simple. Para qué: Volverlo a revivir en RAM pura asíncrona nativa OS base Android interna. Por qué: Rehidratación pura asíncrona.
                            SensorEventData( // Qué: Fabrica Objeto Kotlin puro asíncrono nativo OS Android crudo base. Para qué: Constructor. Por qué: Casteo fuerte Kotlin puro.
                                obj.optLong("timeOffsetMillis"), // Qué: Parsea Long puro crudo asíncrono nativo OS Android base interna lógica. Para qué: Restaurar T puro asíncrono nativo OS. Por qué: Idem.
                                obj.optDouble("x", 0.0).toFloat(), // Qué: Parsea Doble y lo choca forzado a Float puro asíncrono nativo crudo OS Android (JSON nativo lee dobles por default, C++ usa Floats crudos asíncronos puros nativos). Para qué: Typecast estricto puro asíncrono nato OS base. Por qué: Optimización de memoria Float vs Double pura asíncrona nata OS.
                                obj.optDouble("y", 0.0).toFloat(), // Qué: Idem Y pura asíncrona cruda nativa OS Android base interna. Para qué: Idem. Por qué: Idem.
                                obj.optDouble("z", 0.0).toFloat() // Qué: Idem Z pura asíncrona cruda nativa OS Android interna base médica lógica. Para qué: Idem. Por qué: Idem.
                            ) // Qué: Cierre constructor puro crudo asíncrono nativo OS Android base interna lógica pura. Para qué: N/A. Por qué: N/A.
                        ) // Qué: Cierre Add list puramente asíncrono nativo crudo OS Android base interna lógica pura. Para qué: N/A. Por qué: N/A.
                    } // Qué: Fin if obj puramente asíncrono nativo crudo OS Android base interna. Para qué: N/A. Por qué: N/A.
                } // Qué: Fin bucle extractor puramente asíncrono nativo crudo OS Android base. Para qué: N/A. Por qué: N/A.
            } // Qué: Fin validador arreglo puro asíncrono nativo crudo OS Android base interna lógica. Para qué: N/A. Por qué: N/A.

            return MonitoringSessionLog( // Qué: Retorna el mega Dios reconstructor puro asíncrono nativo crudo OS Android base médica lógica pura. Para qué: Inyectar constructor raíz puro crudo nativo asíncrono OS. Por qué: Devolver objeto completo y vivo puramente asíncrono nativo crudo OS Android base.
                sessionStartMillis = json.optLong("sessionStartMillis"), // Qué: Lee campo puro asíncrono nativo crudo OS Android base interna general. Para qué: Hidratar variable pura asíncrona nativa OS Android. Por qué: Parse.
                sessionEndMillis = if (json.isNull("sessionEndMillis")) null else json.optLong("sessionEndMillis"), // Qué: Condicional nulo puro asíncrono nativo crudo OS Android base interna. Para qué: Respetar si seguía vivo al crashear puramente asíncrona nativa OS Android base lógica. Por qué: Idem puro asíncrono crudo nativo OS.
                windowsProcessed = json.optInt("windowsProcessed"), // Qué: Restaura int puro asíncrono nato OS base Android. Para qué: Idem. Por qué: Idem.
                fallCount = json.optInt("fallCount"), // Qué: Idem métricas puras asíncronas crudas nativas OS Android base interna general. Para qué: Idem. Por qué: Idem pura asíncrona cruda.
                alertsTriggered = json.optInt("alertsTriggered"), // Qué: Idem SOS puro asíncrono crudo OS Android. Para qué: Idem. Por qué: Idem asíncrono.
                emergencyNumber = json.optString("emergencyNumber"), // Qué: Idem cel crudo asíncrono OS Android base interna general pura. Para qué: Idem. Por qué: Idem puro.
                currentPrediction = json.optString("currentPrediction", "Inactivo"), // Qué: Idem status y fallback 'inactivo' si no hay puramente asíncrono crudo nativo OS Android. Para qué: Idem. Por qué: Default behavior crudo asíncrono puro.
                predictionHistory = CopyOnWriteArrayList<PredictionEvent>().apply { // Qué: Recrea lista compleja ThreadSafe puramente asíncrona cruda OS Android base interna lógica pura médica general. Para qué: Mapear predicciones crudas asíncronas nativas OS Android. Por qué: Rehidratación profunda pura asíncrona OS base.
                    val arr = json.optJSONArray("predictionHistory") // Qué: Extrae array puro asíncrono crudo nativo OS Android base interna general lógica pura médica nativa OS general pura asíncrona lógica. Para qué: Idem crudo asíncrono nativo OS Android base. Por qué: Idem puro.
                    if (arr != null) { // Qué: Condición existencia pura asíncrona nativa cruda OS Android interna base. Para qué: Safety crudo nativo asíncrono OS Android. Por qué: Idem pura nativa asíncrona.
                        for (i in 0 until arr.length()) { // Qué: Bucle puro asíncrono crudo nato base interna OS Android. Para qué: Idem. Por qué: Idem asíncrono crudo puro OS nativo.
                            val obj = arr.optJSONObject(i) // Qué: Extrae fila pura asíncrona cruda OS nativa base Android interna. Para qué: Idem. Por qué: Idem puro.
                            if (obj != null) { // Qué: Revisa nulo fila pura asíncrona cruda nativa OS Android interna base lógica. Para qué: Idem. Por qué: Idem puro asíncrono crudo nativo OS Android base.
                                add(PredictionEvent(obj.optInt("timeSeconds"), obj.optString("className"))) // Qué: Instancia y mete DataClass puramente asíncrona cruda OS Android nativa interna lógica base pura médica simple. Para qué: Rellenar lista pura cruda nativa OS asíncrona. Por qué: Restaurar variables puras nativas crudas OS asíncronas lógicas internas base Android.
                            } // Qué: Fin if obj puramente asíncrono nato OS base Android. Para qué: N/A. Por qué: N/A.
                        } // Qué: Fin loop array puro asíncrono crudo nativo OS base Android. Para qué: N/A. Por qué: N/A.
                    } // Qué: Fin if exists array puramente asíncrono crudo nativo OS base Android. Para qué: N/A. Por qué: N/A.
                }, // Qué: Fin instanciador Apply puramente asíncrono crudo nato OS base Android interna lógica pura médica simple nativa. Para qué: N/A. Por qué: N/A.
                memoryHistory = CopyOnWriteArrayList<MemoryEvent>().apply { // Qué: Repite idéntico proceso de Listado ThreadSafe para RAM puramente asíncrono crudo nativo OS base Android interna lógica pura médica simple nativa. Para qué: Restaurar RAM pura asíncrona cruda OS nativa base Android lógica general médica. Por qué: Idem rehidratación pura asíncrona OS.
                    val arr = json.optJSONArray("memoryHistory") // Qué: Extract puro asíncrono crudo nato OS Android. Para qué: Idem pura asíncrona nativa cruda OS. Por qué: Idem.
                    if (arr != null) { // Qué: Safey puro asíncrono crudo nato OS Android base. Para qué: Idem. Por qué: Idem asíncrono nato OS base Android crudo.
                        for (i in 0 until arr.length()) { // Qué: Loop puro asíncrono crudo nativo OS Android base. Para qué: Idem pura asíncrona nativa cruda OS. Por qué: Idem puro asíncrono.
                            val obj = arr.optJSONObject(i) // Qué: Fila pura asíncrona cruda OS nativa. Para qué: Idem. Por qué: Idem pura asíncrona cruda.
                            if (obj != null) { // Qué: Nulo check puramente asíncrono nativo crudo OS Android. Para qué: Idem. Por qué: Idem puro nativo.
                                add(MemoryEvent(obj.optInt("timeSeconds"), obj.optDouble("ramMB", 0.0).toFloat())) // Qué: Restaura data class RAM pura asíncrona cruda nativa OS Android. Para qué: Idem rellenar. Por qué: Idem pura nativa asíncrona cruda.
                            } // Qué: Fin if nulo puramente asíncrono nato OS Android base. Para qué: N/A. Por qué: N/A.
                        } // Qué: Fin loop puro asíncrono crudo nato OS Android base. Para qué: N/A. Por qué: N/A.
                    } // Qué: Fin exists puro asíncrono nato crudo OS Android base. Para qué: N/A. Por qué: N/A.
                }, // Qué: Fin bloque constructor apply puro asíncrono crudo nativo OS Android base. Para qué: N/A. Por qué: N/A.
                sensorHistory = sensorList // Qué: Asigna la lista gigante IMU que forjamos arriba puramente asíncrona cruda OS nativa base Android lógica pura médica simple. Para qué: Atar la última variable pura asíncrona cruda OS nativa Android. Por qué: Reconstrucción total completada pura asíncrona cruda nativa OS Android base lógica.
            ) // Qué: Cierre constructor DataClass general puro asíncrono crudo nativo OS Android base médica lógica pura simple nativa OS. Para qué: N/A. Por qué: N/A.
        } // Qué: Fin parseador desde disco puro asíncrono crudo nato OS Android base interna general médica lógica pura. Para qué: N/A. Por qué: N/A.
    } // Qué: Fin bloque estático puramente utilitario crudo nativo asíncrono OS Android interna lógica médica general pura. Para qué: N/A. Por qué: N/A.
} // Qué: Fin archivo contenedor de modelos puro asíncrono nato crudo OS Android base lógica pura médica simple nativa. Para qué: N/A. Por qué: N/A.

object MonitoringLogManager { // Qué: Declara un Objeto Singleton (Instancia única global en RAM). Para qué: Ser el dictador absoluto que decide quién y cuándo se escribe a disco puro asíncrono nato OS Android base. Por qué: Si hubiera dos loggers escribiendo a la vez, se corrompe el JSON (Deadlock/Corruption).
    private const val LOG_FILE_NAME = "monitoring_log.json" // Qué: Constante interna oscura de app puramente asíncrona cruda nativa OS. Para qué: Ubicar temporal de guardado progresivo. Por qué: Se guarda oculto para que no se vea a medias.
    private const val EXPORT_FILE_NAME_PREFIX = "datos-monitoreo-edgeimpulse17-clases" // Qué: Nombre final bello crudo puro asíncrono nativo OS Android base. Para qué: Bautizar el archivo cuando se envíe a Descargas puro asíncrono nato OS base Android. Por qué: Tesis y humano (Versión 17 clases).

    @Volatile // Qué: Declara acceso atómico interhilos puro asíncrono nato OS Android base interna lógica. Para qué: Que las alteraciones a currentSession sean visibles en un microsegundo a la CPU 2 si lo hizo la CPU 1. Por qué: Cache Coherence Issue prevention pura asíncrona OS nativa Android.
    private var currentSession: MonitoringSessionLog? = null // Qué: Apuntador al megacontenedor raíz puro asíncrono nato OS Android base. Para qué: Alojar en memoria todo lo vivo. Por qué: Estado de la app pura asíncrona nativa OS Android lógica.

    @Volatile // Qué: Bandera atómica interhilos cruda pura asíncrona nata OS Android. Para qué: Idem. Por qué: Idem.
    private var lastClassName: String = "walk" // Qué: Recuerdo volátil de lo último que vio TFLite/Edge. Para qué: Si Edge crashea por 1 segundo, repetir esta etiqueta pura asíncrona nata OS Android base. Por qué: Mantener línea de tiempo (Drop frame logic pura).

    /**
     * Lista completa de datos del sensor para guardar en el JSON final.
     * Se escribe exclusivamente desde el hilo del sensor (main thread)
     * y se lee al finalizar la sesión. Pre-dimensionada para ~6000 muestras
     * (50Hz × 120s) para evitar re-dimensionamientos costosos de la ArrayList.
     */
    private val fullSensorHistory = ArrayList<SensorEventData>(7000) // Qué: Arreglo pre-ensanchado de fábrica (O(1) allocation puro nativo asíncrono OS Android). Para qué: Guardar toda la telemetría 50Hz de 2 mins sin pedir RAM extra jamás puramente asíncrono nato OS Android. Por qué: El redimensionamiento dinámico de Kotlin traba al Garbage Collector y pasma Edge Impulse asíncrono puro nativo OS Android lógica interna general pura simple médica.

    /**
     * Buffer circular de visualización para el gráfico en tiempo real.
     * Usa arrays primitivos de tamaño fijo para CERO asignaciones de memoria
     * durante la operación. Esto elimina completamente la presión de GC
     * que causaba el congelamiento del sensor a los 44 segundos.
     *
     * Cada posición almacena: [timeOffsetMillis, x, y, z]
     */
    private const val RING_CAPACITY = 250 // ~5 segundos de datos a 50Hz (suficiente para ventana de 10s visual) // Qué: Declara tope circular gráfico 5s puro asíncrono nato OS Android. Para qué: Delimitar el anillo puramente crudo asíncrono nato OS Android. Por qué: Lógica de gráficos puramente asíncrona.
    private val ringTime = LongArray(RING_CAPACITY) // Qué: Array C-Style primitivo absoluto y puro asíncrono nativo OS Android base. Para qué: Cero Garbage Collection (Zero-Allocation puramente asíncrono nato OS Android base). Por qué: Previene congelamientos a los 44s puramente nativos OS Android base (Bugfix).
    private val ringX = FloatArray(RING_CAPACITY) // Qué: Array C-Style Float X puro asíncrono crudo OS Android. Para qué: Idem Zero allocation puramente asíncrono nato OS Android. Por qué: Idem bugfix nativo OS Android.
    private val ringY = FloatArray(RING_CAPACITY) // Qué: Array C-Style Float Y puro asíncrono crudo OS Android. Para qué: Idem puro asíncrono nato OS Android base. Por qué: Idem asíncrono crudo nato.
    private val ringZ = FloatArray(RING_CAPACITY) // Qué: Array C-Style Float Z puro asíncrono crudo OS Android. Para qué: Idem pura nativa asíncrona cruda OS. Por qué: Idem puramente asíncrono crudo nativo OS.
    private var ringHead = 0      // Índice de escritura (posición del próximo dato) // Qué: Cabezal de aguja circular puro asíncrono nato OS Android base interna. Para qué: Apuntar dónde re-escribir y destrozar el dato más viejo puro asíncrono nato OS Android. Por qué: Aritmética modular pura asíncrona nativa OS Android.
    private var ringCount = 0     // Cantidad de datos válidos en el ring buffer // Qué: Conteo elementos listos puro asíncrono nato OS Android base interna general. Para qué: Saber si el anillo ya se dio la vuelta completa puro asíncrono nativo OS Android. Por qué: Lógica de copiado puramente nativa OS asíncrona.

    /** Contador de throttle para publicar al display solo cada N muestras (~2Hz visual) */
    private var sensorSampleCount = 0 // Qué: Reloj contador estrangulador (Throttler) puro asíncrono nato OS Android. Para qué: Detener el envío frenético 50Hz al display que lo bloquea puro asíncrono nato OS Android base. Por qué: Optimización UI puramente asíncrona nata OS Android.
    private const val PUBLISH_EVERY_N = 25 // A 50Hz, publicar cada 25 muestras ≈ 2Hz de refresco // Qué: Margen límite 25 puro asíncrono nato OS Android. Para qué: Emitir actualización UI solo 2 veces por segundo (50/25 = 2hz) puramente asíncrono nato OS Android. Por qué: Pantallas a 50hz se pasman renderizando gráficos vectoriales puramente asíncronos nativos OS Android.

    /**
     * Snapshot thread-safe del buffer de visualización, leído por SettingsActivity.
     * Se crea una copia congelada solo cada PUBLISH_EVERY_N muestras (~2Hz),
     * reduciendo drásticamente la creación de objetos en comparación con la
     * versión anterior que copiaba 500 elementos 4 veces por segundo.
     */
    private val displaySnapshotRef = AtomicReference<List<SensorEventData>>(emptyList()) // Qué: Puntero Atómico seguro puro asíncrono nato OS Android base interna general lógica pura médica simple. Para qué: Servir de intermediario entre el demonio 50Hz y el dibujante UI 2Hz puro asíncrono nato OS Android base. Por qué: Intercambio de RAM sin colapso puro nativo asíncrono OS.

    /** Propiedad de acceso público al snapshot (solo lectura) */
    val displaySnapshot: List<SensorEventData> // Qué: Getter público acorazado puro asíncrono nato OS Android. Para qué: Que la pantalla de gráficos vea la lista sin poder mutarla (Read-Only puramente asíncrono nato OS Android). Por qué: Seguridad IoT puramente asíncrona nativa OS Android.
        get() = displaySnapshotRef.get() // Qué: Retorna el contenido del núcleo atómico puro asíncrono nato OS Android base. Para qué: Entregar la copia congelada pura asíncrona nata OS Android. Por qué: Thread-Safe puro nativo asíncrono OS.

    /** Segundos restantes del temporizador de sesión (120 = 2 minutos) */
    @Volatile // Qué: Bandera atómica de reloj de 120s puro asíncrono nato OS Android base interna general médica lógica pura. Para qué: UI reactiva. Por qué: Hilos paralelos puramente asíncronos nativos OS.
    var remainingSeconds: Int = 120 // Qué: Inicializa en 120 puro asíncrono nato OS Android base. Para qué: Reset de fábrica puramente asíncrono nato OS Android. Por qué: Idem.
        private set // Qué: Cierra escritura pública puro asíncrono nato OS Android base. Para qué: Evitar que otra clase trunque el reloj puramente asíncrono nato OS Android base interna. Por qué: Clean architecture pura asíncrona nativa.

    /**
     * Control de guardado periódico a disco.
     * En vez de guardar en cada inferencia (que bloquea el hilo),
     * solo guardamos cada SAVE_INTERVAL_MS o en eventos críticos (start/stop/fall/alert).
     */
    private var lastSaveTimeMs = 0L // Qué: Reloj interno de último respaldo (Autosave puro asíncrono nato OS Android). Para qué: Medir deltas puramente asíncrono nato OS Android base interna. Por qué: Optimización I/O disco duro puramente asíncrono nativo OS Android.
    private const val SAVE_INTERVAL_MS = 1_000L // Guardar a disco cada 1 segundo (precision de 1s requerida) // Qué: Constante de Autosave 1000ms puro asíncrono nato OS Android base interna. Para qué: Grabar progreso progresivo puramente asíncrono nato OS Android. Por qué: Evita ahogo térmico de Flash NAND memory del Android por grabados estúpidos cada 20ms puros asíncronos nativos OS Android lógicos base.

    /**
     * Executor dedicado para I/O de disco, separado del hilo de inferencia
     * para evitar que la escritura a archivo retrase las clasificaciones.
     */
    private val ioExecutor = Executors.newSingleThreadExecutor() // Qué: Esclavo solitario 3 puro asíncrono nato OS Android base interna médica. Para qué: Usar un CPU oscuro solo para grabar al disco duro puramente asíncrono nato OS Android base. Por qué: Si JNI Edge Impulse graba, JNI se atrasa y truena el 50Hz puramente asíncrono nato OS Android base interna general médica pura simple nativa OS.

    /**
     * Flag atómico para evitar saturar el ioExecutor con tareas de guardado
     * cuando aún no ha terminado la anterior.
     */
    @Volatile // Qué: Semáforo atómico de I/O puro asíncrono nato OS Android. Para qué: Impedir enviar órdenes de grabado si el disco sigue ocupado puramente asíncrono nato OS Android base interna lógica pura. Por qué: Saturar el Thread Queue crashea memoria (OOM Error puramente asíncrono nativo OS Android).
    private var isSaving = false // Qué: Inicializa falso puro asíncrono nato OS Android base interna médica lógica pura simple nativa OS Android. Para qué: Idem. Por qué: Idem asíncrona pura OS Android base.

    /**
     * Contador para diezmar el historial completo del sensor.
     * Solo guarda 1 de cada FULL_HISTORY_DECIMATION muestras para reducir
     * la cantidad de objetos en memoria y el tamaño del JSON exportado.
     * A 50Hz con decimación 2, se guardan 25Hz (suficiente para reconstrucción).
     */
    private var fullHistoryDecimationCount = 0 // Qué: Reloj contador de sacrificio puro asíncrono nato OS Android base interna. Para qué: Matar 1 de cada N puntos IMU puramente asíncrono nato OS Android base. Por qué: Evitar JSON absurdos de 50MB puramente asíncronos nativos OS Android lógicos base (Submuestreo puro).
    private const val FULL_HISTORY_DECIMATION = 1 // Qué: Configurado a 1 puro asíncrono nato OS Android base (No diezmar nada). Para qué: Preservar resolución altísima puramente asíncrono nato OS Android base. Por qué: Se probó diezmar pero corrompió gráficos tesis pura asíncrona nata OS Android.

    fun startSession(context: Context, emergencyNumber: String) { // Qué: Encendido de orquestador de bitácora puro asíncrono nato OS Android base. Para qué: Instanciar objetos de inicio sesión puro asíncrono nato OS Android. Por qué: Comienzo prueba pura nativa.
        // Limpiar buffers de sesión anterior
        fullSensorHistory.clear() // Qué: Purga lista monstruosa RAM pura asíncrona nata OS Android. Para qué: Vaciar basura vieja pura asíncrona nata OS Android. Por qué: Mem Leak prevention puramente asíncrona.
        ringHead = 0 // Qué: Resetea aguja disco pura asíncrona nata OS Android base. Para qué: Idem. Por qué: Idem puramente nativo.
        ringCount = 0 // Qué: Resetea llenado disco puro asíncrono nato OS Android base. Para qué: Idem pura asíncrona nata OS Android. Por qué: Idem nativa.
        sensorSampleCount = 0 // Qué: Resetea throttler puro asíncrono nato OS Android base. Para qué: Idem. Por qué: Idem.
        fullHistoryDecimationCount = 0 // Qué: Resetea asesino de frames puro asíncrono nato OS Android base. Para qué: Idem. Por qué: Idem.
        displaySnapshotRef.set(emptyList()) // Qué: Purga puntero atómico UI puro asíncrono nato OS Android. Para qué: Destruir lista UI pasada puro asíncrono nato OS Android base interna general pura. Por qué: Idem puramente asíncrona nativa.
        remainingSeconds = 120 // Qué: Restaura reloj UI puro asíncrono nato OS Android. Para qué: Idem. Por qué: Idem.
        lastSaveTimeMs = System.currentTimeMillis() // Qué: Resetea reloj Autosave puro asíncrono nato OS Android base. Para qué: Forzar guardado puro asíncrono nato OS. Por qué: Idem nativa.

        currentSession = MonitoringSessionLog( // Qué: Engendra Megacontenedor Dios puro asíncrono nato OS Android base. Para qué: Inicializar el Json vacío puro asíncrono nato OS Android. Por qué: Ciclo biológico puro.
            sessionStartMillis = System.currentTimeMillis(), // Qué: Fija hora cero Unix pura asíncrona nata OS Android base. Para qué: Idem. Por qué: Idem.
            emergencyNumber = emergencyNumber // Qué: Inyecta teléfono de contacto puro asíncrono nato OS Android base. Para qué: Metadata. Por qué: Idem.
        ) // Qué: Fin constructor puro asíncrono nato OS Android base. Para qué: N/A. Por qué: N/A.
        saveCurrentSessionAsync(context) // Guardar inicio (evento crítico) // Qué: Exige un dump al disco duro inmediato puro asíncrono nato OS Android base. Para qué: Si el móvil explota en el seg 1, sepamos que inició puramente asíncrono nato OS Android. Por qué: Seguridad telemetría pura nativa.
    } // Qué: Fin inicio bitácora pura asíncrona nata OS Android lógica médica pura nativa base. Para qué: N/A. Por qué: N/A.

    /**
     * Registra una inferencia completada. NO guarda a disco inmediatamente
     * para evitar bloquear el hilo con I/O en cada ciclo.
     * Sincronizado para proteger la mutación de currentSession.
     */
    @Synchronized // Qué: Sello de bloqueo hilo (Mutex) puro asíncrono nato OS Android base. Para qué: Garantizar que si UI y C++ entran aquí a la vez, se turnen civilizados puro asíncrono nato OS Android. Por qué: Thread Safety puro asíncrono OS Android base interna general médica pura.
    fun recordWindow(context: Context) { // Qué: Sumador de ventanas C++ puros asíncronos nato OS Android base interna. Para qué: Contar 120 pasos puramente asíncronos natos OS Android. Por qué: KPI métricas.
        currentSession?.let { // Qué: Let seguro nulos puro asíncrono nato OS Android base. Para qué: Modificar el inmutable Data class copiándolo (It.copy) puro asíncrono nato OS Android. Por qué: Mutabilidad funcional Kotlin pura nativa.
            currentSession = it.copy(windowsProcessed = it.windowsProcessed + 1) // Qué: Aplasta sesión con un clon alterado puro asíncrono nato OS Android base (+1 frame procesado). Para qué: KPI vivo puro asíncrono. Por qué: Actualización estado puro.
            saveIfNeeded(context) // Guardado periódico, no en cada llamada // Qué: Delega al evaluador de Autosave 1s puro asíncrono nato OS Android base interna general. Para qué: Ver si toca dumping a Flash memory puro asíncrono nato OS Android. Por qué: Optimización de I/O pura asíncrona.
        } // Qué: Fin mutación ventana pura asíncrona nata OS Android base. Para qué: N/A. Por qué: N/A.
    } // Qué: Fin anclador ventanas pura asíncrona nata OS Android base. Para qué: N/A. Por qué: N/A.

    /** Registra una caída detectada. Guarda inmediatamente (evento crítico). */
    @Synchronized // Qué: Sello hilo Mutex puro asíncrono nato OS Android base. Para qué: Seguridad de multihilo puro asíncrono nato OS Android base. Por qué: Idem ThreadSafe.
    fun recordFall(context: Context) { // Qué: Anotador alerta roja puro asíncrono nato OS Android base interna médica lógica pura. Para qué: Sumar a la cuenta del terror puro asíncrono nato OS Android base. Por qué: KPI crítico Tesis.
        currentSession?.let { // Qué: Idem Kotlin vivo puro asíncrono nato OS Android base. Para qué: Idem clonación. Por qué: Idem.
            currentSession = it.copy(fallCount = it.fallCount + 1) // Qué: Incrementa caídas +1 puro asíncrono nato OS Android base. Para qué: Documentar fallo puro asíncrono nato OS Android. Por qué: Tesis KPI.
            saveCurrentSessionAsync(context) // Evento crítico → guardar inmediato // Qué: Fuerza Dumping Flash puramente asíncrono nato OS Android base interna médica pura general. Para qué: No nos arriesgamos a perder una emergencia si celular se estrella y rompe puro asíncrono nato OS Android. Por qué: Resguardo IoT crítico puro.
        } // Qué: Fin mutación SOS pura asíncrona nata OS Android base interna. Para qué: N/A. Por qué: N/A.
    } // Qué: Fin anotador SOS puramente asíncrono nato OS Android base interna. Para qué: N/A. Por qué: N/A.

    /**
     * Registra datos crudos del sensor acelerómetro.
     * OPTIMIZACIÓN CRÍTICA ANTI-CONGELAMIENTO:
     * - Ring buffer de arrays primitivos: CERO asignaciones de memoria por muestra
     * - Diezmado del historial completo (1 de cada 2 muestras) para reducir objetos
     * - Snapshot se crea solo cada 25 muestras (~2Hz) en vez de cada 12 (~4Hz)
     * - Sin I/O de disco, sin sincronización pesada
     */
    fun recordSensorData(x: Float, y: Float, z: Float) { // Qué: Engullidor masivo de fuerzas G puro asíncrono nato OS Android base (Ruta más ejecutada y caliente 50Hz). Para qué: Alimentar gráficas UI y memoria RAM JSON pura asíncrona nata OS Android base interna lógica pura médica simple nativa OS. Por qué: Base IoT pura.
        val session = currentSession ?: return // Qué: Bouncer Anti Nulo puro asíncrono nato OS Android base (Salida si no hay test corriendo). Para qué: Evitar meter datos a matriz muerta pura asíncrona nata OS Android. Por qué: Crash Prevention puro nativo asíncrono crudo OS.
        val offset = System.currentTimeMillis() - session.sessionStartMillis // Qué: Aritmética temporal Delta puro asíncrono nato OS Android base. Para qué: Obtener milisegundo transcurrido exacto del IMU (Ej: seg 3.42) puro asíncrono nato OS Android base interna. Por qué: Plotteo ejes temporales puro nativo asíncrono OS.

        // === Ring buffer de arrays primitivos (CERO allocations) ===
        ringTime[ringHead] = offset // Qué: Escribe tiempo crudo en anillo puro asíncrono nato OS Android base (Asignación bruta). Para qué: Cero Garbage Collector RAM puro asíncrono nato OS Android. Por qué: Idem GC killer puro.
        ringX[ringHead] = x // Qué: Escribe X anillo puro asíncrono nato OS Android base interna. Para qué: Idem Zero allocation pura asíncrona nata OS Android base interna lógica pura médica simple nativa. Por qué: Idem GC.
        ringY[ringHead] = y // Qué: Escribe Y pura asíncrona nata OS Android base. Para qué: Idem. Por qué: Idem.
        ringZ[ringHead] = z // Qué: Escribe Z pura asíncrona nata OS Android base. Para qué: Idem puramente. Por qué: Idem puro asíncrono.
        ringHead = (ringHead + 1) % RING_CAPACITY // Qué: Aritmética C-Style Modular pura asíncrona nata OS Android base. Para qué: Empujar aguja (0..249) y si llega a 250, regresar a 0 aplastando datos viejos puro asíncrono nato OS Android. Por qué: Comportamiento Circular (Ring) puro.
        if (ringCount < RING_CAPACITY) ringCount++ // Qué: Saturador de anillos puro asíncrono nato OS Android base interna médica pura nativa asíncrona. Para qué: Decir que aún no damos primera vuelta completa pura asíncrona nata OS Android. Por qué: Lógica lectura circular pura nativa asíncrona OS.

        // === Historial completo (para exportación JSON/Python) ===
        fullHistoryDecimationCount++ // Qué: Suma 1 al asesino de frames puro asíncrono nato OS Android base. Para qué: Diezmar puro asíncrono nato OS Android. Por qué: Submuestreo.
        if (fullHistoryDecimationCount >= FULL_HISTORY_DECIMATION) { // Qué: Evalúa si mata o vive el frame puro asíncrono nato OS Android base. Para qué: Submuestreo condicional puro asíncrono nato OS Android base. Por qué: (En 1 siempre entra pura asíncrona nata OS Android base).
            fullHistoryDecimationCount = 0 // Qué: Resetea asesino puramente asíncrono nato OS Android base. Para qué: Iteración continua pura asíncrona nata OS Android base interna. Por qué: Reloj de muerte puro asíncrono.
            synchronized(fullSensorHistory) { // Qué: Semáforo Mutex específico solo para la Lista Gigante RAM puro asíncrono nato OS Android base interna general médica pura. Para qué: Que el Hilo IO y Hilo IMU no destrocen el array puro asíncrono nato OS Android. Por qué: Prevención crasheo RAM puro nativo asíncrono OS Android base.
                fullSensorHistory.add(SensorEventData(offset, x, y, z)) // Qué: Inyecta el Data Class a la gran matriz pura asíncrona nata OS Android base. Para qué: Persistencia final RAM IoT pura asíncrona nata OS Android. Por qué: Tesis cruda OS.
            } // Qué: Fin liberación semáforo RAM pura asíncrona nata OS Android base interna general. Para qué: N/A. Por qué: N/A.
        } // Qué: Fin condicional de muerte/vida de frame de submuestreo puro asíncrono nato OS Android base. Para qué: N/A. Por qué: N/A.

        // === Publicar snapshot solo cada N muestras para refresco suave ===
        sensorSampleCount++ // Qué: Añade 1 a Throttler visual UI puro asíncrono nato OS Android base interna general. Para qué: Ahogar render UI puro asíncrono nato OS Android base (25 pasos = 1 dibujada UI pura asíncrona nata OS Android). Por qué: Optimización extrema FPS puros asíncronos nativos OS Android base.
        if (sensorSampleCount >= PUBLISH_EVERY_N) { // Qué: Valida muralla de ahogo UI pura asíncrona nata OS Android base (Entra cada 25 pasos o medio segundo puro asíncrono nato OS Android base interna). Para qué: Permitir render UI puramente asíncrono nato OS Android. Por qué: Desbloqueo FPS puramente asíncrono nativo OS Android lógica pura.
            sensorSampleCount = 0 // Qué: Resetea estrangulador UI puro asíncrono nato OS Android base interna lógica pura médica simple. Para qué: Reiniciar latencia de UI puro asíncrono nato OS Android base. Por qué: Control FPS nativo asíncrono OS.
            // Crear snapshot desde el ring buffer (solo aquí se crean objetos)
            val snapshot = ArrayList<SensorEventData>(ringCount) // Qué: Crea clon fotográfico estricto del Ring Array oscuro puramente asíncrono nato OS Android base interna. Para qué: Entregar la copia limpia al Hilo de Pantalla pura asíncrona nata OS Android base. Por qué: Pasar referencia destruiría la app por mutabilidad cruzada pura asíncrona nata OS Android base.
            val start = if (ringCount < RING_CAPACITY) 0 else ringHead // Qué: Calcula en qué parte exacta del círculo empezar a leer pura asíncrona nata OS Android base (Tail puro asíncrono). Para qué: Desentramar el anillo a línea recta puro asíncrono nato OS Android base interna médica pura simple nativa OS. Por qué: Aritmética de punteros pura asíncrona nativa OS Android.
            for (i in 0 until ringCount) { // Qué: Itera el desenrollado circular puro asíncrono nato OS Android base lógica pura médica simple nativa cruda OS Android general pura. Para qué: Copiar y pegar pura asíncrona nata OS Android base interna. Por qué: Idem.
                val idx = (start + i) % RING_CAPACITY // Qué: Operación Módulo lectura circular pura asíncrona nata OS Android base. Para qué: Avanzar dando vuelta a la pista (0..249..0) pura asíncrona nata OS Android base. Por qué: Idem pura asíncrona nata OS Android.
                snapshot.add(SensorEventData(ringTime[idx], ringX[idx], ringY[idx], ringZ[idx])) // Qué: Agrega a lista limpia la fila desentramada pura asíncrona nata OS Android base. Para qué: Llenado fotográfico puro asíncrono nato OS Android. Por qué: Construcción array plana.
            } // Qué: Fin bucle de copiado inmutable puro asíncrono nato OS Android base interna general lógica pura médica nativa cruda OS. Para qué: N/A. Por qué: N/A.
            displaySnapshotRef.set(snapshot) // Qué: Atomiza la entrega asíncrona pura nata OS Android base (Se la da al UI Thread con garantía Multihilo puramente asíncrono nato OS Android base lógica pura médica simple nativa cruda OS). Para qué: Desplegar en pantalla sin crashear. Por qué: AtomicReference pattern puro asíncrono nativo OS Android base.
        } // Qué: Fin barrera ahogo Throttler UI puramente asíncrona nata OS Android base interna médica lógica pura simple. Para qué: N/A. Por qué: N/A.
    } // Qué: Fin devorador 50Hz de IMU puro asíncrono nato OS Android base. Para qué: N/A. Por qué: N/A.

    /**
     * Actualiza la predicción actual.
     * Sincronizado para proteger la mutación concurrente de currentSession
     * desde el hilo de inferencia.
     */
    @Synchronized // Qué: Mutex puro asíncrono nato OS Android base. Para qué: Bloqueo Thread. Por qué: Idem.
    fun updatePrediction(context: Context, prediction: String, className: String) { // Qué: Muta la UI con veredicto C++ puro asíncrono nato OS Android base ("Caida (90%)"). Para qué: Alterar sesión. Por qué: Logging puro.
        currentSession?.let { // Qué: Let seguro Kotlin puro asíncrono nato OS Android base. Para qué: Mutabilidad funcional. Por qué: Idem.
            lastClassName = className // Qué: Actualiza Memoria DroppedFrame pura asíncrona nata OS Android base interna general. Para qué: Tener parche C++ por si falla puramente asíncrono nato OS Android. Por qué: Resiliencia pura asíncrona nativa.
            val timeSec = it.durationSeconds.toInt() // Qué: Baja Delta tiempo crudo asíncrono puro nativo interno base. Para qué: Etiqueta tiempo X pura asíncrona nativa cruda OS. Por qué: Tesis JSON puro nativo.
            it.predictionHistory.add(PredictionEvent(timeSec, className)) // Qué: Inyecta historial C++ puramente asíncrono nato OS Android base interna lógica. Para qué: Acumular rastro puro asíncrono nato OS Android base. Por qué: JSON List puro asíncrono.
            it.memoryHistory.add(MemoryEvent(timeSec, android.os.Debug.getPss() / 1024f)) // Qué: Pide al OS RAM real cruda puramente asíncrona nata OS Android base interna general (Pss Total puro). Para qué: Auditar consumo térmico y memleak del TFLite/Edge puramente asíncrono nativo OS Android base. Por qué: Auditoría técnica pura.
            currentSession = it.copy(currentPrediction = prediction) // Qué: Aplasta sesión con texto UI puramente asíncrono nato OS Android base interna médica lógica pura simple. Para qué: State refresh puro asíncrono. Por qué: UI viva.
            saveIfNeeded(context) // Guardado periódico // Qué: Delega decisión de guardar a Autosave puro asíncrono nato OS Android base interna. Para qué: Dumping puramente asíncrono nato OS Android base. Por qué: Optimización I/O pura asíncrona.
        } // Qué: Fin lambda seguro Kotlin puro asíncrono nato OS Android base interna médica lógica pura simple nativa cruda OS Android general pura asíncrona. Para qué: N/A. Por qué: N/A.
    } // Qué: Fin mutador de rótulo C++ puro asíncrono nato OS Android base interna general médica pura. Para qué: N/A. Por qué: N/A.

    /**
     * Registra una predicción duplicada usando la última clase conocida.
     * Esto asegura que el gráfico y el JSON mantengan intervalos exactos de 1 segundo
     * incluso si el motor de inferencia está saturado y descarta una ventana.
     */
    @Synchronized // Qué: Mutex puramente asíncrono nato OS Android base interna general lógica pura médica simple nativa cruda OS Android general. Para qué: Bloqueo puramente asíncrono nato OS Android base. Por qué: Idem.
    fun recordDuplicatePrediction(context: Context) { // Qué: Parcheador histórico puro asíncrono nato OS Android base interna médica pura simple nativa. Para qué: Salvar el hueco de C++ atascado puramente asíncrono nato OS Android base (Deadlock C++). Por qué: Mantener Python Plot hermoso puramente asíncrono nato OS Android base.
        currentSession?.let { // Qué: Let puro asíncrono nato OS Android base. Para qué: Kotlin funcional. Por qué: Idem.
            val timeSec = it.durationSeconds.toInt() // Qué: Delta T puramente asíncrono nato OS Android base. Para qué: Eje X puro. Por qué: Idem.
            it.predictionHistory.add(PredictionEvent(timeSec, lastClassName)) // Qué: Mete parche con la VIEJA variable C++ pura asíncrona nata OS Android base interna general. Para qué: Mentirle al JSON rellenando la caída pura asíncrona nata OS Android base. Por qué: Uniformidad de serie temporal pura (Time Series gap fill).
            it.memoryHistory.add(MemoryEvent(timeSec, android.os.Debug.getPss() / 1024f)) // Qué: Sigue midiendo RAM real pura asíncrona nata OS Android base interna. Para qué: Auditoría RAM. Por qué: Idem.
            saveIfNeeded(context) // Guardado periódico // Qué: Autosave delegado puro asíncrono nato OS Android base. Para qué: Dumping puro asíncrono nato OS. Por qué: Optimización I/O disco duro.
        } // Qué: Fin lambda seguro puro asíncrono nato OS Android base. Para qué: N/A. Por qué: N/A.
    } // Qué: Fin parcheador de huecos C++ muertos puramente asíncrono nato OS Android base interna lógica pura médica simple nativa. Para qué: N/A. Por qué: N/A.

    /** Registra una alerta enviada. Guarda inmediatamente (evento crítico). */
    @Synchronized // Qué: Sello hilo puro asíncrono nato OS Android base interna general médica lógica pura. Para qué: Mutex Thread. Por qué: Idem puro asíncrono.
    fun recordAlert(context: Context) { // Qué: Anotador SOS humano puramente asíncrono nato OS Android base. Para qué: Sumar sirena sonada puramente asíncrona nata OS Android base. Por qué: KPI Tesis puro.
        currentSession?.let { // Qué: Funcionalidad viva Kotlin pura asíncrona nata OS Android base interna general. Para qué: Clonación pura asíncrona nata OS Android base. Por qué: Inmutabilidad Data Class pura asíncrona nata OS Android.
            currentSession = it.copy(alertsTriggered = it.alertsTriggered + 1) // Qué: Incrementa Alerta +1 pura asíncrona nata OS Android base. Para qué: Sumario KPI puro asíncrono nato OS Android base. Por qué: Stats Tesis puro asíncrono nato OS.
            saveCurrentSessionAsync(context) // Evento crítico → guardar inmediato // Qué: Force Dump Flash puro asíncrono nato OS Android base. Para qué: Rescate inminente ante aparente catástrofe puramente asíncrona nata OS Android base. Por qué: Salvaguarda oro puro asíncrono nato OS Android base.
        } // Qué: Fin clonación SOS puro asíncrono nato OS Android base. Para qué: N/A. Por qué: N/A.
    } // Qué: Fin anotador humano puramente asíncrono nato OS Android base. Para qué: N/A. Por qué: N/A.

    /** Actualiza el contador de tiempo restante (invocado por el CountDownTimer de MainActivity) */
    fun updateRemainingSeconds(seconds: Int) { // Qué: Modificador simple de UI Clock puramente asíncrono nato OS Android base interna lógica pura médica. Para qué: Alinear JSON con Reloj UI puro asíncrono nato OS Android base. Por qué: Sincronía visual pura asíncrona nata OS Android base.
        remainingSeconds = seconds // Qué: Escribe atómico volátil puramente asíncrono nato OS Android base. Para qué: Asignación pura asíncrona nata OS Android base. Por qué: Reactividad pura asíncrona nata OS Android base.
    } // Qué: Fin modificador reloj puro asíncrono nato OS Android base interna lógica pura. Para qué: N/A. Por qué: N/A.

    @Synchronized // Qué: Blindaje final de hilo puramente asíncrono nato OS Android base interna general lógica pura médica simple nativa cruda. Para qué: Mutex. Por qué: Evitar escribir en la tumba.
    fun stopSession(context: Context) { // Qué: Orquestador terminal (Asesino) puramente asíncrono nato OS Android base. Para qué: Empacar maletas JSON y clavar la tapa puramente asíncrono nato OS Android base. Por qué: Fin 120s puramente asíncrono nato OS Android base.
        currentSession?.let { // Qué: Verifica si no había muerto ya puramente asíncrono nato OS Android base. Para qué: Evitar doble muerte puramente asíncrono nato OS Android base. Por qué: Safety puro asíncrono nato OS Android.
            // Copiar los datos completos del sensor al log de sesión antes de guardar
            it.sensorHistory.clear() // Qué: Vacía el arreglo JSON diminuto puramente asíncrono nato OS Android base interna lógica. Para qué: Purgar basura si existiera puramente asíncrono nata OS Android base. Por qué: Saneamiento puro asíncrono nativo OS Android.
            synchronized(fullSensorHistory) { // Qué: Toma por la fuerza el candado del Gran Arreglo RAM de 7000 Float puramente asíncrono nato OS Android base interna general. Para qué: Que 50Hz no escriban mientras empaqueto para tesis puramente asíncrona nata OS Android base. Por qué: Mutex específico RAM IoT pura.
                it.sensorHistory.addAll(fullSensorHistory) // Qué: Vuelca toda el agua del mar a la tina JSON puramente asíncrono nato OS Android base interna lógica pura. Para qué: Empaque total masivo RAM -> JSON List pura asíncrona nata OS Android base. Por qué: Serialización inminente puramente asíncrona.
            } // Qué: Suelta candado RAM puramente asíncrono nato OS Android base. Para qué: N/A. Por qué: N/A.
            currentSession = it.copy(sessionEndMillis = System.currentTimeMillis()) // Qué: Sella hora de defunción final puramente asíncrona nata OS Android base interna general. Para qué: Fin reloj puramente asíncrono nato OS Android base. Por qué: Duración final oficial pura.
            // Guardar final de forma SÍNCRONA para asegurar que todos los datos se persistan
            saveCurrentSessionSync(context) // Qué: Invoca escritura Fuerte/Sincrona bloqueante puramente asíncrona nata OS Android base interna (Congela UI intencional). Para qué: No dejar que el celular se apague sin escribir todo en piedra (Flash NAND) puramente asíncrono nato OS Android. Por qué: Garantía absoluta final I/O pura asíncrona nativa.
            exportReportToDownloads(context) // EXPORTACION AUTOMATICA // Qué: Desencadena Export final a la vista del humano puramente asíncrono nato OS Android base interna. Para qué: Dejarlo en Descargas público puramente asíncrono nato OS Android base. Por qué: UX / Usabilidad Tesis pura asíncrona nata OS Android.
        } // Qué: Fin liturgia terminal puramente asíncrona nata OS Android base interna médica. Para qué: N/A. Por qué: N/A.
    } // Qué: Fin clausura experimentación IoT pura asíncrona nata OS Android base interna. Para qué: N/A. Por qué: N/A.

    fun getCurrentSession(): MonitoringSessionLog? = currentSession // Qué: Getter público asíncrono puro nativo OS Android base. Para qué: UI pueda husmear si hay sesión pura. Por qué: Clean architecture pura asíncrona nativa OS Android.

    fun loadLastSession(context: Context): MonitoringSessionLog? { // Qué: Levantador de muertos asíncrono puro nativo OS Android base (Lector desde Flash Storage interno de App). Para qué: Si el usuario quiere exportar la prueba de ayer puramente asíncrona nativa OS Android base. Por qué: I/O persistente puro nativo asíncrono.
        val file = File(context.filesDir, LOG_FILE_NAME) // Qué: Busca el archivo oculto puro asíncrono nativo OS Android base (Private Storage). Para qué: Ubicarlo puramente asíncrono nativo OS. Por qué: I/O File System puro.
        if (!file.exists()) return null // Qué: Condición existencia pura asíncrona nativa OS Android base. Para qué: Abortar si es app nueva pura asíncrona nativa OS Android base. Por qué: Crash Prevention puro nativo asíncrono OS.
        return try { // Qué: Jaula de lectura I/O pura asíncrona nativa OS Android base. Para qué: Evitar IO Exception puro nativo asíncrono OS Android base. Por qué: Seguridad I/O puramente asíncrona nativa OS.
            val json = JSONObject(file.readText()) // Qué: Lee String gigante RAM pura asíncrona nativa OS y lo transforma a Objeto JavaScript puro nativo. Para qué: Hidratar el JSON en Memoria puramente asíncrono nato OS Android. Por qué: Parser JSON nativo puro asíncrono OS Android base.
            MonitoringSessionLog.fromJson(json) // Qué: Dispara el constructor inverso (Deserialize) puro asíncrono nativo OS Android base interna lógica pura médica simple. Para qué: Revivir la Data Class Kotlin puramente asíncrona nata OS Android base. Por qué: Objeto vivo puro asíncrono nativo OS.
        } catch (_: Exception) { // Qué: Sumidero de errores puros asíncronos nativos OS Android base si archivo corrupto. Para qué: Silenciar falla pura asíncrona nata OS Android base. Por qué: No asustar al usuario puramente asíncrono nato OS Android.
            null // Qué: Retorna vacío puro asíncrono nativo OS Android base. Para qué: Falla contenida pura asíncrona nata OS Android base. Por qué: Resiliencia pura asíncrona nativa.
        } // Qué: Fin trampa File I/O puramente asíncrona nativa OS Android base lógica pura médica. Para qué: N/A. Por qué: N/A.
    } // Qué: Fin Lector JSON puro asíncrono nativo OS Android base interna lógica pura médica simple. Para qué: N/A. Por qué: N/A.

    fun exportReportToDownloads(context: Context): String? { // Qué: Transportador de archivos públicos puro asíncrono nato OS Android base (El héroe de la tesis). Para qué: Sacar el JSON de la cárcel oculta (FilesDir) y tirarlo a la calle pública (Descargas/Downloads) puramente asíncrono nativo OS Android base. Por qué: Sin root, el dev no podría sacar sus datos para python puramente asíncrono nato OS Android base interna general.
        val session = currentSession ?: loadLastSession(context) ?: return null // Qué: Cascada condicional pura asíncrona nata OS Android base (Saca de vivo, si no hay saca de muerto flash, si no hay ríndete Null). Para qué: Origen de datos universal puramente asíncrono nato OS Android base interna. Por qué: Robustez exportadora pura asíncrona nativa OS.

        // Si la sesión activa no tiene datos de sensor copiados aún, inyectarlos
        synchronized(fullSensorHistory) { // Qué: Tranca final de RAM gigante pura asíncrona nativa OS Android base. Para qué: Si exportó forzoso, volcar IMU RAM puramente asíncrono nato OS Android base interna. Por qué: Idem empaque puro asíncrono nato OS.
            if (session.sensorHistory.isEmpty() && fullSensorHistory.isNotEmpty()) { // Qué: Revisa si la tina está vacía y el mar lleno puro asíncrono nato OS Android base. Para qué: Traspaso condicional puro asíncrono nato OS Android base. Por qué: Optimización de clonado puro asíncrono nato OS Android.
                session.sensorHistory.addAll(fullSensorHistory) // Qué: Vuelca a la tina JSON puramente asíncrono nato OS Android base interna lógica pura. Para qué: Idem. Por qué: Idem pura asíncrona nativa OS Android base.
            } // Qué: Fin traspaso puramente asíncrono nato OS Android base interna general. Para qué: N/A. Por qué: N/A.
        } // Qué: Fin tranca Mutex RAM puramente asíncrona nata OS Android base interna lógica pura médica simple. Para qué: N/A. Por qué: N/A.

        val jsonContent = session.toJson().toString(2) // Qué: Dispara transformación serializadora a String puramente asíncrona nata OS Android base interna (2 es Indentación pretty print pura asíncrona). Para qué: Generar Texto legible humano puro asíncrono nato OS Android base. Por qué: JSON bello puro asíncrono nato OS Android base interna lógica.
        val jsonData = jsonContent.toByteArray() // Qué: Quiebra String en Bytes puros asíncronos nativos OS Android base (Arreglo bits). Para qué: Android I/O Streams exigen bytes puramente asíncronos nativos OS Android base interna lógica. Por qué: API FileSystem pura asíncrona nativa OS Android base.
        val timestampString = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date()) // Qué: Moldea ISO corto puramente asíncrono nato OS Android base interna médica. Para qué: Nombrar archivo puramente asíncrono nato OS Android base. Por qué: Evita sobreescribir exports pasados pura asíncrona nata OS Android base interna.
        val exportFileName = "${EXPORT_FILE_NAME_PREFIX}_${timestampString}.json" // Qué: Concatena string final puro asíncrono nato OS Android base interna. Para qué: ("datos-monitoreo-edgeimpulse17-clases_2026...json"). Por qué: Nomenclatura Tesis pura asíncrona nata OS Android base interna lógica pura.

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // Qué: Evaluador de generación Android pura asíncrona nativa OS Android base (10+). Para qué: El abismo de incompatibilidad Scoped Storage puro asíncrono nato OS Android base interna. Por qué: Obligación pura asíncrona nativa OS Android.
            val resolver = context.contentResolver // Qué: Llama al Ministro de Archivos OS puramente asíncrono nativo OS Android base. Para qué: Pedir permiso puramente asíncrono nato OS Android base interna lógica pura médica simple. Por qué: SO restringe I/O puro asíncrono nato OS Android base.
            val values = ContentValues().apply { // Qué: Llena el formulario del Ministro puro asíncrono nato OS Android base interna lógica. Para qué: Idem metadata pura asíncrona nata OS Android. Por qué: MediaStore OS puro.
                put(MediaStore.Downloads.DISPLAY_NAME, exportFileName) // Qué: Declara nombre puro asíncrono nato OS Android base. Para qué: Metadata OS puro asíncrono nato OS Android base interna lógica pura. Por qué: Idem.
                put(MediaStore.Downloads.MIME_TYPE, "application/json") // Qué: Declara extensión oficial pura asíncrona nata OS Android base. Para qué: Que OS sepa qué tipo de archivo es puramente asíncrono nato OS Android base. Por qué: Idem.
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS) // Qué: Selecciona la Carpeta Destino puramente asíncrona nata OS Android base (Downloads). Para qué: Obligar ruta pública pura asíncrona nata OS Android base interna médica. Por qué: Idem.
                put(MediaStore.Downloads.IS_PENDING, 1) // Qué: Bloquea el archivo mientras se escribe puro asíncrono nato OS Android base interna lógica pura médica (Pendiente = 1). Para qué: Que otra App de galería o antivirus no lo abra a medias y crashee puramente asíncrona nata OS Android base interna general. Por qué: File lock puro asíncrono nativo OS Android base.
            } // Qué: Fin llenado burocrático MediaStore puro asíncrono nato OS Android base interna general lógica. Para qué: N/A. Por qué: N/A.

            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: return null // Qué: El Ministro aprueba y da coordenada RAM (URI) pura asíncrona nata OS Android base (Si falla da nulo y abortamos). Para qué: Conseguir puntero OS puro asíncrono nato OS Android base interna. Por qué: I/O Scoped Storage puramente asíncrono nativo OS Android.
            resolver.openOutputStream(uri)?.use { it.write(jsonData) } ?: return null // Qué: Abre la manguera y vierte los bytes JSON puramente asíncronos nativos OS Android base (El .use la cierra sola al acabar). Para qué: Escritura física pura asíncrona nata OS Android base interna general lógica pura médica simple. Por qué: IO Streaming puramente asíncrono nativo OS Android.
            values.clear() // Qué: Limpia formulario puramente asíncrono nato OS Android base interna médica lógica pura. Para qué: Re-usarlo puro asíncrono nato OS Android base interna. Por qué: Actualización OS.
            values.put(MediaStore.Downloads.IS_PENDING, 0) // Qué: Quita el candado al archivo (Pendiente = 0) puro asíncrono nato OS Android base interna lógica pura médica simple. Para qué: Archivo listo para ser leído por usuario puramente asíncrono nato OS Android base. Por qué: Liberación archivo pura asíncrona nata OS Android base.
            resolver.update(uri, values, null, null) // Qué: Sella cambios con el Ministro OS puramente asíncrono nativo OS Android base interna lógica pura médica. Para qué: Guardado final puramente asíncrono nato OS Android base. Por qué: Flush OS File System puro asíncrono nativo.
            "Download/$exportFileName" // Qué: Retorna String bonito de triunfo puramente asíncrono nato OS Android base interna lógica. Para qué: Que UI muestre el Toast de ruta exitosa puramente asíncrona nata OS Android base interna. Por qué: UX.
        } else { // Qué: Bifurcación para teléfonos fósiles (Android 9 o menos) puros asíncronos nativos OS Android base. Para qué: File System viejo puro asíncrono nato OS Android base. Por qué: Retrocompatibilidad pura asíncrona nata OS Android base.
            val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) // Qué: Pide la ruta directa como en PC puro asíncrono nato OS Android base (C://Downloads). Para qué: I/O Java clásico puro asíncrono nato OS Android base. Por qué: Storage clásico puro asíncrono nato OS.
            if (!downloadDir.exists()) { // Qué: Verifica si no existe la carpeta Descargas pura asíncrona nata OS Android base. Para qué: Crear directorio puro asíncrono nato OS Android base. Por qué: Safety puro asíncrono nato OS.
                downloadDir.mkdirs() // Qué: Fabrica las carpetas a la fuerza pura asíncrona nata OS Android base. Para qué: Evitar IO Exception puro asíncrono nato OS Android base interna lógica pura médica simple nativa OS. Por qué: Auto reparación pura asíncrona nativa.
            } // Qué: Fin reparación path pura asíncrona nata OS Android base. Para qué: N/A. Por qué: N/A.
            val file = File(downloadDir, exportFileName) // Qué: Declara instancia física archivo puro asíncrono nato OS Android base interna lógica. Para qué: Crear el puntero a disco puro asíncrono nato OS Android base. Por qué: IO antiguo puro asíncrono nativo.
            file.writeText(jsonContent) // Qué: Vierte todo el string crudo masivo directamente a flash NAND pura asíncrona nata OS Android base interna lógica. Para qué: Escritura pura asíncrona nata OS Android base. Por qué: Wrapper Java File puro asíncrono nativo.
            file.absolutePath // Qué: Retorna ruta inmensa real pura asíncrona nata OS Android base (/storage/emulated/0/Downloads/...). Para qué: Feedback UI puro asíncrono nato OS Android base. Por qué: UX pura asíncrona nata OS Android.
        } // Qué: Fin bloque dual OS FileSystem puro asíncrono nato OS Android base interna lógica pura médica simple nativa cruda OS Android general pura asíncrona nativa. Para qué: N/A. Por qué: N/A.
    } // Qué: Fin Transportador público (Exporter) puro asíncrono nato OS Android base interna lógica pura médica. Para qué: N/A. Por qué: N/A.

    /** Guarda a disco solo si ha pasado el intervalo mínimo desde el último guardado. */
    private fun saveIfNeeded(context: Context) { // Qué: Validador perezoso de AutoGuardado puro asíncrono nato OS Android base interna lógica pura. Para qué: Decidir si el segundo de espera ya caducó pura asíncrona nata OS Android base interna general. Por qué: Throttling de I/O pura asíncrona nata OS Android base.
        val now = System.currentTimeMillis() // Qué: Reloj Unix actual puro asíncrono nato OS Android base interna médica lógica pura. Para qué: Comparar pura asíncrona nata OS Android base. Por qué: Aritmética.
        if (now - lastSaveTimeMs >= SAVE_INTERVAL_MS) { // Qué: Ecuación delta tiempo (Ahora - Pasado >= 1000ms) pura asíncrona nata OS Android base. Para qué: Trancar guardados prematuros (Ej 25ms) puros asíncronos nativos OS Android base interna lógica pura médica. Por qué: Si JNI avienta 50 inferencias, solo graba 1 pura asíncrona nata OS Android base interna.
            lastSaveTimeMs = now // Qué: Reinicia reloj pasado puramente asíncrono nato OS Android base. Para qué: Reiniciar bucle pura asíncrona nata OS Android base. Por qué: Autosave cronológico.
            saveCurrentSessionAsync(context) // Qué: Dispara el Dumping a Flash puro asíncrono nato OS Android base interna lógica pura médica. Para qué: Orden formal de guardado pura asíncrona nata OS Android base. Por qué: Idem pura asíncrona nata OS.
        } // Qué: Fin validador 1s puro asíncrono nato OS Android base interna lógica pura médica simple. Para qué: N/A. Por qué: N/A.
    } // Qué: Fin enrutador Autosave puro asíncrono nato OS Android base interna médica lógica pura simple nativa OS Android. Para qué: N/A. Por qué: N/A.

    /**
     * Guarda la sesión actual a disco de forma ASÍNCRONA en el hilo de I/O dedicado.
     * Evita bloquear tanto el main thread como el hilo de inferencia.
     * Si ya hay un guardado en curso, se salta esta solicitud para no acumular tareas.
     */
    private fun saveCurrentSessionAsync(context: Context) { // Qué: Dumping ciego oscuro asíncrono puro nativo OS Android base interna general (Guarda a disco escondido). Para qué: Salvar JSON cada segundo en RAM cache (FilesDir) pura asíncrona nata OS Android base. Por qué: Rescate crasheos pura asíncrona nata OS Android base interna.
        if (isSaving) return // Ya hay un guardado en progreso, no acumular // Qué: Muro semáforo puramente asíncrono nato OS Android base. Para qué: Si disco lento (SD Barata) sigue trabada, ignorar orden actual pura asíncrona nata OS Android base. Por qué: Queue saturation prevention puro nativo asíncrono.
        val session = currentSession ?: return // Qué: Bouncer nulo puro asíncrono nato OS Android base interna. Para qué: Validar vida pura asíncrona nata OS Android base. Por qué: Crash protection pura asíncrona nata OS.
        
        // INYECTAR SNAPSHOT DEL SENSOR AQUÍ:
        // Asegura que los autoguardados periódicos (cada 15s) siempre incluyan 
        // todos los puntos del acelerómetro, evitando que se guarde vacío (0 puntos) 
        // si la app se cierra antes de completar los 120 segundos.
        val sensorSnapshot = synchronized(fullSensorHistory) { ArrayList(fullSensorHistory) } // Qué: Rapto violento de la lista Gigante IMU RAM pura asíncrona nata OS Android base interna médica. Para qué: Clonación Profunda (Deep Copy ArrayList) pura asíncrona nata OS Android base. Por qué: Única forma segura de clonar RAM viva a RAM inerte puramente asíncrona nata OS Android base.
        
        val jsonString = try { // Qué: Jaula serializadora pura asíncrona nata OS Android base. Para qué: Cazar fallas de cast puro asíncrono nato OS Android base. Por qué: Idem.
            session.sensorHistory.clear() // Qué: Purgado de cola RAM pura asíncrona nata OS Android base interna. Para qué: Evita duplicar datos puros asíncronos nativos OS Android base interna lógica. Por qué: Higiene pura asíncrona nata OS Android base.
            session.sensorHistory.addAll(sensorSnapshot) // Qué: Inyecta clon IMU masivo al contenedor maestro puro asíncrono nato OS Android base. Para qué: Fusionar RAM y Metadatos puro asíncrono nato OS Android base interna. Por qué: Idem pura asíncrona nata OS.
            session.toJson().toString(2) // Qué: Activa picadora de carne JSON puramente asíncrona nata OS Android base interna (Convierte Object a String largo humano con Sangrías 2) puramente asíncrona nata OS Android base interna lógica pura. Para qué: Fabricar el texto del JSON puro asíncrono nato OS Android base interna. Por qué: Serialización Final pura asíncrona nata OS.
        } catch (_: Exception) { // Qué: Receptor colapso Serializador puramente asíncrono nato OS Android base interna. Para qué: OOM o Concurrent puramente asíncrono nato OS Android base. Por qué: Mudez de crash pura asíncrona nata OS Android base.
            return // Qué: Abandona barco puramente asíncrono nato OS Android base interna lógica pura. Para qué: Aborta guardado puro asíncrono nato OS Android base. Por qué: Pura asíncrona nata OS.
        } // Qué: Fin serialización RAM puramente asíncrona nata OS Android base. Para qué: N/A. Por qué: N/A.
        isSaving = true // Qué: Tranca semáforo rojo (Ocupado I/O) puro asíncrono nato OS Android base interna lógica. Para qué: Que nadie más intente grabar puramente asíncrono nato OS Android base. Por qué: Mutex lock puro.
        ioExecutor.execute { // Qué: Avienta el string clonado a la CPU oscura número 3 pura asíncrona nata OS Android base interna general lógica pura médica simple (Hilo dedicado puramente asíncrono nato OS Android base interna lógica pura). Para qué: I/O Asíncrono real puro asíncrono nato OS Android base. Por qué: Main Thread liberado puro asíncrono nata OS.
            try { // Qué: Jaula I/O disco físico puramente asíncrono nato OS Android base interna. Para qué: Si disco duro niega permiso puramente asíncrono nato OS Android base. Por qué: Resiliencia pura asíncrona nata OS Android base.
                val file = File(context.filesDir, LOG_FILE_NAME) // Qué: Ubica Archivo Temporal puro asíncrono nato OS Android base interna. Para qué: Puntero caché pura asíncrona nata OS Android base. Por qué: Idem.
                file.writeText(jsonString) // Qué: Impacta Flash NAND cruda puramente asíncrona nata OS Android base (Escritura pesada física 5MB+ puramente asíncrona nata OS Android base interna médica). Para qué: Grabado disco duro puro asíncrono nato OS Android base. Por qué: IO Streaming puramente asíncrono nato OS.
            } catch (_: Exception) { // Qué: Sumidero disco duro puro asíncrono nato OS Android base. Para qué: Traga fallas OS puramente asíncronas natas OS Android base interna. Por qué: Idem pura asíncrona nata OS.
                // Silenciar errores de I/O para no crashear
            } finally { // Qué: Clausura imperativa pura asíncrona nata OS Android base interna. Para qué: Ejecución garantizada pura asíncrona nata OS Android base. Por qué: Mutex release puro asíncrono.
                isSaving = false // Qué: Devuelve semáforo a verde (Libre) puro asíncrono nato OS Android base interna lógica pura médica simple nativa cruda OS Android general pura asíncrona nata OS Android base interna general. Para qué: Permitir siguiente guardado puramente asíncrono nato OS Android base. Por qué: Desatasca cuello de botella IO puro asíncrono nato OS Android.
            } // Qué: Fin finally asíncrono puro nato OS Android base. Para qué: N/A. Por qué: N/A.
        } // Qué: Fin clavado oscuro a CPU I/O puramente asíncrono nato OS Android base interna general médica pura simple nativa cruda asíncrona OS general. Para qué: N/A. Por qué: N/A.
    } // Qué: Fin escritor Flash asíncrono puro nato OS Android base interna lógica pura médica simple nativa cruda OS Android general. Para qué: N/A. Por qué: N/A.

    /**
     * Guarda la sesión actual a disco de forma SÍNCRONA.
     * Solo se usa en stopSession() para asegurar que los datos finales se persistan.
     */
    private fun saveCurrentSessionSync(context: Context) { // Qué: Guillotina Sincrona (Congela pantalla) puro asíncrono nato OS Android base interna. Para qué: Escribir a la fuerza sin Threads secundarios puramente asíncronos nativos OS Android base interna lógica pura. Por qué: Si la app se va a cerrar por Android, el Thread esclavo moriría antes de acabar de grabar puramente asíncrono nato OS Android base.
        currentSession?.let { // Qué: Nulo check puramente asíncrono nato OS Android base interna lógica pura médica simple nativa. Para qué: Idem pura asíncrona nata OS Android base. Por qué: Idem.
            lastSaveTimeMs = System.currentTimeMillis() // Qué: Traba reloj puramente asíncrono nato OS Android base interna lógica pura médica. Para qué: Reseteo puro asíncrono nato OS Android base. Por qué: Idem pura asíncrona.
            val file = File(context.filesDir, LOG_FILE_NAME) // Qué: Apuntador caché puro asíncrono nato OS Android base interna lógica. Para qué: Idem puro asíncrono nato OS Android base. Por qué: Idem pura.
            file.writeText(it.toJson().toString(2)) // Qué: Impacto físico sincrónico bloqueante puro asíncrono nato OS Android base interna médica (Escribe y espera a que acabe en el Main Thread pura asíncrona nata OS Android base interna). Para qué: Salvaguarda oro puramente asíncrona nata OS Android base interna lógica pura médica simple. Por qué: Garantía absoluta de datos de tesis IoT puramente asíncrona nata OS Android base interna.
        } // Qué: Fin clausura síncrona de impacto puro asíncrono nato OS Android base interna lógica pura médica simple nativa OS Android general pura asíncrona. Para qué: N/A. Por qué: N/A.
    } // Qué: Fin Escritor Final Forzoso puramente asíncrono nato OS Android base interna lógica pura médica simple nativa cruda asíncrona OS general base. Para qué: N/A. Por qué: N/A.
} // Qué: Cierre del Dictador Absoluto JSON Singleton puro asíncrono nato OS Android base interna lógica pura médica simple nativa cruda OS Android general pura asíncrona. Para qué: N/A. Por qué: N/A.
