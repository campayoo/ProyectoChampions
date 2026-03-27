package model;

import java.util.*;

/**
 * Torneo — gestiona la UCL desde Octavos hasta la Final.
 *
 * USO DE ArrayList : equipos activos y eliminados (orden importa, acceso por índice).
 * USO DE HashSet   : idsRegistrados garantiza que no haya IDs de jugador duplicados
 *                    en todo el sistema (inserción O(1), búsqueda O(1)).
 * USO DE TreeSet   : tableroGoleadores mantiene los jugadores ordenados por goles
 *                    (desc) de forma automática gracias a Comparable<Jugador>.
 */
public class Torneo {

    private final String                 nombre;
    private final ArrayList<Equipo>      equipos;           // ← ArrayList
    private final ArrayList<Equipo>      equiposEliminados; // ← ArrayList
    private final ArrayList<Eliminatoria> eliminatorias;    // ← ArrayList
    private final TreeSet<Jugador>       tableroGoleadores; // ← TreeSet (auto-ordenado)
    private final HashSet<Integer>       idsRegistrados;    // ← HashSet (sin duplicados)

    private Equipo equipoUsuario;
    private int    rondaActual; // 0=Octavos, 1=Cuartos, 2=Semis, 3=Final

    private static final String[] NOMBRES_RONDA =
        {"Octavos de Final", "Cuartos de Final", "Semifinales", "FINAL"};

    // ─────────────────────────────────────────────────────────────────────
    public Torneo(String nombre) {
        this.nombre            = nombre;
        this.equipos           = new ArrayList<>();           // ← ArrayList
        this.equiposEliminados = new ArrayList<>();           // ← ArrayList
        this.eliminatorias     = new ArrayList<>();           // ← ArrayList
        // TreeSet usa Comparable<Jugador>.compareTo → ordena por goles desc
        this.tableroGoleadores = new TreeSet<>();             // ← TreeSet
        // HashSet previene IDs duplicados de jugadores en todo el torneo
        this.idsRegistrados    = new HashSet<>();             // ← HashSet
        this.rondaActual       = 0;
    }

    // ── Carga de equipos ─────────────────────────────────────────────────

    /** Agrega un equipo y registra los IDs de sus jugadores en el HashSet. */
    public void agregarEquipo(Equipo equipo) {
        equipos.add(equipo); // ArrayList.add

        // USO DE HashSet: add() devuelve false si el ID ya existe (duplicado)
        for (Jugador j : equipo.getPlantilla()) {
            if (!idsRegistrados.add(j.getId())) { // ← HashSet.add (O(1))
                System.err.println("⚠ ID duplicado detectado: " + j.getId()
                        + " (" + j.getNombre() + ")");
            }
        }
        // Añade jugadores al TreeSet de goleadores (se ordena solo)
        tableroGoleadores.addAll(equipo.getPlantilla()); // ← TreeSet.addAll
    }

    /**
     * Verifica si un ID de jugador ya está registrado.
     * USO DE HashSet: contains() en O(1).
     */
    public boolean existeIdJugador(int id) {
        return idsRegistrados.contains(id); // ← HashSet.contains
    }

    /**
     * Registra un ID nuevo (ej. al fichar un jugador externo).
     * @return false si ya existía (duplicado).
     */
    public boolean registrarNuevoId(int id) {
        return idsRegistrados.add(id); // ← HashSet.add
    }

    // ── Generación de cruces ──────────────────────────────────────────────

    /** Genera los cruces aleatorios para la ronda actual. */
    public void generarCruces() {
        eliminatorias.clear();
        ArrayList<Equipo> mezclados = new ArrayList<>(equipos); // copia
        Collections.shuffle(mezclados);

        boolean esFinal = (rondaActual == 3);
        for (int i = 0; i + 1 < mezclados.size(); i += 2) {
            eliminatorias.add(
                new Eliminatoria(mezclados.get(i), mezclados.get(i + 1), !esFinal)
            );
        }
    }

    // ── Avance de ronda ───────────────────────────────────────────────────

    /**
     * Mueve los equipos eliminados a la lista de eliminados y
     * establece la nueva lista activa con los clasificados.
     */
    public void avanzarRonda(ArrayList<Equipo> clasificados) {
        for (Equipo e : equipos) {
            if (!clasificados.contains(e)) {
                equiposEliminados.add(e);      // ArrayList.add
                // Poner sus jugadores disponibles en el mercado (por defecto false)
            }
        }
        equipos.clear();
        equipos.addAll(clasificados);          // ArrayList.addAll
        rondaActual++;
        if (rondaActual < 4) generarCruces();
    }

    // ── Goleadores ────────────────────────────────────────────────────────

    /**
     * Devuelve los N máximos goleadores.
     * El TreeSet ya está ordenado (por compareTo de Jugador), sólo cortamos.
     */
    public ArrayList<Jugador> getTopGoleadores(int n) {
        ArrayList<Jugador> top = new ArrayList<>();
        int count = 0;
        // USO DE Iterator sobre TreeSet (orden garantizado)
        Iterator<Jugador> it = tableroGoleadores.iterator(); // ← Iterator
        while (it.hasNext() && count < n) {
            Jugador j = it.next();
            if (j.getGoles() > 0) { top.add(j); count++; }
        }
        return top;
    }

    /** Reconstruye el TreeSet con datos actualizados (después de cada partido). */
    public void refrescarGoleadores() {
        tableroGoleadores.clear();
        for (Equipo e : equipos)           tableroGoleadores.addAll(e.getPlantilla());
        for (Equipo e : equiposEliminados) tableroGoleadores.addAll(e.getPlantilla());
    }

    // ── Info de ronda ─────────────────────────────────────────────────────

    public String getNombreRonda() {
        return rondaActual < NOMBRES_RONDA.length ? NOMBRES_RONDA[rondaActual] : "Torneo Finalizado";
    }

    public boolean isTerminado() { return rondaActual >= 4 || equipos.size() <= 1; }

    // ── Getters ───────────────────────────────────────────────────────────
    public String                  getNombre()            { return nombre; }
    public ArrayList<Equipo>       getEquipos()           { return equipos; }
    public ArrayList<Equipo>       getEquiposEliminados() { return equiposEliminados; }
    public ArrayList<Eliminatoria> getEliminatorias()     { return eliminatorias; }
    public TreeSet<Jugador>        getTableroGoleadores() { return tableroGoleadores; }
    public HashSet<Integer>        getIdsRegistrados()    { return idsRegistrados; }
    public Equipo                  getEquipoUsuario()     { return equipoUsuario; }
    public void                    setEquipoUsuario(Equipo e) { this.equipoUsuario = e; }
    public int                     getRondaActual()       { return rondaActual; }
}
