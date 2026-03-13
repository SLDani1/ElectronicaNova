package com.example.afinal


import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FallasAdapter(private val lista: List<FallaDia>) : RecyclerView.Adapter<FallasAdapter.FallaVH>() {

    class FallaVH(v: View) : RecyclerView.ViewHolder(v) {
        val dot: View = v.findViewById(R.id.viewEstadoColor)
        val tit: TextView = v.findViewById(R.id.tvLineaFalla)
        val est: TextView = v.findViewById(R.id.tvEstadoTexto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FallaVH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_falla, parent, false)
        return FallaVH(v)
    }

    override fun onBindViewHolder(holder: FallaVH, position: Int) {
        val f = lista[position]

        // Texto principal
        holder.tit.text = f.descripcion
        holder.est.text = f.estado.uppercase()

        // Configuración de colores (Texto y Fondo de la etiqueta)
        val (colorHex, bgHex) = when (f.estado.lowercase()) {
            "abierta", "pendiente" -> Pair("#E53935", "#FFEBEE") // Rojo
            "atendida" -> Pair("#FB8C00", "#FFF3E0")             // Naranja
            "cerrada" -> Pair("#43A047", "#E8F5E9")              // Verde
            else -> Pair("#757575", "#F5F5F5")                   // Gris
        }

        val colorInt = Color.parseColor(colorHex)
        val bgColorInt = Color.parseColor(bgHex)

        // 1. Color del círculo indicador
        holder.dot.backgroundTintList = ColorStateList.valueOf(colorInt)

        // 2. Fondo redondeado para el texto de estado
        val badge = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 30f // Hace que parezca una píldora
            setColor(bgColorInt)
        }

        holder.est.background = badge
        holder.est.setTextColor(colorInt)
    }

    override fun getItemCount() = lista.size
}