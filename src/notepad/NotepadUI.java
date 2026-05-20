package notepad;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.util.Optional;

public class NotepadUI {
    private DatabaseManager dbManager;
    private ListView<Note> noteListView;
    private TextField titleField;
    private TextArea contentArea;
    private Label statusLabel;
    private Label charCountLabel;
    private Note currentNote;
    private ObservableList<Note> notesList;
    private TextField searchField;
    private boolean isDarkTheme = false;
    private ComboBox<Integer> fontSizeCombo;
    private ColorPicker colorPicker;
    private ToggleButton boldBtn;
    private ToggleButton italicBtn;

    public void start(Stage primaryStage) {
        dbManager = new DatabaseManager();
        
        // Main layout
        BorderPane mainLayout = new BorderPane();
        
        // Top - Toolbar
        VBox topContainer = new VBox();
        MenuBar menuBar = createMenuBar(primaryStage);
        ToolBar toolBar = createToolBar();
        topContainer.getChildren().addAll(menuBar, toolBar);
        mainLayout.setTop(topContainer);
        
        // Left panel - Notes list
        VBox leftPanel = createLeftPanel();
        mainLayout.setLeft(leftPanel);
        
        // Center - Note editor
        VBox centerPanel = createCenterPanel();
        mainLayout.setCenter(centerPanel);
        
        // Bottom - Status bar
        HBox statusBar = createStatusBar();
        mainLayout.setBottom(statusBar);
        
        // Create scene
        Scene scene = new Scene(mainLayout, 1000, 650);
        primaryStage.setTitle("Advanced Notepad");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Load existing notes
        refreshNotesList();
        
        // Apply light theme by default
        applyLightTheme(scene);
        
        // Close database when window closes
        primaryStage.setOnCloseRequest(e -> dbManager.close());
    }

    private ToolBar createToolBar() {
        ToolBar toolBar = new ToolBar();
        toolBar.setPadding(new Insets(5));
        
        // Font Size
        Label fontSizeLabel = new Label("Font Size:");
        fontSizeCombo = new ComboBox<>();
        fontSizeCombo.getItems().addAll(10, 12, 14, 16, 18, 20, 24, 28, 32);
        fontSizeCombo.setValue(14);
        fontSizeCombo.setPrefWidth(70);
        fontSizeCombo.setOnAction(e -> changeFontSize());
        
        // Bold Button
        boldBtn = new ToggleButton("B");
        boldBtn.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        boldBtn.setPrefWidth(40);
        boldBtn.setOnAction(e -> toggleBold());
        
        // Italic Button
        italicBtn = new ToggleButton("I");
        italicBtn.setStyle("-fx-font-style: italic; -fx-font-size: 14px;");
        italicBtn.setPrefWidth(40);
        italicBtn.setOnAction(e -> toggleItalic());
        
        // Color Picker
        Label colorLabel = new Label("Color:");
        colorPicker = new ColorPicker(Color.BLACK);
        colorPicker.setPrefWidth(80);
        colorPicker.setOnAction(e -> changeTextColor());
        
        // Theme Toggle Button
        Button themeBtn = new Button("🌙 Dark Theme");
        themeBtn.setPrefWidth(120);
        themeBtn.setOnAction(e -> {
            if (isDarkTheme) {
                applyLightTheme(contentArea.getScene());
                themeBtn.setText("🌙 Dark Theme");
                isDarkTheme = false;
            } else {
                applyDarkTheme(contentArea.getScene());
                themeBtn.setText("☀ Light Theme");
                isDarkTheme = true;
            }
        });
        
        // Separator
        Separator sep1 = new Separator();
        sep1.setOrientation(javafx.geometry.Orientation.VERTICAL);
        Separator sep2 = new Separator();
        sep2.setOrientation(javafx.geometry.Orientation.VERTICAL);
        Separator sep3 = new Separator();
        sep3.setOrientation(javafx.geometry.Orientation.VERTICAL);
        
        toolBar.getItems().addAll(
            fontSizeLabel, fontSizeCombo, sep1,
            boldBtn, italicBtn, sep2,
            colorLabel, colorPicker, sep3,
            themeBtn
        );
        
        return toolBar;
    }

    private MenuBar createMenuBar(Stage stage) {
        MenuBar menuBar = new MenuBar();
        
        // File menu
        Menu fileMenu = new Menu("File");
        MenuItem newNote = new MenuItem("New Note");
        MenuItem saveNote = new MenuItem("Save Note");
        MenuItem deleteNote = new MenuItem("Delete Note");
        MenuItem exit = new MenuItem("Exit");
        
        newNote.setOnAction(e -> createNewNote());
        saveNote.setOnAction(e -> saveCurrentNote());
        deleteNote.setOnAction(e -> deleteSelectedNote());
        exit.setOnAction(e -> {
            dbManager.close();
            stage.close();
        });
        
        fileMenu.getItems().addAll(newNote, saveNote, new SeparatorMenuItem(), 
                                   deleteNote, new SeparatorMenuItem(), exit);
        
        // View menu
        Menu viewMenu = new Menu("View");
        MenuItem toggleTheme = new MenuItem("Toggle Theme");
        toggleTheme.setOnAction(e -> {
            if (isDarkTheme) {
                applyLightTheme(contentArea.getScene());
                isDarkTheme = false;
            } else {
                applyDarkTheme(contentArea.getScene());
                isDarkTheme = true;
            }
        });
        viewMenu.getItems().add(toggleTheme);
        
        // Help menu
        Menu helpMenu = new Menu("Help");
        MenuItem about = new MenuItem("About");
        about.setOnAction(e -> showAbout());
        helpMenu.getItems().add(about);
        
        menuBar.getMenus().addAll(fileMenu, viewMenu, helpMenu);
        return menuBar;
    }

    private VBox createLeftPanel() {
        VBox leftPanel = new VBox(10);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setPrefWidth(250);
        
        // Title
        Label lblNotes = new Label("My Notes");
        lblNotes.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        // Search field
        searchField = new TextField();
        searchField.setPromptText("Search notes...");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            searchNotes(newVal);
        });
        
        // Notes list
        noteListView = new ListView<>();
        notesList = FXCollections.observableArrayList();
        noteListView.setItems(notesList);
        VBox.setVgrow(noteListView, Priority.ALWAYS);
        
        noteListView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> {
                if (newVal != null) {
                    loadNote(newVal);
                }
            }
        );
        
        // Buttons
        Button btnNew = new Button("+ New Note");
        btnNew.setMaxWidth(Double.MAX_VALUE);
        btnNew.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        btnNew.setOnAction(e -> createNewNote());
        
        Button btnDelete = new Button("🗑 Delete");
        btnDelete.setMaxWidth(Double.MAX_VALUE);
        btnDelete.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        btnDelete.setOnAction(e -> deleteSelectedNote());
        
        leftPanel.getChildren().addAll(lblNotes, searchField, noteListView, 
                                       btnNew, btnDelete);
        return leftPanel;
    }

    private VBox createCenterPanel() {
        VBox centerPanel = new VBox(10);
        centerPanel.setPadding(new Insets(10));
        
        // Title field
        Label lblTitle = new Label("Title:");
        lblTitle.setStyle("-fx-font-weight: bold;");
        titleField = new TextField();
        titleField.setPromptText("Enter note title...");
        titleField.setStyle("-fx-font-size: 14px;");
        
        // Content area
        Label lblContent = new Label("Content:");
        lblContent.setStyle("-fx-font-weight: bold;");
        contentArea = new TextArea();
        contentArea.setPromptText("Write your note here...");
        contentArea.setWrapText(true);
        contentArea.setStyle("-fx-font-size: 14px;");
        VBox.setVgrow(contentArea, Priority.ALWAYS);
        
        // Character count listener
        contentArea.textProperty().addListener((obs, oldText, newText) -> {
            updateCharCount();
        });
        
        // Save button
        Button btnSave = new Button("💾 Save Note");
        btnSave.setMaxWidth(Double.MAX_VALUE);
        btnSave.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; " +
                         "-fx-font-size: 14px; -fx-padding: 10px;");
        btnSave.setOnAction(e -> saveCurrentNote());
        
        // Keyboard shortcuts
        centerPanel.setOnKeyPressed(e -> {
            if (e.isControlDown()) {
                switch (e.getCode().toString()) {
                    case "S": saveCurrentNote(); break;
                    case "B": toggleBold(); break;
                    case "I": toggleItalic(); break;
                }
            }
        });
        
        centerPanel.getChildren().addAll(lblTitle, titleField, 
                                         lblContent, contentArea, btnSave);
        return centerPanel;
    }

    private HBox createStatusBar() {
        HBox statusBar = new HBox();
        statusBar.setPadding(new Insets(5));
        statusBar.setStyle("-fx-background-color: #f0f0f0;");
        
        statusLabel = new Label("Ready");
        HBox.setHgrow(statusLabel, Priority.ALWAYS);
        
        charCountLabel = new Label("Characters: 0");
        charCountLabel.setStyle("-fx-padding: 0 10 0 0;");
        
        statusBar.getChildren().addAll(statusLabel, charCountLabel);
        return statusBar;
    }

    private void changeFontSize() {
        Integer size = fontSizeCombo.getValue();
        if (size != null) {
            contentArea.setStyle("-fx-font-size: " + size + "px;");
        }
    }

    private void toggleBold() {
        if (boldBtn.isSelected()) {
            if (italicBtn.isSelected()) {
                contentArea.setStyle(contentArea.getStyle() + "-fx-font-weight: bold;");
            } else {
                contentArea.setStyle(contentArea.getStyle() + "-fx-font-weight: bold;");
            }
        } else {
            contentArea.setStyle(contentArea.getStyle().replace("-fx-font-weight: bold;", ""));
        }
    }

    private void toggleItalic() {
        if (italicBtn.isSelected()) {
            contentArea.setStyle(contentArea.getStyle() + "-fx-font-style: italic;");
        } else {
            contentArea.setStyle(contentArea.getStyle().replace("-fx-font-style: italic;", ""));
        }
    }

    private void changeTextColor() {
        Color color = colorPicker.getValue();
        String colorStyle = String.format("-fx-text-fill: rgb(%d,%d,%d);",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255));
        contentArea.setStyle(contentArea.getStyle() + colorStyle);
    }

    private void applyDarkTheme(Scene scene) {
        if (scene != null) {
            scene.getRoot().setStyle(
                "-fx-base: #2b2b2b;" +
                "-fx-background: #3c3f41;" +
                "-fx-control-inner-background: #2b2b2b;" +
                "-fx-text-fill: #ffffff;" +
                "-fx-text-background-color: #ffffff;"
            );
            contentArea.setStyle("-fx-text-fill: white; -fx-control-inner-background: #2b2b2b;");
            titleField.setStyle("-fx-text-fill: white; -fx-control-inner-background: #2b2b2b;");
            searchField.setStyle("-fx-text-fill: white; -fx-control-inner-background: #2b2b2b;");
        }
    }

    private void applyLightTheme(Scene scene) {
        if (scene != null) {
            scene.getRoot().setStyle("");
            contentArea.setStyle("");
            titleField.setStyle("");
            searchField.setStyle("");
        }
    }

    private void updateCharCount() {
        int count = contentArea.getText().length();
        charCountLabel.setText("Characters: " + count);
    }

    private void createNewNote() {
        currentNote = null;
        titleField.clear();
        contentArea.clear();
        noteListView.getSelectionModel().clearSelection();
        titleField.requestFocus();
        updateCharCount();
        updateStatus("Creating new note...");
    }

    private void loadNote(Note note) {
        currentNote = note;
        titleField.setText(note.getTitle());
        contentArea.setText(note.getContent());
        updateCharCount();
        updateStatus("Loaded: " + note.getTitle());
    }

    private void saveCurrentNote() {
        String title = titleField.getText().trim();
        String content = contentArea.getText();
        
        if (title.isEmpty()) {
            showAlert("Error", "Please enter a title for the note.");
            updateStatus("Error: Title is empty");
            return;
        }
        
        if (currentNote == null) {
            // Save new note
            Note newNote = new Note(title, content);
            int id = dbManager.saveNote(newNote);
            if (id > 0) {
                newNote.setId(id);
                showAlert("Success", "Note saved successfully!");
                refreshNotesList();
                noteListView.getSelectionModel().select(newNote);
                updateStatus("Note saved: " + title);
            } else {
                showAlert("Error", "Failed to save note.");
                updateStatus("Error saving note");
            }
        } else {
            // Update existing note
            currentNote.setTitle(title);
            currentNote.setContent(content);
            if (dbManager.updateNote(currentNote)) {
                showAlert("Success", "Note updated successfully!");
                refreshNotesList();
                updateStatus("Note updated: " + title);
            } else {
                showAlert("Error", "Failed to update note.");
                updateStatus("Error updating note");
            }
        }
    }

    private void deleteSelectedNote() {
        Note selected = noteListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Warning", "Please select a note to delete.");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Note");
        confirm.setHeaderText("Delete \"" + selected.getTitle() + "\"?");
        confirm.setContentText("Are you sure you want to delete this note?");
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (dbManager.deleteNote(selected.getId())) {
                showAlert("Success", "Note deleted successfully!");
                refreshNotesList();
                createNewNote();
                updateStatus("Note deleted");
            } else {
                showAlert("Error", "Failed to delete note.");
                updateStatus("Error deleting note");
            }
        }
    }

    private void searchNotes(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            refreshNotesList();
        } else {
            notesList.clear();
            notesList.addAll(dbManager.searchNotes(keyword.trim()));
        }
    }

    private void refreshNotesList() {
        notesList.clear();
        notesList.addAll(dbManager.getAllNotes());
        updateStatus("Notes loaded: " + notesList.size() + " notes");
    }

    private void updateStatus(String message) {
        statusLabel.setText(message);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("Advanced Notepad Application");
        alert.setContentText("A feature-rich notepad application with MySQL database storage.\n\n" +
                            "Features:\n" +
                            "• Character count\n" +
                            "• Dark/Light theme\n" +
                            "• Font size, Bold, Italic\n" +
                            "• Text color\n" +
                            "• Search notes\n\n" +
                            "Created with JavaFX and XAMPP MySQL\n" +
                            "Version 2.0");
        alert.showAndWait();
    }
}