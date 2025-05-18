package security;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

public class TotpUtil {

    private static final int TIME_STEP_SECONDS = 30;
    private static final int NUM_DIGITS = 6;

    /**
     * Gera uma chave TOTP aleatória e codificada em Base32 (RFC 4648)
     * @return chave secreta em formato Base32 (sem padding)
     */
    public static String gerarSecretoBase32() {
        byte[] segredoBytes = new byte[20]; // 160 bits
        new SecureRandom().nextBytes(segredoBytes);
        Base32 base32 = new Base32(Base32.Alphabet.BASE32, false, false);
        return base32.toString(segredoBytes);
    }

    /**
     * Valida um código TOTP com tolerância de ±30 segundos (3 janelas).
     *
     * @param chaveEncriptadaBase64 chave TOTP criptografada, codificada em Base64
     * @param senha senha para derivar a chave AES
     * @param codigoDigitado código inserido pelo usuário (ex: "123456")
     * @return true se válido, false se inválido
     */
    public static boolean validarCodigoTOTP(String chaveEncriptadaBase64, String senha, String codigoDigitado) {
        try {
            SecretKey chaveAES = CryptoUtil.gerarChaveAES(senha);
            byte[] chaveTOTP = CryptoUtil.descriptografarBytes(chaveEncriptadaBase64, chaveAES);

            long agora = Instant.now().getEpochSecond();
            long intervaloAtual = agora / TIME_STEP_SECONDS;

            for (int i = -1; i <= 1; i++) {
                long intervalo = intervaloAtual + i;
                String codigoGerado = gerarCodigoTOTP(chaveTOTP, intervalo);
                if (codigoGerado.equals(codigoDigitado)) {
                    return true;
                }
            }

        } catch (Exception e) {
            System.err.println("Erro ao validar TOTP: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }


    private static String gerarCodigoTOTP(byte[] chave, long intervalo) throws NoSuchAlgorithmException, InvalidKeyException {
        byte[] buffer = ByteBuffer.allocate(8).putLong(intervalo).array();

        Mac mac = Mac.getInstance("HmacSHA1");
        SecretKeySpec keySpec = new SecretKeySpec(chave, "HmacSHA1");
        mac.init(keySpec);

        byte[] hash = mac.doFinal(buffer);
        int offset = hash[hash.length - 1] & 0xF;

        int binCode = ((hash[offset] & 0x7f) << 24) |
                ((hash[offset + 1] & 0xff) << 16) |
                ((hash[offset + 2] & 0xff) << 8) |
                (hash[offset + 3] & 0xff);

        int totp = binCode % (int) Math.pow(10, NUM_DIGITS);
        return String.format("%0" + NUM_DIGITS + "d", totp);
    }
}
