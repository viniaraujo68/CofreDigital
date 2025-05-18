package cofre;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CofreApp app = new CofreApp();
            app.setVisible(true);
        });
    }
}
