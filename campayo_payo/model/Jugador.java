package model;

import interfaces.Transferible;

/**
 * Jugador — subclase de Persona que implementa Transferible.
 *
 * HERENCIA  : extiende Persona (atributos comunes a toda persona).
 * INTERFACES: implementa Transferible (puede ser fichado) y Comparable
 *             (para ordenarlo en TreeSet por goles, descend.).
 */
public class Jugador extends Persona implements Transferible, Comparable<Jugador> {

    // ── Atributos deportivos ─────────────────────────────────────────────
    private String posicion;      // POR | DEF | MED | DEL
    private int    ataque;        // 1-99
    private int    defensa;       // 1-99
    private int    energiaMax;    // máximo de energía
    private int    energiaActual; // decrece durante el partido
    private int    velocidad;     // 1-99

    // ── Atributos de mercado ─────────────────────────────────────────────
    private double valorMercado;  // millones de euros
    private Equipo equipo;
    private boolean disponible;

    // ── Estadísticas del torneo ──────────────────────────────────────────
    private int goles;
    private int asistencias;
    private int tarjetasAmarillas;
    private boolean titular;      // si está en el once inicial

    // ─────────────────────────────────────────────────────────────────────
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

    // ── Herencia: implementación obligatoria ─────────────────────────────
    @Override
    public String getRol() { return "Jugador · " + posicion; }

    // ── Interfaces: Transferible ─────────────────────────────────────────
    @Override public double  getValorMercado()             { return valorMercado; }
    @Override public void    setValorMercado(double v)     { this.valorMercado = v; }
    @Override public Equipo  getEquipo()                   { return equipo; }
    @Override public void    setEquipo(Equipo e)           { this.equipo = e; }
    @Override public boolean estaDisponible()              { return disponible; }
    @Override public void    setDisponible(boolean d)      { this.disponible = d; }

    // ── Comparable: ordena por goles (desc) para TreeSet goleadores ──────
    @Override
    public int compareTo(Jugador otro) {
        int cmp = Integer.compare(otro.goles, this.goles); // desc
        if (cmp != 0) return cmp;
        return this.nombre.compareTo(otro.nombre);         // tiebreak alfabético
    }

    // ── Lógica deportiva ─────────────────────────────────────────────────

    /**
     * Media general ponderada según la posición.
     * POR depende más de defensa; DEL depende más de ataque.
     */
    public int getMediaGeneral() {
        return switch (posicion) {
            case "POR" -> (int) (defensa * 0.70 + ataque * 0.10 + energiaMax * 0.20);
            case "DEF" -> (int) (defensa * 0.60 + ataque * 0.20 + energiaMax * 0.20);
            case "MED" -> (int) (ataque  * 0.40 + defensa * 0.40 + energiaMax * 0.20);
            case "DEL" -> (int) (ataque  * 0.65 + defensa * 0.10 + energiaMax * 0.25);
            default    -> (ataque + defensa + energiaMax) / 3;
        };
    }

    /**
     * Factor de rendimiento en tiempo real.
     * Combina media general con el porcentaje de energía restante.
     */
    public double getFactorRendimiento() {
        double factorEnergia = energiaActual / (double) energiaMax;
        return (getMediaGeneral() / 100.0) * (0.6 + 0.4 * factorEnergia);
    }

    /** Reduce energía al jugar (mín = 0). */
    public void desgastar(int cantidad) {
        energiaActual = Math.max(0, energiaActual - cantidad);
    }

    /** Restaura energía al 100 % (descanso / media parte). */
    public void recuperarEnergia() {
        energiaActual = energiaMax;
    }

    // ── Estadísticas ─────────────────────────────────────────────────────
    public int     getGoles()              { return goles; }
    public void    addGol()               { goles++; }
    public int     getAsistencias()        { return asistencias; }
    public void    addAsistencia()         { asistencias++; }
    public int     getTarjetasAmarillas()  { return tarjetasAmarillas; }
    public void    addAmarilla()           { tarjetasAmarillas++; }

    // ── Getters de atributos ──────────────────────────────────────────────
    public String  getPosicion()           { return posicion; }
    public int     getAtaque()             { return ataque; }
    public int     getDefensa()            { return defensa; }
    public int     getEnergiaMax()         { return energiaMax; }
    public int     getEnergiaActual()      { return energiaActual; }
    public int     getVelocidad()          { return velocidad; }
    public boolean isTitular()             { return titular; }
    public void    setTitular(boolean t)   { this.titular = t; }

    @Override
    public String toString() {
        return String.format("[%d] %-22s | %-3s | Media:%2d | ⚽%d | %.1fM€",
                id, nombre, posicion, getMediaGeneral(), goles, valorMercado);
    }
}
