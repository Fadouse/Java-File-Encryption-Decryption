package impl;

import javax.crypto.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;

public class FileEncrypt {

    public static void encrypt(String inputFilePath) throws Exception {
        // Generate AES key from the timestamp
        SecureRandom secureRandom = new SecureRandom();
        long timestamp = System.currentTimeMillis();
        secureRandom.setSeed(timestamp);
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256, secureRandom);
        SecretKey secretKey = keyGenerator.generateKey();

        // Encrypt the file with AES
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        // Read the input file and encrypt it
        byte[] inputData = Files.readAllBytes(Paths.get(inputFilePath));
        byte[] encryptedData = cipher.doFinal(inputData);

        Path outputFilePath = Paths.get(inputFilePath + ".encrypted");
        Files.write(outputFilePath, encryptedData);

        // Save the AES key to a file
        ObjectOutputStream keyOut = new ObjectOutputStream(Files.newOutputStream(Paths.get(inputFilePath + ".encrypted" + ".key")));
        keyOut.writeObject(modifyKey(secretKey));
        keyOut.close();
    }

    private static byte[] modifyKey(SecretKey originalKey) {
        byte[] keyBytes = originalKey.getEncoded();

        // Complex bit manipulation operations
        for (int i = 0; i < keyBytes.length; i++) {
            keyBytes[i] = (byte) (keyBytes[i] ^ 0xFF); // Invert all bits using XOR (^)
            //这里可以添加自己的位运算
        }

        return keyBytes;
    }
}
