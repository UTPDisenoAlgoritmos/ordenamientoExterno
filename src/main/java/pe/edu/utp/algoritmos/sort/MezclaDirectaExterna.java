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
 * MEZCLA DIRECTA EXTERNA
 *
 * 1. Lee bloques de tamaño fijo.
 * 2. Ordena cada bloque en memoria.
 * 3. Guarda cada bloque como una corrida ordenada.
 * 4. Fusiona las corridas de dos en dos hasta obtener una sola.
 *
 * Para el caso de clase:
 *   registros = 30
 *   tamaño de bloque = 10
 *   corridas iniciales esperadas = ceil(30/10) = 3
 */
public class MezclaDirectaExterna {

    public MetricasOrdenacion ordenar(
            Path entrada,
            Path salida,
            int tamanoBloque) throws IOException {

        if (tamanoBloque <= 0) {
            throw new IllegalArgumentException("El tamaño de bloque debe ser > 0.");
        }

        MetricasOrdenacion metricas =
                new MetricasOrdenacion("Mezcla Directa", String.valueOf(tamanoBloque));

        long inicio = System.nanoTime();
        Path tempDir = Files.createTempDirectory("mezcla-directa-");

        try {
            List<Path> corridas = generarCorridas(
                    entrada,
                    tamanoBloque,
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

    private List<Path> generarCorridas(
            Path entrada,
            int tamanoBloque,
            Path tempDir,
            MetricasOrdenacion metricas) throws IOException {

        List<Path> corridas = new ArrayList<>();

        try (BufferedReader br =
                     Files.newBufferedReader(entrada, StandardCharsets.UTF_8)) {

            br.readLine(); // cabecera del CSV
            int numeroCorrida = 0;

            while (true) {
                List<Venta> bloque = new ArrayList<>(tamanoBloque);

                while (bloque.size() < tamanoBloque) {
                    String line = br.readLine();

                    if (line == null) {
                        break;
                    }

                    if (line.isBlank()) {
                        continue;
                    }

                    metricas.incrementarLecturas();
                    metricas.incrementarRegistros();
                    bloque.add(Venta.fromCsv(line));
                }

                if (bloque.isEmpty()) {
                    break;
                }

                List<Venta> bloqueOrdenado =
                        OrdenadorBloque.mergeSort(bloque, metricas);

                Path corrida = tempDir.resolve(
                        String.format("run-inicial-%02d.csv", ++numeroCorrida)
                );

                metricas.incrementarCorridasIniciales();
                metricas.incrementarArchivosTemporales();

                try (BufferedWriter bw =
                             Files.newBufferedWriter(corrida, StandardCharsets.UTF_8)) {

                    for (Venta venta : bloqueOrdenado) {
                        FusionArchivos.escribirLinea(
                                bw,
                                venta.toCsv(),
                                metricas
                        );
                    }
                }

                corridas.add(corrida);
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
                    // Corrida impar: pasa al siguiente nivel sin reescribirse.
                    siguientes.add(actuales.get(i));
                    continue;
                }

                Path salidaFusion = tempDir.resolve(
                        String.format("merge-n%02d-%02d.csv", nivel, (i / 2) + 1)
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
