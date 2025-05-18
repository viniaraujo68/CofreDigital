package security;

import org.bouncycastle.crypto.generators.OpenBSDBCrypt;

import java.security.SecureRandom;

public class BcryptUtil {

    private static final int COST = 8;
    private static final String VERSION = "$2y$";

    public static String hash(String senha) {
        byte[] salt = generateSalt();
        // Gera hash com prefixo $2a$
        String hash = OpenBSDBCrypt.generate(senha.toCharArray(), salt, COST);
        // Substitui prefixo $2a$ por $2y$ para manter compatibilidade com sua política
        return hash.replace("$2a$", VERSION);
    }

    public static boolean verify(String senha, String hash) {
        try {
            // Converte de volta para $2a$ para que o BouncyCastle aceite
            String compatHash = hash.replace("$2y$", "$2a$");
            return OpenBSDBCrypt.checkPassword(compatHash, senha.toCharArray());
        } catch (Exception e) {
            return false;
        }
    }

    private static byte[] generateSalt() {
        byte[] salt = new byte[16]; // 128 bits
        new SecureRandom().nextBytes(salt);
        return salt;
    }
}
