package gui;

import model.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * PanelPartido — permite al usuario jugar un partido de forma interactiva.
 *
 * El partido se divide en bloques de ~15 minutos (simularSiguienteBloque()).
 * Entre bloques el usuario puede realizar sustituciones.
 *
 * USO DE Iterator: el combo de titulares se recorre con Iterator para
 * listar a los jugadores que pueden salir del campo.
 */
public class PanelPartido extends JPanel {

    private final MainFrame     frame;
    private final Eliminatoria  eliminatoria;
    private       Partido       partido;
    private       boolean       esVuelta = false;

    // ── Paleta de colores (igual que PanelTorneo) ────────────────────────
    private static final Color BG_DARK  = new Color(10, 14, 30);
    private static final Color BG_CARD  = new Color(20, 28, 58);
    private static final Color UCL_BLUE = new Color(0, 100, 255);
    private static final Color UCL_GOLD = new Color(255, 210, 0);
    private static final Color VERDE    = new Color(0, 200, 100);
    private static final Color ROJO     = new Color(220, 60, 60);
    private static final Color BLANCO   = Color.WHITE;
    private static final Color GRIS     = new Color(160, 175, 210);

    // ── Componentes de UI ────────────────────────────────────────────────
    private JTextArea   txtNarracion;
    private JLabel      lblMarcador;
    private JLabel      lblBloque;
    private JButton     btnSiguiente;
    private JButton     btnPenaltis;
    private JButton     btnVolver;
    private JComboBox<String> cmbSale;
    private JComboBox<String> cmbEntra;
    private JButton     btnCambio;
    private JPanel      panelBotones;
    private JButton     btnCerrar;

    // Referencias para los cambios
    private Equipo equipoUsuario;
    private ArrayList<Jugador> titularesRef;
    private ArrayList<Jugador> suplentesRef;

    // ─────────────────────────────────────────────────────────────────────
    public PanelPartido(MainFrame frame, Eliminatoria eliminatoria) {
        this.frame        = frame;
        this.eliminatoria = eliminatoria;
        setBackground(BG_DARK);
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        iniciarPartido();
        construirUI();
    }

    // ── Inicio del partido ───────────────────────────────────────────────

    private void iniciarPartido() {
        Equipo usr = frame.getTorneo().getEquipoUsuario();

        // Determinar si jugamos ida o vuelta
        if (eliminatoria.getIda() == null) {
            partido = eliminatoria.crearPartidoIda();
            esVuelta = false;
        } else if (eliminatoria.isDoblePartido() && eliminatoria.getVuelta() == null) {
            partido = eliminatoria.crearPartidoVuelta();
            esVuelta = true;
        } else {
            // No debería ocurrir, pero por seguridad
            partido = eliminatoria.crearPartidoIda();
        }

        partido.iniciarSimulacion();

        // Determinar cuál es el equipo del usuario en este partido
        if (partido.getLocal() == usr || partido.getLocal().getNombre().equals(usr.getNombre())) {
            equipoUsuario = partido.getLocal();
        } else {
            equipoUsuario = partido.getVisitante();
        }
    }

    // ── Construcción de la UI ────────────────────────────────────────────

    private void construirUI() {
        // ── Cabecera ──────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setBackground(BG_DARK);

        String titulo = esVuelta ? "⚽ Partido de VUELTA" : "⚽ Partido de IDA";
        JLabel lblTitulo = new JLabel("  " + titulo, SwingConstants.LEFT);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblTitulo.setForeground(UCL_GOLD);

        lblMarcador = new JLabel(marcadorTexto(), SwingConstants.CENTER);
        lblMarcador.setFont(new Font("Monospaced", Font.BOLD, 22));
        lblMarcador.setForeground(BLANCO);

        lblBloque = new JLabel("▶ Minuto 0", SwingConstants.RIGHT);
        lblBloque.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblBloque.setForeground(GRIS);
        lblBloque.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));

        header.add(lblTitulo,   BorderLayout.WEST);
        header.add(lblMarcador, BorderLayout.CENTER);
        header.add(lblBloque,   BorderLayout.EAST);

        // ── Narración ─────────────────────────────────────────────────────
        txtNarracion = new JTextArea();
        txtNarracion.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtNarracion.setBackground(BG_CARD);
        txtNarracion.setForeground(new Color(200, 215, 255));
        txtNarracion.setEditable(false);
        txtNarracion.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        txtNarracion.setText(partido.getNarracion());

        JScrollPane scrollNarracion = new JScrollPane(txtNarracion);
        scrollNarracion.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(UCL_BLUE), " 📡 Narración del partido ",
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("SansSerif", Font.BOLD, 12), UCL_GOLD));
        scrollNarracion.setBackground(BG_DARK);
        scrollNarracion.getViewport().setBackground(BG_CARD);

        // ── Panel lateral: sustituciones ──────────────────────────────────
        JPanel panelCambios = construirPanelCambios();

        panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        panelBotones.setBackground(BG_DARK);

        btnSiguiente = boton("▶▶  Siguiente bloque (15 min)", UCL_BLUE, BLANCO);
        btnPenaltis  = boton("🥅  Penaltis", new Color(80, 20, 20), ROJO);
        btnVolver    = boton("← Volver al torneo", BG_CARD, GRIS);
        btnCerrar    = boton("❌ Cerrar Programa", ROJO, BLANCO);

        btnPenaltis.setVisible(false);
        btnVolver.setVisible(false);
        btnCerrar.setVisible(false);

        btnSiguiente.addActionListener(e -> simularBloque());
        btnPenaltis.addActionListener(e  -> simularPenaltis());
        btnVolver.addActionListener(e    -> volverAlTorneo());
        btnCerrar.addActionListener(e    -> System.exit(0));

        panelBotones.add(btnSiguiente);
        panelBotones.add(btnPenaltis);
        panelBotones.add(btnVolver);
        panelBotones.add(btnCerrar);

        // ── Ensamblado ────────────────────────────────────────────────────
        JPanel centro = new JPanel(new BorderLayout(8, 0));
        centro.setBackground(BG_DARK);
        centro.add(scrollNarracion, BorderLayout.CENTER);
        centro.add(panelCambios,    BorderLayout.EAST);

        add(header,       BorderLayout.NORTH);
        add(centro,       BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);
    }

    private JComboBox<String> cmbTactica;

    private JPanel construirPanelCambios() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG_DARK);
        panel.setPreferredSize(new Dimension(280, 0));
        
        // --- SECCIÓN ESTRATEGIA ---
        JPanel pnlEst = new JPanel();
        pnlEst.setLayout(new BoxLayout(pnlEst, BoxLayout.Y_AXIS));
        pnlEst.setBackground(BG_CARD);
        pnlEst.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(UCL_BLUE), " ⚙ Estrategia en Vivo ",
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("SansSerif", Font.BOLD, 11), UCL_GOLD));
        
        cmbTactica = new JComboBox<>(new String[]{"Equilibrada", "Tiki Taka", "Contraataque", "Autobús", "Por las bandas"});
        cmbTactica.setSelectedItem(equipoUsuario.getTactica());
        estilizarCombo(cmbTactica);
        cmbTactica.addActionListener(e -> {
            equipoUsuario.setTactica(cmbTactica.getSelectedItem().toString());
            frame.setEstado("Táctica cambiada a: " + equipoUsuario.getTactica());
        });

        pnlEst.add(etiqueta("Cambiar Táctica:"));
        pnlEst.add(cmbTactica);

        // --- SECCIÓN SUSTITUCIONES ---
        JPanel pnlSubs = new JPanel();
        pnlSubs.setLayout(new BoxLayout(pnlSubs, BoxLayout.Y_AXIS));
        pnlSubs.setBackground(BG_CARD);
        pnlSubs.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(UCL_BLUE), " 🔄 Sustituciones ",
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("SansSerif", Font.BOLD, 11), UCL_GOLD));

        cmbSale  = new JComboBox<>();
        cmbEntra = new JComboBox<>();
        estilizarCombo(cmbSale);
        estilizarCombo(cmbEntra);

        btnCambio = boton("✔ Realizar Cambio", new Color(20, 60, 20), VERDE);
        btnCambio.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        btnCambio.addActionListener(e -> realizarCambio());
        
        pnlSubs.add(etiqueta("Sale (Titular):"));
        pnlSubs.add(cmbSale);
        pnlSubs.add(Box.createVerticalStrut(5));
        pnlSubs.add(etiqueta("Entra (Suplente):"));
        pnlSubs.add(cmbEntra);
        pnlSubs.add(Box.createVerticalStrut(10));
        pnlSubs.add(btnCambio);

        // --- PLANTILLA ---
        JTextArea txtPlantilla = new JTextArea();
        txtPlantilla.setFont(new Font("Monospaced", Font.PLAIN, 10));
        txtPlantilla.setBackground(BG_CARD);
        txtPlantilla.setForeground(GRIS);
        txtPlantilla.setEditable(false);
        txtPlantilla.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        
        JScrollPane scrollPlantilla = new JScrollPane(txtPlantilla);
        scrollPlantilla.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(40, 60, 100)), " Once inicial ",
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("SansSerif", Font.BOLD, 10), GRIS));
        
        actualizarTextoPlantilla(txtPlantilla);
        actualizarCombos();

        panel.add(pnlEst);
        panel.add(Box.createVerticalStrut(10));
        panel.add(pnlSubs);
        panel.add(Box.createVerticalStrut(10));
        panel.add(scrollPlantilla);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    // ── Lógica de simulación ─────────────────────────────────────────────

    private void simularBloque() {
        String texto = partido.simularSiguienteBloque();
        txtNarracion.append(texto);
        txtNarracion.setCaretPosition(txtNarracion.getDocument().getLength());

        lblMarcador.setText(marcadorTexto());
        lblBloque.setText("▶ Minuto " + Math.min(partido.getMinutoActual() - 1, 90));

        actualizarCombos();

        if (!partido.isEnJuego()) {
            btnSiguiente.setEnabled(false);
            btnCambio.setEnabled(false);
            cmbSale.setEnabled(false);
            cmbEntra.setEnabled(false);

            // Recuperar energía al terminar el partido
            equipoUsuario.recuperarEnergiaPlantilla();

            // Determinar si hay empate global o ganador
            eliminatoria.determinarGanador();

            if (eliminatoria.requierePenaltis()) {
                // Hay empate global. Decidimos por probabilidad (20% penaltis)
                if (new java.util.Random().nextDouble() < 0.20) {
                    btnPenaltis.setVisible(true);
                    txtNarracion.append("\n⚖ EMPATE GLOBAL. ¡El destino se decidirá en los PENALTIS! 🥅\n");
                } else {
                    // 80% -> Se resuelve "en el campo" (rompemos el empate artificialmente)
                    txtNarracion.append("\n⚖ Empate global... ¡Pero hay un gol agónico en los minutos finales! ⚽\n");
                    Equipo mejor = (partido.getLocal().getMediaMedia() > partido.getVisitante().getMediaMedia()) 
                        ? partido.getLocal() : partido.getVisitante();
                    
                    if (mejor == partido.getLocal()) {
                         partido.simularGolForzado(true); // Método a crear en Partido.java
                    } else {
                         partido.simularGolForzado(false);
                    }
                    
                    eliminatoria.determinarGanador(); // Re-determinar tras el gol
                    mostrarResultadoFinal();
                    btnVolver.setVisible(true);
                    if (!eliminatoria.isDoblePartido()) btnCerrar.setVisible(true);
                }
            } else {
                // No hay empate global (o ya hay ganador claro)
                if (!esVuelta && eliminatoria.isDoblePartido()) {
                    btnVolver.setText("← Volver al Torneo (Ajustar equipo)");
                    btnVolver.setVisible(true);
                } else {
                    mostrarResultadoFinal();
                    btnVolver.setVisible(true);
                    if (!eliminatoria.isDoblePartido()) btnCerrar.setVisible(true);
                }
            }
        }
    }

    private void simularPenaltis() {
        ArrayList<Jugador> disponible = new ArrayList<>(equipoUsuario.getTitulares());
        ArrayList<Jugador> seleccionados = new ArrayList<>();
        
        // Diálogo personalizado para ordenar lanzadores
        JList<Jugador> listDisp = new JList<>(new DefaultListModel<>());
        JList<Jugador> listOrden = new JList<>(new DefaultListModel<>());
        
        for (Jugador j : disponible) ((DefaultListModel<Jugador>)listDisp.getModel()).addElement(j);
        
        listDisp.setCellRenderer(new PlayerListRenderer());
        listOrden.setCellRenderer(new PlayerListRenderer());
        
        listDisp.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Jugador sel = listDisp.getSelectedValue();
                if (sel != null) {
                    ((DefaultListModel<Jugador>)listDisp.getModel()).removeElement(sel);
                    ((DefaultListModel<Jugador>)listOrden.getModel()).addElement(sel);
                }
            }
        });

        listOrden.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Jugador sel = listOrden.getSelectedValue();
                if (sel != null) {
                    ((DefaultListModel<Jugador>)listOrden.getModel()).removeElement(sel);
                    ((DefaultListModel<Jugador>)listDisp.getModel()).addElement(sel);
                }
            }
        });

        JPanel p = new JPanel(new GridLayout(1, 2, 10, 0));
        p.add(scrollConTitulo(listDisp, " Disponibles (Haz click) "));
        p.add(scrollConTitulo(listOrden, " Orden de Lanzamiento "));
        p.setPreferredSize(new Dimension(600, 300));

        int res = JOptionPane.showConfirmDialog(this, p, "Configurar Tanda de Penaltis", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            
        if (res == JOptionPane.OK_OPTION) {
            for (int i = 0; i < listOrden.getModel().getSize(); i++) {
                seleccionados.add(listOrden.getModel().getElementAt(i));
            }
            // Autocompletar con los no seleccionados
            for (int i = 0; i < listDisp.getModel().getSize(); i++) {
                seleccionados.add(listDisp.getModel().getElementAt(i));
            }
        } else {
            seleccionados.addAll(disponible); // Default si cancela
        }
        
        ArrayList<Jugador> lanzIA = (partido.getLocal() == equipoUsuario) 
            ? partido.getVisitante().getMejoresLanzadores(11) 
            : partido.getLocal().getMejoresLanzadores(11);
        
        // 2. Preparar tanda paso a paso
        partido.prepararTanda(seleccionados, lanzIA);
        txtNarracion.append("\n🏟  ¡COMIENZA LA TANDA DE PENALTIS! 🥅\n");
        btnPenaltis.setVisible(false);
        btnSiguiente.setText("▶▶  Lanzar Penalti");
        btnSiguiente.setEnabled(true);
        btnSiguiente.removeActionListener(btnSiguiente.getActionListeners()[0]);
        btnSiguiente.addActionListener(e -> avanzarPenalti());
    }

    private void avanzarPenalti() {
        String texto = partido.simularSiguienteRondaPenal();
        txtNarracion.append(texto);
        txtNarracion.setCaretPosition(txtNarracion.getDocument().getLength());
        lblMarcador.setText(String.format(" %s %d(%d) – %d(%d) %s ",
            partido.getLocal().getNombre(),
            partido.getGolesLocal(), partido.getPenaltisLocal(),
            partido.getPenaltisVisitante(), partido.getGolesVisitante(),
            partido.getVisitante().getNombre()));
            
        if (!partido.isEnTanda()) {
            btnSiguiente.setEnabled(false);
            
            // Sincronizar el ganador con la eliminatoria
            eliminatoria.setGanador(partido.getGanador());
            
            mostrarResultadoFinal();
            btnVolver.setVisible(true);
            if (!eliminatoria.isDoblePartido()) {
                btnCerrar.setVisible(true);
            }
        }
    }


    private void volverAlTorneo() {
        frame.mostrarPantalla(MainFrame.PANTALLA_TORNEO);
    }

    private void mostrarResultadoFinal() {
        Equipo ganador = eliminatoria.getGanador();
        boolean esFinal = !eliminatoria.isDoblePartido();
        
        String msg;
        if (esFinal && ganador == equipoUsuario) {
            msg = "\n🏆🏆🏆 ¡¡¡ CAMPEONES DE EUROPA !!! 🏆🏆🏆\n"
                + "Enhorabuena, el " + ganador.getNombre() + " ha conquistado la gloria.\n";
        } else {
            msg = ganador != null
                ? "🏆 CLASIFICADO: " + ganador.getNombre()
                : "⚠ Sin clasificado aún.";
        }
        
        txtNarracion.append("\n" + msg + "\n");
        txtNarracion.setCaretPosition(txtNarracion.getDocument().getLength());
        frame.setEstado(msg);

        // Si es la final, mostrar el botón de cerrar
        if (esFinal) {
             btnVolver.setText("← Salir al Menú Principal");
             // El botón de cerrar se activa en simularBloque/simularPenaltis
        }
    }

    // ── Sustituciones ────────────────────────────────────────────────────

    private void realizarCambio() {
        int iSale  = cmbSale.getSelectedIndex();
        int iEntra = cmbEntra.getSelectedIndex();

        titularesRef = equipoUsuario.getTitulares();
        suplentesRef = equipoUsuario.getSuplentes();

        if (iSale < 0 || iEntra < 0 || titularesRef.isEmpty() || suplentesRef.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecciona un titular y un suplente.");
            return;
        }
        if (iSale  >= titularesRef.size() || iEntra >= suplentesRef.size()) {
            JOptionPane.showMessageDialog(this, "Selección no válida.");
            return;
        }

        Jugador sale  = titularesRef.get(iSale);
        Jugador entra = suplentesRef.get(iEntra);

        boolean ok = equipoUsuario.realizarCambio(sale, entra);
        if (ok) {
            String msg = String.format("🔄 Cambio: %s ← %s", entra.getNombre(), sale.getNombre());
            txtNarracion.append("\n" + msg + "\n");
            txtNarracion.setCaretPosition(txtNarracion.getDocument().getLength());
            frame.setEstado(msg);
            actualizarCombos();
        } else {
            JOptionPane.showMessageDialog(this, "No se pudo realizar el cambio.");
        }
    }

    /**
     * Rellena los combos con titulares y suplentes actuales.
     * USO DE Iterator: recorre los titulares y suplentes del equipo usuario.
     */
    private void actualizarCombos() {
        cmbSale.removeAllItems();
        cmbEntra.removeAllItems();

        // USO DE Iterator sobre los titulares
        Iterator<Jugador> itTit = equipoUsuario.getTitulares().iterator();
        while (itTit.hasNext()) {
            Jugador j = itTit.next();
            cmbSale.addItem(String.format("[%s] %s (⚡%d%%)",
                j.getPosicion(), j.getNombre(),
                (int)(100.0 * j.getEnergiaActual() / Math.max(1, j.getEnergiaMax()))));
        }

        // USO DE Iterator sobre los suplentes
        Iterator<Jugador> itSup = equipoUsuario.getSuplentes().iterator();
        while (itSup.hasNext()) {
            Jugador j = itSup.next();
            cmbEntra.addItem(String.format("[%s] %s", j.getPosicion(), j.getNombre()));
        }

        boolean hayCambios = cmbSale.getItemCount() > 0 && cmbEntra.getItemCount() > 0;
        cmbSale.setEnabled(hayCambios && partido.isEnJuego());
        cmbEntra.setEnabled(hayCambios && partido.isEnJuego());
        btnCambio.setEnabled(hayCambios && partido.isEnJuego());
    }

    private void actualizarTextoPlantilla(JTextArea txt) {
        StringBuilder sb = new StringBuilder();
        sb.append(" POS  JUGADOR              MED  ⚡\n");
        sb.append(" " + "─".repeat(38) + "\n");
        // USO DE Iterator sobre titulares
        Iterator<Jugador> it = equipoUsuario.getTitulares().iterator();
        while (it.hasNext()) {
            Jugador j = it.next();
            int pct = (int)(100.0 * j.getEnergiaActual() / Math.max(1, j.getEnergiaMax()));
            sb.append(String.format(" %-4s %-20s %3d  %3d%%\n",
                j.getPosicion(), j.getNombre().substring(0, Math.min(20, j.getNombre().length())),
                j.getMediaGeneral(), pct));
        }
        txt.setText(sb.toString());
    }

    // ── Helpers de UI ────────────────────────────────────────────────────

    private String marcadorTexto() {
        return String.format(" %s  %d – %d  %s ",
            partido.getLocal().getNombre(),
            partido.getGolesLocal(),
            partido.getGolesVisitante(),
            partido.getVisitante().getNombre());
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

    private JLabel etiqueta(String texto) {
        JLabel lbl = new JLabel("  " + texto);
        lbl.setForeground(GRIS);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        return lbl;
    }

    private void estilizarCombo(JComboBox<String> cmb) {
        cmb.setBackground(BG_CARD);
        cmb.setForeground(BLANCO);
        cmb.setFont(new Font("Monospaced", Font.PLAIN, 11));
        cmb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        cmb.setAlignmentX(LEFT_ALIGNMENT);
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

    private class PlayerListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Jugador j) {
                label.setText(String.format(" [%-4s] %-20s (Atq:%d)", 
                    j.getPosicion(), j.getNombre(), j.getAtaque()));
                label.setFont(new Font("Monospaced", Font.PLAIN, 12));
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
}
