package com.example.afinal

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.PercentFormatter

class DetalleProduccionActivity : AppCompatActivity() {

    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var tvEmpresa: TextView
    private lateinit var statusIndicator: View
    private lateinit var tvMetaVal: TextView
    private lateinit var tvProduccionVal: TextView
    private lateinit var tvPorcentajeVal: TextView
    private lateinit var pieChartCumplimiento: PieChart
    private lateinit var barChartScrap: BarChart

    private val CHANNEL_ID = "ALERTA_CALIDAD_SMT"
    private var empresaSeleccionada = "KIA"
    private val urlApi = "${ApiConfig.URL_BASE}/get_gerente_data.php"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_produccion)

        swipeRefresh = findViewById(R.id.swipeRefresh)
        tvEmpresa = findViewById(R.id.tvEmpresa)
        statusIndicator = findViewById(R.id.statusIndicator)
        tvMetaVal = findViewById(R.id.tvMetaVal)
        tvProduccionVal = findViewById(R.id.tvProduccionVal)
        tvPorcentajeVal = findViewById(R.id.tvPorcentajeVal)
        pieChartCumplimiento = findViewById(R.id.pieChartCumplimiento)
        barChartScrap = findViewById(R.id.barChartScrap)

        empresaSeleccionada = intent.getStringExtra("EMPRESA_TIPO") ?: "KIA"

        crearCanalNotificaciones()
        swipeRefresh.setOnRefreshListener { obtenerDatosServidor() }
        obtenerDatosServidor()
    }

    private fun obtenerDatosServidor() {
        swipeRefresh.isRefreshing = true
        val request = JsonObjectRequest(Request.Method.GET, urlApi, null,
            { response ->
                swipeRefresh.isRefreshing = false
                try {
                    val meta = response.getInt("meta_del_dia")
                    val scrapTotalBD = response.getInt("scrap_total")

                    val produccionArray = response.getJSONArray("produccion_horaria")
                    var produccionActualEmpresa = 0
                    var produccionTotalPlanta = 0

                    for (i in 0 until produccionArray.length()) {
                        val obj = produccionArray.getJSONObject(i)
                        produccionTotalPlanta += obj.getInt("total")
                        if (empresaSeleccionada == "KIA") {
                            produccionActualEmpresa += obj.getInt("kia")
                        } else {
                            produccionActualEmpresa += obj.getInt("toyota")
                        }
                    }

                    val porcentajeScrap = if (produccionTotalPlanta > 0) {
                        (scrapTotalBD.toFloat() / produccionTotalPlanta.toFloat()) * 100
                    } else { 0f }

                    if (porcentajeScrap > 5.0f) {
                        lanzarNotificacionPush("ALERTA DE CALIDAD", "Scrap Crítico: ${String.format("%.1f", porcentajeScrap)}%")
                    }

                    actualizarInterfaz(produccionActualEmpresa, meta, scrapTotalBD, porcentajeScrap)

                } catch (e: Exception) {
                    Toast.makeText(this, "Error en datos: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                swipeRefresh.isRefreshing = false
                Toast.makeText(this, "Error de conexión", Toast.LENGTH_LONG).show()
            }
        )
        Volley.newRequestQueue(this).add(request)
    }

    private fun actualizarInterfaz(actual: Int, meta: Int, scrapReal: Int, scrapPercent: Float) {
        tvEmpresa.text = "$empresaSeleccionada - Producción Real"
        tvMetaVal.text = meta.toString()
        tvProduccionVal.text = actual.toString()

        // Seguridad: Si la meta es 0, el cumplimiento es 0% (evita crash por división / 0)
        val cumplimiento = if (meta > 0) (actual.toFloat() / meta.toFloat() * 100).toInt() else 0
        tvPorcentajeVal.text = "$cumplimiento%"

        val colorSemaforo = when {
            scrapPercent > 5 -> Color.parseColor("#F44336")
            cumplimiento >= 100 -> Color.parseColor("#4CAF50")
            cumplimiento >= 85 -> Color.parseColor("#FF9800")
            else -> Color.parseColor("#F44336")
        }

        statusIndicator.setBackgroundColor(colorSemaforo)
        tvPorcentajeVal.setTextColor(colorSemaforo)

        renderizarGraficas(actual, meta, scrapReal, colorSemaforo)
    }

    private fun renderizarGraficas(actual: Int, meta: Int, scrapReal: Int, color: Int) {
        // PieChart
        val entriesPie = ArrayList<PieEntry>()
        val pLogrado = if (meta > 0) (actual.toFloat() / meta.toFloat() * 100) else 0f

        if (pLogrado > 0) entriesPie.add(PieEntry(pLogrado, "Producción"))
        if (pLogrado < 100) entriesPie.add(PieEntry(100f - pLogrado, "Faltante"))

        val dataSetPie = PieDataSet(entriesPie, "")
        dataSetPie.colors = listOf(color, Color.LTGRAY)
        pieChartCumplimiento.data = PieData(dataSetPie).apply {
            setValueFormatter(PercentFormatter(pieChartCumplimiento))
            setValueTextSize(12f)
            setValueTextColor(Color.WHITE)
        }
        pieChartCumplimiento.setUsePercentValues(true)
        pieChartCumplimiento.description.isEnabled = false
        pieChartCumplimiento.invalidate()

        // BarChart
        val entriesBar = ArrayList<BarEntry>()
        entriesBar.add(BarEntry(0f, actual.toFloat()))
        entriesBar.add(BarEntry(1f, scrapReal.toFloat()))

        val dataSetBar = BarDataSet(entriesBar, "Producción (Azul) vs Scrap (Rojo)")
        dataSetBar.colors = listOf(Color.parseColor("#2196F3"), Color.parseColor("#F44336"))
        barChartScrap.data = BarData(dataSetBar).apply { setValueTextSize(12f) }
        barChartScrap.description.isEnabled = false
        barChartScrap.xAxis.isEnabled = false
        barChartScrap.invalidate()
    }

    private fun crearCanalNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Alertas SMT", NotificationManager.IMPORTANCE_HIGH)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun lanzarNotificacionPush(titulo: String, msg: String) {
        val intent = Intent(this, DetalleProduccionActivity::class.java)
        val pIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notif = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(titulo)
            .setContentText(msg)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pIntent)
            .setAutoCancel(true)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(1, notif)
    }
}