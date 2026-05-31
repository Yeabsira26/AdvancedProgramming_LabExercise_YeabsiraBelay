# Poker Game

A simple 5-card draw poker game built in Java with a JavaFX user interface.

## Project Structure

- `src/`
  - `Card.java` — represents a playing card.
  - `Deck.java` — manages the deck and card shuffling.
  - `HandEvaluator.java` — evaluates poker hands and determines winners.
  - `Player.java` — stores player state, chip count, and hand.
  - `PokerGame.java` — game logic and round flow.
  - `PokerApp.java` — JavaFX application UI and controls.
- `bin/` — compiled classes output directory.
- `.vscode/launch.json` — launch configuration for running the app from VS Code.

## Requirements

- Java 17 or newer
- JavaFX SDK installed
- VS Code Java extension pack (optional, but recommended)

## Run from VS Code

The workspace includes a launch configuration for `PokerApp`.

1. Open the workspace in VS Code.
2. Select the `Run and Debug` view.
3. Choose `Run PokerApp`.
4. Start the application.

## Run from the command line

Replace the JavaFX SDK path below with your local JavaFX installation path.

```powershell
cd "c:\Users\yeabsira\OneDrive\Documents\poker"

javac --module-path "C:\path\to\javafx-sdk\lib" --add-modules javafx.controls,javafx.fxml -d bin src\*.java

java --module-path "C:\path\to\javafx-sdk\lib" --add-modules javafx.controls,javafx.fxml -cp bin PokerApp
```

## Notes

- The application uses JavaFX for the game UI.
- The launch configuration already includes the required `--module-path` and `--add-modules` VM arguments.
- Ensure the JavaFX SDK version matches your installed JDK.
