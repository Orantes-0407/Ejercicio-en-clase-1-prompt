package com.example.ejercicioenclase.repository;

import android.content.Context;

import com.example.ejercicioenclase.database.DatabaseHelper;
import com.example.ejercicioenclase.model.User;

import java.util.List;

/**
 * Repositorio para manejar operaciones de usuarios
 */
public class UserRepository {
    private DatabaseHelper databaseHelper;
    private static UserRepository instance;

    // Nacionalidades restringidas según los requisitos
    private static final String NACIONALIDAD_GUATEMALTECA = "Guatemalteca";
    private static final String NACIONALIDAD_ESTADOUNIDENSE = "Estadounidense";

    private UserRepository(Context context) {
        databaseHelper = DatabaseHelper.getInstance(context);
    }

    // Implementación del patrón Singleton
    public static synchronized UserRepository getInstance(Context context) {
        if (instance == null) {
            instance = new UserRepository(context);
        }
        return instance;
    }

    /**
     * Guarda un usuario en la base de datos
     */
    public long saveUser(User user) {
        return databaseHelper.insertUser(user);
    }

    /**
     * Actualiza un usuario existente
     */
    public int updateUser(User user) {
        return databaseHelper.updateUser(user);
    }

    /**
     * Obtiene todos los usuarios registrados
     */
    public List<User> getAllUsers() {
        return databaseHelper.getAllUsers();
    }

    /**
     * Obtiene un usuario por su ID
     */
    public User getUserById(long userId) {
        return databaseHelper.getUser(userId);
    }

    /**
     * Elimina un usuario
     */
    public void deleteUser(long userId) {
        databaseHelper.deleteUser(userId);
    }

    /**
     * Verifica si la nacionalidad está restringida
     * @return true si la nacionalidad está restringida (Guatemalteca o Estadounidense)
     */
    public boolean isNacionalidadRestringida(String nacionalidad) {
        return NACIONALIDAD_GUATEMALTECA.equalsIgnoreCase(nacionalidad) ||
               NACIONALIDAD_ESTADOUNIDENSE.equalsIgnoreCase(nacionalidad);
    }
}
