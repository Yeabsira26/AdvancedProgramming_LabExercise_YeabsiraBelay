package client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.FileChooser;

import java.io.*;
import java.net.Socket;
import java.util.Base64;

public class ChatClient extends Application {

    private PrintWriter out;
    private BufferedReader in;
    private TextArea chatArea;
    private ListView<String> usersList;
    private String username;

    @Override
    public void start(Stage stage) throws Exception {
        
        // Login dialog
        TextInputDialog loginDialog = new TextInputDialog();
        loginDialog.setTitle("Login");
        loginDialog.setHeaderText("Enter your username");
        username = loginDialog.showAndWait().orElse("Anonymous");
        
        // Connect to server
        try {
            Socket socket = new Socket("localhost", 5000);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.println(username);
            chatArea = new TextArea(); // Initialize here
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Cannot connect to server: " + e.getMessage());
            alert.showAndWait();
            return;
        }
        
        // Chat area
        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setStyle("-fx-background-color: white; -fx-border-color: pink; -fx-border-width: 2px; -fx-font-size: 13px;");
        
        // Users list - LEFT side
        usersList = new ListView<>();
        usersList.setPrefWidth(180);
        usersList.setStyle("-fx-background-color: white; -fx-border-color: pink; -fx-border-width: 2px; -fx-font-size: 13px;");
        
        Label usersLabel = new Label("📋 Online Users");
        usersLabel.setStyle("-fx-background-color: #ffe6f0; -fx-border-color: pink; -fx-padding: 8px; -fx-font-weight: bold;");
        usersLabel.setMaxWidth(Double.MAX_VALUE);
        
        VBox leftPanel = new VBox(10, usersLabel, usersList);
        leftPanel.setPadding(new Insets(10));
        
        // Input area
        TextField messageField = new TextField();
        messageField.setStyle("-fx-border-color: pink; -fx-border-width: 2px; -fx-font-size: 13px;");
        messageField.setPromptText("Type message... (@username for private message)");
        
        Button sendBtn = new Button("Send");
        sendBtn.setStyle("-fx-background-color: #ff69b4; -fx-text-fill: white; -fx-font-weight: bold;");
        
        Button fileBtn = new Button("📎 Send File");
        fileBtn.setStyle("-fx-background-color: #ff69b4; -fx-text-fill: white; -fx-font-weight: bold;");
        
        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.setStyle("-fx-background-color: #ff69b4; -fx-text-fill: white; -fx-font-weight: bold;");
        
        Button privateBtn = new Button("💬 Private Chat");
        privateBtn.setStyle("-fx-background-color: #ff69b4; -fx-text-fill: white; -fx-font-weight: bold;");
        
        HBox bottom = new HBox(10, messageField, sendBtn, fileBtn, refreshBtn);
        bottom.setPadding(new Insets(10));
        
        BorderPane root = new BorderPane();
        root.setLeft(leftPanel);
        root.setCenter(chatArea);
        root.setBottom(bottom);
        
        Scene scene = new Scene(root, 900, 600);
        stage.setTitle("Chat App - " + username);
        stage.setScene(scene);
        stage.show();
        
        // Button actions
        sendBtn.setOnAction(e -> {
            String msg = messageField.getText();
            if (!msg.isEmpty()) {
                out.println(msg);
                messageField.clear();
            }
        });
        
        messageField.setOnAction(e -> sendBtn.fire());
        
        privateBtn.setOnAction(e -> {
            String selected = usersList.getSelectionModel().getSelectedItem();
            if (selected != null && !selected.isEmpty()) {
                messageField.setText("@" + selected + " ");
                messageField.requestFocus();
                chatArea.appendText("💬 Now in private chat with " + selected + "\n");
            } else {
                chatArea.appendText("❌ Please select a user from the list first!\n");
            }
        });
        
        refreshBtn.setOnAction(e -> {
            out.println("/users");
            chatArea.appendText("🔄 Refreshing user list...\n");
        });
        
        fileBtn.setOnAction(e -> {
            String selected = usersList.getSelectionModel().getSelectedItem();
            if (selected == null || selected.isEmpty()) {
                chatArea.appendText("❌ Please select a user from the list first to send a file!\n");
                return;
            }
            
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select File to Send to " + selected);
            File file = fileChooser.showOpenDialog(stage);
            
            if (file != null) {
                sendFile(file, selected);
            }
        });
        
        // Receive messages thread
        new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    String finalLine = line;
                    Platform.runLater(() -> {
                        // Check for USERS update
                        if (finalLine.startsWith("USERS:")) {
                            String users = finalLine.substring(6);
                            usersList.getItems().clear();
                            String[] userArray = users.split(",");
                            for (String u : userArray) {
                                if (!u.equals(username) && !u.isEmpty()) {
                                    usersList.getItems().add(u);
                                }
                            }
                            // Debug output
                            if (usersList.getItems().isEmpty()) {
                                chatArea.appendText("📋 No other users online\n");
                            } else {
                                chatArea.appendText("📋 Online: " + String.join(", ", usersList.getItems()) + "\n");
                            }
                        }
                        // File receipt
                        else if (finalLine.startsWith("FILE:")) {
                            handleFileReceipt(finalLine);
                        }
                        // Regular message
                        else {
                            chatArea.appendText(finalLine + "\n");
                        }
                    });
                }
            } catch (IOException e) {
                Platform.runLater(() -> chatArea.appendText("❌ Disconnected from server\n"));
            }
        }).start();
    }
    
    private void sendFile(File file, String recipient) {
        try {
            chatArea.appendText("📤 Sending '" + file.getName() + "' to " + recipient + "...\n");
            
            byte[] fileBytes = new byte[(int) file.length()];
            try (FileInputStream fis = new FileInputStream(file)) {
                fis.read(fileBytes);
            }
            
            String encodedFile = Base64.getEncoder().encodeToString(fileBytes);
            String fileName = file.getName();
            long fileSize = file.length();
            
            out.println("FILE:" + recipient + ":" + fileName + ":" + fileSize + ":" + encodedFile);
            chatArea.appendText("✅ File sent: " + fileName + " (" + fileSize + " bytes)\n");
            
        } catch (Exception e) {
            chatArea.appendText("❌ Error sending file: " + e.getMessage() + "\n");
        }
    }
    
    private void handleFileReceipt(String message) {
        try {
            String[] parts = message.split(":", 5);
            if (parts.length >= 5) {
                String sender = parts[1];
                String fileName = parts[2];
                String fileSize = parts[3];
                String fileData = parts[4];
                
                byte[] fileBytes = Base64.getDecoder().decode(fileData);
                String saveFileName = "received_from_" + sender + "_" + fileName;
                
                try (FileOutputStream fos = new FileOutputStream(saveFileName)) {
                    fos.write(fileBytes);
                }
                
                chatArea.appendText("📎 File received from " + sender + ": " + fileName + " (" + fileSize + " bytes)\n");
                chatArea.appendText("   Saved as: " + saveFileName + "\n");
            }
        } catch (Exception e) {
            chatArea.appendText("❌ Error receiving file: " + e.getMessage() + "\n");
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}