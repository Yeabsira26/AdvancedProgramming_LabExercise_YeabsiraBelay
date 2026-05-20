 import java.util.*;

/**
 * Pure game logic — no JavaFX imports.
 * The GUI calls these methods and reacts to the returned GameState.
 */
public class PokerGame {

    public enum Phase { DEAL, BET1, DRAW, BET2, SHOWDOWN, GAME_OVER }

    public static final int STARTING_CHIPS = 500;
    public static final int ANTE           = 10;

    private final Deck   deck  = new Deck();
    private final Player human = new Player("You",  STARTING_CHIPS);
    private final Player cpu   = new Player("CPU",  STARTING_CHIPS);

    private Phase  phase      = Phase.DEAL;
    private int    pot        = 0;
    private String message    = "Welcome! Press 'Deal' to start a round.";
    private String resultMsg  = "";

    // ── Accessors ───────────────────────────────────────────────────
    public Player  getHuman()   { return human; }
    public Player  getCpu()     { return cpu; }
    public int     getPot()     { return pot; }
    public Phase   getPhase()   { return phase; }
    public String  getMessage() { return message; }
    public String  getResult()  { return resultMsg; }

    // ── Actions called by GUI ────────────────────────────────────────

    /** Start a new round (ante + deal). */
    public void deal() {
        if (human.getChips() <= 0 || cpu.getChips() <= 0) { phase = Phase.GAME_OVER; return; }

        deck.reset();
        human.clearHand();
        cpu.clearHand();
        resultMsg = "";

        int ante = Math.min(ANTE, Math.min(human.getChips(), cpu.getChips()));
        human.bet(ante);
        cpu.bet(ante);
        pot = ante * 2;

        for (int i = 0; i < 5; i++) { human.receiveCard(deck.deal()); cpu.receiveCard(deck.deal()); }

        phase   = Phase.BET1;
        message = "Ante paid (" + ante + " each). Pot: " + pot + ". Place your bet.";
    }

    /** Human checks (no bet). */
    public void check() {
        if (phase != Phase.BET1 && phase != Phase.BET2) return;
        message = "You checked. CPU's turn…";
        cpuAct(0);
    }

    /** Human bets the given amount. */
    public boolean humanBet(int amount) {
        if (amount <= 0 || amount > human.getChips()) return false;
        if (phase != Phase.BET1 && phase != Phase.BET2) return false;
        human.bet(amount);
        pot += amount;
        message = "You bet " + amount + ". CPU's turn…";
        cpuAct(amount);
        return true;
    }

    /** Human folds. */
    public void fold() {
        if (phase != Phase.BET1 && phase != Phase.BET2) return;
        human.fold();
        cpu.winPot(pot);
        resultMsg = "You folded. CPU wins the pot of " + pot + ".";
        pot = 0;
        phase = Phase.DEAL;
        message = "Press 'Deal' for a new round.";
    }

    /**
     * Human discards cards at the given indices and draws replacements.
     * @param indices 0-based indices of cards to discard (max 3).
     */
    public void draw(List<Integer> indices) {
        if (phase != Phase.DRAW) return;

        List<Integer> sorted = new ArrayList<>(indices);
        sorted.sort(Collections.reverseOrder());
        for (int i : sorted) human.getHand().remove(i);
        int drew = sorted.size();
        for (int i = 0; i < drew; i++) human.receiveCard(deck.deal());

        // CPU draw AI
        int cpuDrew = cpuDraw();

        message = "You drew " + drew + " card(s). CPU drew " + cpuDrew + ". Place your final bet.";
        phase = Phase.BET2;
    }

    // ── Private helpers ──────────────────────────────────────────────

    private void cpuAct(int humanBetAmount) {
        HandEvaluator.HandRank rank = HandEvaluator.evaluate(cpu.getHand());
        boolean decent = rank.ordinal() >= HandEvaluator.HandRank.ONE_PAIR.ordinal();

        if (humanBetAmount == 0) {
            // Human checked — CPU may bet
            if (decent) {
                int cpuBet = 20;
                if (cpu.bet(cpuBet)) {
                    pot += cpuBet;
                    // Human must call or fold — handled by GUI showing CALL/FOLD
                    message += " CPU bets " + cpuBet + ". Call or Fold?";
                    phase = (phase == Phase.BET1) ? Phase.BET1 : Phase.BET2;
                    // set a flag so GUI knows it's a call situation
                    pendingCpuBet = cpuBet;
                    return;
                }
            }
            // CPU checks back → advance phase
            pendingCpuBet = 0;
            advancePhase();
        } else {
            // Human bet — CPU calls or folds
            if (decent && cpu.bet(humanBetAmount)) {
                pot += humanBetAmount;
                message += " CPU calls.";
                pendingCpuBet = 0;
                advancePhase();
            } else {
                cpu.fold();
                human.winPot(pot);
                resultMsg = "CPU folded! You win the pot of " + pot + ".";
                pot = 0;
                phase = Phase.DEAL;
                message = "Press 'Deal' for a new round.";
            }
        }
    }

    /** Amount CPU has bet that the human still needs to call (0 = no pending call). */
    private int pendingCpuBet = 0;
    public int getPendingCpuBet() { return pendingCpuBet; }

    /** Human calls the pending CPU bet. */
    public void call() {
        if (pendingCpuBet <= 0) return;
        int amount = Math.min(pendingCpuBet, human.getChips());
        human.bet(amount);
        pot += amount;
        pendingCpuBet = 0;
        message = "You called " + amount + ".";
        advancePhase();
    }

    private void advancePhase() {
        if (phase == Phase.BET1) {
            phase = Phase.DRAW;
            message += " Select cards to discard (click to toggle), then press 'Draw'.";
        } else if (phase == Phase.BET2) {
            showdown();
        }
    }

    private int cpuDraw() {
        HandEvaluator.HandRank rank = HandEvaluator.evaluate(cpu.getHand());
        if (rank.ordinal() < HandEvaluator.HandRank.ONE_PAIR.ordinal()) {
            // Keep best 2 cards
            cpu.getHand().sort(Comparator.comparingInt(c -> c.getRank().value));
            int n = 3;
            for (int i = 0; i < n; i++) cpu.getHand().remove(0);
            for (int i = 0; i < n; i++) cpu.receiveCard(deck.deal());
            return n;
        }
        return 0;
    }

    private void showdown() {
        HandEvaluator.HandRank hr = HandEvaluator.evaluate(human.getHand());
        HandEvaluator.HandRank cr = HandEvaluator.evaluate(cpu.getHand());
        int cmp = hr.compareTo(cr);

        String humanHand = "Your hand: " + hr.display;
        String cpuHand   = "CPU hand: " + cr.display;

        if (cmp > 0) {
            human.winPot(pot);
            resultMsg = humanHand + " beats " + cpuHand + ". You win " + pot + " chips!";
        } else if (cmp < 0) {
            cpu.winPot(pot);
            resultMsg = cpuHand + " beats " + humanHand + ". CPU wins " + pot + " chips.";
        } else {
            int half = pot / 2;
            human.winPot(half);
            cpu.winPot(pot - half);
            resultMsg = humanHand + " ties " + cpuHand + ". Pot split!";
        }
        pot = 0;
        phase = (human.getChips() > 0 && cpu.getChips() > 0) ? Phase.DEAL : Phase.GAME_OVER;
        message = phase == Phase.GAME_OVER ? "Game over!" : "Press 'Deal' for a new round.";
    }
}


