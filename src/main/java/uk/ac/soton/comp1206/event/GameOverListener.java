package uk.ac.soton.comp1206.event;

/**
 * The GameOver listener is used to handle when the game is over
 */
public interface GameOverListener {
    /**
     * Triggered when player loses all lives
     * or other actions that cause the game to finish
     */
    void gameOver();
}
