package model;


/**
 * Eliminatoria — gestiona la ida y vuelta (o la final a partido único)
 * entre dos equipos. Calcula el clasificado considerando el global y,
 * si hay empate, ejecuta los penaltis a través del Partido de ida.
 */
public class Eliminatoria {

    private final Equipo equipoA;
    private final Equipo equipoB;
    private final boolean doblePartido; // false → Final (partido único)

    private Partido ida;
    private Partido vuelta;
    private Equipo  ganador;

    // ─────────────────────────────────────────────────────────────────────
    public Eliminatoria(Equipo equipoA, Equipo equipoB, boolean doblePartido) {
        this.equipoA      = equipoA;
        this.equipoB      = equipoB;
        this.doblePartido = doblePartido;
    }

    // ── Simulación automática (para los partidos de la IA) ───────────────

    /** Juega la ida automáticamente. */
    public void jugarIdaAuto() {
        ida = new Partido(equipoA, equipoB);
        ida.simular();
    }

    /** Juega la vuelta automáticamente y determina el clasificado. */
    public void jugarVueltaAuto() {
        if (!doblePartido) { determinarGanador(); return; }
        vuelta = new Partido(equipoB, equipoA); // B es local en vuelta
        vuelta.simular();
        determinarGanador();
    }

    /** Determina el ganador de la eliminatoria si no hay empate. */
    public void determinarGanador() {
        if (!doblePartido) {
            // Final: partido único
            if (ida != null && ida.isTerminado()) {
                ganador = ida.getGanador();
            }
            return;
        }

        if (ida == null || !ida.isTerminado() || vuelta == null || !vuelta.isTerminado()) return;

        // Doble partido: suma de goles
        int golesA = ida.getGolesLocal()     + vuelta.getGolesVisitante();
        int golesB = ida.getGolesVisitante() + vuelta.getGolesLocal();

        if (golesA > golesB) {
            ganador = equipoA;
        } else if (golesB > golesA) {
            ganador = equipoB;
        } else {
            // Empate global -> No hay ganador deportivo.
            // Para que el torneo no se rompa, si no es el usuario, resolvemos por suerte o fuerza.
            ganador = null; 
        }
    }

    /** Resolución forzada para simulaciones de la IA. */
    public void resolverEmpateIA() {
        if (ganador != null) return;
        // El que tenga más poder total gana el desempate "en los despachos"
        if (equipoA.getPoderofensivo() + equipoA.getPoderDefensivo() > 
            equipoB.getPoderofensivo() + equipoB.getPoderDefensivo()) {
            ganador = equipoA;
        } else {
            ganador = equipoB;
        }
    }

    /** Verifica si la eliminatoria ha terminado en empate global y requiere penaltis. */
    public boolean requierePenaltis() {
        if (!doblePartido) {
            return ida != null && ida.isTerminado() && ida.getGolesLocal() == ida.getGolesVisitante() && !ida.isNecesitaPenaltis();
        }
        if (ida == null || !ida.isTerminado() || vuelta == null || !vuelta.isTerminado()) return false;
        
        int golesA = ida.getGolesLocal()     + vuelta.getGolesVisitante();
        int golesB = ida.getGolesVisitante() + vuelta.getGolesLocal();
        return golesA == golesB && !vuelta.isNecesitaPenaltis();
    }

    // ── Creación de partidos para el jugador humano ───────────────────────

    /** Crea el partido de ida (sin simularlo). */
    public Partido crearPartidoIda() {
        ida = new Partido(equipoA, equipoB);
        return ida;
    }

    /** Crea el partido de vuelta (sin simularlo). */
    public Partido crearPartidoVuelta() {
        vuelta = new Partido(equipoB, equipoA);
        return vuelta;
    }

    // ── Resumen en texto ──────────────────────────────────────────────────

    public String getResumen() {
        StringBuilder sb = new StringBuilder();
        sb.append(equipoA.getNombre()).append("  vs  ").append(equipoB.getNombre()).append("\n");

        if (ida != null && ida.isTerminado()) {
            sb.append("  Ida:    ").append(ida.getGolesLocal())
              .append(" - ").append(ida.getGolesVisitante()).append("\n");
        }
        if (vuelta != null && vuelta.isTerminado()) {
            sb.append("  Vuelta: ").append(vuelta.getGolesLocal())
              .append(" - ").append(vuelta.getGolesVisitante()).append("\n");

            int gA = ida.getGolesLocal()     + vuelta.getGolesVisitante();
            int gB = ida.getGolesVisitante() + vuelta.getGolesLocal();
            sb.append("  Global: ").append(gA).append(" - ").append(gB).append("\n");
        }
        if (ganador != null) {
            sb.append("  ✅ CLASIFICADO: ").append(ganador.getNombre()).append("\n");
        }
        return sb.toString();
    }

    // ── Getters ───────────────────────────────────────────────────────────
    public Equipo  getEquipoA()      { return equipoA; }
    public Equipo  getEquipoB()      { return equipoB; }
    public Partido getIda()          { return ida; }
    public Partido getVuelta()       { return vuelta; }
    public Equipo  getGanador()      { return ganador; }
    public boolean isDoblePartido()  { return doblePartido; }
    public boolean isCompleta() {
        if (!doblePartido) return ida != null && ida.isTerminado();
        return vuelta != null && vuelta.isTerminado();
    }

    public void setIda(Partido p)    { this.ida = p; }
    public void setVuelta(Partido p) { this.vuelta = p; }
    public void setGanador(Equipo e) { this.ganador = e; }
}
