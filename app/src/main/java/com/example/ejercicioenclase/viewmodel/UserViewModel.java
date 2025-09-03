package com.example.ejercicioenclase.viewmodel;

import android.app.Application;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.ejercicioenclase.model.User;
import com.example.ejercicioenclase.repository.UserRepository;

import java.util.List;
import java.util.Random;

/**
 * ViewModel para manejar la lógica de procesamiento de huellas y usuarios
 */
public class UserViewModel extends AndroidViewModel {
    private final UserRepository userRepository;

    // LiveData para la UI
    private final MutableLiveData<User> currentUser = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isProcessing = new MutableLiveData<>(false);
    private final MutableLiveData<String> processingResult = new MutableLiveData<>();
    private final MutableLiveData<Long> elapsedTimeMillis = new MutableLiveData<>(0L);
    private final MutableLiveData<Boolean> formValid = new MutableLiveData<>(false);

    // Variables para el cronómetro
    private long startTime;

    public UserViewModel(@NonNull Application application) {
        super(application);
        userRepository = UserRepository.getInstance(application);
    }

    // Getters para LiveData (inmutables hacia la UI)
    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public LiveData<Boolean> getIsProcessing() {
        return isProcessing;
    }

    public LiveData<String> getProcessingResult() {
        return processingResult;
    }

    public LiveData<Long> getElapsedTimeMillis() {
        return elapsedTimeMillis;
    }

    public LiveData<Boolean> getFormValid() {
        return formValid;
    }

    /**
     * Prepara un nuevo usuario con los datos iniciales
     */
    public void prepareNewUser(String nombre, String apellido, String fechaNacimiento, String genero) {
        User user = new User(nombre, apellido, fechaNacimiento, genero);
        currentUser.setValue(user);
        validateForm();
    }

    /**
     * Valida si el formulario tiene datos completos
     */
    public void validateForm() {
        User user = currentUser.getValue();
        boolean isValid = user != null &&
                          user.getNombre() != null && !user.getNombre().trim().isEmpty() &&
                          user.getApellido() != null && !user.getApellido().trim().isEmpty() &&
                          user.getFechaNacimiento() != null && !user.getFechaNacimiento().trim().isEmpty() &&
                          user.getGenero() != null && !user.getGenero().trim().isEmpty();

        formValid.setValue(isValid);
    }

    /**
     * Inicia el proceso de escaneo de huella
     */
    public void startFingerprintScan() {
        if (Boolean.FALSE.equals(formValid.getValue())) {
            processingResult.setValue("Por favor complete todos los campos del formulario antes de escanear la huella");
            return;
        }

        isProcessing.setValue(true);
        processingResult.setValue("Escaneando huella...");

        // Inicia el cronómetro
        startTime = SystemClock.elapsedRealtime();

        // Aquí simularíamos el proceso real de escaneo con la API biométrica
    }

    /**
     * Procesa el resultado del escaneo de huella
     * En un caso real, esto sería llamado por el callback de la API biométrica
     */
    public void processFingerprintResult(boolean success) {
        if (success) {
            // Simula la determinación de la nacionalidad (en un caso real sería por API o base de datos)
            determineNationality();
        } else {
            stopProcessing("Error en el escaneo de huella. Intente nuevamente.");
        }
    }

    /**
     * Determina la nacionalidad basada en la huella (simulación)
     * En una implementación real, esto consultaría una API o base de datos
     */
    private void determineNationality() {
        // Lista de nacionalidades para simular
        String[] nacionalidades = {
                "Argentina", "Boliviana", "Brasileña", "Chilena", "Colombiana",
                "Costarricense", "Cubana", "Ecuatoriana", "Guatemalteca", "Hondureña",
                "Mexicana", "Nicaragüense", "Panameña", "Paraguaya", "Peruana",
                "Salvadoreña", "Uruguaya", "Venezolana", "Estadounidense", "Canadiense"
        };

        // Simula una demora en el procesamiento (1-3 segundos)
        try {
            Thread.sleep(new Random().nextInt(2000) + 1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Calcula el tiempo transcurrido
        long endTime = SystemClock.elapsedRealtime();
        long elapsedTime = endTime - startTime;
        elapsedTimeMillis.setValue(elapsedTime);

        // Selecciona una nacionalidad aleatoria
        String randomNacionalidad;
        do {
            randomNacionalidad = nacionalidades[new Random().nextInt(nacionalidades.length)];
        } while (!isValidNacionalidad(randomNacionalidad));

        // Actualiza el usuario con la nacionalidad y el tiempo de escaneo
        User user = currentUser.getValue();
        if (user != null) {
            user.setNacionalidad(randomNacionalidad);
            user.setTiempoEscaneo(elapsedTime);
            user.setHuellaId(generateFingerprintId());
            currentUser.setValue(user);

            // Guarda el usuario en la base de datos
            long userId = userRepository.saveUser(user);
            user.setId((int) userId);

            stopProcessing("Nacionalidad detectada: " + randomNacionalidad);
        } else {
            stopProcessing("Error: no se encontró información del usuario");
        }
    }

    /**
     * Verifica si la nacionalidad es válida según los requisitos
     */
    private boolean isValidNacionalidad(String nacionalidad) {
        // Verifica que no sea una nacionalidad restringida
        return !userRepository.isNacionalidadRestringida(nacionalidad);
    }

    /**
     * Genera un ID único para la huella (simulación)
     */
    private String generateFingerprintId() {
        // En una implementación real, esto vendría de la API biométrica
        return "FP-" + System.currentTimeMillis() + "-" + new Random().nextInt(10000);
    }

    /**
     * Detiene el procesamiento y actualiza el mensaje de resultado
     */
    private void stopProcessing(String message) {
        isProcessing.setValue(false);
        processingResult.setValue(message);
    }

    /**
     * Obtiene todos los usuarios registrados en la base de datos
     */
    public List<User> getAllUsers() {
        return userRepository.getAllUsers();
    }
}
