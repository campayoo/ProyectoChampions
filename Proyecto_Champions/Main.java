import gui.MainFrame;
import javax.swing.SwingUtilities;
import java.io.IOException;

/**
 * Clase Main: Punto de entrada oficial de "Proyecto Champions Elite".
 * 
 * Se encarga de inicializar el entorno gráfico y arrancar el motor del juego.
 */
public class Main {

    /**
     * Método principal (Bootstrap).
     * 
     * Implementa el patrón de diseño para aplicaciones Swing, delegando la 
     * construcción de la interfaz al Event Dispatch Thread (EDT) mediante 
     * SwingUtilities.invokeLater para evitar problemas de concurrencia.
     */
    public static void main(String[] args) {
        
        // BLOQUE: Inicialización Controlada de la UI
        SwingUtilities.invokeLater(() -> {
            try {
                // Instanciación de la ventana maestra que contiene toda la lógica visual
                MainFrame frame = new MainFrame();
                frame.setVisible(true); // Hace visible la aplicación al usuario
            } catch (IOException e) {
                // Gestión de errores críticos en la carga de recursos (imágenes, datos)
                System.err.println("❌ Error crítico al iniciar la aplicación: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
