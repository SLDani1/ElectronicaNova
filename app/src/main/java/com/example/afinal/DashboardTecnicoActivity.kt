package com.example.afinal

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class DashboardTecnicoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_tecnico)

        // 1. RECIBIR DATOS
        val nombreUsuario = intent.getStringExtra("USER_NAME") ?: "Usuario"
        val puestoUsuario = intent.getStringExtra("USER_ROLE") ?: "Técnico SMT"

        // 2. ENLAZAR VISTAS
        val tvUserName = findViewById<TextView>(R.id.tvUserName)
        val tvUserRole = findViewById<TextView>(R.id.tvUserRole)

        tvUserName.text = nombreUsuario
        tvUserRole.text = puestoUsuario

        // 3. CONFIGURAR CLICKS (IDs deben coincidir exactamente con el XML)
        val cardReportar = findViewById<CardView>(R.id.cardReportarFalla)
        val cardHistorial = findViewById<CardView>(R.id.cardHistorialReportes)

        cardReportar?.setOnClickListener {
            val i = Intent(this, ReporteTecnicoActivity::class.java)
            i.putExtra("TECNICO_NOMBRE", nombreUsuario)
            startActivity(i)
        }

        cardHistorial?.setOnClickListener {
            val i = Intent(this, HistorialFallasActivity::class.java)
            startActivity(i)
        }
    }
}