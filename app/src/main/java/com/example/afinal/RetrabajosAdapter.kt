package com.example.afinal

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray

class RetrabajosAdapter(private val datos: JSONArray) : RecyclerView.Adapter<RetrabajosAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvLinea: TextView = v.findViewById(R.id.tvLinea)
        val tvRetrabajo: TextView = v.findViewById(R.id.tvRetrabajo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_retrabajo, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(h: VH, i: Int) {
        val obj = datos.getJSONObject(i)
        h.tvLinea.text = "Proyecto: ${obj.getString("linea")}"
        h.tvRetrabajo.text = obj.getString("retrabajo")

        h.itemView.setOnClickListener {
            val context = h.itemView.context
            val intent = Intent(context, DetalleRetrabajoActivity::class.java).apply {
                putExtra("ID", obj.getString("id"))
                putExtra("LINEA", obj.getString("linea"))
                putExtra("RETRABAJO", obj.getString("retrabajo"))
                putExtra("IMAGEN", obj.optString("imagen", ""))
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = datos.length()
}