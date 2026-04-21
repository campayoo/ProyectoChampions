package model;

import java.util.*;

/**
 * Clase Torneo: Cerebro organizador de la UEFA Champions League.
 * 
 * Orquesta el ciclo completo de la competición, desde el sorteo de octavos
 * hasta la coronación del campeón en la gran final.
 * 
 * DATOS TÉCNICOS:
 * - Gestiona la transición entre fases (Octavos, Cuartos, etc.).
 * - Mantiene un ranking dinámico (TreeSet) de máximos goleadores.
 * - Sortea los cruces de forma aleatoria simulando bombos reales.
 */
public class Torneo {

    // --- BLOQUE: ESTRUCTURAS DE MEMORIA ---
    private final String                 nombre;
    private final ArrayList<Equipo>      equipos;           // Clubes vivos en la competición
    private final ArrayList<Equipo>      equiposEliminados; // Clubes fuera del cuadro
    private final ArrayList<Eliminatoria> eliminatorias;    // Cruces activos de la fase
    private final TreeSet<Jugador>       tableroGoleadores; // Ranking ordenado por goles (TreeSet)
    private final HashSet<Integer>       idsRegistrados;    // Registro para evitar colisión de IDs

    private Equipo equipoUsuario;    // Club controlado por el jugador
    private int    rondaActual;      // Índice de fase (0 a 3)

    // Denominaciones oficiales de la UEFA para el HUD
    private static final String[] NOMBRES_RONDA =
        {"Octavos de Final", "Cuartos de Final", "Semifinales", "GRAN FINAL"};

    /**
     * Constructor: Configura el entorno competitivo inicial.
     */
    public Torneo(String nombre) {
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
     */
    public void agregarEquipo(Equipo equipo) {
        equipos.add(equipo);

        // Bloque: Monitorización de IDs para evitar duplicidad en la base de datos
        for (Jugador j : equipo.getPlantilla()) {
            if (!idsRegistrados.add(j.getId())) {
                System.err.println("🚨 CONFLICTO: Se ha detectado un ID duplicado (" + j.getId() + ").");
            }
        }
        tableroGoleadores.addAll(equipo.getPlantilla());
    }

    public boolean existeIdJugador(int id) { return idsRegistrados.contains(id); }
    public boolean registrarNuevoId(int id) { return idsRegistrados.add(id); }

    // ---------------------------------------------------------------------
    // BLOQUE: SORTEOS Y GESTIÓN DE FASES
    // ---------------------------------------------------------------------

    /**
     * Motor de Sorteo: Genera emparejamientos mediante mezcla aleatoria (shuffle).
     */
    public void generarCruces() {
        eliminatorias.clear();
        ArrayList<Equipo> bombos = new ArrayList<>(equipos);
        Collections.shuffle(bombos); // Mezclado aleatorio simulando sorteo

        boolean esFinal = (rondaActual == 3);
        for (int i = 0; i + 1 < bombos.size(); i += 2) {
            // Regla: Ida/Vuelta en fases previas, Partido único en Gran Final
            eliminatorias.add(new Eliminatoria(bombos.get(i), bombos.get(i + 1), !esFinal));
        }
    }

    /**
     * Procesa el cambio de ronda: Clasifica ganadores y elimina perdedores.
     */
    public void avanzarRonda(ArrayList<Equipo> clasificados) {
        // Bloque: Cribado de equipos eliminados
        for (Equipo e : equipos) {
            if (!clasificados.contains(e)) equiposEliminados.add(e);
        }
        
        equipos.clear();
        equipos.addAll(clasificados);
        rondaActual++;
        
        // Si no hemos terminado, lanzamos nuevo sorteo
        if (rondaActual < 4 && !clasificados.isEmpty()) {
            generarCruces();
        }
    }

    // ---------------------------------------------------------------------
    // BLOQUE: RANKING Y ESTADÍSTICAS (GOLEADORES)
    // ---------------------------------------------------------------------

    /**
     * Retorna el ranking de los mejores anotadores con goles a favor.
     */
    public ArrayList<Jugador> getTopGoleadores(int n) {
        ArrayList<Jugador> top = new ArrayList<>();
        int count = 0;
        
        // Navegación por el set ordenado
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
     * Actualiza el TreeSet para reflejar los nuevos goles tras una ronda de partidos.
     */
    public void refrescarGoleadores() {
        tableroGoleadores.clear();
        for (Equipo e : equipos)           tableroGoleadores.addAll(e.getPlantilla());
        for (Equipo e : equiposEliminados) tableroGoleadores.addAll(e.getPlantilla());
    }

    // ---------------------------------------------------------------------
    // BLOQUE: CONSULTAS DE ESTADO DEL TORNEO
    // ---------------------------------------------------------------------

    public String getNombreRonda() {
        if (rondaActual < NOMBRES_RONDA.length) return NOMBRES_RONDA[rondaActual];
        return "FIN DE LA COMPETICIÓN";
    }

    /**
     * Verifica si se ha llegado a la conclusión del torneo.
     */
    public boolean isTerminado() { 
        // Lógica de finalización por fases o por número de equipos restantes
        if (rondaActual >= 4) return true;
        if (rondaActual == 3 && !eliminatorias.isEmpty() && eliminatorias.get(0).isCompleta()) return true;
        return (rondaActual > 0 && equipos.size() <= 1); 
    }

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
}
