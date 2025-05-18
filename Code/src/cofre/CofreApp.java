package cofre;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import model.Usuario;
import ui.CadastroAdminPanel;
import security.CertificateUtility;
import Database.DAO;
import security.*;
import ui.LoginTotpPanel;

import javax.crypto.SecretKey;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URLEncoder;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.sql.ResultSet;

public class CofreApp extends JFrame {

    private String caminhoCertificado = null;
    private String caminhoChavePrivada = null;

    public CofreApp() {
        super("Cofre Digital");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(700, 500);
        setLocationRelativeTo(null); // Centraliza na tela

        try {
            Boolean firstTime = DAO.getInstance().getUsersCount() == 0;
            if (!firstTime) {
                iniciarLoginEmail();
            } else {
                iniciarCadastroAdministrador(firstTime);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao acessar banco: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }


    private void iniciarCadastroAdministrador(Boolean firstTime) {
        CadastroAdminPanel painelCadastro = new CadastroAdminPanel(null, null, null, null, firstTime);

        painelCadastro.getBotaoCadastrar().addActionListener(e -> cadastrarAdministrador(painelCadastro));
        painelCadastro.getBotaoVoltar().addActionListener(e -> sair());
        painelCadastro.getBotaoSelecionarCertificado().addActionListener(e -> escolherArquivoCertificado(painelCadastro));
        painelCadastro.getBotaoSelecionarChave().addActionListener(e -> escolherArquivoChave(painelCadastro));

        setContentPane(painelCadastro);
        revalidate();
        repaint();
    }


    private void escolherArquivoCertificado(CadastroAdminPanel painel) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecionar Certificado Digital");
        int resultado = fileChooser.showOpenDialog(this);
        if (resultado == JFileChooser.APPROVE_OPTION) {
            File arquivo = fileChooser.getSelectedFile();
            caminhoCertificado = arquivo.getAbsolutePath();
            painel.getLabelCertificado().setText(caminhoCertificado);
        }
    }

    private void escolherArquivoChave(CadastroAdminPanel painel) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecionar Chave Privada");
        int resultado = fileChooser.showOpenDialog(this);
        if (resultado == JFileChooser.APPROVE_OPTION) {
            File arquivo = fileChooser.getSelectedFile();
            caminhoChavePrivada = arquivo.getAbsolutePath();
            painel.getLabelChavePrivada().setText(caminhoChavePrivada);
        }
    }

    private void cadastrarAdministrador(CadastroAdminPanel painel) {
        String senha = new String(painel.getCampoSenha().getPassword());
        String confirmar = new String(painel.getCampoConfirmarSenha().getPassword());
        String frase = new String(painel.getCampoFraseSecreta().getPassword());

        if (!senha.equals(confirmar)) {
            JOptionPane.showMessageDialog(this, "Senhas não coincidem.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (senha.isEmpty() || frase.isEmpty() || caminhoCertificado == null || caminhoChavePrivada == null) {
            JOptionPane.showMessageDialog(this, "Todos os campos devem ser preenchidos.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        CertificateUtility util;
        String loginExtraido;
        byte[] chavePrivadaBytes = null; // agora é visível em todo o método

        try {
            util = new CertificateUtility(caminhoCertificado);
            util.getCertificado().checkValidity();

            loginExtraido = extrairEmailDoSubject(util.getSujeito());

            if (loginExtraido == null || !loginExtraido.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
                JOptionPane.showMessageDialog(this, "Certificado não contém um email válido.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 2. Verificar assinatura digital
            byte[] mensagem = new byte[8192];
            new SecureRandom().nextBytes(mensagem);

            chavePrivadaBytes = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(caminhoChavePrivada));
            PrivateKey chavePrivada = CertificateUtility.carregarChavePrivada(chavePrivadaBytes, frase);

            byte[] assinatura = CertificateUtility.assinarMensagem(chavePrivada, mensagem);
            PublicKey chavePublica = util.getCertificado().getPublicKey();

            if (!CertificateUtility.verificarAssinatura(chavePublica, mensagem, assinatura)) {
                JOptionPane.showMessageDialog(this, "Frase secreta inválida ou chave privada não corresponde ao certificado.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            util.imprimirResumo();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao processar o certificado digital ou a chave privada:\n" + e.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 3. Validações adicionais
        if (!senha.matches("\\d{8,10}")) {
            JOptionPane.showMessageDialog(this,
                    "A senha deve conter entre 8 e 10 dígitos numéricos.",
                    "Senha Inválida",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (contemSequenciaNumerica(senha)) {
            JOptionPane.showMessageDialog(this,
                    "A senha não pode conter sequências numéricas como 123456 ou 987654.",
                    "Senha Inválida",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // 4. Gerar hash da senha
            String senhaHash = BcryptUtil.hash(senha);

            // 5. Gerar segredo TOTP Base32 e criptografar
            String totpBase32 = TotpUtil.gerarSecretoBase32();
            SecretKey chaveAES = CryptoUtil.gerarChaveAES(senha);
            String totpCriptografado = CryptoUtil.criptografar(totpBase32, chaveAES);

            // 6. Criar usuário
            Usuario usuario = new Usuario();
            usuario.setNome("Administrador");
            usuario.setLogin(loginExtraido);
            usuario.setGrupoId(1);
            usuario.setSenhaHash(senhaHash);
            usuario.setTotpSecretoCriptografado(totpCriptografado);

            DAO dao = DAO.getInstance();
            if (!dao.inserirUsuario(usuario)) {
                JOptionPane.showMessageDialog(this, "Erro ao salvar administrador no banco.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 7. Exibir QR Code TOTP
            String label = "Cofre Digital:" + loginExtraido;
            String otpAuthURL = "otpauth://totp/" + URLEncoder.encode(label, "UTF-8") +
                    "?secret=" + totpBase32 + "&issuer=Cofre+Digital";
            BufferedImage qrImage = generateQRCodeImage(otpAuthURL, 200, 200);
            JOptionPane.showMessageDialog(null, new JLabel(new ImageIcon(qrImage)), "Escaneie com o Google Authenticator", JOptionPane.PLAIN_MESSAGE);

            // 8. Salvar certificado e chave privada criptografada no banco
            int uid = dao.getUserIdByEmail(loginExtraido);

            String certificadoPEM = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(caminhoCertificado)), java.nio.charset.StandardCharsets.UTF_8);
            String chavePrivadaCriptografada = CryptoUtil.criptografar(new String(chavePrivadaBytes, java.nio.charset.StandardCharsets.UTF_8), chaveAES);

            dao.insertChaveiro(uid, certificadoPEM, chavePrivadaCriptografada);

            // 9. Finalizar
            JOptionPane.showMessageDialog(this, "Administrador cadastrado com sucesso!");
            getContentPane().removeAll();
            repaint();
            JOptionPane.showMessageDialog(this, "Sistema pronto para login.");

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao processar e salvar os dados:\n" + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }




    private void sair() {
        int opcao = JOptionPane.showConfirmDialog(this, "Deseja realmente sair?", "Sair", JOptionPane.YES_NO_OPTION);
        if (opcao == JOptionPane.YES_OPTION) {
            dispose();
        }
    }

    private String extrairEmailDoSubject(String subjectDN) {
        for (String parte : subjectDN.split(",")) {
            parte = parte.trim();
            if (parte.startsWith("E=")) {
                return parte.substring(2);
            } else if (parte.toLowerCase().startsWith("emailaddress=")) {
                return parte.substring(13);
            }
        }
        return null;
    }

    public static BufferedImage generateQRCodeImage(String text, int width, int height) throws WriterException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    private boolean contemSequenciaNumerica(String senha) {
        for (int i = 0; i <= senha.length() - 3; i++) {
            int n1 = senha.charAt(i) - '0';
            int n2 = senha.charAt(i + 1) - '0';
            int n3 = senha.charAt(i + 2) - '0';

            if ((n2 == n1 + 1 && n3 == n2 + 1) ||  // sequência crescente
                    (n2 == n1 - 1 && n3 == n2 - 1)) {  // sequência decrescente
                return true;
            }
        }
        return false;
    }

    private void iniciarLoginEmail() {
        final ui.LoginEmailPanel[] painelRef = new ui.LoginEmailPanel[1];

        DAO dao = DAO.getInstance();

        painelRef[0] = new ui.LoginEmailPanel(e -> {
            String email = painelRef[0].getEmail(); // acessa de dentro da lambda
            if (email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Email não pode estar vazio.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                if (dao.getEmailCount(email) == 0) {
                    JOptionPane.showMessageDialog(this, "Email não encontrado.", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                iniciarLoginSenha(email);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro ao verificar email: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        setContentPane(painelRef[0]);
        revalidate();
        repaint();
    }

    private void iniciarLoginSenha(String email) {
        final ui.LoginSenhaPanel[] painelRef = new ui.LoginSenhaPanel[1];
        DAO dao = DAO.getInstance();

        painelRef[0] = new ui.LoginSenhaPanel((possiveisSenhas, evt) -> {
            if (possiveisSenhas.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Senha não pode estar vazia.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                Usuario u = dao.getUserByEmail(email);
                if (u == null) {
                    JOptionPane.showMessageDialog(this, "Usuário não encontrado.", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String hashArmazenado = u.getSenhaHash();

                // Tenta todas as combinações possíveis
                for (String senhaTentativa : possiveisSenhas) {
                    if (security.BcryptUtil.verify(senhaTentativa, hashArmazenado)) {
                        iniciarLoginTotp(email, senhaTentativa);
                        return;
                    }
                }

                JOptionPane.showMessageDialog(this, "Senha incorreta.", "Erro", JOptionPane.ERROR_MESSAGE);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro ao verificar senha: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        setContentPane(painelRef[0]);
        revalidate();
        repaint();
    }

    private void iniciarLoginTotp(String email, String senha) {
        DAO dao = DAO.getInstance();
        try {
            Usuario u = dao.getUserByEmail(email);
            if (u == null) {
                JOptionPane.showMessageDialog(this, "Usuário não encontrado.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            LoginTotpPanel totpPanel = new LoginTotpPanel(this, u, senha,(usuario, sucesso) -> {
                if (sucesso) {
                    System.out.println("sucesso");
                    //iniciarTelaPrincipal(usuario);
                } else {
                    System.out.println("erro");
                    iniciarLoginEmail();
                }
            });

            setContentPane(totpPanel);
            revalidate();
            repaint();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao buscar usuário: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}
