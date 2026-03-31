package com.example.afinal

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FallasAdapter(
    private val datos: List<FallaDia>,
    private val onClick: (FallaDia) -> Unit
) : RecyclerView.Adapter<FallasAdapter.FallaVH>() {

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
        val falla = datos[position]

        holder.tit.text = falla.descripcion
        holder.est.text = falla.estatus_falla.uppercase()

        // Lógica de colores basada en tu base de datos
        val estado = falla.estatus_falla.lowercase()
        val (colorHex, bgHex) = when (estado) {
            "abierta", "pendiente" -> Pair("#E53935", "#FFEBEE") // Rojo
            "atendida"             -> Pair("#FB8C00", "#FFF3E0") // Naranja
            "cerrada"              -> Pair("#43A047", "#E8F5E9") // Verde
            else                   -> Pair("#757575", "#F5F5F5") // Gris
        }

        val colorInt = Color.parseColor(colorHex)
        holder.dot.backgroundTintList = ColorStateList.valueOf(colorInt)

        // Fondo redondeado para el texto del estado
        val badge = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 30f
            setColor(Color.parseColor(bgHex))
        }
        holder.est.background = badge
        holder.est.setTextColor(colorInt)

        holder.itemView.setOnClickListener { onClick(falla) }
    }

    override fun getItemCount() = datos.size
}