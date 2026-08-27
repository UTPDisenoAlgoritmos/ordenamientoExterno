package pe.edu.utp.algoritmos.sort;

import pe.edu.utp.algoritmos.metricas.MetricasOrdenacion;
import pe.edu.utp.algoritmos.model.Venta;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

final class FusionArchivos {

    private FusionArchivos() {
    }

    static void fusionar(
            Path archivoA,
            Path archivoB,
            Path salida,
            MetricasOrdenacion metricas) throws IOException {

        try (BufferedReader a = Files.newBufferedReader(archivoA, StandardCharsets.UTF_8);
             BufferedReader b = Files.newBufferedReader(archivoB, StandardCharsets.UTF_8);
             BufferedWriter out = Files.newBufferedWriter(salida, StandardCharsets.UTF_8)) {

            String lineaA = leerLinea(a, metricas);
            String lineaB = leerLinea(b, metricas);

            while (lineaA != null && lineaB != null) {
                Venta ventaA = Venta.fromCsv(lineaA);
                Venta ventaB = Venta.fromCsv(lineaB);

                metricas.incrementarComparaciones();

                if (ventaA.monto() <= ventaB.monto()) {
                    escribirLinea(out, lineaA, metricas);
                    lineaA = leerLinea(a, metricas);
                } else {
                    escribirLinea(out, lineaB, metricas);
                    lineaB = leerLinea(b, metricas);
                }
            }

            while (lineaA != null) {
                escribirLinea(out, lineaA, metricas);
                lineaA = leerLinea(a, metricas);
            }

            while (lineaB != null) {
                escribirLinea(out, lineaB, metricas);
                lineaB = leerLinea(b, metricas);
            }
        }
    }

    static String leerLinea(
            BufferedReader reader,
            MetricasOrdenacion metricas) throws IOException {

        String line = reader.readLine();

        if (line != null) {
            metricas.incrementarLecturas();
        }

        return line;
    }

    static void escribirLinea(
            BufferedWriter writer,
            String line,
            MetricasOrdenacion metricas) throws IOException {

        writer.write(line);
        writer.newLine();
        metricas.incrementarEscrituras();
    }
}
