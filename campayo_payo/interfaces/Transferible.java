package interfaces;

import model.Equipo;

/**
 * Interfaz Transferible — define el contrato que deben cumplir
 * todos los objetos que pueden ser fichados en el mercado.
 * USO JUSTIFICADO: Separa la responsabilidad de la transferibilidad
 * de la jerarquía de herencia; se aplica sólo a Jugador (no a Entrenador).
 */
public interface Transferible {

    /** Valor de mercado en millones de euros. */
    double getValorMercado();

    void setValorMercado(double valor);

    /** Equipo al que pertenece actualmente. */
    Equipo getEquipo();

    void setEquipo(Equipo equipo);

    /** Indica si el objeto está disponible para ser comprado. */
    boolean estaDisponible();

    void setDisponible(boolean disponible);
}
