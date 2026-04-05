package com.example.afinal

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class VistaFallaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vista_falla)

        val tvLinea = findViewById<TextView>(R.id.tvLineaDetalle)
        val tvFalla = findViewById<TextView>(R.id.tvFallaDetalle)
        val tvHora = findViewById<TextView>(R.id.tvHoraDetalle)
        val tvDesc = findViewById<TextView>(R.id.tvDescDetalle)
        val imgEvidencia = findViewById<ImageView>(R.id.imgEvidencia)
        val tvSinFoto = findViewById<TextView>(R.id.tvSinFoto)
        val btnBack = findViewById<ImageView>(R.id.btnBack)

        tvLinea.text = "PRIORIDAD: ${intent.getStringExtra("PRIORIDAD")}"
        tvFalla.text = intent.getStringExtra("ESTATUS")
        tvHora.text = intent.getStringExtra("HORA")
        tvDesc.text = intent.getStringExtra("DESCRIPCION")

        val urlImg = intent.getStringExtra("IMAGEN") ?: ""
        if (urlImg.isNotEmpty()) {
            imgEvidencia.visibility = View.VISIBLE
            tvSinFoto.visibility = View.GONE
            Glide.with(this).load("${ApiConfig.URL_BASE}/$urlImg").into(imgEvidencia)
        }

        btnBack.setOnClickListener { finish() }
    }
}