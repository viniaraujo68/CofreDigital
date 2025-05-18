package ui;

import model.Usuario;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class CadastroPanel extends JPanel {

    private final JLabel labelCertificado;
    private final JLabel labelChavePrivada;
    private final JButton botaoSelecionarCertificado;
    private final JButton botaoSelecionarChave;
    private final JPasswordField campoFraseSecreta;
    private final JComboBox<String> campoGrupo;
    private final JPasswordField campoSenha;
    private final JPasswordField campoConfirmarSenha;
    private final JButton botaoCadastrar;
    private final JButton botaoVoltar;

    public CadastroPanel(ActionListener onCadastrar, ActionListener onVoltar, ActionListener onSelecionarCert, ActionListener onSelecionarChave, Usuario usuario) {
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        JLabel titulo = new JLabel("Cadastre-se");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        titulo.setHorizontalAlignment(SwingConstants.CENTER);
        add(titulo, BorderLayout.NORTH);

        JPanel painelCentral = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        labelCertificado = new JLabel("Nenhum certificado selecionado");
        botaoSelecionarCertificado = new JButton("Selecionar Certificado");
        botaoSelecionarCertificado.addActionListener(onSelecionarCert);

        labelChavePrivada = new JLabel("Nenhuma chave selecionada");
        botaoSelecionarChave = new JButton("Selecionar Chave Privada");
        botaoSelecionarChave.addActionListener(onSelecionarChave);

        campoFraseSecreta = new JPasswordField(20);
        if (usuario == null) {
            System.out.println("oferecerá só administrador");
            campoGrupo = new JComboBox<>(new String[]{"Administrador"});
        } else if (usuario.getGrupoId() == 1) {
            System.out.println("oferecerá administrador e usuario");
            campoGrupo = new JComboBox<>(new String[]{"Administrador", "Usuario"});
        } else {
            System.out.println("oferecerá só usuário");
            campoGrupo = new JComboBox<>(new String[]{"Usuario"});
        }

        campoSenha = new JPasswordField(20);
        campoConfirmarSenha = new JPasswordField(20);

        int linha = 0;

        addLinha(painelCentral, gbc, linha++, "Certificado Digital:", botaoSelecionarCertificado);
        addLinha(painelCentral, gbc, linha++, "Caminho selecionado:", labelCertificado);
        addLinha(painelCentral, gbc, linha++, "Chave Privada:", botaoSelecionarChave);
        addLinha(painelCentral, gbc, linha++, "Caminho selecionado:", labelChavePrivada);
        addLinha(painelCentral, gbc, linha++, "Frase Secreta:", campoFraseSecreta);
        addLinha(painelCentral, gbc, linha++, "Grupo:", campoGrupo);
        addLinha(painelCentral, gbc, linha++, "Senha Pessoal:", campoSenha);
        addLinha(painelCentral, gbc, linha++, "Confirmar Senha:", campoConfirmarSenha);

        add(painelCentral, BorderLayout.CENTER);

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        botaoCadastrar = new JButton("Cadastrar");
        botaoCadastrar.addActionListener(onCadastrar);
        botaoVoltar = new JButton("Voltar");
        if (usuario != null) {
            botaoVoltar.addActionListener(onVoltar);
            painelBotoes.add(botaoVoltar);
        }
        painelBotoes.add(botaoCadastrar);

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

    // Getters
    public JPasswordField getCampoFraseSecreta() { return campoFraseSecreta; }
    public JComboBox<String> getCampoGrupo() { return campoGrupo; }
    public JPasswordField getCampoSenha() { return campoSenha; }
    public JPasswordField getCampoConfirmarSenha() { return campoConfirmarSenha; }
    public JLabel getLabelCertificado() { return labelCertificado; }
    public JLabel getLabelChavePrivada() { return labelChavePrivada; }
    public JButton getBotaoCadastrar() { return botaoCadastrar; }
    public JButton getBotaoVoltar() { return botaoVoltar; }
    public JButton getBotaoSelecionarCertificado() { return botaoSelecionarCertificado; }
    public JButton getBotaoSelecionarChave() { return botaoSelecionarChave; }
}
