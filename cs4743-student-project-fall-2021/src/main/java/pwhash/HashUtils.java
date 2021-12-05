package pwhash;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtils {

    // stolen from: https://medium.com/@TechExpertise/java-cryptographic-hash-functions-a7ae28f3fa42
    //Function to calculate the Cryptographic hash value of an input String and the provided Digest algorithm
    public static String getCryptoHash(String input, String algorithm) {
        try {
            //MessageDigest classes Static getInstance method is called with MD5 hashing
            MessageDigest msgDigest = MessageDigest.getInstance(algorithm);

            //digest() method is called to calculate message digest of the input
            //digest() return array of byte.
            // docrob note: this is the MAGIC
            byte[] inputDigest = msgDigest.digest(input.getBytes());

            // Convert byte array into signum representation
            // BigInteger class is used, to convert the resultant byte array into its signum representation
            BigInteger inputDigestBigInt = new BigInteger(1, inputDigest);

            // Convert the input digest into hexadecimal value (each char converted to 2-byte value in range 00 to ff)
            String hashtext = inputDigestBigInt.toString(16);

            //Add preceding 0's to pad the hashtext to make it 32 bit
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        } catch (NoSuchAlgorithmException e) {
            // docrob note: notice the author wrapped the checked exception in an unchecked exception
            throw new RuntimeException(e);
        }
    }
}
