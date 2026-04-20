package model;

import interfaces.Simulable;
import java.util.ArrayList;
import java.util.Random;

/**
 * Clase Partido: El motor de física táctica y simulación de eventos.
 * 
 * Orquesta cada encuentro minuto a minuto, calculando probabilidades de éxito
 * ofensivo y defensivo basándose en el estado de forma, la formación y
 * la energía residual de los futbolistas.
 * 
 * RESPONSABILIDADES:
 * - Generar una narrativa coherente de sucesos (Goles, Tarjetas).
 * - Aplicar desgaste físico progresivo.
 * - Resolver empates mediante el protocolo de penaltis.
 */
public class Partido implements Simulable {

    // --- BLOQUE: ACTORES Y RECUERTO ---
    private final Equipo            local;
    private final Equipo            visitante;
    private       int               golesLocal;
    private       int               golesVisitante;
    private       boolean           terminado;

    // --- BLOQUE: MOTOR DE NARRATIVA ---
    private final StringBuilder     narracion; // Registro histórico del partido
    private final ArrayList<String> eventos;   // Cola de eventos para disparadores visuales
    private final Random            rng;       // Generador de entropía estratégica
    
    // --- BLOQUE: INCIDENCIAS Y ESTADÍSTICAS ---
    private java.util.HashMap<Jugador, Integer> tarjetasPartido = new java.util.HashMap<>();
    private java.util.HashSet<Jugador>          expulsados      = new java.util.HashSet<>();
    private Jugador mvp; // Jugador más valioso (pendiente implementación)

    // --- BLOQUE: RESOLUCIÓN POR PENALTIS ---
    private int     penaltisLocal;
    private int     penaltisVisitante;
    private Equipo  ganadorPenaltis;

    // --- BLOQUE: CONTROL TEMPORAL ---
    private int     minutoActual = 1;
    private static final int DURACION = 90; // Tiempo reglamentario oficial

    /**
     * Constructor: Inicializa las condiciones atmosféricas del encuentro.
     */
    public Partido(Equipo local, Equipo visitante) {
        this.local     = local;
        this.visitante = visitante;
        this.narracion = new StringBuilder();
        this.eventos   = new ArrayList<>();
        this.rng       = new Random();
    }

    // ---------------------------------------------------------------------
    // BLOQUE: IMPLEMENTACIÓN DE SIMULABLE (MODO CPU RÁPIDO)
    // ---------------------------------------------------------------------

    /**
     * Resuelve el partido de forma inmediata simulando saltos de 5 minutos.
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
     */
    public void iniciarSimulacion() {
        local.establecerMejorOnce();
        visitante.establecerMejorOnce();
        agregarEncabezado();
    }

    /**
     * Avanza el cronómetro 15 minutos reales de simulación táctica.
     * @return El segmento narrativo producido.
     */
    public String simularSiguienteBloque() {
        if (terminado || minutoActual > DURACION) return "";
        StringBuilder bloque = new StringBuilder();
        int limite = Math.min(minutoActual + 14, DURACION);
        
        for (int min = minutoActual; min <= limite; min += 5) {
            String s = procesarMinuto(min);
            bloque.append(s);
            narracion.append(s); // Persistencia en el log general
        }
        
        minutoActual = limite + 1;
        if (minutoActual > DURACION) agregarResultadoFinal();
        return bloque.toString();
    }

    public boolean isEnJuego() { return !terminado && minutoActual <= DURACION; }

    // ---------------------------------------------------------------------
    // BLOQUE: ALGORITMOS DE PROBABILIDAD Y TÁCTICA
    // ---------------------------------------------------------------------

    private void agregarEncabezado() {
        narracion.append("⚽ UCL MATCH: ").append(local.getNombre()).append(" vs ").append(visitante.getNombre()).append("\n");
        narracion.append("--------------------------------------------------\n");
    }

    /**
     * Procesa las variables de éxito para un intervalo de tiempo.
     */
    private String procesarMinuto(int min) {
        StringBuilder sb = new StringBuilder();
        boolean accionDestacada = false;

        // Sub-bloque: Algoritmo de Finalización (Goles)
        if (rng.nextDouble() < probGol(local, visitante)) {
            sb.append(textoGol(local, min)); accionDestacada = true;
        }
        if (rng.nextDouble() < probGol(visitante, local)) {
            sb.append(textoGol(visitante, min)); accionDestacada = true;
        }
        
        // Sub-bloque: Algoritmo de Tensión (Disciplina)
        if (rng.nextDouble() < 0.05) {
            sb.append(textoTarjeta(min)); accionDestacada = true;
        }

        // Narrativa de relleno profesional
        if (!accionDestacada && rng.nextDouble() < 0.03) {
            sb.append(String.format("🕒 %02d' — Dominio disputado en el centro del campo...\n", min));
        }

        // Aplicación de fatiga a los activos en campo
        for (Jugador j : local.getTitulares())     j.desgastar(rng.nextInt(3) + 1);
        for (Jugador j : visitante.getTitulares())  j.desgastar(rng.nextInt(3) + 1);

        return sb.toString();
    }

    /**
     * Calcula la posibilidad real de anotar según el Balance Táctico OVR.
     */
    private double probGol(Equipo atacante, Equipo defensor) {
        double atk = getPoderOfensivoActual(atacante);
        double def = getPoderDefensivoActual(defensor);

        double ratio = atk / Math.max(def, 1.0);
        double base  = 0.12; // 12% de probabilidad estándar por intervalo
        
        // Bonus mánager: EL usuario tiene un ligero plus estratégico
        if (atacante.isUsuario()) base *= 1.15; 
        
        return base * ratio;
    }

    private double getPoderOfensivoActual(Equipo eq) {
        double p = 0;
        for (Jugador j : eq.getTitulares()) {
            if (!expulsados.contains(j)) {
                String cat = Jugador.categoriaGeneral(j.getPosicion());
                // Multiplicadores por perfil ofensivo
                if ("DEL".equals(cat)) p += j.getFactorRendimiento() * j.getAtaque() * 1.5;
                else if ("MED".equals(cat)) p += j.getFactorRendimiento() * j.getAtaque() * 0.9;
            }
        }
        return p;
    }

    private double getPoderDefensivoActual(Equipo eq) {
        double p = 0;
        for (Jugador j : eq.getTitulares()) {
            if (!expulsados.contains(j)) {
                String cat = Jugador.categoriaGeneral(j.getPosicion());
                // Multiplicadores por solidez defensiva
                if ("POR".equals(cat)) p += j.getFactorRendimiento() * j.getDefensa() * 1.8;
                else if ("DEF".equals(cat)) p += j.getFactorRendimiento() * j.getDefensa() * 1.2;
            }
        }
        return p;
    }

    // ---------------------------------------------------------------------
    // BLOQUE: GESTIÓN DE EVENTOS DE CAMPO
    // ---------------------------------------------------------------------

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

    private String textoTarjeta(int min) {
        Equipo eq = rng.nextBoolean() ? local : visitante;
        ArrayList<Jugador> vivos = new ArrayList<>();
        for (Jugador j : eq.getTitulares()) if (!expulsados.contains(j)) vivos.add(j);
        
        if (vivos.isEmpty()) return "";
        Jugador j = vivos.get(rng.nextInt(vivos.size()));
        int amonestaciones = tarjetasPartido.getOrDefault(j, 0) + 1;
        tarjetasPartido.put(j, amonestaciones);
        
        if (amonestaciones >= 2) {
            expulsados.add(j);
            return String.format("🟥 [%02d'] ¡EXPULSIÓN! %s abandona el campo (%s)\n", min, j.getNombre(), eq.getNombre());
        }
        return String.format("🟨 [%02d'] Amonestación: %s (%s)\n", min, j.getNombre(), eq.getNombre());
    }

    /**
     * Selección ponderada del anotador: Los delanteros tienen más 'papeletas' de marcar.
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

    private void agregarResultadoFinal() {
        narracion.append("\n✅ FINAL DEL TIEMPO REGLAMENTARIO: ").append(golesLocal).append(" - ").append(golesVisitante).append("\n");
        local.addGolesAFavor(golesLocal); local.addGolesEnContra(golesVisitante);
        visitante.addGolesAFavor(golesVisitante); visitante.addGolesEnContra(golesLocal);
        terminado = true;
    }

    // ---------------------------------------------------------------------
    // BLOQUE: PROTOCOLO DE DESEMPATE (PENALTIS)
    // ---------------------------------------------------------------------

    public String simularSiguienteRondaPenal() {
        boolean aciertoLocal = rng.nextDouble() < 0.78; 
        boolean aciertoVisit = rng.nextDouble() < 0.78;
        
        if (aciertoLocal) penaltisLocal++;
        if (aciertoVisit) penaltisVisitante++;
        
        String log = String.format("🥅 [%s] %s | %s [%s]\n", 
                     (aciertoLocal ? "⚽" : "❌"), local.getNombre(), 
                     visitante.getNombre(), (aciertoVisit ? "⚽" : "❌"));
        
        if (penaltisLocal != penaltisVisitante) {
             ganadorPenaltis = (penaltisLocal > penaltisVisitante) ? local : visitante;
        }
        return log;
    }

    public boolean isEnTanda() { return ganadorPenaltis == null; }

    // --- ACCESORES DE RESULTADO ---
    public int     getGolesLocal()        { return golesLocal; }
    public int     getGolesVisitante()    { return golesVisitante; }
    public Equipo  getLocal()             { return local; }
    public Equipo  getVisitante()         { return visitante; }

    public int     getPenaltisLocal()     { return penaltisLocal; }
    public int     getPenaltisVisitante() { return penaltisVisitante; }
    public int     getMinutoActual()      { return minutoActual; }

    public Equipo getGanador() {
        if (!terminado && ganadorPenaltis == null) return null;
        if (ganadorPenaltis != null) return ganadorPenaltis;
        if (golesLocal > golesVisitante) return local;
        if (golesVisitante > golesLocal) return visitante;
        return null; 
    }
}
