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
    public static final Color DEEP_BLUE    = new Color(0,   24,  55);    // Fondo base estadio
    public static final Color ROYAL_BLUE   = new Color(1,   14,  31);    // Oscuros de viñeta
    public static final Color UCL_BLUE     = new Color(0,   100, 255);   // Azul eléctrico UCL
    public static final Color UCL_BLUE_LT  = new Color(30,  130, 255);   // Iluminación neón
    public static final Color UCL_SILVER   = new Color(192, 205, 220);   // Acabado metal
    public static final Color UCL_GOLD     = new Color(255, 210, 0);     // Color del trofeo
    
    // Configuración para efectos de cristal (Glassmorphism)
    public static final Color GLASS_BG     = new Color(15,  25,  60, 190);
    public static final Color GLASS_BORDER = new Color(255, 255, 255, 60);
    
    // Semántica de estados
    public static final Color VERDE        = new Color(0,   220, 110);   // Positivo / Clasificado
    public static final Color ROJO         = new Color(230,  50,  50);   // Negativo / Eliminado
    public static final Color NARANJA      = new Color(255, 140,   0);   // Alerta
    public static final Color GRIS_CLARO   = new Color(180, 195, 230);   // Texto informativo

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
                setFont(fontTitle(13));
                setCursor(new Cursor(Cursor.HAND_CURSOR));
                setBorder(BorderFactory.createEmptyBorder(12, 28, 12, 28));

                // Lógica de animación suave para el hover
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { animate(true); }
                    @Override public void mouseExited(MouseEvent e) { animate(false); }
                    
                    private void animate(boolean in) {
                        if (timer != null && timer.isRunning()) timer.stop();
                        timer = new javax.swing.Timer(15, ev -> {
                            hoverProgress = in ? Math.min(1f, hoverProgress + 0.15f) : Math.max(0f, hoverProgress - 0.15f);
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
                
                // Color dinámico según el progreso de la animación
                int alpha = (int)(200 + (55 * hoverProgress));
                Color c = new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), alpha);
                
                // Dibujo del cuerpo del botón (Redondeado)
                g2.setPaint(new GradientPaint(0, 0, c, 0, h, c.darker()));
                g2.fillRoundRect(0, 0, w, h, 8, 8);
                
                // Borde brillante
                g2.setColor(new Color(255, 255, 255, (int)(50 + 70 * hoverProgress)));
                g2.drawRoundRect(0, 0, w-1, h-1, 8, 8);

                // Dibujado del texto central
                FontMetrics fm = g2.getFontMetrics();
                int x = (w - fm.stringWidth(getText())) / 2;
                int y = (h - fm.getHeight()) / 2 + fm.getAscent();
                
                g2.setColor(Color.WHITE);
                g2.drawString(getText(), x, y);
                g2.dispose();
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
                    new Color[]{new Color(25, 40, 85, 200), new Color(15, 25, 60, 220), new Color(5, 10, 30, 240)}));
                g2.fillRoundRect(0, 0, w, h, 15, 15);
                
                // Reflejo de brillo superior (Highlight)
                g2.setPaint(new LinearGradientPaint(0, 0, 0, 25, 
                    new float[]{0f, 1f}, 
                    new Color[]{new Color(255, 255, 255, 30), new Color(255, 255, 255, 0)}));
                g2.fillRoundRect(2, 2, w-4, 25, 15, 15);

                // Contorno de alta definición con gradiente de luz
                g2.setPaint(new LinearGradientPaint(0, 0, w, h, 
                    new float[]{0f, 0.5f, 1f}, 
                    new Color[]{GLASS_BORDER, new Color(255, 255, 255, 10), GLASS_BORDER}));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, w-1, h-1, 15, 15);
                
                g2.dispose();
            }
        };
    }

    // Constructor privado para evitar instancias de una clase de utilidades
    private UCLTheme() {}
}
