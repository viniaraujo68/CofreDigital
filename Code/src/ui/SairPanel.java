// Francisco Lou Gardenberg - 2211275
// Vinicius Barros Pessoa de Araujo - 2210392

package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class SairPanel extends JPanel {

    private final JButton encerrarSessaoBtn;
    private final JButton encerrarSistemaBtn;
    private final JButton voltarMenuBtn;

    public SairPanel(ActionListener encerrarSessaoAction, ActionListener encerrarSistemaAction, ActionListener voltarMenuAction) {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel mensagem = new JLabel("Pressione o botão Encerrar Sessão ou o botão Encerrar Sistema para confirmar.");
        mensagem.setFont(new Font("Arial", Font.BOLD, 14));
        mensagem.setHorizontalAlignment(SwingConstants.CENTER);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(10, 10, 20, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        add(mensagem, gbc);

        encerrarSessaoBtn = new JButton("Encerrar Sessão");
        encerrarSistemaBtn = new JButton("Encerrar Sistema");
        voltarMenuBtn = new JButton("Voltar ao Menu");

        // Tamanho vertical reforçado (largura mínima, altura maior)
        Dimension tamanhoBotao = new Dimension(100, 50); // altura 50px

        encerrarSessaoBtn.setPreferredSize(tamanhoBotao);
        encerrarSistemaBtn.setPreferredSize(tamanhoBotao);
        voltarMenuBtn.setPreferredSize(new Dimension((int) (tamanhoBotao.width * 2 * 0.66), 50));

        // Botão: Encerrar Sessão
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.weightx = 0.5;
        gbc.insets = new Insets(5, 10, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(encerrarSessaoBtn, gbc);

        // Botão: Encerrar Sistema
        gbc.gridx = 1;
        gbc.insets = new Insets(5, 5, 5, 10);
        add(encerrarSistemaBtn, gbc);

        // Placeholder invisível para alinhar botão inferior
        gbc.gridx = 2;
        gbc.weightx = 0.0;
        add(Box.createHorizontalStrut(0), gbc);

        // Botão: Voltar ao Menu
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 40, 10, 40);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        add(voltarMenuBtn, gbc);

        // Ações externas
        encerrarSessaoBtn.addActionListener(encerrarSessaoAction);
        encerrarSistemaBtn.addActionListener(encerrarSistemaAction);
        voltarMenuBtn.addActionListener(voltarMenuAction);
    }

    public JButton getBotaoEncerrarSessao() { return encerrarSessaoBtn; }
    public JButton getBotaoEncerrarSistema() { return encerrarSistemaBtn; }
    public JButton getBotaoVoltar() { return voltarMenuBtn; }
}
