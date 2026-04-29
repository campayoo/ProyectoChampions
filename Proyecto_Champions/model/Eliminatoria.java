package model;

import java.io.Serializable;

/**
 * Clase Eliminatoria: Coordinadora de llaves en el cuadro competitivo.
 *
 * Gestiona el enfrentamiento táctico entre dos clubes, controlando el flujo
 * de partidos (Ida/Vuelta), calculando el marcador global y certificando
 * quién es el ganador legítimo para avanzar de ronda.
 *
 * Implementa {@link Serializable} para persistencia con ObjectOutputStream.
 */
public class Eliminatoria implements Serializable {

    /** Versión de serialización para compatibilidad de ficheros. */
    private static final long serialVersionUID = 6L;

    // --- BLOQUE: CONFIGURACIÓN DE LA LLAVE ---
    /** Club con localía en el partido de Ida. */
    private final Equipo equipoA;
    /** Club con localía en el partido de Vuelta. */
    private final Equipo equipoB;
    /** true = formato Ida/Vuelta; false = partido único (Gran Final). */
    private final boolean doblePartido;

    // --- BLOQUE: REGISTRO DE ENCUENTROS ---
    /** Primer partido de la eliminatoria. */
    private Partido ida;
    /** Segundo partido (solo en formato doble). */
    private Partido vuelta;
    /** Club clasificado para la siguiente ronda. */
    private Equipo  ganador;

    /**
     * Constructor: Inicializa el marco de la eliminatoria.
     *
     * @param equipoA      Equipo con localía en la Ida.
     * @param equipoB      Equipo con localía en la Vuelta.
     * @param doblePartido true para Ida/Vuelta, false para partido único.
     */
    public Eliminatoria(Equipo equipoA, Equipo equipoB, boolean doblePartido) {
        this.equipoA      = equipoA;
        this.equipoB      = equipoB;
        this.doblePartido = doblePartido;
    }

    // ---------------------------------------------------------------------
    // BLOQUE: SIMULACIÓN EN SEGUNDO PLANO (MODO CPU)
    // ---------------------------------------------------------------------

    /** Ejecuta el partido de ida de forma automatizada (IA vs IA). */
    public void jugarIdaAuto() {
        if (ida == null) {
            System.out.println("[DEBUG - Eliminatoria] Simulando IDA automática: " + equipoA.getNombre() + " vs " + equipoB.getNombre());
            ida = new Partido(equipoA, equipoB);
            ida.simular();
            System.out.println("   -> Resultado IDA: " + ida.getGolesLocal() + " - " + ida.getGolesVisitante());
        }
    }

    /**
     * Ejecuta el partido de vuelta y actualiza el vencedor por marcador global.
     * En partidos únicos, simplemente determina el ganador sin crear vuelta.
     */
    public void jugarVueltaAuto() {
        if (!doblePartido) {
            determinarGanador();
            return;
        }
        if (vuelta == null) {
            System.out.println("[DEBUG - Eliminatoria] Simulando VUELTA automática: " + equipoB.getNombre() + " vs " + equipoA.getNombre());
            vuelta = new Partido(equipoB, equipoA);
            vuelta.simular();
            System.out.println("   -> Resultado VUELTA: " + vuelta.getGolesLocal() + " - " + vuelta.getGolesVisitante());
        }
        determinarGanador();
    }

    // ---------------------------------------------------------------------
    // BLOQUE: CÓMPUTO DE RESULTADOS Y DESEMPATES
    // ---------------------------------------------------------------------

    /**
     * Calcula el equipo clasificado sumando los goles de ambos encuentros.
     * En caso de empate global, el ganador queda como null (requiere penaltis).
     */
    public void determinarGanador() {
        // Caso de Partido Único (Gran Final)
        if (!doblePartido) {
            if (ida != null && ida.isTerminado()) {
                ganador = ida.getGanador();
                System.out.println("[DEBUG - Eliminatoria] Final resuelta. Ganador: " + (ganador != null ? ganador.getNombre() : "Empate"));
            }
            return;
        }

        // Caso de Eliminatoria Doble
        if (ida == null || !ida.isTerminado() || vuelta == null || !vuelta.isTerminado()) return;

        // Cómputo global de goles
        int golesA = ida.getGolesLocal()     + vuelta.getGolesVisitante();
        int golesB = ida.getGolesVisitante() + vuelta.getGolesLocal();

        System.out.println("[DEBUG - Eliminatoria] Evaluando global: " + equipoA.getNombre() + " [" + golesA + "] vs [" + golesB + "] " + equipoB.getNombre());

        if (golesA > golesB) {
            ganador = equipoA;
            System.out.println("   -> CLASIFICADO: " + equipoA.getNombre());
        } else if (golesB > golesA) {
            ganador = equipoB;
            System.out.println("   -> CLASIFICADO: " + equipoB.getNombre());
        } else {
            ganador = null; // Empate global → penaltis
            System.out.println("   -> EMPATE GLOBAL. Requiere penaltis.");
        }
    }

    /**
     * Resolución de empate para simulaciones rápidas de la IA.
     * Ahora utiliza el sistema real de penaltis en lugar de lanzar la moneda.
     */
    public void resolverEmpateIA() {
        if (ganador != null) return;
        
        System.out.println("[DEBUG - Eliminatoria] Ejecutando tanda de penaltis automática...");
        Partido p = doblePartido ? vuelta : ida;
        if (p == null) return;
        
        // Simular tanda de penaltis completa
        while (p.isEnTanda()) {
            p.simularSiguienteRondaPenal();
        }
        ganador = p.getGanador();
        System.out.println("[DEBUG - Eliminatoria] Penaltis finalizados. Ganador: " + ganador.getNombre() + " (" + p.getPenaltisLocal() + "-" + p.getPenaltisVisitante() + ")");
    }

    /**
     * Determina si el empate en el global exige una tanda de penaltis.
     * @return true si hay empate y se necesita desempate.
     */
    public boolean requierePenaltis() {
        if (!doblePartido) {
            return ida != null && ida.isTerminado()
                && ida.getGolesLocal() == ida.getGolesVisitante();
        }
        if (ida == null || !ida.isTerminado() || vuelta == null || !vuelta.isTerminado()) return false;
        int golesA = ida.getGolesLocal()     + vuelta.getGolesVisitante();
        int golesB = ida.getGolesVisitante() + vuelta.getGolesLocal();
        return golesA == golesB;
    }

    // ---------------------------------------------------------------------
    // BLOQUE: CONTROL DE PARTIDOS JUGABLES (USER MODE)
    // ---------------------------------------------------------------------

    /** Crea y devuelve el partido de ida para modo interactivo. */
    public Partido crearPartidoIda() {
        if (ida == null) ida = new Partido(equipoA, equipoB);
        return ida;
    }

    /** Crea y devuelve el partido de vuelta para modo interactivo. */
    public Partido crearPartidoVuelta() {
        if (vuelta == null) vuelta = new Partido(equipoB, equipoA);
        return vuelta;
    }

    /**
     * Genera un informe textual del estado de la llave.
     * @return Resumen con marcadores de Ida, Vuelta y Global, además de penaltis.
     */
    public String getResumen() {
        StringBuilder sb = new StringBuilder();
        sb.append(equipoA.getNombre()).append(" vs ").append(equipoB.getNombre());

        if (ida != null && ida.isTerminado()) {
            sb.append("\n [IDA] ").append(ida.getGolesLocal()).append(" - ").append(ida.getGolesVisitante());
            
            // Si es final a partido único y hubo penaltis
            if (!doblePartido && (ida.getPenaltisLocal() > 0 || ida.getPenaltisVisitante() > 0)) {
                sb.append(String.format(" Penaltis: (%d-%d)", ida.getPenaltisLocal(), ida.getPenaltisVisitante()));
            }
        }
        if (vuelta != null && vuelta.isTerminado()) {
            sb.append("\n [VTA] ").append(vuelta.getGolesLocal()).append(" - ").append(vuelta.getGolesVisitante());
            int gA = ida.getGolesLocal() + vuelta.getGolesVisitante();
            int gB = ida.getGolesVisitante() + vuelta.getGolesLocal();
            sb.append(" (Global: ").append(gA).append("-").append(gB).append(")");
            
            // Penaltis en el global (se chutan en la vuelta)
            if (vuelta.getPenaltisLocal() > 0 || vuelta.getPenaltisVisitante() > 0) {
                 // vuelta.local es equipoB, vuelta.visitante es equipoA
                 // Así que el score de penaltis de A es penaltisVisitante, y de B es penaltisLocal
                 sb.append(String.format(" Penaltis: (%d-%d)", vuelta.getPenaltisVisitante(), vuelta.getPenaltisLocal()));
            }
        }
        
        if (ganador != null) {
            sb.append("\n ✓ CLASIFICADO: ").append(ganador.getNombre()).append("\n");
        }
        
        return sb.toString();
    }

    // --- ACCESORES DE ESTADO ---
    public Equipo  getEquipoA()      { return equipoA; }
    public Equipo  getEquipoB()      { return equipoB; }
    public Partido getIda()          { return ida; }
    public Partido getVuelta()       { return vuelta; }
    public Equipo  getGanador()      { return ganador; }
    public void    setGanador(Equipo g) { this.ganador = g; }
    public boolean isDoblePartido()  { return doblePartido; }

    /** @return true si la eliminatoria ya tiene un resultado definitivo. */
    public boolean isCompleta() {
        if (ganador != null) return true;
        if (!doblePartido) return ida != null && ida.isTerminado();
        return vuelta != null && vuelta.isTerminado();
    }

    /**
     * Verifica si un equipo participa en esta llave.
     * @param e Equipo a buscar.
     * @return true si el equipo es A o B de esta eliminatoria.
     */
    public boolean esMiPartido(Equipo e) {
        if (e == null) return false;
        return equipoA.getNombre().equals(e.getNombre())
            || equipoB.getNombre().equals(e.getNombre());
    }
}
