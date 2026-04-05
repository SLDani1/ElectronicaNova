package com.example.afinal

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import java.util.*

class DashboardHorarioActivity : AppCompatActivity() {

    private lateinit var barChartTotals: BarChart
    private lateinit var rvHoraPorHora: RecyclerView
    private lateinit var tvTotalVal: TextView
    private val urlApi = "${ApiConfig.URL_BASE}/get_gerente_data.php"

    data class HourlyProductionEntry(
        val hora: String, val toyota: Int, val kia: Int, val total: Int
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_horario)

        barChartTotals = findViewById(R.id.barChartTotals)
        rvHoraPorHora = findViewById(R.id.rvHoraPorHora)
        tvTotalVal = findViewById(R.id.tvTotalVal)

        obtenerDatosDeBaseDeDatos()
    }

    private fun obtenerDatosDeBaseDeDatos() {
        val request = JsonObjectRequest(Request.Method.GET, urlApi, null,
            { response ->
                try {
                    val produccionArray = response.getJSONArray("produccion_horaria")
                    val listaReal = ArrayList<HourlyProductionEntry>()
                    var granTotal = 0

                    for (i in 0 until produccionArray.length()) {
                        val obj = produccionArray.getJSONObject(i)
                        val h = obj.getString("hora")
                        val t = obj.getInt("toyota")
                        val k = obj.getInt("kia")
                        val tot = obj.getInt("total")

                        listaReal.add(HourlyProductionEntry(h, t, k, tot))
                        granTotal += tot
                    }

                    tvTotalVal.text = granTotal.toString()
                    setupBarChart(listaReal)
                    setupRecyclerView(listaReal)

                } catch (e: Exception) {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            { Toast.makeText(this, "Error de conexión", Toast.LENGTH_LONG).show() }
        )
        Volley.newRequestQueue(this).add(request)
    }

    private fun setupBarChart(data: List<HourlyProductionEntry>) {
        val entriesToyota = ArrayList<BarEntry>()
        val entriesKia = ArrayList<BarEntry>()

        data.forEachIndexed { index, entry ->
            entriesToyota.add(BarEntry(index.toFloat(), entry.toyota.toFloat()))
            entriesKia.add(BarEntry(index.toFloat(), entry.kia.toFloat()))
        }

        val dsT = BarDataSet(entriesToyota, "Toyota").apply { color = Color.parseColor("#E53935") }
        val dsK = BarDataSet(entriesKia, "Kia").apply { color = Color.parseColor("#1E88E5") }

        val barData = BarData(dsT, dsK).apply { barWidth = 0.35f }
        barChartTotals.data = barData
        barChartTotals.description.isEnabled = false
        barChartTotals.groupBars(0f, 0.2f, 0.05f)
        barChartTotals.animateY(1000)
        barChartTotals.invalidate()
    }

    private fun setupRecyclerView(data: List<HourlyProductionEntry>) {
        rvHoraPorHora.layoutManager = LinearLayoutManager(this)
        rvHoraPorHora.adapter = HourlyProduccionAdapter(data)
    }

    class HourlyProduccionAdapter(private val dataList: List<HourlyProductionEntry>) :
        RecyclerView.Adapter<HourlyProduccionAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val row: LinearLayout = view.findViewById(R.id.rowLayout)
            val h: TextView = view.findViewById(R.id.tvHora)
            val t: TextView = view.findViewById(R.id.tvR200) // Toyota
            val k: TextView = view.findViewById(R.id.tvT335) // Kia
            val tot: TextView = view.findViewById(R.id.tvTP2) // Total
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_row_produccion, parent, false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val entry = dataList[position]
            holder.h.text = entry.hora
            holder.t.text = entry.toyota.toString()
            holder.k.text = entry.kia.toString()
            holder.tot.text = entry.total.toString()

            holder.row.setBackgroundColor(if (position % 2 == 1) Color.parseColor("#F8FAFC") else Color.WHITE)
        }
        override fun getItemCount() = dataList.size
    }
}