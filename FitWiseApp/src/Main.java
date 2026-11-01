import gui.MainApp;

import javax.swing.SwingUtilities;

/**
 * Entry point. Run this class to start the application.
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainApp().setVisible(true));
    }
}
