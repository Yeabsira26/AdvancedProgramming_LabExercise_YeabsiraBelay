import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {
    private final List<Card> cards = new ArrayList<>();

    public Deck() { reset(); }

    public void reset() {
        cards.clear();
        for (Card.Suit s : Card.Suit.values())
            for (Card.Rank r : Card.Rank.values())
                cards.add(new Card(s, r));
        Collections.shuffle(cards);
    }

    public Card deal() {
        if (cards.isEmpty()) throw new IllegalStateException("Deck empty");
        return cards.remove(cards.size() - 1);
    }
}