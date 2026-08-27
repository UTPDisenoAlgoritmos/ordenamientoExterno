# Semana 03 · Sesión 01
## Práctica básica: Ordenación Externa de Ventas

### Caso

Una tienda recibe ventas en un CSV pequeño. Se debe ordenar por **monto ascendente**
para revisar primero las operaciones menores.

- Archivo: `ventas_basico.csv`
- Registros: **30**
- Clave: `monto ASC`
- Tamaño de bloque para Mezcla Directa: **10 registros**
- Java: **17**
- Maven

> El archivo es pequeño únicamente por fines didácticos. La técnica de ordenación
> externa se justifica cuando el archivo real no cabe completamente en memoria.

---

## Algoritmos implementados

### 1. Mezcla Directa Externa

1. Lee como máximo 10 registros.
2. Ordena ese bloque en memoria.
3. Escribe una corrida ordenada en un archivo temporal.
4. Repite hasta procesar el archivo completo.
5. Fusiona las corridas de dos en dos.
6. Genera el CSV final ordenado.

Con 30 registros y bloque de 10:

`ceil(30 / 10) = 3 corridas iniciales`

### 2. Fusión Natural Externa

1. Lee secuencialmente el CSV.
2. Detecta corridas que ya vienen ordenadas.
3. Cuando el monto actual es menor al anterior, inicia una nueva corrida.
4. Fusiona las corridas naturales hasta obtener una sola.

El tamaño de bloque fijo **no aplica** a Fusión Natural.

---

## Métricas registradas por algoritmo

| Métrica | Significado |
|---|---|
| registros | cantidad de registros originales |
| comparaciones | comparaciones realizadas sobre `monto` |
| lecturas_registro | registros leídos desde archivos |
| escrituras_registro | registros escritos a archivos |
| corridas_iniciales | runs generados/detectados |
| pasadas_fusion | niveles de fusión |
| archivos_temporales | archivos auxiliares creados |
| tiempo_ms | tiempo total aproximado |

En ordenación externa, **lecturas y escrituras** son especialmente importantes.
Por eso no se utiliza `swap` como métrica principal.

---

## Estructura

```text
ordenacion-externa-ventas/
├── pom.xml
├── README.md
└── src/
    └── main/
        ├── java/
        │   └── pe/edu/utp/algoritmos/
        │       ├── App.java
        │       ├── io/VentaCsv.java
        │       ├── metricas/MetricasOrdenacion.java
        │       ├── model/Venta.java
        │       └── sort/
        │           ├── FusionArchivos.java
        │           ├── FusionNaturalExterna.java
        │           ├── MezclaDirectaExterna.java
        │           └── OrdenadorBloque.java
        └── resources/
            └── ventas_basico.csv
```

---

## Compilar con Maven

```bash
mvn clean package
```

Requiere Maven y JDK 17 o superior.

---

## Ejecutar ambos algoritmos

```bash
java -jar target/ordenacion-externa-ventas-1.0.0.jar ambos
```

También puede ejecutarse con Maven:

```bash
mvn exec:java -Dexec.args="ambos"
```

### Solo Mezcla Directa

```bash
java -jar target/ordenacion-externa-ventas-1.0.0.jar directa
```

### Solo Fusión Natural

```bash
java -jar target/ordenacion-externa-ventas-1.0.0.jar natural
```

---

## Salidas

Se crea la carpeta:

```text
salida/
├── ventas_ordenadas_mezcla_directa.csv
├── ventas_ordenadas_fusion_natural.csv
└── metricas_algoritmos.csv
```

---

## Preguntas para la práctica

1. ¿Por qué Mezcla Directa genera 3 corridas iniciales?
2. ¿Cuántas corridas naturales detecta Fusión Natural en el archivo?
3. ¿Qué algoritmo realiza menos pasadas de fusión?
4. ¿Cuál realiza menos lecturas y escrituras?
5. ¿Por qué `swap` no es la métrica más relevante en ordenación externa?
6. ¿Qué ocurriría si el archivo tuviera 10 millones de registros y solo pudieran
   mantenerse 100 000 en memoria?
