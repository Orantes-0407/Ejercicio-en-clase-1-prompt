package com.example.ejercicioenclase;

import android.app.DatePickerDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.ejercicioenclase.model.User;
import com.example.ejercicioenclase.util.Utils;
import com.example.ejercicioenclase.viewmodel.UserViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {

    // ViewModel
    private UserViewModel userViewModel;

    // UI Components
    private TextInputEditText nombreEditText;
    private TextInputEditText apellidoEditText;
    private TextInputEditText fechaNacimientoEditText;
    private AutoCompleteTextView generoAutoCompleteTextView;
    private TextView chronoTextView;
    private Button scanButton;
    private ProgressBar progressBar;
    private TextView statusTextView;
    private CardView resultCardView;
    private TextView nombreResultTextView;
    private TextView nacionalidadResultTextView;
    private TextView tiempoResultTextView;
    private Button historyButton;

    // Biometric components
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private Executor executor;

    // Chrono update handler
    private Handler chronoHandler;
    private Runnable chronoRunnable;
    private long startTimeMillis;
    private boolean isChronoRunning = false;

    // Permisos
    private ActivityResultLauncher<String[]> requestPermissionLauncher;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar ViewModel
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        // Inicializar componentes de UI
        initUI();

        // Configurar el Biometric Prompt
        setupBiometricPrompt();

        // Configurar el cronómetro
        setupChronometer();

        // Configurar lanzador de permisos
        setupPermissionLauncher();

        // Observar cambios en el ViewModel
        observeViewModel();
    }

    private void initUI() {
        // Referencias a vistas del formulario
        nombreEditText = findViewById(R.id.nombreEditText);
        apellidoEditText = findViewById(R.id.apellidoEditText);
        fechaNacimientoEditText = findViewById(R.id.fechaNacimientoEditText);
        generoAutoCompleteTextView = findViewById(R.id.generoAutoCompleteTextView);

        // Referencias a vistas del cronómetro y escaneo
        chronoTextView = findViewById(R.id.chronoTextView);
        scanButton = findViewById(R.id.scanButton);
        progressBar = findViewById(R.id.progressBar);
        statusTextView = findViewById(R.id.statusTextView);

        // Referencias a vistas de resultado
        resultCardView = findViewById(R.id.resultCardView);
        nombreResultTextView = findViewById(R.id.nombreResultTextView);
        nacionalidadResultTextView = findViewById(R.id.nacionalidadResultTextView);
        tiempoResultTextView = findViewById(R.id.tiempoResultTextView);

        // Botón de historial
        historyButton = findViewById(R.id.historyButton);

        // Configurar selector de fecha
        setupDatePicker();

        // Configurar spinner de género
        setupGenderSpinner();

        // Configurar listeners de TextChange
        setupTextChangeListeners();

        // Configurar click listeners
        setupClickListeners();
    }

    private void setupDatePicker() {
        fechaNacimientoEditText.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    MainActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        calendar.set(Calendar.YEAR, selectedYear);
                        calendar.set(Calendar.MONTH, selectedMonth);
                        calendar.set(Calendar.DAY_OF_MONTH, selectedDay);

                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        String formattedDate = dateFormat.format(calendar.getTime());
                        fechaNacimientoEditText.setText(formattedDate);
                        updateUserData();
                    },
                    year, month, day);

            datePickerDialog.show();
        });
    }

    private void setupGenderSpinner() {
        String[] genders = new String[]{"Masculino", "Femenino", "Otro"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, genders);
        generoAutoCompleteTextView.setAdapter(adapter);

        generoAutoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
            updateUserData();
        });
    }

    private void setupTextChangeListeners() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updateUserData();
            }
        };

        nombreEditText.addTextChangedListener(textWatcher);
        apellidoEditText.addTextChangedListener(textWatcher);
    }

    private void setupClickListeners() {
        // Botón de escaneo de huella
        scanButton.setOnClickListener(v -> {
            if (checkBiometricSupport()) {
                biometricPrompt.authenticate(promptInfo);
            } else {
                Toast.makeText(MainActivity.this,
                        "Este dispositivo no soporta autenticación biométrica",
                        Toast.LENGTH_LONG).show();

                // Simular proceso para propósitos de prueba
                simulateFingerprintScan();
            }
        });

        // Botón de historial
        historyButton.setOnClickListener(v -> showHistoryDialog());
    }

    private void setupBiometricPrompt() {
        executor = ContextCompat.getMainExecutor(this);

        biometricPrompt = new BiometricPrompt(MainActivity.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                statusTextView.setText("Error de autenticación: " + errString);
                stopChronometer();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                userViewModel.processFingerprintResult(true);
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                statusTextView.setText("Autenticación fallida, intente de nuevo.");
                stopChronometer();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Escaneo de Huella Digital")
                .setSubtitle("Coloque su huella para verificar su nacionalidad")
                .setNegativeButtonText("Cancelar")
                .build();
    }

    private boolean checkBiometricSupport() {
        BiometricManager biometricManager = BiometricManager.from(this);
        switch (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                return true;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                statusTextView.setText("Este dispositivo no tiene sensor de huellas dactilares.");
                return false;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                statusTextView.setText("El sensor biométrico no está disponible.");
                return false;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                statusTextView.setText("No hay huellas registradas en este dispositivo.");
                return false;
            default:
                statusTextView.setText("Error desconocido en el sensor biométrico.");
                return false;
        }
    }

    private void setupChronometer() {
        chronoHandler = new Handler(Looper.getMainLooper());
        chronoRunnable = new Runnable() {
            @Override
            public void run() {
                if (isChronoRunning) {
                    long elapsedMillis = System.currentTimeMillis() - startTimeMillis;
                    chronoTextView.setText(Utils.formatElapsedTime(elapsedMillis));
                    chronoHandler.postDelayed(this, 10); // Actualizar cada 10 ms
                }
            }
        };
    }

    private void startChronometer() {
        if (!isChronoRunning) {
            isChronoRunning = true;
            startTimeMillis = System.currentTimeMillis();
            chronoHandler.post(chronoRunnable);
        }
    }

    private void stopChronometer() {
        isChronoRunning = false;
        chronoHandler.removeCallbacks(chronoRunnable);
    }

    private void updateUserData() {
        String nombre = nombreEditText.getText() != null ? nombreEditText.getText().toString() : "";
        String apellido = apellidoEditText.getText() != null ? apellidoEditText.getText().toString() : "";
        String fechaNacimiento = fechaNacimientoEditText.getText() != null ? fechaNacimientoEditText.getText().toString() : "";
        String genero = generoAutoCompleteTextView.getText() != null ? generoAutoCompleteTextView.getText().toString() : "";

        userViewModel.prepareNewUser(nombre, apellido, fechaNacimiento, genero);
    }

    private void observeViewModel() {
        // Observar cambios en la validez del formulario
        userViewModel.getFormValid().observe(this, isValid -> {
            scanButton.setEnabled(isValid);
        });

        // Observar si está procesando
        userViewModel.getIsProcessing().observe(this, isProcessing -> {
            if (isProcessing) {
                progressBar.setVisibility(View.VISIBLE);
                scanButton.setEnabled(false);
                startChronometer();
            } else {
                progressBar.setVisibility(View.GONE);
                scanButton.setEnabled(true);
                stopChronometer();
            }
        });

        // Observar mensaje de resultado
        userViewModel.getProcessingResult().observe(this, result -> {
            statusTextView.setText(result);
        });

        // Observar usuario actual
        userViewModel.getCurrentUser().observe(this, user -> {
            if (user != null && user.getNacionalidad() != null) {
                updateResultUI(user);
            }
        });

        // Observar tiempo transcurrido
        userViewModel.getElapsedTimeMillis().observe(this, timeMillis -> {
            if (timeMillis > 0) {
                chronoTextView.setText(Utils.formatElapsedTime(timeMillis));
            }
        });
    }

    private void updateResultUI(User user) {
        // Mostrar datos en la tarjeta de resultados
        nombreResultTextView.setText(String.format("Nombre: %s %s", user.getNombre(), user.getApellido()));
        nacionalidadResultTextView.setText(String.format("Nacionalidad: %s", user.getNacionalidad()));
        tiempoResultTextView.setText(String.format("Tiempo de proceso: %s", Utils.formatElapsedTime(user.getTiempoEscaneo())));

        // Mostrar tarjeta de resultados
        resultCardView.setVisibility(View.VISIBLE);
    }

    private void simulateFingerprintScan() {
        // Iniciar proceso de escaneo simulado
        userViewModel.startFingerprintScan();

        // Simular resultado exitoso después de un tiempo aleatorio
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            userViewModel.processFingerprintResult(true);
        }, 3000); // 3 segundos de simulación
    }

    private void showHistoryDialog() {
        // Obtener todos los usuarios
        List<User> users = userViewModel.getAllUsers();

        if (users.isEmpty()) {
            Toast.makeText(this, "No hay registros en el historial", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear strings para mostrar en el diálogo
        String[] items = new String[users.size()];
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            items[i] = String.format("%s %s - %s - %s",
                    user.getNombre(), user.getApellido(), user.getNacionalidad(),
                    Utils.formatElapsedTime(user.getTiempoEscaneo()));
        }

        // Crear y mostrar el diálogo
        new MaterialAlertDialogBuilder(this)
                .setTitle("Historial de Escaneos")
                .setItems(items, null)
                .setPositiveButton("Exportar", (dialog, which) -> exportHistoryToCsv())
                .setNegativeButton("Cerrar", null)
                .show();
    }

    private void exportHistoryToCsv() {
        // Verificar permisos
        if (checkPermissions()) {
            // Exportar registros
            List<User> users = userViewModel.getAllUsers();
            boolean success = Utils.exportToCSV(this, users);

            if (!success) {
                Toast.makeText(this, "Error al exportar los registros", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean checkPermissions() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(REQUIRED_PERMISSIONS);
                return false;
            }
        }
        return true;
    }

    private void setupPermissionLauncher() {
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
                    boolean allGranted = true;
                    for (Boolean granted : permissions.values()) {
                        allGranted = allGranted && granted;
                    }

                    if (allGranted) {
                        exportHistoryToCsv();
                    } else {
                        Toast.makeText(this,
                                "Se necesitan permisos de almacenamiento para exportar los registros",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopChronometer();
    }
}