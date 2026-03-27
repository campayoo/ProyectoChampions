package gui;

import model.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * PanelMercado — permite al usuario ver los jugadores disponibles en el
 * mercado de fichajes, filtrarlos por posición y fichar.
 *
 * USO DE Iterator: actualizarTabla() recorre los ofertados usando un
 * Iterator para construir la vista de la lista.
 * USO DE instanceof: al publicar un jugador de la plantilla del usuario,
 * se delega en MercadoFichajes.publicarJugador(), que usa instanceof.
 */
public class PanelMercado extends JPanel {

    private final MainFrame         frame;
    private final MercadoFichajes   mercado;

    // ── Paleta ───────────────────────────────────────────────────────────
    private static final Color BG_DARK  = new Color(10, 14, 30);
    private static final Color BG_CARD  = new Color(20, 28, 58);
    private static final Color UCL_BLUE = new Color(0, 100, 255);
    private static final Color UCL_GOLD = new Color(255, 210, 0);
    private static final Color VERDE    = new Color(0, 200, 100);
    private static final Color ROJO     = new Color(220, 60, 60);
    private static final Color BLANCO   = Color.WHITE;
    private static final Color GRIS     = new Color(160, 175, 210);

    // ── Componentes ──────────────────────────────────────────────────────
    private DefaultListModel<String> modeloLista;
    private JList<String>            listaJugadores;
    private JComboBox<String>        cmbFiltro;
    private JTextArea                txtDetalle;
    private JLabel                   lblPresupuesto;

    /** Guarda los jugadores visibles en la lista (sincronizado con modeloLista). */
    private ArrayList<Jugador>       jugadoresVisibles;

    // ─────────────────────────────────────────────────────────────────────
    public PanelMercado(MainFrame frame, MercadoFichajes mercado) {
        this.frame   = frame;
        this.mercado = mercado;
        setBackground(BG_DARK);
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        construirUI();
    }

    // ── Construcción de la UI ────────────────────────────────────────────

    private void construirUI() {
        // ── Cabecera ──────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setBackground(BG_DARK);

        JLabel lblTitulo = new JLabel("  💶  Mercado de Fichajes", SwingConstants.LEFT);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblTitulo.setForeground(UCL_GOLD);

        lblPresupuesto = new JLabel(presupuestoTexto(), SwingConstants.RIGHT);
        lblPresupuesto.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblPresupuesto.setForeground(VERDE);
        lblPresupuesto.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 6));

        header.add(lblTitulo,    BorderLayout.WEST);
        header.add(lblPresupuesto, BorderLayout.EAST);

        // ── Filtro de posición ────────────────────────────────────────────
        JPanel panelFiltro = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        panelFiltro.setBackground(BG_DARK);

        JLabel lblFiltro = new JLabel("Filtrar por posición:");
        lblFiltro.setForeground(GRIS);
        lblFiltro.setFont(new Font("SansSerif", Font.PLAIN, 12));

        cmbFiltro = new JComboBox<>(new String[]{"TODOS", "POR", "DEF", "MED", "DEL"});
        cmbFiltro.setBackground(BG_CARD);
        cmbFiltro.setForeground(BLANCO);
        cmbFiltro.setFont(new Font("SansSerif", Font.BOLD, 12));
        cmbFiltro.addActionListener(e -> actualizarTabla());

        panelFiltro.add(lblFiltro);
        panelFiltro.add(cmbFiltro);

        JPanel norte = new JPanel(new BorderLayout(0, 4));
        norte.setBackground(BG_DARK);
        norte.add(header,      BorderLayout.NORTH);
        norte.add(panelFiltro, BorderLayout.SOUTH);

        // ── Lista de jugadores ofertados ──────────────────────────────────
        modeloLista      = new DefaultListModel<>();
        jugadoresVisibles = new ArrayList<>();
        listaJugadores   = new JList<>(modeloLista);
        listaJugadores.setBackground(BG_CARD);
        listaJugadores.setForeground(BLANCO);
        listaJugadores.setFont(new Font("Monospaced", Font.PLAIN, 12));
        listaJugadores.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaJugadores.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        listaJugadores.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) mostrarDetalle();
        });

        JScrollPane scrollLista = new JScrollPane(listaJugadores);
        scrollLista.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(UCL_BLUE), " Jugadores disponibles ",
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("SansSerif", Font.BOLD, 12), UCL_GOLD));
        scrollLista.setBackground(BG_DARK);
        scrollLista.getViewport().setBackground(BG_CARD);

        // ── Detalle del jugador seleccionado ──────────────────────────────
        txtDetalle = new JTextArea("← Selecciona un jugador para ver su ficha.");
        txtDetalle.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtDetalle.setBackground(BG_CARD);
        txtDetalle.setForeground(GRIS);
        txtDetalle.setEditable(false);
        txtDetalle.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        JScrollPane scrollDetalle = new JScrollPane(txtDetalle);
        scrollDetalle.setPreferredSize(new Dimension(310, 0));
        scrollDetalle.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(UCL_BLUE), " Ficha del jugador ",
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("SansSerif", Font.BOLD, 12), UCL_GOLD));

        // ── Botones de acción ─────────────────────────────────────────────
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        panelBotones.setBackground(BG_DARK);

        JButton btnFichar  = boton("✅  Fichar jugador",          new Color(20, 60, 20),  VERDE);
        JButton btnVender  = boton("💰  Poner jugador en venta",  new Color(60, 40, 10),  UCL_GOLD);
        JButton btnVolver  = boton("← Volver al torneo",          BG_CARD,                GRIS);

        btnFichar.addActionListener(e -> ficharSeleccionado());
        btnVender.addActionListener(e -> venderJugadorUsuario());
        btnVolver.addActionListener(e -> frame.mostrarPantalla(MainFrame.PANTALLA_TORNEO));

        panelBotones.add(btnFichar);
        panelBotones.add(btnVender);
        panelBotones.add(btnVolver);

        // ── Ensamblado ────────────────────────────────────────────────────
        JPanel centro = new JPanel(new BorderLayout(8, 0));
        centro.setBackground(BG_DARK);
        centro.add(scrollLista,   BorderLayout.CENTER);
        centro.add(scrollDetalle, BorderLayout.EAST);

        add(norte,        BorderLayout.NORTH);
        add(centro,       BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);

        actualizarTabla();
    }

    // ── Lógica de datos ──────────────────────────────────────────────────

    /**
     * Rellena la lista con los jugadores ofertados según el filtro activo.
     *
     * USO DE Iterator: recorre el resultado de filtrarPorPosicion(), que
     * internamente usa un Iterator sobre el ArrayList de ofertados.
     */
    private void actualizarTabla() {
        modeloLista.clear();
        jugadoresVisibles.clear();

        String filtro = (String) cmbFiltro.getSelectedItem();
        ArrayList<Jugador> lista = mercado.filtrarPorPosicion(filtro); // devuelve lista filtrada

        // USO DE Iterator para recorrer la lista filtrada
        Iterator<Jugador> it = lista.iterator();
        while (it.hasNext()) {
            Jugador j = it.next();
            String equipo = j.getEquipo() != null ? j.getEquipo().getNombre() : "Libre";
            modeloLista.addElement(String.format("%-24s [%-3s]  %s  | %.1fM€",
                j.getNombre().substring(0, Math.min(24, j.getNombre().length())),
                j.getPosicion(),
                equipo.substring(0, Math.min(18, equipo.length())),
                j.getValorMercado()));
            jugadoresVisibles.add(j);
        }

        if (modeloLista.isEmpty()) {
            modeloLista.addElement("  — No hay jugadores disponibles con este filtro —");
        }

        lblPresupuesto.setText(presupuestoTexto());
    }

    private void mostrarDetalle() {
        int idx = listaJugadores.getSelectedIndex();
        if (idx < 0 || idx >= jugadoresVisibles.size()) {
            txtDetalle.setText("← Selecciona un jugador para ver su ficha.");
            return;
        }
        Jugador j = jugadoresVisibles.get(idx);
        String equipo = j.getEquipo() != null ? j.getEquipo().getNombre() : "Agente libre";

        StringBuilder sb = new StringBuilder();
        sb.append("════════════════════════════\n");
        sb.append("  ").append(j.getNombre()).append("\n");
        sb.append("════════════════════════════\n");
        sb.append(String.format("  Posición  : %-10s\n", j.getPosicion()));
        sb.append(String.format("  Edad      : %-3d años\n", j.getEdad()));
        sb.append(String.format("  Nación    : %s\n", j.getNacionalidad()));
        sb.append(String.format("  Equipo    : %s\n", equipo));
        sb.append("────────────────────────────\n");
        sb.append(String.format("  Media     : %d\n",  j.getMediaGeneral()));
        sb.append(String.format("  Ataque    : %d\n",  j.getAtaque()));
        sb.append(String.format("  Defensa   : %d\n",  j.getDefensa()));
        sb.append(String.format("  Energía   : %d\n",  j.getEnergiaMax()));
        sb.append(String.format("  Velocidad : %d\n",  j.getVelocidad()));
        sb.append("────────────────────────────\n");
        sb.append(String.format("  Valor     : %.1fM€\n", j.getValorMercado()));
        sb.append(String.format("  Goles     : %d\n",  j.getGoles()));
        sb.append(String.format("  Asistencias: %d\n", j.getAsistencias()));
        sb.append(String.format("  Amarillas : %d\n",  j.getTarjetasAmarillas()));
        txtDetalle.setText(sb.toString());
        txtDetalle.setCaretPosition(0);
    }

    private void ficharSeleccionado() {
        int idx = listaJugadores.getSelectedIndex();
        if (idx < 0 || idx >= jugadoresVisibles.size()) {
            JOptionPane.showMessageDialog(this, "Selecciona un jugador de la lista.");
            return;
        }
        Jugador j      = jugadoresVisibles.get(idx);
        Equipo  destino = frame.getTorneo().getEquipoUsuario();

        String resultado = mercado.transferir(j, destino);
        JOptionPane.showMessageDialog(this, resultado);
        frame.actualizarBarra();
        actualizarTabla();
        txtDetalle.setText("← Selecciona un jugador para ver su ficha.");
    }

    /**
     * Permite al usuario poner en venta a un jugador de su propia plantilla.
     * Muestra un diálogo con la lista de su plantilla para elegir.
     */
    private void venderJugadorUsuario() {
        Equipo eq = frame.getTorneo().getEquipoUsuario();
        ArrayList<Jugador> plantilla = eq.getPlantilla();

        if (plantilla.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tu plantilla está vacía.");
            return;
        }

        String[] nombres = new String[plantilla.size()];
        for (int i = 0; i < plantilla.size(); i++) {
            Jugador j = plantilla.get(i);
            nombres[i] = String.format("[%s] %s — %.1fM€", j.getPosicion(), j.getNombre(), j.getValorMercado());
        }

        String elegido = (String) JOptionPane.showInputDialog(
            this, "Selecciona el jugador a poner en venta:",
            "Poner en venta", JOptionPane.PLAIN_MESSAGE,
            null, nombres, nombres[0]);

        if (elegido == null) return;

        int idx = 0;
        for (int i = 0; i < nombres.length; i++) {
            if (nombres[i].equals(elegido)) { idx = i; break; }
        }

        // USO DE instanceof — delegado en mercado.publicarJugador()
        String resultado = mercado.publicarJugador(plantilla.get(idx));
        JOptionPane.showMessageDialog(this, resultado);
        actualizarTabla();
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private String presupuestoTexto() {
        Equipo eq = frame.getTorneo().getEquipoUsuario();
        return "Tu equipo: " + eq.getNombre()
             + "  |  💶 Presupuesto: "
             + String.format("%.1f", eq.getPresupuesto()) + "M€  ";
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
