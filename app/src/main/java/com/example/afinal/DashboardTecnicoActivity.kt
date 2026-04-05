package com.example.afinal

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import kotlin.math.sqrt

class DashboardTecnicoActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var acelerometro: Sensor? = null
    private var aceleracionActual = SensorManager.GRAVITY_EARTH
    private var aceleracionUltima = SensorManager.GRAVITY_EARTH
    private var variacionAceleracion = 0f
    private var ultimoTiempoSacudida: Long = 0
    private var nombreUsuario = ""

    // El vigilante de paros
    private lateinit var notifier: RealTimeNotifier

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_tecnico)

        // 1. RECIBIR DATOS DEL LOGIN
        val userId = intent.getIntExtra("USER_ID", 1) // <--- CLAVE PARA EL NOTIFICADOR
        nombreUsuario = intent.getStringExtra("USER_NAME") ?: "Usuario"
        val puestoUsuario = intent.getStringExtra("USER_ROLE") ?: "Técnico SMT"

        val tvUserName = findViewById<TextView>(R.id.tvUserName)
        val tvUserRole = findViewById<TextView>(R.id.tvUserRole)
        tvUserName.text = nombreUsuario
        tvUserRole.text = puestoUsuario

        // 2. INICIALIZAR NOTIFIER (CORREGIDO CON USER_ID QUitado del paréntesis)
        notifier = RealTimeNotifier { titulo, mensaje ->
            // Alerta visual para el técnico
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(titulo)
                .setMessage(mensaje)
                .setPositiveButton("ENTENDIDO", null)
                .show()
        }

        // 3. CONFIGURAR SENSORES
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        acelerometro = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // 4. CLICKS
        val cardReportar = findViewById<CardView>(R.id.cardReportarFalla)
        val cardHistorial = findViewById<CardView>(R.id.cardHistorialReportes)
        val btnCerrarSesion = findViewById<ImageView>(R.id.btnCerrarSesion)
        val cardParos = findViewById<CardView>(R.id.cardParosTecnico)

        cardReportar?.setOnClickListener { abrirReporteFalla() }
        cardHistorial?.setOnClickListener { startActivity(Intent(this, ListaFallasActivity::class.java)) }
        cardParos?.setOnClickListener { startActivity(Intent(this, HistorialParosActivity::class.java)) }

        btnCerrarSesion?.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun abrirReporteFalla() {
        val i = Intent(this, ReporteTecnicoActivity::class.java)
        i.putExtra("TECNICO_NOMBRE", nombreUsuario)
        startActivity(i)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            aceleracionUltima = aceleracionActual
            aceleracionActual = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
            val delta = aceleracionActual - aceleracionUltima
            variacionAceleracion = variacionAceleracion * 0.9f + delta

            if (variacionAceleracion > 12) {
                val tiempoActual = System.currentTimeMillis()
                if (tiempoActual - ultimoTiempoSacudida > 2000) {
                    ultimoTiempoSacudida = tiempoActual
                    abrirReporteFalla()
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onResume() {
        super.onResume()
        notifier.start()
        acelerometro?.also { acc ->
            sensorManager.registerListener(this, acc, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        notifier.stop()
        sensorManager.unregisterListener(this)
    }
}