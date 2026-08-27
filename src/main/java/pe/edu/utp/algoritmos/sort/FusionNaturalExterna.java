package pe.edu.utp.algoritmos.sort;

import pe.edu.utp.algoritmos.io.VentaCsv;
import pe.edu.utp.algoritmos.metricas.MetricasOrdenacion;
import pe.edu.utp.algoritmos.model.Venta;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * FUSIÓN NATURAL EXTERNA
 *
 * En lugar de cortar el archivo en bloques de tamaño fijo,
 * detecta secuencias que YA vienen ordenadas (corridas naturales).
 *
 * Cuando el monto actual es menor que el monto anterior,
 * comienza una nueva corrida.
 */
public class FusionNaturalExterna {

    public MetricasOrdenacion ordenar(
            Path entrada,
            Path salida) throws IOException {

        MetricasOrdenacion metricas =
                new MetricasOrdenacion("Fusión Natural", "N/A");

        long inicio = System.nanoTime();
        Path tempDir = Files.createTempDirectory("fusion-natural-");

        try {
            List<Path> corridas = detectarCorridasNaturales(
                    entrada,
                    tempDir,
                    metricas
            );

            corridas = fusionarHastaUna(
                    corridas,
                    tempDir,
                    metricas
            );

            if (corridas.isEmpty()) {
                throw new IllegalStateException("No se encontraron registros para ordenar.");
            }

            escribirSalidaFinal(corridas.get(0), salida, metricas);
        } finally {
            limpiarDirectorio(tempDir);
            metricas.setTiempoNs(System.nanoTime() - inicio);
        }

        return metricas;
    }

    private List<Path> detectarCorridasNaturales(
            Path entrada,
            Path tempDir,
            MetricasOrdenacion metricas) throws IOException {

        List<Path> corridas = new ArrayList<>();

        try (BufferedReader br =
                     Files.newBufferedReader(entrada, StandardCharsets.UTF_8)) {

            br.readLine(); // cabecera
            String line;
            Venta anterior = null;

            BufferedWriter writer = null;
            int numeroCorrida = 0;

            try {
                while ((line = br.readLine()) != null) {
                    if (line.isBlank()) {
                        continue;
                    }

                    metricas.incrementarLecturas();
                    metricas.incrementarRegistros();

                    Venta actual = Venta.fromCsv(line);

                    boolean nuevaCorrida = false;

                    if (anterior == null) {
                        nuevaCorrida = true;
                    } else {
                        metricas.incrementarComparaciones();

                        if (actual.monto() < anterior.monto()) {
                            nuevaCorrida = true;
                        }
                    }

                    if (nuevaCorrida) {
                        if (writer != null) {
                            writer.close();
                        }

                        Path corrida = tempDir.resolve(
                                String.format("run-natural-%02d.csv", ++numeroCorrida)
                        );

                        corridas.add(corrida);
                        metricas.incrementarCorridasIniciales();
                        metricas.incrementarArchivosTemporales();

                        writer = Files.newBufferedWriter(
                                corrida,
                                StandardCharsets.UTF_8
                        );
                    }

                    FusionArchivos.escribirLinea(
                            writer,
                            actual.toCsv(),
                            metricas
                    );

                    anterior = actual;
                }
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }
        }

        return corridas;
    }

    private List<Path> fusionarHastaUna(
            List<Path> corridas,
            Path tempDir,
            MetricasOrdenacion metricas) throws IOException {

        int nivel = 0;
        List<Path> actuales = new ArrayList<>(corridas);

        while (actuales.size() > 1) {
            metricas.incrementarPasadasFusion();
            nivel++;

            List<Path> siguientes = new ArrayList<>();

            for (int i = 0; i < actuales.size(); i += 2) {
                if (i + 1 >= actuales.size()) {
                    siguientes.add(actuales.get(i));
                    continue;
                }

                Path salidaFusion = tempDir.resolve(
                        String.format("natural-merge-n%02d-%02d.csv", nivel, (i / 2) + 1)
                );

                metricas.incrementarArchivosTemporales();

                FusionArchivos.fusionar(
                        actuales.get(i),
                        actuales.get(i + 1),
                        salidaFusion,
                        metricas
                );

                siguientes.add(salidaFusion);
            }

            actuales = siguientes;
        }

        return actuales;
    }

    private void escribirSalidaFinal(
            Path corridaFinal,
            Path salida,
            MetricasOrdenacion metricas) throws IOException {

        Files.createDirectories(salida.toAbsolutePath().getParent());

        try (BufferedReader br =
                     Files.newBufferedReader(corridaFinal, StandardCharsets.UTF_8);
             BufferedWriter bw =
                     Files.newBufferedWriter(salida, StandardCharsets.UTF_8)) {

            bw.write(VentaCsv.CABECERA);
            bw.newLine();

            String line;

            while ((line = FusionArchivos.leerLinea(br, metricas)) != null) {
                FusionArchivos.escribirLinea(bw, line, metricas);
            }
        }
    }

    private void limpiarDirectorio(Path directorio) {
        try (var stream = Files.walk(directorio)) {
            stream.sorted((a, b) -> b.compareTo(a))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ignored) {
                        }
                    });
        } catch (IOException ignored) {
        }
    }
}
