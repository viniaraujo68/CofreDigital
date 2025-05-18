package ui;

import Database.DAO;
import model.Usuario;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class TelaInicialPanel extends JPanel {

    public TelaInicialPanel(Usuario usuario, ActionListener onCadastro, ActionListener onConsulta, ActionListener onSair) {

        DAO dao = DAO.getInstance();
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        // Cabeçalho com informações do usuário
        JLabel titulo = new JLabel("Bem-vindo ao Cofre Digital", SwingConstants.CENTER);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 20));

        JLabel infoUsuario = new JLabel(
                String.format("Login: %s    |    Grupo: %s    |    Nome: %s",
                        usuario.getLogin(), usuario.getGrupo(), usuario.getNome()
                ), SwingConstants.CENTER);
        infoUsuario.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JPanel cabecalho = new JPanel(new GridLayout(2, 1, 5, 5));
        cabecalho.add(titulo);
        cabecalho.add(infoUsuario);
        add(cabecalho, BorderLayout.NORTH);

        // Corpo com opções do sistema
        JPanel corpo = new JPanel(new GridLayout(1, 2, 30, 10));

        // Exemplo: painel lateral com botões de navegação
        JPanel menu = new JPanel(new GridLayout(3, 1, 10, 10));
        JButton btnCadastro = new JButton("Cadastro de Usuários");
        JButton btnConsulta = new JButton("Consulta de Arquivos");
        JButton btnSair = new JButton("Sair");


        btnCadastro.addActionListener(onCadastro);
        btnConsulta.addActionListener(onConsulta);
        btnSair.addActionListener(onSair);

        menu.add(btnCadastro);
        menu.add(btnConsulta);
        menu.add(btnSair);

        // Corpo da tela com conteúdo dinâmico

        JPanel painelInfo = new JPanel(new BorderLayout());
        JLabel totalAcessos = new JLabel("Total de acessos: ", SwingConstants.CENTER);
        try {
            totalAcessos.setText("Total de acessos: "+ Integer.toString(dao.getNumeroAcessos(usuario)));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao pegar número de acessos do usuario", "Erro", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        totalAcessos.setFont(new Font("SansSerif", Font.PLAIN, 16));
        painelInfo.add(totalAcessos, BorderLayout.CENTER);

        corpo.add(menu);
        corpo.add(painelInfo);
        add(corpo, BorderLayout.CENTER);
    }
}
