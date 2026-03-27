package model;

import interfaces.Simulable;
import java.util.ArrayList;
import java.util.Random;

/**
 * Partido — implementa Simulable para ser ejecutado por el motor del torneo.
 *
 * INTERFACES (Simulable): el Torneo y la GUI llaman a simular()
 * sin conocer los detalles internos de la simulación (polimorfismo).
 *
 * La simulación se divide en bloques de 15 minutos para que la GUI
 * ofrezca pausas donde el usuario puede hacer sustituciones.
 */
public class Partido implements Simulable {

    private final Equipo            local;
    private final Equipo            visitante;
    private       int               golesLocal;
    private       int               golesVisitante;
    private       boolean           terminado;
    private final StringBuilder     narracion;
    private final ArrayList<String> eventos;
    private final Random            rng;

    // Penaltis
    private boolean necesitaPenaltis;
    private int     penaltisLocal;
    private int     penaltisVisitante;

    // Control de simulación por bloques
    private int     minutoActual = 1;
    private static final int DURACION = 90;

    public Partido(Equipo local, Equipo visitante) {
        this.local     = local;
        this.visitante = visitante;
        this.narracion = new StringBuilder();
        this.eventos   = new ArrayList<>();
        this.rng       = new Random();
    }

    // ── Simulable: simulación automática completa (para la IA) ───────────
    @Override
    public void simular() {
        local.establecerMejorOnce();
        visitante.establecerMejorOnce();
        agregarEncabezado();
        for (int min = 1; min <= DURACION; min += 5) {
            String texto = procesarMinuto(min);
            narracion.append(texto);
        }
        agregarResultadoFinal();
    }

    @Override public String  getNarracion() { return narracion.toString(); }
    @Override public boolean isTerminado()  { return terminado; }
    @Override public int[]   getMarcador()  { return new int[]{golesLocal, golesVisitante}; }

    // ── Simulación por bloques (modo interactivo para el jugador) ─────────

    /** Prepara el partido y muestra el encabezado. */
    public void iniciarSimulacion() {
        local.establecerMejorOnce();
        visitante.establecerMejorOnce();
        agregarEncabezado();
    }

    /**
     * Simula el siguiente bloque de ~15 minutos.
     * @return texto narrativo del bloque para mostrarlo en la GUI.
     */
    public String simularSiguienteBloque() {
        if (terminado || minutoActual > DURACION) return "";
        StringBuilder bloque = new StringBuilder();
        int hasta = Math.min(minutoActual + 14, DURACION);
        for (int min = minutoActual; min <= hasta; min += 5) {
            String s = procesarMinuto(min);
            bloque.append(s);
            narracion.append(s);
        }
        minutoActual = hasta + 1;
        if (minutoActual > DURACION) agregarResultadoFinal();
        return bloque.toString();
    }

    public boolean isEnJuego() { return !terminado && minutoActual <= DURACION; }

    // ── Lógica interna ────────────────────────────────────────────────────

    private void agregarEncabezado() {
        narracion.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        narracion.append("  ⚽ ").append(local.getNombre())
                 .append("  vs  ").append(visitante.getNombre()).append("\n");
        narracion.append("  Local: ").append(local.getFormacion())
                 .append("  |  Visitante: ").append(visitante.getFormacion()).append("\n");
        narracion.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
    }

    /** Simula 5 minutos y devuelve el texto generado. */
    private String procesarMinuto(int min) {
        StringBuilder sb = new StringBuilder();

        if (rng.nextDouble() < probGol(local.getPoderofensivo(), visitante.getPoderDefensivo()))
            sb.append(textoGol(local, min));
        if (rng.nextDouble() < probGol(visitante.getPoderofensivo(), local.getPoderDefensivo()))
            sb.append(textoGol(visitante, min));
        if (rng.nextDouble() < 0.04)
            sb.append(textoTarjeta(min));

        // Desgaste de titulares
        for (Jugador j : local.getTitulares())     j.desgastar(rng.nextInt(3) + 1);
        for (Jugador j : visitante.getTitulares())  j.desgastar(rng.nextInt(3) + 1);

        return sb.toString();
    }

    /** Probabilidad calibrada para ~2-3 goles promedio por partido. */
    private double probGol(double ataque, double defensa) {
        double total = ataque + defensa;
        return total == 0 ? 0.04 : (ataque / total) * 0.09;
    }

    private String textoGol(Equipo atacante, int min) {
        if (atacante == local) golesLocal++;
        else                   golesVisitante++;

        Jugador goleador  = elegirGoleador(atacante);
        Jugador asistente = elegirAsistente(atacante, goleador);
        if (goleador  != null) goleador.addGol();
        if (asistente != null) asistente.addAsistencia();

        eventos.add("GOL:" + atacante.getNombre() + ":" + min);
        return String.format("⚽ Min %2d' — ¡GOL DE %s!  %s%s  [%d-%d]\n",
            min, atacante.getNombre(),
            goleador  != null ? goleador.getNombre() : "?",
            asistente != null ? " (Asist. " + asistente.getNombre() + ")" : "",
            golesLocal, golesVisitante);
    }

    private String textoTarjeta(int min) {
        Equipo eq = rng.nextBoolean() ? local : visitante;
        if (eq.getTitulares().isEmpty()) return "";
        Jugador j = eq.getTitulares().get(rng.nextInt(eq.getTitulares().size()));
        j.addAmarilla();
        return String.format("🟨 Min %2d' — Amarilla: %s (%s)\n", min, j.getNombre(), eq.getNombre());
    }

    private Jugador elegirGoleador(Equipo eq) {
        ArrayList<Jugador> pool = new ArrayList<>();
        for (Jugador j : eq.getTitulares()) {
            if ("DEL".equals(j.getPosicion()))      { pool.add(j); pool.add(j); }
            else if ("MED".equals(j.getPosicion())) { pool.add(j); }
        }
        return pool.isEmpty() ? null : pool.get(rng.nextInt(pool.size()));
    }

    private Jugador elegirAsistente(Equipo eq, Jugador exc) {
        if (rng.nextDouble() > 0.70) return null;
        ArrayList<Jugador> pool = new ArrayList<>();
        for (Jugador j : eq.getTitulares())
            if (j != exc && ("MED".equals(j.getPosicion()) || "DEL".equals(j.getPosicion()))) pool.add(j);
        return pool.isEmpty() ? null : pool.get(rng.nextInt(pool.size()));
    }

    private void agregarResultadoFinal() {
        narracion.append("\n🏁 RESULTADO FINAL:  ")
                 .append(local.getNombre()).append("  ")
                 .append(golesLocal).append(" - ").append(golesVisitante)
                 .append("  ").append(visitante.getNombre()).append("\n");
        local.addGolesAFavor(golesLocal);
        local.addGolesEnContra(golesVisitante);
        visitante.addGolesAFavor(golesVisitante);
        visitante.addGolesEnContra(golesLocal);
        terminado = true;
    }

    // ── Penaltis ──────────────────────────────────────────────────────────

    /** Simula la tanda de penaltis; devuelve el texto para la GUI. */
    public String simularPenaltis() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n🥅 ¡TANDA DE PENALTIS!\n");
        penaltisLocal = penaltisVisitante = 0;

        for (int i = 1; i <= 5; i++) {
            boolean gL = rng.nextDouble() < 0.75;
            boolean gV = rng.nextDouble() < 0.75;
            if (gL) penaltisLocal++;
            if (gV) penaltisVisitante++;
            sb.append(String.format("  %d.  %-22s%-12s | %-22s%s\n",
                i, local.getNombre(),     gL ? "⚽ GOL" : "❌ FALLO",
                   visitante.getNombre(), gV ? "⚽ GOL" : "❌ FALLO"));
        }
        int ronda = 1;
        while (penaltisLocal == penaltisVisitante) {
            boolean gL = rng.nextDouble() < 0.75;
            boolean gV = rng.nextDouble() < 0.75;
            if (gL) penaltisLocal++;
            if (gV) penaltisVisitante++;
            sb.append(String.format("  MS %d:  %s  |  %s\n",
                ronda++, gL ? "⚽" : "❌", gV ? "⚽" : "❌"));
        }
        sb.append(String.format("  Resultado penaltis: %d - %d\n\n", penaltisLocal, penaltisVisitante));
        necesitaPenaltis = true;
        narracion.append(sb);
        return sb.toString();
    }

    // ── Getters ───────────────────────────────────────────────────────────
    public Equipo  getLocal()             { return local; }
    public Equipo  getVisitante()         { return visitante; }
    public int     getGolesLocal()        { return golesLocal; }
    public int     getGolesVisitante()    { return golesVisitante; }
    public int     getPenaltisLocal()     { return penaltisLocal; }
    public int     getPenaltisVisitante() { return penaltisVisitante; }
    public boolean isNecesitaPenaltis()   { return necesitaPenaltis; }
    public int     getMinutoActual()      { return minutoActual; }

    public Equipo getGanador() {
        if (!terminado) return null;
        if (necesitaPenaltis) return penaltisLocal > penaltisVisitante ? local : visitante;
        if (golesLocal     > golesVisitante) return local;
        if (golesVisitante > golesLocal)     return visitante;
        return null;
    }
}
