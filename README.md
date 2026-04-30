# Aplicación de Detección de Caídas (Edge Impulse - 17 Clases)

Esta aplicación para Android tiene como propósito fundamental monitorear constantemente los movimientos del usuario a través del acelerómetro del dispositivo y detectar si ha ocurrido una caída mediante un modelo de Inteligencia Artificial (Machine Learning) entrenado en la plataforma **Edge Impulse**.

Una vez que se detecta una caída con una fiabilidad alta (mayor o igual al 85%), la aplicación despliega una pantalla de alerta roja con una alarma sonora y una cuenta regresiva de 5 segundos. Si la alerta no es cancelada manualmente, el sistema ejecuta un protocolo de emergencia para solicitar ayuda automáticamente a un número de teléfono de 10 dígitos (México) configurado por el usuario.

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

## ⚠️ Documentación Importante y Permisos

Para que el protocolo de emergencia funcione, la aplicación requiere de permisos estrictos en tiempo de ejecución. Al abrir la app por primera vez, se solicitará autorización para:
- **`SEND_SMS`**: Esencial para poder despachar el SMS de emergencia de forma 100% automática y en segundo plano sin que el usuario tenga que interactuar con la pantalla en el momento de una posible incapacidad (caída).
- **`CALL_PHONE`**: Necesario para iniciar la llamada telefónica sin mostrar el marcador (dialer) manual.

### Limitación de Seguridad de WhatsApp
El protocolo de WhatsApp funciona a través del enlace estructurado (`api.whatsapp.com/send`). **WhatsApp no permite que terceras aplicaciones envíen mensajes automáticamente (zero-click)** por políticas anti-spam y seguridad del usuario. Por lo tanto, la app abrirá WhatsApp y dejará el mensaje de texto completamente redactado en la caja de conversación del contacto, pero requiere que el usuario presione el botón de "Enviar". Es por esto que el **SMS automático** actúa como medida de contingencia principal que sí se envía solo.
