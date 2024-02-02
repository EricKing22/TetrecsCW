package uk.ac.soton.comp1206.component;

import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Utility.Triplet;

import java.util.Collections;
import java.util.Comparator;

/**
 * Leaderboard component for MutiPlayer game scene.
 * It shows the all players' names and scores that are in the same online game.
 */
public class Leaderboard extends ScoresList{
    private static final Logger logger = LogManager.getLogger(Leaderboard.class);

    SimpleListProperty<Triplet<String, String, String>> records = new SimpleListProperty<>();

    /**
     * The leaderboard is a component used in multiplayer scene that shows all players' scores in a game
     */
    public Leaderboard() {
        super();
    }



    /**
     * Update scores and lives
     */
    public void update(){
        logger.info("Removing all leaderboard children");
        this.getChildren().clear();


        for (Triplet<String, String,String> triplet : records){
            String name = triplet.getFirst();
            String score = triplet.getSecond();
            String lives = triplet.getThird();

            Text text = new Text(name + ": " + score);
            if (lives.equals("DEAD")){
                text.setStrikethrough(true);
            }


            HBox hBox = new HBox();
            hBox.setAlignment(Pos.CENTER);
            hBox.getChildren().addAll(text);

            this.getChildren().add(hBox);



        }


    }

    /**
     * Get the triplet
     * @return SimpleListProperty
     */
    public SimpleListProperty<Triplet<String,String,String>> getRecords(){
        return records;
    }


}
