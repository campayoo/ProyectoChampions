package gui;

import model.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import static gui.UCLTheme.*;

/**
 * Clase PanelPartido: El "Match Center" de la UEFA Champions League.
 * 
 * Este componente recrea la atmósfera de una retransmisión televisiva premium:
 * - Marcador dinámico sincronizado con el motor del partido.
 * - Narración procedural de eventos (Comentarios en vivo).
 * - Notificaciones de gol con efectos visuales.
 * - Panel táctico interactivo para realizar sustituciones y cambios de estrategia.
 */
public class PanelPartido extends JPanel {

    // --- BLOQUE: REFERENCIAS DE ESTADO ---
    private final MainFrame    frame;
    private final Eliminatoria eliminatoria;
    private       Partido      partido;
    private       boolean      esVuelta = false;
    private       Equipo       equipoUsuario;

    // --- BLOQUE: COMPONENTES DE RETRANSMISIÓN ---
    private JTextArea   txtNarracion; // Log de comentarios
    private JLabel      lblMarcador;  // Marcador central
    private JLabel      lblMinuto;    // Cronómetro
    private JLabel      lblGolNotif;  // Animación de GOL
    private JButton     btnSiguiente; // Control de tiempo
    private JButton     btnPenaltis;  // Desempate
    private JButton     btnVolver;    // Finalización

    // --- BLOQUE: GESTIÓN TÁCTICA (Banquillo) ---
    private JComboBox<String> cmbSale;
    private JComboBox<String> cmbEntra;
    private JButton     btnCambio;
    private JTextArea   txtOnce;

    /**
     * Constructor: Inicializa la señal de retransmisión del encuentro.
     */
    public PanelPartido(MainFrame frame, Eliminatoria eliminatoria) {
        this.frame        = frame;
        this.eliminatoria = eliminatoria;
        setLayout(new BorderLayout(0, 0));
        setBackground(DEEP_BLUE);
        iniciarLogicaPartido();
        construirUI();
    }

    // ---------------------------------------------------------------------
    // BLOQUE: GESTIÓN DE LA LÓGICA DEL ENCUENTRO
    // ---------------------------------------------------------------------

    /**
     * Configura el tipo de partido (Ida/Vuelta) y prepara el motor de simulación.
     */
    private void iniciarLogicaPartido() {
        Equipo usr = frame.getTorneo().getEquipoUsuario();
        
        // Determinamos si es ida, vuelta o partido único (final)
        if (eliminatoria.getIda() == null) {
            partido = eliminatoria.crearPartidoIda(); esVuelta = false;
        } else if (eliminatoria.isDoblePartido() && eliminatoria.getVuelta() == null) {
            partido = eliminatoria.crearPartidoVuelta(); esVuelta = true;
        } else {
            partido = eliminatoria.crearPartidoIda();
        }
        
        partido.iniciarSimulacion();
        equipoUsuario = (partido.getLocal().getNombre().equals(usr.getNombre()))
                ? partido.getLocal() : partido.getVisitante();
    }

    // ---------------------------------------------------------------------
    // BLOQUE: ARQUITECTURA VISUAL (BROADCAST STYLE)
    // ---------------------------------------------------------------------

    private void construirUI() {
        // 1. Cabecera: Scoreboard Digital
        JPanel hudMarcador = construirHUDMarcador();

        // 2. Centro: Feed de Comentarios y Narración
        txtNarracion = new JTextArea() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(0, 0, 0, 160));
                g.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };
        txtNarracion.setOpaque(false);
        txtNarracion.setFont(fontMono(13));
        txtNarracion.setLineWrap(true);
        txtNarracion.setWrapStyleWord(true);
        txtNarracion.setForeground(new Color(200, 230, 255));
        txtNarracion.setEditable(false);
        txtNarracion.setText(partido.getNarracion());

        JScrollPane scrollNarracion = new JScrollPane(txtNarracion);
        scrollNarracion.setOpaque(false);
        scrollNarracion.getViewport().setOpaque(false);
        styleScrollBar(scrollNarracion);

        JPanel wrapNarracion = glassPanel(new BorderLayout(0, 5));
        wrapNarracion.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        wrapNarracion.add(glowLabel("📺 COMENTARISTAS EN DIRECTO", UCL_GOLD, 10, true), BorderLayout.NORTH);
        wrapNarracion.add(scrollNarracion, BorderLayout.CENTER);

        // 3. Lateral: Gestión del Banquillo
        JPanel sidebar = construirSidebarTactica();

        // 4. Pie: Controles de Simulación
        JPanel footer = construirFooter();

        // Ensamblado Final de Capas
        JPanel content = new JPanel(new BorderLayout(15, 0));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        lblGolNotif = new JLabel("", SwingConstants.CENTER);
        lblGolNotif.setFont(fontTitle(20));
        lblGolNotif.setForeground(UCL_GOLD);

        JPanel liveArea = new JPanel(new BorderLayout(0, 10));
        liveArea.setOpaque(false);
        liveArea.add(lblGolNotif, BorderLayout.NORTH);
        liveArea.add(wrapNarracion, BorderLayout.CENTER);

        content.add(liveArea, BorderLayout.CENTER);
        content.add(sidebar, BorderLayout.EAST);

        add(hudMarcador, BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
    }

    // ---------------------------------------------------------------------
    // BLOQUE: MOTOR DE SIMULACIÓN Y CRONÓMETRO
    // ---------------------------------------------------------------------

    /**
     * Avanza el tiempo del partido y dispara los eventos tácticos.
     */
    private void avanzarSimulacion() {
        String log = partido.simularSiguienteBloque();
        
        // Efecto visual de GOL
        if (log.contains("⚽") && log.contains("GOL")) {
            lblGolNotif.setText("¡GOOOOOOOOOOL!");
            new javax.swing.Timer(3000, e -> { 
                lblGolNotif.setText(""); 
                ((javax.swing.Timer)e.getSource()).stop(); 
            }).start();
        }

        txtNarracion.append(log);
        txtNarracion.setCaretPosition(txtNarracion.getDocument().getLength());
        lblMarcador.setText(marcadorTexto());
        lblMinuto.setText("⌚ MIN " + Math.min(partido.getMinutoActual(), 90) + "'");
        
        actualizarSidebar();

        // Finalización reglamentaria
        if (!partido.isEnJuego()) finalizarEncuentro();
    }

    /**
     * Cierra el partido y actualiza el estado de la eliminatoria.
     */
    private void finalizarEncuentro() {
        btnSiguiente.setEnabled(false);
        equipoUsuario.recuperarEnergiaPlantilla(); 
        eliminatoria.determinarGanador();

        // Caso 1: Doble partido y solo hemos jugado la ida
        if (eliminatoria.isDoblePartido() && eliminatoria.getVuelta() == null) {
            btnVolver.setVisible(true);
            txtNarracion.append("\n\u2705 IDA COMPLETADA. Vuelve al cuadro para jugar la vuelta.\n");
            JOptionPane.showMessageDialog(this, "Fin del partido de Ida.\nResultado: " + marcadorTexto(), "Resultados Ida", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Caso 2: Empate global requiere tanda de penaltis
        if (eliminatoria.requierePenaltis()) {
            btnPenaltis.setVisible(true);
            txtNarracion.append("\n\u26A0\uFE0F EMPATE GLOBAL: TANDA DE PENALTIS \uD83E\uDD45\n");
            JOptionPane.showMessageDialog(this, "El global está empatado.\nNos vamos a la tanda de penaltis.", "Empate Global", JOptionPane.WARNING_MESSAGE);
        } else {
            // Caso 3: Hay ganador claro
            btnVolver.setVisible(true);
            String outcome = (eliminatoria.getGanador() != null && 
                              eliminatoria.getGanador().getNombre().equals(equipoUsuario.getNombre())) 
                             ? "CLASIFICADO" : "ELIMINADO";
            txtNarracion.append("\n\uD83C\uDFC1 FIN DE LA ELIMINATORIA: " + outcome + "\n");
            
            JOptionPane.showMessageDialog(this, 
                "Fin del partido.\nResultado: " + marcadorTexto() + "\n\nEstado en Torneo: " + outcome, 
                "Resultados", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // ---------------------------------------------------------------------
    // BLOQUE: UTILIDADES VISUALES (HUD)
    // ---------------------------------------------------------------------

    private JPanel construirHUDMarcador() {
        JPanel p = new JPanel(new BorderLayout());
        p.setPreferredSize(new Dimension(0, 85));
        p.setBackground(new Color(1, 10, 30));
        p.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, UCL_BLUE));

        lblMarcador = new JLabel(marcadorTexto(), SwingConstants.CENTER);
        lblMarcador.setFont(fontTitle(32));
        lblMarcador.setForeground(Color.WHITE);

        lblMinuto = glowLabel("⌚ MIN 0'", UCL_BLUE_LT, 18, true);
        lblMinuto.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 40));

        p.add(lblMarcador, BorderLayout.CENTER);
        p.add(lblMinuto, BorderLayout.EAST);
        return p;
    }

    private JPanel construirSidebarTactica() {
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setOpaque(false);
        side.setPreferredSize(new Dimension(280, 0));

        // Sub-bloque: Gestión de Sustituciones
        JPanel pnlCambios = glassPanel(new GridLayout(4, 1, 5, 5));
        pnlCambios.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        pnlCambios.add(glowLabel("🔄 BANQUILLO", UCL_GOLD, 10, true));
        
        cmbSale = new JComboBox<>();
        cmbEntra = new JComboBox<>();
        btnCambio = uclButton("EJECUTAR CAMBIO", UCL_BLUE);
        btnCambio.addActionListener(e -> intentarCambio());
        
        pnlCambios.add(cmbSale);
        pnlCambios.add(cmbEntra);
        pnlCambios.add(btnCambio);

        // Sub-bloque: Monitor de Estamina
        txtOnce = new JTextArea();
        txtOnce.setOpaque(false);
        txtOnce.setFont(fontMono(11));
        txtOnce.setEditable(false);
        txtOnce.setForeground(Color.WHITE);
        
        JScrollPane scrollOnce = new JScrollPane(txtOnce);
        scrollOnce.setOpaque(false);
        scrollOnce.getViewport().setOpaque(false);
        styleScrollBar(scrollOnce);

        JPanel pnlOnce = glassPanel(new BorderLayout());
        pnlOnce.add(glowLabel("📋 RENDIMIENTO EN VIVO", UCL_BLUE_LT, 10, true), BorderLayout.NORTH);
        pnlOnce.add(scrollOnce, BorderLayout.CENTER);

        side.add(pnlCambios); side.add(Box.createVerticalStrut(15));
        side.add(pnlOnce);

        actualizarSidebar();
        return side;
    }

    private void intentarCambio() {
        int s = cmbSale.getSelectedIndex(), e = cmbEntra.getSelectedIndex();
        if (s < 0 || e < 0) return;
        
        Jugador sale = equipoUsuario.getTitulares().get(s);
        Jugador entra = equipoUsuario.getSuplentes().get(e);
        
        if (equipoUsuario.realizarCambio(sale, entra)) {
            txtNarracion.append("\n📢 TÁCTICA: " + entra.getNombre() + " entra por " + sale.getNombre() + ".\n");
            actualizarSidebar();
        }
    }

    private void actualizarSidebar() {
        cmbSale.removeAllItems();
        cmbEntra.removeAllItems();
        for (Jugador j : equipoUsuario.getTitulares()) cmbSale.addItem("↓ " + j.getNombre() + " (" + (int)j.getEnergiaActual() + "%)");
        for (Jugador j : equipoUsuario.getSuplentes()) cmbEntra.addItem("↑ " + j.getNombre());
        
        StringBuilder sb = new StringBuilder();
        for (Jugador j : equipoUsuario.getTitulares()) {
            sb.append(String.format("[%s] %-15s %d%%\n", j.getPosicion(), j.getNombre(), (int)j.getEnergiaActual()));
        }
        txtOnce.setText(sb.toString());
    }

    private JPanel construirFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 20));
        footer.setOpaque(false);

        btnSiguiente = uclButton("⏩ SIGUIENTE BLOQUE", UCL_BLUE);
        btnPenaltis  = uclButton("🥅 TANDA DE PENALTIS", NARANJA);
        btnVolver    = uclButton("↩ VOLVER AL CUADRO", VERDE);

        btnPenaltis.setVisible(false);
        btnVolver.setVisible(false);

        btnSiguiente.addActionListener(e -> avanzarSimulacion());
        btnPenaltis.addActionListener(e -> avanzarPenaltis());
        btnVolver.addActionListener(e -> { 
            try { frame.mostrarPantalla(MainFrame.PANTALLA_TORNEO); } catch (IOException ex) {} 
        });

        footer.add(btnSiguiente);
        footer.add(btnPenaltis);
        footer.add(btnVolver);
        return footer;
    }

    private void avanzarPenaltis() {
        String log = partido.simularSiguienteRondaPenal();
        txtNarracion.append(log);
        lblMarcador.setText(String.format("%d(%d) - %d(%d)", 
            partido.getGolesLocal(), partido.getPenaltisLocal(), 
            partido.getGolesVisitante(), partido.getPenaltisVisitante()));
        
        if (!partido.isEnTanda()) {
            // Asignar el ganador de penaltis a la eliminatoria
            Equipo ganadorPenaltis = partido.getGanador();
            if (ganadorPenaltis != null) {
                eliminatoria.setGanador(ganadorPenaltis);
            }
            
            btnPenaltis.setEnabled(false);
            btnVolver.setVisible(true);
            
            String outcome = (ganadorPenaltis != null && 
                              ganadorPenaltis.getNombre().equals(equipoUsuario.getNombre())) 
                             ? "CLASIFICADO" : "ELIMINADO";
            txtNarracion.append("\n\uD83C\uDFC6 RESULTADO PENALTIS: " + outcome + "\n");
            
            JOptionPane.showMessageDialog(this, 
                "Fin de la tanda de penaltis.\nResultado: " + lblMarcador.getText() + "\n\nEstado en Torneo: " + outcome, 
                "Resultados Penaltis", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private String marcadorTexto() {
        return String.format("%s %d - %d %s", 
            partido.getLocal().getNombre(), partido.getGolesLocal(), 
            partido.getGolesVisitante(), partido.getVisitante().getNombre());
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(new GradientPaint(0, 0, DEEP_BLUE, 0, getHeight(), ROYAL_BLUE));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
    }
}
