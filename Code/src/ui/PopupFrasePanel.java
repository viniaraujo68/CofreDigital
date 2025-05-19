// Francisco Lou Gardenberg - 2211275
// Vinicius Barros Pessoa de Araujo - 2210392

package ui;

import javax.swing.*;
import java.awt.*;

public class PopupFrasePanel {

    /**
     * Exibe um popup modal solicitando a frase secreta ao usuário.
     *
     * @param parent componente pai (pode ser JFrame ou null)
     * @return a frase digitada ou null se o usuário cancelar
     */
    public static String solicitarFraseSecreta(Component parent) {
        JPasswordField campoSenha = new JPasswordField(20);
        campoSenha.setEchoChar('*');

        JPanel painel = new JPanel(new BorderLayout(10, 10));
        painel.add(new JLabel("Digite sua frase secreta:"), BorderLayout.NORTH);
        painel.add(campoSenha, BorderLayout.CENTER);

        int resultado = JOptionPane.showConfirmDialog(
                parent,
                painel,
                "Frase Secreta",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (resultado == JOptionPane.OK_OPTION) {
            return new String(campoSenha.getPassword());
        } else {
            return null;
        }
    }
}