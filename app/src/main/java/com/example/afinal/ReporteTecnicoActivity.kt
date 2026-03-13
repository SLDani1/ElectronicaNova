package com.example.afinal

import android.app.*
import android.content.*
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import java.sql.Statement

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reporte_tecnico)

        // Solicitar permiso de notificaciones para Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                101
            )
        }

        imgPreview = findViewById(R.id.imgPreview)
        createNotificationChannel()
        cargarConfiguracionSpinners()

        // Listeners
        findViewById<Button>(R.id.btnTomarFoto).setOnClickListener {
            cameraLauncher.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
        }

        findViewById<Button>(R.id.btnEnviarReporte).setOnClickListener {
            guardarEnBD()
        }

        findViewById<TextView>(R.id.btnIrHistorial).setOnClickListener {
            startActivity(Intent(this, HistorialFallasActivity::class.java))
        }
    }

    private fun cargarConfiguracionSpinners() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val conn = DatabaseHelper.getConnection()
                val ordenes = mutableListOf("Seleccione Orden...")
                val lineas = mutableListOf("Seleccione Línea...")

                conn?.use {
                    val rsO = it.createStatement().executeQuery("SELECT numero_orden FROM ordenes_trabajo")
                    while (rsO.next()) ordenes.add(rsO.getString(1))

                    val rsL = it.createStatement().executeQuery("SELECT nombre FROM estaciones")
                    while (rsL.next()) lineas.add(rsL.getString(1))
                }

                withContext(Dispatchers.Main) {
                    findViewById<Spinner>(R.id.spinnerOrden).adapter = ArrayAdapter(this@ReporteTecnicoActivity, android.R.layout.simple_spinner_dropdown_item, ordenes)
                    findViewById<Spinner>(R.id.spinnerLinea).adapter = ArrayAdapter(this@ReporteTecnicoActivity, android.R.layout.simple_spinner_dropdown_item, lineas)
                    findViewById<Spinner>(R.id.spinnerTipoFalla).adapter = ArrayAdapter(this@ReporteTecnicoActivity, android.R.layout.simple_spinner_dropdown_item, arrayOf("Mecánica", "Eléctrica", "Neumática"))
                    findViewById<Spinner>(R.id.spinnerPrioridad).adapter = ArrayAdapter(this@ReporteTecnicoActivity, android.R.layout.simple_spinner_dropdown_item, arrayOf("Baja", "Media", "Alta", "Critica"))
                }
            } catch (e: Exception) {
                Log.e("DB_ERROR", "Error cargando spinners: ${e.message}")
            }
        }
    }

    private fun guardarEnBD() {
        val desc = findViewById<EditText>(R.id.etDescripcion).text.toString().trim()
        val prioridad = findViewById<Spinner>(R.id.spinnerPrioridad).selectedItem.toString()

        if (desc.length < 10) {
            Toast.makeText(this, "Descripción mínima 10 caracteres", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val conn = DatabaseHelper.getConnection() ?: return@launch
            try {
                conn.autoCommit = false
                val ps = conn.prepareStatement("INSERT INTO fallas_tecnicas (descripcion, prioridad, estatus_falla, registrado_por) VALUES (?, ?, 'Abierta', 1)", Statement.RETURN_GENERATED_KEYS)
                ps.setString(1, desc)
                ps.setString(2, prioridad)
                ps.executeUpdate()

                val rs = ps.generatedKeys
                if (rs.next()) {
                    val idFalla = rs.getInt(1)
                    if (fotoBitmap != null) {
                        val stream = ByteArrayOutputStream()
                        fotoBitmap?.compress(Bitmap.CompressFormat.JPEG, 70, stream)
                        val psImg = conn.prepareStatement("INSERT INTO evidencias (falla_id, imagen_blob) VALUES (?, ?)")
                        psImg.setInt(1, idFalla)
                        psImg.setBytes(2, stream.toByteArray())
                        psImg.executeUpdate()
                    }
                }
                conn.commit()

                withContext(Dispatchers.Main) {
                    if (prioridad.equals("Alta", true) || prioridad.equals("Critica", true)) {
                        enviarNotificacion(prioridad)
                    }
                    Toast.makeText(this@ReporteTecnicoActivity, "¡REGISTRO EXITOSO!", Toast.LENGTH_LONG).show()
                    finish()
                }
            } catch (e: Exception) {
                conn.rollback()
                Log.e("ERROR_SAVE", "${e.message}")
            } finally {
                conn.close()
            }
        }
    }

    private fun enviarNotificacion(prio: String) {
        // Intent para abrir el historial al tocar la notificación
        val intent = Intent(this, HistorialFallasActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT else PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_falla) // TU NUEVO ICONO
            .setContentTitle("ALERTA: FALLA $prio")
            .setContentText("Se ha registrado una falla crítica. Toca para ver detalles.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(longArrayOf(1000, 1000, 1000))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        // Agrega la foto capturada como imagen expandible en la notificación
        fotoBitmap?.let {
            builder.setStyle(NotificationCompat.BigPictureStyle().bigPicture(it))
        }

        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(101, builder.build())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Alertas SMT", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Canal para alertas de fallas técnicas"
                enableVibration(true)
            }
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }
}