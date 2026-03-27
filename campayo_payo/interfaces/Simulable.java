package interfaces;

/**
 * Interfaz Simulable — contrato para cualquier entidad que pueda
 * ser simulada (un Partido, una tanda de penaltis, etc.).
 * USO JUSTIFICADO: Permite tratar distintos tipos de simulación
 * de forma polimórfica en el motor del torneo.
 */
public interface Simulable {

    /** Ejecuta la simulación completa. */
    void simular();

    /** Devuelve el texto narrativo generado. */
    String getNarracion();

    /** True si la simulación ha concluido. */
    boolean isTerminado();

    /** [golesLocal, golesVisitante] */
    int[] getMarcador();
}
