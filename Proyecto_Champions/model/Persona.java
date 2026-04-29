package model;

import java.io.Serializable;

/**
 * Clase Abstracta Persona: Cimiento de la jerarquía de entidades del simulador.
 *
 * Establece los atributos biográficos obligatorios para cualquier individuo
 * dentro del ecosistema del simulador (Jugadores y Entrenadores).
 *
 * Implementa {@link Serializable} para permitir la persistencia de objetos
 * mediante {@link java.io.ObjectOutputStream} y {@link java.io.ObjectInputStream},
 * garantizando que el estado completo de la aplicación pueda ser guardado y
 * restaurado desde ficheros binarios (.dat).
 *
 * PATRÓN DE DISEÑO:
 * Utiliza Herencia para evitar la duplicidad de código entre Jugadores y
 * Entrenadores, centralizando la gestión de identidad y datos demográficos.
 */
public abstract class Persona implements Serializable {

    /**
     * Identificador de versión para la serialización.
     * Garantiza compatibilidad entre diferentes versiones del fichero binario.
     */
    private static final long serialVersionUID = 1L;

    // --- BLOQUE: DATOS BIOGRÁFICOS BASE ---
    /** Identificador único de la persona en la base de datos del simulador. */
    protected int    id;

    /** Nombre deportivo o civil del individuo. */
    protected String nombre;

    /** Edad en años del individuo. */
    protected int    edad;

    /** País de origen; se usa para gestión de cupos de extranjeros. */
    protected String nacionalidad;

    /**
     * Constructor Protegido: Define la estructura básica de la entidad.
     *
     * @param id            Identificador único en la base de datos.
     * @param nombre        Nombre completo o deportivo.
     * @param edad          Edad biológica en años.
     * @param nacionalidad  País de origen del individuo.
     */
    protected Persona(int id, String nombre, int edad, String nacionalidad) {
        this.id           = id;
        this.nombre       = nombre;
        this.edad         = edad;
        this.nacionalidad = nacionalidad;
    }

    /**
     * Método Abstracto: Devuelve la función específica que desempeña en el simulador.
     * Cada subclase concreta debe definir su cargo o demarcación.
     *
     * @return El cargo o demarcación (e.g., "Portero", "Director Técnico").
     */
    public abstract String getRol();

    // --- BLOQUE: ACCESO A DATOS (GETTERS) ---

    /** @return Identificador único de la persona. */
    public int    getId()           { return id; }

    /** @return Nombre deportivo o civil. */
    public String getNombre()       { return nombre; }

    /** @return Edad en años. */
    public int    getEdad()         { return edad; }

    /** @return País de origen. */
    public String getNacionalidad() { return nacionalidad; }

    /**
     * Representación textual comprimida para registros de log y depuración.
     *
     * @return Cadena con formato "Nombre [Rol]".
     */
    @Override
    public String toString() {
        return nombre + " [" + getRol() + "]";
    }
}
