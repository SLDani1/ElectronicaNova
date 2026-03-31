package com.example.afinal

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class DashboardIngenieroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_ingeniero)

        val tvUserName = findViewById<TextView>(R.id.tvUserName)
        val btnLogout = findViewById<ImageButton>(R.id.btnLogout)
        val cardTickets = findViewById<CardView>(R.id.cardTickets)
        val cardMantenimiento = findViewById<CardView>(R.id.cardMantenimiento)
        val cardManuales = findViewById<CardView>(R.id.cardManuales)

        tvUserName.text = intent.getStringExtra("USER_NAME") ?: "Ingeniero"

        // 1. Tickets → Validar Fallas
        cardTickets.setOnClickListener {
            startActivity(Intent(this, ValidarFallasActivity::class.java))
        }

        // 2. Mantenimiento → Validar Retrabajos
        cardMantenimiento.setOnClickListener {
            startActivity(Intent(this, ValidarRetrabajosActivity::class.java))
        }

        // 3. Manuales → pendiente
        cardManuales.setOnClickListener {
            Toast.makeText(this, "Próximamente", Toast.LENGTH_SHORT).show()
        }

        // 4. Logout
        btnLogout.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }
    }
}