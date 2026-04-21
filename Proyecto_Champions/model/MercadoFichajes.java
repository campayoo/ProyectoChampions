package model;

import interfaces.Transferible;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Clase MercadoFichajes: El 'Broker' de la Competición.
 * 
 * Centraliza la oferta y demanda de atletas profesionales. 
 * Gestión Técnica:
 * - Mantiene un repositorio global de jugadores en 'Transfer List'.
 * - Valida transacciones financieras según el presupuesto de los clubes.
 * - Garantiza la integridad de los datos mediante el uso de IDs únicos.
 */
public class MercadoFichajes {

    // --- BLOQUE: INFRAESTRUCTURA DE DATOS ---
    private final HashSet<Integer>   mercadoIds;   // Búsqueda rápida O(1) de disponibilidad
    private final ArrayList<Jugador> ofertados;    // Representación secuencial para la interfaz
    private final Torneo             torneo;       // Ámbito de aplicación del mercado

    /**
     * Constructor: Inicializa las cámaras de compensación de fichajes.
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
     * @param persona El objeto a transferir.
     * @return Log descriptivo de la operación.
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

        // Control de duplicados en la bolsa
        if (!mercadoIds.add(jugador.getId())) {
            return "⚠️ El jugador ya forma parte del mercado de activos.";
        }

        jugador.setDisponible(true);
        ofertados.add(jugador);
        return "✅ Registrado correctamente: " + jugador.getNombre() + " (Tasación: " + String.format("%.1f", jugador.getValorMercado()) + "M€).";
    }

    /**
     * Retira la oferta de un jugador para que deje de ser visible en el mercado.
     */
    public String retirarDelMercado(Jugador jugador) {
        if (!mercadoIds.contains(jugador.getId())) {
            return "⚠️ Registro no encontrado: El jugador no está en venta.";
        }
        
        mercadoIds.remove(jugador.getId());
        jugador.setDisponible(false);

        // Eliminación segura mediante iterador para evitar concurrencia en la lista
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
     * Deduce fondos, transfiere la ficha y actualiza el balance de ambos clubes.
     */
    public String transferir(Persona persona, Equipo destino) {
        if (!(persona instanceof Jugador)) return "❌ Objeto de tipo erróneo.";
        
        Jugador jugador = (Jugador) persona;

        if (!mercadoIds.contains(jugador.getId())) {
            return "❌ Transacción fallida: El jugador no está disponible legalmente.";
        }

        // Bloque: Chequeo de caja y liquidez
        double precio = jugador.getValorMercado();
        if (destino.getPresupuesto() < precio) {
            return String.format("❌ Liquidez insuficiente. El club necesita %.1f M€ adicionales.", 
                                 precio - destino.getPresupuesto());
        }

        // Bloque: Protocolo de Traspaso (Salida)
        Equipo origen = jugador.getEquipo();
        if (origen != null) {
            origen.removerJugador(jugador);
            // El club vendedor amortiza el 95% del capital recibido
            origen.setPresupuesto(origen.getPresupuesto() + (precio * 0.95));
        }

        // Bloque: Protocolo de Traspaso (Entrada)
        destino.agregarJugador(jugador);
        destino.setPresupuesto(destino.getPresupuesto() - precio);

        // Registro en el archivo maestro del torneo
        torneo.registrarNuevoId(jugador.getId());
        retirarDelMercado(jugador);

        return String.format("🤝 OPERACIÓN EXITOSA: %s firma por el %s.", 
                              jugador.getNombre(), destino.getNombre());
    }

    /**
     * Proceso automático: Publica toda la plantilla de un equipo disuelto en subasta.
     */
    public void publicarPlantillaEliminada(Equipo equipo) {
        Iterator<Jugador> it = equipo.getPlantilla().iterator();
        while (it.hasNext()) {
            publicarJugador(it.next());
        }
    }

    // --- Consultas de Estado de Bolsa ---
    public ArrayList<Jugador> getOfertados()    { return ofertados; }
    public boolean estaEnMercado(int id)         { return mercadoIds.contains(id); }

    /**
     * Filtra el catálogo de jugadores según su rol táctico (POR, DEF, MED, DEL).
     */
    public ArrayList<Jugador> filtrarPorPosicion(String posicion) {
        ArrayList<Jugador> resultado = new ArrayList<>();
        for (Jugador j : ofertados) {
            if (posicion == null || posicion.equals("TODOS") || j.getPosicion().equalsIgnoreCase(posicion)) {
                resultado.add(j);
            }
        }
        return resultado;
    }
}
