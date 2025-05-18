package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;
import java.util.function.BiConsumer;

public class LoginSenhaPanel extends JPanel {

    private final JLabel labelProgresso;
    private final JButton[] botoes;
    private final List<int[]> opcoesDigitadas; // Cada clique representa duas opções possíveis
    private final JButton botaoContinuar;
    private final Random random = new Random();

    public LoginSenhaPanel(BiConsumer<List<String>, ActionEvent> onContinuar) {
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        JLabel titulo = new JLabel("Autenticação - Etapa 2");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        titulo.setHorizontalAlignment(SwingConstants.CENTER);
        add(titulo, BorderLayout.NORTH);

        opcoesDigitadas = new ArrayList<>();
        botoes = new JButton[5];

        JPanel painelCentral = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        labelProgresso = new JLabel("Dígitos escolhidos: 0");
        labelProgresso.setFont(new Font("SansSerif", Font.PLAIN, 14));
        addLinha(painelCentral, gbc, 0, "Progresso:", labelProgresso);

        JPanel botoesPanel = new JPanel(new GridLayout(1, 5, 10, 10));
        for (int i = 0; i < 5; i++) {
            botoes[i] = new JButton();
            int index = i;
            botoes[i].addActionListener(e -> {
                String texto = botoes[index].getText();
                String[] partes = texto.split(" / ");
                int d1 = Integer.parseInt(partes[0]);
                int d2 = Integer.parseInt(partes[1]);
                opcoesDigitadas.add(new int[]{d1, d2});
                atualizarProgresso();
                embaralharBotoes(); // embaralha após cada clique
            });
            botoesPanel.add(botoes[i]);
        }

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        painelCentral.add(botoesPanel, gbc);

        add(painelCentral, BorderLayout.CENTER);

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        botaoContinuar = new JButton("Continuar");
        botaoContinuar.setEnabled(false);
        botaoContinuar.addActionListener(e -> {
            List<String> combinacoes = gerarCombinacoes();
            onContinuar.accept(combinacoes, e);
        });
        painelBotoes.add(botaoContinuar);
        add(painelBotoes, BorderLayout.SOUTH);

        embaralharBotoes(); // Inicializa os botões
    }

    private void atualizarProgresso() {
        labelProgresso.setText("Dígitos escolhidos: " + opcoesDigitadas.size());
        botaoContinuar.setEnabled(!opcoesDigitadas.isEmpty());
    }

    private List<String> gerarCombinacoes() {
        List<String> resultados = new ArrayList<>();
        gerarCombinacoesRec(opcoesDigitadas, 0, "", resultados);
        return resultados;
    }

    private void gerarCombinacoesRec(List<int[]> opcoes, int idx, String atual, List<String> resultados) {
        if (idx == opcoes.size()) {
            resultados.add(atual);
            return;
        }
        int[] par = opcoes.get(idx);
        gerarCombinacoesRec(opcoes, idx + 1, atual + par[0], resultados);
        gerarCombinacoesRec(opcoes, idx + 1, atual + par[1], resultados);
    }

    private void embaralharBotoes() {
        List<Integer> digitos = new ArrayList<>();
        for (int i = 0; i < 10; i++) digitos.add(i);
        Collections.shuffle(digitos, random);
        for (int i = 0; i < 5; i++) {
            int d1 = digitos.get(i * 2);
            int d2 = digitos.get(i * 2 + 1);
            botoes[i].setText(d1 + " / " + d2);
        }
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

    public List<int[]> getOpcoesDigitadas() {
        return opcoesDigitadas;
    }
}
