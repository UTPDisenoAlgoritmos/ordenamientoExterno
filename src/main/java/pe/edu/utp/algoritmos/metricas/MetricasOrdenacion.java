package pe.edu.utp.algoritmos.metricas;

/**
 * Métricas orientadas a algoritmos de ordenación EXTERNA.
 *
 * A diferencia de los algoritmos internos, aquí importan especialmente
 * las lecturas/escrituras de registros en almacenamiento secundario,
 * las corridas (runs) y las pasadas de fusión.
 */
public class MetricasOrdenacion {

    private final String algoritmo;
    private final String tamanoBloque;

    private long registros;
    private long comparaciones;
    private long lecturasRegistros;
    private long escriturasRegistros;
    private long corridasIniciales;
    private long pasadasFusion;
    private long archivosTemporales;
    private double tiempoMs;

    public MetricasOrdenacion(String algoritmo, String tamanoBloque) {
        this.algoritmo = algoritmo;
        this.tamanoBloque = tamanoBloque;
    }

    public String getAlgoritmo() {
        return algoritmo;
    }

    public String getTamanoBloque() {
        return tamanoBloque;
    }

    public long getRegistros() {
        return registros;
    }

    public long getComparaciones() {
        return comparaciones;
    }

    public long getLecturasRegistros() {
        return lecturasRegistros;
    }

    public long getEscriturasRegistros() {
        return escriturasRegistros;
    }

    public long getCorridasIniciales() {
        return corridasIniciales;
    }

    public long getPasadasFusion() {
        return pasadasFusion;
    }

    public long getArchivosTemporales() {
        return archivosTemporales;
    }

    public double getTiempoMs() {
        return tiempoMs;
    }

    public void incrementarRegistros() {
        registros++;
    }

    public void incrementarComparaciones() {
        comparaciones++;
    }

    public void incrementarLecturas() {
        lecturasRegistros++;
    }

    public void incrementarEscrituras() {
        escriturasRegistros++;
    }

    public void incrementarCorridasIniciales() {
        corridasIniciales++;
    }

    public void incrementarPasadasFusion() {
        pasadasFusion++;
    }

    public void incrementarArchivosTemporales() {
        archivosTemporales++;
    }

    public void setTiempoNs(long tiempoNs) {
        this.tiempoMs = tiempoNs / 1_000_000.0;
    }

    public String cabeceraCsv() {
        return "algoritmo,registros,tamano_bloque,corridas_iniciales,pasadas_fusion,"
                + "comparaciones,lecturas_registro,escrituras_registro,archivos_temporales,tiempo_ms";
    }

    public String toCsv() {
        return String.format(
                java.util.Locale.US,
                "%s,%d,%s,%d,%d,%d,%d,%d,%d,%.3f",
                algoritmo,
                registros,
                tamanoBloque,
                corridasIniciales,
                pasadasFusion,
                comparaciones,
                lecturasRegistros,
                escriturasRegistros,
                archivosTemporales,
                tiempoMs
        );
    }
}
