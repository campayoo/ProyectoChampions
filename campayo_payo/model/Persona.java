package model;

/**
 * Clase base abstracta Persona.
 * USO DE HERENCIA: Centraliza atributos comunes a Jugador y Entrenador,
 * evitando duplicación de código. Obliga a las subclases a definir getRol().
 */
public abstract class Persona {

    protected int    id;
    protected String nombre;
    protected int    edad;
    protected String nacionalidad;

    public Persona(int id, String nombre, int edad, String nacionalidad) {
        this.id           = id;
        this.nombre       = nombre;
        this.edad         = edad;
        this.nacionalidad = nacionalidad;
    }

    // ── Método abstracto que cada subclase implementa ──────────────────
    public abstract String getRol();

    // ── Getters ─────────────────────────────────────────────────────────
    public int    getId()           { return id; }
    public String getNombre()       { return nombre; }
    public int    getEdad()         { return edad; }
    public String getNacionalidad() { return nacionalidad; }

    @Override
    public String toString() {
        return nombre + " [" + getRol() + "]";
    }
}
