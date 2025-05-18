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
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
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
        // 1. Converta o conteúdo em texto
        String chaveCodificada = new String(chaveBytes, StandardCharsets.UTF_8)
                .replaceAll("-----BEGIN (ENCRYPTED )?PRIVATE KEY-----", "")
                .replaceAll("-----END (ENCRYPTED )?PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        // 2. Decodifique de BASE32 (não Base64)
        Base32 base32 = new Base32(Base32.Alphabet.BASE32, false, false);
        byte[] chaveCriptografada = base32.fromString(chaveCodificada);
        if (chaveCriptografada == null) {
            throw new IllegalArgumentException("Erro ao decodificar a chave privada em BASE32.");
        }

        // 3. Gere chave AES (256 bits) com SHA1PRNG a partir da frase secreta
        SecureRandom sha1Prng = SecureRandom.getInstance("SHA1PRNG");
        sha1Prng.setSeed(fraseSecreta.getBytes(StandardCharsets.UTF_8));
        byte[] chaveAESBytes = new byte[32]; // 256 bits = 32 bytes
        sha1Prng.nextBytes(chaveAESBytes);
        SecretKey chaveAES = new SecretKeySpec(chaveAESBytes, "AES");

        // 4. Descriptografe a chave privada
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, chaveAES);
        byte[] chaveDescriptografada = cipher.doFinal(chaveCriptografada);

        // 5. Converta para objeto PrivateKey (formato PKCS#8)
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(chaveDescriptografada);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
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
