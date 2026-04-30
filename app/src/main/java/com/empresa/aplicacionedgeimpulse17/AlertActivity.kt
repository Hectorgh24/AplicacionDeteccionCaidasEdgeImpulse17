package com.empresa.aplicacionedgeimpulse17

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.telephony.SmsManager
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AlertActivity : AppCompatActivity() {

    private lateinit var tvCountdown: TextView
    private lateinit var tvFallType: TextView
    private lateinit var btnCancelAlert: Button
    
    private var fallTimer: CountDownTimer? = null
    private var mediaPlayer: MediaPlayer? = null
    
    private var phone: String = ""
    private var fallType: String = ""

    companion object {
        const val EXTRA_PHONE = "EXTRA_PHONE"
        const val EXTRA_FALL_TYPE = "EXTRA_FALL_TYPE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alert)

        tvCountdown = findViewById(R.id.tvCountdown)
        tvFallType = findViewById(R.id.tvFallType)
        btnCancelAlert = findViewById(R.id.btnCancelAlert)

        phone = intent.getStringExtra(EXTRA_PHONE) ?: ""
        fallType = intent.getStringExtra(EXTRA_FALL_TYPE) ?: "Desconocida"

        tvFallType.text = fallType

        // Alarma de sonido (usando sonido de notificacion por defecto o un beep simple)
        // Puedes reemplazar esto con un archivo en res/raw/ si lo tienes
        try {
            val notification = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_ALARM)
            mediaPlayer = MediaPlayer.create(this, notification)
            mediaPlayer?.isLooping = true
            mediaPlayer?.start()
        } catch (e: Exception) {
            Log.e("AlertActivity", "Error reproduciendo sonido: ${e.message}")
        }

        btnCancelAlert.setOnClickListener {
            cancelAlert()
        }

        startCountdown()
    }

    private fun startCountdown() {
        fallTimer = object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val sec = millisUntilFinished / 1000
                tvCountdown.text = sec.toString()
            }

            override fun onFinish() {
                tvCountdown.text = "0"
                executeEmergencyProtocol()
            }
        }.start()
    }

    private fun cancelAlert() {
        fallTimer?.cancel()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        Toast.makeText(this, "Alerta cancelada", Toast.LENGTH_SHORT).show()
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    private fun executeEmergencyProtocol() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        
        Toast.makeText(this, "Ejecutando protocolo de emergencia...", Toast.LENGTH_LONG).show()

        val message = "🚨 *SOS: ALERTA DE EMERGENCIA* 🚨\nSufrí una posible caída o accidente ($fallType). Por favor, comunícate conmigo de inmediato o envía ayuda."
        
        // 1. Enviar SMS automáticamente de fondo (Garantiza el envío automático sin que el usuario deba presionar Enviar)
        try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phone, null, message, null, null)
            Log.i("AlertActivity", "SMS automático enviado a $phone")
        } catch (e: Exception) {
            Log.e("AlertActivity", "Error enviando SMS: ${e.message}")
        }

        // 2. Hacer la llamada telefónica automáticamente
        try {
            val callIntent = Intent(Intent.ACTION_CALL)
            callIntent.data = Uri.parse("tel:$phone")
            startActivity(callIntent)
            Log.i("AlertActivity", "Llamada iniciada a $phone")
        } catch (e: SecurityException) {
            Log.e("AlertActivity", "Permiso de llamada denegado.")
        } catch (e: Exception) {
            Log.e("AlertActivity", "Error al llamar: ${e.message}")
        }

        // 3. Dejar listo WhatsApp con el mensaje en caso de que quieran enviarlo por ahí también
        try {
            val waIntent = Intent(Intent.ACTION_VIEW)
            val phoneWa = "52$phone" // Agregar 52 fijo ya que asumimos México
            waIntent.data = Uri.parse("https://api.whatsapp.com/send?phone=$phoneWa&text=${Uri.encode(message)}")
            startActivity(waIntent)
            Log.i("AlertActivity", "WhatsApp abierto para $phoneWa")
        } catch (e: Exception) {
            Log.e("AlertActivity", "Error abriendo WhatsApp: ${e.message}")
        }

        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        fallTimer?.cancel()
        mediaPlayer?.stop()
        mediaPlayer?.release()
    }
}
