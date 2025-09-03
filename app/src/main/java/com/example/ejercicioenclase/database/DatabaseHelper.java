package com.example.ejercicioenclase.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.ejercicioenclase.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper de base de datos para manejar operaciones SQLite
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";

    // Información de la base de datos
    private static final String DATABASE_NAME = "fingerprint_scanner.db";
    private static final int DATABASE_VERSION = 1;

    // Singleton para asegurar una sola instancia de la base de datos
    private static DatabaseHelper instance;

    // Tabla de usuarios
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NOMBRE = "nombre";
    private static final String COLUMN_APELLIDO = "apellido";
    private static final String COLUMN_FECHA_NACIMIENTO = "fecha_nacimiento";
    private static final String COLUMN_GENERO = "genero";
    private static final String COLUMN_NACIONALIDAD = "nacionalidad";
    private static final String COLUMN_HUELLA_ID = "huella_id";
    private static final String COLUMN_TIEMPO_ESCANEO = "tiempo_escaneo";

    // Sentencia SQL para crear la tabla de usuarios
    private static final String CREATE_TABLE_USERS =
            "CREATE TABLE " + TABLE_USERS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_NOMBRE + " TEXT NOT NULL,"
            + COLUMN_APELLIDO + " TEXT NOT NULL,"
            + COLUMN_FECHA_NACIMIENTO + " TEXT,"
            + COLUMN_GENERO + " TEXT,"
            + COLUMN_NACIONALIDAD + " TEXT,"
            + COLUMN_HUELLA_ID + " TEXT,"
            + COLUMN_TIEMPO_ESCANEO + " INTEGER"
            + ")";

    // Método para obtener la instancia única (Singleton)
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USERS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    /**
     * Inserta un nuevo usuario en la base de datos
     */
    public long insertUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_NOMBRE, user.getNombre());
        values.put(COLUMN_APELLIDO, user.getApellido());
        values.put(COLUMN_FECHA_NACIMIENTO, user.getFechaNacimiento());
        values.put(COLUMN_GENERO, user.getGenero());
        values.put(COLUMN_NACIONALIDAD, user.getNacionalidad());
        values.put(COLUMN_HUELLA_ID, user.getHuellaId());
        values.put(COLUMN_TIEMPO_ESCANEO, user.getTiempoEscaneo());

        long id = db.insert(TABLE_USERS, null, values);
        db.close();
        return id;
    }

    /**
     * Actualiza un usuario existente en la base de datos
     */
    public int updateUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_NOMBRE, user.getNombre());
        values.put(COLUMN_APELLIDO, user.getApellido());
        values.put(COLUMN_FECHA_NACIMIENTO, user.getFechaNacimiento());
        values.put(COLUMN_GENERO, user.getGenero());
        values.put(COLUMN_NACIONALIDAD, user.getNacionalidad());
        values.put(COLUMN_HUELLA_ID, user.getHuellaId());
        values.put(COLUMN_TIEMPO_ESCANEO, user.getTiempoEscaneo());

        int rowsUpdated = db.update(TABLE_USERS, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(user.getId())});
        db.close();
        return rowsUpdated;
    }

    /**
     * Obtiene todos los usuarios de la base de datos
     */
    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_USERS + " ORDER BY " + COLUMN_ID + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        try {
            if (cursor.moveToFirst()) {
                do {
                    User user = new User();
                    user.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                    user.setNombre(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOMBRE)));
                    user.setApellido(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_APELLIDO)));
                    user.setFechaNacimiento(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FECHA_NACIMIENTO)));
                    user.setGenero(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GENERO)));
                    user.setNacionalidad(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NACIONALIDAD)));
                    user.setHuellaId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HUELLA_ID)));
                    user.setTiempoEscaneo(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIEMPO_ESCANEO)));

                    userList.add(user);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener usuarios", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        db.close();
        return userList;
    }

    /**
     * Obtiene un usuario por su ID
     */
    public User getUser(long userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        User user = null;

        Cursor cursor = db.query(TABLE_USERS, null, COLUMN_ID + " = ?",
                new String[]{String.valueOf(userId)}, null, null, null);

        try {
            if (cursor != null && cursor.moveToFirst()) {
                user = new User();
                user.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                user.setNombre(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOMBRE)));
                user.setApellido(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_APELLIDO)));
                user.setFechaNacimiento(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FECHA_NACIMIENTO)));
                user.setGenero(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GENERO)));
                user.setNacionalidad(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NACIONALIDAD)));
                user.setHuellaId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HUELLA_ID)));
                user.setTiempoEscaneo(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIEMPO_ESCANEO)));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener usuario", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        db.close();
        return user;
    }

    /**
     * Elimina un usuario de la base de datos
     */
    public void deleteUser(long userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_USERS, COLUMN_ID + " = ?", new String[]{String.valueOf(userId)});
        db.close();
    }
}
