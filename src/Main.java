import impl.FileDecryptor;
import impl.FileEncryptor;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.nio.file.Paths;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Main {
    private static String selectedFilePath;
    private static String selectedPath;
    private static boolean isEncryptionMode = true;

    public static void main(String[] args) {
        JFrame frame = new JFrame("File Encryption/Decryption");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel titleLabel = new JLabel("Select a file to " + (isEncryptionMode ? "encrypt" : "decrypt") + ":");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16)); // 调整标题的字体大小
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        mainPanel.add(titleLabel, gbc);

        JTextField filePathField = new JTextField();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 1.0;
        mainPanel.add(filePathField, gbc);

        JButton browseButton = new JButton("Browse");
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.2;
        mainPanel.add(browseButton, gbc);

        JButton toggleButton = new JButton("Toggle (Encrypt/Decrypt)");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        mainPanel.add(toggleButton, gbc);

        JButton performButton = new JButton("Perform Action");
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        mainPanel.add(performButton, gbc);

        frame.add(mainPanel);

        frame.setPreferredSize(new Dimension(500, 250)); // 调整窗口大小
        frame.pack();
        frame.setLocationRelativeTo(null);

        // 拖拽文件功能
        frame.setDropTarget(new DropTarget() {
            @Override
            public void drop(DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    java.util.List<File> droppedFiles = (java.util.List<File>) dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    if (!droppedFiles.isEmpty()) {
                        File selectedFile = droppedFiles.get(0);
                        selectedFilePath = selectedFile.getAbsolutePath();
                        selectedPath = selectedFile.getParent();
                        filePathField.setText(selectedFilePath);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        browseButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Text Files", "txt");
            fileChooser.setFileFilter(filter);
            int returnValue = fileChooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                selectedFilePath = selectedFile.getAbsolutePath();
                selectedPath = selectedFile.getParent();
                filePathField.setText(selectedFilePath);
            }
        });

        toggleButton.addActionListener(e -> {
            isEncryptionMode = !isEncryptionMode;
            titleLabel.setText("Select a file to " + (isEncryptionMode ? "encrypt" : "decrypt") + ":");
        });

        performButton.addActionListener(e -> {
            if (selectedFilePath == null || selectedFilePath.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Please select a file.");
                return;
            }
            if (isEncryptionMode) {
                // 文件加密
                try {
                    FileEncryptor.encrypt(selectedFilePath);
                    JOptionPane.showMessageDialog(null, "File encrypted successfully.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
                }
            } else {
                // 文件解密
                try {
                    String returnValue = JOptionPane.showInputDialog("Save File name");
                    if (returnValue != null) {
                        String outputFilePath = String.valueOf(Paths.get(selectedPath, returnValue));
                        FileDecryptor.decrypt(selectedFilePath, outputFilePath, selectedFilePath);
                        JOptionPane.showMessageDialog(null, "File decrypted successfully.");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
                }
            }
        });

        frame.setVisible(true);
    }
}
