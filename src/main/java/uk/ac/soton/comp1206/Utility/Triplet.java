package uk.ac.soton.comp1206.Utility;

/**
 * The Triplet class holds three elements together.
 * Used for leaderboard component which takes three arguments.
 * @param <A> Player's name
 * @param <B> Player's score
 * @param <C> Player's rest lives
 */
public class Triplet<A, B, C> {
    private final A first;
    private final B second;
    private final C third;

    public Triplet(A first, B second, C third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public A getFirst() {
        return first;
    }

    public B getSecond() {
        return second;
    }

    public C getThird() {
        return third;
    }
}