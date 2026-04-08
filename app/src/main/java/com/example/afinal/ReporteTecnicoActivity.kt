package com.example.afinal

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class ReporteTecnicoActivity : AppCompatActivity() {

    private var fotoBitmap: Bitmap? = null
    private lateinit var imgPreview: ImageView
    private val CHANNEL_ID = "ALERTA_NOVA"

    // Launcher para capturar la foto
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data?.extras?.get("data") as? Bitmap
            if (data != null) {
                fotoBitmap = data
                imgPreview.setImageBitmap(fotoBitmap)
                imgPreview.visibility = View.VISIBLE
                findViewById<Button>(R.id.btnTomarFoto).text = "Cambiar Foto"
            }
        }
    }

    // Launcher para pedir permiso de la cámara
    private val requestCameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            abrirCamara()
        } else {
            Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reporte_tecnico)

        // Pedir permiso de notificaciones en Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        imgPreview = findViewById(R.id.imgPreview)
        createNotificationChannel()

        // Cargar los Spinners desde la Base de Datos al abrir la pantalla
        cargarConfiguracionSpinners()

        // Botón Tomar Foto
        findViewById<Button>(R.id.btnTomarFoto).setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                abrirCamara()
            } else {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        // Botón Enviar Reporte
        findViewById<Button>(R.id.btnEnviarReporte).setOnClickListener {
            guardarEnBD_API()
        }

        // Botón Ver Historial
        findViewById<TextView>(R.id.btnIrHistorial).setOnClickListener {
            startActivity(Intent(this, HistorialFallasActivity::class.java))
        }
    }

    private fun abrirCamara() {
        try {
            cameraLauncher.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
        } catch (e: Exception) {
            Toast.makeText(this, "No se encontró aplicación de cámara", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cargarConfiguracionSpinners() {
        // Spinners con datos fijos
        findViewById<Spinner>(R.id.spinnerTipoFalla).adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, arrayOf("Mecánica", "Eléctrica", "Neumática"))
        findViewById<Spinner>(R.id.spinnerPrioridad).adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, arrayOf("Baja", "Media", "Alta", "Critica"))

        // Pedimos las Órdenes y Líneas a la API (Base de datos)
        val url = "${ApiConfig.URL_BASE}/get_opciones_reporte.php"

        val request = StringRequest(Request.Method.GET, url,
            { response ->
                try {
                    val json = JSONObject(response)

                    // Extraer Órdenes
                    val jsonOrdenes = json.getJSONArray("ordenes")
                    val ordenes = ArrayList<String>()
                    for (i in 0 until jsonOrdenes.length()) {
                        ordenes.add(jsonOrdenes.getString(i))
                    }

                    // Extraer Líneas
                    val jsonLineas = json.getJSONArray("lineas")
                    val lineas = ArrayList<String>()
                    for (i in 0 until jsonLineas.length()) {
                        lineas.add(jsonLineas.getString(i))
                    }

                    // Llenar los Spinners en la vista
                    findViewById<Spinner>(R.id.spinnerOrden).adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, ordenes)
                    findViewById<Spinner>(R.id.spinnerLinea).adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, lineas)

                } catch (e: Exception) {
                    Toast.makeText(this, "Error leyendo opciones de la BD", Toast.LENGTH_SHORT).show()
                    Log.e("API_SPINNERS", "Error JSON: ${e.message}")
                }
            },
            { error ->
                Toast.makeText(this, "Error de red cargando opciones", Toast.LENGTH_SHORT).show()
                Log.e("API_SPINNERS", "Error Red: ${error.message}")
            }
        )
        Volley.newRequestQueue(this).add(request)
    }

    private fun guardarEnBD_API() {
        // 1. Recopilar datos de la vista
        val desc = findViewById<EditText>(R.id.etDescripcion).text.toString().trim()
        val prioridad = findViewById<Spinner>(R.id.spinnerPrioridad).selectedItem?.toString() ?: ""
        val orden = findViewById<Spinner>(R.id.spinnerOrden).selectedItem?.toString() ?: ""
        val linea = findViewById<Spinner>(R.id.spinnerLinea).selectedItem?.toString() ?: ""
        val tipoFalla = findViewById<Spinner>(R.id.spinnerTipoFalla).selectedItem?.toString() ?: ""

        // RECUPERAMOS EL ID QUE VIENE DEL DASHBOARD
        val idUsuario = intent.getIntExtra("USER_ID", -1).toString()

        // 2. Validaciones básicas
        if (desc.length < 10) {
            Toast.makeText(this, "Descripción mínima 10 caracteres", Toast.LENGTH_SHORT).show()
            return
        }
        if (orden == "Seleccione Orden..." || linea == "Seleccione Línea..." || orden.isEmpty() || linea.isEmpty()) {
            Toast.makeText(this, "Por favor seleccione Orden y Línea validas", Toast.LENGTH_SHORT).show()
            return
        }

        // 3. Convertir imagen a Base64 si existe
        var imagenBase64 = ""
        if (fotoBitmap != null) {
            val stream = ByteArrayOutputStream()
            fotoBitmap!!.compress(Bitmap.CompressFormat.JPEG, 70, stream)
            val byteFormat = stream.toByteArray()
            imagenBase64 = Base64.encodeToString(byteFormat, Base64.DEFAULT)
        }

        // 4. Enviar a la API
        val url = "${ApiConfig.URL_BASE}/insertar_reporte.php"

        val request = object : StringRequest(Request.Method.POST, url,
            { response ->
                try {
                    val json = JSONObject(response)
                    if (json.getString("status") == "success") {
                        // Lanzar notificación si es grave
                        if (prioridad.equals("Alta", true) || prioridad.equals("Critica", true)) {
                            enviarNotificacion(prioridad)
                        }
                        Toast.makeText(this, "¡REGISTRO EXITOSO!", Toast.LENGTH_LONG).show()
                        finish() // Cierra la pantalla de reporte
                    } else {
                        Toast.makeText(this, "Error en BD: ${json.getString("message")}", Toast.LENGTH_LONG).show()
                        Log.e("API_GUARDAR", "Error BD: ${json.getString("message")}")
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Error procesando respuesta", Toast.LENGTH_SHORT).show()
                    Log.e("API_GUARDAR", "Error JSON: ${e.message} - Respuesta: $response")
                }
            },
            { error ->
                Toast.makeText(this, "Error de red al guardar", Toast.LENGTH_LONG).show()
                Log.e("API_GUARDAR", "Error Red: ${error.message}")
            }) {
            override fun getParams(): Map<String, String> {
                return mapOf(
                    "descripcion" to desc,
                    "prioridad" to prioridad,
                    "orden" to orden,
                    "linea" to linea,
                    "tipo_falla" to tipoFalla,
                    "imagen" to imagenBase64,
                    "registrado_por" to idUsuario
                )
            }
        }
        Volley.newRequestQueue(this).add(request)
    }

    private fun enviarNotificacion(prio: String) {
        val intent = Intent(this, HistorialFallasActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT else PendingIntent.FLAG_UPDATE_CURRENT
        )
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("ALERTA: FALLA $prio")
            .setContentText("Se ha registrado una falla técnica. Toca para ver detalles.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(101, builder.build())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Alertas SMT", NotificationManager.IMPORTANCE_HIGH)
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }
}