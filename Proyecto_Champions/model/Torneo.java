package model;

import java.io.Serializable;
import java.util.*;

/**
 * Clase Torneo: Cerebro organizador de la UEFA Champions League.
 *
 * Orquesta el ciclo completo de la competición, desde el sorteo de octavos
 * hasta la coronación del campeón en la gran final.
 *
 * Implementa {@link Serializable} para permitir guardado/carga del estado
 * completo del torneo mediante {@link java.io.ObjectOutputStream}.
 */
public class Torneo implements Serializable {

    /** Versión de serialización para compatibilidad de ficheros. */
    private static final long serialVersionUID = 7L;

    // --- BLOQUE: ESTRUCTURAS DE MEMORIA ---
    /** Nombre oficial del torneo. */
    private final String                 nombre;
    /** Clubes que siguen vivos en la competición. */
    private final ArrayList<Equipo>      equipos;
    /** Clubes eliminados del cuadro. */
    private final ArrayList<Equipo>      equiposEliminados;
    /** Cruces activos de la fase actual. */
    private final ArrayList<Eliminatoria> eliminatorias;
    /** Ranking ordenado por goles (usa compareTo de Jugador). */
    private final TreeSet<Jugador>       tableroGoleadores;
    /** Registro de IDs para evitar colisiones en la base de datos. */
    private final HashSet<Integer>       idsRegistrados;

    /** Club controlado por el jugador humano. */
    private Equipo equipoUsuario;
    /** Índice de fase actual (0=Octavos, 1=Cuartos, 2=Semis, 3=Final). */
    private int    rondaActual;

    /** Denominaciones oficiales de la UEFA para el HUD. */
    private static final String[] NOMBRES_RONDA =
        {"Octavos de Final", "Cuartos de Final", "Semifinales", "GRAN FINAL"};

    /**
     * Constructor: Configura el entorno competitivo inicial.
     * @param nombre Nombre del torneo (e.g., "UEFA CHAMPIONS LEAGUE").
     */
    public Torneo(String nombre) {
        System.out.println("[DEBUG - Torneo] Inicializando torneo: " + nombre);
        this.nombre            = nombre;
        this.equipos           = new ArrayList<>();
        this.equiposEliminados = new ArrayList<>();
        this.eliminatorias     = new ArrayList<>();
        this.tableroGoleadores = new TreeSet<>();
        this.idsRegistrados    = new HashSet<>();
        this.rondaActual       = 0;
    }

    // ---------------------------------------------------------------------
    // BLOQUE: GESTIÓN DE PARTICIPANTES E INTEGRIDAD
    // ---------------------------------------------------------------------

    /**
     * Inscribe a un club y sincroniza sus jugadores con el registro maestro.
     * Detecta IDs duplicados por seguridad de datos.
     *
     * @param equipo Equipo a inscribir en el torneo.
     */
    public void agregarEquipo(Equipo equipo) {
        System.out.println("[DEBUG - Torneo] Inscribiendo equipo: " + equipo.getNombre());
        equipos.add(equipo);
        for (Jugador j : equipo.getPlantilla()) {
            if (!idsRegistrados.add(j.getId())) {
                System.err.println("⚠️ CONFLICTO: ID duplicado detectado (" + j.getId() + ").");
            }
        }
        tableroGoleadores.addAll(equipo.getPlantilla());
    }

    /** @return true si el ID ya existe en la base de datos del torneo. */
    public boolean existeIdJugador(int id) { return idsRegistrados.contains(id); }

    /** Registra un nuevo ID en la base de datos. @return true si se añadió. */
    public boolean registrarNuevoId(int id) { return idsRegistrados.add(id); }

    // ---------------------------------------------------------------------
    // BLOQUE: SORTEOS Y GESTIÓN DE FASES
    // ---------------------------------------------------------------------

    /**
     * Motor de Sorteo: Genera emparejamientos mediante mezcla aleatoria.
     * En la Gran Final se usa partido único; en fases previas, Ida/Vuelta.
     */
    public void generarCruces() {
        System.out.println("\n[DEBUG - Torneo] Generando cruces para la ronda: " + getNombreRonda());
        eliminatorias.clear();
        ArrayList<Equipo> bombos = new ArrayList<>(equipos);
        Collections.shuffle(bombos);

        boolean esFinal = (rondaActual == 3);
        for (int i = 0; i + 1 < bombos.size(); i += 2) {
            Eliminatoria elim = new Eliminatoria(bombos.get(i), bombos.get(i + 1), !esFinal);
            eliminatorias.add(elim);
            System.out.println("   -> Cruce generado: " + elim.getEquipoA().getNombre() + " vs " + elim.getEquipoB().getNombre());
        }
    }

    /**
     * Procesa el cambio de ronda: clasifica ganadores y elimina perdedores.
     * @param clasificados Lista de equipos que avanzan a la siguiente fase.
     */
    public void avanzarRonda(ArrayList<Equipo> clasificados) {
        System.out.println("\n[DEBUG - Torneo] Avanzando de ronda. Equipos clasificados: " + clasificados.size());
        for (Equipo e : equipos) {
            if (!clasificados.contains(e)) {
                equiposEliminados.add(e);
                System.out.println("   ❌ Eliminado: " + e.getNombre());
            } else {
                System.out.println("   ✅ Avanza: " + e.getNombre());
            }
        }
        equipos.clear();
        equipos.addAll(clasificados);
        rondaActual++;

        if (rondaActual < 4 && !clasificados.isEmpty()) {
            generarCruces();
        }
    }

    // ---------------------------------------------------------------------
    // BLOQUE: RANKING Y ESTADÍSTICAS (GOLEADORES)
    // ---------------------------------------------------------------------

    /**
     * Retorna el ranking de los mejores anotadores con al menos 1 gol.
     * @param n Número máximo de goleadores a devolver.
     * @return Lista ordenada de goleadores.
     */
    public ArrayList<Jugador> getTopGoleadores(int n) {
        ArrayList<Jugador> top = new ArrayList<>();
        int count = 0;
        Iterator<Jugador> it = tableroGoleadores.iterator();
        while (it.hasNext() && count < n) {
            Jugador j = it.next();
            if (j.getGoles() > 0) {
                top.add(j);
                count++;
            }
        }
        return top;
    }

    /**
     * Actualiza el TreeSet para reflejar los nuevos goles tras una ronda.
     * Se reconstruye completamente para que el orden sea correcto.
     */
    public void refrescarGoleadores() {
        System.out.println("\n[DEBUG - Torneo] Refrescando y reordenando el TreeSet de Goleadores...");
        tableroGoleadores.clear();
        for (Equipo e : equipos)           tableroGoleadores.addAll(e.getPlantilla());
        for (Equipo e : equiposEliminados) tableroGoleadores.addAll(e.getPlantilla());
        
        System.out.println("[DEBUG - Torneo] Estado actual del TOP 5 Goleadores (TreeSet ordenado por Comparable):");
        int count = 1;
        for (Jugador j : tableroGoleadores) {
            if (j.getGoles() > 0) {
                System.out.println("   #" + count + " " + j.getNombre() + " - " + j.getGoles() + " goles (Media: " + j.getMediaGeneral() + ")");
                count++;
                if (count > 5) break;
            }
        }
    }

    // ---------------------------------------------------------------------
    // BLOQUE: CONSULTAS DE ESTADO DEL TORNEO
    // ---------------------------------------------------------------------

    /** @return Nombre oficial de la ronda actual (e.g., "Cuartos de Final"). */
    public String getNombreRonda() {
        if (rondaActual < NOMBRES_RONDA.length) return NOMBRES_RONDA[rondaActual];
        return "FIN DE LA COMPETICIÓN";
    }

    /**
     * Verifica si el torneo ha llegado a su conclusión.
     * @return true si ya no quedan rondas o hay campeón.
     */
    public boolean isTerminado() {
        if (rondaActual >= 4) return true;
        if (rondaActual == 3 && !eliminatorias.isEmpty()
            && eliminatorias.get(0).isCompleta()) return true;
        return (rondaActual > 0 && equipos.size() <= 1);
    }

    /** @return Equipo campeón del torneo, o null si no ha terminado. */
    public Equipo getGanadorTorneo() {
        if (!isTerminado() || eliminatorias.isEmpty()) return null;
        return eliminatorias.get(eliminatorias.size() - 1).getGanador();
    }

    // --- ACCESORES DE ESTADO ---
    public ArrayList<Equipo>       getEquipos()           { return equipos; }
    public ArrayList<Eliminatoria> getEliminatorias()     { return eliminatorias; }
    public Equipo                  getEquipoUsuario()     { return equipoUsuario; }
    public void                    setEquipoUsuario(Equipo e) { this.equipoUsuario = e; }
    public int                     getRondaActual()       { return rondaActual; }
    public String                  getNombre()            { return nombre; }
}
