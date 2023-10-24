package impl;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;

public class FileDecrypt {
    public static void decrypt(String inputFilePath, String outputDirectory, String keyFilePath) throws Exception {
        // Read the AES key from the key file
        SecretKey secretKey = null;
        try {
            ObjectInputStream keyIn = new ObjectInputStream(Files.newInputStream(Paths.get(keyFilePath + ".key")));
            secretKey = restoreKey((byte[]) keyIn.readObject());
            keyIn.close();
        }catch (Exception e){
            JOptionPane.showMessageDialog(null, "Aes key file not found, please select key file.");

            JFileChooser fileChooser = new JFileChooser();
            int returnValue = fileChooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                ObjectInputStream keyIn = new ObjectInputStream(Files.newInputStream(selectedFile.toPath()));
                secretKey = restoreKey((byte[]) keyIn.readObject());
                keyIn.close();
            }
        }

        if(secretKey == null)
            throw new Exception("Aes key file not valid.");

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

    private static SecretKey restoreKey(byte[] modifiedKey) {
        try {
            return new SecretKeySpec(restore(modifiedKey), "AES");
        }catch (Exception e){
            return null;
        }
    }

    private static byte[] restore(byte[] modifiedBytes){
        // Reverse the complex bit manipulation operations
        for (int i = 0; i < modifiedBytes.length; i++) {
            modifiedBytes[i] = (byte) (modifiedBytes[i] ^ 0xFF); // Reverse the inversion of all bits using XOR (^)
        }
        return modifiedBytes;
    }
}
