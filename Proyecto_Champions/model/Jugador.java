package model;

import interfaces.Transferible;
import java.util.Set;

/**
 * Clase Jugador: Representa al recurso humano principal del simulador.
 * 
 * Gestiona el perfil completo de un futbolista, incluyendo:
 * - Atributos técnicos (Ataque/Defensa) y físicos (Energía/Velocidad).
 * - Lógica de compatibilidad posicional y penalizaciones por rol.
 * - Registro de logros individuales (Goles, Tarjetas).
 * - Integración con el sistema de mercado e índices de competitividad.
 */
public class Jugador extends Persona implements Transferible, Comparable<Jugador> {

    // --- BLOQUE: ATRIBUTOS DE RENDIMIENTO ---
    private String posicion;      // Demarcación natural (e.g., "POR", "DC")
    private int    ataque;        // Capacidad ofensiva / Definición
    private int    defensa;       // Capacidad destructiva / Colocación
    private int    energiaMax;    // Potencial físico máximo
    private int    energiaActual; // Estado físico actual durante la jornada
    private int    velocidad;     // Capacidad de desborde

    // --- BLOQUE: ATRIBUTOS COMERCIALES ---
    private double valorMercado;  // Precio estimado en M€
    private Equipo equipo;        // Club de pertenencia
    private boolean disponible;   // Estado de transferibilidad

    // --- BLOQUE: ESTADÍSTICAS Y TÁCTICA ---
    private int goles;            // Goles marcados en el torneo
    private int asistencias;      // Pases de gol (tracking opcional)
    private int tarjetasAmarillas; // Disciplina acumulada
    private boolean titular;      // Determina si salta al campo inicialmente
    private String posicionNodo = null; // Rol asignado en la pizarra táctica

    // --- BLOQUE: SISTEMA DE INTELIGENCIA POSICIONAL ---
    public enum Compatibilidad { NATURAL, AFIN, OPUESTA, SIN_ASIGNAR }

    /**
     * Diccionario de Afinidades Tácticas:
     * Define qué posiciones secundarias puede ocupar un jugador sin perder eficacia extrema.
     */
    private static final java.util.Map<String, Set<String>> AFINES = new java.util.HashMap<>();
    static {
        AFINES.put("DFC",  Set.of("LD","LI","MCD","DEF"));
        AFINES.put("LD",   Set.of("DFC","MD","DEF","CAD"));
        AFINES.put("LI",   Set.of("DFC","MI","DEF","CAI"));
        AFINES.put("MC",   Set.of("MCD","MCO","MI","MD","MED"));
        AFINES.put("DC",   Set.of("ED","EI","SD","MCO"));
    }

    /**
     * Constructor: Crea un nuevo activo deportivo con stats base.
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

    @Override
    public String getRol() { return "Futbolista (" + posicion + ")"; }

    @Override public double  getValorMercado()             { return valorMercado; }
    @Override public void    setValorMercado(double v)     { this.valorMercado = v; }
    @Override public Equipo  getEquipo()                   { return equipo; }
    @Override public void    setEquipo(Equipo e)           { this.equipo = e; }
    @Override public boolean estaDisponible()              { return disponible; }
    @Override public void    setDisponible(boolean d)      { this.disponible = d; }

    /**
     * Ordenación natural por relevancia deportiva: Goles > Nombre > ID.
     */
    @Override
    public int compareTo(Jugador otro) {
        // Bloque: Jerarquía por Goleo
        int cmp = Integer.compare(otro.goles, this.goles); 
        if (cmp != 0) return cmp;
        // Bloque: Jerarquía Alfabética
        cmp = this.nombre.compareTo(otro.nombre);
        if (cmp != 0) return cmp;
        // Bloque: Diferenciador Único
        return Integer.compare(this.id, otro.id);
    }

    // ---------------------------------------------------------------------
    // BLOQUE: LÓGICA DE ADAPTACIÓN TÁCTICA
    // ---------------------------------------------------------------------

    /**
     * Determina el ajuste actual del jugador en la posición que le ha asignado el mánager.
     */
    public Compatibilidad getCompatibilidad() {
        if (posicionNodo == null) return Compatibilidad.SIN_ASIGNAR;
        return getCompatibilidadConNodo(posicionNodo);
    }

    /**
     * Evalúa si una posición específica es adecuada para este jugador.
     */
    public Compatibilidad getCompatibilidadConNodo(String nodo) {
        if (nodo == null) return Compatibilidad.SIN_ASIGNAR;
        
        // Ajuste Perfecto: Misma posición o misma zona de influencia
        if (posicion.equals(nodo) || mismaCategoriaGeneral(posicion, nodo)) 
            return Compatibilidad.NATURAL;
            
        // Ajuste Admisible: Posición secundaria conocida
        if (AFINES.getOrDefault(posicion, Set.of()).contains(nodo))
            return Compatibilidad.AFIN;
            
        // Ajuste Deficiente: Fuera de zona
        return Compatibilidad.OPUESTA;
    }

    private boolean mismaCategoriaGeneral(String p1, String p2) {
        return categoriaGeneral(p1).equals(categoriaGeneral(p2));
    }

    /**
     * Clasifica las abreviaturas técnicas en bloques tácticos (POR, DEF, MED, DEL).
     */
    public static String categoriaGeneral(String pos) {
        if (pos == null) return "OTRO";
        return switch (pos) {
            case "POR" -> "POR";
            case "DEF","LD","LI","DFC","CAD","CAI" -> "DEF";
            case "MED","MC","MCD","MCO","MI","MD" -> "MED";
            case "DEL","DC","ED","EI","SD" -> "DEL";
            default -> "OTRO";
        };
    }

    // ---------------------------------------------------------------------
    // BLOQUE: ALGORITMOS DE RENDIMIENTO Y ESTADO FÍSICO
    // ---------------------------------------------------------------------

    /**
     * Calcula la media real en campo (OVR) aplicando penalizaciones de posición.
     */
    public int getOvrEnPosicion() {
        int base = getMediaGeneral();
        return switch (getCompatibilidad()) {
            case NATURAL, SIN_ASIGNAR -> base;
            case AFIN    -> Math.max(1, base - 10); // Menor impacto por adaptación
            case OPUESTA -> Math.max(1, base - 40); // Gran pérdida por improvisación
        };
    }

    /**
     * Retorna la valoración media bruta según el perfil posicional del jugador.
     */
    public int getMediaGeneral() {
        return switch (posicion) {
            case "POR" -> (int) (defensa * 0.70 + ataque * 0.05 + energiaMax * 0.25);
            case "DEF", "LD", "LI", "DFC" -> (int) (defensa * 0.65 + ataque * 0.15 + energiaMax * 0.20);
            case "MED", "MC", "MCD", "MCO" -> (int) (ataque * 0.45 + defensa * 0.35 + energiaMax * 0.20);
            case "DEL", "DC", "ED", "EI" -> (int) (ataque * 0.75 + defensa * 0.05 + energiaMax * 0.20);
            default -> (ataque + defensa + energiaMax) / 3;
        };
    }

    /**
     * Factor corrector que combina Calidad (OVR) y Fatiga acumulada.
     */
    public double getFactorRendimiento() {
        double ratioEnergia = energiaActual / (double) energiaMax;
        // La energía influye en el 50% de la eficacia final
        return (getOvrEnPosicion() / 100.0) * (0.5 + 0.5 * ratioEnergia);
    }

    // ---------------------------------------------------------------------
    // BLOQUE: ACCIONES Y MANTENIMIENTO
    // ---------------------------------------------------------------------

    /**
     * Reduce la estamina del jugador por el esfuerzo en el campo.
     */
    public void desgastar(int cantidad) {
        energiaActual = Math.max(0, energiaActual - cantidad);
    }

    /**
     * Restaura completamente la condición física del futbolista.
     */
    public void recuperarEnergia() {
        energiaActual = energiaMax;
    }

    public void addGol() { goles++; }
    public void addAmarilla() { tarjetasAmarillas++; }

    // --- ACCESORES TÉCNICOS ---
    public String  getPosicion()      { return posicion; }
    public int     getAtaque()        { return ataque; }
    public int     getDefensa()       { return defensa; }
    public int     getEnergiaMax()    { return energiaMax; }
    public int     getEnergiaActual() { return energiaActual; }
    public int     getGoles()         { return goles; }
    public boolean isTitular()        { return titular; }
    public void    setTitular(boolean t) { this.titular = t; }
    public String  getPosicionNodo()  { return posicionNodo; }
    public void    setPosicionNodo(String n) { this.posicionNodo = n; }

    @Override
    public String toString() {
        return nombre + " (" + posicion + ") [" + getMediaGeneral() + "]";
    }
}
