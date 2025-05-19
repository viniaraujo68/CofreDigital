// Francisco Lou Gardenberg - 2211275
// Vinicius Barros Pessoa de Araujo - 2210392

package ui;

import Database.DAO;
import Database.Database;
import model.Usuario;
import cofre.CofreApp;
import security.CryptoUtil;
import security.CertificateUtility;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Files;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.util.List;

public class TelaConsultaArquivosPanel extends JPanel {

    private final JTextField campoPasta;
    private final JTextField campoFraseSecreta;
    private final DefaultTableModel tabelaModel;

    public TelaConsultaArquivosPanel(Usuario usuario, ActionListener onVoltar) {
        DAO dao = DAO.getInstance();
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        // Cabeçalho
        JLabel titulo = new JLabel("Consulta de Arquivos Secretos", SwingConstants.CENTER);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 20));

        String info = String.format("Login: %s    |    Grupo: %s    |    Nome: %s", usuario.getLogin(), usuario.getGrupo(), usuario.getNome());
        JLabel infoUsuario = new JLabel(info, SwingConstants.CENTER);
        infoUsuario.setFont(new Font("SansSerif", Font.PLAIN, 14));

        int consultas = 0;
        try {
            consultas = dao.getNumeroConsultas(usuario);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao consultar total de consultas.", "Erro", JOptionPane.ERROR_MESSAGE);
        }

        JLabel totalConsultas = new JLabel("Total de consultas do usuário: " + consultas, SwingConstants.CENTER);
        totalConsultas.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JPanel cabecalho = new JPanel(new GridLayout(3, 1, 5, 5));
        cabecalho.add(titulo);
        cabecalho.add(infoUsuario);
        cabecalho.add(totalConsultas);
        add(cabecalho, BorderLayout.NORTH);

        // Corpo central com formulário
        JPanel corpo = new JPanel(new GridLayout(2, 2, 10, 10));
        campoPasta = new JTextField(255);
        campoFraseSecreta = new JTextField(255);

        corpo.add(new JLabel("Caminho da pasta:"));
        corpo.add(campoPasta);
        corpo.add(new JLabel("Frase secreta:"));
        corpo.add(campoFraseSecreta);
        add(corpo, BorderLayout.CENTER);

        // Painel inferior com tabela e botões
        JPanel painelInferior = new JPanel(new BorderLayout(10, 10));
        tabelaModel = new DefaultTableModel(new String[]{"Nome do Arquivo", "Dono", "Grupo"}, 0);
        JTable tabela = new JTable(tabelaModel);
        JScrollPane scroll = new JScrollPane(tabela);
        painelInferior.add(scroll, BorderLayout.CENTER);

        JPanel botoes = new JPanel(new GridLayout(1, 2, 20, 10));
        JButton btnListar = new JButton("Listar Arquivos");
        JButton btnVoltar = new JButton("Voltar ao Menu");

        btnListar.addActionListener(e -> {
            Database.log(7003, usuario.getNome());
            listarArquivos(usuario);
        });
        btnVoltar.addActionListener(onVoltar);

        botoes.add(btnListar);
        botoes.add(btnVoltar);
        painelInferior.add(botoes, BorderLayout.SOUTH);

        add(painelInferior, BorderLayout.SOUTH);
    }

    private void listarArquivos(Usuario usuario) {
        try {
            // ETAPA 1 – Solicita a frase secreta
            JPasswordField campoFrase = new JPasswordField();
            int resultado = JOptionPane.showConfirmDialog(
                    this, campoFrase, "Digite sua frase secreta",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
            );
            if (resultado != JOptionPane.OK_OPTION) {
                JOptionPane.showMessageDialog(this, "Operação cancelada pelo usuário.");
                return;
            }
            String fraseSecreta = new String(campoFrase.getPassword());
            String fraseSecretaAdmin = CofreApp.fraseSecreta;

            // ETAPA 2 – Seleciona a pasta que contém os arquivos index.*
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Selecione o diretório da pasta segura");
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            if (fileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
                JOptionPane.showMessageDialog(this, "Nenhum diretório selecionado.");
                return;
            }

            File pasta = fileChooser.getSelectedFile();

            if (pasta == null || !pasta.exists() || !pasta.isDirectory()) {
                Database.log(7004, usuario.getNome());
                JOptionPane.showMessageDialog(this, "Caminho inválido ou inacessível. Por favor, selecione um diretório válido.");
                return;
            }

            File arqIndexEnc = new File(pasta, "index.enc");
            File arqIndexEnv = new File(pasta, "index.env");
            File arqIndexAsd = new File(pasta, "index.asd");

            // ETAPA 3 – Verifica existência dos arquivos
            if (!arqIndexEnc.exists() || !arqIndexEnv.exists() || !arqIndexAsd.exists()) {
                JOptionPane.showMessageDialog(this, "Arquivos index.enc/env/asd ausentes na pasta.");
                return;
            }

            // ETAPA 4 – Carrega a chave privada do admin com a frase secreta
            // (Certificado e chave devem estar carregados conforme seu sistema)
            PrivateKey chavePrivadaAdmin;
            java.security.cert.X509Certificate certificadoAdmin;
            try {

                certificadoAdmin = CertificateUtility.carregarCertificadoAdmin();
                chavePrivadaAdmin = CertificateUtility.carregarChavePrivadaAdmin(fraseSecreta);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erro ao carregar chave ou certificado do administrador:\n" + e.getMessage());
                return;
            }

            // ETAPA 5 – Descriptografa o envelope index.env → obtém a semente AES
            byte[] semente;
            try {
                byte[] dadosEnv = Files.readAllBytes(arqIndexEnv.toPath());

                Cipher rsa = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                rsa.init(Cipher.DECRYPT_MODE, chavePrivadaAdmin);
                semente = rsa.doFinal(dadosEnv);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erro ao descriptografar envelope index.env:\n" + e.getMessage());
                return;
            }

            // ETAPA 6 – Gera chave AES a partir da semente com SHA1PRNG
            SecretKey chaveAES;
            try {
                SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
                sr.setSeed(semente);
                byte[] chave = new byte[32]; // 256 bits
                sr.nextBytes(chave);
                chaveAES = new SecretKeySpec(chave, "AES");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erro ao gerar chave AES a partir da semente:\n" + e.getMessage());
                return;
            }

            // ETAPA 7 – Descriptografa o arquivo index.enc com a chave AES obtida
            byte[] dadosIndex;
            try {
                dadosIndex = CryptoUtil.descriptografarArquivo(arqIndexEnc, chaveAES);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erro ao descriptografar index.enc:\n" + e.getMessage());
                return;
            }

            // ETAPA 8 – Verifica a assinatura digital com index.asd
            try {
                Signature assinatura = Signature.getInstance("SHA256withRSA");
                assinatura.initVerify(certificadoAdmin.getPublicKey());
                assinatura.update(dadosIndex);

                byte[] sigBytes = Files.readAllBytes(arqIndexAsd.toPath());

                if (!assinatura.verify(sigBytes)) {
                    JOptionPane.showMessageDialog(this, "Assinatura digital inválida! Integridade comprometida.");
                    return;
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erro ao verificar assinatura digital do índice:\n" + e.getMessage());
                return;
            }

            // ETAPA 9 – Interpreta e filtra o conteúdo do índice (somente arquivos do usuário ou grupo)
            String conteudo = new String(dadosIndex, "UTF-8");
            StringBuilder resultadoFinal = new StringBuilder();

            for (String linha : conteudo.split("\n")) {
                String[] partes = linha.trim().split(" ");
                if (partes.length != 4) continue;

                String nomeCodigo = partes[0];
                String nomeSecreto = partes[1];
                String dono = partes[2];
                String grupo = partes[3];

                // Somente arquivos do usuário ou do grupo do usuário
                if (dono.equals(usuario.getLogin()) || grupo.equals(usuario.getGrupo())) {
                    resultadoFinal.append(String.format(
                            "Código: %s\nNome: %s\nDono: %s\nGrupo: %s\n\n",
                            nomeCodigo, nomeSecreto, dono, grupo
                    ));
                }
            }

            // ETAPA 10 – Mostra os arquivos visíveis ao usuário
            if (resultadoFinal.length() == 0) {
                JOptionPane.showMessageDialog(this, "Nenhum arquivo visível ao seu perfil foi encontrado.");
            } else {
                JTextArea area = new JTextArea(resultadoFinal.toString());
                area.setEditable(false);
                JScrollPane scroll = new JScrollPane(area);
                JOptionPane.showMessageDialog(this, scroll, "Arquivos disponíveis", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro inesperado ao listar arquivos:\n" + e.getMessage());
            e.printStackTrace();
        }
    }
}
