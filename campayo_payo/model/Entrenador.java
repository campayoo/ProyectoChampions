package model;

/**
 * Entrenador — subclase de Persona.
 * HERENCIA: hereda id, nombre, edad, nacionalidad de Persona.
 * No implementa Transferible: los entrenadores NO se pueden fichar
 * en este juego; el uso de instanceof en MercadoFichajes comprueba esto.
 */
public class Entrenador extends Persona {

    private String estilo;            // OFENSIVO | DEFENSIVO | EQUILIBRADO
    private int    experiencia;       // años de carrera
    private String formacionFavorita; // e.g. "4-3-3"

    public Entrenador(int id, String nombre, int edad, String nacionalidad,
                      String estilo, int experiencia, String formacionFavorita) {
        super(id, nombre, edad, nacionalidad);
        this.estilo            = estilo;
        this.experiencia       = experiencia;
        this.formacionFavorita = formacionFavorita;
    }

    // ── Herencia: implementación obligatoria ─────────────────────────────
    @Override
    public String getRol() { return "Entrenador"; }

    /**
     * Bonificación táctica que aplica el entrenador al equipo.
     * Cada año de experiencia suma un 0.8 % de rendimiento extra (máx 25 %).
     */
    public double getBonificacionTactica() {
        return Math.min(1.25, 1.0 + experiencia * 0.008);
    }

    /**
     * Multiplica el ataque o la defensa del equipo según el estilo del mister.
     */
    public double getMultiplicadorOfensivo() {
        return switch (estilo) {
            case "OFENSIVO"    -> 1.15;
            case "DEFENSIVO"   -> 0.90;
            default            -> 1.00; // EQUILIBRADO
        };
    }

    public double getMultiplicadorDefensivo() {
        return switch (estilo) {
            case "DEFENSIVO"   -> 1.15;
            case "OFENSIVO"    -> 0.90;
            default            -> 1.00;
        };
    }

    // ── Getters / Setters ─────────────────────────────────────────────────
    public String getEstilo()                           { return estilo; }
    public int    getExperiencia()                      { return experiencia; }
    public String getFormacionFavorita()                { return formacionFavorita; }
    public void   setFormacionFavorita(String f)        { this.formacionFavorita = f; }

    @Override
    public String toString() {
        return String.format("%s | %s | %d años exp. | Favorita: %s",
                nombre, estilo, experiencia, formacionFavorita);
    }
}
