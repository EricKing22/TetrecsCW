package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Utility.Multimedia;
import uk.ac.soton.comp1206.Utility.Triplet;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.Leaderboard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.event.CommunicationsListener;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * The MultiplayerScene extends the ChallengeScene. It holds the UIs for multiplayer game.
 */
public class MultiplayerScene extends  ChallengeScene{

    private static final Logger logger = LogManager.getLogger(MultiplayerScene.class);

    private final ArrayList<Triplet<String, String, String>> list = new ArrayList<>();

    private SimpleListProperty<Triplet<String, String, String>> scores = new SimpleListProperty(FXCollections.observableList(list));
    private Leaderboard leaderboard;

    private Communicator communicator;
    private CommunicationsListener communicationsListener;
    private boolean gameStart = false;

    private VBox bottomBar = new VBox();
    private TextField textField;
    private SimpleBooleanProperty sendingMessage = new SimpleBooleanProperty();

    private Text text;

    private Timer getScoresTimer;

    /**
     * Create a new Single Player challenge scene
     *
     * @param gameWindow the Game Window
     */
    public MultiplayerScene(GameWindow gameWindow) {


        super(gameWindow);



        Platform.runLater(() -> {
            communicator = gameWindow.getCommunicator();
            communicator.addListener(communicationsListener);

            //Timer to get scores
            getScoresTimer = new Timer();
            getScoresTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    communicator.send("SCORES");
                }
            }, 0,2000);
        });







    }

    @Override
    public void initialise() {

        logger.info("Initialising Challenge");

        //Incoming information
        var incoming = new Text("Incoming");
        incoming.getStyleClass().add("heading");
        //Current piece grid
        pieceBoard = new PieceBoard(3,3,gameWindow.getWidth()/5,gameWindow.getWidth()/5);
        //Handle rotation of currentPiece and followingPiece when the pieceBoard is clicked
        pieceBoard.setOnBlockClick((e) -> mainBoardRightClicked());


        //Following piece grid
        followingBoard = new PieceBoard(3,3,gameWindow.getWidth()/7,gameWindow.getWidth()/7);
        followingBoard.setPadding(new Insets(20));
        //Swap currentPiece and followingPiece when the followingBoard is clicked
        followingBoard.setOnBlockClick((e) -> {
            game.swapCurrentPiece();
            pieceBoard.setPiece(game.getCurrentPiece());
            followingBoard.setPiece(game.getFollowingPiece());
        });

        /**
         * Multiplier
         */
        multiplierText = new Text();
        multiplierText.textProperty().bind(game.getMultiplier().asString());
        multiplierText.getStyleClass().add("level");

        //Left bar for current multiplier
        Text multiplier = new Text("Multiplier");
        multiplier.getStyleClass().add("heading");
        leftBar.getChildren().add(multiplier);
        leftBar.getChildren().add(multiplierText);
        leftBar.setAlignment(Pos.CENTER);
        leftBar.setPadding(new Insets(0,0,0,20));


        //Leaderboard text
        Text leaderboardText = new Text("Versus");
        leaderboardText.getStyleClass().add("heading");
        sideBar.getChildren().add(leaderboardText);

        //Leaderboard
        leaderboard = new Leaderboard();
        leaderboard.setAlignment(Pos.CENTER);
        leaderboard.getRecords().bind(scores);
        leaderboard.getStyleClass().add("leaderboard");
        sideBar.getChildren().add(leaderboard);

        //Add piece boards to the sideBar container
        sideBar.getChildren().addAll(incoming, pieceBoard, followingBoard);
        sideBar.setAlignment(Pos.CENTER);


        //Return to menu page
        scene.setOnKeyPressed(this::handleKey);

        /**
         * Score box
         */
        VBox scoreBox = new VBox();
        Label currentScore = new Label("current score");
        currentScore.getStyleClass().add("challengemodesub");
        currentScore.textProperty().set("Score");
        scoreText = new Text();
        scoreText.textProperty().bind(game.getScore().asString());


        score = new SimpleIntegerProperty();
        score.bind(game.getScore());
        score.addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                communicator.send("SCORE " + newValue.toString());
            }
        });


        scoreText.getStyleClass().add("score");
        scoreBox.getChildren().addAll(currentScore, scoreText);
        scoreBox.setAlignment(Pos.CENTER);
        topBar.getChildren().add(scoreBox);

        /**
         * Region 1 to make label at center
         */
        Region region1 = new Region();
        HBox.setHgrow(region1, Priority.ALWAYS);
        topBar.getChildren().add(region1);

        /**
         * Mode Label
         */
        Label title = new Label("title");
        title.getStyleClass().add("challengemode");
        title.textProperty().set("Challenge Mode");
        title.setAlignment(Pos.CENTER);
        title.setPadding(new Insets(40,0,0,0));
        topBar.getChildren().add(title);


        /**
         * Region 2 to make lives box at right
         */
        Region region2 = new Region();
        HBox.setHgrow(region2,Priority.ALWAYS);
        topBar.getChildren().add(region2);

        /**
         * Lives box
         */
        VBox livesBox = new VBox();
        Label currentLives = new Label("current lives");
        currentLives.getStyleClass().add("challengemodesub");
        currentLives.textProperty().set("Lives");
        livesText = new Text();
        livesText.textProperty().bind(game.getLives().asString());
        livesText.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                communicator.send("LIVES " + newValue);
                if(newValue.equals("-1")){
                    communicator.send("DIE");
                }
            }
        });
        livesText.getStyleClass().add("lives");
        livesBox.getChildren().addAll(currentLives, livesText);
        livesBox.setAlignment(Pos.CENTER);
        topBar.getChildren().add(livesBox);


        /**
         * Time bar
         */
        resetTimeBar();

    }

    /**
     * Set up game
     */
    @Override
    public void setupGame() {
        logger.info("Setting up game");
        game = new MultiplayerGame(5,5);
    }

    @Override
    public void build() {
        super.build();
        setCommunicationsListener(this::receivedMessage);

        text = new Text();
        text.setText("Chat channel. Press T to send message");
        text.getStyleClass().add("instructions");
        bottomBar.getChildren().add(text);

        textField = new TextField();
        textField.getStyleClass().add("textField");
        textField.setPrefWidth(board.getPrefWidth());
        textField.setPromptText("Enter message to send:");
        textField.visibleProperty().bind(sendingMessage);
        bottomBar.getChildren().add(textField);

        bottomBar.getChildren().add(timeBar);
        bottomBar.setAlignment(Pos.CENTER);
        mainPane.setBottom(bottomBar);




    }

    @Override
    protected void blockClicked(GameBlock gameBlock) {
        super.blockClicked(gameBlock);
        communicator.send("PIECE");
    }

    /**
     * Override to make gameWindow open a online score scene rather than a local score scene
     */
    @Override
    protected void loadScoreScene() {
        //Stop requesting scores
        getScoresTimer.cancel();
        //Start scene for multiplayer score scene
        Platform.runLater(() -> gameWindow.startOnlineScore((MultiplayerGame) game, new ArrayList<>(scores)));
    }

    /**
     * Handle information received
     */
    private void receivedMessage(String message){
        //Receive piece
        if(message.contains("PIECE")){
            String pieceName = message.split(" ")[1];
            //logger.info("Adding multi player pieces queue");
            game.getPieces().add(pieceName);
            //logger.info("PIECES is now at size {}", game.getPieces().size());

            if (game.getPieces().size() == 2 && !gameStart){
                game.start();
                gameStart = true;
            }

            if (game.getPieces().size() < 4){
                communicator.send("PIECE");
            }
        }

        //Receive message
        else if(message.contains("MSG")){

            message = message.split("MSG ")[1];
            Multimedia.playAudio("sounds/message.wav");
            String playerName = message.split(":")[0];
            String receivedMessage = message.split(":")[1];
            Platform.runLater(() -> {text.setText(playerName + ": " + receivedMessage);});


        }

        //Receive scores
        else if(message.contains("SCORES") & !message.contains("HISCORES")){
            scores.clear();

            message = message.split("SCORES ")[1];

            String[] lines;

            //Split lines
            if (message.contains("\n")){
                lines = message.split("\n");
            }
            else{
                lines = new String[] {message};
            }

            //Update for each line
            for (String line : lines){
                String name = line.split(":")[0];
                String score = line.split(":")[1];
                String lives = line.split(":")[2];

                scores.add(new Triplet<>(name, score, lives));
            }

            Platform.runLater(() -> leaderboard.update());




        }
    }


    /**
     * Initialise communicator listener
     * @param listener
     */
    private void setCommunicationsListener(CommunicationsListener listener){
        communicationsListener = listener;
    }


    /**
     * Handel when a key is pressed
     */
    @Override
    protected void handleKey(KeyEvent event) {
        super.handleKey(event);

        //Start sending message
        if (event.getCode() == KeyCode.T){
            sendingMessage.set(true);
        }

        //Send message
        else if(event.getCode() == KeyCode.ENTER){
            if (sendingMessage.get()){
                communicator.send("MSG " + textField.getText());
                sendingMessage.set(false);
                textField.clear();
            }
        }

        //If escape
        if(event.getCode() == KeyCode.ESCAPE){

            if (gameStart){
                communicator.send("DIE");
            }
            communicator.send("PART");
            getScoresTimer.cancel();
        }



    }
}
