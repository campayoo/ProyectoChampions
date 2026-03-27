package gui;

import model.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * PanelAlineacion — permite al usuario gestionar la formación, el estilo de juego
 * y el once inicial de forma visual (estilo FIFA).
 */
public class PanelAlineacion extends JPanel {

    private final MainFrame frame;
    private final Equipo    equipo;

    private static final Color BG_DARK  = new Color(10, 14, 30);
    private static final Color BG_CARD  = new Color(20, 28, 58);
    private static final Color UCL_BLUE = new Color(0, 100, 255);
    private static final Color UCL_GOLD = new Color(255, 210, 0);
    private static final Color VERDE    = new Color(0, 200, 100);
    private static final Color BLANCO   = Color.WHITE;
    private static final Color GRIS     = new Color(160, 175, 210);

    private JComboBox<String> cmbFormacion;
    private JComboBox<String> cmbTactica;
    private PanelCampo        panelCampo;
    private JList<Jugador>    listSuplentes;
    
    private Jugador seleccionadoBanquillo = null;

    public PanelAlineacion(MainFrame frame, Equipo equipo) {
        this.frame  = frame;
        this.equipo = equipo;
        setBackground(BG_DARK);
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        construirUI();
    }

    private void construirUI() {
        // ── Cabecera ──────────────────────────────────────────────────────
        JLabel lblTitulo = new JLabel("⚙ Estrategia y Alineación Visual", SwingConstants.LEFT);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 26));
        lblTitulo.setForeground(UCL_GOLD);
        add(lblTitulo, BorderLayout.NORTH);

        // ── Panel Central ─────────────────────────────────────────────────
        JPanel centro = new JPanel(new BorderLayout(20, 0));
        centro.setBackground(BG_DARK);

        // --- Columna Izquierda: Controles y Banquillo ---
        JPanel colIzq = new JPanel();
        colIzq.setLayout(new BoxLayout(colIzq, BoxLayout.Y_AXIS));
        colIzq.setBackground(BG_DARK);
        colIzq.setPreferredSize(new Dimension(340, 0));

        // Esquema Táctico
        JPanel pnlForm = seccionPanel(" 📋 Configuración ");
        cmbFormacion = new JComboBox<>(new String[]{"4-4-2", "4-3-3", "3-5-2", "5-3-2", "4-5-1", "3-4-3"});
        cmbFormacion.setSelectedItem(equipo.getFormacion());
        estilizarCombo(cmbFormacion);
        
        cmbTactica = new JComboBox<>(new String[]{"Equilibrada", "Tiki Taka", "Contraataque", "Autobús", "Por las bandas"});
        cmbTactica.setSelectedItem(equipo.getTactica());
        estilizarCombo(cmbTactica);

        JButton btnAuto = boton("🔄 Auto-Alinear Mejores", UCL_BLUE, BLANCO);
        btnAuto.addActionListener(e -> {
            equipo.setFormacion(cmbFormacion.getSelectedItem().toString());
            equipo.setTactica(cmbTactica.getSelectedItem().toString());
            equipo.establecerMejorOnce();
            actualizarVistas();
        });
        
        pnlForm.add(etiqueta("Formación:"));
        pnlForm.add(cmbFormacion);
        pnlForm.add(Box.createVerticalStrut(10));
        pnlForm.add(etiqueta("Estilo de Juego (Táctica):"));
        pnlForm.add(cmbTactica);
        pnlForm.add(Box.createVerticalStrut(15));
        pnlForm.add(btnAuto);

        // Banquillo
        listSuplentes = crearListaJugadores();
        listSuplentes.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seleccionadoBanquillo = listSuplentes.getSelectedValue();
                if (seleccionadoBanquillo != null) {
                    frame.setEstado("Sustituto: " + seleccionadoBanquillo.getNombre() + ". Haz click en el Titular a cambiar en el campo.");
                    panelCampo.setSeleccionado(null);
                }
            }
        });

        colIzq.add(pnlForm);
        colIzq.add(Box.createVerticalStrut(15));
        colIzq.add(scrollConTitulo(listSuplentes, " 🪑 Banquillo (Suplentes) "));

        // --- Panel Derecha: Campo FIFA ---
        panelCampo = new PanelCampo(equipo.getTitulares(), null);
        panelCampo.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                Jugador tit = panelCampo.getJugadorEn(e.getX(), e.getY());
                if (tit != null) {
                    if (seleccionadoBanquillo != null) {
                        // Realizar cambio
                        if (equipo.realizarCambio(tit, seleccionadoBanquillo)) {
                            seleccionadoBanquillo = null;
                            listSuplentes.clearSelection();
                            actualizarVistas();
                            frame.setEstado("🔄 Cambio realizado: " + tit.getNombre() + " sale del campo.");
                        } else {
                            JOptionPane.showMessageDialog(null, "No se pudo realizar el cambio.");
                        }
                    } else {
                        // Solo seleccionar visualmente
                        panelCampo.setSeleccionado(tit);
                        frame.setEstado("Titular: " + tit.getNombre() + " (" + tit.getPosicion() + ") | Med: " + tit.getMediaGeneral());
                    }
                } else {
                     panelCampo.setSeleccionado(null);
                }
            }
        });

        centro.add(colIzq, BorderLayout.WEST);
        centro.add(panelCampo, BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);

        // ── Botonera Inferior ─────────────────────────────────────────────
        JPanel sur = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        sur.setBackground(BG_DARK);

        JButton btnVolver = boton("← Volver", BG_CARD, GRIS);
        JButton btnJugar  = boton("⚽ Confirmar y Jugar", VERDE, BLANCO);
        btnJugar.setFont(new Font("SansSerif", Font.BOLD, 16));

        btnVolver.addActionListener(e -> frame.mostrarPantalla(MainFrame.PANTALLA_TORNEO));
        btnJugar.addActionListener(e -> {
            if (frame.getEliminatoriaActual() == null) {
                JOptionPane.showMessageDialog(this, "No hay ningún partido pendiente.");
                return;
            }
            equipo.setFormacion(cmbFormacion.getSelectedItem().toString());
            equipo.setTactica(cmbTactica.getSelectedItem().toString());
            frame.mostrarPantalla(MainFrame.PANTALLA_PARTIDO);
        });

        sur.add(btnVolver);
        sur.add(btnJugar);
        add(sur, BorderLayout.SOUTH);

        actualizarVistas();
    }

    private void actualizarVistas() {
        DefaultListModel<Jugador> modelSups = new DefaultListModel<>();
        for (Jugador j : equipo.getSuplentes()) modelSups.addElement(j);
        listSuplentes.setModel(modelSups);
        
        if (panelCampo != null) {
            panelCampo.setTitulares(equipo.getTitulares());
        }
    }

    private JList<Jugador> crearListaJugadores() {
        JList<Jugador> list = new JList<>();
        list.setBackground(BG_CARD);
        list.setForeground(BLANCO);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(new PlayerListRenderer());
        return list;
    }

    private class PlayerListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Jugador j) {
                int pct = (int)(100.0 * j.getEnergiaActual() / Math.max(1, j.getEnergiaMax()));
                label.setText(String.format(" [%-4s] %-18s | Med:%2d | ⚡%d%%", 
                    j.getPosicion(), j.getNombre(), j.getMediaGeneral(), pct));
                label.setFont(new Font("Monospaced", Font.PLAIN, 12));
                label.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
                if (isSelected) {
                    label.setBackground(UCL_BLUE);
                    label.setForeground(BLANCO);
                } else {
                    label.setBackground(BG_CARD);
                    label.setForeground(BLANCO);
                }
            }
            return label;
        }
    }

    private JPanel seccionPanel(String titulo) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG_CARD);
        p.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(UCL_BLUE), titulo,
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("SansSerif", Font.BOLD, 13), UCL_GOLD));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));
        return p;
    }

    private JScrollPane scrollConTitulo(Component comp, String titulo) {
        JScrollPane scroll = new JScrollPane(comp);
        scroll.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(UCL_BLUE), titulo,
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("SansSerif", Font.BOLD, 12), UCL_GOLD));
        scroll.getViewport().setBackground(BG_CARD);
        return scroll;
    }

    private JLabel etiqueta(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setForeground(GRIS);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 11));
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        return lbl;
    }

    private void estilizarCombo(JComboBox<String> cmb) {
        cmb.setBackground(BG_DARK);
        cmb.setForeground(BLANCO);
        cmb.setFont(new Font("SansSerif", Font.PLAIN, 13));
        cmb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        cmb.setAlignmentX(LEFT_ALIGNMENT);
    }

    private JButton boton(String texto, Color bg, Color fg) {
        JButton btn = new JButton(texto);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        btn.setAlignmentX(LEFT_ALIGNMENT);
        return btn;
    }
}
