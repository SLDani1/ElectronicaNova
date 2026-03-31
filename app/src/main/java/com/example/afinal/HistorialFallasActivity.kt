package com.example.afinal

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray

class HistorialFallasActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial)

        val rv = findViewById<RecyclerView>(R.id.rvFallasDia)
        rv.layoutManager = LinearLayoutManager(this)

        cargarDatosAPI(rv)
    }

    private fun cargarDatosAPI(rv: RecyclerView) {
        val url = "${ApiConfig.URL_BASE}/get_fallas.php"

        val request = StringRequest(Request.Method.GET, url,
            { response ->
                try {
                    val jsonArray = JSONArray(response)
                    val listaDeFallas = mutableListOf<FallaDia>()

                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)

                        // CORRECCIÓN: Ahora pasamos los 6 parámetros requeridos por FallaDia
                        listaDeFallas.add(
                            FallaDia(
                                id = obj.getString("id"),
                                descripcion = obj.getString("descripcion"),
                                prioridad = obj.getString("prioridad"),
                                estatus_falla = obj.getString("estatus_falla"),
                                hora = obj.getString("hora"),
                                // Usamos optString por si la base de datos devuelve null en la imagen
                                imagen = obj.optString("imagen", "")
                            )
                        )
                    }

                    // Configurar el adaptador con la lista y el evento click
                    rv.adapter = FallasAdapter(listaDeFallas) { falla ->
                        // Abrir detalle al hacer click enviando TODOS los datos, incluyendo imagen
                        val intent = Intent(this, DetalleFallaActivity::class.java).apply {
                            putExtra("ID", falla.id)
                            putExtra("DESCRIPCION", falla.descripcion)
                            putExtra("PRIORIDAD", falla.prioridad)
                            putExtra("ESTATUS", falla.estatus_falla)
                            putExtra("HORA", falla.hora)
                            putExtra("IMAGEN", falla.imagen) // <--- CRUCIAL PARA LA FOTO
                        }
                        startActivity(intent)
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error de parseo: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Error de red", Toast.LENGTH_SHORT).show()
            }
        )
        Volley.newRequestQueue(this).add(request)
    }
}