package com.example.afinal

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class DashboardGerenteActivity : AppCompatActivity() {

    private lateinit var notifier: RealTimeNotifier

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_gerente)

        // 1. RECIBIR DATOS DEL LOGIN
        val userId = intent.getIntExtra("USER_ID", 1) // <--- PASAR AL NOTIFICADOR
        val userName = intent.getStringExtra("USER_NAME") ?: "Gerente"

        val tvUserName = findViewById<TextView>(R.id.tvUserName)
        val cardDashboard = findViewById<CardView>(R.id.cardDashboard)
        val cardReportes = findViewById<CardView>(R.id.cardReportes)
        val cardFallasDia = findViewById<CardView>(R.id.cardFallasDia)
        val cardAgregarUsuarios = findViewById<CardView>(R.id.cardAgregarUsuarios)
        val btnLogout = findViewById<ImageButton>(R.id.btnLogout)
        val cardParos = findViewById<CardView>(R.id.cardHistorialParos)

        tvUserName.text = userName

        // 2. INICIALIZAR NOTIFIER (CORREGIDO - Solo se quitó el userId del paréntesis)
        notifier = RealTimeNotifier { titulo, mensaje ->
            AlertDialog.Builder(this)
                .setTitle(titulo)
                .setMessage(mensaje)
                .setCancelable(false)
                .setPositiveButton("VER DETALLES") { _, _ ->
                    startActivity(Intent(this, HistorialParosActivity::class.java))
                }
                .setNegativeButton("CERRAR", null)
                .show()
        }

        // 3. EVENTOS DE CLIC
        cardDashboard.setOnClickListener {
            val opciones = arrayOf("KIA", "TOYOTA")
            AlertDialog.Builder(this).setTitle("Seleccione la Planta")
                .setItems(opciones) { _, which ->
                    val intent = Intent(this, DetalleProduccionActivity::class.java)
                    intent.putExtra("EMPRESA_TIPO", opciones[which])
                    startActivity(intent)
                }.show()
        }

        cardReportes.setOnClickListener { startActivity(Intent(this, DashboardHorarioActivity::class.java)) }
        cardFallasDia.setOnClickListener { startActivity(Intent(this, ListaFallasActivity::class.java)) }
        cardAgregarUsuarios.setOnClickListener { startActivity(Intent(this, GestionUsuariosActivity::class.java)) }
        cardParos?.setOnClickListener { startActivity(Intent(this, HistorialParosActivity::class.java)) }

        btnLogout.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        notifier.start()
    }

    override fun onPause() {
        super.onPause()
        notifier.stop()
    }
}