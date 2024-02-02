package uk.ac.soton.comp1206.event;

/**
 * GameLoopListener to handle the ui timer changes
 */
public interface GameLoopListener {
    /**
     * Handle a gameLoop restarts
     */
    void gameLooped();
}
