// Francisco Lou Gardenberg - 2211275
// Vinicius Barros Pessoa de Araujo - 2210392

package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class LoginEmailPanel extends JPanel {

    private final JTextField campoEmail;
    private final JButton botaoContinuar;

    public LoginEmailPanel(ActionListener onContinuar) {
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        JLabel titulo = new JLabel("Autenticação - Etapa 1");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        titulo.setHorizontalAlignment(SwingConstants.CENTER);
        add(titulo, BorderLayout.NORTH);

        JPanel painelCentral = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        campoEmail = new JTextField(30);
        addLinha(painelCentral, gbc, 0, "Digite seu email:", campoEmail);

        add(painelCentral, BorderLayout.CENTER);

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        botaoContinuar = new JButton("Continuar");
        botaoContinuar.addActionListener(onContinuar);
        painelBotoes.add(botaoContinuar);

        add(painelBotoes, BorderLayout.SOUTH);
    }

    private void addLinha(JPanel panel, GridBagConstraints gbc, int linha, String labelTexto, Component campo) {
        gbc.gridx = 0;
        gbc.gridy = linha;
        gbc.weightx = 0.3;
        JLabel label = new JLabel(labelTexto);
        label.setFont(new Font("SansSerif", Font.PLAIN, 14));
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        panel.add(campo, gbc);
    }

    public String getEmail() {
        return campoEmail.getText().trim();
    }
}
