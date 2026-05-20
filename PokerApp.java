import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;

public class PokerApp extends Application {

    // ── Colours ─────────────────────────────────────────────────────
    private static final String FELT_COLOR   = "#1a5c2a";
    private static final String FELT_DARK    = "#0e3d1c";
    private static final String GOLD         = "#c9a84c";
    private static final String GOLD_LIGHT   = "#f0d080";
    private static final String MSG_BG       = "#0d2b14";

    private final PokerGame game = new PokerGame();

    // ── Card UI state ────────────────────────────────────────────────
    private final List<StackPane> humanCardPanes = new ArrayList<>();
    private final List<Boolean>   selected       = new ArrayList<>();

    // ── Controls ─────────────────────────────────────────────────────
    private HBox    humanHandBox;
    private HBox    cpuHandBox;
    private Label   potLabel;
    private Label   humanChipsLabel;
    private Label   cpuChipsLabel;
    private Label   messageLabel;
    private Label   resultLabel;
    private Button  dealBtn;
    private Button  checkBtn;
    private Button  betBtn;
    private Button  callBtn;
    private Button  foldBtn;
    private Button  drawBtn;
    private TextField betField;

    @Override
    public void start(Stage stage) {
        stage.setTitle("5-Card Draw Poker");

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + FELT_DARK + ";");

        // ── Top: chip counts ─────────────────────────────────────────
        root.setTop(buildTopBar());

        // ── Center: table felt ───────────────────────────────────────
        root.setCenter(buildTable());

        // ── Bottom: controls ─────────────────────────────────────────
        root.setBottom(buildControls());

        Scene scene = new Scene(root, 820, 620);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

        refresh();
    }

    // ── UI Builders ──────────────────────────────────────────────────

    private HBox buildTopBar() {
        humanChipsLabel = chipLabel("YOUR CHIPS: 500");
        cpuChipsLabel   = chipLabel("CPU CHIPS:  500");
        potLabel        = chipLabel("POT: 0");

        HBox bar = new HBox(40, humanChipsLabel, potLabel, cpuChipsLabel);
        bar.setAlignment(Pos.CENTER);
        bar.setPadding(new Insets(14, 20, 10, 20));
        bar.setStyle("-fx-background-color: #0a1a0d; -fx-border-color: " + GOLD + "; -fx-border-width: 0 0 2 0;");
        return bar;
    }

    private Label chipLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Monospace", FontWeight.BOLD, 15));
        l.setTextFill(Color.web(GOLD_LIGHT));
        return l;
    }

    private VBox buildTable() {
        // CPU hand area
        Label cpuLabel = sectionLabel("CPU HAND");
        cpuHandBox = new HBox(10);
        cpuHandBox.setAlignment(Pos.CENTER);
        cpuHandBox.setMinHeight(130);

        // Center strip: message + result
        messageLabel = new Label("Welcome! Press 'Deal' to start.");
        messageLabel.setFont(Font.font("Georgia", FontWeight.NORMAL, 15));
        messageLabel.setTextFill(Color.web("#a8d8a8"));
        messageLabel.setWrapText(true);
        messageLabel.setTextAlignment(TextAlignment.CENTER);
        messageLabel.setMaxWidth(600);

        resultLabel = new Label("");
        resultLabel.setFont(Font.font("Georgia", FontWeight.BOLD, 17));
        resultLabel.setTextFill(Color.web(GOLD_LIGHT));
        resultLabel.setWrapText(true);
        resultLabel.setTextAlignment(TextAlignment.CENTER);
        resultLabel.setMaxWidth(600);

        VBox msgBox = new VBox(6, messageLabel, resultLabel);
        msgBox.setAlignment(Pos.CENTER);
        msgBox.setPadding(new Insets(12, 20, 12, 20));
        msgBox.setStyle("-fx-background-color: " + MSG_BG + "; -fx-background-radius: 10;"
                + "-fx-border-color: " + GOLD + "; -fx-border-radius: 10; -fx-border-width: 1;");
        msgBox.setMaxWidth(620);

        // Human hand area
        Label youLabel = sectionLabel("YOUR HAND  (click cards to select for discard)");
        humanHandBox = new HBox(10);
        humanHandBox.setAlignment(Pos.CENTER);
        humanHandBox.setMinHeight(140);

        VBox table = new VBox(14, cpuLabel, cpuHandBox, msgBox, youLabel, humanHandBox);
        table.setAlignment(Pos.CENTER);
        table.setPadding(new Insets(16, 20, 10, 20));
        table.setStyle("-fx-background-color: " + FELT_COLOR + "; -fx-background-radius: 60;"
                + "-fx-border-color: " + GOLD + "; -fx-border-width: 3; -fx-border-radius: 60;");

        VBox wrapper = new VBox(table);
        wrapper.setAlignment(Pos.CENTER);
        wrapper.setPadding(new Insets(16, 30, 10, 30));
        return wrapper;
    }

    private Label sectionLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
        l.setTextFill(Color.web(GOLD));
        l.setOpacity(0.75);
        return l;
    }

    private HBox buildControls() {
        dealBtn  = actionButton("Deal",  "#2e7d32");
        checkBtn = actionButton("Check", "#1565c0");
        callBtn  = actionButton("Call",  "#6a1b9a");
        betBtn   = actionButton("Bet",   "#e65100");
        foldBtn  = actionButton("Fold",  "#b71c1c");
        drawBtn  = actionButton("Draw",  "#00695c");

        betField = new TextField("20");
        betField.setPrefWidth(65);
        betField.setFont(Font.font("Monospace", FontWeight.BOLD, 14));
        betField.setStyle("-fx-background-color: #1a1a1a; -fx-text-fill: white;"
                + "-fx-border-color: " + GOLD + "; -fx-border-radius: 5; -fx-background-radius: 5;");

        Label betLbl = new Label("Bet:");
        betLbl.setTextFill(Color.web(GOLD_LIGHT));
        betLbl.setFont(Font.font("Monospace", 13));

        dealBtn.setOnAction(e  -> { game.deal(); refresh(); });
        checkBtn.setOnAction(e -> { game.check(); refresh(); });
        callBtn.setOnAction(e  -> { game.call(); refresh(); });
        foldBtn.setOnAction(e  -> { game.fold(); refresh(); });
        drawBtn.setOnAction(e  -> {
            List<Integer> idx = new ArrayList<>();
            for (int i = 0; i < selected.size(); i++) if (selected.get(i)) idx.add(i);
            game.draw(idx);
            refresh();
        });
        betBtn.setOnAction(e -> {
            try {
                int amt = Integer.parseInt(betField.getText().trim());
                if (!game.humanBet(amt)) {
                    messageLabel.setText("Invalid bet amount. Try again.");
                    return;
                }
                refresh();
            } catch (NumberFormatException ex) {
                messageLabel.setText("Enter a valid number.");
            }
        });

        HBox controls = new HBox(10,
                dealBtn, new Separator(javafx.geometry.Orientation.VERTICAL),
                checkBtn, betLbl, betField, betBtn, callBtn, foldBtn,
                new Separator(javafx.geometry.Orientation.VERTICAL),
                drawBtn);
        controls.setAlignment(Pos.CENTER);
        controls.setPadding(new Insets(12, 20, 16, 20));
        controls.setStyle("-fx-background-color: #0a1a0d; -fx-border-color: " + GOLD + "; -fx-border-width: 2 0 0 0;");
        return controls;
    }

    private Button actionButton(String text, String bg) {
        Button b = new Button(text);
        b.setFont(Font.font("Monospace", FontWeight.BOLD, 13));
        b.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: white;"
                + "-fx-background-radius: 6; -fx-padding: 7 18;");
        b.setOnMouseEntered(e -> b.setOpacity(0.8));
        b.setOnMouseExited(e  -> b.setOpacity(1.0));
        return b;
    }

    // ── Refresh UI from game state ───────────────────────────────────

    private void refresh() {
        PokerGame.Phase phase = game.getPhase();

        // Chip / pot labels
        humanChipsLabel.setText("YOUR CHIPS: " + game.getHuman().getChips());
        cpuChipsLabel  .setText("CPU CHIPS:  " + game.getCpu().getChips());
        potLabel       .setText("POT: " + game.getPot());
        messageLabel   .setText(game.getMessage());
        resultLabel    .setText(game.getResult());

        // Rebuild human hand
        humanCardPanes.clear();
        selected.clear();
        humanHandBox.getChildren().clear();
        for (int i = 0; i < game.getHuman().getHand().size(); i++) {
            Card card = game.getHuman().getHand().get(i);
            selected.add(false);
            final int idx = i;
            StackPane pane = buildCardPane(card, false);
            pane.setOnMouseClicked(e -> {
                if (phase != PokerGame.Phase.DRAW) return;
                selected.set(idx, !selected.get(idx));
                pane.setStyle(cardPaneStyle(selected.get(idx)));
            });
            humanCardPanes.add(pane);
            humanHandBox.getChildren().add(pane);
        }

        // Rebuild CPU hand
        cpuHandBox.getChildren().clear();
        boolean revealCpu = (phase == PokerGame.Phase.DEAL || phase == PokerGame.Phase.GAME_OVER
                          || !game.getResult().isEmpty());
        for (Card card : game.getCpu().getHand()) {
            cpuHandBox.getChildren().add(buildCardPane(card, !revealCpu));
        }
        // Show backs if no cards dealt yet
        if (game.getCpu().getHand().isEmpty() && phase != PokerGame.Phase.DEAL) {
            for (int i = 0; i < 5; i++) cpuHandBox.getChildren().add(buildCardBack());
        }

        // Button states
        dealBtn .setDisable(phase != PokerGame.Phase.DEAL && phase != PokerGame.Phase.GAME_OVER);
        checkBtn.setDisable(phase != PokerGame.Phase.BET1 && phase != PokerGame.Phase.BET2 || game.getPendingCpuBet() > 0);
        betBtn  .setDisable(phase != PokerGame.Phase.BET1 && phase != PokerGame.Phase.BET2 || game.getPendingCpuBet() > 0);
        betField.setDisable(phase != PokerGame.Phase.BET1 && phase != PokerGame.Phase.BET2 || game.getPendingCpuBet() > 0);
        callBtn .setDisable(game.getPendingCpuBet() <= 0);
        foldBtn .setDisable(phase != PokerGame.Phase.BET1 && phase != PokerGame.Phase.BET2);
        drawBtn .setDisable(phase != PokerGame.Phase.DRAW);

        if (game.getPendingCpuBet() > 0)
            callBtn.setText("Call " + game.getPendingCpuBet());
        else
            callBtn.setText("Call");
    }

    // ── Card rendering ───────────────────────────────────────────────

    private StackPane buildCardPane(Card card, boolean faceDown) {
        if (faceDown) return buildCardBack();

        Rectangle bg = new Rectangle(72, 110);
        bg.setArcWidth(10); bg.setArcHeight(10);
        bg.setFill(Color.web("#fffdf8"));
        bg.setStroke(Color.web("#cccccc")); bg.setStrokeWidth(1);

        Color suitColor = card.isRed() ? Color.web("#cc2200") : Color.web("#111111");

        Text topRank = cardText(card.getRank().label, 16, suitColor);
        Text topSuit = cardText(card.getSuit().symbol, 14, suitColor);
        Text center  = cardText(card.getSuit().symbol, 34, suitColor);

        VBox topLeft = new VBox(0, topRank, topSuit);
        topLeft.setAlignment(Pos.TOP_LEFT);

        StackPane pane = new StackPane(bg, topLeft, center);
        StackPane.setAlignment(topLeft, Pos.TOP_LEFT);
        StackPane.setMargin(topLeft, new Insets(5, 0, 0, 6));
        StackPane.setAlignment(center, Pos.CENTER);

        pane.setStyle(cardPaneStyle(false));
        pane.setPrefSize(72, 110);
        return pane;
    }

    private StackPane buildCardBack() {
        Rectangle bg = new Rectangle(72, 110);
        bg.setArcWidth(10); bg.setArcHeight(10);
        bg.setFill(Color.web("#1a3a8f"));
        bg.setStroke(Color.web(GOLD)); bg.setStrokeWidth(1.5);

        Rectangle inner = new Rectangle(58, 96);
        inner.setArcWidth(7); inner.setArcHeight(7);
        inner.setFill(Color.TRANSPARENT);
        inner.setStroke(Color.web(GOLD)); inner.setStrokeWidth(1);

        Text star = new Text("♦");
        star.setFont(Font.font("Georgia", 28));
        star.setFill(Color.web(GOLD));

        StackPane pane = new StackPane(bg, inner, star);
        pane.setPrefSize(72, 110);
        return pane;
    }

    private Text cardText(String s, int size, Color fill) {
        Text t = new Text(s);
        t.setFont(Font.font("Georgia", FontWeight.BOLD, size));
        t.setFill(fill);
        return t;
    }

    private String cardPaneStyle(boolean sel) {
        return sel
            ? "-fx-effect: dropshadow(gaussian, #ffdd44, 12, 0.8, 0, -6); -fx-cursor: hand;"
            : "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 6, 0, 2, 3); -fx-cursor: hand;";
    }

    public static void main(String[] args) {
        launch(args);
    }
}
