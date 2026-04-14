package data;

import model.*;
import java.io.*;
import java.util.*;

/**
 * LectorDatos — Carga exclusiva de datos desde el recurso equipos.csv.
 */
public class LectorDatos {

    /**
     * Carga la lista de equipos y sus plantillas desde el archivo CSV.
     * @return ArrayList con los equipos cargados.
     * @throws IOException Si el archivo no existe o no se puede leer.
     */
    public static ArrayList<Equipo> cargarEquipos() throws IOException {
        ArrayList<Equipo> equipos = new ArrayList<>();
        Map<String, Equipo> mapaEquipos = new LinkedHashMap<>();

        // El nombre del archivo es fijo ya que siempre debe leer de ahí
        String nombreArchivo = "equipos.csv";
        InputStream is = LectorDatos.class.getResourceAsStream(nombreArchivo);

        if (is == null) {
            throw new FileNotFoundException("CRÍTICO: No se encontró el archivo '"
                    + nombreArchivo + "' en el paquete data.");
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();

                // Ignorar líneas vacías, comentarios (#) o la cabecera (tipo,nombre...)
                if (linea.isEmpty() || linea.startsWith("#") || linea.startsWith("tipo")) {
                    continue;
                }

                String[] p = linea.split(",");
                String tipo = p[0].trim().toUpperCase();

                try {
                    if ("EQUIPO".equals(tipo)) {
                        procesarEquipo(p, mapaEquipos, equipos);
                    } else if ("JUGADOR".equals(tipo)) {
                        procesarJugador(p, mapaEquipos);
                    }
                } catch (Exception e) {
                    System.err.println("Error en línea: [" + linea + "] -> " + e.getMessage());
                }
            }
        }
        return equipos;
    }

    private static void procesarEquipo(String[] p, Map<String, Equipo> mapa, ArrayList<Equipo> lista) {
        // Columnas: EQUIPO, nombre, pais, presupuesto, nombreEntrenador, edadEnt, nacionEnt, estilo, exp, formacion
        String nombreEq = p[1].trim();
        double presupuesto = Double.parseDouble(p[3].trim());

        Equipo eq = new Equipo(nombreEq, p[2].trim(), presupuesto);

        Entrenador ent = new Entrenador(
                mapa.size() + 1, // ID autogenerado
                p[4].trim(),     // nombreEntrenador
                Integer.parseInt(p[5].trim()), // edadEnt
                p[6].trim(),     // nacionEnt
                p[7].trim(),     // estilo
                Integer.parseInt(p[8].trim()), // exp
                p[9].trim()      // formacion
        );

        eq.setEntrenador(ent);
        eq.setFormacion(p[9].trim());

        mapa.put(nombreEq, eq);
        lista.add(eq);
    }

    private static void procesarJugador(String[] p, Map<String, Equipo> mapa) {
        // Columnas: JUGADOR, nombreEquipo, id, nombre, edad, nacion, posicion, ataque, defensa, energia, velocidad, valor
        Equipo eq = mapa.get(p[1].trim());
        if (eq == null) return;

        Jugador j = new Jugador(
                Integer.parseInt(p[2].trim()), // id
                p[3].trim(),                   // nombre
                Integer.parseInt(p[4].trim()), // edad
                p[5].trim(),                   // nacion
                p[6].trim(),                   // posicion
                Integer.parseInt(p[7].trim()), // ataque
                Integer.parseInt(p[8].trim()), // defensa
                Integer.parseInt(p[9].trim()), // energia
                Integer.parseInt(p[10].trim()),// velocidad
                Double.parseDouble(p[11].trim()) // valor
        );
        eq.agregarJugador(j);
    }
}