package gui;

import model.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
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

        // ── Botones de control ────────────────────────────────────────────
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        panelBotones.setBackground(BG_DARK);

        btnSiguiente = boton("▶▶  Siguiente bloque (15 min)", UCL_BLUE, BLANCO);
        btnPenaltis  = boton("🥅  Penaltis", new Color(80, 20, 20), ROJO);
        btnVolver    = boton("← Volver al torneo", BG_CARD, GRIS);

        btnPenaltis.setVisible(false);
        btnVolver.setVisible(false);

        btnSiguiente.addActionListener(e -> simularBloque());
        btnPenaltis.addActionListener(e  -> simularPenaltis());
        btnVolver.addActionListener(e    -> volverAlTorneo());

        panelBotones.add(btnSiguiente);
        panelBotones.add(btnPenaltis);
        panelBotones.add(btnVolver);

        // ── Ensamblado ────────────────────────────────────────────────────
        JPanel centro = new JPanel(new BorderLayout(8, 0));
        centro.setBackground(BG_DARK);
        centro.add(scrollNarracion, BorderLayout.CENTER);
        centro.add(panelCambios,    BorderLayout.EAST);

        add(header,       BorderLayout.NORTH);
        add(centro,       BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);
    }

    private JPanel construirPanelCambios() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG_DARK);
        panel.setPreferredSize(new Dimension(270, 0));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(UCL_BLUE), " 🔄 Sustituciones (" + equipoUsuario.getNombre() + ") ",
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("SansSerif", Font.BOLD, 11), UCL_GOLD));

        JLabel lblSale  = etiqueta("Sale (titular):");
        JLabel lblEntra = etiqueta("Entra (suplente):");

        cmbSale  = new JComboBox<>();
        cmbEntra = new JComboBox<>();
        estilizarCombo(cmbSale);
        estilizarCombo(cmbEntra);



        btnCambio = boton("✔  Realizar cambio", new Color(20, 60, 20), VERDE);
        btnCambio.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btnCambio.addActionListener(e -> realizarCambio());
        actualizarCombos();

        // Plantilla actual  con Iterator
        JTextArea txtPlantilla = new JTextArea();
        txtPlantilla.setFont(new Font("Monospaced", Font.PLAIN, 10));
        txtPlantilla.setBackground(BG_CARD);
        txtPlantilla.setForeground(GRIS);
        txtPlantilla.setEditable(false);
        txtPlantilla.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        actualizarTextoPlantilla(txtPlantilla);

        JScrollPane scrollPlantilla = new JScrollPane(txtPlantilla);
        scrollPlantilla.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(40, 60, 100)), " Once inicial ",
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("SansSerif", Font.BOLD, 10), GRIS));

        panel.add(Box.createVerticalStrut(8));
        panel.add(lblSale);
        panel.add(Box.createVerticalStrut(3));
        panel.add(cmbSale);
        panel.add(Box.createVerticalStrut(8));
        panel.add(lblEntra);
        panel.add(Box.createVerticalStrut(3));
        panel.add(cmbEntra);
        panel.add(Box.createVerticalStrut(10));
        panel.add(btnCambio);
        panel.add(Box.createVerticalStrut(12));
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

            // Cuando termina la ida, determinar si se puede declarar ganador
            if (!esVuelta && eliminatoria.isDoblePartido()) {
                // Hay vuelta pendiente
                btnVolver.setText("→ Jugar Vuelta");
                btnVolver.setVisible(true);
                btnVolver.removeActionListener(btnVolver.getActionListeners()[0]);
                btnVolver.addActionListener(e -> prepararVuelta());
            } else {
                // Es partido único o vuelta → determinar clasificado
                eliminatoria.determinarGanador();

                boolean empate = eliminatoria.getGanador() == null;
                if (empate && !eliminatoria.isDoblePartido()) {
                    // Final en empate → penaltis
                    btnPenaltis.setVisible(true);
                } else {
                    mostrarResultadoFinal();
                    btnVolver.setVisible(true);
                }
            }
        }
    }

    private void simularPenaltis() {
        String texto = partido.simularPenaltis();
        txtNarracion.append(texto);
        txtNarracion.setCaretPosition(txtNarracion.getDocument().getLength());
        lblMarcador.setText(marcadorTexto());

        eliminatoria.determinarGanador();
        mostrarResultadoFinal();
        btnPenaltis.setVisible(false);
        btnVolver.setVisible(true);
    }

    private void prepararVuelta() {
        // El partido de vuelta se abre en un nuevo PanelPartido (reutilizamos la misma eliminatoria)
        frame.setEliminatoriaActual(eliminatoria);
        frame.mostrarPantalla(MainFrame.PANTALLA_PARTIDO);
    }

    private void volverAlTorneo() {
        frame.mostrarPantalla(MainFrame.PANTALLA_TORNEO);
    }

    private void mostrarResultadoFinal() {
        Equipo ganador = eliminatoria.getGanador();
        String msg = ganador != null
            ? "🏆 CLASIFICADO: " + ganador.getNombre()
            : "⚠ Sin clasificado aún.";
        txtNarracion.append("\n" + msg + "\n");
        txtNarracion.setCaretPosition(txtNarracion.getDocument().getLength());
        frame.setEstado(msg);
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
}
