package uk.ac.soton.comp1206.component;

import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Application;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.scene.ScoresScene;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A VBox that shows players' scores.
 * Used by ScoreScene to show rankings of local scores and online scores
 */
public class ScoresList extends VBox {
    /**
     * Logger
     */
    private static final Logger logger = LogManager.getLogger(ScoresList.class);
    /**
     * SimpleListProperty of scores
     */
    SimpleListProperty<Pair<String, Integer>> scores;
    /**
     * Array of that Colours the text can choose from
     */
    protected static final Color[] COLOURS = {
            Color.DEEPPINK,
            Color.RED,
            Color.ORANGE,
            Color.YELLOW,
            Color.YELLOWGREEN,
            Color.LIME,
            Color.GREEN,
            Color.DARKGREEN,
            Color.DARKTURQUOISE,
            Color.AQUA,
            Color.AQUAMARINE,
            Color.BLUE,
            Color.MEDIUMPURPLE,
            Color.PURPLE
    };

    /**
     * Override the ScoresList
     * Add initialise the scores
     */
    public ScoresList(){
        super();
        scores= new SimpleListProperty();

    }


    /**
     * Animate the display of the scores
     */
    public void reveal(){

        //logger.info("Animating scores");
        Text title = new Text();
        this.getChildren().add(title);


        if (getId().equals("local")){
            title.setText("Local Scores");
            title.getStyleClass().add("scorelist");
            title.setFill(Color.WHITE);
        }
        else if(getId().equals("remote")){
            title.setText("Online Scores");
            title.getStyleClass().add("scorelist");
            title.setFill(Color.WHITE);
        }
        else if(getId().equals("lobby")){
            title.setText("This game");
            title.getStyleClass().add("scorelist");
            title.setFill(Color.WHITE);
        }


        List<Pair<String,Integer>> list = scores.stream().toList();

        //Set alignment center
        this.setAlignment(Pos.CENTER);

        SequentialTransition sequentialTransition = new SequentialTransition();


        //Add title animation
        FadeTransition ft1 = new FadeTransition(new Duration(1000),title);
        ft1.setFromValue(0.0);
        ft1.setToValue(1.0);
        sequentialTransition.getChildren().add(ft1);



        //Set animation of all scores in list
        for (Pair<String,Integer> pair : list){
            var player = new Text(pair.toString().replace("=", ": "));
            player.getStyleClass().add("scorelist");
            player.setFill(COLOURS[new Random().nextInt(0,COLOURS.length)]);
            this.getChildren().add(player);

            FadeTransition ft = new FadeTransition(new Duration(500), player);
            ft.setFromValue(0.0);
            ft.setToValue(1.0);
            sequentialTransition.getChildren().add(ft);
        }

        //Play the animation one by one
        sequentialTransition.play();
    }

    /**
     * Get the scores property
     * @return SimpleListProperty
     */
    public SimpleListProperty getScores(){
        return scores;
    }
}
