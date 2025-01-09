package com.example.LoquoxLocation.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.LoquoxLocation.MainActivity

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    // Crear la base de datos y sus tablas
    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_SITIOS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                titulo TEXT,
                descripcion TEXT,
                latitud TEXT,
                longitud TEXT,
                imagen TEXT
            )
        """
        db.execSQL(createTable)
    }

    // Actualizar la base de datos si cambia la versión
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Eliminar la tabla existente y crearla de nuevo (útil para pruebas)
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SITIOS")
        onCreate(db)
    }

    // Insertar un nuevo sitio
    fun insertarSitio(titulo: String, descripcion: String,  latitud: String, longitud: String, imagen: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("titulo", titulo)
            put("descripcion", descripcion)
            put("latitud", latitud)
            put("longitud", longitud)
            put("imagen", imagen)
        }
        return db.insert(TABLE_SITIOS, null, values)
    }

    // Obtener los sitios desde la base de datos
    fun obtenerSitios(): List<Sitio> {
        val sitiosList = mutableListOf<Sitio>()
        val db = readableDatabase
        val cursor: Cursor = db.query(TABLE_SITIOS, null, null, null, null, null, null)

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val titulo = cursor.getString(cursor.getColumnIndexOrThrow("titulo"))
            val descripcion = cursor.getString(cursor.getColumnIndexOrThrow("descripcion"))
            val latitud = cursor.getString(cursor.getColumnIndexOrThrow("latitud"))
            val longitud = cursor.getString(cursor.getColumnIndexOrThrow("longitud"))
            val imagen = cursor.getString(cursor.getColumnIndexOrThrow("imagen"))
            sitiosList.add(Sitio(id, titulo, descripcion, latitud, longitud, imagen))
        }
        cursor.close()
        db.close()
        return sitiosList
    }

    fun borrarSitio(sitio: Sitio) {
        val db = writableDatabase
        val whereClause = "titulo = ? AND descripcion = ? AND latitud = ? AND longitud = ? AND imagen = ?"
        val whereArgs = arrayOf(sitio.titulo, sitio.descripcion, sitio.latidud, sitio.longitud, sitio.imagen)
        db.delete(TABLE_SITIOS, whereClause, whereArgs)
        db.close()


    }

    fun obtenerSitioPorId(sitioId: String): Sitio? {
        val db = readableDatabase
        val cursor: Cursor = db.query(
            TABLE_SITIOS,
            null,
            "id = ?",
            arrayOf(sitioId),
            null,
            null,
            null
        )
        var sitio: Sitio? = null
        if (cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val titulo = cursor.getString(cursor.getColumnIndexOrThrow("titulo"))
            val descripcion = cursor.getString(cursor.getColumnIndexOrThrow("descripcion"))
            val latitud = cursor.getString(cursor.getColumnIndexOrThrow("latitud"))
            val longitud = cursor.getString(cursor.getColumnIndexOrThrow("longitud"))
            val imagen = cursor.getString(cursor.getColumnIndexOrThrow("imagen"))


            sitio = Sitio(
                id = id,
                titulo = titulo,
                descripcion = descripcion,
                latidud = latitud,
                longitud = longitud,
                imagen = imagen
            )
        }

        cursor.close()
        db.close()

        return sitio

    }

    companion object {
        // Nombre de la base de datos
        private const val DATABASE_NAME = "sitios_db"

        // Versión de la base de datos (si realizas cambios en la base de datos, aumenta esta versión)
        private const val DATABASE_VERSION = 7

        // Nombre de la tabla
        private const val TABLE_SITIOS = "sitios"
    }
}
