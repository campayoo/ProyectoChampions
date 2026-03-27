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

    // Estadísticas y Táctica
    private int     golesAFavor;
    private int     golesEnContra;
    private String  tactica = "Equilibrada"; // "Tiki Taka", "Autobús", "Contraataque", "Por las bandas"

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
     * Recupera la energía de todos los jugadores de la plantilla al 100%.
     */
    public void recuperarEnergiaPlantilla() {
        for (Jugador j : plantilla) {
            j.recuperarEnergia();
        }
    }

    public int getMediaMedia() {
        if (titulares.isEmpty()) return 0;
        int sum = 0;
        for (Jugador j : titulares) sum += j.getMediaGeneral();
        return sum / titulares.size();
    }

    /**
     * Devuelve los 5 mejores lanzadores de penaltis (basado en el atributo ataque).
     */
    public ArrayList<Jugador> getMejoresLanzadores(int cantidad) {
        ArrayList<Jugador> todos = new ArrayList<>(plantilla);
        todos.sort((a, b) -> Integer.compare(b.getAtaque(), a.getAtaque()));
        ArrayList<Jugador> mejores = new ArrayList<>();
        for (int i = 0; i < Math.min(cantidad, todos.size()); i++) {
            mejores.add(todos.get(i));
        }
        return mejores;
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
        String[] categorias = {"POR", "DEF", "MED", "DEL"};

        for (int p = 0; p < categorias.length; p++) {
            int cuota   = cuotas[p];
            int puestos = 0;
            String cat  = categorias[p];

            while (puestos < cuota) {
                Jugador mejorNoUsado = null;
                Iterator<Jugador> it = plantilla.iterator();
                while (it.hasNext()) {
                    Jugador j = it.next();
                    if (perteneceACategoria(j.getPosicion(), cat) && !titulares.contains(j)) {
                        if (mejorNoUsado == null || j.getMediaGeneral() > mejorNoUsado.getMediaGeneral()) {
                            mejorNoUsado = j;
                        }
                    }
                }
                if (mejorNoUsado != null) {
                    mejorNoUsado.setTitular(true);
                    titulares.add(mejorNoUsado);
                    puestos++;
                } else break;
            }
        }
    }

    private boolean perteneceACategoria(String pos, String cat) {
        return switch (cat) {
            case "POR" -> pos.equals("POR");
            case "DEF" -> pos.matches("DEF|LD|LI|DFC|CAD|CAI");
            case "MED" -> pos.matches("MED|MC|MCD|MCO|MI|MD");
            case "DEL" -> pos.matches("DEL|DC|ED|EI|SD");
            default    -> false;
        };
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
        
        // Bonus Táctico
        switch (tactica) {
            case "Tiki Taka"   -> total *= 1.3;
            case "Contraataque" -> total *= 1.2;
            case "Autobús"     -> total *= 0.5;
            case "Por las bandas" -> total *= 1.1;
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

        // Bonus Táctico
        switch (tactica) {
            case "Autobús"     -> total *= 1.5;
            case "Tiki Taka"   -> total *= 0.8;
            case "Contraataque" -> total *= 1.1;
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
    public String             getTactica()     { return tactica; }
    public void               setTactica(String t)        { this.tactica = t; }
    public int                getGolesAFavor() { return golesAFavor; }
    public int                getGolesEnContra(){ return golesEnContra; }
    public void addGolesAFavor(int g)   { golesAFavor   += g; }
    public void addGolesEnContra(int g) { golesEnContra += g; }

    @Override
    public String toString() { return nombre + " (" + pais + ")"; }
}
