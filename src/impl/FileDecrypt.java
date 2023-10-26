package impl;

import javafx.stage.FileChooser;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;

public class FileDecrypt {
    public static void decrypt(String inputFilePath, String outputDirectory, String keyFilePath) throws Exception {
        // Read the AES key from the key file
        SecretKey secretKey;
        Path path = Paths.get(keyFilePath + ".key");

        if (!Files.exists(path)) {
            // If the key file doesn't exist, prompt the user to select it
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select AES Key File");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("AES Key Files", "*.key"));
            File selectedFile = fileChooser.showOpenDialog(null);

            if (selectedFile == null)
                throw new Exception("AES key file not found.");
            path = selectedFile.toPath();
        }

        try {
            ObjectInputStream keyIn = new ObjectInputStream(Files.newInputStream(path));
            secretKey = new SecretKeySpec(restore((byte[]) keyIn.readObject()), "AES");
            keyIn.close();
        } catch (Exception e) {
            throw new Exception("AES key file not valid.");
        }

        // Read the encrypted file
        byte[] encryptedData = Files.readAllBytes(Paths.get(inputFilePath));

        // Find the end of the original file name
        int originalFileNameEnd = 0;
        while (originalFileNameEnd < encryptedData.length && encryptedData[originalFileNameEnd] != 0x00) {
            originalFileNameEnd++;
        }

        if (originalFileNameEnd >= encryptedData.length) {
            throw new Exception("Original file name end marker not found.");
        }

        // Extract the original file name
        byte[] originalFileNameBytes = Arrays.copyOf(encryptedData, originalFileNameEnd);
        String originalFileName = "Dec-" + new String(restore(originalFileNameBytes), StandardCharsets.UTF_8);

        // Extract the encrypted data without the original file name and end marker
        byte[] encryptedDataWithoutFileName = Arrays.copyOfRange(encryptedData, originalFileNameEnd+1, encryptedData.length);

        // Decrypt the file with AES
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        byte[] decryptedData = cipher.doFinal(encryptedDataWithoutFileName);
        byte[] decompressedData = getDecompressedData(decryptedData);

        // Save the decrypted data to a file
        Path outputFilePath = Paths.get(outputDirectory, originalFileName);
        Files.write(outputFilePath, decompressedData);
    }

    private static byte[] getDecompressedData(byte[] decryptedData) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(decryptedData);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (GZIPInputStream gzipStream = new GZIPInputStream(inputStream)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = gzipStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }

        return outputStream.toByteArray();
    }


    private static byte[] restore(byte[] modifiedBytes){
        for (int i = 0; i < modifiedBytes.length; i++) {
            modifiedBytes[i] = (byte) (modifiedBytes[i] ^ 0xFF);
        }
        return modifiedBytes;
    }
}
