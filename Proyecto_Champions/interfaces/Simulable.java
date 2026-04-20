package interfaces;

/**
 * Interfaz Simulable: El 'Motor de Eventos' abstracto.
 * 
 * Define el protocolo que debe seguir cualquier proceso que genere resultados
 * deportivos y narrativa dentro del simulador (e.g., Partidos, Penaltis).
 * 
 * RATIONALE:
 * Facilita el procesamiento polimórfico en la UI, permitiendo tratar
 * diferentes tipos de competición bajo una misma interfaz de control.
 */
public interface Simulable {

    /**
     * Lanza el proceso de cálculo de eventos y resultados.
     */
    void simular();

    /**
     * Recupera el log de sucesos generados durante el proceso.
     */
    String getNarracion();

    /**
     * Verifica si el proceso ha llegado a su conclusión lógica.
     */
    boolean isTerminado();

    /**
     * Retorna el estado actual del marcador [Local, Visitante].
     */
    int[] getMarcador();
}
