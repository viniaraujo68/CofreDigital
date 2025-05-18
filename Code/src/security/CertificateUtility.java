package security;

import java.io.*;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateException;
import java.util.Date;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.*;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.EncryptedPrivateKeyInfo;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.Signature;

public class CertificateUtility {

    private final X509Certificate certificado;

    public CertificateUtility(String caminhoArquivo) throws CertificateException, IOException {
        try (FileInputStream fis = new FileInputStream(caminhoArquivo)) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            this.certificado = (X509Certificate) cf.generateCertificate(fis);
        }
    }

    public String getEmissor() {
        return certificado.getIssuerDN().getName();
    }

    public String getSujeito() {
        return certificado.getSubjectDN().getName();
    }

    public String getAlgoritmoAssinatura() {
        return certificado.getSigAlgName();
    }

    public String getNumeroSerie() {
        return certificado.getSerialNumber().toString(16).toUpperCase();
    }

    public Date getValidadeInicial() {
        return certificado.getNotBefore();
    }

    public Date getValidadeFinal() {
        return certificado.getNotAfter();
    }

    public String getTipo() {
        return certificado.getType();
    }

    public void imprimirResumo() {
        System.out.println("------ Informações do Certificado ------");
        System.out.println("Sujeito       : " + getSujeito());
        System.out.println("Emissor       : " + getEmissor());
        System.out.println("Número Série  : " + getNumeroSerie());
        System.out.println("Validade Início: " + getValidadeInicial());
        System.out.println("Validade Fim   : " + getValidadeFinal());
        System.out.println("Algoritmo     : " + getAlgoritmoAssinatura());
        System.out.println("Tipo          : " + getTipo());
        System.out.println("----------------------------------------");
    }

    public X509Certificate getCertificado() {
        return certificado;
    }

    public static PrivateKey carregarChavePrivada(byte[] chaveBytes, String fraseSecreta) throws Exception {
        String chavePEM = new String(chaveBytes, StandardCharsets.UTF_8);

        // Remove cabeçalhos e rodapés e espaços em branco
        chavePEM = chavePEM.replaceAll("-----BEGIN (ENCRYPTED )?PRIVATE KEY-----", "")
                .replaceAll("-----END (ENCRYPTED )?PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] chaveDecodificada;
        try {
            chaveDecodificada = Base64.getDecoder().decode(chavePEM);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Erro ao decodificar a chave privada em Base64. Verifique se o arquivo contém apenas caracteres válidos.");
        }

        try {
            // Tenta descriptografar chave protegida
            EncryptedPrivateKeyInfo encryptedInfo = new EncryptedPrivateKeyInfo(chaveDecodificada);
            PBEKeySpec pbeKeySpec = new PBEKeySpec(fraseSecreta.toCharArray());
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(encryptedInfo.getAlgName());
            Key pbeKey = keyFactory.generateSecret(pbeKeySpec);
            Cipher cipher = Cipher.getInstance(encryptedInfo.getAlgName());
            cipher.init(Cipher.DECRYPT_MODE, pbeKey, encryptedInfo.getAlgParameters());

            PKCS8EncodedKeySpec keySpec = encryptedInfo.getKeySpec(cipher);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(keySpec);

        } catch (IOException e) {
            // Caso não seja uma chave criptografada
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(chaveDecodificada);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(keySpec);
        }
    }


    public static byte[] assinarMensagem(PrivateKey chavePrivada, byte[] dados) throws Exception {
        Signature assinatura = Signature.getInstance("SHA256withRSA");
        assinatura.initSign(chavePrivada);
        assinatura.update(dados);
        return assinatura.sign();
    }

    public static boolean verificarAssinatura(PublicKey chavePublica, byte[] dados, byte[] assinaturaBytes) throws Exception {
        Signature verificador = Signature.getInstance("SHA256withRSA");
        verificador.initVerify(chavePublica);
        verificador.update(dados);
        return verificador.verify(assinaturaBytes);
    }
}
