import gui.MainFrame;
import javax.swing.SwingUtilities;

/**
 * Main — punto de entrada de la aplicación.
 * Lanza la ventana principal en el hilo de despacho de eventos de Swing
 * (Event Dispatch Thread) para garantizar la seguridad de la interfaz gráfica.
 */
public class Main {

    public static void main(String[] args) {
        // SwingUtilities.invokeLater asegura que toda la UI se crea en el EDT
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
