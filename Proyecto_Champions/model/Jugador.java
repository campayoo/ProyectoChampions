package model;

import interfaces.Transferible;
import java.io.Serializable;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

/**
 * Clase Jugador: Representa al recurso humano principal del simulador.
 *
 * Gestiona el perfil completo de un futbolista, incluyendo:
 * - Atributos técnicos (Ataque/Defensa) y físicos (Energía/Velocidad).
 * - Lógica de compatibilidad posicional y penalizaciones por rol.
 * - Registro de logros individuales (Goles, Tarjetas).
 * - Integración con el sistema de mercado e índices de competitividad.
 *
 * Implementa {@link Transferible} para habilitar las operaciones de fichaje,
 * {@link Comparable} para el ranking de goleadores mediante {@link java.util.TreeSet},
 * y {@link Serializable} (heredada de {@link Persona}) para la persistencia
 * en ficheros binarios con {@link java.io.ObjectOutputStream}.
 */
public class Jugador extends Persona implements Transferible, Comparable<Jugador>, Serializable {

    /** Identificador de versión para la serialización binaria. */
    private static final long serialVersionUID = 2L;

    // --- BLOQUE: ATRIBUTOS DE RENDIMIENTO ---
    /** Demarcación natural del jugador (e.g., "POR", "DC", "MC"). */
    private String posicion;

    /** Capacidad ofensiva: puntuación de finalización y definición (0-100). */
    private int    ataque;

    /** Capacidad defensiva: puntuación de marcaje y colocación (0-100). */
    private int    defensa;

    /** Potencial físico máximo del jugador. */
    private int    energiaMax;

    /** Estado físico actual durante la jornada (disminuye con el desgaste). */
    private int    energiaActual;

    /** Capacidad de desborde y velocidad punta (0-100). */
    private int    velocidad;

    // --- BLOQUE: ATRIBUTOS COMERCIALES ---
    /** Precio estimado del jugador en millones de euros (M€). */
    private double valorMercado;

    /** Club al que pertenece actualmente; null si es agente libre. */
    private transient Equipo equipo;

    /** Indica si el jugador está disponible en el mercado de fichajes. */
    private boolean disponible;

    // --- BLOQUE: ESTADÍSTICAS Y TÁCTICA ---
    /** Goles marcados acumulados en el torneo actual. */
    private int goles;

    /** Pases de gol registrados (asistencias). */
    private int asistencias;

    /** Tarjetas amarillas acumuladas durante el torneo. */
    private int tarjetasAmarillas;

    /** Indica si el jugador forma parte del once inicial. */
    private boolean titular;

    /** Rol asignado en la pizarra táctica (null si no está alineado). */
    private String posicionNodo = null;

    // --- BLOQUE: SISTEMA DE INTELIGENCIA POSICIONAL ---

    /**
     * Enumeración que define los niveles de afinidad posicional de un jugador.
     * Se utiliza para calcular penalizaciones de rendimiento según la posición asignada.
     */
    public enum Compatibilidad {
        /** El jugador juega en su posición natural o en la misma zona táctica. */
        NATURAL,
        /** El jugador juega en una posición secundaria conocida. */
        AFIN,
        /** El jugador juega completamente fuera de su zona de influencia. */
        OPUESTA,
        /** El jugador no tiene posición táctica asignada en la pizarra. */
        SIN_ASIGNAR
    }

    /**
     * Diccionario de Afinidades Tácticas:
     * Define qué posiciones secundarias puede ocupar un jugador sin perder
     * eficacia de forma extrema. Se usa un mapa estático para consulta rápida O(1).
     */
    private static final java.util.Map<String, Set<String>> AFINES = new java.util.HashMap<>();
    static {
        AFINES.put("DFC", new HashSet<>(Arrays.asList("LD", "LI", "MCD", "DEF")));
        AFINES.put("LD",  new HashSet<>(Arrays.asList("DFC", "MD", "DEF", "CAD")));
        AFINES.put("LI",  new HashSet<>(Arrays.asList("DFC", "MI", "DEF", "CAI")));
        AFINES.put("MC",  new HashSet<>(Arrays.asList("MCD", "MCO", "MI", "MD", "MED")));
        AFINES.put("DC",  new HashSet<>(Arrays.asList("ED", "EI", "SD", "MCO")));
    }

    /**
     * Constructor: Crea un nuevo jugador con sus estadísticas base.
     *
     * @param id            Identificador único en la base de datos.
     * @param nombre        Nombre deportivo del futbolista.
     * @param edad          Edad biológica en años.
     * @param nacionalidad  País de origen.
     * @param posicion      Demarcación natural (e.g., "POR", "DC").
     * @param ataque        Puntuación ofensiva (0-100).
     * @param defensa       Puntuación defensiva (0-100).
     * @param energia       Fondo físico máximo (energía).
     * @param velocidad     Velocidad punta (0-100).
     * @param valorMercado  Valoración económica en millones de euros.
     */
    public Jugador(int id, String nombre, int edad, String nacionalidad,
                   String posicion, int ataque, int defensa,
                   int energia, int velocidad, double valorMercado) {
        super(id, nombre, edad, nacionalidad);
        this.posicion      = posicion;
        this.ataque        = ataque;
        this.defensa       = defensa;
        this.energiaMax    = energia;
        this.energiaActual = energia;
        this.velocidad     = velocidad;
        this.valorMercado  = valorMercado;
        this.disponible    = false;
        this.titular       = false;
    }

    // ---------------------------------------------------------------------
    // BLOQUE: IMPLEMENTACIÓN DE CONTRATOS (INTERFACES)
    // ---------------------------------------------------------------------

    /**
     * Devuelve el rol que desempeña el jugador en el simulador.
     *
     * @return Cadena descriptiva con la posición natural del jugador.
     */
    @Override
    public String getRol() { return "Futbolista (" + posicion + ")"; }

    /** {@inheritDoc} */
    @Override public double  getValorMercado()             { return valorMercado; }

    /** {@inheritDoc} */
    @Override public void    setValorMercado(double v)     { this.valorMercado = v; }

    /** {@inheritDoc} */
    @Override public Equipo  getEquipo()                   { return equipo; }

    /** {@inheritDoc} */
    @Override public void    setEquipo(Equipo e)           { this.equipo = e; }

    /** {@inheritDoc} */
    @Override public boolean estaDisponible()              { return disponible; }

    /** {@inheritDoc} */
    @Override public void    setDisponible(boolean d)      { this.disponible = d; }

    /**
     * Ordenación natural por relevancia deportiva: Goles (desc) > Nombre (asc) > ID (asc).
     * Se utiliza en el {@link java.util.TreeSet} del ranking de goleadores.
     *
     * @param otro Jugador contra el que se compara.
     * @return Valor negativo, cero o positivo según el orden natural.
     */
    @Override
    public int compareTo(Jugador otro) {
        // Prioridad 1: Mayor número de goles primero
        int cmp = Integer.compare(otro.goles, this.goles);
        if (cmp != 0) return cmp;
        // Prioridad 2: Orden alfabético por nombre
        cmp = this.nombre.compareTo(otro.nombre);
        if (cmp != 0) return cmp;
        // Prioridad 3: Diferenciador único por ID
        return Integer.compare(this.id, otro.id);
    }

    // ---------------------------------------------------------------------
    // BLOQUE: LÓGICA DE ADAPTACIÓN TÁCTICA
    // ---------------------------------------------------------------------

    /**
     * Determina el ajuste actual del jugador en la posición que le ha
     * asignado el mánager en la pizarra táctica.
     *
     * @return Nivel de compatibilidad con la posición asignada.
     */
    public Compatibilidad getCompatibilidad() {
        if (posicionNodo == null) return Compatibilidad.SIN_ASIGNAR;
        return getCompatibilidadConNodo(posicionNodo);
    }

    /**
     * Evalúa si una posición específica es adecuada para este jugador.
     *
     * @param nodo Identificador de la posición a evaluar (e.g., "MC", "DC").
     * @return Nivel de compatibilidad del jugador con el nodo dado.
     */
    public Compatibilidad getCompatibilidadConNodo(String nodo) {
        if (nodo == null) return Compatibilidad.SIN_ASIGNAR;

        // Ajuste Perfecto: Misma posición o misma zona de influencia táctica
        if (posicion.equals(nodo) || mismaCategoriaGeneral(posicion, nodo))
            return Compatibilidad.NATURAL;

        // Ajuste Admisible: Posición secundaria registrada en el diccionario
        Set<String> afines = AFINES.get(posicion);
        if (afines != null && afines.contains(nodo))
            return Compatibilidad.AFIN;

        // Ajuste Deficiente: Fuera de zona de influencia natural
        return Compatibilidad.OPUESTA;
    }

    /**
     * Compara si dos abreviaturas posicionales pertenecen a la misma zona táctica.
     *
     * @param p1 Primera posición a comparar.
     * @param p2 Segunda posición a comparar.
     * @return true si ambas posiciones pertenecen al mismo bloque táctico.
     */
    private boolean mismaCategoriaGeneral(String p1, String p2) {
        return categoriaGeneral(p1).equals(categoriaGeneral(p2));
    }

    /**
     * Clasifica las abreviaturas posicionales en bloques tácticos generales.
     * Los bloques son: POR (Portero), DEF (Defensa), MED (Mediocampo), DEL (Delantera).
     *
     * @param pos Abreviatura de la posición (e.g., "MC", "DFC", "DC").
     * @return Bloque táctico al que pertenece la posición.
     */
    public static String categoriaGeneral(String pos) {
        if (pos == null) return "OTRO";
        switch (pos) {
            case "POR":
                return "POR";
            case "DEF":
            case "LD":
            case "LI":
            case "DFC":
            case "CAD":
            case "CAI":
                return "DEF";
            case "MED":
            case "MC":
            case "MCD":
            case "MCO":
            case "MI":
            case "MD":
                return "MED";
            case "DEL":
            case "DC":
            case "ED":
            case "EI":
            case "SD":
                return "DEL";
            default:
                return "OTRO";
        }
    }

    // ---------------------------------------------------------------------
    // BLOQUE: ALGORITMOS DE RENDIMIENTO Y ESTADO FÍSICO
    // ---------------------------------------------------------------------

    /**
     * Calcula la media real en campo (OVR) aplicando penalizaciones
     * según la compatibilidad con la posición asignada en la pizarra.
     *
     * - NATURAL / SIN_ASIGNAR: Sin penalización.
     * - AFÍN: Penalización de -10 puntos.
     * - OPUESTA: Penalización de -40 puntos.
     *
     * @return Valoración OVR ajustada (mínimo 1).
     */
    public int getOvrEnPosicion() {
        int base = getMediaGeneral();
        switch (getCompatibilidad()) {
            case NATURAL:
            case SIN_ASIGNAR:
                return base;
            case AFIN:
                return Math.max(1, base - 10);
            case OPUESTA:
                return Math.max(1, base - 40);
            default:
                return base;
        }
    }

    /**
     * Calcula la valoración media bruta del jugador según su perfil posicional.
     * Los pesos de ataque, defensa y energía varían según la demarcación natural.
     *
     * @return Media general del jugador (OVR bruto).
     */
    public int getMediaGeneral() {
        switch (posicion) {
            case "POR":
                return (int) (defensa * 0.70 + ataque * 0.05 + energiaMax * 0.25);
            case "DEF":
            case "LD":
            case "LI":
            case "DFC":
                return (int) (defensa * 0.65 + ataque * 0.15 + energiaMax * 0.20);
            case "MED":
            case "MC":
            case "MCD":
            case "MCO":
                return (int) (ataque * 0.45 + defensa * 0.35 + energiaMax * 0.20);
            case "DEL":
            case "DC":
            case "ED":
            case "EI":
                return (int) (ataque * 0.75 + defensa * 0.05 + energiaMax * 0.20);
            default:
                return (ataque + defensa + energiaMax) / 3;
        }
    }

    /**
     * Factor corrector que combina la Calidad (OVR) con la Fatiga acumulada.
     * La energía influye en el 50% de la eficacia final del jugador.
     *
     * @return Factor de rendimiento entre 0.0 y 1.0.
     */
    public double getFactorRendimiento() {
        double ratioEnergia = energiaActual / (double) energiaMax;
        return (getOvrEnPosicion() / 100.0) * (0.5 + 0.5 * ratioEnergia);
    }

    // ---------------------------------------------------------------------
    // BLOQUE: ACCIONES Y MANTENIMIENTO DE ESTADO
    // ---------------------------------------------------------------------

    /**
     * Reduce la estamina del jugador simulando el esfuerzo físico en el campo.
     * La energía nunca baja de 0.
     *
     * @param cantidad Puntos de energía a descontar.
     */
    public void desgastar(int cantidad) {
        energiaActual = Math.max(0, energiaActual - cantidad);
    }

    /**
     * Restaura completamente la condición física del futbolista
     * al finalizar un partido o durante el descanso entre jornadas.
     */
    public void recuperarEnergia() {
        energiaActual = energiaMax;
    }

    /** Incrementa en 1 el contador de goles del jugador. */
    public void addGol() { goles++; }

    /** Incrementa en 1 el contador de tarjetas amarillas. */
    public void addAmarilla() { tarjetasAmarillas++; }

    // --- BLOQUE: ACCESORES TÉCNICOS (Getters y Setters) ---

    /** @return Posición natural del jugador. */
    public String  getPosicion()      { return posicion; }

    /** @return Puntuación ofensiva. */
    public int     getAtaque()        { return ataque; }

    /** @return Puntuación defensiva. */
    public int     getDefensa()       { return defensa; }

    /** @return Energía máxima del jugador. */
    public int     getEnergiaMax()    { return energiaMax; }

    /** @return Energía actual del jugador. */
    public int     getEnergiaActual() { return energiaActual; }

    /** @return Velocidad del jugador. */
    public int     getVelocidad()     { return velocidad; }

    /** @return Goles marcados en el torneo. */
    public int     getGoles()         { return goles; }

    /** @return Tarjetas amarillas acumuladas. */
    public int     getTarjetasAmarillas() { return tarjetasAmarillas; }

    /** @return true si el jugador es titular en el once inicial. */
    public boolean isTitular()        { return titular; }

    /** @param t true para marcar como titular, false como suplente. */
    public void    setTitular(boolean t) { this.titular = t; }

    /** @return Posición asignada en la pizarra táctica (puede ser null). */
    public String  getPosicionNodo()  { return posicionNodo; }

    /** @param n Posición a asignar en la pizarra táctica. */
    public void    setPosicionNodo(String n) { this.posicionNodo = n; }

    /**
     * Representación textual del jugador con su posición y media general.
     *
     * @return Cadena con formato "Nombre (Posición) [OVR]".
     */
    @Override
    public String toString() {
        return nombre + " (" + posicion + ") [" + getMediaGeneral() + "]";
    }
}
