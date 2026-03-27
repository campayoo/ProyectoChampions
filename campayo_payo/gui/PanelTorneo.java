package gui;

import model.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * PanelTorneo — muestra el cuadro de la ronda actual,
 * resultados y botones para jugar o acceder al mercado.
 *
 * USO DE Iterator: getTopGoleadores usa un Iterator sobre el TreeSet
 * para mostrar la tabla de máximos goleadores.
 */
public class PanelTorneo extends JPanel {

    private final MainFrame frame;

    private static final Color BG_DARK  = new Color(10, 14, 30);
    private static final Color BG_CARD  = new Color(20, 28, 58);
    private static final Color UCL_BLUE = new Color(0, 100, 255);
    private static final Color UCL_GOLD = new Color(255, 210, 0);
    private static final Color VERDE    = new Color(0, 200, 100);
    private static final Color ROJO     = new Color(220, 60, 60);
    private static final Color BLANCO   = Color.WHITE;
    private static final Color GRIS     = new Color(160, 175, 210);

    private JPanel panelCruces;
    private JTextArea txtGoleadores;

    // ─────────────────────────────────────────────────────────────────────
    public PanelTorneo(MainFrame frame) {
        this.frame = frame;
        setBackground(BG_DARK);
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        construirUI();
    }

    private void construirUI() {
        Torneo t = frame.getTorneo();

        // ── Cabecera ──────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_DARK);

        JLabel lblRonda = new JLabel("  ⚽ " + t.getNombreRonda(), SwingConstants.LEFT);
        lblRonda.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblRonda.setForeground(UCL_GOLD);

        JLabel lblEquipo = new JLabel("Tu equipo: " + t.getEquipoUsuario().getNombre() + "  ", SwingConstants.RIGHT);
        lblEquipo.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblEquipo.setForeground(VERDE);

        header.add(lblRonda,  BorderLayout.WEST);
        header.add(lblEquipo, BorderLayout.EAST);

        // ── Botones de acción ─────────────────────────────────────────────
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        panelBotones.setBackground(BG_DARK);

        JButton btnJugar   = boton("▶  Jugar mi partido", UCL_BLUE,  BLANCO);
        JButton btnSimular = boton("⚡ Simular ronda completa (IA)", new Color(40, 80, 40), VERDE);
        JButton btnMercado = boton("💶  Mercado de fichajes", new Color(60, 40, 10), UCL_GOLD);

        btnJugar.addActionListener(e -> jugarPartidoUsuario());
        btnSimular.addActionListener(e -> simularRondaIA());
        btnMercado.addActionListener(e -> frame.mostrarPantalla(MainFrame.PANTALLA_MERCADO));

        panelBotones.add(btnJugar);
        panelBotones.add(btnSimular);
        panelBotones.add(btnMercado);

        // ── Cruces ────────────────────────────────────────────────────────
        panelCruces = new JPanel();
        panelCruces.setBackground(BG_DARK);
        panelCruces.setLayout(new BoxLayout(panelCruces, BoxLayout.Y_AXIS));
        actualizarCruces();

        JScrollPane scrollCruces = new JScrollPane(panelCruces);
        scrollCruces.setBackground(BG_DARK);
        scrollCruces.getViewport().setBackground(BG_DARK);
        scrollCruces.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(UCL_BLUE), " Eliminatorias ",
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("SansSerif", Font.BOLD, 12), UCL_GOLD));

        // ── Tabla de goleadores ───────────────────────────────────────────
        txtGoleadores = new JTextArea();
        txtGoleadores.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtGoleadores.setBackground(BG_CARD);
        txtGoleadores.setForeground(BLANCO);
        txtGoleadores.setEditable(false);
        txtGoleadores.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        actualizarGoleadores();

        JScrollPane scrollGol = new JScrollPane(txtGoleadores);
        scrollGol.setPreferredSize(new Dimension(300, 200));
        scrollGol.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(UCL_BLUE), " ⚽ Máximos Goleadores (TreeSet) ",
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("SansSerif", Font.BOLD, 12), UCL_GOLD));

        // ── Lateral derecho ───────────────────────────────────────────────
        JPanel lateral = new JPanel(new BorderLayout(0, 10));
        lateral.setBackground(BG_DARK);
        lateral.setPreferredSize(new Dimension(310, 0));
        lateral.add(scrollGol, BorderLayout.NORTH);
        lateral.add(infoEquipoPanel(), BorderLayout.CENTER);

        // ── Ensamblado ────────────────────────────────────────────────────
        JPanel norte = new JPanel(new BorderLayout(0, 8));
        norte.setBackground(BG_DARK);
        norte.add(header,      BorderLayout.NORTH);
        norte.add(panelBotones, BorderLayout.SOUTH);

        add(norte,       BorderLayout.NORTH);
        add(scrollCruces, BorderLayout.CENTER);
        add(lateral,     BorderLayout.EAST);
    }

    // ── Cruces ────────────────────────────────────────────────────────────

    private void actualizarCruces() {
        panelCruces.removeAll();
        Torneo t = frame.getTorneo();

        for (Eliminatoria elim : t.getEliminatorias()) {
            JPanel fila = crearFilaEliminatoria(elim, t.getEquipoUsuario());
            panelCruces.add(fila);
            panelCruces.add(Box.createVerticalStrut(6));
        }
        panelCruces.revalidate();
        panelCruces.repaint();
    }

    private JPanel crearFilaEliminatoria(Eliminatoria elim, Equipo usuario) {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setBackground(BG_CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(40, 60, 100)),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        boolean involucraUsuario = elim.getEquipoA() == usuario || elim.getEquipoB() == usuario;
        Color colorBorde = involucraUsuario ? UCL_GOLD : new Color(40, 60, 100);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(colorBorde, involucraUsuario ? 2 : 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));

        // Texto del cruce
        String marcador = "";
        if (elim.getIda() != null && elim.getIda().isTerminado()) {
            int[] m1 = elim.getIda().getMarcador();
            marcador = "  Ida: " + m1[0] + "-" + m1[1];
            if (elim.getVuelta() != null && elim.getVuelta().isTerminado()) {
                int[] m2 = elim.getVuelta().getMarcador();
                marcador += "  Vuelta: " + m2[0] + "-" + m2[1];
            }
        }

        String texto = String.format("%-25s  vs  %-25s%s",
            elim.getEquipoA().getNombre(),
            elim.getEquipoB().getNombre(),
            marcador);

        JLabel lblCruce = new JLabel(texto);
        lblCruce.setFont(new Font("Monospaced", Font.BOLD, 13));
        lblCruce.setForeground(involucraUsuario ? UCL_GOLD : BLANCO);

        JLabel lblEstado = new JLabel();
        if (elim.getGanador() != null) {
            lblEstado.setText("✅ " + elim.getGanador().getNombre());
            lblEstado.setForeground(VERDE);
        } else {
            lblEstado.setText("⏳ Pendiente");
            lblEstado.setForeground(GRIS);
        }
        lblEstado.setFont(new Font("SansSerif", Font.BOLD, 12));

        panel.add(lblCruce,  BorderLayout.CENTER);
        panel.add(lblEstado, BorderLayout.EAST);
        return panel;
    }

    // ── Goleadores ────────────────────────────────────────────────────────

    private void actualizarGoleadores() {
        frame.getTorneo().refrescarGoleadores();
        // USO DE Iterator (a través de getTopGoleadores que usa Iterator sobre TreeSet)
        ArrayList<Jugador> top = frame.getTorneo().getTopGoleadores(10);
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(" %-3s %-22s %-5s %-5s\n", "#", "Jugador", "Club", "Goles"));
        sb.append(" " + "─".repeat(42) + "\n");

        Iterator<Jugador> it = top.iterator(); // ← Iterator sobre ArrayList de goleadores
        int pos = 1;
        while (it.hasNext()) {
            Jugador j = it.next();
            String club = j.getEquipo() != null ? j.getEquipo().getNombre().substring(0, Math.min(10, j.getEquipo().getNombre().length())) : "?";
            sb.append(String.format(" %-3d %-22s %-10s ⚽%d\n", pos++, j.getNombre(), club, j.getGoles()));
        }
        if (top.isEmpty()) sb.append(" Sin goles aún — ¡juega el primer partido!\n");
        txtGoleadores.setText(sb.toString());
    }

    // ── Info equipo usuario ───────────────────────────────────────────────

    private JPanel infoEquipoPanel() {
        Equipo eq = frame.getTorneo().getEquipoUsuario();
        JTextArea txt = new JTextArea();
        txt.setFont(new Font("Monospaced", Font.PLAIN, 11));
        txt.setBackground(BG_CARD);
        txt.setForeground(GRIS);
        txt.setEditable(false);
        txt.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

        StringBuilder sb = new StringBuilder();
        sb.append("Equipo:    ").append(eq.getNombre()).append("\n");
        sb.append("País:      ").append(eq.getPais()).append("\n");
        sb.append("Formación: ").append(eq.getFormacion()).append("\n");
        sb.append("Presupuesto: ").append(String.format("%.1f", eq.getPresupuesto())).append("M€\n");
        if (eq.getEntrenador() != null)
            sb.append("Mister: ").append(eq.getEntrenador().getNombre()).append("\n");
        sb.append("Plantilla: ").append(eq.getPlantilla().size()).append(" jugadores\n");
        sb.append("Goles a favor:  ").append(eq.getGolesAFavor()).append("\n");
        sb.append("Goles en contra: ").append(eq.getGolesEnContra()).append("\n");
        txt.setText(sb.toString());

        JScrollPane scroll = new JScrollPane(txt);
        scroll.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(UCL_BLUE), " Tu equipo ",
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("SansSerif", Font.BOLD, 12), UCL_GOLD));

        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_DARK);
        p.add(scroll);
        return p;
    }

    // ── Acciones ──────────────────────────────────────────────────────────

    private void jugarPartidoUsuario() {
        Equipo usr = frame.getTorneo().getEquipoUsuario();
        Eliminatoria elim = null;
        for (Eliminatoria e : frame.getTorneo().getEliminatorias()) {
            if (e.getEquipoA() == usr || e.getEquipoB() == usr) {
                elim = e; break;
            }
        }
        if (elim == null) {
            JOptionPane.showMessageDialog(this, "No tienes partido en esta ronda.");
            return;
        }
        if (elim.isCompleta()) {
            JOptionPane.showMessageDialog(this, "Tu partido ya está jugado.");
            return;
        }
        frame.setEliminatoriaActual(elim);
        frame.mostrarPantalla(MainFrame.PANTALLA_PARTIDO);
    }

    private void simularRondaIA() {
        // Verificar si el partido del usuario ya fue jugado
        Equipo usr = frame.getTorneo().getEquipoUsuario();
        for (Eliminatoria e : frame.getTorneo().getEliminatorias()) {
            if ((e.getEquipoA() == usr || e.getEquipoB() == usr) && !e.isCompleta()) {
                int op = JOptionPane.showConfirmDialog(this,
                    "Tu partido aún no ha sido jugado.\n¿Deseas que la IA lo simule también?",
                    "Simular todo", JOptionPane.YES_NO_CANCEL_OPTION);
                if (op == JOptionPane.CANCEL_OPTION) return;
                if (op == JOptionPane.NO_OPTION) { jugarPartidoUsuario(); return; }
                // Simular partido del usuario también
                e.jugarIdaAuto();
                if (e.isDoblePartido()) e.jugarVueltaAuto();
                else e.determinarGanador();
                break;
            }
        }
        frame.simularRondaIA();
        actualizarCruces();
        actualizarGoleadores();
    }
    private JButton boton(String texto, Color bg, Color fg) {
        JButton btn = new JButton(texto);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bg.brighter(), 1),
                BorderFactory.createEmptyBorder(7, 16, 7, 16)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
