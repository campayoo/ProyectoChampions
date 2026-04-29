package gui;

import model.*;
import data.LectorDatos;
import data.GestorFicheros;
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
 * - Proveer la capacidad de guardar/cargar la partida en cualquier momento.
 * - Proporcionar un HUD inferior con información económica del club.
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
    private final JPanel     contenedor; // Lienzo donde se apilan los paneles
    
    // HUD inferior (Barra de estado)
    private final JLabel lblEstado;
    private final JLabel lblPresupuesto;
    private final JLabel lblRonda;

    public MainFrame() throws IOException {
        super("⚽ UCL MANAGER PRO: Edición Elite");
        System.out.println("[DEBUG - MainFrame] Arrancando aplicación UCL MANAGER PRO...");
        
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 800);
        setExtendedState(JFrame.MAXIMIZED_BOTH); 
        setMinimumSize(new Dimension(1024, 720));
        setLocationRelativeTo(null);
        setBackground(new Color(0, 5, 20));

        // Orquestador Simplificado: Ahora OPACO para evitar fugas visuales (ghosting)
        contenedor = new JPanel(new BorderLayout());
        contenedor.setOpaque(true);
        contenedor.setBackground(new Color(0, 5, 20)); // Fondo base sólido Champions

        JPanel barraEstado = new JPanel(new BorderLayout(15, 0));
        barraEstado.setBackground(new Color(1, 8, 25));
        barraEstado.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, UCLTheme.UCL_BLUE));

        lblEstado      = new JLabel("🟢 SISTEMAS ONLINE");
        lblPresupuesto = new JLabel("---");
        lblRonda       = new JLabel("---");
        
        for (JLabel l : new JLabel[] { lblEstado, lblPresupuesto, lblRonda }) {
            l.setForeground(UCLTheme.UCL_SILVER);
            l.setFont(UCLTheme.fontTitle(13));
            l.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        }
        
        barraEstado.add(lblEstado, BorderLayout.WEST);
        barraEstado.add(lblRonda, BorderLayout.CENTER);
        barraEstado.add(lblPresupuesto, BorderLayout.EAST);

        setLayout(new BorderLayout());
        add(contenedor, BorderLayout.CENTER);
        add(barraEstado, BorderLayout.SOUTH);

        mostrarPantalla(PANTALLA_BIENVENIDA);
    }

    public void mostrarPantalla(String nombre) throws IOException {
        System.out.println("[DEBUG - MainFrame] Navegando a pantalla: " + nombre);
        contenedor.removeAll(); 
        
        JPanel nuevaPantalla = null;
        switch (nombre) {
            case PANTALLA_BIENVENIDA:
                nuevaPantalla = new PanelBienvenida(this);
                break;
            case PANTALLA_TORNEO:
                nuevaPantalla = new PanelTorneo(this);
                actualizarBarra();
                break;
            case PANTALLA_PARTIDO:
                nuevaPantalla = new PanelPartido(this, eliminatoriaActual);
                break;
            case PANTALLA_MERCADO:
                nuevaPantalla = new PanelMercado(this, mercado);
                break;
            case PANTALLA_ALINEACION:
                nuevaPantalla = new PanelAlineacion(this, torneo.getEquipoUsuario());
                break;
        }
        
        if (nuevaPantalla != null) {
            contenedor.add(nuevaPantalla, BorderLayout.CENTER);
        }
        
        contenedor.revalidate();
        contenedor.repaint();
        this.validate(); 
        this.repaint();
    }

    // ---------------------------------------------------------------------
    // BLOQUE: PERSISTENCIA Y CARGA
    // ---------------------------------------------------------------------

    /**
     * Guarda el progreso actual en un fichero binario.
     */
    public void guardarPartida() {
        if (torneo != null && mercado != null) {
            System.out.println("[DEBUG - MainFrame] Iniciando proceso de GUARDADO binario...");
            String resultado = GestorFicheros.guardarPartidaBinaria(torneo, mercado);
            System.out.println("[DEBUG - MainFrame] Resultado de guardado: " + resultado);
            setEstado("PARTIDA GUARDADA");
            JOptionPane.showMessageDialog(this, resultado, "Guardar Partida", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Carga el progreso desde el fichero binario.
     */
    public void cargarPartida() {
        System.out.println("[DEBUG - MainFrame] Iniciando proceso de CARGA binaria...");
        Object[] datos = GestorFicheros.cargarPartidaBinaria();
        if (datos != null && datos.length == 2) {
            torneo = (Torneo) datos[0];
            mercado = (MercadoFichajes) datos[1];
            System.out.println("[DEBUG - MainFrame] Carga exitosa. Torneo en fase: " + torneo.getNombreRonda());
            setEstado("PARTIDA CARGADA");
            try {
                mostrarPantalla(PANTALLA_TORNEO);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("[DEBUG - MainFrame] Error al cargar: Archivo no encontrado o datos corruptos.");
            JOptionPane.showMessageDialog(this, "No se encontró ninguna partida o el archivo está corrupto.", 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---------------------------------------------------------------------
    // BLOQUE: LÓGICA DE CONTROL DEL TORNEO
    // ---------------------------------------------------------------------

    /**
     * Inicializa la base de datos y genera el cuadro de competición desde cero.
     */
    public void iniciarTorneo(Equipo seleccionUsuario) throws IOException {
        System.out.println("[DEBUG - MainFrame] Inicializando Torneo desde cero. Seleccionado: " + seleccionUsuario.getNombre());
        torneo = new Torneo("UEFA CHAMPIONS LEAGUE");
        ArrayList<Equipo> equiposDB = LectorDatos.cargarEquipos();
        for (Equipo e : equiposDB) torneo.agregarEquipo(e);

        for (Equipo e : torneo.getEquipos()) {
            if (e.getNombre().equalsIgnoreCase(seleccionUsuario.getNombre())) {
                e.setUsuario(true);
                torneo.setEquipoUsuario(e);
                break;
            }
        }

        mercado = new MercadoFichajes(torneo);
        
        // BLOQUE: Sembrado inicial del mercado (Scouting Activo)
        for (Equipo e : torneo.getEquipos()) {
            if (e != torneo.getEquipoUsuario()) {
                ArrayList<Jugador> p = e.getPlantilla();
                // Sembrado de calidad: Ponemos a los 3 mejores disponibles
                p.sort((j1, j2) -> Integer.compare(j2.getMediaGeneral(), j1.getMediaGeneral()));
                if (p.size() >= 3) {
                    mercado.publicarJugador(p.get(0));
                    mercado.publicarJugador(p.get(1));
                    mercado.publicarJugador(p.get(2));
                }
            }
        }

        torneo.generarCruces();
        mostrarPantalla(PANTALLA_TORNEO);
    }

    /**
     * Simula los encuentros pendientes (incluyendo los del usuario si decide saltarlos).
     * @return Resumen en texto de los partidos de la ronda.
     */
    public String simularRondaIA() throws IOException {
        System.out.println("\n[DEBUG - MainFrame] ==============================================");
        System.out.println("[DEBUG - MainFrame] INICIANDO SIMULACIÓN DE FASE: " + torneo.getNombreRonda());
        ArrayList<Equipo> clasificados = new ArrayList<>();
        StringBuilder resultados = new StringBuilder("🏆 RESULTADOS DE LA RONDA:\n\n");

        for (Eliminatoria elim : torneo.getEliminatorias()) {
            // Si el partido no está completo, lo completamos auto (incluso el del usuario)
            if (!elim.isCompleta()) {
                elim.jugarIdaAuto();
                if (elim.isDoblePartido()) elim.jugarVueltaAuto();
                else elim.determinarGanador();
                
                if (elim.getGanador() == null) elim.resolverEmpateIA();
            }
            
            // Añadimos el clasificado
            if (elim.getGanador() != null) clasificados.add(elim.getGanador());
            
            // Adjuntamos el registro de este enfrentamiento al reporte
            resultados.append(elim.getResumen()).append("\n");
        }

        System.out.println("[DEBUG - MainFrame] Fase simulada. Clasificados: " + clasificados.size());
        
        torneo.refrescarGoleadores();
        torneo.avanzarRonda(clasificados);
        mostrarPantalla(PANTALLA_TORNEO);
        
        // Comprobar si hay un campeón definitivo
        Equipo campeon = torneo.getGanadorTorneo();
        if (campeon != null) {
            System.out.println("[DEBUG - MainFrame] Torneo Finalizado. Campeón detectado: " + campeon.getNombre());
            mostrarCelebracionCampeon(campeon);
        }
        
        System.out.println("[DEBUG - MainFrame] ==============================================\n");
        return resultados.toString();
    }
    
    private void mostrarCelebracionCampeon(Equipo campeon) {
        String msg = "¡ENHORABUENA AL " + campeon.getNombre().toUpperCase() + "!\n\n"
                   + "Se han coronado como los absolutos Campeones de Europa.\n"
                   + "La gloria eterna ya es suya.";
                   
        // Si fue el usuario, felicitación especial
        if (campeon == torneo.getEquipoUsuario()) {
            msg = "🏆 ¡ERES EL REY DE EUROPA! 🏆\n\n"
                + "Has llevado al " + campeon.getNombre() + " a la gloria continental.\n"
                + "¡Eres el mejor mánager del mundo!";
        }
        
        JOptionPane.showMessageDialog(this, msg, "⚽ ¡GRAN GANADOR DE LA CHAMPIONS! ⚽", JOptionPane.WARNING_MESSAGE);
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
