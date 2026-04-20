package data;

import model.*;
import java.io.*;
import java.util.*;

/**
 * Clase LectorDatos: El motor de carga y persistencia del simulador.
 * 
 * Esta clase interpreta el archivo maestro 'equipos.csv' para reconstruir el estado
 * inicial de la competición en memoria.
 * 
 * CAPACIDADES:
 * - Carga de clubes y asignación de presupuestos.
 * - Instanciación de cuerpos técnicos (Entrenadores).
 * - Importación masiva de plantillas de atletas.
 * - Gestión de robustez ante fallos de formato en el CSV.
 */
public class LectorDatos {

    /**
     * Carga todos los equipos y sus respectivos jugadores desde el archivo CSV.
     * 
     * @return Una lista de equipos totalmente equipados.
     * @throws IOException Si ocurre un error de acceso físico al archivo.
     */
    public static ArrayList<Equipo> cargarEquipos() throws IOException {
        ArrayList<Equipo> equipos = new ArrayList<>();
        Map<String, Equipo> mapaEquipos = new LinkedHashMap<>();

        // El archivo debe residir en la misma carpeta que esta clase (.class)
        String nombreArchivo = "equipos.csv";
        InputStream is = LectorDatos.class.getResourceAsStream(nombreArchivo);

        if (is == null) {
            throw new FileNotFoundException("ERROR DE CONFIGURACIÓN: El archivo '" + nombreArchivo + "' no fue hallado.");
        }

        int numLinea = 0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                numLinea++;
                linea = linea.trim();

                // Bloque: Salto de líneas vacías, comentarios o descriptores de tipo
                if (linea.isEmpty() || linea.startsWith("#") || linea.startsWith("tipo")) {
                    continue;
                }

                String[] p = linea.split(",");
                try {
                    String tipo = p[0].trim().toUpperCase();
                    // Discriminador de tipos de entidad
                    if ("EQUIPO".equals(tipo)) {
                        procesarEquipo(p, mapaEquipos, equipos);
                    } else if ("JUGADOR".equals(tipo)) {
                        procesarJugador(p, mapaEquipos);
                    }
                } catch (Exception e) {
                    System.err.println("⚠️ ERROR DE PARSEO: Línea " + numLinea + " omitida por inconsistencias.");
                }
            }
        }
        return equipos;
    }

    // ---------------------------------------------------------------------
    // BLOQUE: PROCESAMIENTO DE INFRAESTRUCTURA (EQUIPOS)
    // ---------------------------------------------------------------------

    /**
     * Extrae los datos de un club y le asigna su líder táctico (Entrenador).
     */
    private static void procesarEquipo(String[] p, Map<String, Equipo> mapa, ArrayList<Equipo> lista) {
        String nombreEq = p[1].trim();
        double presupuesto = Double.parseDouble(p[3].trim());
        Equipo eq = new Equipo(nombreEq, p[2].trim(), presupuesto);

        // Bloque: Reconstrucción del historial del entrenador
        Entrenador ent = new Entrenador(
                mapa.size() + 1,        // ID incremental autogenerado
                p[4].trim(),            // Nombre común
                Integer.parseInt(p[5].trim()), // Edad biológica
                p[6].trim(),            // Nacionalidad
                p[7].trim(),            // Estilo táctico (ej. Tiki-Taka)
                Integer.parseInt(p[8].trim()), // Años de experiencia
                p[9].trim()             // Esquema preferido (ej. 4-3-3)
        );

        eq.setEntrenador(ent);
        eq.setFormacion(p[9].trim()); // Se aplica la formación preferida del míster como base del club

        mapa.put(nombreEq, eq);
        lista.add(eq);
    }

    // ---------------------------------------------------------------------
    // BLOQUE: PROCESAMIENTO DE ACTIVOS (JUGADORES)
    // ---------------------------------------------------------------------

    private static void procesarJugador(String[] p, Map<String, Equipo> mapa) {
        // Validación de pertenencia
        Equipo eq = mapa.get(p[1].trim());
        if (eq == null) return; 

        int offset = 0;
        
        // Bloque: Manejador de redundancia de columnas (Compatibilidad con versiones de CSV extendidas)
        if (p.length >= 13) {
            offset = 1;
        }

        // Bloque: Lógica de recuperación ante pérdida de datos en el CSV (Caso "Sin Edad")
        if (p.length == 11 && p[4].equals("España")) {
             String[] p2 = new String[12];
             System.arraycopy(p, 0, p2, 0, 4); 
             p2[4] = "20"; // Valor de contingencia (Edad por defecto)
             System.arraycopy(p, 4, p2, 5, 7); 
             p = p2;
             offset = 0;
        }

        // Creación del objeto Jugador con sus atributos físicos y técnicos
        Jugador j = new Jugador(
                Integer.parseInt(p[2].trim()),          // Clave única
                p[3].trim(),                            // Nombre deportivo
                Integer.parseInt(p[4 + offset].trim()),  // Edad
                p[5 + offset].trim(),                    // Nación
                p[6 + offset].trim(),                    // Demarcación habitual
                Integer.parseInt(p[7 + offset].trim()),  // Potencial ofensivo
                Integer.parseInt(p[8 + offset].trim()),  // Nivel defensivo
                Integer.parseInt(p[9 + offset].trim()),  // Fondo físico (Energía)
                Integer.parseInt(p[10 + offset].trim()), // Velocidad punta
                Double.parseDouble(p[11 + offset].trim())// Valoración económica en M€
        );
        eq.agregarJugador(j); // Registro oficial en la plantilla del club
    }
}