package model;

import interfaces.Simulable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

/**
 * Clase Partido: Motor de simulación de eventos deportivos.
 *
 * Orquesta cada encuentro minuto a minuto, calculando probabilidades de éxito
 * ofensivo y defensivo basándose en el estado de forma, la formación,
 * la energía residual de los futbolistas y la **Mentalidad Táctica** elegida.
 *
 * Implementa {@link Simulable} para permitir simulación rápida (CPU)
 * y {@link Serializable} para persistencia con ObjectOutputStream.
 */
public class Partido implements Simulable, Serializable {

    /** Versión de serialización para compatibilidad de ficheros. */
    private static final long serialVersionUID = 5L;

    // --- BLOQUE: ACTORES Y RESULTADO ---
    /** Equipo que juega como local. */
    private final Equipo            local;
    /** Equipo que juega como visitante. */
    private final Equipo            visitante;
    /** Goles anotados por el equipo local. */
    private       int               golesLocal;
    /** Goles anotados por el equipo visitante. */
    private       int               golesVisitante;
    /** Indica si el partido ha finalizado. */
    private       boolean           terminado;

    // --- BLOQUE: MOTOR DE NARRATIVA ---
    /** Registro histórico completo del partido (log textual). */
    private final StringBuilder     narracion;
    /** Cola de eventos para disparadores visuales en la UI. */
    private final ArrayList<String> eventos;
    /** Generador de números aleatorios para la simulación. */
    private final Random            rng;

    // --- BLOQUE: INCIDENCIAS Y ESTADÍSTICAS ---
    /** Mapa de tarjetas acumuladas por jugador durante el partido. */
    private java.util.HashMap<Jugador, Integer> tarjetasPartido = new java.util.HashMap<>();
    /** Conjunto de jugadores expulsados (doble amarilla). */
    private java.util.HashSet<Jugador>          expulsados      = new java.util.HashSet<>();

    // --- BLOQUE: RESOLUCIÓN POR PENALTIS ---
    /** Goles marcados en la tanda por el equipo local. */
    private int     penaltisLocal;
    /** Goles marcados en la tanda por el equipo visitante. */
    private int     penaltisVisitante;
    /** Equipo ganador de la tanda de penaltis (null si no se ha resuelto). */
    private Equipo  ganadorPenaltis;

    // --- BLOQUE: CONTROL TEMPORAL ---
    /** Minuto actual de la simulación. */
    private int     minutoActual = 1;
    /** Duración total del tiempo reglamentario. */
    private static final int DURACION = 90;
    /** Ronda actual de la tanda de penaltis. */
    private int rondaPenal = 0;
    /** Número mínimo de rondas antes de poder decidir ganador por penaltis. */
    private static final int RONDAS_MINIMAS_PENALTIS = 5;

    /**
     * Constructor: Inicializa las condiciones del encuentro.
     *
     * @param local     Equipo que juega en casa.
     * @param visitante Equipo que juega fuera.
     */
    public Partido(Equipo local, Equipo visitante) {
        this.local     = local;
        this.visitante = visitante;
        this.narracion = new StringBuilder();
        this.eventos   = new ArrayList<>();
        this.rng       = new Random();
    }

    // ---------------------------------------------------------------------
    // BLOQUE: SIMULACIÓN RÁPIDA (MODO CPU)
    // ---------------------------------------------------------------------

    /**
     * Resuelve el partido de forma inmediata simulando bloques de 5 minutos.
     * Usado para partidos CPU vs CPU que no requieren interacción.
     */
    @Override
    public void simular() {
        local.establecerMejorOnce();
        visitante.establecerMejorOnce();
        agregarEncabezado();
        for (int min = 1; min <= DURACION; min += 5) {
            narracion.append(procesarMinuto(min));
        }
        agregarResultadoFinal();
    }

    @Override public String  getNarracion() { return narracion.toString(); }
    @Override public boolean isTerminado()  { return terminado; }
    @Override public int[]   getMarcador()  { return new int[]{golesLocal, golesVisitante}; }

    // ---------------------------------------------------------------------
    // BLOQUE: SIMULACIÓN POR SEGMENTOS (MODO INTERACTIVO)
    // ---------------------------------------------------------------------

    /**
     * Prepara el terreno de juego y las alineaciones iniciales.
     * Debe llamarse antes de {@link #simularSiguienteBloque()}.
     */
    public void iniciarSimulacion() {
        local.establecerMejorOnce();
        visitante.establecerMejorOnce();
        agregarEncabezado();
    }

    /**
     * Avanza el cronómetro 15 minutos reales de simulación táctica.
     * @return El segmento narrativo producido en este bloque.
     */
    public String simularSiguienteBloque() {
        if (terminado || minutoActual > DURACION) return "";
        StringBuilder bloque = new StringBuilder();
        int limite = Math.min(minutoActual + 14, DURACION);

        for (int min = minutoActual; min <= limite; min += 5) {
            String s = procesarMinuto(min);
            bloque.append(s);
            narracion.append(s);
        }

        minutoActual = limite + 1;
        if (minutoActual > DURACION) agregarResultadoFinal();
        return bloque.toString();
    }

    /** @return true si el partido sigue en curso. */
    public boolean isEnJuego() { return !terminado && minutoActual <= DURACION; }

    // ---------------------------------------------------------------------
    // BLOQUE: ALGORITMOS DE PROBABILIDAD Y TÁCTICA
    // ---------------------------------------------------------------------

    /** Añade la cabecera del partido a la narración. */
    private void agregarEncabezado() {
        narracion.append("⚽ UCL MATCH: ").append(local.getNombre())
                 .append(" vs ").append(visitante.getNombre()).append("\n");
        narracion.append("--------------------------------------------------\n");
    }

    /**
     * Procesa las variables de éxito para un intervalo de 5 minutos.
     * Calcula probabilidades de gol, tarjetas y aplica desgaste físico.
     *
     * @param min Minuto actual de simulación.
     * @return Texto narrativo de los eventos ocurridos.
     */
    private String procesarMinuto(int min) {
        StringBuilder sb = new StringBuilder();
        boolean accionDestacada = false;

        // Cálculo de goles para cada equipo
        if (rng.nextDouble() < probGol(local, visitante)) {
            sb.append(textoGol(local, min));
            accionDestacada = true;
        }
        if (rng.nextDouble() < probGol(visitante, local)) {
            sb.append(textoGol(visitante, min));
            accionDestacada = true;
        }

        // Cálculo de tarjetas (5% de probabilidad por bloque)
        if (rng.nextDouble() < 0.05) {
            sb.append(textoTarjeta(min));
            accionDestacada = true;
        }

        // Narrativa de relleno para dar ritmo al partido
        if (!accionDestacada && rng.nextDouble() < 0.03) {
            sb.append(String.format("🕒 %02d' — Dominio disputado en el centro del campo...\n", min));
        }

        // Aplicación de fatiga progresiva a los jugadores en campo
        for (Jugador j : local.getTitulares())     j.desgastar(rng.nextInt(3) + 1);
        for (Jugador j : visitante.getTitulares())  j.desgastar(rng.nextInt(3) + 1);

        return sb.toString();
    }

    /**
     * Calcula la probabilidad de que un equipo anote en un bloque de 5 minutos.
     *
     * @param atacante Equipo que ataca.
     * @param defensor Equipo que defiende.
     * @return Probabilidad de gol (0.0 - 1.0).
     */
    private double probGol(Equipo atacante, Equipo defensor) {
        double atk = getPoderOfensivoActual(atacante);
        double def = getPoderDefensivoActual(defensor);
        double ratio = atk / Math.max(def, 1.0);
        double base  = 0.12;

        // -------------------------------------------------------------
        // APLICACIÓN DE MODIFICADORES TÁCTICOS (MENTALIDAD)
        // -------------------------------------------------------------
        String tacticaAtk = atacante.getTactica();
        String tacticaDef = defensor.getTactica();

        // Modificador ofensivo (Equipo Atacante)
        if ("Ofensiva Total".equals(tacticaAtk)) base *= 1.35; // +35% creación ofensiva
        else if ("Tiki Taka".equals(tacticaAtk)) base *= 1.15; // +15% creación ofensiva
        else if ("Contraataque".equals(tacticaAtk)) base *= 1.10; // +10% creación
        else if ("Autobús".equals(tacticaAtk)) base *= 0.60;   // -40% creación ofensiva (se ataca muy poco)

        // Modificador defensivo (Equipo Defensor frente a ratio de gol)
        if ("Autobús".equals(tacticaDef)) ratio *= 0.65;       // -35% prob de recibir gol
        else if ("Ofensiva Total".equals(tacticaDef)) ratio *= 1.30; // +30% prob de recibir gol (huecos atrás)
        else if ("Contraataque".equals(tacticaDef)) ratio *= 0.85; // Defensa sólida

        // El usuario tiene un ligero plus estratégico (+15%)
        if (atacante.isUsuario()) base *= 1.15;
        
        return base * ratio;
    }

    /** Calcula el poder ofensivo actual excluyendo expulsados. */
    private double getPoderOfensivoActual(Equipo eq) {
        double p = 0;
        for (Jugador j : eq.getTitulares()) {
            if (!expulsados.contains(j)) {
                String cat = Jugador.categoriaGeneral(j.getPosicion());
                if ("DEL".equals(cat))      p += j.getFactorRendimiento() * j.getAtaque() * 1.5;
                else if ("MED".equals(cat)) p += j.getFactorRendimiento() * j.getAtaque() * 0.9;
            }
        }
        return p;
    }

    /** Calcula el poder defensivo actual excluyendo expulsados. */
    private double getPoderDefensivoActual(Equipo eq) {
        double p = 0;
        for (Jugador j : eq.getTitulares()) {
            if (!expulsados.contains(j)) {
                String cat = Jugador.categoriaGeneral(j.getPosicion());
                if ("POR".equals(cat))      p += j.getFactorRendimiento() * j.getDefensa() * 1.8;
                else if ("DEF".equals(cat)) p += j.getFactorRendimiento() * j.getDefensa() * 1.2;
            }
        }
        return p;
    }

    // ---------------------------------------------------------------------
    // BLOQUE: GESTIÓN DE EVENTOS DE CAMPO
    // ---------------------------------------------------------------------

    /** Genera el texto narrativo de un gol y actualiza estadísticas. */
    private String textoGol(Equipo atacante, int min) {
        if (atacante == local) golesLocal++; else golesVisitante++;
        Jugador goleador = elegirGoleador(atacante);
        if (goleador != null) {
            goleador.addGol();
            return String.format("⚽ [%02d'] ¡GOOOL! %s marca para el %s (%d-%d)\n",
                                  min, goleador.getNombre(), atacante.getNombre(), golesLocal, golesVisitante);
        }
        return String.format("⚽ [%02d'] ¡GOL del %s! (%d-%d)\n", min, atacante.getNombre(), golesLocal, golesVisitante);
    }

    /** Genera el texto narrativo de una tarjeta amarilla o roja. */
    private String textoTarjeta(int min) {
        Equipo eq = rng.nextBoolean() ? local : visitante;
        ArrayList<Jugador> vivos = new ArrayList<>();
        for (Jugador j : eq.getTitulares()) {
            if (!expulsados.contains(j)) vivos.add(j);
        }
        if (vivos.isEmpty()) return "";

        Jugador j = vivos.get(rng.nextInt(vivos.size()));
        int amonestaciones = tarjetasPartido.getOrDefault(j, 0) + 1;
        tarjetasPartido.put(j, amonestaciones);

        if (amonestaciones >= 2) {
            expulsados.add(j);
            return String.format("🟥 [%02d'] ¡EXPULSIÓN! %s abandona el campo (%s)\n",
                                  min, j.getNombre(), eq.getNombre());
        }
        return String.format("🟨 [%02d'] Amonestación: %s (%s)\n", min, j.getNombre(), eq.getNombre());
    }

    /**
     * Selección ponderada del goleador: los delanteros tienen más probabilidades.
     * Se usa un sistema de "boletos" donde DEL=10, MED=5, otros=1.
     */
    private Jugador elegirGoleador(Equipo eq) {
        ArrayList<Jugador> candidatos = new ArrayList<>();
        for (Jugador j : eq.getTitulares()) {
            if (expulsados.contains(j)) continue;
            String cat = Jugador.categoriaGeneral(j.getPosicion());
            int boletos = ("DEL".equals(cat)) ? 10 : ("MED".equals(cat)) ? 5 : 1;
            for (int i = 0; i < boletos; i++) candidatos.add(j);
        }
        return candidatos.isEmpty() ? null : candidatos.get(rng.nextInt(candidatos.size()));
    }

    /** Cierra el partido, registra goles en los equipos y marca como terminado. */
    private void agregarResultadoFinal() {
        narracion.append("\n✅ FINAL DEL TIEMPO REGLAMENTARIO: ")
                 .append(golesLocal).append(" - ").append(golesVisitante).append("\n");
        local.addGolesAFavor(golesLocal);
        local.addGolesEnContra(golesVisitante);
        visitante.addGolesAFavor(golesVisitante);
        visitante.addGolesEnContra(golesLocal);
        terminado = true;
    }

    // ---------------------------------------------------------------------
    // BLOQUE: PROTOCOLO DE DESEMPATE (PENALTIS)
    // ---------------------------------------------------------------------

    /**
     * Simula una ronda de la tanda de penaltis (ambos equipos lanzan).
     * Probabilidad de acierto: 78% por lanzamiento.
     *
     * @return Log narrativo de la ronda.
     */
    public String simularSiguienteRondaPenal() {
        rondaPenal++;
        boolean aciertoLocal = rng.nextDouble() < 0.78;
        boolean aciertoVisit = rng.nextDouble() < 0.78;

        if (aciertoLocal) penaltisLocal++;
        if (aciertoVisit) penaltisVisitante++;

        String log = String.format("🥅 Ronda %d: [%s] %s (%d) | (%d) %s [%s]\n",
                     rondaPenal,
                     (aciertoLocal ? "⚽" : "❌"), local.getNombre(), penaltisLocal,
                     penaltisVisitante, visitante.getNombre(), (aciertoVisit ? "⚽" : "❌"));

        // Solo se decide ganador pasadas las 5 rondas mínimas
        if (rondaPenal >= RONDAS_MINIMAS_PENALTIS && penaltisLocal != penaltisVisitante) {
             ganadorPenaltis = (penaltisLocal > penaltisVisitante) ? local : visitante;
        }
        // Muerte súbita: si la diferencia es insalvable antes de completar 5 rondas
        else if (rondaPenal < RONDAS_MINIMAS_PENALTIS) {
            int rondasRestantes = RONDAS_MINIMAS_PENALTIS - rondaPenal;
            if (Math.abs(penaltisLocal - penaltisVisitante) > rondasRestantes) {
                ganadorPenaltis = (penaltisLocal > penaltisVisitante) ? local : visitante;
            }
        }
        return log;
    }

    /** @return true si la tanda de penaltis sigue en curso. */
    public boolean isEnTanda() { return ganadorPenaltis == null; }

    // --- BLOQUE: ACCESORES DE RESULTADO ---
    public int     getGolesLocal()        { return golesLocal; }
    public int     getGolesVisitante()    { return golesVisitante; }
    public Equipo  getLocal()             { return local; }
    public Equipo  getVisitante()         { return visitante; }
    public int     getPenaltisLocal()     { return penaltisLocal; }
    public int     getPenaltisVisitante() { return penaltisVisitante; }
    public int     getMinutoActual()      { return minutoActual; }

    /**
     * Determina el equipo ganador del partido.
     * @return Equipo ganador, o null si hay empate (requiere penaltis).
     */
    public Equipo getGanador() {
        if (!terminado && ganadorPenaltis == null) return null;
        if (ganadorPenaltis != null) return ganadorPenaltis;
        if (golesLocal > golesVisitante) return local;
        if (golesVisitante > golesLocal) return visitante;
        return null;
    }
}
