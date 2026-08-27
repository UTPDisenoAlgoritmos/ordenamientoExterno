package pe.edu.utp.algoritmos.io;

import pe.edu.utp.algoritmos.model.Venta;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class VentaCsv {

    public static final String CABECERA = "id_venta,fecha,cliente,producto,cantidad,monto";

    private VentaCsv() {
    }

    /**
     * Solo se usa para mostrar una pequeña muestra al usuario.
     * Los algoritmos de ordenación externa NO cargan todo el archivo en memoria.
     */
    public static List<Venta> leerPrimeros(Path archivo, int cantidad) throws IOException {
        List<Venta> resultado = new ArrayList<>();

        try (BufferedReader br = Files.newBufferedReader(archivo, StandardCharsets.UTF_8)) {
            br.readLine(); // cabecera
            String line;

            while (resultado.size() < cantidad && (line = br.readLine()) != null) {
                if (!line.isBlank()) {
                    resultado.add(Venta.fromCsv(line));
                }
            }
        }

        return resultado;
    }

    public static boolean estaOrdenadoPorMonto(Path archivo) throws IOException {
        try (BufferedReader br = Files.newBufferedReader(archivo, StandardCharsets.UTF_8)) {
            br.readLine(); // cabecera

            String line;
            Double anterior = null;

            while ((line = br.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }

                double actual = Venta.fromCsv(line).monto();

                if (anterior != null && actual < anterior) {
                    return false;
                }

                anterior = actual;
            }
        }

        return true;
    }
}
