// Francisco Lou Gardenberg - 2211275
// Vinicius Barros Pessoa de Araujo - 2210392

package security;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public class CryptoUtil {

//    public static SecretKey gerarChaveAES(String fraseSecreta) throws Exception {
//        MessageDigest sha = MessageDigest.getInstance("SHA-256");
//        byte[] chave = sha.digest(fraseSecreta.getBytes("UTF-8"));
//        return new SecretKeySpec(chave, 0, 16, "AES"); // usa apenas os 128 bits iniciais
//    }

    public static SecretKey gerarChaveAES(String senha) {
        try {
            byte[] senhaBytes = senha.getBytes("UTF-8");

            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            sr.setSeed(senhaBytes);

            byte[] chave = new byte[32];
            sr.nextBytes(chave);

            return new SecretKeySpec(chave, "AES");
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar chave AES: " + e.getMessage(), e);
        }
    }

    public static byte[] criptografar(byte[] dados, SecretKey chave) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, chave);
            return cipher.doFinal(dados);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao criptografar: " + e.getMessage(), e);
        }
    }

    public static byte[] descriptografar(byte[] dadosCriptografados, SecretKey chaveAES) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, chaveAES);
            return cipher.doFinal(dadosCriptografados);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao descriptografar: " + e.getMessage(), e);
        }
    }

    public static byte[] descriptografarArquivo(File arquivo, SecretKey chaveAES) {
        try {
            byte[] dadosCriptografados = Files.readAllBytes(arquivo.toPath());
            return descriptografar(dadosCriptografados, chaveAES);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao descriptografar arquivo: " + e.getMessage(), e);
        }
    }

}
