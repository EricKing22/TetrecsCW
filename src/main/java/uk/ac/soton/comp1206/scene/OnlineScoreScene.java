package uk.ac.soton.comp1206.scene;

import javafx.beans.property.SimpleListProperty;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Utility.Triplet;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.util.List;

/**
 * The OnlineScoreScene shows rankings of scores of this game and scores from online.
 */
public class OnlineScoreScene extends ScoresScene{
    List<Triplet<String, String, String>> records;

    private static final Logger logger = LogManager.getLogger(OnlineScoreScene.class);

    /**
     * Create a new scene, passing in the GameWindow the scene will be displayed in
     * Game contains information about the finished game
     *
     * @param gameWindow the game window
     * @param game MultiplayerGame
     * @param scores list of scores of all players of the game
     */
    public OnlineScoreScene(GameWindow gameWindow, MultiplayerGame game, List scores) {
        super(gameWindow, game);
        this.records = scores;

        loadScores();

    }

    /**
     * Override the initialise method to loadScores from all players
     */
    @Override
    public void initialise() {
        super.initialise();

    }

    /**
     * Override the load local scores method.
     * Try load scores of all players in one online game
     * If records is null, means it's a Single Player game
     */
    @Override
    protected void loadScores() {
        try{
            for (Triplet<String,String,String> record : records){
                String name = record.getFirst();
                Integer score = Integer.parseInt(record.getSecond());
                localScores.add(new Pair<>(name, score));
            }
        }catch (Exception e){
            return;
        }

    }

    /**
     * Handle key event
     * @param event Key event
     */
    @Override
    protected void handleKey(KeyEvent event) {
        super.handleKey(event);
        if (event.getCode() == KeyCode.ESCAPE){
            logger.info("Exiting multiplayer mode");
            communicator.send("PART");
        }
    }
}
