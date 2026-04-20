package gui;

import model.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;

import static gui.UCLTheme.*;

/**
 * Clase PanelTorneo: Centro de control y tablero de la competición.
 * 
 * Este panel es el 'hub' principal del usuario donde puede:
 * - Supervisar el cuadro de eliminatorias y resultados.
 * - Consultar el ranking de 'Pichichi' (Goleadores) mediante scroll.
 * - Gestionar el estado deportivo y financiero de su club (HUD lateral).
 * - Navegar a las secciones de táctica, mercado y simulación.
 * 
 * REQUERIMIENTOS ESPECIALES:
 * - Solo la tabla de goleadores es scrolleable para mantener el foco en los marcadores fijos.
 */
public class PanelTorneo extends JPanel {

    // --- BLOQUE: ELEMENTOS DE CONTROL ---
    private final MainFrame frame;
    private JPanel panelCruces;          // Lista visual de eliminatorias (Fija)
    private JPanel panelListaGoleadores; // Contenedor del ranking (Scrolleable)
    private JPanel hudEquipo;            // Panel lateral de información del usuario

    /**
     * Constructor: Configura el entorno visual del torneo.
     */
    public PanelTorneo(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout(0, 0));
        setBackground(DEEP_BLUE);
        construirUI();
    }

    // ---------------------------------------------------------------------
    // BLOQUE: ARQUITECTURA VISUAL (LAYOUTS)
    // ---------------------------------------------------------------------

    private void construirUI() {
        Torneo t = frame.getTorneo();

        // Sub-bloque: Cabecera con Tipografía Champions
        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));

        JPanel izqHeader = new JPanel();
        izqHeader.setLayout(new BoxLayout(izqHeader, BoxLayout.Y_AXIS));
        izqHeader.setOpaque(false);
        JLabel lblRondaSmall = new JLabel("⭐ CHAMPIONS LEAGUE - EDICIÓN ELITE");
        lblRondaSmall.setFont(fontBody(10));
        lblRondaSmall.setForeground(new Color(150, 180, 230));
        JLabel lblRonda = glowLabel("🏆 " + t.getNombreRonda().toUpperCase(), UCL_GOLD, 24, true);
        izqHeader.add(lblRondaSmall);
        izqHeader.add(lblRonda);
        header.add(izqHeader, BorderLayout.WEST);

        // Sub-bloque: Cuerpo Central con Columnas (GridBagLayout para control total)
        JPanel centroColumnas = new JPanel(new GridBagLayout());
        centroColumnas.setOpaque(false);
        centroColumnas.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 5, 0, 5);

        // 1. HUD LATERAL (ESTADO CLUB) - Izquierda
        hudEquipo = glassPanel(new BorderLayout(0, 15));
        hudEquipo.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.25; gbc.weighty = 1.0;
        centroColumnas.add(hudEquipo, gbc);

        // CONTENEDOR DERECHO: Cuadros y Clasificación - Derecha
        JPanel panelInfoDerecha = new JPanel(new GridBagLayout());
        panelInfoDerecha.setOpaque(false);
        GridBagConstraints gbcR = new GridBagConstraints();
        gbcR.fill = GridBagConstraints.BOTH;
        gbcR.gridx = 0; gbcR.weightx = 1.0;

        // 2. ELIMINATORIAS (CUADRO FIJO)
        panelCruces = new JPanel();
        panelCruces.setOpaque(false);
        panelCruces.setLayout(new BoxLayout(panelCruces, BoxLayout.Y_AXIS));

        JPanel wrapCruces = glassPanel(new BorderLayout(0, 10));
        wrapCruces.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        wrapCruces.add(glowLabel("⚽ CUADRO DE ELIMINATORIAS", UCL_BLUE_LT, 12, true), BorderLayout.NORTH);
        wrapCruces.add(panelCruces, BorderLayout.CENTER);
        
        gbcR.gridy = 0; gbcR.weighty = 0.4; // Menor peso inicial
        panelInfoDerecha.add(wrapCruces, gbcR);

        // 3. GOLEADORES (ZONA CON SCROLL)
        panelListaGoleadores = new JPanel();
        panelListaGoleadores.setLayout(new BoxLayout(panelListaGoleadores, BoxLayout.Y_AXIS));
        panelListaGoleadores.setOpaque(false);

        JScrollPane scrollGoleadores = new JScrollPane(panelListaGoleadores);
        scrollGoleadores.getViewport().setOpaque(false);
        scrollGoleadores.setOpaque(false);
        scrollGoleadores.setBorder(null);
        scrollGoleadores.getVerticalScrollBar().setUnitIncrement(16);

        JPanel wrapGoleadores = glassPanel(new BorderLayout(0, 10));
        wrapGoleadores.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        wrapGoleadores.add(glowLabel("🔥 MÁXIMOS GOLEADORES", UCL_GOLD, 12, true), BorderLayout.NORTH);
        wrapGoleadores.add(scrollGoleadores, BorderLayout.CENTER);

        gbcR.gridy = 1; gbcR.weighty = 1.0; gbcR.insets = new Insets(15, 0, 0, 0);
        panelInfoDerecha.add(wrapGoleadores, gbcR);

        gbc.gridx = 1; gbc.weightx = 0.75;
        centroColumnas.add(panelInfoDerecha, gbc);

        // Ensamblado Integral
        add(header, BorderLayout.NORTH);
        add(centroColumnas, BorderLayout.CENTER);
        add(botonesAccion(), BorderLayout.SOUTH);

        actualizarDatos();
    }

    // ---------------------------------------------------------------------
    // BLOQUE: MOTOR DE ACTUALIZACIÓN (DATA BINDING)
    // ---------------------------------------------------------------------

    /**
     * Refresca todos los componentes visuales con los datos actuales del torneo.
     */
    public void actualizarDatos() {
        if (frame.getTorneo() == null) return;
        
        // Bloque: Cuadro de Partidos
        panelCruces.removeAll();
        for (Eliminatoria e : frame.getTorneo().getEliminatorias()) {
            panelCruces.add(crearFilaPartido(e));
            panelCruces.add(Box.createVerticalStrut(5));
        }
        panelCruces.revalidate();
        panelCruces.repaint();

        // Bloque: Ránking Scrolleable
        panelListaGoleadores.removeAll();
        ArrayList<Jugador> top = frame.getTorneo().getTopGoleadores(30);
        int pos = 1;
        for (Jugador j : top) {
            panelListaGoleadores.add(crearFilaGoleador(j, pos++));
        }
        panelListaGoleadores.revalidate();
        panelListaGoleadores.repaint();

        // Bloque: Perfil del Club
        actualizarHUD();
    }

    private void actualizarHUD() {
        if (hudEquipo == null) return;
        hudEquipo.removeAll();
        
        Equipo eq = frame.getTorneo().getEquipoUsuario();
        JLabel ti = glowLabel("🛡 PERFIL DE MÁNAGER", UCL_BLUE_LT, 14, true);
        
        JTextPane tp = new JTextPane();
        tp.setContentType("text/html");
        tp.setEditable(false);
        tp.setOpaque(false);
        
        String html = String.format("<html><body style='font-family:Segoe UI; color:#B4D2FF; margin:10px;'>"
            + "<h2 style='color:#FFD700; margin:0;'>%s</h2>"
            + "<p style='margin:2px 0;'>🌍 Liga: %s</p>"
            + "<p style='margin:2px 0;'>👥 Plantilla: %d jugadores</p>"
            + "<hr style='border:0; border-top:1px solid #336699;'>"
            + "<h4 style='color:#00AAFF; margin:8px 0 2px 0;'>ESTADÍSTICAS</h4>"
            + "⚽ Favor: <span style='color:#00FF00;'>%d</span><br>"
            + "🛡 Contra: <span style='color:#FF4444;'>%d</span><br>"
            + "<hr style='border:0; border-top:1px solid #336699;'>"
            + "<h2 style='color:#FFF; margin-top:10px;'>%.2f M€</h2>"
            + "<small style='color:#888;'>PRESUPUESTO TRANSFER</small>"
            + "</body></html>", 
            eq.getNombre(), eq.getPais(), eq.getPlantilla().size(), 
            eq.getGolesAFavor(), eq.getGolesEnContra(), eq.getPresupuesto());
            
        tp.setText(html);

        hudEquipo.add(ti, BorderLayout.NORTH);
        hudEquipo.add(tp, BorderLayout.CENTER);
        hudEquipo.revalidate();
    }

    // ---------------------------------------------------------------------
    // BLOQUE: COMPONENTES DE RENDERIZADO FILA A FILA
    // ---------------------------------------------------------------------

    private JPanel crearFilaPartido(Eliminatoria elim) {
        JPanel p = glassPanel(new BorderLayout());
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        p.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

        JPanel nombres = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        nombres.setOpaque(false);
        
        JLabel eqA = new JLabel(elim.getEquipoA().getNombre().toUpperCase());
        eqA.setForeground(Color.WHITE);
        eqA.setFont(fontTitle(12));
        
        JLabel vs = new JLabel("VS");
        vs.setForeground(UCL_GOLD);
        vs.setFont(fontBody(10));
        
        JLabel eqB = new JLabel(elim.getEquipoB().getNombre().toUpperCase());
        eqB.setForeground(Color.WHITE);
        eqB.setFont(fontTitle(12));

        nombres.add(eqA); nombres.add(vs); nombres.add(eqB);

        boolean completado = elim.isCompleta();
        JLabel lblEstado = new JLabel(completado ? "✓ FINALIZADO" : "• EN ESPERA");
        lblEstado.setForeground(completado ? VERDE : GRIS_CLARO);
        lblEstado.setFont(fontMono(10));

        p.add(nombres, BorderLayout.CENTER);
        p.add(lblEstado, BorderLayout.EAST);
        return p;
    }

    private JPanel crearFilaGoleador(Jugador j, int pos) {
        JPanel p = new JPanel(new BorderLayout(15, 0));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        p.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

        JLabel infoPos = new JLabel(String.format("%02d", pos));
        infoPos.setForeground(pos <= 3 ? UCL_GOLD : new Color(100, 120, 160));
        infoPos.setFont(fontMono(13));
        infoPos.setPreferredSize(new Dimension(30, 0));

        JLabel infoNom = new JLabel(j.getNombre() + " (" + j.getEquipo().getNombre() + ")");
        infoNom.setForeground(Color.WHITE);
        infoNom.setFont(fontBody(13));

        JLabel goles = new JLabel(j.getGoles() + " GOLES");
        goles.setForeground(UCL_GOLD);
        goles.setFont(fontTitle(12));

        p.add(infoPos, BorderLayout.WEST);
        p.add(infoNom, BorderLayout.CENTER);
        p.add(goles, BorderLayout.EAST);
        return p;
    }

    // ---------------------------------------------------------------------
    // BLOQUE: BARRA DE NAVEGACIÓN Y COMANDOS
    // ---------------------------------------------------------------------

    /**
     * Barra inferior con los accesos directos a las funcionalidades del juego.
     */
    private JPanel botonesAccion() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 25));
        p.setOpaque(false);

        JButton bJ = uclButton("▶ JUGAR PARTIDO", UCL_BLUE);
        JButton bS = uclButton("⚡ SIMULAR FASE", VERDE);
        JButton bA = uclButton("📋 ESTRATEGIA", UCL_GOLD);
        JButton bM = uclButton("💶 MERCADO", NARANJA);

        bJ.addActionListener(e -> { try { jugarPrimerPartidoPendiente(); } catch (Exception ex) {} });
        bS.addActionListener(e -> {
            try {
                frame.simularRondaIA();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            actualizarDatos(); });
        bA.addActionListener(e -> {
            try {
                frame.mostrarPantalla(MainFrame.PANTALLA_ALINEACION);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        bM.addActionListener(e -> {
            try {
                frame.mostrarPantalla(MainFrame.PANTALLA_MERCADO);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        p.add(bJ); p.add(bS); p.add(bA); p.add(bM);
        return p;
    }

    private void jugarPrimerPartidoPendiente() throws IOException {
        Torneo t = frame.getTorneo();
        for (Eliminatoria e : t.getEliminatorias()) {
            if (e.esMiPartido(t.getEquipoUsuario()) && !e.isCompleta()) {
                frame.setEliminatoriaActual(e);
                frame.mostrarPantalla(MainFrame.PANTALLA_ALINEACION);
                return;
            }
        }
        JOptionPane.showMessageDialog(this, "No hay partidos pendientes para tu equipo.");
    }
}
