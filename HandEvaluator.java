import java.util.*;

public class HandEvaluator {

    public enum HandRank {
        HIGH_CARD("High Card"), ONE_PAIR("One Pair"), TWO_PAIR("Two Pair"),
        THREE_OF_A_KIND("Three of a Kind"), STRAIGHT("Straight"), FLUSH("Flush"),
        FULL_HOUSE("Full House"), FOUR_OF_A_KIND("Four of a Kind"),
        STRAIGHT_FLUSH("Straight Flush"), ROYAL_FLUSH("Royal Flush");

        public final String display;
        HandRank(String d) { this.display = d; }
    }

    public static HandRank evaluate(List<Card> hand) {
        boolean flush    = isFlush(hand);
        boolean straight = isStraight(hand);
        int high         = highCard(hand);

        if (flush && straight && high == 14) return HandRank.ROYAL_FLUSH;
        if (flush && straight)               return HandRank.STRAIGHT_FLUSH;

        List<Integer> counts = new ArrayList<>(frequency(hand).values());
        counts.sort(Collections.reverseOrder());

        if (counts.get(0) == 4) return HandRank.FOUR_OF_A_KIND;
        if (counts.get(0) == 3 && counts.size() > 1 && counts.get(1) == 2) return HandRank.FULL_HOUSE;
        if (flush)               return HandRank.FLUSH;
        if (straight)            return HandRank.STRAIGHT;
        if (counts.get(0) == 3) return HandRank.THREE_OF_A_KIND;
        if (counts.get(0) == 2 && counts.size() > 1 && counts.get(1) == 2) return HandRank.TWO_PAIR;
        if (counts.get(0) == 2) return HandRank.ONE_PAIR;
        return HandRank.HIGH_CARD;
    }

    private static boolean isFlush(List<Card> hand) {
        Card.Suit s = hand.get(0).getSuit();
        return hand.stream().allMatch(c -> c.getSuit() == s);
    }

    private static boolean isStraight(List<Card> hand) {
        List<Integer> vals = hand.stream().map(c -> c.getRank().value).sorted().toList();
        for (int i = 1; i < vals.size(); i++)
            if (vals.get(i) != vals.get(i - 1) + 1) return false;
        return true;
    }

    private static int highCard(List<Card> h) {
        return h.stream().mapToInt(c -> c.getRank().value).max().orElse(0);
    }

    private static Map<Integer, Integer> frequency(List<Card> hand) {
        Map<Integer, Integer> map = new HashMap<>();
        for (Card c : hand) map.merge(c.getRank().value, 1, Integer::sum);
        return map;
    }
}