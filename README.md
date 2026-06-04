# Aplicación de Detección de Caídas (Edge Impulse - 17 Clases)

Esta aplicación para Android tiene como propósito fundamental monitorear constantemente los movimientos del usuario a través del acelerómetro del dispositivo y detectar si ha ocurrido una caída mediante un modelo de Inteligencia Artificial (Machine Learning) entrenado en la plataforma **Edge Impulse**.

Una vez que se detecta una caída con una fiabilidad alta (mayor o igual al 85%), la aplicación despliega una pantalla de alerta roja con una alarma sonora y una cuenta regresiva de 5 segundos. Si la alerta no es cancelada manualmente, el sistema ejecuta un protocolo de emergencia para solicitar ayuda automáticamente a un número de teléfono de 10 dígitos (México) configurado por el usuario.

## Índice
1. [Características Principales](#-características-principales)
2. [Clases Detectadas](#-clases-detectadas)
3. [Recursos y Tecnologías Utilizadas](#️-recursos-y-tecnologías-utilizadas)
4. [Documentación Importante y Permisos](#️-documentación-importante-y-permisos)
5. [Monitoreo y Visualización de Gráficos](#-monitoreo-y-visualización-de-gráficos)
6. [Optimizaciones de Rendimiento y Estabilidad](#-optimizaciones-de-rendimiento-y-estabilidad)
7. [Registro y Exportación de Reportes](#-registro-y-exportación-de-reportes)
8. [Herramienta Python de Reconstrucción Visual (JSON a MP4)](#-herramienta-python-de-reconstrucción-visual-json-a-mp4)

## 🚀 Características Principales

- **Monitoreo en Tiempo Real:** Utiliza el sensor acelerómetro del dispositivo para recopilar datos de movimiento de forma ininterrumpida.
- **Inferencia Local (Offline):** El modelo de Machine Learning (TensorFlow Lite) corre completamente de forma local en el dispositivo usando C++ (NDK), lo que significa que no requiere conexión a internet para detectar caídas.
- **Detección de 17 Clases Diferentes:** El modelo es capaz de distinguir entre diversas actividades diarias y distintos tipos de caídas.
- **Protocolo de Emergencia Híbrido:** Al confirmar una caída, la app realiza tres acciones simultáneas:
  1. Envía un **SMS automático** con la alerta (Garantiza el envío sin interacción).
  2. Inicia una **Llamada telefónica automática**.
  3. Abre **WhatsApp** con un mensaje pre-escrito listo para enviar en caso de ser necesario.

## 📚 Clases Detectadas

El modelo está entrenado para reconocer las siguientes 17 actividades/caídas:

**Tipos de Caídas (Disparan la Alerta):**
1. **Caída hacia atrás** (`fall_backward`)
2. **Caída doblándose** (`fall_bending`)
3. **Caída hacia adelante** (`fall_forward`)
4. **Caída sobre manos** (`fall_hand`)
5. **Caída lateral izquierda** (`fall_sideward_left`)
6. **Caída lateral derecha** (`fall_sideward_right`)
7. **Caída sentado** (`fall_sitting`)
8. **Caída por desmayo (síncope)** (`fall_syncope`)

**Actividades Diarias (Movimiento normal):**
9. **Bajando escaleras** (`going_down_stairs`)
10. **Subiendo escaleras** (`going_up_stairs`)
11. **Saltando** (`jump`)
12. **Acostándose (FS)** (`lying_down_fs`)
13. **Corriendo** (`run`)
14. **Sentándose** (`sitting_down`)
15. **Levantándose (FL)** (`standing_up_fl`)
16. **Levantándose (FS)** (`standing_up_fs`)
17. **Caminando** (`walk`)

## 🛠️ Recursos y Tecnologías Utilizadas

- **Kotlin:** Lenguaje de programación principal para el desarrollo de la interfaz (UI) y la lógica de negocio en Android.
- **Android NDK y CMake:** Herramientas utilizadas para compilar y vincular el código fuente en C/C++ como una librería dinámica (`libaplicacionedgeimpulse17.so`).
- **Edge Impulse C++ SDK:** El SDK oficial generado por Edge Impulse que contiene el código matemático y de procesamiento de señales (DSP) para preparar los datos.
- **TensorFlow Lite (TFLite):** Motor de inferencia integrado dentro del SDK de Edge Impulse para procesar la red neuronal en el dispositivo móvil de manera óptima.
- **Android SensorManager:** API de Android utilizada para acceder al sensor físico `TYPE_ACCELEROMETER`. El modelo está diseñado para ventanas de entrada de 300 datos (100 muestras x 3 ejes: X, Y, Z).
- **SmsManager:** API de Android nativa para el envío silencioso y automatizado de mensajes de texto SMS.
- **Intents (Deep Linking):** Sistema nativo de comunicación entre apps usado para lanzar las llamadas (`ACTION_CALL`) y abrir WhatsApp (`ACTION_VIEW`).
- **MPAndroidChart:** Librería de gráficos para Android utilizada para mostrar los datos del acelerómetro y las predicciones en tiempo real.

## ⚠️ Documentación Importante y Permisos

Para que el protocolo de emergencia y el monitoreo funcionen correctamente, la aplicación requiere de permisos estrictos. Permisos definidos en `AndroidManifest.xml`:

- **`SEND_SMS`**: Esencial para poder despachar el SMS de emergencia de forma 100% automática y en segundo plano sin que el usuario tenga que interactuar con la pantalla en el momento de una posible incapacidad (caída).
- **`CALL_PHONE`**: Necesario para iniciar la llamada telefónica sin mostrar el marcador (dialer) manual.
- **`WAKE_LOCK`**: Permite mantener la CPU activa cuando la pantalla está apagada, garantizando que el monitoreo no se interrumpa durante la sesión de 120 segundos.

### Limitación de Seguridad de WhatsApp
El protocolo de WhatsApp funciona a través del enlace estructurado (`api.whatsapp.com/send`). **WhatsApp no permite que terceras aplicaciones envíen mensajes automáticamente (zero-click)** por políticas anti-spam y seguridad del usuario. Por lo tanto, la app abrirá WhatsApp y dejará el mensaje de texto completamente redactado en la caja de conversación del contacto, pero requiere que el usuario presione el botón de "Enviar". Es por esto que el **SMS automático** actúa como medida de contingencia principal que sí se envía solo.

## 📊 Monitoreo y Visualización de Gráficos

La pantalla de ajustes (`SettingsActivity`) cuenta con un dashboard visual interactivo compuesto por dos gráficos principales que se actualizan en tiempo real sin congelamientos:

1. **Gráfico de Línea de Tiempo (Scatter Plot)**: Presenta un historial interactivo de todas las predicciones arrojadas por el modelo Edge Impulse, mapeando el tiempo de operación contra la actividad o caída detectada. Las 17 clases se muestran en el eje Y con sus nombres traducidos al español. Soporta scroll horizontal y zoom.

2. **Gráfico de Acelerómetro (Line Chart)**: Muestra los valores de los ejes X (rojo), Y (verde) y Z (azul) capturados por el sensor en una ventana deslizante de 10 segundos. Permite observar la física de los movimientos en tiempo real con auto-scroll.

Ambos gráficos utilizan la librería **MPAndroidChart** y se refrescan cada segundo mediante un `Handler` que lee snapshots throttleados de los datos del sensor.

## ⚡ Optimizaciones de Rendimiento y Estabilidad

Mejoras implementadas para garantizar una operación confiable del monitoreo y una experiencia de usuario fluida:

### Temporizador de sesión de 2 minutos
- Cada sesión de monitoreo dura exactamente **120 segundos** y se auto-detiene al finalizar.
- Se muestra un temporizador visual en la pantalla principal (`MainActivity`) con formato `M:SS`.
- Implementado con `CountDownTimer` que actualiza tanto la UI como `MonitoringLogManager.remainingSeconds`.
- Al llegar a 0, el monitoreo se detiene limpiamente guardando todos los datos, y se muestra un mensaje indicando que el usuario puede exportar los datos desde Ajustes.

### WakeLock parcial para ejecución con pantalla apagada
- Se adquiere un `PARTIAL_WAKE_LOCK` al iniciar el monitoreo para mantener la CPU activa incluso con la pantalla apagada.
- Esto garantiza que el `CountDownTimer`, el `SensorEventListener` y la recolección de datos continúen funcionando sin interrupciones durante los 120 segundos completos.
- Timeout de seguridad de 3 minutos para evitar fugas de recursos en caso de error.
- Se liberan correctamente tanto en `stopMonitoring()` como en `onDestroy()`.
- Se agregó el permiso `WAKE_LOCK` al `AndroidManifest.xml`.

### Optimización Extrema de Rendimiento (Anti-Congelamiento v2)

Se identificó y resolvió un bug crítico donde la aplicación se congelaba completamente alrededor del segundo 8-9 de monitoreo: los gráficos de clasificación y acelerómetro dejaban de actualizarse, y la detección de caídas se detenía (sin alertas), aunque el cronómetro de sesión seguía avanzando hasta los 120 segundos. A continuación se documenta la causa raíz y todas las correcciones aplicadas:

#### Causa Raíz: Saturación del Executor de Inferencia

El modelo Edge Impulse de 17 clases ejecuta inferencia en C++ vía JNI, lo cual puede tomar más de 2 segundos por ventana en dispositivos móviles. Como el buffer del sensor se llena cada ~2 segundos (100 muestras × 3 ejes a ~50Hz), el `ExecutorService` de un solo hilo recibía nuevas tareas de inferencia al mismo ritmo (o más rápido) de lo que podía procesarlas. Esto causaba una **cola infinita de tareas** que:

1. Saturaba la memoria con clones del buffer acumulados en la cola.
2. Impedía que las callbacks de post-inferencia (registro de predicción, detección de caída) se ejecutaran a tiempo.
3. Provocaba presión de GC extrema que terminaba bloqueando la entrega de eventos del sensor por parte del sistema Android.

Al no recibirse más eventos del sensor, el buffer dejaba de llenarse, los gráficos se congelaban y la detección de caídas se detenía permanentemente.

#### Correcciones Implementadas

1. **Gate Atómico Anti-Saturación (`AtomicBoolean`)**: Se agregó un flag `inferenceInProgress` de tipo `AtomicBoolean` en `MainActivity`. Antes de enviar una nueva tarea al executor, se verifica con `compareAndSet(false, true)`. Si la inferencia anterior aún no ha terminado, la ventana actual se **descarta** en vez de acumularse en la cola. El flag se libera en un bloque `finally` para garantizar recuperación incluso ante errores. Esto previene completamente la acumulación de tareas.

2. **I/O de Disco Asíncrono Dedicado**: El guardado periódico a disco (`saveCurrentSession`) se movió a un `ioExecutor` separado del hilo de inferencia. Anteriormente, la serialización JSON y escritura a archivo se ejecutaban dentro del mismo `ExecutorService` de inferencia, retrasándolo aún más. Ahora la inferencia no se bloquea por I/O. Se usa un flag `isSaving` para evitar acumular múltiples escrituras pendientes.

3. **Sincronización Completa de Sesión**: Todos los métodos que mutan `currentSession` via `.copy()` (`recordWindow`, `recordFall`, `recordAlert`, `updatePrediction`, `stopSession`) ahora están protegidos con `@Synchronized` sobre el objeto `MonitoringLogManager`. Esto elimina race conditions entre el hilo principal (sensor), el hilo de inferencia y el hilo de I/O que podían corromper el estado de la sesión.

4. **Pre-dimensionamiento de Buffers**: `fullSensorHistory` se inicializa con capacidad para 7000 entradas (50Hz × 120s + margen) para eliminar los costosos re-dimensionamientos de `ArrayList` que generaban arrays temporales grandes y presión de GC.

5. **Snapshot Atómico (`AtomicReference`)**: El `displaySnapshot` leído por `SettingsActivity` ahora se publica mediante `AtomicReference.set()` en lugar de una asignación volátil simple, garantizando visibilidad thread-safe sin sincronización pesada.

6. **Ring Buffer de Arrays Primitivos (CERO Allocations por Muestra)**: Se reemplazó el `ArrayList<SensorEventData>` del buffer de visualización por arrays primitivos de tamaño fijo (`LongArray`, `FloatArray`) organizados como ring buffer. Esto elimina **completamente** la creación de objetos por cada muestra del sensor durante operación normal, reduciendo la presión de GC a prácticamente cero. Los snapshots para la UI se materializan solo cada 25 muestras (~2Hz).

7. **Diezmado del Historial Completo (50Hz → 25Hz)**: El `fullSensorHistory` ahora guarda 1 de cada 2 muestras del sensor, reduciendo a la mitad la cantidad de objetos `SensorEventData` creados durante la sesión. A 25Hz la resolución sigue siendo más que suficiente para la reconstrucción de gráficos en Python.

8. **Diezmado Adaptativo del Gráfico (max 100 puntos)**: `SettingsActivity.updateSensorChart()` ahora limita el renderizado a máximo 100 puntos por eje (en vez de los ~250-500 anteriores), usando muestreo uniforme del snapshot. Esto reduce el costo de renderizado del chart en un ~80%, liberando el main thread para procesar eventos del sensor sin interrupciones.

9. **Refresh de Gráficos Reducido (2s)**: El `Handler` de `SettingsActivity` ahora refresca los gráficos cada 2 segundos en vez de cada 1 segundo, reduciendo a la mitad la cantidad de ciclos de renderizado que compiten con el sensor por tiempo del main thread.

10. **Desactivación de Hardware Acceleration en Charts**: Se desactivó la aceleración por hardware en ambos gráficos (`setHardwareAccelerationEnabled(false)`) para evitar la acumulación de texturas GPU que causaban fugas de memoria nativa no rastreables por el GC de Java.

11. **Red de Seguridad en `onSensorChanged` (`try-catch`)**: Se envolvió todo el cuerpo de `onSensorChanged` en un `try-catch` para prevenir que cualquier excepción inesperada (ej: `ArrayIndexOutOfBoundsException` por corrupción de estado) mate silenciosamente el listener del sensor. En caso de error, se resetea `bufferIndex` a un valor seguro y el monitoreo continúa.

12. **Thread-Safety en Predicciones**: El historial de predicciones usa `CopyOnWriteArrayList` para permitir iteración segura desde el hilo de UI mientras el hilo de inferencia agrega nuevas entradas sin `ConcurrentModificationException`.

### Exportación completa de datos del acelerómetro
- El reporte JSON ahora incluye el campo `sensorHistory` con **todos** los datos brutos del acelerómetro (offset en ms, ejes X/Y/Z).
- Esto permite reconstruir ambos gráficos exactos en Python usando la herramienta visual incluida.
- El nombre del archivo exportado es `datos-monitoreo-edgeimpulse17-clases.json` para diferenciarlo del proyecto TensorFlow/Keras.

## 📁 Registro y Exportación de Reportes

- La sesión de monitoreo se guarda en `monitoring_log.json` dentro de `filesDir`.
- Desde la pantalla de Ajustes se puede exportar un reporte JSON completo presionando el botón "Descargar Datos para Video (.json)".
- El archivo se guarda en la carpeta **Descargas** del dispositivo con el nombre `datos-monitoreo-edgeimpulse17-clases.json`.
- Datos incluidos en el JSON:
  - Inicio/fin de sesión (timestamp y formato ISO)
  - Duración total en segundos
  - Ventanas (inferencias) procesadas
  - Caídas detectadas y alertas enviadas
  - Número de emergencia configurado
  - Última predicción
  - `predictionHistory`: historial completo de predicciones (segundo + clase)
  - `sensorHistory`: historial completo del acelerómetro (offset ms + X/Y/Z)

## 🐍 Herramienta Python de Reconstrucción Visual (JSON a MP4)

Se incluye un módulo externo de Python (ubicado en la carpeta `python_tools/`) para leer el JSON exportado desde la aplicación y generar animaciones precisas en video.

### Características Técnicas de Generación de Video
- **Alta Definición y Fluidez (1080p a 30 FPS):** La herramienta matemática fue recalibrada para generar videos fluidos animando el recorrido del sensor a 30 cuadros por segundo. Su lienzo está escalado a 16:9 con 120 DPI y 8000 kbps de bitrate, lo que garantiza una salida nítida exactamente a 1920x1080 píxeles, sin textos borrosos.
- **Compatibilidad Universal MP4 (yuv420p):** La herramienta inyecta comandos avanzados a FFmpeg (`-vcodec libx264`, `-pix_fmt yuv420p`, `-profile:v high`) forzando una codificación de color estándar. Esto repara el error típico de matplotlib donde los videos MP4 generados bloquean los controles del reproductor (impidiendo adelantar o atrasar). Ahora son nativamente compatibles con QuickTime, Windows Media y navegadores web.
- **Sin Dependencias de Sistema:** La herramienta instala y utiliza el paquete `imageio-ffmpeg` para descargar un binario portátil de FFmpeg interno, eliminando la necesidad de que el usuario lo instale manualmente en el sistema operativo.
- **Tolerancia a fallos (Fallback a GIF):** Si ocurre alguna excepción crítica al codificar en H.264 (.mp4), el bloque `try-except` captura el fallo y delega la tarea a `PillowWriter` para generar una animación en formato `.gif` de respaldo.
- **Prevención de Bugs Gráficos:** Para evitar los cierres forzados (`array is 1-dimensional`) de Matplotlib al inicializar la gráfica cuando aún no hay puntos detectados, se inyectan matrices bidimensionales vacías mediante `np.empty((0, 2))`.
- **Clases adaptadas a Edge Impulse:** El script usa los nombres crudos de clase del modelo Edge Impulse (`fall_backward`, `walk`, etc.) con traducciones al español para las etiquetas visuales.

### Instalación y Uso Automático

La herramienta contiene lógica de autodescubrimiento. Si falta alguna librería (`matplotlib`, `numpy`, `Pillow`, `imageio-ffmpeg`), invocará a `pip` internamente para instalarla y se reiniciará automáticamente.

1. Entra a la carpeta `python_tools/`.
2. Ejecuta la interfaz gráfica haciendo doble clic o usando la terminal:
```bash
python interfaz_grafica.py
```
3. La herramienta creará automáticamente las carpetas `input_json/` y `output_videos/`.
4. Coloca **solo un archivo JSON** en la carpeta `python_tools/input_json/`.
5. Presiona el botón verde "🚀 GENERAR VIDEOS" en la interfaz.
6. Los videos MP4 generados (`linea_tiempo_monitoreo.mp4` y `acelerometro_monitoreo.mp4`) aparecerán en `output_videos/`.

### Videos Generados
- **`linea_tiempo_monitoreo.mp4`**: Animación del scatter plot que muestra las predicciones en el tiempo. Los puntos cyan representan actividades normales y los rojos representan caídas detectadas.
- **`acelerometro_monitoreo.mp4`**: Animación del gráfico de líneas del acelerómetro mostrando los ejes X (rojo), Y (verde), Z (azul) con ventana deslizante de 10 segundos.

---

Autor: Hector (Licenciatura en Tecnologías Computacionales)  
Última actualización: Junio 2026
