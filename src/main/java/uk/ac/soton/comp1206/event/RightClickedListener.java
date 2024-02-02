package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.component.GameBoard;

/**
 * The RightClickedListener is used to monitor if the main game board is right-clicked.
 */
public interface RightClickedListener {
    /**
     * Handle when a gameBord is right-clicked
     */
    void rightClicked();
}
