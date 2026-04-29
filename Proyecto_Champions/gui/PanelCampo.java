package gui;

import model.Jugador;
import model.Jugador.Compatibilidad;
import model.Equipo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

import static gui.UCLTheme.*;

/**
 * Clase PanelCampo: Lienzo interactivo del terreno de juego.
 * 
 * Este componente es el centro neurálgico la gestión táctica. Permite:
 * - Visualizar la disposición de los jugadores en el césped.
 * - Gestionar cambios y posiciones mediante Drag & Drop (Arrastrar y Soltar).
 * - Obtener feedback inmediato del OVR real según la posición ocupada.
 * - Ver animaciones tácticas y efectos visuales de alta fidelidad.
 */
public class PanelCampo extends JPanel {

    // --- BLOQUE: PALETA DE COLORES TÁCTICA ---
    private static final Color BG_FIELD_TOP    = DEEP_BLUE;
    private static final Color BG_FIELD_BTM    = ROYAL_BLUE;
    private static final Color CESPED_1        = new Color(20, 70,  40);
    private static final Color CESPED_2        = new Color(15, 60,  35);
    private static final Color LINEAS_CAMPO    = new Color(255, 255, 255, 60);
    private static final Color OVR_NATURAL     = VERDE;
    private static final Color OVR_AFIN        = NARANJA;
    private static final Color OVR_OPUESTA     = ROJO;

    // --- BLOQUE: ESTADO INTERNO Y GESTIÓN DE NODOS ---
    private Equipo                equipo;
    private List<Jugador>         titulares;
    private List<Jugador>         suplentes;
    private List<NodoPosicion>    nodos = new ArrayList<>();
    
    private Jugador               seleccionado;
    private Jugador               arrastrando;
    private Point                 dragPoint;
    private NodoPosicion          nodoResaltado;
    private SelectionListener     selectionListener;
    private String                formacion;

    /**
     * NodoPosicion: Define un slot táctico o 'hueco' en el campo.
     */
    public static class NodoPosicion {
        public final String etiqueta;      // Nombre del rol (ej: 'MCD')
        public final int relX, relY;       // Ubicación relativa (0-100)
        public Jugador jugadorAsignado;    // Activo que ocupa el slot

        public NodoPosicion(String etiqueta, int relX, int relY) {
            this.etiqueta = etiqueta;
            this.relX     = relX;
            this.relY     = relY;
        }

        /** Convierte coordenadas porcentuales a píxeles reales. */
        public Point toPixel(int w, int h) {
            int fieldH = h - 130; // Descontamos el área del banquillo
            return new Point((int)(relX * w / 100.0), (int)(relY * fieldH / 100.0));
        }
    }

    public interface SelectionListener { void onPlayerSelected(Jugador j); }

    /**
     * Constructor: Configura el tapete verde y habilita los eventos de ratón.
     */
    public PanelCampo(Equipo equipo, SelectionListener listener) {
        this.equipo = equipo;
        this.selectionListener = listener;
        this.formacion = equipo.getFormacion();
        
        setBackground(BG_FIELD_TOP);
        generarNodosFormacion(formacion);
        configurarEventosInteractivos();
        refreshData();
    }

    // ---------------------------------------------------------------------
    // BLOQUE: LÓGICA DE ACTUALIZACIÓN TÁCTICA
    // ---------------------------------------------------------------------

    /** Sincroniza las listas de jugadores locales con el modelo. */
    public void refreshData() {
        if (equipo == null) return;
        this.titulares = equipo.getTitulares();
        this.suplentes = equipo.getSuplentes();
        asignarJugadoresANodos();
        repaint();
    }

    public void setFormacion(String f) {
        this.formacion = f;
        generarNodosFormacion(f);
        asignarJugadoresANodos();
        repaint();
    }

    /** Vincula cada titular con su nodo correspondiente en la pizarra. */
    public void asignarJugadoresANodos() {
        for (NodoPosicion n : nodos) n.jugadorAsignado = null;
        for (Jugador j : titulares) {
            for (NodoPosicion n : nodos) {
                if (n.jugadorAsignado == null && n.etiqueta.equals(j.getPosicionNodo())) {
                    n.jugadorAsignado = j;
                    break;
                }
            }
        }
    }

    private void generarNodosFormacion(String f) {
        nodos.clear();
        nodos.add(new NodoPosicion("POR", 50, 85)); // Arquero
        int[] cuotas = parseFormacion(f);

        // 1. Línea Defensiva
        generarLinea(cuotas[1], 70, new String[]{"LD", "DFC", "DFC", "LI"});
        
        // 2. Línea Medular
        generarLinea(cuotas[2], 45, new String[]{"MD", "MC", "MC", "MI"});
        
        // 3. Línea de Ataque
        generarLinea(cuotas[3], 20, new String[]{"ED", "DC", "DC", "EI"});
    }

    private void generarLinea(int num, int y, String[] labelsBase) {
        if (num <= 0) return;
        // Seleccionamos las etiquetas según el número solicitado
        String[] finales = new String[num];
        if (num == 1) {
            finales[0] = labelsBase[1]; // Central/Medio
        } else if (num == 2) {
            finales[0] = labelsBase[1]; finales[1] = labelsBase[2];
        } else if (num == 3) {
            finales[0] = labelsBase[0]; finales[1] = labelsBase[1]; finales[2] = labelsBase[3];
        } else if (num == 4) {
            finales = labelsBase;
        } else {
            // Para 5 o más, repetimos centrales
            finales[0] = labelsBase[0];
            finales[num-1] = labelsBase[3];
            for(int i=1; i<num-1; i++) finales[i] = labelsBase[1];
        }

        // Distribución X (15% a 85% del ancho)
        for (int i = 0; i < num; i++) {
            double x = (num == 1) ? 50 : 15 + (70.0 / (num - 1)) * i;
            nodos.add(new NodoPosicion(finales[i], (int)x, y));
        }
    }

    // ---------------------------------------------------------------------
    // BLOQUE: SISTEMA DRAG & DROP (MANEJO DE EVENTOS)
    // ---------------------------------------------------------------------

    private void configurarEventosInteractivos() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Bloque: Detección de inicio de arrastre
                arrastrando = getJugadorEn(e.getX(), e.getY());
                if (arrastrando != null) {
                    dragPoint = e.getPoint();
                    setSeleccionado(arrastrando);
                } else {
                    setSeleccionado(null);
                }
                if (selectionListener != null) selectionListener.onPlayerSelected(seleccionado);
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // Bloque: Resolución del destino del jugador
                if (arrastrando != null) {
                    NodoPosicion dest = getNodoCercano(e.getX(), e.getY(), 65);
                    procesarIntercambio(dest);
                    arrastrando = null;
                    dragPoint = null;
                    nodoResaltado = null;
                    refreshData();
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                // Bloque: Feedback visual durante el desplazamiento
                if (arrastrando != null) {
                    dragPoint = e.getPoint();
                    nodoResaltado = getNodoCercano(e.getX(), e.getY(), 60);
                    repaint();
                }
            }
        });
    }

    private void procesarIntercambio(NodoPosicion destino) {
        if (destino == null) return;
        
        Jugador jSale = destino.jugadorAsignado;
        Jugador jEntra = arrastrando;

        if (suplentes.contains(jEntra)) {
            // Acción: Sustitución (Cambio Suplente -> Titular)
            if (jSale != null) {
                if (equipo.realizarCambio(jSale, jEntra)) {
                    jEntra.setPosicionNodo(destino.etiqueta);
                }
            } else {
                // Si el slot estaba vacío, simplemente lo movemos a titulares
                jEntra.setTitular(true);
                jEntra.setPosicionNodo(destino.etiqueta);
                equipo.getTitulares().add(jEntra);
            }
        } else {
            // Acción: Movimiento Táctico (Reubicación interna)
            if (jSale != null) {
                String labelOrigen = getLabelDeJugador(jEntra);
                jEntra.setPosicionNodo(destino.etiqueta);
                jSale.setPosicionNodo(labelOrigen);
            } else {
                jEntra.setPosicionNodo(destino.etiqueta);
            }
        }
    }

    // ---------------------------------------------------------------------
    // BLOQUE: MOTOR DE RENDERIZADO (GDI+)
    // ---------------------------------------------------------------------

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();
        int fieldH = h - 130;

        // Capa 1: Dibujo del Césped Realista
        dibujarCampo(g2, w, fieldH);
        
        // Capa 2: Representación de Nodos y Cartas de Jugador
        for (NodoPosicion n : nodos) {
            Point p = n.toPixel(w, h);
            if (n.jugadorAsignado != null && n.jugadorAsignado != arrastrando) {
                dibujarCarta(g2, p.x, p.y, n.jugadorAsignado, n.jugadorAsignado == seleccionado, n.etiqueta);
            } else {
                dibujarSlotVacio(g2, p.x, p.y, n == nodoResaltado, n.etiqueta);
            }
        }

        // Capa 3: Área de Reservas (Banquillo)
        dibujarBanquillo(g2, w, h);

        // Capa 4: Renderizado de Arrastre (Overlay)
        if (arrastrando != null && dragPoint != null) {
            dibujarCarta(g2, dragPoint.x, dragPoint.y, arrastrando, true, arrastrando.getPosicionNodo());
        }
    }

    private void dibujarCampo(Graphics2D g2, int w, int h) {
        // Bloque: Césped con patrones de corte y degradado
        g2.setPaint(new GradientPaint(0, 0, BG_FIELD_TOP, 0, h, BG_FIELD_BTM));
        g2.fillRect(0, 0, w, h);
        
        for (int i = 0; i < 10; i++) {
            g2.setColor(i % 2 == 0 ? CESPED_1 : CESPED_2);
            g2.fillRect(i * w / 10, 0, w / 10, h);
        }
        
        // Líneas reglamentarias (atenuadas)
        g2.setColor(LINEAS_CAMPO);
        g2.drawRect(20, 20, w - 40, h - 40);
        g2.drawOval(w/2-60, h/2-60, 120, 120);

        // Líneas Divisorias de Bloques (Tácticas)
        float[] dash1 = {10.0f};
        g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f));
        g2.setColor(new Color(255, 255, 255, 30));
        
        int hDef = (int)(h * 0.77); // Límite defensa
        int hMed = (int)(h * 0.52); // Límite medios
        int hDel = (int)(h * 0.27); // Límite ataque

        g2.drawLine(20, hDef, w - 20, hDef);
        g2.drawLine(20, hMed, w - 20, hMed);
        g2.drawLine(20, hDel, w - 20, hDel);
    }

    private void dibujarBanquillo(Graphics2D g2, int w, int h) {
        int y = h - 130;
        
        // Efecto de cristal para el banquillo
        g2.setPaint(new LinearGradientPaint(0, y, 0, h, new float[]{0f, 1f}, new Color[]{new Color(5, 15, 45, 220), new Color(0, 5, 20, 250)}));
        g2.fillRect(0, y, w, 130);
        
        g2.setColor(UCL_BLUE);
        g2.setStroke(new BasicStroke(2));
        g2.drawLine(0, y, w, y); // Separador táctico
        
        int x = 50;
        for (Jugador s : suplentes) {
            if (s != arrastrando) {
                dibujarCarta(g2, x, y + 65, s, s == seleccionado, s.getPosicion());
                x += 65;
            }
        }
    }

    private void dibujarCarta(Graphics2D g2, int x, int y, Jugador j, boolean sel, String tag) {
        Color cOvr = getColorOvr(j.getCompatibilidadConNodo(tag));
        
        if (sel) {
            g2.setPaint(new RadialGradientPaint(x, y, 40, new float[]{0f, 1f}, new Color[]{new Color(UCL_GOLD.getRed(), UCL_GOLD.getGreen(), UCL_GOLD.getBlue(), 100), new Color(0,0,0,0)}));
            g2.fillOval(x - 40, y - 40, 80, 80);
        }

        // Cuerpo de la carta (Hexágono o círculo estilizado)
        g2.setPaint(new LinearGradientPaint(x-22, y-22, x+22, y+22, new float[]{0f, 1f}, new Color[]{new Color(255,255,255,250), new Color(220,230,255)}));
        g2.fillOval(x - 22, y - 22, 44, 44);
        
        g2.setColor(cOvr);
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawOval(x - 22, y - 22, 44, 44);

        // Valoración
        g2.setColor(new Color(0, 10, 40));
        g2.setFont(fontTitle(16));
        String ovr = String.valueOf(j.getOvrEnPosicion());
        g2.drawString(ovr, x - g2.getFontMetrics().stringWidth(ovr)/2, y + 6);

        // Nombre con fondo premium
        g2.setPaint(new LinearGradientPaint(x-30, y+25, x+30, y+25, new float[]{0f, 0.5f, 1f}, new Color[]{new Color(0,0,0,0), new Color(0,0,0,180), new Color(0,0,0,0)}));
        g2.fillRect(x - 40, y + 27, 80, 16);
        
        g2.setColor(Color.WHITE);
        g2.setFont(fontBody(11));
        String nom = j.getNombre().toUpperCase();
        if (nom.length() > 10) nom = nom.substring(0, 9) + ".";
        g2.drawString(nom, x - g2.getFontMetrics().stringWidth(nom)/2, y + 39);
    }

    private void dibujarSlotVacio(Graphics2D g2, int x, int y, boolean hover, String tag) {
        g2.setColor(hover ? new Color(0, 120, 255, 120) : new Color(255, 255, 255, 45));
        g2.fillOval(x - 15, y - 15, 30, 30);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 9));
        g2.drawString(tag, x - g2.getFontMetrics().stringWidth(tag)/2, y + 30);
    }

    // ---------------------------------------------------------------------
    // BLOQUE: CÁLCULOS GEOMÉTRICOS Y PARSING
    // ---------------------------------------------------------------------

    private Jugador getJugadorEn(int x, int y) {
        // Búsqueda en el 11 inicial
        for (NodoPosicion n : nodos) {
            Point p = n.toPixel(getWidth(), getHeight());
            if (n.jugadorAsignado != null && p.distance(x, y) < 30) return n.jugadorAsignado;
        }
        // Búsqueda en el banquillo lateral
        int bY = getHeight() - 130;
        if (y > bY) {
            int sx = 50;
            for (Jugador s : suplentes) {
                if (new Point(sx, bY + 65).distance(x, y) < 30) return s;
                sx += 65;
            }
        }
        return null;
    }

    private NodoPosicion getNodoCercano(int x, int y, int radio) {
        for (NodoPosicion n : nodos) {
            if (n.toPixel(getWidth(), getHeight()).distance(x, y) < radio) return n;
        }
        return null;
    }

    private String getLabelDeJugador(Jugador j) {
        for (NodoPosicion n : nodos) if (n.jugadorAsignado == j) return n.etiqueta;
        return j.getPosicion();
    }

    private Color getColorOvr(Compatibilidad c) {
        switch (c) {
            case NATURAL:
                return OVR_NATURAL;
            case AFIN:
                return OVR_AFIN;
            default:
                return OVR_OPUESTA;
        }
    }

    private void setSeleccionado(Jugador j) { this.seleccionado = j; }

    private int[] parseFormacion(String f) {
        try {
            String[] p = f.split("-");
            return new int[]{ 1, Integer.parseInt(p[0]), Integer.parseInt(p[1]), Integer.parseInt(p[2]) };
        } catch (Exception e) { return new int[]{1, 4, 4, 2}; }
    }
}
