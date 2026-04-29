package gui;

import model.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import static gui.UCLTheme.*;

/**
 * Clase PanelAlineacion: Centro de estrategia táctica del club.
 * 
 * Permite al usuario configurar el esquema de juego (4-4-2, 4-3-3, etc.), 
 * seleccionar la mentalidad táctica y gestionar el once inicial de forma visual 
 * mediante un campo interactivo.
 * 
 * MEJORAS DE ACCESIBILIDAD:
 * - Se ha implementado un JScrollPane para la columna de comandos para asegurar la visibilidad.
 * - Estilización de botones según el estándar Champions Elite.
 */
public class PanelAlineacion extends JPanel {

    // --- Referencias de Control ---
    private final MainFrame frame;
    private final Equipo    equipo;

    // --- Paleta de Colores "Champions Elite" ---
    private static final Color BG_DARK  = new Color(10, 14, 30);
    private static final Color BG_CARD  = new Color(20, 28, 58);
    private static final Color GRIS     = new Color(160, 175, 210);

    // --- Elementos de Interfaz ---
    private JComboBox<String> cmbFormacion;
    private JComboBox<String> cmbTactica;
    private PanelCampo        panelCampo;

    /**
     * Constructor: Inicializa la pizarra de estrategia.
     */
    public PanelAlineacion(MainFrame frame, Equipo equipo) {
        this.frame  = frame;
        this.equipo = equipo;
        
        setBackground(BG_DARK);
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        construirUI();
    }

    /**
     * Construcción de la Interfaz: Cabecera, Controles y el Tapete Verde.
     */
    private void construirUI() {
        // BLOQUE: Título de Sección
        JLabel lblTitulo = UCLTheme.glowLabel("⚙  ESTRATEGIA Y DISPOSICIÓN TÁCTICA", UCLTheme.UCL_GOLD, 28, true);
        add(lblTitulo, BorderLayout.NORTH);

        // BLOQUE: Cuerpo Central
        JPanel centro = new JPanel(new BorderLayout(20, 0));
        centro.setBackground(BG_DARK);

        // Sub-Bloque: Panel de Parámetros (Contenedor con Scroll)
        JPanel colIzqContent = new JPanel();
        colIzqContent.setLayout(new BoxLayout(colIzqContent, BoxLayout.Y_AXIS));
        colIzqContent.setOpaque(false);

        // Bloque: Marco de Configuración Táctica
        JPanel pnlForm = UCLTheme.glassPanel(new GridLayout(0, 1, 0, 8));
        pnlForm.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        cmbFormacion = new JComboBox<>(new String[]{"4-4-2", "4-3-3", "3-5-2", "5-3-2", "4-5-1", "3-4-3", "4-2-3-1"});
        cmbFormacion.setSelectedItem(equipo.getFormacion());
        estilizarCombo(cmbFormacion);
        
        cmbTactica = new JComboBox<>(new String[]{"Equilibrada", "Tiki Taka", "Contraataque", "Autobús", "Ofensiva Total"});
        cmbTactica.setSelectedItem(equipo.getTactica());
        estilizarCombo(cmbTactica);

        // Listeners: Sincronización en tiempo real
        cmbFormacion.addActionListener(e -> {
            if (panelCampo != null) panelCampo.setFormacion(cmbFormacion.getSelectedItem().toString());
        });
        
        pnlForm.add(etiqueta("Dibujo Táctico:"));
        pnlForm.add(cmbFormacion);
        pnlForm.add(Box.createVerticalStrut(10));
        pnlForm.add(etiqueta("Mentalidad:"));
        pnlForm.add(cmbTactica);
        
        colIzqContent.add(pnlForm);
        colIzqContent.add(Box.createVerticalStrut(20));
        
        // Bloque: Panel de Gestión Operativa (Botones)
        JPanel pnlAcciones = UCLTheme.glassPanel(new GridLayout(0, 1, 0, 12));
        pnlAcciones.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JButton btnAuto = UCLTheme.uclButton("AUTO-OPTIMIZAR ONCE", UCLTheme.UCL_BLUE);
        btnAuto.addActionListener(e -> ejecutarAutoAlineacion());

        JButton btnVolver = UCLTheme.uclButton("VOLVER AL CLUB", new Color(100, 110, 130));
        btnVolver.addActionListener(e -> volverAlTorneo());

        JButton btnJugar  = UCLTheme.uclButton("CONFIRMAR Y JUGAR", UCLTheme.VERDE);
        btnJugar.addActionListener(e -> guardarYComenzar());
        
        pnlAcciones.add(glowLabel("COMANDOS TACTICOS", UCLTheme.UCL_MAGENTA, 12, true));
        pnlAcciones.add(btnAuto);
        pnlAcciones.add(btnJugar);
        pnlAcciones.add(btnVolver);
        
        colIzqContent.add(pnlAcciones);

        // Integración de Scroll en la columna izquierda
        JScrollPane scrollIzq = new JScrollPane(colIzqContent);
        UCLTheme.styleScrollBar(scrollIzq);
        scrollIzq.setPreferredSize(new Dimension(360, 0));

        // Sub-Bloque: Representación del Terreno de Juego (Derecha)
        panelCampo = new PanelCampo(equipo, j -> {
            if (j != null) {
                String desc = String.format("👤 %s | OVR: %d | %s", 
                                            j.getNombre(), j.getOvrEnPosicion(), j.getCompatibilidad());
                frame.setEstado(desc);
            }
        });

        centro.add(scrollIzq, BorderLayout.WEST);
        centro.add(panelCampo, BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);

        actualizarVistas();
    }

    // ---------------------------------------------------------------------
    // BLOQUE: LÓGICA DE CONTROL TÁCTICO
    // ---------------------------------------------------------------------

    /**
     * Motor de Alineación Inteligente:
     * Aplica el algoritmo de optimización del club y refresca la pantalla.
     */
    private void ejecutarAutoAlineacion() {
        equipo.setFormacion(cmbFormacion.getSelectedItem().toString());
        equipo.setTactica(cmbTactica.getSelectedItem().toString());
        equipo.establecerMejorOnce(); 
        
        if (panelCampo != null) {
            panelCampo.setFormacion(cmbFormacion.getSelectedItem().toString());
            panelCampo.asignarJugadoresANodos();
        }
        actualizarVistas();
        frame.setEstado("🧠 Algoritmo: Pizarra optimizada para ganar.");
    }

    private void volverAlTorneo() {
        try { frame.mostrarPantalla(MainFrame.PANTALLA_TORNEO); } catch (Exception ignored) {}
    }

    /**
     * Persiste los cambios tácticos y avanza a la retransmisión.
     */
    private void guardarYComenzar() {
        if (frame.getEliminatoriaActual() == null) return;
        
        // Validación: Asegurar que el club tiene 11 gladiadores en el campo
        if (equipo.getTitulares().size() < 11) {
            int resp = JOptionPane.showConfirmDialog(this, 
                "⚠️ Tu equipo no tiene 11 titulares asignados.\n¿Deseas que el sistema complete la alineación automáticamente?",
                "VALIDACIÓN TÁCTICA", JOptionPane.YES_NO_OPTION);
            
            if (resp == JOptionPane.YES_OPTION) {
                ejecutarAutoAlineacion();
            } else {
                return;
            }
        }

        equipo.setFormacion(cmbFormacion.getSelectedItem().toString());
        equipo.setTactica(cmbTactica.getSelectedItem().toString());
        
        try { 
            frame.setEstado("🚀 ¡Saliendo al túnel de vestuarios!");
            frame.mostrarPantalla(MainFrame.PANTALLA_PARTIDO); 
        } catch (Exception ignored) {}
    }

    /**
     * Refresco visual de los componentes de representación.
     */
    public void actualizarVistas() {
        if (panelCampo != null) {
            panelCampo.refreshData();
            panelCampo.repaint(); 
        }
    }

    // ---------------------------------------------------------------------
    // BLOQUE: ASISTENTES DE ESTILIZACIÓN (UCL Look)
    // ---------------------------------------------------------------------

    private JPanel seccionPanel(String titulo) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG_CARD);
        p.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(UCLTheme.UCL_BLUE), titulo,
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("SansSerif", Font.BOLD, 13), UCLTheme.UCL_GOLD));
        return p;
    }

    private JLabel etiqueta(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setForeground(GRIS);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 11));
        return lbl;
    }

    private void estilizarCombo(JComboBox<String> cmb) {
        cmb.setBackground(BG_DARK);
        cmb.setForeground(Color.WHITE);
        cmb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
    }
}
