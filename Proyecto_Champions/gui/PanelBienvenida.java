package gui;

import data.LectorDatos;
import model.Equipo;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.IOException;
import java.util.ArrayList;

import static gui.UCLTheme.*;

/**
 * Clase PanelBienvenida: Puerta de acceso a la experiencia Champions.
 * 
 * Diseñada para sumergir al jugador mediante:
 * - Un motor de partículas que simula un campo estelar con paralaje.
 * - Estética UCL (Dorado, Azul Eléctrico y Marino).
 * - Selección intuitiva de club con previsualización financiera.
 */
public class PanelBienvenida extends JPanel {

    // --- Referencias de Control ---
    private final MainFrame         frame;
    private       JList<String>     listaEquipos;
    private       ArrayList<Equipo> equipos;
    private       JTextArea         txtInfo;

    // --- BLOQUE: MOTOR DE PARTÍCULAS (Paralaje Estelar) ---
    private int mouseX = 0, mouseY = 0;
    private static final int N_STARS = 120;
    private final float[] sx = new float[N_STARS]; // Posición X normalizada
    private final float[] sy = new float[N_STARS]; // Posición Y normalizada
    private final float[] ss = new float[N_STARS]; // Velocidad y tamaño

    // --- BLOQUE: EFECTOS DE TRANSICIÓN ---
    private float introAlpha = 1f; // Opacidad para el fade-in cinematográfico
    private javax.swing.Timer introTimer;

    /**
     * Constructor: Configura el entorno visual de bienvenida.
     */
    public PanelBienvenida(MainFrame frame) throws IOException {
        this.frame = frame;
        setLayout(new BorderLayout(0, 0));
        setOpaque(true);
        setBackground(DEEP_BLUE);

        // Inicializar coordenadas aleatorias de las estrellas
        java.util.Random rnd = new java.util.Random(42);
        for (int i = 0; i < N_STARS; i++) {
            sx[i] = rnd.nextFloat();
            sy[i] = rnd.nextFloat();
            ss[i] = 0.5f + rnd.nextFloat() * 2.0f;
        }

        // Seguimiento del cursor para el efecto de profundidad
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                mouseX = e.getX(); mouseY = e.getY();
                repaint();
            }
        });

        construirUI();
        iniciarFadeIn();
    }

    // ---------------------------------------------------------------------
    // BLOQUE: RENDERIZADO GRÁFICO (Canvas)
    // ---------------------------------------------------------------------

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        int w = getWidth(), h = getHeight();

        // 1. Capa base: Fondo con degradado real
        g2.setPaint(new GradientPaint(0, 0, DEEP_BLUE, 0, h, ROYAL_BLUE));
        g2.fillRect(0, 0, w, h);

        // 2. Capa: Viñeta radial para enfoque central
        g2.setPaint(new RadialGradientPaint(
                new Point2D.Float(w / 2f, h / 2f),
                Math.max(w, h) * 0.72f,
                new float[]{0f, 1f},
                new Color[]{new Color(0, 30, 80, 0), new Color(0, 5, 15, 210)}));
        g2.fillRect(0, 0, w, h);

        // 3. Capa: Campo de Estrellas (Paralaje dinámico)
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        float px = mouseX / (float) Math.max(w, 1);
        float py = mouseY / (float) Math.max(h, 1);
        for (int i = 0; i < N_STARS; i++) {
            float bx = (sx[i] * w + (px - 0.5f) * 24 * ss[i] + w) % w;
            float by = (sy[i] * h + (py - 0.5f) * 24 * ss[i] + h) % h;
            int alpha = Math.min(255, 90 + (int)(ss[i] * 55));
            g2.setColor(new Color(200, 215, 255, alpha));
            g2.fill(new Ellipse2D.Float(bx - ss[i]/2, by - ss[i]/2, ss[i], ss[i]));
        }

        g2.dispose();
        super.paintComponent(g);

        // 4. Capa: Efecto de fundido a negro (Inicio de ejecución)
        if (introAlpha > 0f) {
            Graphics2D g3 = (Graphics2D) g.create();
            g3.setColor(new Color(0, 0, 0, (int)(introAlpha * 255)));
            g3.fillRect(0, 0, w, h);
            g3.dispose();
        }
    }

    /**
     * Motor de animación para el desvanecimiento inicial.
     */
    private void iniciarFadeIn() {
        introAlpha = 1f;
        introTimer = new javax.swing.Timer(20, e -> {
            introAlpha = Math.max(0f, introAlpha - 0.05f);
            repaint();
            if (introAlpha <= 0f) ((javax.swing.Timer) e.getSource()).stop();
        });
        introTimer.start();
    }

    // ---------------------------------------------------------------------
    // BLOQUE: CONSTRUCCIÓN DE COMPONENTES SWING
    // ---------------------------------------------------------------------

    private void construirUI() throws IOException {
        setOpaque(false); // Permite ver el renderizado del paintComponent

        // --- SUB-BLOQUE: Cabecera Logotipo ---
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(40, 0, 20, 0));

        JLabel titulo = new JLabel("CHAMPIONS MANAGER Elite", SwingConstants.CENTER);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 42));
        titulo.setForeground(UCL_GOLD);
        titulo.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subtitulo = new JLabel("FORJA TU DINASTÍA EN EL FÚTBOL EUROPEO", SwingConstants.CENTER);
        subtitulo.setFont(new Font("SansSerif", Font.PLAIN, 16));
        subtitulo.setForeground(Color.LIGHT_GRAY);
        subtitulo.setAlignmentX(CENTER_ALIGNMENT);

        header.add(titulo);
        header.add(Box.createVerticalStrut(10));
        header.add(subtitulo);

        // --- SUB-BLOQUE: Centro de Selección de Club ---
        equipos = LectorDatos.cargarEquipos();
        DefaultListModel<String> modelo = new DefaultListModel<>();
        for (Equipo e : equipos) {
            modelo.addElement("  " + e.getNombre() + " — " + e.getPais());
        }

        listaEquipos = new JList<>(modelo);
        listaEquipos.setBackground(new Color(8, 18, 48, 200));
        listaEquipos.setForeground(Color.WHITE);
        listaEquipos.setFixedCellHeight(35);
        listaEquipos.setFont(fontBody(13));
        listaEquipos.setSelectedIndex(0);

        // Actualización dinámica de la ficha del club
        listaEquipos.addListSelectionListener(e -> {
            int idx = listaEquipos.getSelectedIndex();
            if (idx >= 0) actualizarInfo(equipos.get(idx));
        });

        JScrollPane scroll = new JScrollPane(listaEquipos);
        scroll.setBorder(BorderFactory.createLineBorder(UCL_BLUE, 1));

        // --- SUB-BLOQUE: Informe de Análisis del Club ---
        txtInfo = new JTextArea(5, 0);
        txtInfo.setEditable(false);
        txtInfo.setFont(fontMono(12));
        txtInfo.setBackground(new Color(6, 14, 38, 180));
        txtInfo.setForeground(Color.CYAN);
        txtInfo.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        actualizarInfo(equipos.get(0));

        // --- SUB-BLOQUE: Control de Inicio ---
        JButton btnIniciar = uclButton("⚽ EMPEZAR CARRERA", UCL_BLUE);
        btnIniciar.addActionListener(e -> iniciarJuego());

        JPanel panelBotones = new JPanel(new FlowLayout());
        panelBotones.setOpaque(false);
        panelBotones.add(btnIniciar);

        // Asamblea de contenedores
        JPanel centro = new JPanel(new BorderLayout(0, 15));
        centro.setOpaque(false);
        centro.setBorder(BorderFactory.createEmptyBorder(10, 100, 20, 100));
        centro.add(scroll, BorderLayout.CENTER);
        centro.add(txtInfo, BorderLayout.SOUTH);

        add(header, BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);
    }

    /**
     * Muestra datos financieros del equipo en el panel de información.
     */
    private void actualizarInfo(Equipo eq) {
        String info = String.format(" [ FICHA CLUB ]\n" +
                                     " Nombre     : %s\n" +
                                     " Nacionality: %s\n" +
                                     " Presupuesto: %.1f M€\n" +
                                     " Objetivo   : Ganar la UCL",
                                     eq.getNombre(), eq.getPais(), eq.getPresupuesto());
        txtInfo.setText(info);
    }

    /**
     * Transfiere el control al MainFrame para arrancar el orquestador del torneo.
     */
    private void iniciarJuego() {
        int idx = listaEquipos.getSelectedIndex();
        if (idx >= 0) {
            try {
                frame.iniciarTorneo(equipos.get(idx));
            } catch (IOException e) {
                frame.setEstado("ERROR: No se pudo cargar el torneo.");
            }
        }
    }
}
