package com.example.afinal

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

data class Usuario(val id: Int, val nombre: String, val numeroEmpleado: String, val rolId: Int)

class GestionUsuariosActivity : AppCompatActivity() {

    private val urlApi = "${ApiConfig.URL_BASE}/crud_usuarios.php"
    private lateinit var rvUsuarios: RecyclerView
    private val listaUsuarios = mutableListOf<Usuario>()
    private lateinit var adapter: UsuarioAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gestion_usuarios)

        rvUsuarios = findViewById(R.id.rvUsuarios)
        rvUsuarios.layoutManager = LinearLayoutManager(this)
        adapter = UsuarioAdapter(listaUsuarios) { mostrarDialogoUsuario(it) }
        rvUsuarios.adapter = adapter

        findViewById<FloatingActionButton>(R.id.fabAgregarUsuario).setOnClickListener { mostrarDialogoUsuario(null) }
        cargarUsuarios()
    }

    private fun cargarUsuarios() {
        thread {
            try {
                val conn = URL("$urlApi?accion=leer").openConnection() as HttpURLConnection
                if (conn.responseCode == 200) {
                    val res = conn.inputStream.bufferedReader().use { it.readText() }
                    val array = JSONObject(res).getJSONArray("usuarios")
                    val temp = mutableListOf<Usuario>()
                    for (i in 0 until array.length()) {
                        val o = array.getJSONObject(i)
                        temp.add(Usuario(o.getInt("id"), o.getString("nombre"), o.getString("numero_empleado"), o.getInt("rol_id")))
                    }
                    runOnUiThread {
                        listaUsuarios.clear()
                        listaUsuarios.addAll(temp)
                        adapter.notifyDataSetChanged()
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun mostrarDialogoUsuario(usuario: Usuario?) {
        val isCrear = usuario == null
        val builder = AlertDialog.Builder(this)
        builder.setTitle(if (isCrear) "Nuevo Usuario" else "Actualizar Usuario")

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 40, 60, 20)
        }

        val inputNombre = EditText(this).apply { hint = "Nombre Completo"; setText(usuario?.nombre ?: "") }
        val inputNumEmp = EditText(this).apply { hint = "Ej: EMP T 123"; setText(usuario?.numeroEmpleado ?: "") }
        val tvStatus = TextView(this).apply { textSize = 12f; setPadding(10, 0, 0, 10) }

        val inputPass = EditText(this).apply {
            hint = if (isCrear) "Contraseña" else "Nueva contraseña (opcional)"
            transformationMethod = PasswordTransformationMethod.getInstance()
        }

        val rg = RadioGroup(this).apply {
            addView(RadioButton(context).apply { text = "Técnico"; id = 4 })
            addView(RadioButton(context).apply { text = "Ingeniero"; id = 5 })
            addView(RadioButton(context).apply { text = "Gerente"; id = 6 })
            check(usuario?.rolId ?: 4)
        }

        inputNumEmp.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val texto = s.toString()
                val upper = texto.uppercase()

                if (upper.contains("T")) rg.check(4)
                else if (upper.contains("I")) rg.check(5)
                else if (upper.contains("G")) rg.check(6)

                if (texto.length >= 4) {
                    thread {
                        try {
                            val conn = URL(urlApi).openConnection() as HttpURLConnection
                            conn.requestMethod = "POST"
                            conn.doOutput = true
                            val jsonCheck = JSONObject().apply {
                                put("accion", "verificar")
                                put("numero_empleado", texto)
                                if (!isCrear) put("id", usuario?.id)
                            }
                            OutputStreamWriter(conn.outputStream).use { it.write(jsonCheck.toString()) }
                            val res = conn.inputStream.bufferedReader().use { it.readText() }
                            val ocupado = JSONObject(res).getBoolean("ocupado")
                            runOnUiThread {
                                if (ocupado) {
                                    tvStatus.text = "⚠Este número ya está en uso"
                                    tvStatus.setTextColor(Color.RED)
                                } else {
                                    tvStatus.text = "Número disponible"
                                    tvStatus.setTextColor(Color.parseColor("#27AE60"))
                                }
                            }
                        } catch (e: Exception) {}
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        layout.addView(inputNombre)
        layout.addView(inputNumEmp)
        layout.addView(tvStatus)
        layout.addView(inputPass)
        layout.addView(rg)

        builder.setView(layout)
        builder.setPositiveButton("Guardar") { _, _ ->
            val json = JSONObject().apply {
                put("accion", if (isCrear) "crear" else "actualizar")
                if (!isCrear) put("id", usuario?.id)
                put("nombre", inputNombre.text.toString())
                put("numero_empleado", inputNumEmp.text.toString())
                put("rol_id", rg.checkedRadioButtonId)

                val passTxt = inputPass.text.toString()
                if (passTxt.isNotEmpty()) {
                    put("password", passTxt)
                }
            }
            enviarDatos(json)
        }
        builder.setNegativeButton("Cerrar", null)
        builder.show()
    }

    private fun enviarDatos(json: JSONObject) {
        thread {
            try {
                val conn = URL(urlApi).openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.doOutput = true
                OutputStreamWriter(conn.outputStream).use { it.write(json.toString()) }
                if (conn.responseCode == 200) runOnUiThread { cargarUsuarios() }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }
}

class UsuarioAdapter(private val lista: List<Usuario>, private val onClick: (Usuario) -> Unit) : RecyclerView.Adapter<UsuarioAdapter.ViewHolder>() {
    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val tvNombre: TextView = v.findViewById(R.id.tvItemNombre)
        val tvRol: TextView = v.findViewById(R.id.tvItemRol)
        val tvNumEmpleado: TextView = v.findViewById(R.id.tvItemNumEmpleado)
        val btn: ImageButton = v.findViewById(R.id.btnEditar)
    }

    override fun onCreateViewHolder(p: ViewGroup, t: Int) = ViewHolder(LayoutInflater.from(p.context).inflate(R.layout.item_usuario, p, false))

    override fun onBindViewHolder(h: ViewHolder, p: Int) {
        val u = lista[p]
        h.tvNombre.text = u.nombre
        h.tvNumEmpleado.text = u.numeroEmpleado

        val shape = GradientDrawable().apply { cornerRadius = 12f }

        when(u.rolId) {
            4 -> {
                h.tvRol.text = "TÉCNICO"
                shape.setColor(Color.parseColor("#2ECC71"))
            }
            5 -> {
                h.tvRol.text = "INGENIERO"
                shape.setColor(Color.parseColor("#E67E22"))
            }
            6 -> {
                h.tvRol.text = "GERENTE"
                shape.setColor(Color.parseColor("#34495E"))
            }
            else -> {
                h.tvRol.text = "OTRO ROL"
                shape.setColor(Color.GRAY)
            }
        }

        h.tvRol.setTextColor(Color.WHITE)
        h.tvRol.background = shape
        h.btn.setOnClickListener { onClick(u) }
    }

    override fun getItemCount() = lista.size
}