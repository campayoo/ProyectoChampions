package data;

import model.*;
import java.io.*;
import java.util.*;

/**
 * Clase LectorDatos: Motor de carga de datos desde fichero CSV.
 *
 * Interpreta el archivo maestro 'equipos.csv' para reconstruir el estado
 * inicial de la competición en memoria. Utiliza {@link BufferedReader}
 * envuelto sobre un {@link InputStreamReader} para lectura eficiente
 * con soporte de codificación UTF-8.
 *
 * CAPACIDADES:
 * - Carga de clubes y asignación de presupuestos.
 * - Instanciación de cuerpos técnicos (Entrenadores).
 * - Importación masiva de plantillas de jugadores.
 * - Gestión robusta de errores ante fallos de formato en el CSV.
 */
public class LectorDatos {

    /**
     * Carga todos los equipos y sus jugadores desde el fichero CSV.
     *
     * Utiliza un {@link BufferedReader} para lectura línea a línea con buffer
     * de memoria, lo que mejora el rendimiento al reducir las operaciones
     * de I/O directas con el disco.
     *
     * @return Lista de equipos totalmente equipados con sus plantillas.
     * @throws IOException Si ocurre un error de acceso al archivo.
     */
    public static ArrayList<Equipo> cargarEquipos() throws IOException {
        ArrayList<Equipo> equipos = new ArrayList<>();
        Map<String, Equipo> mapaEquipos = new LinkedHashMap<>();

        // El fichero CSV se carga como recurso del classpath
        String nombreArchivo = "equipos.csv";
        InputStream is = LectorDatos.class.getResourceAsStream(nombreArchivo);

        if (is == null) {
            throw new FileNotFoundException(
                "ERROR DE CONFIGURACIÓN: El archivo '" + nombreArchivo + "' no fue hallado.");
        }

        int numLinea = 0;

        // BufferedReader envuelve InputStreamReader para lectura eficiente con buffer
        // InputStreamReader convierte bytes a caracteres usando la codificación UTF-8
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
            String linea;

            // readLine() lee una línea completa del buffer hasta encontrar \n o EOF
            while ((linea = br.readLine()) != null) {
                numLinea++;
                linea = linea.trim();

                // Saltar líneas vacías, comentarios (#) o cabecera descriptiva (tipo)
                if (linea.isEmpty() || linea.startsWith("#") || linea.startsWith("tipo")) {
                    continue;
                }

                String[] p = linea.split(",");
                try {
                    String tipo = p[0].trim().toUpperCase();

                    // Discriminador de tipos de entidad en el CSV
                    if ("EQUIPO".equals(tipo)) {
                        procesarEquipo(p, mapaEquipos, equipos);
                    } else if ("JUGADOR".equals(tipo)) {
                        procesarJugador(p, mapaEquipos);
                    }
                } catch (Exception e) {
                    // Registro de errores de parseo sin interrumpir la carga
                    System.err.println("⚠️ ERROR DE PARSEO: Línea " + numLinea
                        + " omitida. Detalle: " + e.getMessage());
                }
            }
        }
        return equipos;
    }

    // ---------------------------------------------------------------------
    // BLOQUE: PROCESAMIENTO DE EQUIPOS
    // ---------------------------------------------------------------------

    /**
     * Extrae los datos de un club desde una fila del CSV y le asigna
     * su entrenador con los datos incluidos en la misma fila.
     *
     * Formato esperado de la fila:
     * EQUIPO,nombre,pais,presupuesto,nombreEnt,edadEnt,nacionEnt,estilo,exp,formacion
     *
     * @param p     Array de campos de la línea CSV.
     * @param mapa  Mapa para búsqueda rápida de equipos por nombre.
     * @param lista Lista acumulativa de todos los equipos cargados.
     */
    private static void procesarEquipo(String[] p, Map<String, Equipo> mapa,
                                       ArrayList<Equipo> lista) {
        String nombreEq = p[1].trim();
        double presupuesto = Double.parseDouble(p[3].trim());
        Equipo eq = new Equipo(nombreEq, p[2].trim(), presupuesto);

        // Construcción del Entrenador con datos del CSV
        Entrenador ent = new Entrenador(
                mapa.size() + 1,                       // ID incremental autogenerado
                p[4].trim(),                           // Nombre del entrenador
                Integer.parseInt(p[5].trim()),         // Edad
                p[6].trim(),                           // Nacionalidad
                p[7].trim(),                           // Estilo táctico
                Integer.parseInt(p[8].trim()),         // Años de experiencia
                p[9].trim()                            // Formación favorita
        );

        eq.setEntrenador(ent);
        eq.setFormacion(p[9].trim());

        mapa.put(nombreEq, eq);
        lista.add(eq);
    }

    // ---------------------------------------------------------------------
    // BLOQUE: PROCESAMIENTO DE JUGADORES
    // ---------------------------------------------------------------------

    /**
     * Extrae los datos de un jugador desde una fila del CSV y lo asigna
     * al equipo correspondiente. Soporta dos formatos de CSV:
     *
     * - 13 columnas: incluye un campo extra (offset=1) antes de la edad real.
     * - 12 columnas: formato estándar sin campo extra (offset=0).
     * - 11 columnas: caso especial con edad faltante (se asigna valor por defecto).
     *
     * @param p    Array de campos de la línea CSV.
     * @param mapa Mapa de equipos para buscar el club del jugador.
     */
    private static void procesarJugador(String[] p, Map<String, Equipo> mapa) {
        // Buscar el equipo al que pertenece el jugador
        Equipo eq = mapa.get(p[1].trim());
        if (eq == null) return;

        int offset = 0;

        // Formato extendido: el CSV tiene un campo extra en la columna 4
        if (p.length >= 13) {
            offset = 1;
        }

        // Caso especial: línea con un campo menos (edad ausente)
        // Se reconstruye el array insertando una edad por defecto (20 años)
        if (p.length == 11) {
            String[] p2 = new String[12];
            System.arraycopy(p, 0, p2, 0, 4);
            p2[4] = "20"; // Valor de contingencia: edad por defecto
            System.arraycopy(p, 4, p2, 5, 7);
            p = p2;
            offset = 0;
        }

        // Creación del objeto Jugador con todos sus atributos
        Jugador j = new Jugador(
                Integer.parseInt(p[2].trim()),           // ID único
                p[3].trim(),                             // Nombre deportivo
                Integer.parseInt(p[4 + offset].trim()),  // Edad
                p[5 + offset].trim(),                    // Nacionalidad
                p[6 + offset].trim(),                    // Posición/Demarcación
                Integer.parseInt(p[7 + offset].trim()),  // Ataque
                Integer.parseInt(p[8 + offset].trim()),  // Defensa
                Integer.parseInt(p[9 + offset].trim()),  // Energía máxima
                Integer.parseInt(p[10 + offset].trim()), // Velocidad
                Double.parseDouble(p[11 + offset].trim())// Valor de mercado (M€)
        );

        // Registro oficial en la plantilla del club
        eq.agregarJugador(j);
    }
}