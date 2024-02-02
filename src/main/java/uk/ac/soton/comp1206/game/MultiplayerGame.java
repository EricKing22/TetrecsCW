package uk.ac.soton.comp1206.game;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.network.Communicator;

import java.util.LinkedList;
import java.util.Queue;

/**
 * The MultiplayerGame extends Game that are used for Multi-player game.
 * It receives game pieces from the server instead of generating it from random numbers
 */
public class MultiplayerGame extends Game{

    private static final Logger logger = LogManager.getLogger(MultiplayerGame.class);

    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     *
     * @param cols number of columns
     * @param rows number of rows
     */
    public MultiplayerGame(int cols, int rows) {
        super(cols, rows);

    }

    @Override
    public void nextPiece() {
        super.nextPiece();

    }

    /**
     * Override spawnPiece to generate current piece from the pieces queue
     */
    @Override
    public GamePiece spawnPiece() {
        int num = Integer.parseInt(pieces.remove());
        logger.info("Generating new game piece of {}", num);
        return GamePiece.createPiece(num);
    }




}
