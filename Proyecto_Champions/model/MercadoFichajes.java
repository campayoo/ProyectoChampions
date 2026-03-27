package model;

import interfaces.Transferible;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

/**
 * MercadoFichajes — gestiona las compras y ventas de jugadores.
 *
 * USO DE HashSet   : mercadoIds garantiza que un jugador no aparezca
 *                    dos veces en el mercado (O(1) en inserción y búsqueda).
 * USO DE instanceof: antes de transferir una Persona, se comprueba
 *                    que sea realmente un Jugador (y no un Entrenador).
 * USO DE Iterator  : para recorrer y eliminar del mercado de forma segura.
 */
public class MercadoFichajes {

    private final HashSet<Integer>   mercadoIds;   // ← HashSet: IDs en mercado
    private final ArrayList<Jugador> ofertados;    // ← ArrayList: jugadores disponibles
    private final Torneo             torneo;

    public MercadoFichajes(Torneo torneo) {
        this.torneo     = torneo;
        this.mercadoIds = new HashSet<>();          // ← HashSet
        this.ofertados  = new ArrayList<>();        // ← ArrayList
    }

    // ── Publicar / retirar ────────────────────────────────────────────────

    /**
     * Publica un jugador en el mercado.
     * USO DE instanceof: verifica que la Persona sea transferible.
     * USO DE HashSet   : add() devuelve false si ya estaba publicado.
     */
    public String publicarJugador(Persona persona) {
        // instanceof — comprobación de tipo en tiempo de ejecución
        if (!(persona instanceof Jugador)) {
            return "❌ Solo se pueden publicar jugadores en el mercado, no entrenadores.";
        }

        Jugador jugador = (Jugador) persona; // cast seguro tras instanceof

        // instanceof sobre la interfaz Transferible
        if (!(jugador instanceof Transferible)) {
            return "❌ Este jugador no es transferible.";
        }

        if (!mercadoIds.add(jugador.getId())) { // ← HashSet.add → false si duplicado
            return "⚠ " + jugador.getNombre() + " ya está en el mercado.";
        }

        jugador.setDisponible(true);
        ofertados.add(jugador);
        return "✅ " + jugador.getNombre() + " publicado en el mercado por "
                + String.format("%.1f", jugador.getValorMercado()) + "M€.";
    }

    /**
     * Retira a un jugador del mercado usando Iterator.
     * USO DE Iterator: eliminación segura durante el recorrido.
     */
    public String retirarDelMercado(Jugador jugador) {
        if (!mercadoIds.contains(jugador.getId())) {
            return "⚠ " + jugador.getNombre() + " no está en el mercado.";
        }
        mercadoIds.remove(jugador.getId());   // ← HashSet.remove
        jugador.setDisponible(false);

        // USO DE Iterator para eliminar de la ArrayList sin ConcurrentModificationException
        Iterator<Jugador> it = ofertados.iterator(); // ← Iterator
        while (it.hasNext()) {
            if (it.next().getId() == jugador.getId()) {
                it.remove();                  // ← it.remove()
                break;
            }
        }
        return "✅ " + jugador.getNombre() + " retirado del mercado.";
    }

    // ── Transferencia ─────────────────────────────────────────────────────

    /**
     * Ejecuta la transferencia de una Persona (debe ser Jugador) a un equipo destino.
     *
     * USO DE instanceof: valida el tipo antes del cast.
     *   Si la persona fuera un Entrenador, retorna error sin lanzar excepción.
     */
    public String transferir(Persona persona, Equipo destino) {

        // ① instanceof para validar el tipo — USO JUSTIFICADO
        if (!(persona instanceof Jugador)) {
            return "❌ Solo los jugadores pueden ser fichados. "
                 + persona.getNombre() + " es un " + persona.getRol() + ".";
        }

        Jugador jugador = (Jugador) persona; // cast seguro tras instanceof

        // ② Verificar que esté en el mercado
        if (!mercadoIds.contains(jugador.getId())) {
            return "❌ " + jugador.getNombre() + " no está en el mercado de fichajes.";
        }

        // ③ Verificar disponibilidad a través de la interfaz Transferible
        Transferible t = (Transferible) jugador; // instanceof Transferible ya verificado por Jugador
        if (!t.estaDisponible()) {
            return "❌ " + jugador.getNombre() + " no está disponible para ser fichado.";
        }

        // ④ Verificar presupuesto
        double precio = jugador.getValorMercado();
        if (destino.getPresupuesto() < precio) {
            return String.format("❌ Presupuesto insuficiente.\n"
                + "   Necesitas: %.1fM€ | Disponible: %.1fM€",
                precio, destino.getPresupuesto());
        }

        // ⑤ Ejecutar la transferencia
        Equipo origen = jugador.getEquipo();
        if (origen != null) {
            origen.removerJugador(jugador);
            origen.setPresupuesto(origen.getPresupuesto() + precio * 0.9); // 90% al vendedor
        }

        destino.agregarJugador(jugador);
        destino.setPresupuesto(destino.getPresupuesto() - precio);

        // ⑥ Notificar al torneo del nuevo ID (por si fuera jugador externo)
        torneo.registrarNuevoId(jugador.getId());

        retirarDelMercado(jugador);

        return String.format("✅ FICHAJE COMPLETADO\n"
            + "   %s → %s\n   Precio: %.1fM€\n   Presupuesto restante: %.1fM€",
            jugador.getNombre(), destino.getNombre(), precio, destino.getPresupuesto());
    }

    // ── Poner en venta jugadores de equipos eliminados ────────────────────

    /**
     * Publica automáticamente los jugadores de un equipo eliminado.
     * USO DE Iterator: recorre la plantilla de forma segura.
     */
    public void publicarPlantillaEliminada(Equipo equipo) {
        Iterator<Jugador> it = equipo.getPlantilla().iterator(); // ← Iterator
        while (it.hasNext()) {
            Jugador j = it.next();
            publicarJugador(j); // instanceof se verifica internamente
        }
    }

    // ── Consultas ─────────────────────────────────────────────────────────

    public ArrayList<Jugador> getOfertados()    { return ofertados; }
    public HashSet<Integer>   getMercadoIds()   { return mercadoIds; }
    public boolean estaEnMercado(int id)         { return mercadoIds.contains(id); }

    /** Filtra por posición usando Iterator. */
    public ArrayList<Jugador> filtrarPorPosicion(String posicion) {
        ArrayList<Jugador> resultado = new ArrayList<>();
        Iterator<Jugador> it = ofertados.iterator(); // ← Iterator
        while (it.hasNext()) {
            Jugador j = it.next();
            if (posicion == null || posicion.equals("TODOS") || j.getPosicion().equals(posicion)) {
                resultado.add(j);
            }
        }
        return resultado;
    }
}
