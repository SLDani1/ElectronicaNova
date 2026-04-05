package com.example.afinal

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class DashboardIngenieroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_ingeniero)

        val tvUserName = findViewById<TextView>(R.id.tvUserName)
        val btnLogout = findViewById<ImageButton>(R.id.btnLogout)

        // Solo 2 botones: Fallas y Retrabajos
        val cardFallas = findViewById<CardView>(R.id.cardTickets)
        val cardRetrabajos = findViewById<CardView>(R.id.cardMantenimiento)

        // Mostrar el nombre del ingeniero
        tvUserName.text = intent.getStringExtra("USER_NAME") ?: "Ingeniero de Procesos"

        // Botón 1: Gestión de Fallas Técnicas (Máquinas)
        cardFallas.setOnClickListener {
            startActivity(Intent(this, ValidarFallasActivity::class.java))
        }

        // Botón 2: Validación Técnica de Retrabajos (Piezas)
        cardRetrabajos.setOnClickListener {
            startActivity(Intent(this, ValidarRetrabajosActivity::class.java))
        }

        // Logout
        btnLogout.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }
    }
}