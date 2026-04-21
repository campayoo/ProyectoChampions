package model;

/**
 * Clase Eliminatoria: Coordinadora de llaves en el cuadro competitivo.
 * 
 * Gestiona el enfrentamiento táctico entre dos clubes, controlando el flujo
 * de partidos (Ida/Vuelta), calculando el marcador global y certificando
 * quién es el ganador legítimo para avanzar de ronda.
 */
public class Eliminatoria {

    // --- BLOQUE: CONFIGURACIÓN DE LA LLAVE ---
    private final Equipo equipoA;       // Club con localía en la Ida
    private final Equipo equipoB;       // Club con localía en la Vuelta
    private final boolean doblePartido; // Formato: true (Ida/Vta), false (Final única)

    // --- BLOQUE: REGISTRO DE ENCUENTROS ---
    private Partido ida;                // Primer round
    private Partido vuelta;             // Segundo round
    private Equipo  ganador;            // Club clasificado

    /**
     * Constructor: Inicializa el marco de la eliminatoria.
     */
    public Eliminatoria(Equipo equipoA, Equipo equipoB, boolean doblePartido) {
        this.equipoA      = equipoA;
        this.equipoB      = equipoB;
        this.doblePartido = doblePartido;
    }

    // ---------------------------------------------------------------------
    // BLOQUE: SIMULACIÓN EN SEGUNDO PLANO (MODO CPU)
    // ---------------------------------------------------------------------

    /**
     * Ejecuta el partido de ida de forma automatizada (IA vs IA).
     */
    public void jugarIdaAuto() {
        ida = new Partido(equipoA, equipoB);
        ida.simular();
    }

    /**
     * Ejecuta el partido de vuelta y actualiza el vencedor por marcador global.
     */
    public void jugarVueltaAuto() {
        if (!doblePartido) { 
            determinarGanador(); 
            return; 
        }
        // Inversión de localía para el segundo encuentro
        vuelta = new Partido(equipoB, equipoA);
        vuelta.simular();
        determinarGanador();
    }

    // ---------------------------------------------------------------------
    // BLOQUE: CÓMPUTO DE RESULTADOS Y DESEMPATES
    // ---------------------------------------------------------------------

    /**
     * Calcula el equipo clasificado sumando los goles de ambos encuentros.
     */
    public void determinarGanador() {
        // Bloque: Caso de Partido Único (Final UEFA)
        if (!doblePartido) {
            if (ida != null && ida.isTerminado()) {
                ganador = ida.getGanador();
            }
            return;
        }

        // Bloque: Caso de Eliminatoria Doble
        if (ida == null || !ida.isTerminado() || vuelta == null || !vuelta.isTerminado()) return;

        // Cómputo global de goles
        int golesA = ida.getGolesLocal()     + vuelta.getGolesVisitante();
        int golesB = ida.getGolesVisitante() + vuelta.getGolesLocal();

        if (golesA > golesB)      ganador = equipoA;
        else if (golesB > golesA) ganador = equipoB;
        else                      ganador = null; // Empate global que escala a penaltis
    }

    /**
     * Algoritmo de resolución para simulaciones rápidas donde no interviene el usuario.
     */
    public void resolverEmpateIA() {
        if (ganador != null) return;
        // En caso de empate técnico total, avanza el club con mejor ratio OVR
        double poderA = equipoA.getPoderOfensivo() + equipoA.getPoderDefensivo();
        double poderB = equipoB.getPoderOfensivo() + equipoB.getPoderDefensivo();
        ganador = (poderA > poderB) ? equipoA : equipoB;
    }

    /**
     * Determina si la igualdad en el global exige una tanda de penaltis.
     */
    public boolean requierePenaltis() {
        // Verificación en partido único
        if (!doblePartido) {
            return ida != null && ida.isTerminado() && ida.getGolesLocal() == ida.getGolesVisitante();
        }
        // Verificación en doble partido
        if (ida == null || !ida.isTerminado() || vuelta == null || !vuelta.isTerminado()) return false;
        
        int golesA = ida.getGolesLocal()     + vuelta.getGolesVisitante();
        int golesB = ida.getGolesVisitante() + vuelta.getGolesLocal();
        return golesA == golesB;
    }

    // ---------------------------------------------------------------------
    // BLOQUE: CONTROL DE PARTIDOS JUGABLES (USER MODE)
    // ---------------------------------------------------------------------

    public Partido crearPartidoIda() {
        ida = new Partido(equipoA, equipoB);
        return ida;
    }

    public Partido crearPartidoVuelta() {
        vuelta = new Partido(equipoB, equipoA);
        return vuelta;
    }

    /**
     * Genera un informe textual del estado de la llave para el panel de Torneo.
     */
    public String getResumen() {
        StringBuilder sb = new StringBuilder();
        sb.append(equipoA.getNombre()).append(" vs ").append(equipoB.getNombre());

        if (ida != null && ida.isTerminado()) {
            sb.append("\n [IDA] ").append(ida.getGolesLocal()).append(" - ").append(ida.getGolesVisitante());
        }
        if (vuelta != null && vuelta.isTerminado()) {
            sb.append("\n [VTA] ").append(vuelta.getGolesLocal()).append(" - ").append(vuelta.getGolesVisitante());
            int gA = ida.getGolesLocal() + vuelta.getGolesVisitante();
            int gB = ida.getGolesVisitante() + vuelta.getGolesLocal();
            sb.append(" (Global: ").append(gA).append("-").append(gB).append(")");
        }
        return sb.toString();
    }

    // --- ACCESORES DE ESTADO ---
    public Equipo  getEquipoA()      { return equipoA; }
    public Equipo  getEquipoB()      { return equipoB; }
    public Partido getIda()          { return ida; }
    public Partido getVuelta()       { return vuelta; }
    public Equipo  getGanador()      { return ganador; }
    public boolean isDoblePartido()  { return doblePartido; }
    
    public boolean isCompleta() {
        if (!doblePartido) return ida != null && ida.isTerminado();
        return (vuelta != null && vuelta.isTerminado()) || (ganador != null);
    }

    /**
     * Verifica si un equipo participa en esta llave.
     */
    public boolean esMiPartido(Equipo e) {
        if (e == null) return false;
        return equipoA.getNombre().equals(e.getNombre()) || equipoB.getNombre().equals(e.getNombre());
    }
}
