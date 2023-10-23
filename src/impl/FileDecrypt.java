package impl;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

public class FileDecrypt {
    public static void decrypt(String inputFilePath, String outputDirectory, String keyFilePath) throws Exception {
        // Read the AES key from the key file
        ObjectInputStream keyIn = new ObjectInputStream(Files.newInputStream(Paths.get(keyFilePath + ".key")));
        SecretKey secretKey = restoreKey((byte[]) keyIn.readObject());
        keyIn.close();

        // Read the encrypted file
        byte[] encryptedData = Files.readAllBytes(Paths.get(inputFilePath));

        // Decrypt the file with AES
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedData = cipher.doFinal(encryptedData);

        // Save the decrypted data to a file
        Path outputFilePath = Paths.get(outputDirectory);
        Files.write(outputFilePath, decryptedData);
    }

    private static SecretKey restoreKey(byte[] modifiedKey) {

        // Reverse the complex bit manipulation operations
        for (int i = 0; i < modifiedKey.length; i++) {
            modifiedKey[i] = (byte) (modifiedKey[i] ^ 0xFF); // Reverse the inversion of all bits using XOR (^)
            //这里可以添加自己的位运算
        }

        return new SecretKeySpec(modifiedKey, "AES");
    }
}
