package model;

import java.io.Serializable;

/**
 * Clase Entrenador: Representa al Director Técnico del equipo.
 *
 * Modela al mánager cuya veteranía y filosofía táctica aplican
 * modificadores directos sobre el desempeño durante la simulación.
 *
 * Implementa {@link Serializable} para persistencia con ObjectOutputStream.
 */
public class Entrenador extends Persona implements Serializable {

    /** Versión de serialización para compatibilidad de ficheros. */
    private static final long serialVersionUID = 3L;

    /** Filosofía táctica (e.g., "OFENSIVO", "DEFENSIVO", "EQUILIBRADO"). */
    private String estilo;

    /** Años de carrera profesional como entrenador. */
    private int    experiencia;

    /** Dibujo táctico predilecto (e.g., "4-3-3", "3-5-2"). */
    private String formacionFavorita;

    /**
     * Constructor: Define la identidad y el CV del preparador.
     *
     * @param id                Identificador único.
     * @param nombre            Nombre completo.
     * @param edad              Edad biológica.
     * @param nacionalidad      País de origen.
     * @param estilo            Filosofía táctica.
     * @param experiencia       Años de experiencia.
     * @param formacionFavorita Esquema táctico predilecto.
     */
    public Entrenador(int id, String nombre, int edad, String nacionalidad,
                       String estilo, int experiencia, String formacionFavorita) {
        super(id, nombre, edad, nacionalidad);
        this.estilo            = estilo;
        this.experiencia       = experiencia;
        this.formacionFavorita = formacionFavorita;
    }

    /** @return Siempre "Director Técnico". */
    @Override
    public String getRol() { return "Director Técnico"; }

    /**
     * Factor de cohesión basado en veteranía. Máx 1.25 (+25%).
     * @return Multiplicador táctico (1.00 - 1.25).
     */
    public double getBonificacionTactica() {
        return Math.min(1.25, 1.0 + (experiencia * 0.01));
    }

    /**
     * Multiplicador ofensivo según el estilo del DT.
     * @return Factor de ataque (0.85 - 1.20).
     */
    public double getMultiplicadorOfensivo() {
        String est = estilo.toUpperCase();
        if (est.contains("OFENSIVO") || est.contains("ATAQUE")) return 1.20;
        if (est.contains("DEFENSIVO")) return 0.85;
        return 1.00;
    }

    /**
     * Multiplicador defensivo según el estilo del DT.
     * @return Factor de defensa (0.90 - 1.25).
     */
    public double getMultiplicadorDefensivo() {
        String est = estilo.toUpperCase();
        if (est.contains("DEFENSIVO") || est.contains("MURO")) return 1.25;
        if (est.contains("OFENSIVO")) return 0.90;
        return 1.00;
    }

    // --- Accesores ---
    public String getEstilo()            { return estilo; }
    public int    getExperiencia()       { return experiencia; }
    public String getFormacionFavorita() { return formacionFavorita; }

    @Override
    public String toString() {
        return nombre + " [" + estilo + "] - EXP: " + experiencia + "a";
    }
}
