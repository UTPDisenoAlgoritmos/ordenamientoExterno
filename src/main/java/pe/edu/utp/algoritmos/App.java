package pe.edu.utp.algoritmos;

import pe.edu.utp.algoritmos.io.VentaCsv;
import pe.edu.utp.algoritmos.metricas.MetricasOrdenacion;
import pe.edu.utp.algoritmos.model.Venta;
import pe.edu.utp.algoritmos.sort.FusionNaturalExterna;
import pe.edu.utp.algoritmos.sort.MezclaDirectaExterna;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Caso básico:
 * Una tienda recibe 30 ventas en CSV.
 * Se ordena por monto ASC.
 *
 * Algoritmos implementados:
 * 1) Mezcla Directa Externa
 * 2) Fusión Natural Externa
 *
 * Uso:
 *   java -jar target/ordenacion-externa-ventas-1.0.0.jar ambos
 *   java -jar target/ordenacion-externa-ventas-1.0.0.jar directa
 *   java -jar target/ordenacion-externa-ventas-1.0.0.jar natural
 */
public class App {

    private static final int TAMANO_BLOQUE = 10;
    private static final int MUESTRA = 10;

    public static void main(String[] args) throws Exception {

        String modo = args.length >= 1
                ? args[0].trim().toLowerCase()
                : "ambos";

        Path entrada = resolverEntrada();
        Path carpetaSalida = Paths.get("salida");
        Files.createDirectories(carpetaSalida);

        System.out.println("==============================================================");
        System.out.println(" CASO: ORDENACIÓN EXTERNA DE VENTAS");
        System.out.println("==============================================================");
        System.out.println("Archivo      : " + entrada.toAbsolutePath());
        System.out.println("Clave        : monto ASC");
        System.out.println("Registros    : 30");
        System.out.println("Bloque MD    : " + TAMANO_BLOQUE + " registros");
        System.out.println();

        mostrarMuestra("ANTES DE ORDENAR", entrada);

        List<MetricasOrdenacion> resultados = new ArrayList<>();

        if (modo.equals("ambos") || modo.equals("directa")) {
            Path salidaDirecta =
                    carpetaSalida.resolve("ventas_ordenadas_mezcla_directa.csv");

            MetricasOrdenacion md =
                    new MezclaDirectaExterna().ordenar(
                            entrada,
                            salidaDirecta,
                            TAMANO_BLOQUE
                    );

            resultados.add(md);

            System.out.println();
            mostrarMuestra("DESPUÉS - MEZCLA DIRECTA", salidaDirecta);

            System.out.println(
                    "Validación Mezcla Directa: "
                            + (VentaCsv.estaOrdenadoPorMonto(salidaDirecta)
                            ? "CORRECTO"
                            : "ERROR")
            );
        }

        if (modo.equals("ambos") || modo.equals("natural")) {
            Path salidaNatural =
                    carpetaSalida.resolve("ventas_ordenadas_fusion_natural.csv");

            MetricasOrdenacion fn =
                    new FusionNaturalExterna().ordenar(
                            entrada,
                            salidaNatural
                    );

            resultados.add(fn);

            System.out.println();
            mostrarMuestra("DESPUÉS - FUSIÓN NATURAL", salidaNatural);

            System.out.println(
                    "Validación Fusión Natural: "
                            + (VentaCsv.estaOrdenadoPorMonto(salidaNatural)
                            ? "CORRECTO"
                            : "ERROR")
            );
        }

        if (resultados.isEmpty()) {
            System.out.println();
            System.out.println("Modo no válido: " + modo);
            System.out.println("Use: ambos | directa | natural");
            return;
        }

        imprimirMetricas(resultados);
        exportarMetricas(
                carpetaSalida.resolve("metricas_algoritmos.csv"),
                resultados
        );

        System.out.println();
        System.out.println("Archivos generados en: "
                + carpetaSalida.toAbsolutePath());
    }

    private static Path resolverEntrada() throws IOException {
        Path local = Paths.get("src/main/resources/ventas_basico.csv");

        if (Files.exists(local)) {
            return local;
        }

        // Permite ejecutar también desde el JAR si el archivo local no existe.
        try (InputStream in =
                     App.class.getResourceAsStream("/ventas_basico.csv")) {

            if (in == null) {
                throw new IOException(
                        "No se encontró ventas_basico.csv"
                );
            }

            Path temporal = Files.createTempFile(
                    "ventas_basico-",
                    ".csv"
            );

            Files.copy(
                    in,
                    temporal,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );

            temporal.toFile().deleteOnExit();
            return temporal;
        }
    }

    private static void mostrarMuestra(
            String titulo,
            Path archivo) throws IOException {

        System.out.println();
        System.out.println("---- " + titulo + " ----");
        System.out.printf("%-8s %-12s %-18s %10s%n",
                "ID", "FECHA", "PRODUCTO", "MONTO");

        for (Venta v : VentaCsv.leerPrimeros(archivo, MUESTRA)) {
            System.out.printf(
                    java.util.Locale.US,
                    "%-8s %-12s %-18s %10.2f%n",
                    v.idVenta(),
                    v.fecha(),
                    v.producto(),
                    v.monto()
            );
        }
    }

    private static void imprimirMetricas(
            List<MetricasOrdenacion> resultados) {

        System.out.println();
        System.out.println("==============================================================");
        System.out.println(" MÉTRICAS POR ALGORITMO");
        System.out.println("==============================================================");

        System.out.printf(
                "%-17s %8s %8s %8s %8s %12s %10s %10s %10s %10s%n",
                "Algoritmo",
                "Regs.",
                "Bloque",
                "Runs",
                "Pasadas",
                "Comparac.",
                "Lecturas",
                "Escrit.",
                "Tmp",
                "ms"
        );

        for (MetricasOrdenacion m : resultados) {
            System.out.printf(
                    java.util.Locale.US,
                    "%-17s %8d %8s %8d %8d %12d %10d %10d %10d %10.3f%n",
                    m.getAlgoritmo(),
                    m.getRegistros(),
                    m.getTamanoBloque(),
                    m.getCorridasIniciales(),
                    m.getPasadasFusion(),
                    m.getComparaciones(),
                    m.getLecturasRegistros(),
                    m.getEscriturasRegistros(),
                    m.getArchivosTemporales(),
                    m.getTiempoMs()
            );
        }

        System.out.println();
        System.out.println("Interpretación:");
        System.out.println("- Comparaciones: veces que se comparó la clave monto.");
        System.out.println("- Lecturas/Escrituras: E/S de registros en archivos.");
        System.out.println("- Runs: corridas iniciales generadas o detectadas.");
        System.out.println("- Pasadas: niveles de fusión necesarios.");
        System.out.println("- Tmp: archivos temporales creados.");
        System.out.println("- No se usa 'swap' como métrica principal porque en");
        System.out.println("  ordenación externa domina el costo de E/S a disco.");
    }

    private static void exportarMetricas(
            Path archivo,
            List<MetricasOrdenacion> resultados) throws IOException {

        List<String> lineas = new ArrayList<>();
        lineas.add(resultados.get(0).cabeceraCsv());

        for (MetricasOrdenacion m : resultados) {
            lineas.add(m.toCsv());
        }

        Files.write(
                archivo,
                lineas,
                StandardCharsets.UTF_8
        );
    }
}
