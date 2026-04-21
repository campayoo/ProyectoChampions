package model;

/**
 * Clase Entrenador: Representa la identidad del estratega en el banquillo.
 * 
 * Modela al Mánager del equipo, cuya veteranía y filosofía táctica aplican
 * modificadores directos sobre el desempeño de los jugadores durante la simulación.
 * 
 * INFLUENCIA TÉCNICA:
 * - Bonificación por Experiencia (Cohesión de grupo).
 * - Multiplicadores de Estilo (Especialización Ofensiva/Defensiva).
 */
public class Entrenador extends Persona {

    // --- BLOQUE: PERFIL TÉCNICO ---
    private String estilo;            // Filosofía (e.g., "Tiki-Taka", "Catenaccio")
    private int    experiencia;       // Años de carrera profesional
    private String formacionFavorita; // Dibujo táctico predilecto (e.g., "4-3-3")

    /**
     * Constructor: Define la identidad y el CV del nuevo preparador.
     */
    public Entrenador(int id, String nombre, int edad, String nacionalidad,
                       String estilo, int experiencia, String formacionFavorita) {
        super(id, nombre, edad, nacionalidad);
        this.estilo            = estilo;
        this.experiencia       = experiencia;
        this.formacionFavorita = formacionFavorita;
    }

    @Override
    public String getRol() { return "Director Técnico"; }

    // ---------------------------------------------------------------------
    // BLOQUE: ALGORITMOS DE BONIFICACIÓN ESTRATÉGICA
    // ---------------------------------------------------------------------

    /**
     * Calcula el factor de 'oficio' basado en la veteranía del técnico.
     * @return Multiplicador de cohesión (Max 1.25).
     */
    public double getBonificacionTactica() {
        // La experiencia mejora el rendimiento colectivo en un 1% anual
        return Math.min(1.25, 1.0 + (experiencia * 0.01));
    }

    /**
     * Determina el peso extra en la zona de ataque según la filosofía del DT.
     */
    public double getMultiplicadorOfensivo() {
        String est = estilo.toUpperCase();
        if (est.contains("OFENSIVO") || est.contains("ATAQUE")) return 1.20;
        if (est.contains("DEFENSIVO")) return 0.85;
        return 1.00; // Perfil equilibrado
    }

    /**
     * Determina el refuerzo en la muralla defensiva según la filosofía del DT.
     */
    public double getMultiplicadorDefensivo() {
        String est = estilo.toUpperCase();
        if (est.contains("DEFENSIVO") || est.contains("MURO")) return 1.25;
        if (est.contains("OFENSIVO")) return 0.90;
        return 1.00; // Perfil equilibrado
    }

    // --- ACCESORES TÉCNICOS ---
    public String getEstilo()            { return estilo; }
    public String getFormacionFavorita() { return formacionFavorita; }

    /**
     * Ficha técnica reducida para listados del staff.
     */
    @Override
    public String toString() {
        return nombre + " [" + estilo + "] - EXP: " + experiencia + "a";
    }
}
