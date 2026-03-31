package com.example.afinal

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etUser = findViewById<EditText>(R.id.etUser)
        val etPass = findViewById<TextInputEditText>(R.id.etPass)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val user = etUser.text.toString().trim()
            val pass = etPass.text.toString().trim()

            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Por favor, llena todos los campos", Toast.LENGTH_SHORT).show()
            } else {
                ejecutarLoginAPI(user, pass)
            }
        }
    }

    private fun ejecutarLoginAPI(user: String, pass: String) {
        val url = "${ApiConfig.URL_BASE}/login.php"

        val request = object : StringRequest(Request.Method.POST, url,
            { response ->
                try {
                    val json = JSONObject(response)
                    if (json.getString("status") == "success") {
                        val nombre = json.getString("nombre")
                        val rolId = json.getInt("rol_id")

                        // Redirigir según el rol recibido
                        irAlDashboard(rolId, nombre)

                    } else {
                        Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Error en respuesta del servidor", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Error de red: Verifica tu conexión o IP", Toast.LENGTH_LONG).show()
            }) {
            override fun getParams(): Map<String, String> {
                return mapOf("user" to user, "pass" to pass)
            }
        }
        Volley.newRequestQueue(this).add(request)
    }

    private fun irAlDashboard(rolId: Int, nombre: String) {
        val intent: Intent? = when (rolId) {
            1 -> Intent(this, DashboardTecnicoActivity::class.java)       //TECNICO
            2 -> Intent(this, DashboardIngenieroActivity::class.java)   //INGENIERO
            3 -> Intent(this, DashboardGerenteActivity::class.java)    //GERENTE

            else -> null
        }

        if (intent != null) {
            intent.putExtra("USER_NAME", nombre)
            // Opcional: Pasar empresa por defecto si no tienes un selector de planta aún
            intent.putExtra("EMPRESA_TIPO", "KIA")

            startActivity(intent)
            finish() // Evita que regresen al login con el botón de "atrás"
        } else {
            Toast.makeText(this, "Acceso denegado: Rol $rolId no reconocido", Toast.LENGTH_SHORT).show()
        }
    }
}