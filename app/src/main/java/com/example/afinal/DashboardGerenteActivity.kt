package com.example.afinal

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class DashboardGerenteActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_gerente)

        // Vincular vistas
        val tvUserName = findViewById<TextView>(R.id.tvUserName)

        // Ahora vinculamos las tarjetas completas en lugar de solo los botones
        val cardDashboard = findViewById<CardView>(R.id.cardDashboard)
        val cardReportes = findViewById<CardView>(R.id.cardReportes)
        val cardAgregarUsuarios = findViewById<CardView>(R.id.cardAgregarUsuarios)

        // El botón de logout ahora es un ImageButton (La flecha)
        val btnLogout = findViewById<ImageButton>(R.id.btnLogout)

        // Recibir nombre del Login
        tvUserName.text = intent.getStringExtra("USER_NAME") ?: "Gerente"

        // 1. DASHBOARD: Elegir KIA o TOYOTA
        cardDashboard.setOnClickListener {
            val opciones = arrayOf("KIA", "TOYOTA")
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Seleccione la Planta")
            builder.setItems(opciones) { _, which ->
                val empresaSeleccionada = opciones[which]
                val intent = Intent(this, DetalleProduccionActivity::class.java)
                intent.putExtra("EMPRESA_TIPO", empresaSeleccionada)
                startActivity(intent)
            }
            builder.show()
        }

        // 2. REPORTES: Abrir Tabla Horaria
        cardReportes.setOnClickListener {
            startActivity(Intent(this, DashboardHorarioActivity::class.java))
        }

        // 3. AGREGAR USUARIOS
        cardAgregarUsuarios.setOnClickListener {
            startActivity(Intent(this, GestionUsuariosActivity::class.java))
        }

        // 4. CERRAR SESIÓN (Flecha)
        btnLogout.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            // Estas banderas borran el historial de pantallas para evitar que regresen con el botón "Atrás"
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}