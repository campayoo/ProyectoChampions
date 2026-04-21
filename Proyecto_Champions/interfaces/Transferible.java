package interfaces;

import model.Equipo;

/**
 * Interfaz Transferible: Contrato legal para activos del mercado.
 * 
 * Define las capacidades necesarias para que una entidad (normalmente Jugadores)
 * pueda participar en el sistema de compra-venta del simulador.
 * 
 * DESIGN RATIONALE:
 * Permite que el Mercado de Fichajes opere de forma genérica sobre cualquier
 * objeto que 'sepa' gestionarse económicamente, sin depender de una clase concreta.
 */
public interface Transferible {

    /**
     * Obtiene la tasación actual del activo en millones de euros.
     */
    double getValorMercado();

    /**
     * Actualiza la valoración económica (inflación/depreciación).
     */
    void setValorMercado(double valor);

    /**
     * Recupera el club que posee los derechos federativos.
     */
    Equipo getEquipo();

    /**
     * Vincula el activo a una nueva disciplina de club.
     */
    void setEquipo(Equipo equipo);

    /**
     * Consulta el estado de disponibilidad en el listado público.
     */
    boolean estaDisponible();

    /**
     * Habilita o deshabilita la posibilidad de recibir ofertas.
     */
    void setDisponible(boolean disponible);
}
