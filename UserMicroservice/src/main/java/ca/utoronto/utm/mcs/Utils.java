package ca.utoronto.utm.mcs;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.stream.Collectors;

public class Utils {
   public static String convert(InputStream inputStream) throws IOException {

      try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
         return br.lines().collect(Collectors.joining(System.lineSeparator()));
      }
   }

   public static String convertStringToHash(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
      String salt = "saltKey";
      SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
      PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 1000, 512);
      SecretKey key = secretKeyFactory.generateSecret(spec);
      return Utils.toHex(key.getEncoded());
   }

   private static String toHex(byte[] array)
   {
      BigInteger bi = new BigInteger(1, array);
      String hex = bi.toString(16);
      int paddingLength = (array.length * 2) - hex.length();
      if(paddingLength > 0)
         return String.format("%0" + paddingLength + "d", 0) + hex;
      else
         return hex;
   }
}
