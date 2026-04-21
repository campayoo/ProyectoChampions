package gui;

import model.*;
import data.LectorDatos;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Clase MainFrame: El 'Cerebro' de la Interfaz de Usuario.
 * 
 * Esta clase es la ventana raíz y orquestadora del programa. 
 * Responsabilidades:
 * - Gestionar la navegación entre pantallas (Bienvenida, Torneo, Partido, Mercado, Alineación).
 * - Centralizar el estado global del Torneo y el Mercado.
 * - Proporcionar un HUD (Heads-Up Display) inferior con información económica y cronológica del club.
 */
public class MainFrame extends JFrame {

    // --- Identificadores Literales para Navegación ---
    public static final String PANTALLA_BIENVENIDA = "BIENVENIDA";
    public static final String PANTALLA_TORNEO    = "TORNEO";
    public static final String PANTALLA_PARTIDO    = "PARTIDO";
    public static final String PANTALLA_MERCADO    = "MERCADO";
    public static final String PANTALLA_ALINEACION = "ALINEACION";

    // --- BLOQUE: ESTADO GLOBAL DEL SIMULADOR ---
    private Torneo             torneo;
    private MercadoFichajes    mercado;
    private Eliminatoria       eliminatoriaActual;

    // --- BLOQUE: COMPONENTES DE PRESENTACIÓN ---
    private final CardLayout cardLayout; // Motor de cambio de pantallas
    private final JPanel     contenedor; // Lienzo donde se apilan los paneles
    
    // HUD inferior (Barra de estado)
    private final JLabel lblEstado;
    private final JLabel lblPresupuesto;
    private final JLabel lblRonda;

    /**
     * Constructor: Configura la arquitectura del marco principal.
     */
    public MainFrame() throws IOException {
        super("⚽ UCL MANAGER PRO: Edición Elite");
        
        // Configuración de ventana (Resolución progresiva)
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 800);
        setExtendedState(JFrame.MAXIMIZED_BOTH); 
        setMinimumSize(new Dimension(1024, 720));
        setLocationRelativeTo(null);
        setBackground(new Color(0, 5, 20));

        // Iniciamos el orquestador de vistas
        cardLayout = new CardLayout();
        contenedor = new JPanel(cardLayout);
        contenedor.setOpaque(false);

        // SUB-BLOQUE: Construcción del HUD Financiero y Deportivo
        JPanel barraEstado = new JPanel(new BorderLayout(15, 0));
        barraEstado.setBackground(new Color(2, 8, 28));
        barraEstado.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0, 100, 255)));

        lblEstado      = new JLabel("🟢 SISTEMAS ONLINE");
        lblPresupuesto = new JLabel("---");
        lblRonda       = new JLabel("---");
        
        for (JLabel l : new JLabel[] { lblEstado, lblPresupuesto, lblRonda }) {
            l.setForeground(new Color(180, 210, 255));
            l.setFont(new Font("Segoe UI", Font.BOLD, 12));
            l.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        }
        
        barraEstado.add(lblEstado, BorderLayout.WEST);
        barraEstado.add(lblRonda, BorderLayout.CENTER);
        barraEstado.add(lblPresupuesto, BorderLayout.EAST);

        // Montaje final en el JFrame
        setLayout(new BorderLayout());
        add(contenedor, BorderLayout.CENTER);
        add(barraEstado, BorderLayout.SOUTH);

        // Pantalla de arranque: Bienvenida
        mostrarPantalla(PANTALLA_BIENVENIDA);
    }

    // ---------------------------------------------------------------------
    // BLOQUE: MOTOR DE NAVEGACIÓN (CARD SWITCHER)
    // ---------------------------------------------------------------------

    /**
     * Cambia dinámicamente la pantalla actual reconstruyendo los componentes si es necesario.
     */
    public void mostrarPantalla(String nombre) throws IOException {
        switch (nombre) {
            case PANTALLA_BIENVENIDA -> {
                contenedor.add(new PanelBienvenida(this), PANTALLA_BIENVENIDA);
                cardLayout.show(contenedor, PANTALLA_BIENVENIDA);
            }
            case PANTALLA_TORNEO -> {
                contenedor.add(new PanelTorneo(this), PANTALLA_TORNEO);
                cardLayout.show(contenedor, PANTALLA_TORNEO);
                actualizarBarra();
            }
            case PANTALLA_PARTIDO -> {
                contenedor.add(new PanelPartido(this, eliminatoriaActual), PANTALLA_PARTIDO);
                cardLayout.show(contenedor, PANTALLA_PARTIDO);
            }
            case PANTALLA_MERCADO -> {
                contenedor.add(new PanelMercado(this, mercado), PANTALLA_MERCADO);
                cardLayout.show(contenedor, PANTALLA_MERCADO);
            }
            case PANTALLA_ALINEACION -> {
                contenedor.add(new PanelAlineacion(this, torneo.getEquipoUsuario()), PANTALLA_ALINEACION);
                cardLayout.show(contenedor, PANTALLA_ALINEACION);
            }
        }
        revalidate(); repaint();
    }

    // ---------------------------------------------------------------------
    // BLOQUE: LÓGICA DE CONTROL DEL TORNEO
    // ---------------------------------------------------------------------

    /**
     * Inicializa la base de datos y genera el cuadro de competición.
     */
    public void iniciarTorneo(Equipo seleccionUsuario) throws IOException {
        torneo = new Torneo("UEFA CHAMPIONS LEAGUE");
        ArrayList<Equipo> equiposDB = LectorDatos.cargarEquipos();
        for (Equipo e : equiposDB) torneo.agregarEquipo(e);

        // Sincronización del equipo del usuario
        for (Equipo e : torneo.getEquipos()) {
            if (e.getNombre().equalsIgnoreCase(seleccionUsuario.getNombre())) {
                e.setUsuario(true);
                torneo.setEquipoUsuario(e);
                break;
            }
        }

        mercado = new MercadoFichajes(torneo);
        torneo.generarCruces();
        mostrarPantalla(PANTALLA_TORNEO);
    }

    /**
     * Simula los encuentros de aquellos equipos controlados por la CPU.
     */
    public void simularRondaIA() throws IOException {
        ArrayList<Equipo> clasificados = new ArrayList<>();

        for (Eliminatoria elim : torneo.getEliminatorias()) {
            Equipo usr = torneo.getEquipoUsuario();
            boolean esPartidoUsuario = elim.getEquipoA() == usr || elim.getEquipoB() == usr;

            // Procesamos automáticamente partidos que no involucren al mánager humano
            if (!esPartidoUsuario) {
                elim.jugarIdaAuto();
                if (elim.isDoblePartido()) elim.jugarVueltaAuto();
                else elim.determinarGanador();
                
                if (elim.getGanador() == null) elim.resolverEmpateIA();
                if (elim.getGanador() != null) clasificados.add(elim.getGanador());
            } else {
                // Si el usuario ya jugó y ganó, se añade a la lista de clasificados
                if (elim.getGanador() != null) clasificados.add(elim.getGanador());
            }
        }

        torneo.refrescarGoleadores();
        torneo.avanzarRonda(clasificados);
        mostrarPantalla(PANTALLA_TORNEO);
    }

    // ---------------------------------------------------------------------
    // BLOQUE: ACCESO Y ESTADO
    // ---------------------------------------------------------------------

    public void setEliminatoriaActual(Eliminatoria e) { this.eliminatoriaActual = e; }
    public Eliminatoria getEliminatoriaActual()       { return eliminatoriaActual; }
    public Torneo getTorneo()                         { return torneo; }
    public MercadoFichajes getMercado()               { return mercado; }
    
    public void setEstado(String msg)                 { lblEstado.setText("🟢 " + msg.toUpperCase()); }

    /**
     * Sincroniza el HUD inferior con los datos actuales del modelo.
     */
    public void actualizarBarra() {
        if (torneo == null) return;
        lblRonda.setText("📅 " + torneo.getNombreRonda());
        if (torneo.getEquipoUsuario() != null) {
            String cash = String.format("%.2f", torneo.getEquipoUsuario().getPresupuesto());
            lblPresupuesto.setText("💶 PRESUPUESTO: " + cash + " M€");
        }
    }
}
