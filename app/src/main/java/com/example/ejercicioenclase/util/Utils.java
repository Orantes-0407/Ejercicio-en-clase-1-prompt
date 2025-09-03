package com.example.ejercicioenclase.util;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.example.ejercicioenclase.model.User;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Clase de utilidades para funciones comunes
 */
public class Utils {
    private static final String TAG = "Utils";

    /**
     * Formatea el tiempo en milisegundos a formato MM:SS.ss
     */
    public static String formatElapsedTime(long elapsedMillis) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedMillis);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedMillis) % 60;
        long millis = elapsedMillis % 1000 / 10; // Solo tomamos dos dígitos para centésimas
        return String.format(Locale.getDefault(), "%02d:%02d.%02d", minutes, seconds, millis);
    }

    /**
     * Exporta los registros a un archivo CSV
     */
    public static boolean exportToCSV(Context context, List<User> users) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
            String timestamp = dateFormat.format(new Date());
            String fileName = "fingerprint_records_" + timestamp + ".csv";

            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File file = new File(dir, fileName);
            FileOutputStream fos = new FileOutputStream(file);
            OutputStreamWriter writer = new OutputStreamWriter(fos);

            // Escribir encabezado CSV
            writer.write("ID,Nombre,Apellido,Fecha de Nacimiento,Género,Nacionalidad,Tiempo de Escaneo (ms),Tiempo Formateado\n");

            // Escribir datos
            for (User user : users) {
                writer.write(user.getId() + "," +
                        escapeCsvField(user.getNombre()) + "," +
                        escapeCsvField(user.getApellido()) + "," +
                        escapeCsvField(user.getFechaNacimiento()) + "," +
                        escapeCsvField(user.getGenero()) + "," +
                        escapeCsvField(user.getNacionalidad()) + "," +
                        user.getTiempoEscaneo() + "," +
                        escapeCsvField(formatElapsedTime(user.getTiempoEscaneo())) + "\n");
            }

            writer.close();
            Toast.makeText(context, "Registros exportados a: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error al exportar registros", e);
            Toast.makeText(context, "Error al exportar registros: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    /**
     * Escapa campos para el formato CSV
     */
    private static String escapeCsvField(String field) {
        if (field == null) return "";
        // Si el campo contiene comas, comillas o saltos de línea, lo encerramos entre comillas
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }
}
