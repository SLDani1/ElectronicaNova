package com.example.afinal

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

object DatabaseHelper {
    // Para la versión 5.1.49 que tienes en Gradle, debe ser este:
    private const val DRIVER = "com.mysql.jdbc.Driver"

    // Simplifiquemos la URL para evitar errores de parámetros
    // Cambia la IP de 192.168... a 10.0.2.2
    private const val URL = "jdbc:mysql://10.0.2.2:3307/sistema_smt?useSSL=false&allowPublicKeyRetrieval=true"
    private const val USER = "root"
    private const val PASS = ""

    fun getConnection(): Connection? {
        return try {
            Class.forName(DRIVER)
            DriverManager.setLoginTimeout(5)
            DriverManager.getConnection(URL, USER, PASS)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}