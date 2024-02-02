package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.component.GameBlockCoordinate;

import java.util.Set;

/**
 * The Line cleared listener is used to handle the event when lines need to be cleared. It passes
 * a list of game block coordinates which are blocks that need to be cleared
 */

public interface LineClearedListener {
    /**
     * Handle when a list of coordinates need to be cleared
     * @param coordinates list of coordinates of blocks need to be cleared
     */
    void lineCleared(Set<GameBlockCoordinate> coordinates);
}
