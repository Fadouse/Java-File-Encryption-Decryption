import impl.FileDecrypt;
import impl.FileEncrypt;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Paths;
import java.util.Objects;

public class Main extends Application {
    private String selectedFilePath;
    private String selectedPath;
    private String selectedFileName;
    private boolean isEncryptionMode = true;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("File Encryption/Decryption");

        Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("resource/lock.png")));
        primaryStage.getIcons().add(icon);

        GridPane mainGrid = new GridPane();
        mainGrid.setHgap(10);
        mainGrid.setVgap(10);
        mainGrid.setPadding(new Insets(10, 10, 10, 10));

        Label titleLabel = new Label("Select a file to " + (isEncryptionMode ? "encrypt" : "decrypt") + ":");
        titleLabel.setStyle("-fx-font-size: 16");

        HBox fileSelectionBox = new HBox(); // Create an HBox for file selection
        TextField filePathField = new TextField();
        filePathField.setPrefColumnCount(20);
        filePathField.setEditable(false);
        Button selectFileButton = new Button("Select File"); // Create a "Select File" button

        fileSelectionBox.getChildren().addAll(filePathField, selectFileButton); // Add both TextField and Button to HBox
        fileSelectionBox.setSpacing(5); // Set spacing between elements

        Button toggleButton = new Button("Toggle (Encrypt/Decrypt)");
        Button performButton = new Button("Perform Action");

        mainGrid.add(titleLabel, 0, 0, 2, 1);
        mainGrid.add(fileSelectionBox, 0, 1, 2, 1); // Add the HBox with TextField and Button
        mainGrid.add(toggleButton, 0, 2, 2, 1);
        mainGrid.add(performButton, 0, 3, 2, 1);

        mainGrid.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        mainGrid.setOnDragDropped(event -> {
            filePathField.clear();
            if (event.getDragboard().hasFiles()) {
                selectedFilePath = event.getDragboard().getFiles().get(0).getAbsolutePath();
                selectedFileName = event.getDragboard().getFiles().get(0).getName();
                selectedPath = event.getDragboard().getFiles().get(0).getParent();
                filePathField.setText(selectedFilePath);
            }
            event.setDropCompleted(true);
            event.consume();
        });

        Scene scene = new Scene(mainGrid, 500, 250);
        primaryStage.setScene(scene);
        primaryStage.show();

        selectFileButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select File");
            File selectedFile = fileChooser.showOpenDialog(primaryStage);

            if (selectedFile != null) {
                selectedFilePath = selectedFile.getAbsolutePath();
                selectedFileName = selectedFile.getName();
                selectedPath = selectedFile.getParent();
                filePathField.setText(selectedFilePath);
            }
        });

        toggleButton.setOnAction(event -> {
            isEncryptionMode = !isEncryptionMode;
            titleLabel.setText("Select a file to " + (isEncryptionMode ? "encrypt" : "decrypt") + ":");
        });

        performButton.setOnAction(event -> {
            if (selectedFilePath == null || selectedFilePath.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Please select a file.");
                alert.showAndWait();
                return;
            }
            if (isEncryptionMode) {
                // 文件加密
                if (selectedFileName.contains(".encrypted")) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Information");
                    alert.setHeaderText(null);
                    alert.setContentText("This file is already encrypted.");
                    alert.showAndWait();
                } else {
                    try {
                        // 调用文件加密方法
                         FileEncrypt.encrypt(selectedFilePath);
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Information");
                        alert.setHeaderText(null);
                        alert.setContentText("File encrypted successfully.");
                        alert.showAndWait();
                    } catch (Exception ex) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText(null);
                        alert.setContentText("Error: " + ex.getMessage());
                        alert.showAndWait();
                    }
                }
            } else {
                // 文件解密
                try {
                    // 调用文件解密方法
                     String outputFilePath = String.valueOf(Paths.get(selectedPath));
                     FileDecrypt.decrypt(selectedFilePath, outputFilePath, selectedFilePath);
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Information");
                    alert.setHeaderText(null);
                    alert.setContentText("File decrypted successfully.");
                    alert.showAndWait();
                } catch (Exception ex) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Error: " + ex.getMessage());
                    alert.showAndWait();
                }
            }
        });
    }
}
