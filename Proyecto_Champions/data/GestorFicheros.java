package data;

import model.*;
import java.io.*;
import java.util.ArrayList;

/**
 * Clase GestorFicheros: Sistema de persistencia del simulador.
 *
 * Proporciona métodos para guardar y cargar el estado completo de la partida
 * utilizando las siguientes tecnologías de ficheros de Java:
 *
 * <ul>
 *   <li><b>ObjectOutputStream / ObjectInputStream</b>: Serialización binaria
 *       de objetos Java para guardar/cargar el estado completo del torneo
 *       en ficheros .dat. Requiere que las clases implementen {@link Serializable}.</li>
 *   <li><b>BufferedWriter / BufferedReader</b>: Escritura y lectura eficiente
 *       de ficheros de texto plano (.txt, .log) con buffer de memoria para
 *       exportar informes, rankings y logs de partidos.</li>
 * </ul>
 *
 * PATRÓN DE DISEÑO: Utiliza métodos estáticos (patrón Utility) ya que
 * no necesita mantener estado interno propio.
 */
public class GestorFicheros {

    /** Ruta por defecto para los ficheros de guardado binario. */
    private static final String RUTA_GUARDADO  = "data/partida_guardada.dat";

    /** Ruta por defecto para el log de partidos en texto plano. */
    private static final String RUTA_LOG       = "data/log_torneo.txt";

    /** Ruta por defecto para el ranking de goleadores en texto plano. */
    private static final String RUTA_RANKING   = "data/ranking_goleadores.txt";

    // =====================================================================
    // BLOQUE 1: SERIALIZACIÓN BINARIA (ObjectOutputStream / ObjectInputStream)
    // =====================================================================

    /**
     * Guarda el estado completo del torneo en un fichero binario (.dat)
     * utilizando {@link ObjectOutputStream}.
     *
     * El ObjectOutputStream convierte los objetos Java en una secuencia de
     * bytes que se pueden almacenar en disco. Para que funcione, todas las
     * clases involucradas deben implementar {@link Serializable}.
     *
     * @param torneo  Objeto Torneo con todo el estado de la competición.
     * @param mercado Objeto MercadoFichajes con el estado del mercado.
     * @return Mensaje indicando el resultado de la operación.
     */
    public static String guardarPartidaBinaria(Torneo torneo, MercadoFichajes mercado) {
        // Usamos try-with-resources para cerrar automáticamente los streams
        try (FileOutputStream fos = new FileOutputStream(RUTA_GUARDADO);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {

            // writeObject() serializa el objeto completo y todo su árbol de dependencias
            oos.writeObject(torneo);
            oos.writeObject(mercado);

            // flush() fuerza la escritura de los datos pendientes en el buffer al disco
            oos.flush();

            return "✅ Partida guardada correctamente en: " + RUTA_GUARDADO;

        } catch (IOException e) {
            System.err.println("❌ Error al guardar la partida: " + e.getMessage());
            e.printStackTrace();
            return "❌ Error al guardar la partida: " + e.getMessage();
        }
    }

    /**
     * Carga el estado completo del torneo desde un fichero binario (.dat)
     * utilizando {@link ObjectInputStream}.
     *
     * El ObjectInputStream reconstruye los objetos Java a partir de la
     * secuencia de bytes almacenada previamente por ObjectOutputStream.
     * Es fundamental que las clases tengan el mismo serialVersionUID.
     *
     * @return Array de 2 objetos: [0]=Torneo, [1]=MercadoFichajes, o null si falla.
     */
    public static Object[] cargarPartidaBinaria() {
        File archivo = new File(RUTA_GUARDADO);

        // Verificamos que el fichero existe antes de intentar leerlo
        if (!archivo.exists()) {
            System.err.println("⚠️ No se encontró fichero de guardado en: " + RUTA_GUARDADO);
            return null;
        }

        // Usamos try-with-resources para cerrar automáticamente los streams
        try (FileInputStream fis = new FileInputStream(archivo);
             ObjectInputStream ois = new ObjectInputStream(fis)) {

            // readObject() deserializa el objeto y reconstruye su estado completo
            Torneo torneo           = (Torneo) ois.readObject();
            MercadoFichajes mercado = (MercadoFichajes) ois.readObject();

            System.out.println("✅ Partida cargada correctamente desde: " + RUTA_GUARDADO);
            return new Object[]{torneo, mercado};

        } catch (IOException e) {
            System.err.println("❌ Error de lectura del fichero: " + e.getMessage());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Error de deserialización: Clase no encontrada - " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Verifica si existe un fichero de partida guardada.
     * @return true si el fichero de guardado existe en disco.
     */
    public static boolean existePartidaGuardada() {
        return new File(RUTA_GUARDADO).exists();
    }

    // =====================================================================
    // BLOQUE 2: ESCRITURA DE TEXTO (BufferedWriter)
    // =====================================================================

    /**
     * Exporta el ranking de goleadores a un fichero de texto plano (.txt)
     * utilizando {@link BufferedWriter} para escritura eficiente con buffer.
     *
     * BufferedWriter envuelve a un FileWriter y agrupa las operaciones de
     * escritura en bloques, reduciendo las llamadas al sistema operativo
     * y mejorando significativamente el rendimiento.
     *
     * @param torneo Torneo del que extraer los goleadores.
     * @return Mensaje indicando el resultado de la operación.
     */
    public static String exportarRankingGoleadores(Torneo torneo) {
        ArrayList<Jugador> top = torneo.getTopGoleadores(50);

        // BufferedWriter con try-with-resources para cierre automático
        try (FileWriter fw = new FileWriter(RUTA_RANKING);
             BufferedWriter bw = new BufferedWriter(fw)) {

            // Cabecera del fichero
            bw.write("═══════════════════════════════════════════════════");
            bw.newLine(); // newLine() escribe el separador de línea del SO
            bw.write("  RANKING DE GOLEADORES - " + torneo.getNombre());
            bw.newLine();
            bw.write("═══════════════════════════════════════════════════");
            bw.newLine();
            bw.newLine();

            // Escritura de cada goleador con formato tabular
            int posicion = 1;
            for (Jugador j : top) {
                String linea = String.format("  %02d. %-25s | %-20s | %d goles",
                    posicion++,
                    j.getNombre(),
                    (j.getEquipo() != null ? j.getEquipo().getNombre() : "Sin club"),
                    j.getGoles());
                bw.write(linea);
                bw.newLine();
            }

            // flush() asegura que todos los datos del buffer se escriban al fichero
            bw.flush();

            return "✅ Ranking exportado en: " + RUTA_RANKING;

        } catch (IOException e) {
            System.err.println("❌ Error al exportar ranking: " + e.getMessage());
            return "❌ Error al exportar ranking: " + e.getMessage();
        }
    }

    /**
     * Exporta el log narrativo de un partido a un fichero de texto plano.
     * Utiliza {@link BufferedWriter} para escritura eficiente.
     *
     * @param partido      Partido cuya narración se quiere exportar.
     * @param nombreFichero Nombre del fichero de salida (sin ruta).
     * @return Mensaje indicando el resultado de la operación.
     */
    public static String exportarLogPartido(Partido partido, String nombreFichero) {
        String ruta = "data/" + nombreFichero;

        try (FileWriter fw = new FileWriter(ruta);
             BufferedWriter bw = new BufferedWriter(fw)) {

            bw.write("═══════════════════════════════════════════════════");
            bw.newLine();
            bw.write("  LOG DE PARTIDO");
            bw.newLine();
            bw.write("═══════════════════════════════════════════════════");
            bw.newLine();
            bw.newLine();

            // Escribimos la narración completa del partido
            bw.write(partido.getNarracion());
            bw.newLine();

            // Resultado final
            bw.write(String.format("\n  RESULTADO: %s %d - %d %s",
                partido.getLocal().getNombre(), partido.getGolesLocal(),
                partido.getGolesVisitante(), partido.getVisitante().getNombre()));
            bw.newLine();

            bw.flush();
            return "✅ Log de partido exportado en: " + ruta;

        } catch (IOException e) {
            System.err.println("❌ Error al exportar log: " + e.getMessage());
            return "❌ Error al exportar log: " + e.getMessage();
        }
    }

    // =====================================================================
    // BLOQUE 3: LECTURA DE TEXTO (BufferedReader)
    // =====================================================================

    /**
     * Lee el contenido completo de un fichero de texto plano
     * utilizando {@link BufferedReader} para lectura eficiente con buffer.
     *
     * BufferedReader envuelve a un FileReader y lee los datos en bloques
     * grandes, minimizando las operaciones de I/O con el disco.
     *
     * @param rutaFichero Ruta al fichero de texto a leer.
     * @return Contenido completo del fichero como String, o null si falla.
     */
    public static String leerFicheroTexto(String rutaFichero) {
        File archivo = new File(rutaFichero);

        if (!archivo.exists()) {
            System.err.println("⚠️ Fichero no encontrado: " + rutaFichero);
            return null;
        }

        StringBuilder contenido = new StringBuilder();

        // BufferedReader con try-with-resources
        try (FileReader fr = new FileReader(archivo);
             BufferedReader br = new BufferedReader(fr)) {

            String linea;
            // readLine() lee una línea completa del buffer (hasta \n o EOF)
            while ((linea = br.readLine()) != null) {
                contenido.append(linea);
                contenido.append(System.lineSeparator());
            }

            return contenido.toString();

        } catch (IOException e) {
            System.err.println("❌ Error al leer fichero: " + e.getMessage());
            return null;
        }
    }

    /**
     * Lee el log de un torneo previamente exportado y lo devuelve como String.
     * Utiliza {@link BufferedReader} internamente.
     *
     * @return Contenido del log o mensaje de error.
     */
    public static String leerLogTorneo() {
        String contenido = leerFicheroTexto(RUTA_LOG);
        if (contenido == null) {
            return "⚠️ No hay log de torneo disponible.";
        }
        return contenido;
    }

    /**
     * Exporta un resumen completo del estado del torneo a texto plano,
     * incluyendo equipos activos, eliminados y resultados.
     *
     * @param torneo Torneo a resumir.
     * @return Mensaje indicando el resultado.
     */
    public static String exportarResumenTorneo(Torneo torneo) {
        try (FileWriter fw = new FileWriter(RUTA_LOG);
             BufferedWriter bw = new BufferedWriter(fw)) {

            bw.write("═══════════════════════════════════════════════════");
            bw.newLine();
            bw.write("  RESUMEN DEL TORNEO: " + torneo.getNombre());
            bw.newLine();
            bw.write("  Ronda actual: " + torneo.getNombreRonda());
            bw.newLine();
            bw.write("═══════════════════════════════════════════════════");
            bw.newLine();
            bw.newLine();

            // Equipos activos
            bw.write("  EQUIPOS EN COMPETICIÓN:");
            bw.newLine();
            for (Equipo e : torneo.getEquipos()) {
                bw.write("    • " + e.getNombre() + " (" + e.getPais() + ")");
                bw.newLine();
            }
            bw.newLine();

            // Eliminatorias actuales
            bw.write("  ELIMINATORIAS ACTUALES:");
            bw.newLine();
            for (Eliminatoria elim : torneo.getEliminatorias()) {
                bw.write("    " + elim.getResumen());
                bw.newLine();
            }

            bw.flush();
            return "✅ Resumen exportado en: " + RUTA_LOG;

        } catch (IOException e) {
            System.err.println("❌ Error al exportar resumen: " + e.getMessage());
            return "❌ Error al exportar resumen: " + e.getMessage();
        }
    }

    // Constructor privado: clase de utilidades, no se debe instanciar
    private GestorFicheros() {}
}
