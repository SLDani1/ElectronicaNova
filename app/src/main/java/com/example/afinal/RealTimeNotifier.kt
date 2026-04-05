package com.example.afinal

import android.os.Handler
import android.os.Looper
import org.json.JSONObject
import java.net.URL
import kotlin.concurrent.thread

class RealTimeNotifier(
    // Ya no pedimos el userId, solo la función que dibuja la alerta
    private val onAlertaRecibida: (String, String) -> Unit
) {
    private val handler = Handler(Looper.getMainLooper())
    // Apuntamos directo a tu API real
    private val urlApi = "${ApiConfig.URL_BASE}/gestion_paros.php"

    // El celular recuerda el último ID que le notificaron
    private var ultimoIdVisto = 0

    private val runnable = object : Runnable {
        override fun run() {
            thread {
                try {
                    // Le mandamos al PHP el último ID que vimos
                    val respuesta = URL("$urlApi?accion=verificar_alerta&ultimo_id=$ultimoIdVisto&t=${System.currentTimeMillis()}").readText()

                    if (respuesta != "null" && respuesta.isNotBlank()) {
                        val json = JSONObject(respuesta)
                        val nuevoId = json.getInt("id")
                        val titulo = json.getString("titulo")
                        val mensaje = json.getString("mensaje")

                        // Guardamos el nuevo ID para no repetir la alerta
                        if (ultimoIdVisto == 0) {
                            // Si es la primera vez que abre la app, solo sincroniza el ID pero no pita
                            ultimoIdVisto = nuevoId
                        } else {
                            // Si ya estaba abierta y llega uno nuevo, actualiza y LANZA ALERTA
                            ultimoIdVisto = nuevoId
                            handler.post { onAlertaRecibida(titulo, mensaje) }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            handler.postDelayed(this, 5000)
        }
    }

    fun start() = handler.post(runnable)
    fun stop() = handler.removeCallbacks(runnable)
}