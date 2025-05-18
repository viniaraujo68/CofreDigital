package security;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.Base64;

public class CryptoUtil {

    public static SecretKey gerarChaveAES(String fraseSecreta) throws Exception {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] chave = sha.digest(fraseSecreta.getBytes("UTF-8"));
        return new SecretKeySpec(chave, 0, 16, "AES"); // usa apenas os 128 bits iniciais
    }

    public static String criptografar(String texto, SecretKey chave) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, chave);
        byte[] criptografado = cipher.doFinal(texto.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(criptografado);
    }

    public static String descriptografar(String textoCriptografado, SecretKey chave) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, chave);
        byte[] decodificado = Base64.getDecoder().decode(textoCriptografado);
        return new String(cipher.doFinal(decodificado), "UTF-8");
    }

    public static byte[] descriptografarBytes(String textoCriptografado, SecretKey chave) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, chave);
        byte[] decodificado = Base64.getDecoder().decode(textoCriptografado);
        return cipher.doFinal(decodificado); // retorna byte[]
    }

}
