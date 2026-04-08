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
        val urlConFiltro = "$urlApi?proyecto=$empresaSeleccionada"

        val request = JsonObjectRequest(Request.Method.GET, urlConFiltro, null,
            { response ->
                swipeRefresh.isRefreshing = false
                try {
                    val meta = response.getInt("meta_del_dia")
                    val scrapTotalBD = response.getInt("scrap_total")

                    val produccionArray = response.getJSONArray("produccion_horaria")
                    var produccionActual = 0

                    for (i in 0 until produccionArray.length()) {
                        val obj = produccionArray.getJSONObject(i)
                        produccionActual += obj.getInt("cantidad")
                    }

                    // Porcentaje de Scrap sobre el total de piezas tocadas (OK + Scrap)
                    val totalPiezasTocadas = produccionActual + scrapTotalBD
                    val porcentajeScrap = if (totalPiezasTocadas > 0) {
                        (scrapTotalBD.toFloat() / totalPiezasTocadas.toFloat()) * 100
                    } else { 0f }

                    if (porcentajeScrap > 5.0f) {
                        lanzarNotificacionPush("SCRAP ALTO - $empresaSeleccionada",
                            "Nivel crítico detectado: ${String.format("%.1f", porcentajeScrap)}%")
                    }

                    actualizarInterfaz(produccionActual, meta, scrapTotalBD, porcentajeScrap)

                } catch (e: Exception) {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                swipeRefresh.isRefreshing = false
                Toast.makeText(this, "Error de red", Toast.LENGTH_LONG).show()
            }
        )
        Volley.newRequestQueue(this).add(request)
    }

    private fun actualizarInterfaz(actual: Int, meta: Int, scrap: Int, scrapPercent: Float) {
        tvEmpresa.text = "$empresaSeleccionada - PLANTA SMT"
        tvMetaVal.text = "$meta"
        tvProduccionVal.text = "$actual"

        val cumplimiento = if (meta > 0) (actual.toFloat() / meta.toFloat() * 100).toInt() else 0
        tvPorcentajeVal.text = "$cumplimiento%"

        // Lógica de semáforo mejorada
        val colorSemaforo = when {
            scrapPercent > 5 -> Color.parseColor("#EF4444") // Rojo por Calidad
            cumplimiento >= 95 -> Color.parseColor("#22C55E") // Verde (Meta casi cumplida)
            cumplimiento >= 75 -> Color.parseColor("#F59E0B") // Ámbar
            else -> Color.parseColor("#EF4444") // Rojo por productividad
        }

        statusIndicator.setBackgroundColor(colorSemaforo)
        tvPorcentajeVal.setTextColor(colorSemaforo)

        renderizarGraficas(actual, meta, scrap, colorSemaforo)
    }

    private fun renderizarGraficas(actual: Int, meta: Int, scrapReal: Int, color: Int) {
        // PieChart
        val entriesPie = ArrayList<PieEntry>()
        val pLogrado = if (meta > 0) (actual.toFloat() / meta.toFloat() * 100) else 0f

        // No permitimos que el gráfico se rompa si superamos el 100%
        val logDisplay = if (pLogrado > 100f) 100f else pLogrado
        val pPendiente = if (100f - logDisplay < 0) 0f else 100f - logDisplay

        entriesPie.add(PieEntry(logDisplay, "Logrado"))
        if (pPendiente > 0) entriesPie.add(PieEntry(pPendiente, "Pendiente"))

        val dataSetPie = PieDataSet(entriesPie, "").apply {
            colors = listOf(color, Color.parseColor("#E2E8F0"))
            valueTextColor = Color.WHITE
            valueTextSize = 14f
        }

        pieChartCumplimiento.data = PieData(dataSetPie).apply {
            setValueFormatter(PercentFormatter(pieChartCumplimiento))
        }
        pieChartCumplimiento.setUsePercentValues(true)
        pieChartCumplimiento.centerText = "EFICIENCIA"
        pieChartCumplimiento.description.isEnabled = false
        pieChartCumplimiento.animateY(800)
        pieChartCumplimiento.invalidate()

        // BarChart
        val entriesBar = ArrayList<BarEntry>()
        entriesBar.add(BarEntry(0f, actual.toFloat()))
        entriesBar.add(BarEntry(1f, scrapReal.toFloat()))

        val dataSetBar = BarDataSet(entriesBar, "Piezas").apply {
            colors = listOf(Color.parseColor("#3B82F6"), Color.parseColor("#EF4444"))
            valueTextSize = 12f
        }

        barChartScrap.data = BarData(dataSetBar)
        barChartScrap.description.isEnabled = false
        barChartScrap.xAxis.isEnabled = false
        barChartScrap.animateY(1000)
        barChartScrap.invalidate()
    }

    private fun crearCanalNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Alertas Calidad", NotificationManager.IMPORTANCE_HIGH)
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun lanzarNotificacionPush(titulo: String, msg: String) {
        val intent = Intent(this, DetalleProduccionActivity::class.java)
        val pIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notif = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_error)
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