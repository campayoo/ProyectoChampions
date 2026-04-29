package model;

import interfaces.Transferible;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Clase MercadoFichajes: El 'Broker' de la competición.
 *
 * Centraliza la oferta y demanda de atletas profesionales.
 * - Mantiene un repositorio global de jugadores en 'Transfer List'.
 * - Valida transacciones financieras según el presupuesto de los clubes.
 * - Garantiza la integridad de datos mediante IDs únicos con {@link HashSet}.
 *
 * Implementa {@link Serializable} para persistencia con ObjectOutputStream.
 */
public class MercadoFichajes implements Serializable {

    /** Versión de serialización para compatibilidad de ficheros. */
    private static final long serialVersionUID = 8L;

    /** Conjunto de IDs para búsqueda rápida O(1) de disponibilidad. */
    private final HashSet<Integer>   mercadoIds;
    /** Lista secuencial de jugadores ofertados para la interfaz gráfica. */
    private final ArrayList<Jugador> ofertados;
    /** Referencia al torneo (ámbito de aplicación del mercado). */
    private final Torneo             torneo;

    /**
     * Constructor: Inicializa las estructuras del mercado de fichajes.
     * @param torneo Torneo al que pertenece este mercado.
     */
    public MercadoFichajes(Torneo torneo) {
        this.torneo     = torneo;
        this.mercadoIds = new HashSet<>();
        this.ofertados  = new ArrayList<>();
    }

    // ---------------------------------------------------------------------
    // BLOQUE: OPERATIVA DE VENTA (PUBLISHERS)
    // ---------------------------------------------------------------------

    /**
     * Pone a la venta un activo del club en el mercado global.
     * Valida que el objeto sea un {@link Jugador} y que implemente {@link Transferible}.
     *
     * @param persona Persona a transferir (debe ser un Jugador).
     * @return Mensaje descriptivo del resultado de la operación.
     */
    public String publicarJugador(Persona persona) {
        // Validación de polimorfismo
        if (!(persona instanceof Jugador)) {
            return "❌ Error: Solo se pueden transferir fichas profesionales de Jugadores.";
        }

        Jugador jugador = (Jugador) persona;

        // Comprobación de contrato legal (Interface Transferible)
        if (!(jugador instanceof Transferible)) {
            return "❌ Acción denegada: Jugador intransferible según contrato.";
        }

        // Control de duplicados en la bolsa usando HashSet
        if (!mercadoIds.add(jugador.getId())) {
            return "⚠️ El jugador ya forma parte del mercado de activos.";
        }

        jugador.setDisponible(true);
        ofertados.add(jugador);
        return "✅ Registrado correctamente: " + jugador.getNombre()
             + " (Tasación: " + String.format("%.1f", jugador.getValorMercado()) + "M€).";
    }

    /**
     * Retira la oferta de un jugador del mercado.
     * Usa {@link Iterator} para eliminación segura sin ConcurrentModificationException.
     *
     * @param jugador Jugador a retirar del mercado.
     * @return Mensaje descriptivo del resultado.
     */
    public String retirarDelMercado(Jugador jugador) {
        if (!mercadoIds.contains(jugador.getId())) {
            return "⚠️ Registro no encontrado: El jugador no está en venta.";
        }

        mercadoIds.remove(jugador.getId());
        jugador.setDisponible(false);

        // Eliminación segura mediante iterador
        Iterator<Jugador> it = ofertados.iterator();
        while (it.hasNext()) {
            if (it.next().getId() == jugador.getId()) {
                it.remove();
                break;
            }
        }
        return "✅ Oferta retirada para " + jugador.getNombre() + ".";
    }

    // ---------------------------------------------------------------------
    // BLOQUE: EJECUCIÓN FINANCIERA (TRADING)
    // ---------------------------------------------------------------------

    /**
     * Ejecuta una compra-venta entre clubes.
     * Deduce fondos del comprador, abona al vendedor (95% del precio)
     * y transfiere la ficha del jugador.
     *
     * @param persona Jugador a transferir.
     * @param destino Club comprador.
     * @return Mensaje descriptivo del resultado de la transacción.
     */
    public String transferir(Persona persona, Equipo destino) {
        if (!(persona instanceof Jugador)) return "❌ Objeto de tipo erróneo.";

        Jugador jugador = (Jugador) persona;

        if (!mercadoIds.contains(jugador.getId())) {
            return "❌ Transacción fallida: El jugador no está disponible legalmente.";
        }

        // Chequeo de liquidez financiera del club comprador
        double precio = jugador.getValorMercado();
        if (destino.getPresupuesto() < precio) {
            return String.format("❌ Liquidez insuficiente. El club necesita %.1f M€ adicionales.",
                                 precio - destino.getPresupuesto());
        }

        // Protocolo de Traspaso: salida del club vendedor
        Equipo origen = jugador.getEquipo();
        if (origen != null) {
            origen.removerJugador(jugador);
            // El club vendedor amortiza el 95% del capital recibido
            origen.setPresupuesto(origen.getPresupuesto() + (precio * 0.95));
        }

        // Protocolo de Traspaso: entrada al club comprador
        destino.agregarJugador(jugador);
        destino.setPresupuesto(destino.getPresupuesto() - precio);

        // Registro en el archivo maestro del torneo
        torneo.registrarNuevoId(jugador.getId());
        retirarDelMercado(jugador);

        return String.format("🤝 OPERACIÓN EXITOSA: %s firma por el %s.",
                              jugador.getNombre(), destino.getNombre());
    }

    /**
     * Proceso automático: publica toda la plantilla de un equipo eliminado.
     * @param equipo Equipo cuya plantilla se pone en subasta.
     */
    public void publicarPlantillaEliminada(Equipo equipo) {
        Iterator<Jugador> it = equipo.getPlantilla().iterator();
        while (it.hasNext()) {
            publicarJugador(it.next());
        }
    }

    // --- BLOQUE: CONSULTAS DE ESTADO ---

    /** @return Lista de jugadores actualmente ofertados en el mercado. */
    public ArrayList<Jugador> getOfertados()    { return ofertados; }

    /** @return true si el jugador con ese ID está en el mercado. */
    public boolean estaEnMercado(int id)         { return mercadoIds.contains(id); }

    /**
     * Filtra el catálogo por rol táctico (POR, DEF, MED, DEL).
     *
     * @param posicion Filtro de posición ("TODOS" para ver todos).
     * @return Lista de jugadores que coinciden con el filtro.
     */
    public ArrayList<Jugador> filtrarPorPosicion(String posicion) {
        ArrayList<Jugador> resultado = new ArrayList<>();
        for (Jugador j : ofertados) {
            if (posicion == null || posicion.equals("TODOS")) {
                resultado.add(j);
            } else {
                String catJugador = Jugador.categoriaGeneral(j.getPosicion());
                if (catJugador.equals(posicion)) {
                    resultado.add(j);
                }
            }
        }
        return resultado;
    }
}
