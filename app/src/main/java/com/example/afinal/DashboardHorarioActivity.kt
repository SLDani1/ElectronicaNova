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

    // URL convertida al formato ApiConfig
    private val urlApi = "${ApiConfig.URL_BASE}/get_gerente_data.php"

    // Actualizamos el Data Class para los nuevos datos del PHP
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
        // Se utiliza la variable urlApi definida arriba
        val request = JsonObjectRequest(Request.Method.GET, urlApi, null,
            { response ->
                try {
                    val produccionArray = response.getJSONArray("produccion_horaria")
                    val listaReal = ArrayList<HourlyProductionEntry>()
                    var granTotal = 0

                    for (i in 0 until produccionArray.length()) {
                        val obj = produccionArray.getJSONObject(i)
                        val hora = obj.getString("hora")
                        val tyt = obj.getInt("toyota")
                        val kia = obj.getInt("kia")
                        val total = obj.getInt("total")

                        listaReal.add(HourlyProductionEntry(hora, tyt, kia, total))
                        granTotal += total
                    }

                    tvTotalVal.text = granTotal.toString()
                    setupBarChart(listaReal)
                    setupRecyclerView(listaReal)

                } catch (e: Exception) {
                    Toast.makeText(this, "Error procesando JSON", Toast.LENGTH_SHORT).show()
                }
            },
            {
                Toast.makeText(this, "Error de conexión con el servidor", Toast.LENGTH_LONG).show()
            }
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

        val dataSetToyota = BarDataSet(entriesToyota, "Toyota")
        dataSetToyota.color = Color.parseColor("#E53935") // Rojo

        val dataSetKia = BarDataSet(entriesKia, "Kia")
        dataSetKia.color = Color.parseColor("#1E88E5") // Azul

        // Agrupamos las barras
        val barData = BarData(dataSetToyota, dataSetKia)
        barData.barWidth = 0.35f

        barChartTotals.data = barData
        barChartTotals.description.isEnabled = false
        barChartTotals.animateY(1000)

        // Configuración para que las barras queden juntas por hora
        barChartTotals.groupBars(0f, 0.2f, 0.05f)
        barChartTotals.invalidate()
    }

    private fun setupRecyclerView(data: List<HourlyProductionEntry>) {
        rvHoraPorHora.layoutManager = LinearLayoutManager(this)
        rvHoraPorHora.adapter = HourlyProduccionAdapter(data)
    }

    // Adapter ajustado para mostrar Toyota, Kia y el Total
    class HourlyProduccionAdapter(private val dataList: List<HourlyProductionEntry>) :
        RecyclerView.Adapter<HourlyProduccionAdapter.ViewHolder>() {
        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val rowLayout: LinearLayout = view.findViewById(R.id.rowLayout)
            val tvHora: TextView = view.findViewById(R.id.tvHora)
            val tvCol1: TextView = view.findViewById(R.id.tvR200)
            val tvCol2: TextView = view.findViewById(R.id.tvT335)
            val tvCol3: TextView = view.findViewById(R.id.tvTP2)
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_row_produccion, parent, false)
            return ViewHolder(view)
        }
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val entry = dataList[position]
            holder.tvHora.text = entry.hora
            holder.tvCol1.text = entry.toyota.toString()
            holder.tvCol2.text = entry.kia.toString()
            holder.tvCol3.text = entry.total.toString()

            val tvCol4: TextView? = holder.itemView.findViewById(R.id.tvT510)
            tvCol4?.visibility = View.GONE

            holder.rowLayout.setBackgroundColor(if (position % 2 == 1) Color.parseColor("#EEEEEE") else Color.WHITE)
        }
        override fun getItemCount() = dataList.size
    }
}