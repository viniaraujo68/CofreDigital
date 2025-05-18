package ui;

import model.Usuario;
import security.Base32;
import security.TotpUtil;

import javax.crypto.SecretKey;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.function.BiConsumer;

public class LoginTotpPanel extends JPanel {

    private final JTextField campoCodigo;
    private final JLabel tentativasLabel;
    private int tentativas = 0;
    private final int MAX_TENTATIVAS = 3;

    public LoginTotpPanel(JFrame frame, Usuario usuario, String senha, BiConsumer<Usuario, Boolean> callback) {
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        JLabel titulo = new JLabel("Autenticação - Etapa 3");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        titulo.setHorizontalAlignment(SwingConstants.CENTER);
        add(titulo, BorderLayout.NORTH);

        JPanel painelCentral = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        campoCodigo = new JTextField(10);
        tentativasLabel = new JLabel("Tentativas restantes: " + (MAX_TENTATIVAS - tentativas));
        tentativasLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JButton verificarBtn = new JButton("Verificar Código");
        verificarBtn.addActionListener((ActionEvent e) -> {
            String codigo = campoCodigo.getText().trim();
            long codigoLong = Long.parseLong(codigo);

            boolean valido;
            try {
                // 1. Derivar chave AES da senha do usuário
                SecretKey chaveAES = security.CryptoUtil.gerarChaveAES(senha);

                // 2. Descriptografar chave TOTP em Base32
                byte[] chaveTotp = security.CryptoUtil.descriptografar(usuario.getTotpSecretoCriptografado(), chaveAES);
                Base32 base32 = new Base32(Base32.Alphabet.BASE32, false, false);
                if (chaveTotp == null) throw new IllegalArgumentException("Chave TOTP inválida");

                long agora = System.currentTimeMillis() / 1000L;
                long intervaloAtual = agora / 30;

                valido = false;
                for (int delta = -1; delta <= 1; delta++) {
                    long intervalo = intervaloAtual + delta;
                    long esperado = TotpUtil.gerarCodigo(chaveTotp, intervalo);
                    if (codigoLong == esperado) {
                        valido = true;
                        break;
                    }
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Erro na verificação do código TOTP.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (valido) {
                callback.accept(usuario, true);
            } else {
                tentativas++;
                if (tentativas >= MAX_TENTATIVAS) {
                    JOptionPane.showMessageDialog(this, "Código incorreto. Retornando à etapa 1.", "Erro", JOptionPane.ERROR_MESSAGE);
                    callback.accept(usuario, false);
                } else {
                    tentativasLabel.setText("Tentativas restantes: " + (MAX_TENTATIVAS - tentativas));
                    JOptionPane.showMessageDialog(this, "Código incorreto.", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        int linha = 0;
        addLinha(painelCentral, gbc, linha++, "Código TOTP:", campoCodigo);
        gbc.gridx = 0;
        gbc.gridy = linha++;
        gbc.gridwidth = 2;
        painelCentral.add(verificarBtn, gbc);

        gbc.gridy = linha;
        painelCentral.add(tentativasLabel, gbc);

        add(painelCentral, BorderLayout.CENTER);
    }

    private void addLinha(JPanel panel, GridBagConstraints gbc, int linha, String labelTexto, Component campo) {
        gbc.gridx = 0;
        gbc.gridy = linha;
        gbc.weightx = 0.3;
        gbc.gridwidth = 1;
        JLabel label = new JLabel(labelTexto);
        label.setFont(new Font("SansSerif", Font.PLAIN, 14));
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        panel.add(campo, gbc);
    }
}
