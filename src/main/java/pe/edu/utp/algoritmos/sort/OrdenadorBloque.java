package pe.edu.utp.algoritmos.sort;

import pe.edu.utp.algoritmos.metricas.MetricasOrdenacion;
import pe.edu.utp.algoritmos.model.Venta;

import java.util.ArrayList;
import java.util.List;

/**
 * Merge Sort EN MEMORIA usado únicamente para ordenar cada bloque pequeño.
 *
 * Esto NO convierte el proceso completo en una ordenación interna:
 * solo se carga un bloque (por ejemplo, 10 registros), se ordena y
 * se devuelve a disco como una corrida ordenada.
 */
final class OrdenadorBloque {

    private OrdenadorBloque() {
    }

    static List<Venta> mergeSort(List<Venta> datos, MetricasOrdenacion metricas) {
        if (datos.size() <= 1) {
            return new ArrayList<>(datos);
        }

        int mitad = datos.size() / 2;

        List<Venta> izquierda =
                mergeSort(new ArrayList<>(datos.subList(0, mitad)), metricas);

        List<Venta> derecha =
                mergeSort(new ArrayList<>(datos.subList(mitad, datos.size())), metricas);

        return fusionar(izquierda, derecha, metricas);
    }

    private static List<Venta> fusionar(
            List<Venta> izquierda,
            List<Venta> derecha,
            MetricasOrdenacion metricas) {

        List<Venta> salida = new ArrayList<>(izquierda.size() + derecha.size());

        int i = 0;
        int j = 0;

        while (i < izquierda.size() && j < derecha.size()) {
            metricas.incrementarComparaciones();

            if (izquierda.get(i).monto() <= derecha.get(j).monto()) {
                salida.add(izquierda.get(i++));
            } else {
                salida.add(derecha.get(j++));
            }
        }

        while (i < izquierda.size()) {
            salida.add(izquierda.get(i++));
        }

        while (j < derecha.size()) {
            salida.add(derecha.get(j++));
        }

        return salida;
    }
}
