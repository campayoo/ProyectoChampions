package model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Clase Equipo: Representación integral de un Club de Fútbol.
 * 
 * Centraliza la gestión de una entidad deportiva, incluyendo:
 * - Operaciones financieras (Presupuesto y Balance).
 * - Administración de la Plantilla (Altas, Bajas y Recuperación).
 * - Inteligencia Táctica (Selección automática de titulares y formaciones).
 * - Cálculo de Potencial (Algoritmos de fuerza ofensiva y defensiva).
 */
public class Equipo {

    // --- BLOQUE: IDENTIDAD Y LIQUIDEZ ---
    private String             nombre;          // Marca oficial del equipo
    private String             pais;            // Liga nacional de origen
    private double             presupuesto;     // Fondos para fichajes en M€
    
    // --- BLOQUE: GESTIÓN DE TALENTO (ATLETAS) ---
    private ArrayList<Jugador> plantilla;       // Todos los jugadores bajo contrato
    private ArrayList<Jugador> titulares;       // Los 11 protagosnistas en el campo
    
    // --- BLOQUE: DIRECCIÓN TÉCNICA ---
    private Entrenador         entrenador;      // Liderazgo del staff
    private String             formacion;       // Esquema visual (e.g., 4-3-3)
    private String             tactica = "Equilibrada"; // Estilo de juego dinámico
    
    // --- BLOQUE: REGISTRO HISTÓRICO ---
    private int     golesAFavor;                // Eficacia goleadora acumulada
    private int     golesEnContra;              // Solidez defensiva acumulada
    private boolean usuario = false;            // Controlado por mánager humano

    /**
     * Constructor: Inicializa la infraestructura administrativa y deportiva.
     */
    public Equipo(String nombre, String pais, double presupuesto) {
        this.nombre        = nombre;
        this.pais          = pais;
        this.presupuesto   = presupuesto;
        this.plantilla     = new ArrayList<>();
        this.titulares     = new ArrayList<>();
        this.formacion     = "4-4-2"; // Dibujo táctico base
    }

    // ---------------------------------------------------------------------
    // BLOQUE: GESTIÓN CONTRACTUAL Y FISIOLOGÍA
    // ---------------------------------------------------------------------

    /**
     * Incorpora a un deportista a la disciplina del equipo.
     */
    public void agregarJugador(Jugador j) {
        plantilla.add(j);
        j.setEquipo(this);
    }

    /**
     * Resciende el contrato de un jugador, liberándolo del club y del once.
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
     * Protocolo de recuperación post-partido para restaurar la energía.
     */
    public void recuperarEnergiaPlantilla() {
        for (Jugador j : plantilla) j.recuperarEnergia();
    }

    /**
     * Calcula la calidad media (OVR) de los titulares en el campo.
     */
    public int getMediaMedia() {
        if (titulares.isEmpty()) return 0;
        int sum = 0;
        for (Jugador j : titulares) sum += j.getMediaGeneral();
        return sum / titulares.size();
    }

    /**
     * Identifica a los especialistas en ataque para la tanda de penaltis.
     */
    public ArrayList<Jugador> getMejoresLanzadores(int cantidad) {
        ArrayList<Jugador> todos = new ArrayList<>(plantilla);
        todos.sort((a, b) -> Integer.compare(b.getAtaque(), a.getAtaque()));
        ArrayList<Jugador> mejores = new ArrayList<>();
        for (int i = 0; i < Math.min(cantidad, todos.size()); i++) mejores.add(todos.get(i));
        return mejores;
    }

    /**
     * Algoritmo de Optimización Táctica V2:
     * Selecciona a los 11 mejores jugadores del club y los posiciona para maximizar
     * el rendimiento colectivo del equipo.
     */
    public void establecerMejorOnce() {
        // Bloque: Limpieza de la pizarra anterior
        for (Jugador j : plantilla) {
            j.setTitular(false);
            j.setPosicionNodo(null);
        }
        titulares.clear();

        // Bloque: Identificación de slots necesarios
        int[] cuotas = parseFormacion(formacion);
        ArrayList<String> nodosHabilitados = generarNodosDeFormacion(cuotas);
        
        // Bloque: Selección de candidatos (Los 11 con mayor potencial bruto)
        List<Jugador> candidatos = new ArrayList<>(plantilla).stream()
                .sorted(Comparator.comparingInt(Jugador::getMediaGeneral).reversed())
                .limit(11) // Nos quedamos solo con los "Titulares de Élite"
                .collect(Collectors.toList());

        ArrayList<Jugador> asignados = new ArrayList<>();

        // Bloque: Asignación por Desempeño Máximo
        // Iteramos sobre los nodos para encontrar para cada uno al mejor aspirante entre los cracks
        for (String nodo : nodosHabilitados) {
            Jugador mejorCandidato = null;
            int maxOvr = -1;

            for (Jugador j : candidatos) {
                if (asignados.contains(j)) continue;
                
                int ovrEnPos = j.getCompatibilidadConNodo(nodo) == Jugador.Compatibilidad.NATURAL 
                               ? j.getMediaGeneral() 
                               : j.getMediaGeneral() - 5; // Ligera penalización para preferir naturales

                if (ovrEnPos > maxOvr) {
                    maxOvr = ovrEnPos;
                    mejorCandidato = j;
                }
            }

            if (mejorCandidato != null) {
                registrarEnOnce(mejorCandidato, nodo, asignados);
            }
        }
        
        // Bloque: Relleno de emergencia si la plantilla es inferior a 11
        if (asignados.size() < 11 && asignados.size() < plantilla.size()) {
            for (String nodo : nodosHabilitados) {
                boolean ocupado = false;
                for (Jugador t : titulares) if (nodo.equals(t.getPosicionNodo())) { ocupado = true; break; }
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

    private void registrarEnOnce(Jugador j, String nodo, List<Jugador> asignados) {
        j.setTitular(true);
        j.setPosicionNodo(nodo);
        titulares.add(j);
        asignados.add(j);
    }

    /**
     * Genera el mapa de slots tácticos según la formación elegida.
     */
    private ArrayList<String> generarNodosDeFormacion(int[] cuotas) {
        ArrayList<String> nodos = new ArrayList<>();
        nodos.add("POR"); // Slot guardameta

        // Slots Defensivos
        int nDef = cuotas[1];
        if (nDef >= 4) {
            nodos.add("LD"); nodos.add("LI");
            for (int i = 0; i < nDef - 2; i++) nodos.add("DFC");
        } else {
            for (int i = 0; i < nDef; i++) nodos.add("DFC");
        }

        // Slots Medulares
        int nMed = cuotas[2];
        if (nMed >= 3) {
            nodos.add("MD"); nodos.add("MI");
            for (int i = 0; i < nMed - 2; i++) nodos.add("MC");
        } else {
            for (int i = 0; i < nMed; i++) nodos.add("MC");
        }

        // Slots Ofensivos
        int nDel = cuotas[3];
        if (nDel >= 2) {
            nodos.add("ED"); nodos.add("EI");
            for (int i = 0; i < nDel - 2; i++) nodos.add("DC");
        } else {
            for (int i = 0; i < nDel; i++) nodos.add("DC");
        }
        return nodos;
    }

    private int[] parseFormacion(String f) {
        try {
            String[] split = f.split("-");
            return new int[]{ 1, Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]) };
        } catch (Exception e) { return new int[]{1, 4, 4, 2}; }
    }

    // ---------------------------------------------------------------------
    // BLOQUE: MOTOR DE POTENCIA (SIMULACIÓN)
    // ---------------------------------------------------------------------

    /**
     * Calcula la fuerza de ataque real del club en un momento dado.
     */
    public double getPoderOfensivo() {
        double totalAtk = 0;
        for (Jugador j : titulares) {
            if (Jugador.categoriaGeneral(j.getPosicion()).equals("DEL")) 
                totalAtk += j.getFactorRendimiento() * j.getAtaque() * 1.5; // Factor goleador
            else 
                totalAtk += j.getFactorRendimiento() * j.getAtaque() * 0.8;
        }
        
        // Modificadores por Filosofía Táctica
        switch (tactica) {
            case "Ofensiva Total" -> totalAtk *= 1.40;
            case "Contraataque"   -> totalAtk *= 1.25;
            case "Catenaccio"     -> totalAtk *= 0.50;
        }
 
        // Bonificación por Mando Técnico
        if (entrenador != null) totalAtk *= entrenador.getBonificacionTactica() * entrenador.getMultiplicadorOfensivo();
        return totalAtk;
    }

    /**
     * Calcula la solidez del muro defensivo del club.
     */
    public double getPoderDefensivo() {
        double totalDef = 0;
        for (Jugador j : titulares) {
            String cat = Jugador.categoriaGeneral(j.getPosicion());
            if (cat.equals("POR")) totalDef += j.getFactorRendimiento() * j.getDefensa() * 1.8;
            else if (cat.equals("DEF")) totalDef += j.getFactorRendimiento() * j.getDefensa() * 1.2;
            else totalDef += j.getFactorRendimiento() * j.getDefensa() * 0.4;
        }

        if ("Catenaccio".equals(tactica)) totalDef *= 1.60;

        if (entrenador != null) totalDef *= entrenador.getBonificacionTactica() * entrenador.getMultiplicadorDefensivo();
        return totalDef;
    }

    // --- ACCESORES TÉCNICOS ---
    public String  getNombre()      { return nombre; }
    public String  getPais()        { return pais; }
    public double  getPresupuesto() { return presupuesto; }
    public void    setPresupuesto(double p) { this.presupuesto = p; }
    public ArrayList<Jugador> getPlantilla() { return plantilla; }
    public ArrayList<Jugador> getTitulares() { return titulares; }
    
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
     */
    public boolean realizarCambio(Jugador sale, Jugador entra) {
        if (sale == null || entra == null) return false;
        if (titulares.contains(sale) && !titulares.contains(entra)) {
            titulares.remove(sale);
            sale.setTitular(false);
            sale.setPosicionNodo(null);
            
            entra.setTitular(true);
            entra.setPosicionNodo(sale.getPosicionNodo()); // Hereda la posición si es posible
            titulares.add(entra);
            return true;
        }
        return false;
    }

    @Override
    public String toString() { return nombre + " (" + pais + ")"; }
}
