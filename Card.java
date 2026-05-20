public class Card {
    public enum Suit {
        HEARTS("♥"), DIAMONDS("♦"), CLUBS("♣"), SPADES("♠");
        public final String symbol;
        Suit(String symbol) { this.symbol = symbol; }
    }

    public enum Rank {
        TWO(2,"2"), THREE(3,"3"), FOUR(4,"4"), FIVE(5,"5"),
        SIX(6,"6"), SEVEN(7,"7"), EIGHT(8,"8"), NINE(9,"9"),
        TEN(10,"10"), JACK(11,"J"), QUEEN(12,"Q"), KING(13,"K"), ACE(14,"A");
        public final int value;
        public final String label;
        Rank(int value, String label) { this.value = value; this.label = label; }
    }

    private final Suit suit;
    private final Rank rank;

    public Card(Suit suit, Rank rank) { this.suit = suit; this.rank = rank; }

    public Suit getSuit() { return suit; }
    public Rank getRank() { return rank; }
    public boolean isRed() { return suit == Suit.HEARTS || suit == Suit.DIAMONDS; }

    @Override
    public String toString() { return rank.label + suit.symbol; }
}