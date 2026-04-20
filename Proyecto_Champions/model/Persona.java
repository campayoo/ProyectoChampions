package model;

/**
 * Clase Abstracta Persona: Cimiento de la jerarquía de entidades.
 * 
 * Establece los atributos biográficos obligatorios para cualquier individuo
 * dentro del ecosistema del simulador. 
 * 
 * DESIGN PATTERN:
 * Utiliza Herencia para evitar la duplicidad de código entre Jugadores y 
 * Entrenadores, centralizando la gestión de identidad y datos demográficos.
 */
public abstract class Persona {

    // --- BLOQUE: DATOS BIOGRÁFICOS BASE ---
    protected int    id;            // Identificador de base de datos
    protected String nombre;        // Nombre deportivo o civil
    protected int    edad;          // Años de vida
    protected String nacionalidad;  // Origen para cupos de extranjeros

    /**
     * Constructor Protegido: Define la estructura básica de la entidad.
     */
    protected Persona(int id, String nombre, int edad, String nacionalidad) {
        this.id           = id;
        this.nombre       = nombre;
        this.edad         = edad;
        this.nacionalidad = nacionalidad;
    }

    /**
     * Método Abstracto: Define la función específica que desempeña en el simulador.
     * @return El cargo o demarcación (e.g., "Portero", "Mánager").
     */
    public abstract String getRol();

    // --- BLOQUE: ACCESO A DATOS (GETTERS) ---
    
    public int    getId()           { return id; }
    public String getNombre()       { return nombre; }
    public int    getEdad()         { return edad; }
    public String getNacionalidad() { return nacionalidad; }

    /**
     * Representación textual comprimida para registros de log.
     */
    @Override
    public String toString() {
        return nombre + " [" + getRol() + "]";
    }
}
