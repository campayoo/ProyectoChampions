package model;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Equipo — representa un club de fútbol con su plantilla y entrenador.
 *
 * USO DE ArrayList: plantilla y titulares se gestionan como ArrayList<Jugador>
 *   porque el orden de inserción importa y se necesita acceso por índice.
 *
 * USO DE Iterator: removerJugador() usa un Iterator para evitar
 *   ConcurrentModificationException al eliminar durante el recorrido.
 */
public class Equipo {

    private String             nombre;
    private String             pais;
    private double             presupuesto;        // millones de euros
    private ArrayList<Jugador> plantilla;           // ← ArrayList
    private ArrayList<Jugador> titulares;           // once inicial ← ArrayList
    private Entrenador         entrenador;
    private String             formacion;           // "4-4-2", "4-3-3", etc.

    // Estadísticas del torneo
    private int golesAFavor;
    private int golesEnContra;

    // ─────────────────────────────────────────────────────────────────────
    public Equipo(String nombre, String pais, double presupuesto) {
        this.nombre        = nombre;
        this.pais          = pais;
        this.presupuesto   = presupuesto;
        this.plantilla     = new ArrayList<>();  // ← ArrayList
        this.titulares     = new ArrayList<>();  // ← ArrayList
        this.formacion     = "4-4-2";
    }

    // ── Gestión de jugadores ──────────────────────────────────────────────

    public void agregarJugador(Jugador j) {
        plantilla.add(j);
        j.setEquipo(this);
    }

    /**
     * Elimina un jugador de la plantilla usando Iterator.
     * USO DE ITERATOR: permite remover de forma segura durante la iteración
     * sin lanzar ConcurrentModificationException.
     */
    public boolean removerJugador(Jugador jugador) {
        Iterator<Jugador> it = plantilla.iterator(); // ← Iterator
        while (it.hasNext()) {
            Jugador j = it.next();
            if (j.getId() == jugador.getId()) {
                it.remove();          // ← it.remove() (método seguro)
                titulares.remove(j);  // también del once si estaba
                j.setEquipo(null);
                return true;
            }
        }
        return false;
    }

    /**
     * Realiza una sustitución en el once inicial.
     * Usa Iterator para buscar al suplido y reemplazarlo.
     * USO DE ITERATOR: recorrido con posibilidad de remoción.
     */
    public boolean realizarCambio(Jugador sale, Jugador entra) {
        Iterator<Jugador> it = titulares.iterator(); // ← Iterator
        while (it.hasNext()) {
            Jugador t = it.next();
            if (t.getId() == sale.getId()) {
                it.remove();
                sale.setTitular(false);
                entra.setTitular(true);
                titulares.add(entra);
                return true;
            }
        }
        return false;
    }

    /**
     * Establece automáticamente el mejor once posible según la formación.
     * Distribuye jugadores por posición respetando el esquema táctico.
     */
    public void establecerMejorOnce() {
        titulares.clear();
        int[] cuotas = parseFormacion(formacion); // [POR, DEF, MED, DEL]
        String[] posiciones = {"POR", "DEF", "MED", "DEL"};

        for (int p = 0; p < posiciones.length; p++) {
            int cuota   = cuotas[p];
            int puestos = 0;

            // Ordenamos la plantilla para coger a los mejores (sin Collections.sort
            // para demostrar el iterator de búsqueda)
            Jugador mejorNoUsado;
            while (puestos < cuota) {
                mejorNoUsado = null;
                // USO DE Iterator para recorrer plantilla y encontrar el mejor
                Iterator<Jugador> it = plantilla.iterator(); // ← Iterator
                while (it.hasNext()) {
                    Jugador j = it.next();
                    if (j.getPosicion().equals(posiciones[p]) && !titulares.contains(j)) {
                        if (mejorNoUsado == null || j.getMediaGeneral() > mejorNoUsado.getMediaGeneral()) {
                            mejorNoUsado = j;
                        }
                    }
                }
                if (mejorNoUsado != null) {
                    mejorNoUsado.setTitular(true);
                    titulares.add(mejorNoUsado);
                    puestos++;
                } else break; // no hay más jugadores en esa posición
            }
        }
    }

    /** Parsea "4-3-3" → [1, 4, 3, 3]. */
    private int[] parseFormacion(String f) {
        try {
            String[] partes = f.split("-");
            return new int[]{
                1,
                Integer.parseInt(partes[0].trim()),
                Integer.parseInt(partes[1].trim()),
                Integer.parseInt(partes[2].trim())
            };
        } catch (Exception e) {
            return new int[]{1, 4, 4, 2}; // default 4-4-2
        }
    }

    // ── Cálculo de poder táctico ──────────────────────────────────────────

    public double getPoderofensivo() {
        double total = 0;
        for (Jugador j : titulares) {
            if ("DEL".equals(j.getPosicion())) total += j.getFactorRendimiento() * j.getAtaque() * 1.2;
            else if ("MED".equals(j.getPosicion())) total += j.getFactorRendimiento() * j.getAtaque() * 0.7;
        }
        if (entrenador != null) {
            total *= entrenador.getBonificacionTactica() * entrenador.getMultiplicadorOfensivo();
        }
        return total;
    }

    public double getPoderDefensivo() {
        double total = 0;
        for (Jugador j : titulares) {
            if ("POR".equals(j.getPosicion())) total += j.getFactorRendimiento() * j.getDefensa() * 1.3;
            else if ("DEF".equals(j.getPosicion())) total += j.getFactorRendimiento() * j.getDefensa() * 1.0;
            else if ("MED".equals(j.getPosicion())) total += j.getFactorRendimiento() * j.getDefensa() * 0.4;
        }
        if (entrenador != null) {
            total *= entrenador.getBonificacionTactica() * entrenador.getMultiplicadorDefensivo();
        }
        return total;
    }

    // ── Suplentes (plantilla − titulares) ────────────────────────────────
    public ArrayList<Jugador> getSuplentes() {
        ArrayList<Jugador> sups = new ArrayList<>();
        for (Jugador j : plantilla) {
            if (!titulares.contains(j)) sups.add(j);
        }
        return sups;
    }

    // ── Getters / Setters ──────────────────────────────────────────────────
    public String             getNombre()      { return nombre; }
    public String             getPais()        { return pais; }
    public double             getPresupuesto() { return presupuesto; }
    public void               setPresupuesto(double p) { this.presupuesto = p; }
    public ArrayList<Jugador> getPlantilla()   { return plantilla; }
    public ArrayList<Jugador> getTitulares()   { return titulares; }
    public Entrenador         getEntrenador()  { return entrenador; }
    public void               setEntrenador(Entrenador e) { this.entrenador = e; }
    public String             getFormacion()   { return formacion; }
    public void               setFormacion(String f)      { this.formacion = f; }
    public int                getGolesAFavor() { return golesAFavor; }
    public int                getGolesEnContra(){ return golesEnContra; }
    public void addGolesAFavor(int g)   { golesAFavor   += g; }
    public void addGolesEnContra(int g) { golesEnContra += g; }

    @Override
    public String toString() { return nombre + " (" + pais + ")"; }
}
