package com.example.afinal

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.PreparedStatement
import java.sql.ResultSet

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
                Toast.makeText(this, "Por favor llena todos los campos", Toast.LENGTH_SHORT).show()
            } else {
                ejecutarLoginDirecto(user, pass)
            }
        }
    }

    private fun ejecutarLoginDirecto(user: String, pass: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val conn = DatabaseHelper.getConnection()

            if (conn != null) {
                try {
                    val sql = "SELECT nombre, rol_id FROM usuarios WHERE numero_empleado = ? AND password = ? AND activo = 1"
                    val statement: PreparedStatement = conn.prepareStatement(sql)
                    statement.setString(1, user)
                    statement.setString(2, pass)

                    val resultSet: ResultSet = statement.executeQuery()

                    if (resultSet.next()) {
                        val nombre = resultSet.getString("nombre")
                        val rolId = resultSet.getInt("rol_id")

                        withContext(Dispatchers.Main) {
                            irAlDashboard(rolId, nombre)
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@MainActivity, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                        }
                    }
                    conn.close()
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Error en consulta: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "No se pudo conectar a la base de datos (XAMPP)", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun irAlDashboard(rolId: Int, nombre: String) {
        val intent: Intent? = when (rolId) {
            1 -> Intent(this, DashboardTecnicoActivity::class.java)
            else -> null
        }

        if (intent != null) {
            intent.putExtra("USER_NAME", nombre)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Rol no reconocido: $rolId", Toast.LENGTH_SHORT).show()
        }
    }
}