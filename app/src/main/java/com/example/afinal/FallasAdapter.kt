package com.example.afinal

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

class FallasAdapter(
    private val datos: List<FallaDia>,
    private val onClick: (FallaDia) -> Unit
) : RecyclerView.Adapter<FallasAdapter.FallaVH>() {

    class FallaVH(v: View) : RecyclerView.ViewHolder(v) {
        val mainCard: MaterialCardView = v.findViewById(R.id.mainCard)
        val dot: MaterialCardView = v.findViewById(R.id.viewEstadoColor)
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

        // Lógica de colores moderna
        val (colorHex, bgHex) = when (falla.estatus_falla.lowercase()) {
            "abierta", "pendiente" -> Pair("#EF4444", "#FEF2F2") // Rojo suave
            "atendida"             -> Pair("#F59E0B", "#FFFBEB") // Naranja/Ambar
            "cerrada"              -> Pair("#10B981", "#ECFDF5") // Verde esmeralda
            else                   -> Pair("#64748B", "#F8FAFC") // Gris pizarra
        }

        val colorInt = Color.parseColor(colorHex)
        val bgInt = Color.parseColor(bgHex)

        // Aplicar color al punto (MaterialCardView como dot)
        holder.dot.setCardBackgroundColor(colorInt)

        // Aplicar color sutil al borde de la tarjeta principal
        holder.mainCard.strokeColor = colorInt
        holder.mainCard.strokeWidth = 3 // Grosor sutil para resaltar el estado

        // Crear el fondo redondeado del badge dinámicamente
        val badge = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 40f // Forma de cápsula
            setColor(bgInt)
        }

        holder.est.background = badge
        holder.est.setTextColor(colorInt)

        holder.itemView.setOnClickListener { onClick(falla) }
    }

    override fun getItemCount() = datos.size
}