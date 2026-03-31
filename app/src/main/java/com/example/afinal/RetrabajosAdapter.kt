package com.example.afinal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import org.json.JSONArray

class RetrabajosAdapter(
    private val datos: JSONArray,
    private val onAction: (id: String, estado: String) -> Unit
) : RecyclerView.Adapter<RetrabajosAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvLinea: TextView = v.findViewById(R.id.tvLinea)
        val tvRetrabajo: TextView = v.findViewById(R.id.tvRetrabajo)
        val btnAceptar: MaterialButton = v.findViewById(R.id.btnAceptar)
        val btnNoViable: MaterialButton = v.findViewById(R.id.btnNoViable)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_retrabajo, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(h: VH, i: Int) {
        val obj = datos.getJSONObject(i)
        val id = obj.getString("id")
        h.tvLinea.text = obj.getString("linea")
        h.tvRetrabajo.text = obj.getString("retrabajo")

        h.btnAceptar.setOnClickListener { onAction(id, "Aceptado") }
        h.btnNoViable.setOnClickListener { onAction(id, "No Viable") }
    }

    override fun getItemCount() = datos.length()
}