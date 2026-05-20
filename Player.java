import java.util.ArrayList;
import java.util.List;

public class Player {
    private final String name;
    private int chips;
    private final List<Card> hand = new ArrayList<>();
    private boolean folded = false;
    private int currentBet = 0;

    public Player(String name, int chips) { this.name = name; this.chips = chips; }

    public void receiveCard(Card c) { hand.add(c); }
    public void clearHand()        { hand.clear(); folded = false; currentBet = 0; }

    public boolean bet(int amount) {
        if (amount > chips) return false;
        chips -= amount; currentBet += amount; return true;
    }
    public void fold()            { folded = true; }
    public void winPot(int amount){ chips += amount; }
    public void resetBet()        { currentBet = 0; }

    public String getName()       { return name; }
    public int getChips()         { return chips; }
    public List<Card> getHand()   { return hand; }
    public boolean isFolded()     { return folded; }
    public int getCurrentBet()    { return currentBet; }
}