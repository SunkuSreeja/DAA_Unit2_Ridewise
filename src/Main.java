import ui.MainFrame;
import javax.swing.*;

/**
 * RideWise — Smart Cab Management System
 * Entry point for the application.
 *
 * Compile:
 *   javac -d out -sourcepath src src/Main.java
 *
 * Run:
 *   java -cp out Main
 */
public class Main {
    public static void main(String[] args) {
        // Run UI on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
