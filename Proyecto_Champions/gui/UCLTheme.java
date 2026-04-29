package gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

/**
 * Clase UCLTheme: ADN visual y Sistema de Diseño (Design System).
 * 
 * Centraliza la estética de la aplicación basándose en la identidad de la UEFA Champions League:
 * - Define la paleta de colores oficial (Azules profundos y Oro trofeo).
 * - Proporciona tipografías optimizadas para lectura deportiva.
 * - Ofrece "Factorías de Componentes" para botones premium y paneles de cristal (Glassmorphism).
 */
public final class UCLTheme {

    // ---------------------------------------------------------------------
    // BLOQUE: PALETA DE COLORES CORPORATIVA
    // ---------------------------------------------------------------------
    public static final Color DEEP_BLUE    = new Color(0,   12,  35);    // Fondo base estadio (Más profundo)
    public static final Color ROYAL_BLUE   = new Color(5,   25,  60);    // Azul real UCL
    public static final Color UCL_BLUE     = new Color(30,  100, 255);   // Azul eléctrico UCL
    public static final Color UCL_BLUE_LT  = new Color(0,   200, 255);   // Cyan neón (Accento)
    public static final Color UCL_SILVER   = new Color(220, 230, 245);   // Metal refinado
    public static final Color UCL_GOLD     = new Color(255, 215, 0);     // Oro trofeo brillante
    public static final Color UCL_MAGENTA  = new Color(230, 0,   120);   // Accento secundario UCL
    
    // Configuración para efectos de cristal (Glassmorphism)
    public static final Color GLASS_BG     = new Color(20,  40,  80, 180);
    public static final Color GLASS_BORDER = new Color(255, 255, 255, 40);
    
    // Semántica de estados (Refinados)
    public static final Color VERDE        = new Color(0,   230, 120);   // Positivo
    public static final Color ROJO         = new Color(255, 60,  80);    // Negativo
    public static final Color NARANJA      = new Color(255, 150,  0);    // Advertencia
    public static final Color GRIS_CLARO   = new Color(170, 190, 220);   // Texto informativo

    // ---------------------------------------------------------------------
    // BLOQUE: RECURSOS TIPOGRÁFICOS
    // ---------------------------------------------------------------------
    public static Font fontTitle(int size)  { return new Font("Segoe UI", Font.BOLD,  size); }
    public static Font fontBody(int size)   { return new Font("Segoe UI", Font.PLAIN, size); }
    public static Font fontMono(int size)   { return new Font("Consolas", Font.BOLD, size); }

    /**
     * Factory Method: uclButton
     * Crea un botón interactivo con respuesta visual al pasar el ratón (Hover).
     */
    public static JButton uclButton(String text, Color accent) {
        JButton btn = new JButton(text.toUpperCase()) {
            private float hoverProgress = 0f;
            private javax.swing.Timer timer;

            {
                setOpaque(false);
                setFocusPainted(false);
                setBorderPainted(false);
                setContentAreaFilled(false);
                setForeground(Color.WHITE);
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                setCursor(new Cursor(Cursor.HAND_CURSOR));
                setBorder(BorderFactory.createEmptyBorder(12, 32, 12, 32));

                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { animate(true); }
                    @Override public void mouseExited(MouseEvent e) { animate(false); }
                    private void animate(boolean in) {
                        if (timer != null && timer.isRunning()) timer.stop();
                        timer = new javax.swing.Timer(15, ev -> {
                            hoverProgress = in ? Math.min(1f, hoverProgress + 0.12f) : Math.max(0f, hoverProgress - 0.12f);
                            if (hoverProgress <= 0 || hoverProgress >= 1) ((javax.swing.Timer)ev.getSource()).stop();
                            repaint();
                        });
                        timer.start();
                    }
                });
            }

            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int w = getWidth(), h = getHeight();
                boolean pressed = getModel().isPressed();
                
                // Fondo con degradado
                Color baseColor = pressed ? accent.darker() : accent;
                GradientPaint gp = new GradientPaint(0, 0, baseColor, 0, h, baseColor.darker());
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, w, h, 12, 12);

                // Brillo Hover
                if (hoverProgress > 0) {
                    g2.setPaint(new Color(255, 255, 255, (int)(40 * hoverProgress)));
                    g2.fillRoundRect(0, 0, w, h, 12, 12);
                }

                // Borde
                g2.setStroke(new BasicStroke(1.5f));
                g2.setColor(new Color(255, 255, 255, (int)(40 + 60 * hoverProgress)));
                g2.drawRoundRect(1, 1, w-2, h-2, 12, 12);

                // Texto
                FontMetrics fm = g2.getFontMetrics();
                int tx = (w - fm.stringWidth(getText())) / 2;
                int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
                g2.setColor(Color.WHITE);
                g2.drawString(getText(), tx, ty);
                g2.dispose();
            }

            @Override public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                return new Dimension(d.width + 10, d.height); // Extra margin
            }
        };
        return btn;
    }

    /**
     * Factory Method: glowLabel
     * Etiqueta con fuente optimizada para títulos.
     */
    public static JLabel glowLabel(String text, Color color, int size, boolean bold) {
        JLabel l = new JLabel(text);
        l.setFont(bold ? fontTitle(size) : fontBody(size));
        l.setForeground(color);
        return l;
    }

    /**
     * Factory Method: glassPanel
     * Panel con efecto de cristal translúcido esmerilado.
     */
    public static JPanel glassPanel(LayoutManager layout) {
        return new JPanel(layout) {
            { setOpaque(false); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int w = getWidth(), h = getHeight();
                
                // Relleno suave con gradiente para profundidad
                g2.setPaint(new LinearGradientPaint(0, 0, 0, h, 
                    new float[]{0f, 0.7f, 1f}, 
                    new Color[]{new Color(25, 45, 95, 180), new Color(15, 25, 55, 210), new Color(5, 10, 30, 230)}));
                g2.fillRoundRect(0, 0, w, h, 20, 20);
                
                // Reflejo de brillo superior (Highlight)
                g2.setPaint(new LinearGradientPaint(0, 0, w, 40, 
                    new float[]{0f, 1f}, 
                    new Color[]{new Color(255, 255, 255, 25), new Color(255, 255, 255, 0)}));
                g2.fillRoundRect(2, 2, w-4, 40, 20, 20);

                // Contorno de alta definición
                g2.setPaint(new LinearGradientPaint(0, 0, w, h, 
                    new float[]{0f, 0.5f, 1f}, 
                    new Color[]{new Color(255,255,255,50), new Color(255, 255, 255, 10), new Color(255,255,255,50)}));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, w-1, h-1, 20, 20);
                
                g2.dispose();
            }
        };
    }

    /**
     * Utility: styleScrollBar
     * Personaliza el aspecto de las barras de scroll para que no desentonen.
     */
    public static void styleScrollBar(JScrollPane scroll) {
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
    }

    /**
     * Utility: setupTable (para JList)
     */
    public static void styleList(JList<?> list) {
        list.setOpaque(false);
        list.setBackground(new Color(0,0,0,0));
        list.setForeground(Color.WHITE);
        list.setSelectionBackground(new Color(UCL_BLUE.getRed(), UCL_BLUE.getGreen(), UCL_BLUE.getBlue(), 120));
        list.setSelectionForeground(UCL_GOLD);
        list.setFont(fontBody(14));
    }

    // Constructor privado para evitar instancias de una clase de utilidades
    private UCLTheme() {}
}
