package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Clase Equipo: Representación integral de un Club de Fútbol.
 *
 * Centraliza la gestión de una entidad deportiva, incluyendo:
 * - Operaciones financieras (Presupuesto y Balance).
 * - Administración de la Plantilla (Altas, Bajas y Recuperación).
 * - Inteligencia Táctica (Selección automática de titulares y formaciones).
 * - Cálculo de Potencial (Algoritmos de fuerza ofensiva y defensiva).
 *
 * Implementa {@link Serializable} para persistencia con ObjectOutputStream.
 */
public class Equipo implements Serializable {

    /** Versión de serialización para compatibilidad de ficheros. */
    private static final long serialVersionUID = 4L;

    // --- BLOQUE: IDENTIDAD Y LIQUIDEZ ---
    /** Nombre oficial del equipo. */
    private String             nombre;
    /** País/Liga de origen. */
    private String             pais;
    /** Fondos disponibles para fichajes en millones de euros (M€). */
    private double             presupuesto;

    // --- BLOQUE: GESTIÓN DE TALENTO ---
    /** Lista completa de jugadores bajo contrato. */
    private ArrayList<Jugador> plantilla;
    /** Los 11 jugadores seleccionados como titulares. */
    private ArrayList<Jugador> titulares;

    // --- BLOQUE: DIRECCIÓN TÉCNICA ---
    /** Entrenador asignado al equipo. */
    private Entrenador         entrenador;
    /** Esquema táctico actual (e.g., "4-3-3", "4-2-3-1"). */
    private String             formacion;
    /** Estilo de juego dinámico (e.g., "Equilibrada", "Ofensiva Total"). */
    private String             tactica = "Equilibrada";

    // --- BLOQUE: REGISTRO HISTÓRICO ---
    /** Goles marcados acumulados en el torneo. */
    private int     golesAFavor;
    /** Goles encajados acumulados en el torneo. */
    private int     golesEnContra;
    /** true si el equipo es controlado por el jugador humano. */
    private boolean usuario = false;

    /**
     * Constructor: Inicializa la infraestructura del club.
     *
     * @param nombre      Nombre oficial del equipo.
     * @param pais        País/Liga de origen.
     * @param presupuesto Fondos iniciales en M€.
     */
    public Equipo(String nombre, String pais, double presupuesto) {
        this.nombre        = nombre;
        this.pais          = pais;
        this.presupuesto   = presupuesto;
        this.plantilla     = new ArrayList<>();
        this.titulares     = new ArrayList<>();
        this.formacion     = "4-4-2";
    }

    // ---------------------------------------------------------------------
    // BLOQUE: GESTIÓN CONTRACTUAL Y FISIOLOGÍA
    // ---------------------------------------------------------------------

    /**
     * Incorpora a un jugador a la disciplina del equipo.
     * @param j Jugador a añadir a la plantilla.
     */
    public void agregarJugador(Jugador j) {
        plantilla.add(j);
        j.setEquipo(this);
    }

    /**
     * Rescinde el contrato de un jugador, liberándolo del club y del once.
     * Usa un {@link Iterator} para eliminación segura sin ConcurrentModificationException.
     *
     * @param jugador Jugador a remover.
     * @return true si se encontró y eliminó correctamente.
     */
    public boolean removerJugador(Jugador jugador) {
        Iterator<Jugador> it = plantilla.iterator();
        while (it.hasNext()) {
            Jugador j = it.next();
            if (j.getId() == jugador.getId()) {
                it.remove();
                titulares.remove(j);
                j.setEquipo(null);
                return true;
            }
        }
        return false;
    }

    /**
     * Protocolo de recuperación post-partido: restaura la energía de toda la plantilla.
     */
    public void recuperarEnergiaPlantilla() {
        for (Jugador j : plantilla) j.recuperarEnergia();
    }

    /**
     * Calcula la calidad media (OVR) de los titulares en el campo.
     * @return Media aritmética de getMediaGeneral() de los titulares.
     */
    public int getMediaMedia() {
        if (titulares.isEmpty()) return 0;
        int sum = 0;
        for (Jugador j : titulares) sum += j.getMediaGeneral();
        return sum / titulares.size();
    }

    /**
     * Identifica a los mejores lanzadores para la tanda de penaltis,
     * ordenados por su atributo de ataque descendente.
     *
     * @param cantidad Número de lanzadores a seleccionar.
     * @return Lista con los mejores tiradores.
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
     * Algoritmo de Optimización Táctica V2:
     * Selecciona a los 11 mejores jugadores y los posiciona
     * para maximizar el rendimiento colectivo del equipo.
     */
    public void establecerMejorOnce() {
        // Resetear estado de todos los jugadores
        for (Jugador j : plantilla) {
            j.setTitular(false);
            j.setPosicionNodo(null);
        }
        titulares.clear();

        int[] cuotas = parseFormacion(formacion);
        ArrayList<String> nodosHabilitados = generarNodosDeFormacion(cuotas);
        ArrayList<Jugador> asignados = new ArrayList<>();

        // Asignar por categorías para mantener coherencia táctica
        for (String nodo : nodosHabilitados) {
            String catRequerida = Jugador.categoriaGeneral(nodo);
            Jugador mejorParaNodo = null;
            int mejorPuntaje = -1;

            for (Jugador j : plantilla) {
                if (asignados.contains(j)) continue;

                int puntaje = j.getMediaGeneral();
                String catJugador = Jugador.categoriaGeneral(j.getPosicion());

                // Prioridad absoluta a la categoría correcta
                if (catJugador.equals(catRequerida)) {
                    puntaje += 100;
                }

                // Bonus extra por posición exacta
                if (j.getPosicion().equals(nodo)) {
                    puntaje += 50;
                }

                if (puntaje > mejorPuntaje) {
                    mejorPuntaje = puntaje;
                    mejorParaNodo = j;
                }
            }

            if (mejorParaNodo != null) {
                registrarEnOnce(mejorParaNodo, nodo, asignados);
            }
        }

        // Relleno de emergencia si la plantilla es inferior a 11
        if (asignados.size() < 11 && asignados.size() < plantilla.size()) {
            for (String nodo : nodosHabilitados) {
                boolean ocupado = false;
                for (Jugador t : titulares) {
                    if (nodo.equals(t.getPosicionNodo())) { ocupado = true; break; }
                }
                if (ocupado) continue;

                for (Jugador j : plantilla) {
                    if (!asignados.contains(j)) {
                        registrarEnOnce(j, nodo, asignados);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Registra a un jugador como titular en una posición del campo.
     */
    private void registrarEnOnce(Jugador j, String nodo, List<Jugador> asignados) {
        j.setTitular(true);
        j.setPosicionNodo(nodo);
        titulares.add(j);
        asignados.add(j);
    }

    /**
     * Genera el mapa de slots tácticos según la formación elegida.
     * @param cuotas Array con [porteros, defensas, mediocampistas, delanteros].
     * @return Lista de etiquetas de posiciones a rellenar.
     */
    private ArrayList<String> generarNodosDeFormacion(int[] cuotas) {
        ArrayList<String> nodos = new ArrayList<>();
        nodos.add("POR");
        nodos.addAll(Arrays.asList(obtenerLabelsLinea(cuotas[1], new String[]{"LD", "DFC", "DFC", "LI"})));
        nodos.addAll(Arrays.asList(obtenerLabelsLinea(cuotas[2], new String[]{"MD", "MC", "MC", "MI"})));
        nodos.addAll(Arrays.asList(obtenerLabelsLinea(cuotas[3], new String[]{"ED", "DC", "DC", "EI"})));
        return nodos;
    }

    /**
     * Asigna etiquetas posicionales a una línea según el número de jugadores.
     */
    private String[] obtenerLabelsLinea(int num, String[] base) {
        if (num <= 0) return new String[0];
        String[] res = new String[num];
        if (num == 1) res[0] = base[1];
        else if (num == 2) { res[0] = base[1]; res[1] = base[2]; }
        else if (num == 3) { res[0] = base[0]; res[1] = base[1]; res[2] = base[3]; }
        else if (num == 4) res = base;
        else {
            res[0] = base[0]; res[num-1] = base[3];
            for (int i = 1; i < num - 1; i++) res[i] = base[1];
        }
        return res;
    }

    /**
     * Parsea una cadena de formación en un array de cuotas por línea.
     * Soporta formaciones de 3 segmentos ("4-3-3") y 4 segmentos ("4-2-3-1").
     * Para formaciones de 4 segmentos, se combinan los dos segmentos
     * centrales como mediocampo (e.g., "4-2-3-1" → DEF=4, MED=5, DEL=1).
     *
     * @param f Cadena de formación (e.g., "4-3-3" o "4-2-3-1").
     * @return Array {porteros, defensas, mediocampistas, delanteros}.
     */
    private int[] parseFormacion(String f) {
        try {
            String[] split = f.split("-");
            if (split.length == 4) {
                // Formación de 4 segmentos: combinar medios
                int def = Integer.parseInt(split[0]);
                int med = Integer.parseInt(split[1]) + Integer.parseInt(split[2]);
                int del = Integer.parseInt(split[3]);
                return new int[]{1, def, med, del};
            }
            // Formación estándar de 3 segmentos
            return new int[]{1,
                Integer.parseInt(split[0]),
                Integer.parseInt(split[1]),
                Integer.parseInt(split[2])};
        } catch (Exception e) {
            return new int[]{1, 4, 4, 2};
        }
    }

    // ---------------------------------------------------------------------
    // BLOQUE: MOTOR DE POTENCIA (SIMULACIÓN)
    // ---------------------------------------------------------------------

    /**
     * Calcula la fuerza de ataque real del club aplicando modificadores
     * de táctica, entrenador y fatiga de los jugadores.
     *
     * @return Poder ofensivo acumulado del equipo.
     */
    public double getPoderOfensivo() {
        double totalAtk = 0;
        for (Jugador j : titulares) {
            if (Jugador.categoriaGeneral(j.getPosicion()).equals("DEL"))
                totalAtk += j.getFactorRendimiento() * j.getAtaque() * 1.5;
            else
                totalAtk += j.getFactorRendimiento() * j.getAtaque() * 0.8;
        }

        // Modificadores por Filosofía Táctica
        switch (tactica) {
            case "Ofensiva Total": totalAtk *= 1.40; break;
            case "Contraataque":  totalAtk *= 1.25; break;
            case "Catenaccio":    totalAtk *= 0.50; break;
        }

        // Bonificación por Mando Técnico
        if (entrenador != null) {
            totalAtk *= entrenador.getBonificacionTactica() * entrenador.getMultiplicadorOfensivo();
        }
        return totalAtk;
    }

    /**
     * Calcula la solidez del muro defensivo del club.
     * @return Poder defensivo acumulado del equipo.
     */
    public double getPoderDefensivo() {
        double totalDef = 0;
        for (Jugador j : titulares) {
            String cat = Jugador.categoriaGeneral(j.getPosicion());
            if (cat.equals("POR"))      totalDef += j.getFactorRendimiento() * j.getDefensa() * 1.8;
            else if (cat.equals("DEF")) totalDef += j.getFactorRendimiento() * j.getDefensa() * 1.2;
            else                        totalDef += j.getFactorRendimiento() * j.getDefensa() * 0.4;
        }

        if ("Catenaccio".equals(tactica)) totalDef *= 1.60;
        if (entrenador != null) {
            totalDef *= entrenador.getBonificacionTactica() * entrenador.getMultiplicadorDefensivo();
        }
        return totalDef;
    }

    // --- BLOQUE: ACCESORES ---
    public String  getNombre()      { return nombre; }
    public String  getPais()        { return pais; }
    public double  getPresupuesto() { return presupuesto; }
    public void    setPresupuesto(double p) { this.presupuesto = p; }
    public ArrayList<Jugador> getPlantilla() { return plantilla; }
    public ArrayList<Jugador> getTitulares() { return titulares; }

    /**
     * Obtiene la lista de suplentes (jugadores no titulares).
     * @return Lista filtrada de jugadores que no están en el once.
     */
    public List<Jugador> getSuplentes() {
        return plantilla.stream()
                .filter(j -> !titulares.contains(j))
                .collect(Collectors.toList());
    }

    public String  getTactica()   { return tactica; }
    public void    setTactica(String t)   { this.tactica = t; }
    public Entrenador getEntrenador() { return entrenador; }
    public void    setEntrenador(Entrenador e) { this.entrenador = e; }
    public String  getFormacion() { return formacion; }
    public void    setFormacion(String f) { this.formacion = f; }
    public int getGolesAFavor() { return golesAFavor; }
    public int getGolesEnContra() { return golesEnContra; }
    public void addGolesAFavor(int g)   { golesAFavor += g; }
    public void addGolesEnContra(int g) { golesEnContra += g; }
    public boolean isUsuario() { return usuario; }
    public void    setUsuario(boolean u) { this.usuario = u; }

    /**
     * Ejecuta una sustitución táctica entre un titular y un suplente.
     * El suplente hereda la posición del titular sustituido.
     *
     * @param sale  Jugador titular que abandona el campo.
     * @param entra Jugador suplente que ingresa al campo.
     * @return true si el cambio se realizó correctamente.
     */
    public boolean realizarCambio(Jugador sale, Jugador entra) {
        if (sale == null || entra == null) return false;
        if (titulares.contains(sale) && !titulares.contains(entra)) {
            String posicionHeredada = sale.getPosicionNodo();
            titulares.remove(sale);
            sale.setTitular(false);
            sale.setPosicionNodo(null);
            entra.setTitular(true);
            entra.setPosicionNodo(posicionHeredada);
            titulares.add(entra);
            return true;
        }
        return false;
    }

    @Override
    public String toString() { return nombre + " (" + pais + ")"; }
}
