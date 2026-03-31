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

    // Variables para el Sensor de Movimiento (Sacudida)
    private lateinit var sensorManager: SensorManager
    private var acelerometro: Sensor? = null

    private var aceleracionActual = SensorManager.GRAVITY_EARTH
    private var aceleracionUltima = SensorManager.GRAVITY_EARTH
    private var variacionAceleracion = 0f

    // Evitar que abra la pantalla 10 veces si la sacudida dura mucho
    private var ultimoTiempoSacudida: Long = 0

    // Variables globales para usarlas al agitar el teléfono
    private var nombreUsuario = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_tecnico)

        // 1. RECIBIR DATOS
        nombreUsuario = intent.getStringExtra("USER_NAME") ?: "Usuario"
        val puestoUsuario = intent.getStringExtra("USER_ROLE") ?: "Técnico SMT"

        // 2. ENLAZAR VISTAS
        val tvUserName = findViewById<TextView>(R.id.tvUserName)
        val tvUserRole = findViewById<TextView>(R.id.tvUserRole)
        tvUserName.text = nombreUsuario
        tvUserRole.text = puestoUsuario

        // 3. CONFIGURAR SENSORES
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        acelerometro = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // 4. CONFIGURAR CLICKS
        val cardReportar = findViewById<CardView>(R.id.cardReportarFalla)
        val cardHistorial = findViewById<CardView>(R.id.cardHistorialReportes)
        val btnCerrarSesion = findViewById<ImageView>(R.id.btnCerrarSesion)

        cardReportar?.setOnClickListener {
            abrirReporteFalla()
        }

        cardHistorial?.setOnClickListener {
            startActivity(Intent(this, HistorialFallasActivity::class.java))
        }

        // CERRAR SESIÓN
        btnCerrarSesion?.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            // Esto limpia el historial de pantallas para que no puedan regresar dándole "Atrás"
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    // Función unificada para abrir el reporte
    private fun abrirReporteFalla() {
        val i = Intent(this, ReporteTecnicoActivity::class.java)
        i.putExtra("TECNICO_NOMBRE", nombreUsuario)
        startActivity(i)
    }

    // ==========================================
    // LÓGICA DEL SENSOR (AGITAR EL CELULAR)
    // ==========================================

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            // Cálculo matemático de la fuerza G actual
            aceleracionUltima = aceleracionActual
            aceleracionActual = sqrt((x * x + y * y + z * z).toDouble()).toFloat()

            val delta = aceleracionActual - aceleracionUltima
            variacionAceleracion = variacionAceleracion * 0.9f + delta

            // Si la fuerza de sacudida supera el umbral (12 es un buen número para sacudidas firmes)
            if (variacionAceleracion > 12) {
                val tiempoActual = System.currentTimeMillis()

                // Esperamos mínimo 2 segundos (2000 ms) entre cada sacudida para evitar spam
                if (tiempoActual - ultimoTiempoSacudida > 2000) {
                    ultimoTiempoSacudida = tiempoActual
                    Toast.makeText(this, "¡Atajo detectado!", Toast.LENGTH_SHORT).show()
                    abrirReporteFalla()
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No necesitamos hacer nada aquí para este caso
    }

    // Es vital encender el sensor solo cuando la app está en pantalla y apagarlo
    // cuando se minimiza, de lo contrario drenará la batería del dispositivo.
    override fun onResume() {
        super.onResume()
        acelerometro?.also { acc ->
            sensorManager.registerListener(this, acc, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }
}