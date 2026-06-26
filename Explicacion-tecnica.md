# Arquitectura y Flujo de Detección de Caídas: Edge Impulse (17 Clases)

Este documento detalla la estructura lógica y el flujo de datos del repositorio `AplicacionEdgeImpulse17`. A diferencia de los proyectos basados en TensorFlow/Keras, esta aplicación utiliza un SDK pre-compilado en C/C++ proporcionado por Edge Impulse, el cual es ejecutado mediante JNI (Java Native Interface) para lograr una inferencia de ultra-baja latencia (Bare-Metal).

## 1. El Núcleo de Inferencia: C++ SDK vs Keras
En lugar de cargar un archivo `.tflite` derivado de un modelo `.keras`, este proyecto compila directamente el código fuente generado por Edge Impulse (`edge-impulse-sdk`, `model-parameters`, `tflite-model`). 
* **Ausencia de archivo Keras:** El modelo está integrado y hardcodeado dentro del binario nativo de la aplicación (`libaplicacionedgeimpulse17.so`).
* **JNI Bridge:** El archivo `MainActivity.kt` se comunica con el motor en C++ enviándole un arreglo bruto de flotantes (Acelerómetro XYZ) y recibe de vuelta un String con la clasificación.

## 2. Flujo de Datos para la Detección de Clases

El ciclo de vida de la detección sigue una arquitectura de productor-consumidor acoplada directamente a los sensores físicos del dispositivo:

### A. Adquisición (Productor) - `MainActivity.kt (onSensorChanged)`
1. **Muestreo a 50Hz:** El acelerómetro dispara el evento `onSensorChanged` 50 veces por segundo.
2. **Llenado del Buffer:** Los valores X, Y, Z se inyectan en un `FloatArray` plano (Zero-Allocation) hasta completar 300 flotantes (100 muestras * 3 ejes). Esto representa 2 segundos completos de datos.
3. **Sliding Window (Ventana Deslizante):** Una vez que el buffer llega a 300, se realiza una copia (para enviar a la IA) y luego se recorren los datos 150 lugares (1 segundo) hacia atrás. Esto permite que el modelo evalúe ventanas que se solapan (overlap) y entregue una predicción cada 1 segundo.

### B. Inferencia (Procesamiento) - `MainActivity.kt (performInferenceAsync)`
1. **Asincronía Obligatoria:** Para evitar congelar la Interfaz de Usuario (UI), el buffer copiado se envía a un `ExecutorService` (hilo secundario aislado).
2. **Llamada Nativa:** Se invoca la función externa JNI `runClassification(features: FloatArray)`.
3. **Ejecución C++:** La librería dinámica de Edge Impulse ejecuta el DSP (Digital Signal Processing, extracción de características espectrales) y pasa los datos por la Red Neuronal (NN) nativa.
4. **Respuesta:** Devuelve un String simple en formato `clase|confianza` (Ej: `fall_sideward_left|0.98`).

### C. Evaluación (Consumidor) - `MainActivity.kt (Lógica de Decisión)`
1. **Parseo y Traducción:** El string es cortado y convertido a flotante en Kotlin. La etiqueta en inglés se traduce al español usando el mapa `classTranslations`.
2. **Criterio de Alarma:** Se evalúan tres condiciones estrictas (El Juez Supremo):
   * ¿La etiqueta pertenece a la lista negra `FALL_CLASSES` (8 tipos de caída)?
   * ¿La confianza matemática es mayor al umbral del `85%` (`FALL_THRESHOLD`)?
   * ¿El sistema NO está ya pitando una alerta (`!isAlertActive`)?
3. **Detonación SOS:** Si las tres condiciones se cumplen, se dispara un Intent a `AlertActivity.kt` que hace sonar una alarma ensordecedora e inicia el protocolo de pánico (SMS, WakeLock).

## 3. Gestor de Telemetría: `MonitoringLogManager.kt`
Este archivo es el corazón de la tesis IoT. Se encarga de grabar TODO lo que sucede en la memoria RAM hacia el almacenamiento físico Flash del teléfono (JSON).
* **Zero Allocation Arrays:** Utiliza arreglos primitivos estilo C (LongArray, FloatArray) pre-ensanchados a 7000 posiciones. Esto engaña al *Garbage Collector* de Java/Kotlin, impidiendo que pause la aplicación. 
* **Thread-Safety (Multihilo seguro):** Puesto que la UI pinta gráficos a 2Hz, el Acelerómetro lee a 50Hz, C++ infiere a 1Hz y el Logger graba a disco cada 1 segundo, existen múltiples hilos intentando leer y escribir. `MonitoringLogManager` utiliza Mutex (`@Synchronized`) y `CopyOnWriteArrayList` para evitar *ConcurrentModificationException* (el error más letal en arquitecturas multihilo Android).
* **Volcado JSON:** Una vez que los 120 segundos reglamentarios terminan, formatea masivamente la RAM en un archivo estandarizado ISO 8601 y lo expulsa mediante la API nativa de Android 10+ (Scoped Storage) hacia la carpeta pública de Descargas.

## Resumen del Flujo de Ejecución (Pipeline):
1. **Sensor Táctil** -> 50Hz (XYZ) -> **MainActivity**
2. **MainActivity** -> 300 Floats -> **Hilo Secundario (Executor)**
3. **Hilo Secundario** -> `runClassification()` -> **C++ NDK Edge Impulse**
4. **C++ NDK** -> String ("fall_bending|0.89") -> **Hilo Secundario**
5. **Hilo Secundario** -> Validar Confianza (>0.85) -> **Detonación de Alerta**
6. Simultáneamente: **MonitoringLogManager** absorbe RAM y hace dump a archivo JSON cada 1 segundo.
