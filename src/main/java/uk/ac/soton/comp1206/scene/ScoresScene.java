package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Utility.Multimedia;
import uk.ac.soton.comp1206.component.ScoresList;
import uk.ac.soton.comp1206.event.CommunicationsListener;
import uk.ac.soton.comp1206.event.DisplayListener;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

/**
 * The ScoreScene shows rankings of local scores and online scores from the server.
 */
public class ScoresScene extends BaseScene{
    private static final Logger logger = LogManager.getLogger(ScoresScene.class);

    /**
     * Arraylist to store local scores
     */
    protected ArrayList<Pair<String, Integer>> localScores = new ArrayList<>(){};

    /**
     * Arraylist to store online scores
     */
    protected ArrayList<Pair<String, Integer>> remoteScores = new ArrayList<>();
    /**
     * SimpleListProperty for scores loaded from local
     */
    protected final SimpleListProperty localRank = new SimpleListProperty<>(FXCollections.observableList(localScores));
    /**
     * SimpleListProperty for scores from online
     */
    protected final SimpleListProperty remoteRank = new SimpleListProperty<>(FXCollections.observableList(remoteScores));
    /**
     * ScoreList for local scores
     */
    protected ScoresList scoresList;
    /**
     * ScoreList for online scores
     */
    protected ScoresList remoteScoresList;

    private Integer gameScore;

    /**
     * Text field for user to enter name
     */
    protected TextField name;
    /**
     * Communicator for the server
     */
    protected Communicator communicator;
    /**
     * Listener that triggered when the communicator receives message
     */
    protected CommunicationsListener listener;

    /**
     * Check if the player's score is higher than the lowest in the local file
     */
    protected boolean ifHighScore = false;

    /**
     * Container contains all elements
     */
    protected VBox container;



    /**
     * Create a new scene, passing in the GameWindow the scene will be displayed in
     * @param gameWindow the game window
     * @param game the game information that is passed in
     */
    public ScoresScene(GameWindow gameWindow, Game game) {
        super(gameWindow);


        if(!(this instanceof OnlineScoreScene)){
            //Pass game information
            this.gameScore = game.getScore().getValue();
            //Load scores from local file
            loadScores();
            //Check if this score is higher than any of the local scores
            for (Pair<String,Integer> pair: localScores){
                if (pair.getValue() < gameScore){
                    ifHighScore = true;
                    break;
                }
            }
        }



    }


    /**
     * Override the initialise method
     * Add communicator and request High scores from the server
     * Add key pressed event handler
     */
    @Override
    public void initialise() {

        //Initialise communicator
        communicator = gameWindow.getCommunicator();
        communicator.addListener(listener);
        communicator.send("HISCORES");


        /**
         * Handle key event
         */
        scene.setOnKeyPressed(this::handleKey);



    }

    /**
     * Override the build method, add GameOver information
     */
    @Override
    public void build(){

        /**
         * Handle scores received
         */
        setReceived(this::loadOnlineScores);

        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var ScorePane = new StackPane();
        ScorePane.setMaxWidth(gameWindow.getWidth());
        ScorePane.setMaxHeight(gameWindow.getHeight());
        ScorePane.getStyleClass().add("score-background");
        root.getChildren().add(ScorePane);

        /**
         * Container as Vertical Box to contain all elements
         */
        container = new VBox();
        container.setAlignment(Pos.CENTER);
        ScorePane.getChildren().add(container);

        /**
         * Title image
         */
        Image titleImage = new Image(getClass().getResource("/images/TetrECS.png").toExternalForm());
        var imageView = new ImageView(titleImage);
        imageView.setFitWidth(600);
        imageView.setPreserveRatio(true);
        var imagePane = new BorderPane(imageView);
        imagePane.setPrefSize(imageView.getFitWidth(), imageView.getFitHeight());
        container.getChildren().add(imagePane);

        /**
         * Game Over and High Scores text
         */
        Text gameOverText = new Text("GAME OVER");
        gameOverText.getStyleClass().add("bigtitle");
        container.getChildren().add(gameOverText);
        Text highScores = new Text("High Scores");
        highScores.getStyleClass().add("title");
        container.getChildren().add(highScores);


        /**
         * Initialise scoreList component
         * Bind scoreList component's scores with list-wrapper list
         */
        scoresList = new ScoresList();
        scoresList.setId("local");
        scoresList.getScores().bind(localRank);

        /**
         * Initialise remoteScoreList component
         * Bind scoreList component's scores with list-wrapper list
         */
        remoteScoresList = new ScoresList();
        remoteScoresList.setId("remote");
        remoteScoresList.getScores().bind(remoteRank);



        /**
         * If the player's score is higher thn the lowest score in the local file
         * then show textFiled and ask for name
         * else show local scores rank
         */
        if (ifHighScore){
            //Remove last from local scores
            localScores.remove(9);
            /**
             * VBox promptBox for prompt information
             */
            VBox promptBox = new VBox();
            promptBox.setAlignment(Pos.CENTER);

            //Text
            Text text = new Text("You have got a high score");
            text.getStyleClass().add("mediumtitle");

            //TextField
            name = new TextField();
            name.requestFocus();
            name.setPrefWidth(gameWindow.getWidth());
            name.setPromptText("Enter your name");

            //Button
            Button button = new Button();
            button.setText("Submit");
            button.setOnAction((event) -> {
                container.getChildren().remove(promptBox);
                writeScores(name.getText(), gameScore);
                writeOnlineScore();
                //Add this new pair to localScores
                localScores.add(new Pair<>(name.getText(),gameScore));
                display();
            });

            promptBox.getChildren().addAll(text, name, button);

            container.getChildren().add(promptBox);

        }





    }

    /**
     * Load online scores into remoteRank simpleListProperty
     * @param message String score message that is received from the server
     */
    protected void loadOnlineScores(String message){

        logger.info("Load online scores");
        //Return if no HISCORES information is included
        if (!message.contains("HISCORES ")){return;}

        message = message.split("HISCORES ")[1];
        for (String line : message.split("\n")){
            String name = line.split(":")[0];
            Integer score = Integer.parseInt(line.split(":")[1]);
            remoteRank.add(new Pair<String,Integer>(name, score));
        }


        Platform.runLater(() -> {
            buildScoreBox();
            if(!ifHighScore || this instanceof OnlineScoreScene){
                display();
            }
        });




    }

    /**
     * Send new scores
     */
    protected void writeOnlineScore(){
        for(Pair<String, Integer> pair : remoteScores){
            //Check if this user's score is higher than any one of the scores online
            if(pair.getValue() < gameScore && this.name.getText()!=null){
                String sendText = "HISCORE "  + this.name.getText() + ":" + gameScore.toString();
                //remove the last score to keep the rank only contains top ten
                remoteRank.remove(9);
                remoteRank.add(new Pair<String,Integer>(this.name.getText(), gameScore));
                communicator.send(sendText);
                break;
            }
        }
    }

    /**
     * Display scores
     */
    protected void display(){
        logger.info("Displaying");
        // Sort the list based on the integer value in each pair
        Collections.sort(localScores, new Comparator<Pair<String, Integer>>() {
            @Override
            public int compare(Pair<String, Integer> p1, Pair<String, Integer> p2) {
                return p2.getValue().compareTo(p1.getValue()); // Descending order
            }
        });


        // Sort the list based on the integer value in each pair
        Collections.sort(remoteScores, new Comparator<Pair<String, Integer>>() {
            @Override
            public int compare(Pair<String, Integer> p1, Pair<String, Integer> p2) {
                return p2.getValue().compareTo(p1.getValue()); // Descending order
            }
        });


        //Animation of scores
        scoresList.reveal();
        remoteScoresList.reveal();
    }

    /**
     * Load scores and names from local file "localScores.txt"
     */
    protected void loadScores(){
        try {
            //logger.info("Loading scores from local file");

            File file = new File("localScores.txt");

            if (!file.exists()){ //If file doesn't exist
                FileWriter fileWriter = new FileWriter(file);
                //Adds five default lines
                for (int i = 0; i < 5; i++){
                    fileWriter.write("Default:0");
                }
            }

            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            ArrayList<Pair<String,Integer>> collection = new ArrayList<>();
            while ((line = br.readLine()) != null ) {
                String name = line.split(":")[0];
                String score = line.split(":")[1];

                collection.add(new Pair<>(name,Integer.parseInt(score)));

            }

            //If there is not enough scores, add up to 10
            while(collection.size() < 10){
                collection.add(new Pair<>("Default", 0));
            }

            //Sort collection
            Collections.sort(collection, new Comparator<Pair<String, Integer>>() {
                @Override
                public int compare(Pair<String, Integer> p1, Pair<String, Integer> p2) {
                    return p2.getValue().compareTo(p1.getValue()); // Descending order
                }
            });

            for (Pair pair : collection){
                //Only show top 10 scores from local files
                if (localScores.size() == 10){break;}
                localScores.add(pair);
            }

            br.close();


        } catch (IOException e) {
            logger.error("File input unsuccessful");
            e.printStackTrace();
        }
    }

    /**
     * Write name and score to the file "localScores.txt"
     * @param name String name of the player
     * @param score Integer of the score the player get
     */
    protected void writeScores(String name, Integer score){
        String text = name + ":" + score.toString();
        try {
            logger.info("Writing to the file");
            FileWriter writer = new FileWriter("localScores.txt",true);
            BufferedReader reader = new BufferedReader(new FileReader("localScores.txt"));
            if (reader.readLine() == null){
                writer.write(text);
                writer.close();
            }
            else{
                writer.write("\n" + text);
                writer.close();
            }

            writer.close();
        } catch (IOException e) {
            logger.error("File output unsuccessful");
            e.printStackTrace();
        }
    }

    /**
     * Build local and remote scores
     */
    protected void buildScoreBox(){
        logger.info("Building local and remote score boxes");
        /**
         * HBox for local scores and online scores
         */
        Region region1 = new Region();
        HBox.setHgrow(region1, Priority.ALWAYS);
        Region region2 = new Region();
        HBox.setHgrow(region2,Priority.ALWAYS);
        Region region3 = new Region();
        HBox.setHgrow(region3,Priority.ALWAYS);

        HBox scoresBox = new HBox();
        scoresBox.getChildren().add(region1);
        scoresBox.getChildren().add(scoresList);
        scoresBox.getChildren().add(region2);
        scoresBox.getChildren().add(remoteScoresList);
        scoresBox.getChildren().add(region3);

        container.getChildren().add(scoresBox);


    }

    /**
     * Handle CommunicationsListener
     * @param listener Handle received messages when this listener is triggered
     */
    protected void setReceived(CommunicationsListener listener){
        this.listener = listener;
    }


    /**
     * Handle key event
     * @param event Keyevent
     */
    protected void handleKey(KeyEvent event){
        //Return to menu
        if (event.getCode() == KeyCode.ESCAPE){
            logger.info("Return to Menu scene");
            Multimedia.stop();
            gameWindow.startMenu();
        }
    }
}
