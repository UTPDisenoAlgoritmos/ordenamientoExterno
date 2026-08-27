package pe.edu.utp.algoritmos.model;

import java.util.Locale;

/**
 * Representa una fila del archivo ventas_basico.csv.
 * La clave de ordenación del caso es monto ASC.
 */
public record Venta(
        String idVenta,
        String fecha,
        String cliente,
        String producto,
        int cantidad,
        double monto) {

    public static Venta fromCsv(String line) {
        String[] p = line.split(",", -1);
        if (p.length != 6) {
            throw new IllegalArgumentException("Fila CSV inválida: " + line);
        }

        return new Venta(
                p[0].trim(),
                p[1].trim(),
                p[2].trim(),
                p[3].trim(),
                Integer.parseInt(p[4].trim()),
                Double.parseDouble(p[5].trim())
        );
    }

    public String toCsv() {
        return String.format(
                Locale.US,
                "%s,%s,%s,%s,%d,%.2f",
                idVenta, fecha, cliente, producto, cantidad, monto
        );
    }
}
