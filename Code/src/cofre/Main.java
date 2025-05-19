// Francisco Lou Gardenberg - 2211275
// Vinicius Barros Pessoa de Araujo - 2210392

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
