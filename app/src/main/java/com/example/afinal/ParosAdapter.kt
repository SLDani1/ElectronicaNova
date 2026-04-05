package com.example.afinal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class ParosAdapter(private val lista: List<Map<String, String>>) :
    RecyclerView.Adapter<ParosAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // TUS IDS REALES DEL XML (activity_historial_paros.xml / item_paro.xml)
        val tvOrden: TextView = view.findViewById(R.id.tvOrdenParo)
        val tvStatus: TextView = view.findViewById(R.id.tvStatusParo)
        val tvMotivo: TextView = view.findViewById(R.id.tvMotivoParo)
        val tvFecha: TextView = view.findViewById(R.id.tvFechaParo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_paro, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]

        // Mostramos los datos de tu BD
        holder.tvMotivo.text = item["motivo"]
        holder.tvOrden.text = "Línea: ${item["numero_orden"]}"
        holder.tvFecha.text = "Inicio: ${item["fecha_inicio"]}"
        holder.tvStatus.text = item["estado"]

        // Color según estado (ACTIVO = Rojo en tu XML)
        if (item["estado"] == "ACTIVO") {
            holder.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.white))
        } else {
            holder.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.white))
        }
    }

    override fun getItemCount(): Int = lista.size
}