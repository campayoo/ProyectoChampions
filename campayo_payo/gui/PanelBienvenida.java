package gui;

import data.LectorDatos;
import model.Equipo;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

/**
 * PanelBienvenida — pantalla de inicio con selector de equipo.
 * El usuario elige con qué club desea competir en la Champions League.
 */
public class PanelBienvenida extends JPanel {

    private final MainFrame     frame;
    private       JList<String> listaEquipos;
    private       ArrayList<Equipo> equipos;

    // Paleta UCL
    private static final Color BG_DARK   = new Color(10, 14, 30);
    private static final Color BG_CARD   = new Color(20, 28, 58);
    private static final Color UCL_BLUE  = new Color(0, 100, 255);
    private static final Color UCL_GOLD  = new Color(255, 210, 0);
    private static final Color TXT_WHITE = Color.WHITE;
    private static final Color TXT_GRAY  = new Color(160, 175, 210);

    // ─────────────────────────────────────────────────────────────────────
    public PanelBienvenida(MainFrame frame) {
        this.frame = frame;
        setBackground(BG_DARK);
        setLayout(new BorderLayout(0, 0));
        construirUI();
    }

    private void construirUI() {
        // ── Cabecera ──────────────────────────────────────────────────────
        JPanel header = new JPanel(new GridBagLayout());
        header.setBackground(BG_DARK);
        header.setBorder(BorderFactory.createEmptyBorder(40, 20, 20, 20));

        JLabel titulo = new JLabel("UEFA Champions League Manager", SwingConstants.CENTER);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 32));
        titulo.setForeground(UCL_GOLD);

        JLabel subtitulo = new JLabel("Selecciona tu equipo para comenzar la temporada", SwingConstants.CENTER);
        subtitulo.setFont(new Font("SansSerif", Font.PLAIN, 16));
        subtitulo.setForeground(TXT_GRAY);

        JLabel estrella = new JLabel("★ ★ ★  UCL 2024/25  ★ ★ ★", SwingConstants.CENTER);
        estrella.setFont(new Font("SansSerif", Font.BOLD, 14));
        estrella.setForeground(UCL_BLUE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.insets = new Insets(0, 0, 6, 0);
        header.add(titulo, gbc);
        gbc.gridy = 1; header.add(subtitulo, gbc);
        gbc.gridy = 2; gbc.insets = new Insets(10, 0, 0, 0);
        header.add(estrella, gbc);

        // ── Centro: lista de equipos ──────────────────────────────────────
        equipos = LectorDatos.generarDatosDefecto();
        DefaultListModel<String> modelo = new DefaultListModel<>();
        for (Equipo e : equipos) {
            modelo.addElement(String.format("%-30s  ·  %s  ·  💶 %.0fM€",
                e.getNombre(), e.getPais(), e.getPresupuesto()));
        }

        listaEquipos = new JList<>(modelo);
        listaEquipos.setFont(new Font("Monospaced", Font.PLAIN, 13));
        listaEquipos.setBackground(BG_CARD);
        listaEquipos.setForeground(TXT_WHITE);
        listaEquipos.setSelectionBackground(UCL_BLUE);
        listaEquipos.setSelectionForeground(Color.WHITE);
        listaEquipos.setFixedCellHeight(30);
        listaEquipos.setSelectedIndex(0);
        listaEquipos.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

        // Doble click para seleccionar
        listaEquipos.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) iniciarJuego();
            }
        });

        JScrollPane scroll = new JScrollPane(listaEquipos);
        scroll.setBackground(BG_CARD);
        scroll.setBorder(BorderFactory.createLineBorder(UCL_BLUE, 1));
        scroll.getViewport().setBackground(BG_CARD);

        JPanel centroWrapper = new JPanel(new BorderLayout());
        centroWrapper.setBackground(BG_DARK);
        centroWrapper.setBorder(BorderFactory.createEmptyBorder(0, 100, 0, 100));

        JLabel lblElege = new JLabel("▶  Elige tu club:", SwingConstants.LEFT);
        lblElege.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblElege.setForeground(UCL_GOLD);
        lblElege.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        centroWrapper.add(lblElege, BorderLayout.NORTH);
        centroWrapper.add(scroll,   BorderLayout.CENTER);

        // ── Panel de info del equipo ──────────────────────────────────────
        JTextArea txtInfo = new JTextArea(5, 40);
        txtInfo.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtInfo.setBackground(new Color(15, 22, 50));
        txtInfo.setForeground(TXT_GRAY);
        txtInfo.setEditable(false);
        txtInfo.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        listaEquipos.addListSelectionListener(e -> {
            int idx = listaEquipos.getSelectedIndex();
            if (idx >= 0 && idx < equipos.size()) {
                Equipo eq = equipos.get(idx);
                txtInfo.setText(infoEquipo(eq));
            }
        });
        txtInfo.setText(infoEquipo(equipos.get(0)));

        JScrollPane scrollInfo = new JScrollPane(txtInfo);
        scrollInfo.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(UCL_BLUE), " ℹ Detalles del equipo ",
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("SansSerif", Font.BOLD, 12), UCL_GOLD));
        scrollInfo.setBackground(BG_DARK);

        JPanel infoWrapper = new JPanel(new BorderLayout());
        infoWrapper.setBackground(BG_DARK);
        infoWrapper.setBorder(BorderFactory.createEmptyBorder(10, 100, 0, 100));
        infoWrapper.add(scrollInfo);

        // ── Botones ───────────────────────────────────────────────────────
        JButton btnIniciar = crearBoton("⚽  INICIAR TORNEO", UCL_BLUE, Color.WHITE);
        btnIniciar.addActionListener(e -> iniciarJuego());

        JButton btnSalir = crearBoton("✕  Salir", new Color(80, 20, 20), new Color(255, 180, 180));
        btnSalir.addActionListener(e -> System.exit(0));

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panelBotones.setBackground(BG_DARK);
        panelBotones.setBorder(BorderFactory.createEmptyBorder(15, 0, 25, 0));
        panelBotones.add(btnIniciar);
        panelBotones.add(btnSalir);

        // ── Ensamblado ────────────────────────────────────────────────────
        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setBackground(BG_DARK);
        center.add(centroWrapper, BorderLayout.CENTER);
        center.add(infoWrapper,  BorderLayout.SOUTH);

        add(header,      BorderLayout.NORTH);
        add(center,      BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);
    }

    private String infoEquipo(Equipo eq) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("  %-20s  País: %-20s  Presupuesto: %.0fM€\n",
            eq.getNombre(), eq.getPais(), eq.getPresupuesto()));
        if (eq.getEntrenador() != null) {
            sb.append(String.format("  Entrenador: %-25s  Estilo: %-12s  Formación: %s\n",
                eq.getEntrenador().getNombre(), eq.getEntrenador().getEstilo(),
                eq.getFormacion()));
        }
        sb.append(String.format("  Plantilla: %d jugadores\n", eq.getPlantilla().size()));
        sb.append("  Jugadores destacados: ");
        eq.establecerMejorOnce();
        int mostrados = 0;
        for (model.Jugador j : eq.getTitulares()) {
            if (mostrados > 0) sb.append(", ");
            sb.append(j.getNombre()).append(" (").append(j.getPosicion()).append(")");
            mostrados++;
            if (mostrados >= 5) { sb.append("..."); break; }
        }
        return sb.toString();
    }

    private void iniciarJuego() {
        int idx = listaEquipos.getSelectedIndex();
        if (idx < 0) { JOptionPane.showMessageDialog(this, "Selecciona un equipo."); return; }
        Equipo elegido = equipos.get(idx);
        int confirm = JOptionPane.showConfirmDialog(this,
            "¿Iniciar el torneo con " + elegido.getNombre() + "?",
            "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) frame.iniciarTorneo(elegido);
    }

    private JButton crearBoton(String texto, Color bg, Color fg) {
        JButton btn = new JButton(texto);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        return btn;
    }
}
