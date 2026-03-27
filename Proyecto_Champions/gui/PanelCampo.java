package gui;

import model.Jugador;
import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PanelCampo — representa visualmente el campo de fútbol con los jugadores (estilo FIFA).
 */
public class PanelCampo extends JPanel {
    private final Color VERDE_CESPED = new Color(34, 139, 34);
    private final Color LINEAS_CAMPO = new Color(255, 255, 255, 180);
    
    private List<Jugador> titulares;
    private Map<String, Point> posicionesCoordenadas = new HashMap<>();
    private Jugador seleccionado;
    private SelectionListener selectionListener;

    public interface SelectionListener {
        void onPlayerSelected(Jugador j);
    }

    public PanelCampo(List<Jugador> titulares, SelectionListener listener) {
        this.titulares = titulares;
        this.selectionListener = listener;
        setBackground(VERDE_CESPED);
        initCoordenadas();
    }

    public void setTitulares(List<Jugador> titulares) {
        this.titulares = titulares;
        repaint();
    }

    public void setSeleccionado(Jugador j) {
        this.seleccionado = j;
        repaint();
    }

    private void initCoordenadas() {
        // Coordenadas relativas (0-100) para un campo vertical
        // Portero
        posicionesCoordenadas.put("POR", new Point(50, 90));
        
        // Defensas
        posicionesCoordenadas.put("LD",  new Point(85, 70));
        posicionesCoordenadas.put("LI",  new Point(15, 70));
        posicionesCoordenadas.put("DFC", new Point(50, 75));
        posicionesCoordenadas.put("DFCI",new Point(35, 75));
        posicionesCoordenadas.put("DFCD",new Point(65, 75));
        posicionesCoordenadas.put("DEF", new Point(50, 75)); // fallback

        // Medios
        posicionesCoordenadas.put("MC",  new Point(50, 45));
        posicionesCoordenadas.put("MCI", new Point(30, 45));
        posicionesCoordenadas.put("MCR", new Point(70, 45)); // Antes MCD
        posicionesCoordenadas.put("MCO", new Point(50, 35));
        posicionesCoordenadas.put("MCD", new Point(50, 58)); // Mediocentro Defensivo (atrasado)
        posicionesCoordenadas.put("MD",  new Point(85, 45));
        posicionesCoordenadas.put("MI",  new Point(15, 45));
        posicionesCoordenadas.put("MED", new Point(50, 45)); // fallback

        // Delanteros
        posicionesCoordenadas.put("DC",  new Point(50, 15));
        posicionesCoordenadas.put("DCI", new Point(35, 15));
        posicionesCoordenadas.put("DCD", new Point(65, 15));
        posicionesCoordenadas.put("ED",  new Point(85, 20));
        posicionesCoordenadas.put("EI",  new Point(15, 20));
        posicionesCoordenadas.put("DEL", new Point(50, 15)); // fallback
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Dibujar campo
        g2.setColor(LINEAS_CAMPO);
        g2.setStroke(new BasicStroke(2));
        g2.drawRect(10, 10, w - 20, h - 20); // Banda
        g2.drawLine(10, h / 2, w - 10, h / 2); // Medio campo
        g2.drawOval(w / 2 - 50, h / 2 - 50, 100, 100); // Círculo central
        
        // Áreas
        g2.drawRect(w / 2 - 80, 10, 160, 60); // Área local superior
        g2.drawRect(w / 2 - 80, h - 70, 160, 60); // Área local inferior

        // Dibujar jugadores
        if (titulares == null) return;

        // Contador para posiciones duplicadas (ej: 2 DFCs)
        Map<String, Integer> usados = new HashMap<>();

        for (Jugador j : titulares) {
            String pos = j.getPosicion();
            int offset = usados.getOrDefault(pos, 0);
            usados.put(pos, offset + 1);

            Point pRel = getCoordenadaEspecial(pos, offset);
            int x = (int) (pRel.x * w / 100.0);
            int y = (int) (pRel.y * h / 100.0);

            dibujarJugador(g2, x, y, j);
        }
    }

    private Point getCoordenadaEspecial(String pos, int occ) {
        // Lógica para separar jugadores con la misma etiqueta de posición (ej: dos DFC)
        if (pos.equals("DFC") && occ == 1) return posicionesCoordenadas.getOrDefault("DFCD", new Point(65, 75));
        if (pos.equals("DFC") && occ == 0) return posicionesCoordenadas.getOrDefault("DFCI", new Point(35, 75));
        
        // Mediocentros
        if (pos.equals("MC") && occ == 1)  return posicionesCoordenadas.getOrDefault("MCR", new Point(70, 45));
        if (pos.equals("MC") && occ == 0)  return posicionesCoordenadas.getOrDefault("MCI", new Point(30, 45));
        if (pos.equals("MC") && occ == 2)  return posicionesCoordenadas.getOrDefault("MC",  new Point(50, 45));
        
        // Mediocentro Defensivo (MCD en el modelo y en el FIFA)
        if (pos.equals("MCD")) return posicionesCoordenadas.getOrDefault("MCD", new Point(50, 58));
        
        return posicionesCoordenadas.getOrDefault(pos, new Point(50, 50));
    }

    private void dibujarJugador(Graphics2D g2, int x, int y, Jugador j) {
        boolean isSel = (seleccionado != null && seleccionado.getId() == j.getId());
        
        // Sombra
        g2.setColor(new Color(0, 0, 0, 50));
        g2.fillOval(x - 22, y - 22, 44, 44);

        // Círculo base
        g2.setColor(isSel ? new Color(255, 215, 0) : Color.WHITE);
        g2.fillOval(x - 20, y - 20, 40, 40);
        
        g2.setColor(new Color(0, 51, 102)); // Azul UCL
        g2.setStroke(new BasicStroke(isSel ? 3 : 1));
        g2.drawOval(x - 20, y - 20, 40, 40);

        // Media
        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        String media = String.valueOf(j.getMediaGeneral());
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(media, x - fm.stringWidth(media) / 2, y + 5);

        // Nombre y Posición
        g2.setFont(new Font("SansSerif", Font.BOLD, 10));
        g2.setColor(Color.WHITE);
        String txt = j.getPosicion() + " " + j.getNombre();
        fm = g2.getFontMetrics();
        
        // Fondo texto
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(x - fm.stringWidth(txt)/2 - 4, y + 22, fm.stringWidth(txt) + 8, 14);
        
        g2.setColor(Color.WHITE);
        g2.drawString(txt, x - fm.stringWidth(txt) / 2, y + 33);
        
        // Área clicable invisible (simplificada: el paint solo dibuja)
    }

    // El manejo de clicks se hará desde PanelAlineacion usando las coordenadas calculadas
    public Jugador getJugadorEn(int mouseX, int mouseY) {
        int w = getWidth();
        int h = getHeight();
        Map<String, Integer> usados = new HashMap<>();
        
        for (Jugador j : titulares) {
            String pos = j.getPosicion();
            int offset = usados.getOrDefault(pos, 0);
            usados.put(pos, offset + 1);

            Point pRel = getCoordenadaEspecial(pos, offset);
            int x = (int) (pRel.x * w / 100.0);
            int y = (int) (pRel.y * h / 100.0);

            if (Math.hypot(mouseX - x, mouseY - y) < 25) {
                return j;
            }
        }
        return null;
    }
}
