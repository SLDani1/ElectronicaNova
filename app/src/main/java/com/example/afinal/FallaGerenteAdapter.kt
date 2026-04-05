package com.example.afinal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONObject

class FallaGerenteAdapter(
    private val lista: List<JSONObject>,
    private val onClick: (JSONObject) -> Unit
) : RecyclerView.Adapter<FallaGerenteAdapter.ViewHolder>() {

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val tvPrioridad: TextView = v.findViewById(R.id.tvPrioridadItem)
        val tvDescripcion: TextView = v.findViewById(R.id.tvDescripcionItem)
        val tvHora: TextView = v.findViewById(R.id.tvHoraItem)
        val tvStatus: TextView = v.findViewById(R.id.tvStatusItem)
        val card: View = v.findViewById(R.id.cardFalla)
    }

    override fun onCreateViewHolder(p: ViewGroup, t: Int) =
        ViewHolder(LayoutInflater.from(p.context).inflate(R.layout.item_falla_gerente, p, false))

    override fun onBindViewHolder(h: ViewHolder, p: Int) {
        val f = lista[p]
        h.tvPrioridad.text = "Prioridad: ${f.optString("prioridad")}"
        h.tvDescripcion.text = f.optString("descripcion")
        h.tvHora.text = f.optString("hora")
        h.tvStatus.text = f.optString("estatus_falla").uppercase()
        h.card.setOnClickListener { onClick(f) }
    }

    override fun getItemCount() = lista.size
}