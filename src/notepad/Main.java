package notepad;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.control.Alert;

public class Main extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        try {
            NotepadUI ui = new NotepadUI();
            ui.start(primaryStage);
        } catch (Exception e) {
            // Show error in a dialog
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Application Error");
            alert.setHeaderText("Failed to start application");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            
            // Also print to console
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            launch(args);
        } catch (Exception e) {
            System.err.println("Fatal error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}