package uk.ac.soton.comp1206.scene;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.Pair;
import javafx.util.converter.NumberStringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Utility.Multimedia;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.io.*;
import java.util.ArrayList;

/**
 * The Single Player challenge scene. Holds the UI for the single player challenge mode in the game.
 */
public class ChallengeScene extends BaseScene {
    /**
     * Logger
     */
    private static final Logger logger = LogManager.getLogger(ChallengeScene.class);
    /**
     * Game
     */
    protected Game game;
    /**
     * pieceBoard to show the next game piece
     */
    protected PieceBoard pieceBoard;
    /**
     * Sidebar contains high score, level and board for the next game piece
     */
    protected final VBox sideBar = new VBox();

    /**
     * Left bar for current Multiplier
     */
    protected final VBox leftBar = new VBox();

    /**
     * Topbar contains score, title and lives
     */
    protected final HBox topBar = new HBox();

    /**
     * Text information about score, level and lives
     */
    protected Text scoreText;

    /**
     * SimpleIntegerProperty score used to bind for UI
     */
    protected SimpleIntegerProperty score;
    /**
     * Level text
     */
    protected Text levelText;
    /**
     * Lives text
     */
    protected Text livesText;
    /**
     * HighScore text
     */
    protected Text highScoreText;
    /**
     * Multiplier text
     */
    protected Text multiplierText;
    /**
     * HighestScore text
     */
    protected SimpleStringProperty highestScore;
    /**
     * Main game board
     */
    protected GameBoard board;

    /**
     * A smaller piece board to show the following gamep piece
     */
    protected PieceBoard followingBoard;

    /**
     * TimeBar to show the rest of the time
     */
    protected Rectangle timeBar;

    /**
     * StackPane ChallengePane
     */
    protected StackPane challengePane;

    /**
     * Main boarderPane
     */
    protected BorderPane mainPane;

    /**
     * Pause BoarderPane
     */
    protected BorderPane pausePane;

    /**
     * Parallel transition
     */
    private ParallelTransition parallel;



    /**
     * Create a new Single Player challenge scene
     * @param gameWindow the Game Window
     */
    public ChallengeScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Challenge Scene");
    }

    /**
     * Build the Challenge window
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        setupGame();

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        challengePane = new StackPane();
        challengePane.setMaxWidth(gameWindow.getWidth());
        challengePane.setMaxHeight(gameWindow.getHeight());
        challengePane.getStyleClass().add("menu-background");
        root.getChildren().add(challengePane);

        pausePane = new BorderPane();
        pausePane.setMaxWidth((double)gameWindow.getWidth()/2);
        pausePane.setMaxHeight((double)gameWindow.getHeight()/2);
        pausePane.getStyleClass().add("pause-background");

        mainPane = new BorderPane();
        challengePane.getChildren().add(mainPane);

        //Set topBar for title, lives and scores
        mainPane.setTop(topBar);

        //Set sidebar to the right and padding 30 to the surroundings
        mainPane.setRight(sideBar);

        //Set timeBar to the bottom of the pane
        timeBar = new Rectangle(gameWindow.getWidth(),20);
        timeBar.setFill(Color.GREEN);
        mainPane.setBottom(timeBar);

        board = new GameBoard(game.getGrid(),gameWindow.getWidth()/2,gameWindow.getWidth()/2);

        mainPane.setCenter(board);

        mainPane.setLeft(leftBar);

        //Handle block on gameboard grid being clicked
        board.setOnBlockClick(this::blockClicked);

        //Handle rotation of currentPiece and followingPiece when the board is right-clicked
        board.setOnRightClicked(this::mainBoardRightClicked);

        //Handle display next game piece to pieceBoard and reset timeBar
        game.setNextPieceListener((event,e) -> {
            resetTimeBar();
            pieceBoard.setPiece(game.getCurrentPiece());
            followingBoard.setPiece(game.getFollowingPiece());
        });

        //Handle to clear the line
        game.setLineClearedListener((coordinateSet) -> {
            board.fadeOut(coordinateSet);
        });

        //Handle new game loop started
        game.setOnGameLoop(this::resetTimeBar);

        //Handle game over
        game.setGameOver(this::loadScoreScene);

    }

    /**
     * Handle when the main game board is right-clicked
     */
    protected void mainBoardRightClicked(){
        game.rotateCurrentPiece();
        //Update pieceBoard and followingBoard
        pieceBoard.setPiece(game.getCurrentPiece());
    }


    /**
     * Handle when a block is clicked
     * @param gameBlock the Game Block that was clocked
     */
    protected void blockClicked(GameBlock gameBlock) {
        game.blockClicked(gameBlock);
    }

    /**
     * Setup the game object and model
     */
    public void setupGame() {
        logger.info("Starting a new challenge");

        //Start new game
        game = new Game(5, 5);


    }

    /**
     * Initialise the scene and start the game
     * Create and bind text variables
     */
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
         * Current level
         */
        levelText = new Text();
        levelText.textProperty().bind(game.getLevel().asString());
        levelText.getStyleClass().add("level");

        /**
         * Multiplier
         */
        multiplierText = new Text();
        multiplierText.textProperty().bind(game.getMultiplier().asString());
        multiplierText.getStyleClass().add("level");

        /**
         * High score to beat
         */
        highestScore = getHighScore();

        highScoreText = new Text();
        highScoreText.textProperty().bind(highestScore);
        highScoreText.getStyleClass().add("hiscore");

        //Add high score to sideBar
        Text highScore = new Text("High Score");
        highScore.getStyleClass().add("heading");
        sideBar.getChildren().add(highScore);
        sideBar.getChildren().add(highScoreText);


        //Add level information to sidebar
        Text level = new Text("Level");
        level.getStyleClass().add("heading");
        sideBar.getChildren().add(level);
        sideBar.getChildren().add(levelText);

        //Add piece boards to the sideBar container
        sideBar.getChildren().add(incoming);
        sideBar.getChildren().add(pieceBoard);
        sideBar.getChildren().add(followingBoard);

        sideBar.setAlignment(Pos.CENTER);

        //Left bar for current multiplier
        Text multiplier = new Text("Multiplier");
        multiplier.getStyleClass().add("heading");
        leftBar.getChildren().add(multiplier);
        leftBar.getChildren().add(multiplierText);
        leftBar.setAlignment(Pos.CENTER);
        leftBar.setPadding(new Insets(0,0,0,20));




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

        //Handle when score exceeds the highest score
        score = new SimpleIntegerProperty();
        score.bind(game.getScore());
        score.addListener((obs, oldVal, newVal) -> {
            if (newVal.intValue() > Integer.parseInt(highestScore.get())){
                highScoreText.textProperty().bind(game.getScore().asString());
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
        HBox.setHgrow(region1,Priority.ALWAYS);
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
        livesText.getStyleClass().add("lives");
        livesBox.getChildren().addAll(currentLives, livesText);
        livesBox.setAlignment(Pos.CENTER);
        topBar.getChildren().add(livesBox);


        /**
         * Time bar
         */
        resetTimeBar();


        game.start();
    }

    /**
     * Handle what happens when a key is pressed
     * @param event key pressed
     */
    protected void handleKey(KeyEvent event){
        //Return to menu
        if (event.getCode() == KeyCode.ESCAPE){
            logger.info("Return to Menu scene");
            game.getTimeline().stop();
            Multimedia.stop();
            gameWindow.startMenu();
        }
        //Place current piece (keyboard support)
        else if(event.getCode() == KeyCode.SPACE){
            GameBlockCoordinate currentCoordinate = board.getCurrentCoordinate();
            game.blockClicked(board.getBlock(currentCoordinate.getX(),currentCoordinate.getY()));
        }
        //Rotation (keyboard support)
        else if(event.getCode() == KeyCode.Z){
            game.rotateCurrentPiece();
            //Update pieceBoard
            pieceBoard.setPiece(game.getCurrentPiece());
        }
        //Switch currentPiece & followingPiece
        else if(event.getCode() == KeyCode.R){
            game.swapCurrentPiece();
            //Update pieceBoard & followingBoard
            pieceBoard.setPiece(game.getCurrentPiece());
            followingBoard.setPiece(game.getFollowingPiece());
        }

        //Mute sound
        else if(event.getCode() == KeyCode.M){
            Multimedia.turnSilentMode();
        }

        //Hoover effect (keyboard support)
        else {
            GameBlockCoordinate currentCoordinate = board.getCurrentCoordinate();
            board.getBlock(currentCoordinate.getX(),currentCoordinate.getY()).hooverOff();

            if (event.getCode() == KeyCode.W || event.getCode() == KeyCode.UP) {
                logger.info("Key W/UP pressed. Decrement y-value by 1");
                if(currentCoordinate.getY()>0) {
                    currentCoordinate = currentCoordinate.subtract(0, 1);
                }
            } else if (event.getCode() == KeyCode.A || event.getCode() == KeyCode.LEFT) {
                logger.info("Key A/LEFT pressed. Decrement x-value by 1");
                if(currentCoordinate.getX()>0) {
                    currentCoordinate = currentCoordinate.subtract(1, 0);
                }
            } else if (event.getCode() == KeyCode.S || event.getCode() == KeyCode.DOWN) {
                logger.info("Key S/DOWN pressed. Increment y-value by 1");
                if(currentCoordinate.getY()<board.getColumnCount()-1) {
                    currentCoordinate = currentCoordinate.add(0, 1);
                }
            } else if (event.getCode() == KeyCode.D || event.getCode() == KeyCode.RIGHT) {
                logger.info("Key D/RIGHT pressed. Increment x-value by 1");
                if(currentCoordinate.getX()<board.getRowCount()-1) {
                    currentCoordinate = currentCoordinate.add(1, 0);
                }
            }
            logger.info("New coordinate: {}",currentCoordinate);
            board.setCurrentCoordinate(currentCoordinate);
            board.getBlock(currentCoordinate.getX(),currentCoordinate.getY()).hooverOn();

        }
    }

    /**
     * Reset the time bar
     */
    protected void resetTimeBar(){
        logger.info("Resetting time bar");

        parallel = new ParallelTransition(doScale(),doFill());

        parallel.play();
    }

    /**
     * Transition to scale the time bar
     * @return scaleTransition
     */
    protected Transition doScale(){
        int duration = game.getTimerDelay().get();
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(duration), timeBar);


        // Set the pivot point to the left edge of the rectangle so that it expands to the right
        scaleTransition.setFromX(1.0);
        scaleTransition.setToX(0.0);


        // Disable scaling along the y-axis to keep the height fixed
        scaleTransition.setByY(0.0);



        return scaleTransition;
    }

    /**
     * Transition to change timeBar's fill color from Green to Red
     * @return fillTransition
     */
    protected Transition doFill(){
        int duration = game.getTimerDelay().get();

        FillTransition fillTransition = new FillTransition(Duration.millis(duration), timeBar);

        //From color Green to color red
        fillTransition.setFromValue(Color.GREEN);
        fillTransition.setToValue(Color.RED);

        return fillTransition;
    }

    /**
     * Load score scene when game over
     */
    protected void loadScoreScene(){
        Platform.runLater(() ->  gameWindow.startScore(game));
    }

    /**
     * Get high score to beat
     */
    private SimpleStringProperty getHighScore(){

        Integer highScore = 0;

        try {
            logger.info("Loading scores from local file");

            File file = new File("localScores.txt");

            if (!file.exists()){ //If file doesn't exist
                FileWriter fileWriter = new FileWriter(file,true);
                //Adds five default lines
                fileWriter.write("Default:0");
                for (int i = 0; i < 8; i++){
                    fileWriter.write("\n" + "Default:0");
                }
                fileWriter.close();
            }

            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                Integer score = Integer.parseInt(line.split(":")[1]);
                if (score > highScore){
                    highScore = score;
                }
            }
            br.close();


        } catch (IOException e) {
            logger.error("File input unsuccessful");
            e.printStackTrace();
        }

        return new SimpleStringProperty(highScore.toString());
    }




}
