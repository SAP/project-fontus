import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class CryptoTest {
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        String secret = "foobar";
        Base64.Encoder enc = Base64.getEncoder();
        byte[] hashedSecret = MessageDigest.getInstance("SHA-256").digest(secret.getBytes());
        SecretKeySpec key = new SecretKeySpec(hashedSecret, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] ciphertext = cipher.doFinal("lulz".getBytes());
        String foo = enc.encodeToString(ciphertext);
        System.out.printf("\n%s\n",foo);
    }
}
