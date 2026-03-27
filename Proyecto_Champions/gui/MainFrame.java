package gui;

import model.*;
import data.LectorDatos;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * MainFrame — ventana principal del Football Manager.
 * Coordina todos los paneles mediante un CardLayout y mantiene
 * el estado global del torneo (modelo compartido).
 */
public class MainFrame extends JFrame {

    // ── Constantes de navegación ──────────────────────────────────────────
    public static final String PANTALLA_BIENVENIDA = "BIENVENIDA";
    public static final String PANTALLA_TORNEO     = "TORNEO";
    public static final String PANTALLA_PARTIDO    = "PARTIDO";
    public static final String PANTALLA_MERCADO    = "MERCADO";
    public static final String PANTALLA_ALINEACION = "ALINEACION";

    // ── Modelo ────────────────────────────────────────────────────────────
    private Torneo          torneo;
    private MercadoFichajes mercado;
    private Partido         partidoActual;
    private Eliminatoria    eliminatoriaActual;

    // ── GUI ───────────────────────────────────────────────────────────────
    private final CardLayout    cardLayout;
    private final JPanel        contenedor;
    private       PanelTorneo  panelTorneo;
    private       PanelPartido panelPartido;
    private       PanelMercado panelMercado;

    // Barra de estado inferior
    private final JLabel lblEstado;
    private final JLabel lblPresupuesto;
    private final JLabel lblRonda;

    // ─────────────────────────────────────────────────────────────────────
    public MainFrame() {
        super("⚽ Champions League Manager");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 750);
        setMinimumSize(new Dimension(900, 650));
        setLocationRelativeTo(null);
        setBackground(new Color(15, 20, 40));

        // ── Layout principal ──────────────────────────────────────────────
        cardLayout = new CardLayout();
        contenedor = new JPanel(cardLayout);
        contenedor.setBackground(new Color(15, 20, 40));

        // ── Barra de estado inferior ──────────────────────────────────────
        JPanel barraEstado = new JPanel(new BorderLayout(10, 0));
        barraEstado.setBackground(new Color(10, 14, 30));
        barraEstado.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));

        lblEstado     = new JLabel("Bienvenido a Champions League Manager");
        lblPresupuesto = new JLabel("");
        lblRonda       = new JLabel("");
        for (JLabel l : new JLabel[]{lblEstado, lblPresupuesto, lblRonda}) {
            l.setForeground(new Color(180, 200, 255));
            l.setFont(new Font("SansSerif", Font.PLAIN, 12));
        }
        barraEstado.add(lblEstado,      BorderLayout.WEST);
        barraEstado.add(lblRonda,       BorderLayout.CENTER);
        barraEstado.add(lblPresupuesto, BorderLayout.EAST);

        // ── Ensamblado ────────────────────────────────────────────────────
        setLayout(new BorderLayout());
        add(contenedor,  BorderLayout.CENTER);
        add(barraEstado, BorderLayout.SOUTH);

        mostrarPantalla(PANTALLA_BIENVENIDA);
    }

    // ── Navegación ────────────────────────────────────────────────────────

    public void mostrarPantalla(String nombre) {
        switch (nombre) {
            case PANTALLA_BIENVENIDA -> {
                PanelBienvenida pb = new PanelBienvenida(this);
                contenedor.add(pb, PANTALLA_BIENVENIDA);
                cardLayout.show(contenedor, PANTALLA_BIENVENIDA);
            }
            case PANTALLA_TORNEO -> {
                panelTorneo = new PanelTorneo(this);
                contenedor.add(panelTorneo, PANTALLA_TORNEO);
                cardLayout.show(contenedor, PANTALLA_TORNEO);
                actualizarBarra();
            }
            case PANTALLA_PARTIDO -> {
                panelPartido = new PanelPartido(this, eliminatoriaActual);
                contenedor.add(panelPartido, PANTALLA_PARTIDO);
                cardLayout.show(contenedor, PANTALLA_PARTIDO);
            }
            case PANTALLA_MERCADO -> {
                panelMercado = new PanelMercado(this, mercado);
                contenedor.add(panelMercado, PANTALLA_MERCADO);
                cardLayout.show(contenedor, PANTALLA_MERCADO);
            }
            case PANTALLA_ALINEACION -> {
                PanelAlineacion pa = new PanelAlineacion(this, torneo.getEquipoUsuario());
                contenedor.add(pa, PANTALLA_ALINEACION);
                cardLayout.show(contenedor, PANTALLA_ALINEACION);
            }
        }
    }

    // ── Inicialización del torneo ─────────────────────────────────────────

    public void iniciarTorneo(Equipo equipoUsuario) {
        torneo = new Torneo("UEFA Champions League 2024/25");
        ArrayList<Equipo> equipos = LectorDatos.generarDatosDefecto();
        for (Equipo e : equipos) torneo.agregarEquipo(e);

        // Asegurar que el equipo elegido esté en el torneo
        boolean encontrado = false;
        for (Equipo e : torneo.getEquipos()) {
            if (e.getNombre().equals(equipoUsuario.getNombre())) {
                torneo.setEquipoUsuario(e);
                encontrado = true;
                break;
            }
        }
        if (!encontrado) {
            torneo.setEquipoUsuario(torneo.getEquipos().get(0));
        }

        mercado = new MercadoFichajes(torneo);
        torneo.generarCruces();
        mostrarPantalla(PANTALLA_TORNEO);
    }

    // ── Avance entre rondas ───────────────────────────────────────────────

    /**
     * Simula todos los partidos de la ronda actuales (menos el del usuario)
     * y avanza al siguiente turno.
     */
    public void simularRondaIA() {
        ArrayList<Equipo> clasificados = new ArrayList<>();

        for (Eliminatoria elim : torneo.getEliminatorias()) {
            Equipo usr = torneo.getEquipoUsuario();
            boolean involucraUsuario =
                elim.getEquipoA() == usr || elim.getEquipoB() == usr;

            if (!involucraUsuario) {
                elim.jugarIdaAuto();
                if (elim.isDoblePartido()) elim.jugarVueltaAuto();
                else                       elim.determinarGanador();
                
                if (elim.getGanador() == null) elim.resolverEmpateIA();
                if (elim.getGanador() != null) clasificados.add(elim.getGanador());
            }
        }
        // El partido del usuario ya fue jugado antes de llegar aquí
        for (Eliminatoria elim : torneo.getEliminatorias()) {
            Equipo usr = torneo.getEquipoUsuario();
            if (elim.getEquipoA() == usr || elim.getEquipoB() == usr) {
                if (elim.getGanador() != null && !clasificados.contains(elim.getGanador()))
                    clasificados.add(elim.getGanador());
            }
        }

        torneo.refrescarGoleadores();

        for (Equipo e : torneo.getEquipos()) {
            if (!clasificados.contains(e)) mercado.publicarPlantillaEliminada(e);
            e.recuperarEnergiaPlantilla(); // Todos recuperan energía tras los partidos
        }

        torneo.avanzarRonda(clasificados);
        mostrarPantalla(PANTALLA_TORNEO);
    }

    // ── Estado compartido ─────────────────────────────────────────────────

    public void setEliminatoriaActual(Eliminatoria e) { this.eliminatoriaActual = e; }
    public Eliminatoria getEliminatoriaActual()        { return eliminatoriaActual; }

    public Torneo          getTorneo()   { return torneo; }
    public MercadoFichajes getMercado()  { return mercado; }

    // ── Barra de estado ───────────────────────────────────────────────────

    public void setEstado(String msg) { lblEstado.setText(msg); }

    public void actualizarBarra() {
        if (torneo == null) return;
        lblRonda.setText("📅 " + torneo.getNombreRonda());
        if (torneo.getEquipoUsuario() != null) {
            lblPresupuesto.setText("💶 "
                + torneo.getEquipoUsuario().getNombre() + "  |  Presupuesto: "
                + String.format("%.1f", torneo.getEquipoUsuario().getPresupuesto()) + "M€");
        }
    }
}
