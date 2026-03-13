package com.example.afinal

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*

class HistorialFallasActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial)

        val rv = findViewById<RecyclerView>(R.id.rvFallasDia)
        rv.layoutManager = LinearLayoutManager(this)

        // Lanzamos la corrutina correctamente dentro del ciclo de vida
        cargarDatos(rv)
    }

    private fun cargarDatos(rv: RecyclerView) {
        CoroutineScope(Dispatchers.IO).launch {
            val conn = DatabaseHelper.getConnection()

            if (conn == null) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@HistorialFallasActivity, "Error: No hay conexión al servidor", Toast.LENGTH_LONG).show()
                }
                return@launch
            }

            val fallas = mutableListOf<FallaDia>()
            try {
                conn.use { connection ->
                    val query = "SELECT prioridad, descripcion, estatus_falla FROM fallas_tecnicas ORDER BY id DESC"
                    val rs = connection.createStatement().executeQuery(query)
                    while (rs.next()) {
                        fallas.add(
                            FallaDia(
                                prioridad = rs.getString(1),
                                descripcion = rs.getString(2),
                                estado = rs.getString(3) // Asegúrate que tu data class use 'estado'
                            )
                        )
                    }
                }

                withContext(Dispatchers.Main) {
                    if (fallas.isEmpty()) {
                        Toast.makeText(this@HistorialFallasActivity, "No hay reportes hoy", Toast.LENGTH_SHORT).show()
                    }
                    rv.adapter = FallasAdapter(fallas)
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@HistorialFallasActivity, "Error en consulta: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}