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
    
    // Tarjetas y Expulsiones (específicas de este partido)
    private java.util.HashMap<Jugador, Integer> tarjetasPartido = new java.util.HashMap<>();
    private java.util.HashSet<Jugador>          expulsados      = new java.util.HashSet<>();
    private Jugador mvp;

    // Penaltis
    private boolean necesitaPenaltis;
    private int     penaltisLocal;
    private int     penaltisVisitante;

    // Control de simulación por bloques
    private int     minutoActual = 1;
    private static final int DURACION = 90;
    private Equipo  ganadorPenaltis;

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

        if (rng.nextDouble() < probGol(local, visitante))
            sb.append(textoGol(local, min));
        if (rng.nextDouble() < probGol(visitante, local))
            sb.append(textoGol(visitante, min));
        if (rng.nextDouble() < 0.05) // Probabilidad de tarjeta
            sb.append(textoTarjeta(min));

        // Desgaste de titulares
        for (Jugador j : local.getTitulares())     j.desgastar(rng.nextInt(3) + 1);
        for (Jugador j : visitante.getTitulares())  j.desgastar(rng.nextInt(3) + 1);

        return sb.toString();
    }

    /** Probabilidad calibrada basada en el balance de poder actual y tácticas específicas. */
    private double probGol(Equipo atacante, Equipo defensor) {
        String tactAtac = atacante.getTactica();
        String tactDef  = defensor.getTactica();

        // 1. Caso AUTOBÚS: 10% (0.10) por bloque
        if (tactAtac.equals("Autobús")) return 0.10;

        // 2. Caso POR LAS BANDAS: 60% (0.60) si tiene extremos y el rival no tiene Autobús
        if (tactAtac.equals("Por las bandas") && tieneExtremos(atacante) && !tactDef.equals("Autobús")) {
            return 0.60;
        }

        // 3. Caso general: Base alta pero influenciada por defensa rival
        double ataque  = getPoderOfensivoActual(atacante);
        double defensa = getPoderDefensivoActual(defensor);
        double balance = (ataque + 1) / (defensa + 1); // evitamos div por cero
        
        double base = 0.25; // Base arcade alta (~4-5 goles/partido)
        if (tactDef.equals("Autobús")) base *= 0.5; // Si el rival tiene bus y yo no voy por bandas
        
        return base * balance;
    }

    private boolean tieneExtremos(Equipo eq) {
        for (Jugador j : eq.getTitulares()) {
            if (j.getPosicion().matches("ED|EI|MD|MI|SD")) return true;
        }
        return false;
    }

    private double getPoderOfensivoActual(Equipo eq) {
        double p = 0;
        for (Jugador j : eq.getTitulares()) {
            if (!expulsados.contains(j)) {
                if ("DEL".equals(j.getPosicion())) p += j.getFactorRendimiento() * j.getAtaque() * 1.2;
                else if ("MED".equals(j.getPosicion())) p += j.getFactorRendimiento() * j.getAtaque() * 0.7;
            }
        }
        // Bono táctico (replicado de Equipo.java pero filtrando expulsados)
        switch (eq.getTactica()) {
            case "Tiki Taka"   -> p *= 1.3;
            case "Contraataque" -> p *= 1.2;
            case "Autobús"     -> p *= 0.5;
            case "Por las bandas" -> p *= 1.1;
        }
        return p;
    }

    private double getPoderDefensivoActual(Equipo eq) {
        double p = 0;
        for (Jugador j : eq.getTitulares()) {
            if (!expulsados.contains(j)) {
                if ("POR".equals(j.getPosicion())) p += j.getFactorRendimiento() * j.getDefensa() * 1.3;
                else if ("DEF".equals(j.getPosicion())) p += j.getFactorRendimiento() * j.getDefensa() * 1.0;
                else if ("MED".equals(j.getPosicion())) p += j.getFactorRendimiento() * j.getDefensa() * 0.4;
            }
        }
        switch (eq.getTactica()) {
            case "Autobús"     -> p *= 1.5;
            case "Tiki Taka"   -> p *= 0.8;
            case "Contraataque" -> p *= 1.1;
        }
        return p;
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
        // Filtrar jugadores no expulsados
        ArrayList<Jugador> candidatos = new ArrayList<>();
        for (Jugador j : eq.getTitulares()) if (!expulsados.contains(j)) candidatos.add(j);
        
        if (candidatos.isEmpty()) return "";
        Jugador j = candidatos.get(rng.nextInt(candidatos.size()));
        
        int amarillas = tarjetasPartido.getOrDefault(j, 0) + 1;
        tarjetasPartido.put(j, amarillas);
        j.addAmarilla(); // Estadística global
        
        if (amarillas == 2) {
            expulsados.add(j);
            return String.format("🟥 Min %2d' — ¡ROJA! (Doble amarilla) para %s (%s)\n", min, j.getNombre(), eq.getNombre());
        }
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
        
        determinarMVP();
        if (mvp != null) {
            narracion.append(" ⭐ JUGADOR DEL PARTIDO (MVP): ").append(mvp.getNombre())
                     .append(" [").append(mvp.getPosicion()).append("]\n");
        }
        
        local.addGolesAFavor(golesLocal);
        local.addGolesEnContra(golesVisitante);
        visitante.addGolesAFavor(golesVisitante);
        visitante.addGolesEnContra(golesLocal);
        terminado = true;
    }

    private void determinarMVP() {
        ArrayList<Jugador> todos = new ArrayList<>(local.getTitulares());
        todos.addAll(visitante.getTitulares());
        
        Jugador mejor = null;
        double maxRating = -1;
        
        for (Jugador j : todos) {
            // Un cálculo simple de mérito: Media + (Goles * 15)
            // Para MVPs reales necesitaría registrar goles por partido en un mapa
            // pero para simplificar, usaremos la media general + un factor azaroso
            double rating = j.getMediaGeneral() + rng.nextInt(10);
            if (mejor == null || rating > maxRating) {
                mejor = j;
                maxRating = rating;
            }
        }
        this.mvp = mejor;
    }

    // ── Penaltis ──────────────────────────────────────────────────────────


    // ── Penaltis (Paso a Paso) ────────────────────────────────────────────

    private int     rondaPenal = 0;
    private boolean enTanda    = false;
    private ArrayList<Jugador> lanzadoresL;
    private ArrayList<Jugador> lanzadoresV;

    public void prepararTanda(ArrayList<Jugador> l, ArrayList<Jugador> v) {
        this.lanzadoresL = l;
        this.lanzadoresV = v;
        this.penaltisLocal = 0;
        this.penaltisVisitante = 0;
        this.rondaPenal = 0;
        this.enTanda = true;
        this.necesitaPenaltis = true;
    }

    /** Simula la siguiente ronda de penaltis (un tiro por equipo). */
    public String simularSiguienteRondaPenal() {
        if (!enTanda) return "";
        
        Jugador pL = (lanzadoresL != null && rondaPenal < lanzadoresL.size()) ? lanzadoresL.get(rondaPenal) : null;
        Jugador pV = (lanzadoresV != null && rondaPenal < lanzadoresV.size()) ? lanzadoresV.get(rondaPenal) : null;

        boolean gL = simularTiro(pL);
        boolean gV = simularTiro(pV);

        if (gL) penaltisLocal++;
        if (gV) penaltisVisitante++;

        String nomL = pL != null ? pL.getNombre() : local.getNombre();
        String nomV = pV != null ? pV.getNombre() : visitante.getNombre();
        
        String tipo = (rondaPenal < 5) ? "Ronda " + (rondaPenal + 1) : "Muerte Súbita " + (rondaPenal - 4);
        String res = String.format("\n⚽ %s:\n  %-15s %s\n  %-15s %s\n  Marcador: %d - %d\n",
            tipo, nomL, gL ? "¡GOL!" : "FALLA", nomV, gV ? "¡GOL!" : "FALLA", penaltisLocal, penaltisVisitante);
        
        narracion.append(res);
        rondaPenal++;

        // Verificar fin
        if (rondaPenal >= 5 && penaltisLocal != penaltisVisitante) {
            finalizarTanda();
        } else if (rondaPenal >= 15) { // Límite
            finalizarTanda();
        }

        return res;
    }

    private void finalizarTanda() {
        enTanda = false;
        if (penaltisLocal == penaltisVisitante) {
            if (rng.nextBoolean()) penaltisLocal++; else penaltisVisitante++;
        }
        this.ganadorPenaltis = (penaltisLocal > penaltisVisitante) ? local : visitante;
    }

    public void simularGolForzado(boolean localGana) {
        if (localGana) {
            golesLocal++;
            local.addGolesAFavor(1);
            visitante.addGolesEnContra(1);
            narracion.append("\n⚡ ¡MINUTO 90! Gol agónico de ").append(local.getNombre()).append(". Evita la tanda de penaltis.\n");
        } else {
            golesVisitante++;
            visitante.addGolesAFavor(1);
            local.addGolesEnContra(1);
            narracion.append("\n⚡ ¡MINUTO 90! Gol agónico de ").append(visitante.getNombre()).append(". Evita la tanda de penaltis.\n");
        }
    }

    public boolean isEnTanda() { return enTanda; }

    /** Simula la tanda de penaltis con lanzadores específicos; devuelve el texto para la GUI. */
    public String simularPenaltis(ArrayList<Jugador> lanzLocal, ArrayList<Jugador> lanzVisit) {
        prepararTanda(lanzLocal, lanzVisit);
        StringBuilder sb = new StringBuilder("\n🏟 ¡COMIENZA LA TANDA DE PENALTIS! 🥅\n");
        while (enTanda) {
            sb.append(simularSiguienteRondaPenal());
        }
        return sb.toString();
    }

    private boolean simularTiro(Jugador j) {
        // Probabilidad: 0.60 base + hasta 0.35 por ataque -> 0.60 a 0.95
        double prob = j != null ? (0.60 + (j.getAtaque() / 280.0)) : 0.75;
        return rng.nextDouble() < prob;
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
        if (!terminado && !necesitaPenaltis) return null;
        if (necesitaPenaltis && !enTanda) return ganadorPenaltis;
        if (golesLocal     > golesVisitante) return local;
        if (golesVisitante > golesLocal)     return visitante;
        return null;
    }
}
