package security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Date;

public class TotpUtil {
    private final byte[] key;
    private final long timeStepInSeconds;

    public TotpUtil(String base32EncodedSecret, long timeStepInSeconds) throws Exception {
        this.timeStepInSeconds = timeStepInSeconds;
        this.key = decodeBase32(base32EncodedSecret);
    }

    // Decodificador BASE32 (simples, pode usar Apache Commons ou código próprio)
    private byte[] decodeBase32(String base32) throws Exception {
        base32 = base32.replace("=", "").toUpperCase();
        String base32Chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int currentByte = 0;
        int bitsRemaining = 8;
        for (char c : base32.toCharArray()) {
            int val = base32Chars.indexOf(c);
            if (val == -1) throw new IllegalArgumentException("Caractere inválido na chave BASE32");

            int bits = 5;
            while (bits > 0) {
                if (bitsRemaining > bits) {
                    currentByte = (currentByte << bits) | val;
                    bitsRemaining -= bits;
                    bits = 0;
                } else {
                    currentByte = (currentByte << bitsRemaining) | (val >> (bits - bitsRemaining));
                    bits -= bitsRemaining;
                    buffer.write((byte) currentByte);
                    currentByte = 0;
                    bitsRemaining = 8;
                }
            }
        }

        return buffer.toByteArray();
    }

    // Gera código TOTP com base no contador de tempo
    private String TOTPCode(long timeInterval) {
        byte[] counter = new byte[8];
        for (int i = 7; i >= 0; i--) {
            counter[i] = (byte) (timeInterval & 0xFF);
            timeInterval >>= 8;
        }

        byte[] hash = HMAC_SHA1(counter, key);
        return getTOTPCodeFromHash(hash);
    }

    private byte[] HMAC_SHA1(byte[] counter, byte[] keyByteArray) {
        try {
            SecretKeySpec signKey = new SecretKeySpec(keyByteArray, "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signKey);
            return mac.doFinal(counter);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar HMAC-SHA1", e);
        }
    }

    private String getTOTPCodeFromHash(byte[] hash) {
        int offset = hash[hash.length - 1] & 0x0F;
        int binary = ((hash[offset] & 0x7F) << 24)
                | ((hash[offset + 1] & 0xFF) << 16)
                | ((hash[offset + 2] & 0xFF) << 8)
                | (hash[offset + 3] & 0xFF);

        int otp = binary % 1_000_000;
        return String.format("%06d", otp);
    }

    public static long gerarCodigo(byte[] chave, long timeStep) {
        try {
            byte[] counter = new byte[8];
            for (int i = 7; i >= 0; i--) {
                counter[i] = (byte) (timeStep & 0xFF);
                timeStep >>= 8;
            }

            SecretKeySpec keySpec = new SecretKeySpec(chave, "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(keySpec);
            byte[] hash = mac.doFinal(counter);

            int offset = hash[hash.length - 1] & 0x0F;
            int binary = ((hash[offset] & 0x7F) << 24)
                    | ((hash[offset + 1] & 0xFF) << 16)
                    | ((hash[offset + 2] & 0xFF) << 8)
                    | (hash[offset + 3] & 0xFF);

            return binary % 1_000_000L;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar código TOTP", e);
        }
    }

    public boolean validateCode(String inputTOTP) {
        long time = new Date().getTime() / 1000L;
        long timeInterval = time / timeStepInSeconds;

        for (int i = -1; i <= 1; i++) {
            if (TOTPCode(timeInterval + i).equals(inputTOTP)) {
                return true;
            }
        }

        return false;
    }
}
