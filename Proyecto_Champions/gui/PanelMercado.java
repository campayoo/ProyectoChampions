package gui;

import model.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

import static gui.UCLTheme.*;

/**
 * Clase PanelMercado: Interfaz de gestión de fichajes y traspasos de activos.
 * 
 * Permite al mánager:
 * - Filtrar el mercado global buscando perfiles específicos (POR, DEF, etc.).
 * - Consultar el informe de 'Scouting' detallado de cada futbolista.
 * - Ejecutar transacciones financieras (Fichajes y Ventas) para mejorar la plantilla.
 * - Monitorizar el presupuesto del club en tiempo real.
 */
public class PanelMercado extends JPanel {

    // --- BLOQUE: REFERENCIAS DE CONTROL ---
    private final MainFrame         frame;
    private final MercadoFichajes   mercado;

    // --- BLOQUE: COMPONENTES GRÁFICOS ---
    private DefaultListModel<String> modeloLista;
    private JList<String>            listaJugadores;
    private JComboBox<String>        cmbFiltro;
    private JTextArea                txtDetalle;
    private JLabel                   lblPresupuesto;
    private ArrayList<Jugador>       jugadoresVisibles;

    /**
     * Constructor: Inicializa la oficina de traspasos del club.
     */
    public PanelMercado(MainFrame frame, MercadoFichajes mercado) {
        this.frame   = frame;
        this.mercado = mercado;
        
        setBackground(DEEP_BLUE);
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        construirUI();
    }

    // ---------------------------------------------------------------------
    // BLOQUE: CONSTRUCCIÓN DE LA ARQUITECTURA DE NEGOCIOS
    // ---------------------------------------------------------------------

    private void construirUI() {
        // Sub-bloque: Cabecera con Balance Financiero
        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setOpaque(false);

        JLabel lblTitulo = glowLabel("💶  CENTRO DE NEGOCIOS Y FICHAJES", UCL_GOLD, 24, true);
        lblPresupuesto = new JLabel(presupuestoTexto(), SwingConstants.RIGHT);
        lblPresupuesto.setFont(fontBody(14));
        lblPresupuesto.setForeground(VERDE);

        header.add(lblTitulo, BorderLayout.WEST);
        header.add(lblPresupuesto, BorderLayout.EAST);

        // Sub-bloque: Barra de Filtros de Scouting
        JPanel panelFiltro = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        panelFiltro.setOpaque(false);
        JLabel lblF = new JLabel("MODIFICAR FILTRO DE POSICIÓN:");
        lblF.setForeground(Color.LIGHT_GRAY);
        lblF.setFont(fontTitle(11));

        cmbFiltro = new JComboBox<>(new String[]{"TODOS", "POR", "DEF", "MED", "DEL"});
        cmbFiltro.addActionListener(e -> actualizarTabla());

        panelFiltro.add(lblF);
        panelFiltro.add(cmbFiltro);

        JPanel norte = new JPanel(new BorderLayout(0, 15));
        norte.setOpaque(false);
        norte.add(header, BorderLayout.NORTH);
        norte.add(panelFiltro, BorderLayout.SOUTH);

        // Sub-bloque: Listado Maestro (Oferta de Jugadores)
        modeloLista       = new DefaultListModel<>();
        jugadoresVisibles = new ArrayList<>();
        listaJugadores    = new JList<>(modeloLista);
        listaJugadores.setBackground(new Color(10, 20, 45));
        listaJugadores.setForeground(Color.WHITE);
        listaJugadores.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaJugadores.setFixedCellHeight(35);
        listaJugadores.addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) mostrarDetalle(); });

        JScrollPane scrollLista = new JScrollPane(listaJugadores);
        scrollLista.setBorder(BorderFactory.createLineBorder(UCL_BLUE, 1));

        // Sub-bloque: Informe de Scouting (Panel Lateral)
        txtDetalle = new JTextArea();
        txtDetalle.setFont(fontMono(12));
        txtDetalle.setEditable(false);
        txtDetalle.setBackground(new Color(6, 12, 30));
        txtDetalle.setForeground(GRIS_CLARO);
        txtDetalle.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel wrapDetalle = glassPanel(new BorderLayout());
        wrapDetalle.setPreferredSize(new Dimension(320, 0));
        wrapDetalle.add(txtDetalle, BorderLayout.CENTER);

        // Sub-bloque: Panel de Comandos Estratégicos (Footer)
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 10));
        panelBotones.setOpaque(false);

        JButton btnFichar = uclButton("✅ CERRAR FICHAJE", VERDE);
        JButton btnVender = uclButton("💰 PONER EN VENTA", UCL_GOLD);
        JButton btnVolver = uclButton("↩ VOLVER AL CLUB", UCL_BLUE);

        btnFichar.addActionListener(e -> ficharSeleccionado());
        btnVender.addActionListener(e -> venderJugador());
        btnVolver.addActionListener(e -> {
            try { frame.mostrarPantalla(MainFrame.PANTALLA_TORNEO); } catch (IOException ex) {}
        });

        panelBotones.add(btnFichar);
        panelBotones.add(btnVender);
        panelBotones.add(btnVolver);

        // Integración Final
        JPanel centro = new JPanel(new BorderLayout(15, 0));
        centro.setOpaque(false);
        centro.add(scrollLista, BorderLayout.CENTER);
        centro.add(wrapDetalle, BorderLayout.EAST);

        add(norte, BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);

        actualizarTabla();
    }

    // ---------------------------------------------------------------------
    // BLOQUE: LÓGICA DE GESTIÓN DE MERCADO
    // ---------------------------------------------------------------------

    /**
     * Sincroniza la tabla con los activos disponibles en el mercado global.
     */
    private void actualizarTabla() {
        modeloLista.clear();
        jugadoresVisibles.clear();

        String filtro = (String) cmbFiltro.getSelectedItem();
        ArrayList<Jugador> lista = mercado.filtrarPorPosicion(filtro);

        for (Jugador j : lista) {
            String fila = String.format("  %-20s [%-3s]  OVR:%d  |  %.1f M€", 
                j.getNombre(), j.getPosicion(), j.getMediaGeneral(), j.getValorMercado());
            modeloLista.addElement(fila);
            jugadoresVisibles.add(j);
        }

        lblPresupuesto.setText(presupuestoTexto());
    }

    /**
     * Genera el desglose técnico del futbolista seleccionado para el mánager.
     */
    private void mostrarDetalle() {
        int idx = listaJugadores.getSelectedIndex();
        if (idx < 0 || idx >= jugadoresVisibles.size()) {
            txtDetalle.setText("\n\n   Selecciona un activo\n   para ver su informe detallado.");
            return;
        }

        Jugador j = jugadoresVisibles.get(idx);
        String info = String.format(
            " INFORME DE SCOUTING: %s\n" +
            " ────────────────────────────\n" +
            " ROL TÁCTICO: %s\n" +
            " CALIDAD    : %d OVR\n" +
            " EDAD       : %d años\n" +
            " NACIONALIDAD: %s\n" +
            " CLUB ACTUAL : %s\n\n" +
            " ATRIBUTOS TÉCNICOS\n" +
            "  ⚽ Ataque  : %d\n" +
            "  🛡 Defensa : %d\n" +
            "  ⚡ Estamina : %d\n\n" +
            " ────────────────────────────\n" +
            " VALORACIÓN MERCADO: %.2f M€",
            j.getNombre().toUpperCase(), j.getPosicion(), j.getMediaGeneral(), 
            j.getEdad(), j.getNacionalidad(), 
            (j.getEquipo() != null ? j.getEquipo().getNombre() : "Agente Libre"),
            j.getAtaque(), j.getDefensa(), j.getEnergiaMax(), j.getValorMercado()
        );
        txtDetalle.setText(info);
    }

    /**
     * Ejecuta el protocolo de contratación tras confirmación.
     */
    private void ficharSeleccionado() {
        int idx = listaJugadores.getSelectedIndex();
        if (idx < 0) return;

        Jugador j = jugadoresVisibles.get(idx);
        Equipo usr = frame.getTorneo().getEquipoUsuario();

        int res = JOptionPane.showConfirmDialog(this, "¿Autorizar el desembolso de " + j.getValorMercado() + "M€ por " + j.getNombre() + "?");
        if (res == JOptionPane.YES_OPTION) {
            String msg = mercado.transferir(j, usr);
            JOptionPane.showMessageDialog(this, msg);
            actualizarTabla();
        }
    }

    /**
     * Habilita un jugador de la propia plantilla para ser transferido.
     */
    private void venderJugador() {
        Equipo eq = frame.getTorneo().getEquipoUsuario();
        ArrayList<Jugador> plantilla = eq.getPlantilla();

        if (plantilla.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No dispones de jugadores para transferir.");
            return;
        }

        String[] nombres = new String[plantilla.size()];
        for (int i = 0; i < plantilla.size(); i++) {
            Jugador j = plantilla.get(i);
            nombres[i] = String.format("[%s] %s — %.1fM€", j.getPosicion(), j.getNombre(), j.getValorMercado());
        }

        String elegido = (String) JOptionPane.showInputDialog(
            this, "Selecciona el activo a transferir:",
            "Venta de Jugador", JOptionPane.PLAIN_MESSAGE,
            null, nombres, nombres[0]);

        if (elegido == null) return;

        int idx = 0;
        for (int i = 0; i < nombres.length; i++) {
            if (nombres[i].equals(elegido)) { idx = i; break; }
        }

        String resultado = mercado.publicarJugador(plantilla.get(idx));
        JOptionPane.showMessageDialog(this, resultado);
        actualizarTabla();
    }

    private String presupuestoTexto() {
        Equipo eq = frame.getTorneo().getEquipoUsuario();
        return String.format("TESORERÍA DEL CLUB: %.1f M€  ", eq.getPresupuesto());
    }
}
