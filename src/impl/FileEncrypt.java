package impl;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.zip.GZIPOutputStream;

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

        //Read Input
        Path inputPath = Paths.get(inputFilePath);
        File inputFile = inputPath.toFile();
        String inputFileName = inputFile.getName();
        int lastDotIndex = inputFileName.lastIndexOf('.');
        if (lastDotIndex > 0)
            inputFileName = inputFileName.substring(0, lastDotIndex);

        // Read the input file and encrypt it
        ByteArrayOutputStream compressedStream = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipStream = new GZIPOutputStream(compressedStream)) {
            gzipStream.write(Files.readAllBytes(inputPath));
        }

        byte[] compressedData = compressedStream.toByteArray();
        byte[] inputData = cipher.doFinal(compressedData);

        // Get original file name
        String originalFileName = new File(inputFilePath).getName();
        byte[] originalFileNameBytes = modify(originalFileName.getBytes(StandardCharsets.UTF_8));

        // Add a 0x00 as a marker
        byte[] encryptedData = new byte[originalFileNameBytes.length + 1 + inputData.length];
        encryptedData[originalFileNameBytes.length] = 0x00; // 结束标记
        System.arraycopy(originalFileNameBytes, 0, encryptedData, 0, originalFileNameBytes.length);
        System.arraycopy(inputData, 0, encryptedData, originalFileNameBytes.length + 1, inputData.length);
        Path outputFilePath = Paths.get(inputPath.getParent() + "/" + inputFileName + ".encrypted");

        Files.write(outputFilePath, encryptedData);

        // Save the AES key to a file
        ObjectOutputStream keyOut = new ObjectOutputStream(Files.newOutputStream(Paths.get(inputPath.getParent() + "/" + inputFileName + ".encrypted" + ".key")));
        keyOut.writeObject(modify(secretKey.getEncoded()));
        keyOut.close();
    }

    private static byte[] modify(byte[] keyBytes) {
        for (int i = 0; i < keyBytes.length; i++) {
            keyBytes[i] = (byte) (keyBytes[i] ^ 0xFF);
            //Here you can add other modify.
        }
        return keyBytes;
    }
}
