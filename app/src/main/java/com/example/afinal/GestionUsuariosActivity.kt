package com.example.afinal

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

// Modelo de datos
data class Usuario(val id: Int, val nombre: String, val numeroEmpleado: String, val rolId: Int)

class GestionUsuariosActivity : AppCompatActivity() {

    // URL centralizada según tu estándar
    private val urlApi = "${ApiConfig.URL_BASE}/crud_usuarios.php"

    private lateinit var rvUsuarios: RecyclerView
    private val listaUsuarios = mutableListOf<Usuario>()
    private lateinit var adapter: UsuarioAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gestion_usuarios)

        rvUsuarios = findViewById(R.id.rvUsuarios)
        rvUsuarios.layoutManager = LinearLayoutManager(this)

        // Configuramos el adaptador
        adapter = UsuarioAdapter(listaUsuarios) { usuarioAEditar ->
            mostrarDialogoUsuario(usuarioAEditar) // ACTUALIZAR
        }
        rvUsuarios.adapter = adapter

        findViewById<FloatingActionButton>(R.id.fabAgregarUsuario).setOnClickListener {
            mostrarDialogoUsuario(null) // CREAR
        }

        cargarUsuarios()
    }

    private fun cargarUsuarios() {
        thread {
            try {
                // Se concatena la acción leer a la URL base
                val url = URL("$urlApi?accion=leer")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                if (connection.responseCode == 200) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonResponse = JSONObject(response)

                    if (!jsonResponse.getBoolean("error")) {
                        val jsonArray = jsonResponse.getJSONArray("usuarios")
                        listaUsuarios.clear()
                        for (i in 0 until jsonArray.length()) {
                            val obj = jsonArray.getJSONObject(i)
                            listaUsuarios.add(
                                Usuario(
                                    id = obj.getInt("id"),
                                    nombre = obj.getString("nombre"),
                                    numeroEmpleado = obj.getString("numero_empleado"),
                                    rolId = obj.getInt("rol_id")
                                )
                            )
                        }
                        runOnUiThread { adapter.notifyDataSetChanged() }
                    }
                }
                connection.disconnect()
            } catch (e: Exception) {
                runOnUiThread { Toast.makeText(this@GestionUsuariosActivity, "Error de red", Toast.LENGTH_SHORT).show() }
            }
        }
    }

    private fun mostrarDialogoUsuario(usuario: Usuario?) {
        val isCrear = usuario == null
        val builder = AlertDialog.Builder(this)
        builder.setTitle(if (isCrear) "Nuevo Usuario" else "Actualizar Usuario")

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 40, 50, 10)

        val inputNombre = EditText(this).apply { hint = "Nombre Completo"; setText(usuario?.nombre ?: "") }
        val inputNumEmp = EditText(this).apply { hint = "Número de Empleado"; setText(usuario?.numeroEmpleado ?: "") }
        val inputPass = EditText(this).apply { hint = "Contraseña" }

        // IDs DE ROLES
        val rbTecnico = RadioButton(this).apply { text = "Técnico"; id = 1 }
        val rbIngeniero = RadioButton(this).apply { text = "Ingeniero"; id = 2 }
        val rbGerente = RadioButton(this).apply { text = "Gerente"; id = 3 }

        val radioGroup = RadioGroup(this).apply {
            addView(rbTecnico)
            addView(rbIngeniero)
            addView(rbGerente)

            when (usuario?.rolId) {
                1 -> rbTecnico.isChecked = true
                2 -> rbIngeniero.isChecked = true
                3 -> rbGerente.isChecked = true
                else -> rbTecnico.isChecked = true
            }
        }

        layout.addView(inputNombre)
        layout.addView(inputNumEmp)
        if (isCrear) layout.addView(inputPass)
        layout.addView(TextView(this).apply { text = "Rol del usuario:"; setPadding(0,20,0,0) })
        layout.addView(radioGroup)

        builder.setView(layout)

        builder.setPositiveButton("Guardar") { _, _ ->
            val nombre = inputNombre.text.toString()
            val numEmp = inputNumEmp.text.toString()
            val pass = inputPass.text.toString()
            val rolId = radioGroup.checkedRadioButtonId

            if (nombre.isNotEmpty() && numEmp.isNotEmpty()) {
                val jsonAEnviar = JSONObject().apply {
                    put("accion", if (isCrear) "crear" else "actualizar")
                    if (!isCrear) put("id", usuario?.id)
                    put("nombre", nombre)
                    put("numero_empleado", numEmp)
                    put("rol_id", rolId)
                    if (isCrear) put("password", pass)
                }
                enviarDatos(jsonAEnviar)
            } else {
                Toast.makeText(this, "Faltan datos", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    private fun enviarDatos(jsonData: JSONObject) {
        thread {
            try {
                val url = URL(urlApi)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json; utf-8")
                connection.doOutput = true

                OutputStreamWriter(connection.outputStream).use { it.write(jsonData.toString()) }

                if (connection.responseCode == 200) {
                    // Limpiamos el buffer de respuesta para cerrar correctamente
                    connection.inputStream.bufferedReader().use { it.readText() }

                    runOnUiThread {
                        Toast.makeText(this@GestionUsuariosActivity, "Guardado correctamente", Toast.LENGTH_SHORT).show()
                        cargarUsuarios()
                    }
                }
                connection.disconnect()
            } catch (e: Exception) {
                runOnUiThread { Toast.makeText(this@GestionUsuariosActivity, "Error al guardar", Toast.LENGTH_SHORT).show() }
            }
        }
    }
}

// Adaptador de Usuario
class UsuarioAdapter(
    private val lista: List<Usuario>,
    private val onEditarClick: (Usuario) -> Unit
) : RecyclerView.Adapter<UsuarioAdapter.ViewHolder>() {

    class ViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvItemNombre)
        val tvNumEmp: TextView = view.findViewById(R.id.tvItemNumEmpleado)
        val tvRol: TextView = view.findViewById(R.id.tvItemRol)
        val btnEditar: ImageButton = view.findViewById(R.id.btnEditar)
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_usuario, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = lista[position]
        holder.tvNombre.text = user.nombre
        holder.tvNumEmp.text = "EMP: ${user.numeroEmpleado}"

        when (user.rolId) {
            1 -> {
                holder.tvRol.text = "TÉCNICO"
                holder.tvRol.setBackgroundColor(android.graphics.Color.parseColor("#38A169"))
            }
            2 -> {
                holder.tvRol.text = "INGENIERO"
                holder.tvRol.setBackgroundColor(android.graphics.Color.parseColor("#DD6B20"))
            }
            3 -> {
                holder.tvRol.text = "GERENTE"
                holder.tvRol.setBackgroundColor(android.graphics.Color.parseColor("#3182CE"))
            }
            else -> {
                holder.tvRol.text = "DESCONOCIDO"
                holder.tvRol.setBackgroundColor(android.graphics.Color.parseColor("#A0AEC0"))
            }
        }

        holder.btnEditar.setOnClickListener { onEditarClick(user) }
    }

    override fun getItemCount() = lista.size
}