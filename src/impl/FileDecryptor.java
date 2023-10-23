package impl;

import javax.crypto.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

public class FileDecryptor {
    public static void decrypt(String inputFilePath, String outputDirectory, String keyFilePath) throws Exception {
        // Read the AES key from the key file
        ObjectInputStream keyIn = new ObjectInputStream(Files.newInputStream(Paths.get(keyFilePath + ".key")));
        SecretKey secretKey = (SecretKey) keyIn.readObject();
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
}
