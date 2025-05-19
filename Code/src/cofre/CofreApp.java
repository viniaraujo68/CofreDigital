// Francisco Lou Gardenberg - 2211275
// Vinicius Barros Pessoa de Araujo - 2210392

package cofre;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import model.Usuario;
import ui.CadastroPanel;
import security.CertificateUtility;
import Database.DAO;
import Database.Database;
import security.*;
import ui.LoginTotpPanel;
import ui.SairPanel;
import ui.TelaConsultaArquivosPanel;
import ui.PopupFrasePanel;

import javax.crypto.SecretKey;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CofreApp extends JFrame {

    private String caminhoCertificado = null;
    private String caminhoChavePrivada = null;
    private Usuario usuarioCorrente;
    private final int MAX_TENTATIVAS = 3;
    public static String fraseSecreta = null;

    public CofreApp() {
        super("Cofre Digital");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(700, 500);
        setLocationRelativeTo(null); // Centraliza na tela

        try {
            Database.log(1001);
            Boolean firstTime = DAO.getInstance().getUsersCount() == 0;
            if (!firstTime) {
                String frase = PopupFrasePanel.solicitarFraseSecreta(this);
                if (frase == null || frase.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Frase secreta é obrigatória. Encerrando.", "Erro", JOptionPane.ERROR_MESSAGE);
                    Database.log(1002);
                    System.exit(0); // ou volte para uma tela anterior
                } else {
                    this.fraseSecreta = frase;
                    Database.log(1006);
                    iniciarLoginEmail();
                }
            } else {
                Database.log(1005);
                iniciarCadastro(null);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao acessar banco: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }


    private void iniciarCadastro(Usuario usuario) {
        CadastroPanel painelCadastro = new CadastroPanel(null, null, null, null, usuario);

        painelCadastro.getBotaoCadastrar().addActionListener(e -> cadastrar(painelCadastro));
        painelCadastro.getBotaoVoltar().addActionListener(e -> sair());
        painelCadastro.getBotaoSelecionarCertificado().addActionListener(e -> escolherArquivoCertificado(painelCadastro));
        painelCadastro.getBotaoSelecionarChave().addActionListener(e -> escolherArquivoChave(painelCadastro));
        System.out.println("Listeners setados");
        setContentPane(painelCadastro);
        revalidate();
        repaint();
    }


    private void escolherArquivoCertificado(CadastroPanel painel) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecionar Certificado Digital");
        int resultado = fileChooser.showOpenDialog(this);
        if (resultado == JFileChooser.APPROVE_OPTION) {
            File arquivo = fileChooser.getSelectedFile();
            caminhoCertificado = arquivo.getAbsolutePath();
            painel.getLabelCertificado().setText(caminhoCertificado);
        }
    }

    private void escolherArquivoChave(CadastroPanel painel) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecionar Chave Privada");
        int resultado = fileChooser.showOpenDialog(this);
        if (resultado == JFileChooser.APPROVE_OPTION) {
            File arquivo = fileChooser.getSelectedFile();
            caminhoChavePrivada = arquivo.getAbsolutePath();
            painel.getLabelChavePrivada().setText(caminhoChavePrivada);
        }
    }

    private void cadastrar(CadastroPanel painel) {
        if (usuarioCorrente != null) {
            Database.log(6002, usuarioCorrente.getNome());

        }
        String senha = new String(painel.getCampoSenha().getPassword());
        String confirmar = new String(painel.getCampoConfirmarSenha().getPassword());
        String frase = new String(painel.getCampoFraseSecreta().getPassword());
        String grupo = painel.getCampoGrupo().getSelectedItem().toString();
        fraseSecreta = frase;

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
        byte[] certificadoBytes;
        byte[] challenge;
        byte[] signature;
        byte[] chavePrivadaCripto;

        try {
            util = new CertificateUtility(caminhoCertificado);
            util.getCertificado().checkValidity();

            //COMECO
            File chavePrivadaFile = new File(caminhoChavePrivada);
            PrivateKey chavePrivada = CertificateUtility.carregarChavePrivadaCriptografada(chavePrivadaFile, fraseSecreta);

            SecretKey chaveAESFrase = CryptoUtil.gerarChaveAES(fraseSecreta);
            byte[] chavePrivadaDescriptografada = CryptoUtil.descriptografarArquivo(chavePrivadaFile, chaveAESFrase);

            // 3. Ler certificado
            File certFile = new File(caminhoCertificado);
            java.security.cert.CertificateFactory cf = java.security.cert.CertificateFactory.getInstance("X.509");
            java.security.cert.X509Certificate certificado = (java.security.cert.X509Certificate)
                    cf.generateCertificate(new java.io.FileInputStream(certFile));
            certificadoBytes = certificado.getEncoded();

            challenge = new byte[8192];
            new SecureRandom().nextBytes(challenge);

            // 5. Assinar o desafio
            java.security.Signature signer = java.security.Signature.getInstance("SHA256withRSA");
            signer.initSign(chavePrivada);
            signer.update(challenge);
            signature = signer.sign();

            // 6. Criptografar chave privada novamente para salvar
            chavePrivadaCripto = CryptoUtil.criptografar(chavePrivadaDescriptografada, chaveAESFrase);
            // FIM
            loginExtraido = extrairEmailDoSubject(util.getSujeito());

            if (loginExtraido == null || !loginExtraido.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
                JOptionPane.showMessageDialog(this, "Certificado não contém um email válido.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            util.imprimirResumo();
        } catch (Exception e) {
            if (usuarioCorrente != null) { Database.log(6004, usuarioCorrente.getNome()); }
            JOptionPane.showMessageDialog(this,
                    "Erro ao processar o certificado digital:\n" + e.getMessage(),
                    "Certificado Inválido",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!senha.matches("\\d{8,10}")) {
            if (usuarioCorrente != null) { Database.log(6003, usuarioCorrente.getNome()); }
            JOptionPane.showMessageDialog(this,
                    "A senha deve conter entre 8 e 10 dígitos numéricos.",
                    "Senha Inválida",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (contemSequenciaNumerica(senha)) {
            if (usuarioCorrente != null) { Database.log(6003, usuarioCorrente.getNome()); }
            JOptionPane.showMessageDialog(this,
                    "A senha não pode conter sequências numéricas como 123456 ou 987654.",
                    "Senha Inválida",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // 1. Gerar hash da senha
            String senhaHash = security.BcryptUtil.hash(senha);

            // 2. Gerar segredo TOTP Base32
            byte[] chaveTotp = new byte[20];
            new SecureRandom().nextBytes(chaveTotp);

            Base32 base32 = new Base32(Base32.Alphabet.BASE32, false, false);
            String chaveTotpBase32 = base32.toString(chaveTotp);

            SecretKey chaveAES = CryptoUtil.gerarChaveAES(senha);
            byte[] totpCriptografadoByte = CryptoUtil.criptografar(chaveTotp, chaveAES);

            String issuer = "Cofre Digital";
            String uri = String.format(
                    "otpauth://totp/%s:%s?secret=%s&issuer=%s",
                    issuer.replace(" ", "%20"),
                    loginExtraido,
                    chaveTotpBase32,
                    issuer.replace(" ", "%20")
            );

            // Resumo do certificado formatado como string
            String resumo = String.format("""
        Resumo do Certificado Digital:
        
        Sujeito:  %s
        Emissor:  %s
        Válido de: %s
        Até:       %s
        Serial:    %s
        Algoritmo: %s
        
        Deseja continuar com o cadastro?
        """,
                    util.getCertificado().getSubjectDN().getName(),
                    util.getCertificado().getIssuerDN().getName(),
                    util.getCertificado().getNotBefore(),
                    util.getCertificado().getNotAfter(),
                    util.getCertificado().getSerialNumber().toString(),
                    util.getCertificado().getSigAlgName()
            );

            int opcao = JOptionPane.showConfirmDialog(
                    this,
                    resumo,
                    "Confirmação do Certificado",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.INFORMATION_MESSAGE
            );

            if (opcao != JOptionPane.OK_OPTION) {
                if (usuarioCorrente != null) { Database.log(6009, usuarioCorrente.getNome()); }
                JOptionPane.showMessageDialog(this, "Cadastro cancelado pelo usuário.");
                return;
            }
            if (usuarioCorrente != null) { Database.log(6008, usuarioCorrente.getNome());}

            // 5. Criar usuário e inserir no banco
            model.Usuario usuario = new model.Usuario();
            String subject = util.getCertificado().getSubjectDN().getName();

            Pattern pattern = Pattern.compile("CN=([^,]+)");
            Matcher matcher = pattern.matcher(subject);
            String cn = null;
            if (matcher.find()) {
                cn = matcher.group(1); // Agora é seguro acessar
            }
            usuario.setNome(cn);
            usuario.setLogin(loginExtraido);
            System.out.println("Id do grupo:" + stringToGroup(grupo));
            usuario.setGrupoId(stringToGroup(grupo)); // grupo_id "Administrador", você pode obter dinamicamente se quiser
            usuario.setSenhaHash(senhaHash);
            usuario.setTotpSecretoCriptografado(totpCriptografadoByte);


            DAO dao = DAO.getInstance();
            if (!dao.inserirUsuario(usuario)) {
                JOptionPane.showMessageDialog(this, "Erro ao salvar administrador no banco.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            BufferedImage qrImage = generateQRCodeImage(uri, 200, 200);
            ImageIcon qrIcon = new ImageIcon(qrImage);
            JLabel qrLabel = new JLabel(qrIcon);
            JOptionPane.showMessageDialog(null, qrLabel, "Escaneie com o Google Authenticator", JOptionPane.PLAIN_MESSAGE);

            // 6. Recuperar UID
            int uid = dao.getUserIdByEmail(loginExtraido);

            // 7. Ler e criptografar a chave privada
            byte[] chaveBytes = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(caminhoChavePrivada));
            byte[] chavePrivadaCriptografada = security.CryptoUtil.criptografar(chaveBytes, chaveAES);
            //String chavePrivadaCriptografagaTexto = new String(chavePrivadaCriptografada, StandardCharsets.UTF_8);

            // 8. Ler o certificado como texto
            byte[] certBytes = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(caminhoCertificado));
            //String certificadoTexto = new String(certBytes, java.nio.charset.StandardCharsets.UTF_8);

            // 9. Salvar no chaveiro
            dao.insertChaveiro(uid, chavePrivadaCriptografada, chavePrivadaCriptografada);

            // 10. Finalizar
            JOptionPane.showMessageDialog(this, "Administrador cadastrado com sucesso!");
            getContentPane().removeAll();
            repaint();
            if (usuarioCorrente == null) {
                JOptionPane.showMessageDialog(this, "Sistema pronto para login.");
                iniciarLoginEmail();
            } else
                iniciarCadastro(usuarioCorrente);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao processar e salvar os dados:\n" + e.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }


    private void sair() {
        if (usuarioCorrente != null) {
            Database.log(6010, usuarioCorrente.getNome());
            iniciarTelaPrincipal(usuarioCorrente);
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
        Database.log(2001);
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
                    Database.log(2005, email);
                    return;
                }
                Usuario usuario = dao.getUserByEmail(email);
                if (diferencaEmMinutos(dao.consultarUltimoBloqueio(usuario)) >= 2) {
                    Database.log(2003, usuario.getNome());
                    Database.log(2002, usuario.getNome());
                    iniciarLoginSenha(email);
                } else {
                    Database.log(2004, usuario.getNome());
                    JOptionPane.showMessageDialog(this, "Acesso bloqueado. Tente novamente mais tarde.", "Erro",  JOptionPane.ERROR_MESSAGE);
                }
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

        try {
            Usuario usuario = dao.getUserByEmail(email);
            Database.log(3001, usuario.getNome());

            painelRef[0] = new ui.LoginSenhaPanel(usuario, (possiveisSenhas, evt) -> {
                if (possiveisSenhas.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Senha não pode estar vazia.", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    String hashArmazenado = usuario.getSenhaHash();

                    for (String senhaTentativa : possiveisSenhas) {
                        if (security.BcryptUtil.verify(senhaTentativa, hashArmazenado)) {
                            Database.log(3003, usuario.getNome());
                            Database.log(3002, usuario.getNome());
                            iniciarLoginTotp(email, senhaTentativa);
                            return;
                        }
                    }

                    if (dao.getQtdTentativas(usuario, "tentativas_senha") == 2) {
                        dao.atualizarUltimoBloqueio(usuario);
                        dao.resetarTentativas(usuario, "tentativas_senha");
                        Database.log(3006, usuario.getNome());
                        Database.log(3007, usuario.getNome());
                        Database.log(3002, usuario.getNome());
                        JOptionPane.showMessageDialog(this, "Limite de tentativas excedido. Retornando à etapa 1.", "Erro", JOptionPane.ERROR_MESSAGE);
                        iniciarLoginEmail();
                    } else {
                        dao.incrementaQtdTentativas(usuario, "tentativas_senha");
                        int qtdTentativas = dao.getQtdTentativas(usuario, "tentativas_senha");
                        Database.log(3006 - (MAX_TENTATIVAS - qtdTentativas), usuario.getNome());
                        JOptionPane.showMessageDialog(this, "Senha incorreta.", "Erro", JOptionPane.ERROR_MESSAGE);
                        painelRef[0].setTentativasRestantes(dao.getQtdTentativas(usuario, "tentativas_senha"));
                        painelRef[0].zerarTentativa();
                    }

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Erro ao verificar senha: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            });

            setContentPane(painelRef[0]);
            revalidate();
            repaint();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar painel de senha: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }


    private void iniciarLoginTotp(String email, String senha) {
        DAO dao = DAO.getInstance();
        try {
            Usuario u = dao.getUserByEmail(email);
            Database.log(4001, u.getNome());

            LoginTotpPanel totpPanel = new LoginTotpPanel(this, u, senha,(usuario, sucesso) -> {
                if (sucesso) {
                    Database.log(4003, usuario.getNome());
                    usuarioCorrente = usuario;
                    Database.log(4002, usuario.getNome());
                    iniciarTelaPrincipal(usuario);
                } else {
                    Database.log(4002, usuario.getNome());
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

    private void iniciarTelaPrincipal(Usuario usuario) {
        DAO dao = DAO.getInstance();
        try {
            Database.log(5001, usuario.getNome());
            dao.incrementaAcesso(usuario);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao incrementar acesso: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        ui.TelaInicialPanel painel = new ui.TelaInicialPanel(
                usuario,

                // onCadastro
                e -> {
                    Database.log(5002, usuario.getNome());
                    iniciarCadastro(usuarioCorrente);
                },

                // onConsulta
                e -> {
                    Database.log(5003, usuario.getNome());
                    iniciarTelaConsulta(usuario);
                },

                // onSair
                e -> {
                    Database.log(5004, usuario.getNome());
                    SairPanel painelSair = new SairPanel(null, null, null);

                    painelSair.getBotaoEncerrarSessao().addActionListener(ev-> {
                        Database.log(8002, usuarioCorrente.getNome());
                        usuarioCorrente = null;
                        iniciarLoginEmail();
                    });
                    painelSair.getBotaoEncerrarSistema().addActionListener(ev -> {
                        Database.log(8003, usuarioCorrente.getNome());
                        Database.log(1002);
                        System.exit(0);
                    });

                    painelSair.getBotaoVoltar().addActionListener(ev -> {
                        Database.log(8004, usuarioCorrente.getNome());
                        iniciarTelaPrincipal(usuarioCorrente);
                    });
                    setContentPane(painelSair);
                    revalidate();
                    repaint();
                }
        );


        setContentPane(painel);
        revalidate();
        repaint();
    }

    private void iniciarTelaConsulta(Usuario usuario) {
        Database.log(7001, usuario.getNome());
        JPanel consultaPanel = new TelaConsultaArquivosPanel(
                usuario,
                e -> {
                    Database.log(7002, usuario.getNome());
                    iniciarTelaPrincipal(usuario); // onVoltar
                }
        );
        setContentPane(consultaPanel);
        revalidate();
        repaint();
    }

    private int stringToGroup(String str) {
        return switch (str) {
            case "Administrador" -> 1;
            case "Usuario" -> 2;
            default -> -1;
        };
    }

    public static long diferencaEmMinutos(Timestamp tempo) {
        if (tempo == null) return 3; // força desbloqueio se nunca bloqueado

        // Timestamp do banco (UTC) → convertido para horário local
        ZonedDateTime tempoPassadoLocal = tempo.toInstant().atZone(ZoneId.systemDefault());

        // Agora em horário local
        ZonedDateTime agoraLocal = ZonedDateTime.now(ZoneId.systemDefault());

        Duration duracao = Duration.between(tempoPassadoLocal, agoraLocal);

        return duracao.toMinutes();
    }
}

