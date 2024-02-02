package uk.ac.soton.comp1206.game;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.shape.Line;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Utility.Multimedia;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.event.GameLoopListener;
import uk.ac.soton.comp1206.event.GameOverListener;
import uk.ac.soton.comp1206.event.LineClearedListener;
import uk.ac.soton.comp1206.event.NextPieceListener;

import java.util.*;


/**
 * The Game class handles the main logic, state and properties of the TetrECS game. Methods to manipulate the game state
 * and to handle actions made by the player should take place inside this class.
 */
public class Game {

    private static final Logger logger = LogManager.getLogger(Game.class);

    /**
     * Number of rows
     */
    protected final int rows;

    /**
     * Number of columns
     */
    protected final int cols;

    /**
     * The grid model linked to the game
     */
    protected final Grid grid;

    /**
     * Current piece
     */
    protected GamePiece currentPiece;

    /**
     * Following piece
     */
    protected GamePiece followingPiece;

    /**
     * Score
     */
    protected SimpleIntegerProperty score = new SimpleIntegerProperty(0);

    /**
     * Level
     */
    protected SimpleIntegerProperty level = new SimpleIntegerProperty(0);

    /**
     * Lives
     */
    protected SimpleIntegerProperty lives = new SimpleIntegerProperty(3);


    /**
     * Multiplier
     */
    protected SimpleIntegerProperty multiplier = new SimpleIntegerProperty(1);

    /**
     * Timer Delay
     */
    protected SimpleIntegerProperty timerDelay = new SimpleIntegerProperty(0);

    /**
     * Next piece listener
     */
    protected NextPieceListener nextPieceListener;

    /**
     * Line cleared listener
     */
    protected LineClearedListener lineClearedListener;

    /**
     * Game loop restarts listener
     */
    protected GameLoopListener gameLoopListener;

    /**
     * Game over listener
     */
    protected GameOverListener gameOverListener;

    /**
     * Random number
     */
    protected final Random random = new Random();

    /**
     * Timeline
     */
    protected Timeline timeline;

    /**
     * The list of pieces received from server
     */
    protected Queue<String> pieces = new LinkedList<>();

    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     * @param cols number of columns
     * @param rows number of rows
     */
    public Game(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;

        //Create a new grid model to represent the game state
        this.grid = new Grid(cols,rows);

    }

    /**
     * Start the game
     */
    public void start() {
        logger.info("Starting game");

        initialiseGame();
    }

    /**
     * Initialise a new game and set up anything that needs to be done at the start
     */
    public void initialiseGame() {
        //logger.info("Initialising game");
        Multimedia.playBGM("music/game_start.wav");
        currentPiece = spawnPiece();
        followingPiece = spawnPiece();
        //Call the next piece listener to show the next piece on the pieceBoard
        nextPieceListener.nextPiece(currentPiece, followingPiece);

        //Initialise timeline to call gameLoop()
        timeline = new Timeline(new KeyFrame(Duration.millis(getTimerDelay().get()), event -> {
            gameLoop();
        }));

        // Set the cycle count to infinite and play the timeline
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

    }

    /**
     * gameLoop to decrement lives by one
     * Discard currentPiece
     *
     */
    protected void gameLoop(){
        //logger.info("New game loop is started");
        //New game loop started
        gameLoopListener.gameLooped();

        //Decrease live by one
        logger.info("Lives decreases by 1");
        lives.set(lives.get()-1);
        Multimedia.playAudio("sounds/lifelose.wav");
        nextPiece();

        //Stop old timeLine
        timeline.stop();

        timeline = new Timeline(new KeyFrame(Duration.millis(getTimerDelay().get()), event -> {
            gameLoop();
        }));

        // Set the cycle count to infinite and play the timeline
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();


        //If live belows zero stops timeline
        if (lives.get() == -1){
            timeline.stop();
            gameOverListener.gameOver();
        }

    }

    /**
     * Clear any full vertical / horizontal lines
     * Use hashset so no block resets repeatedly
     */
    public void afterPiece(){
        int sum = 0;
        int numClearLines = 0;
        int numClearBlocks = 0;

        //Stop old timeline
        timeline.stop();
        //Reset Timeline
        timeline = new Timeline(new KeyFrame(Duration.millis(getTimerDelay().get()), event -> {
            gameLoop();
        }));
        // Set the cycle count to infinite and play the timeline
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        //Hashset to store the coordinates of the blocks to be reset
        HashSet<GameBlockCoordinate> clearList = new HashSet<>();

        //Check full horizontal
        for(int r = 0; r < rows; r++){
            for(int c = 0; c < cols; c++){
                if (grid.get(c,r)!=0) {
                    sum = sum + 1;
                }
            }
            //If this horizontal line is full
            if (sum == cols){
                numClearLines++;
                for (int c = 0; c < cols; c++){
                    GameBlockCoordinate gameBlockCoordinate = new GameBlockCoordinate(c,r);
                    //Check if this coordinate is already stored in the hashset
                    for (GameBlockCoordinate g : clearList){
                        if (g.equals(gameBlockCoordinate)){
                            break;
                        }
                    }
                    clearList.add(gameBlockCoordinate);

                }
            }
            sum = 0;
        }

        //Check full vertical
        for(int c = 0; c < cols; c++){
            for(int r = 0; r < rows;r++){
                if (grid.get(c,r)!=0) {
                    sum = sum + 1;
                }
            }
            //If this vertical line is full
            if (sum == cols){
                numClearLines++;
                for (int r = 0; r < rows; r++){
                    GameBlockCoordinate gameBlockCoordinate = new GameBlockCoordinate(c,r);
                    //Check if this coordinate is already stored in the hashset
                    for (GameBlockCoordinate g : clearList){
                        if (g.equals(gameBlockCoordinate)){
                            break;
                        }
                    }
                    clearList.add(gameBlockCoordinate);
                }
            }
            sum = 0;
        }

        //Number of blocks to be cleared
        numClearBlocks = clearList.size();

        //Trigger line cleared listener
        lineClearedListener.lineCleared(clearList);


        //Clear blocks
        for (GameBlockCoordinate coordinate : clearList) {
            //logger.info("Clearing block at {} {}.", coordinate.getX(), coordinate.getY());
            //logger.info("Clearing {} lines.", numClearLines);
            grid.set(coordinate.getX(), coordinate.getY(), 0);
        }

        //Calculate score
        if(numClearLines != 0){
            //logger.info("Adding score.");
            score.set(score.get() + (numClearLines * numClearBlocks * 10 * multiplier.get()));
            multiplier.set(multiplier.get()+1);
            Multimedia.playAudio("sounds/clear.wav");

        }

        //Modify multiplier if no line is cleared
        else if(numClearLines == 0){
            //logger.info("Resetting multiplier.");
            multiplier.set(1);
        }

        //Modify level
        //logger.info("Modifying level.");
        level.set(score.get() / 1000);

    }

    /**
     * Generate a random new piece
     * @return gamePiece
     */
    public GamePiece spawnPiece(){
        return GamePiece.createPiece(random.nextInt(0,15));
    }

    /**
     * Replace the current piece with a new piece
     * Call the nextPiece listener
     */
    public void nextPiece(){
        //logger.info("Generating next game piece");
        currentPiece = followingPiece;
        followingPiece = spawnPiece();
        //Call the nextPiece listener
        nextPieceListener.nextPiece(currentPiece, followingPiece);
    }

    /**
     * Handle the new game piece when it's generated
     * @param listener triggered when next game piece is generated
     */
    public void setNextPieceListener(NextPieceListener listener){nextPieceListener = listener;}

    /**
     * Handle the clearing of the lines
     * @param listener triggers when a line is cleared
     */
    public void setLineClearedListener(LineClearedListener listener){lineClearedListener = listener;}

    /**
     * Handle the gameLoop restarts
     * @param listener triggers when the time runs out and game is looped
     */
    public void setOnGameLoop(GameLoopListener listener){gameLoopListener = listener;}

    /**
     * Handle the game over
     * @param listener triggers when the game is over
     */
    public void setGameOver(GameOverListener listener){gameOverListener = listener;}

    /**
     * Handle what should happen when a particular block is clicked
     * @param gameBlock the block that was clicked
     */
    public void blockClicked(GameBlock gameBlock) {
        //Get the position of this block
        int x = gameBlock.getX();
        int y = gameBlock.getY();

        if(grid.canPlayPiece(currentPiece,x,y)){
            //logger.info("Playing piece at {}-{}", x,y);
            grid.playPiece(currentPiece,x,y);
            //logger.info("Playing place sound");
            Multimedia.playAudio("sounds/place.wav");
            afterPiece();
            nextPiece();
        }
        else{
            logger.info("Unable to place a piece here.");
            Multimedia.playAudio("sounds/fail.wav");
        }
    }

    /**
     * Rotate the current piece
     */
    public void rotateCurrentPiece(){
        //logger.info("Rotating current piece");
        currentPiece.rotate();
        //logger.info("Playing rotation sound");
        Multimedia.playAudio("sounds/rotate.wav");
    }


    /**
     * Get the grid model inside this game representing the game state of the board
     * @return game grid model
     */
    public Grid getGrid() {
        return grid;
    }

    /**
     * Get the number of columns in this game
     * @return number of columns
     */
    public int getCols() {
        return cols;
    }

    /**
     * Get the number of rows in this game
     * @return number of rows
     */
    public int getRows() {
        return rows;
    }

    /**
     * Get current piece
     * @return current piece
     */
    public GamePiece getCurrentPiece(){
        return currentPiece;
    }

    /**
     * Get following piece
     * @return following piece
     */
    public GamePiece getFollowingPiece(){
        return followingPiece;
    }

    /**
     * Swap currentPiece and followingPiece
     */
    public void swapCurrentPiece(){
        //logger.info("Swapping current piece and following piece");
        var temp = currentPiece;
        currentPiece = followingPiece;
        followingPiece = temp;

        //logger.info("Playing swap sound");
        Multimedia.playAudio("sounds/transition.wav");
    }

    /**
     * Get score
     * @return score
     */
    public SimpleIntegerProperty getScore(){
        return score;
    }

    /**
     * Get level
     * @return level
     */
    public SimpleIntegerProperty getLevel(){
        return level;
    }

    /**
     * Get multiplier
     * @return multiplier
     */
    public SimpleIntegerProperty getMultiplier(){
        return multiplier;
    }

    /**
     * Get lives
     * @return lives number
     */
    public SimpleIntegerProperty getLives(){
        return lives;
    }

    /**
     * Get time delay in millisecond
     * @return SimpleIntegerProperty Time interval
     */
    public SimpleIntegerProperty getTimerDelay(){
        timerDelay.set(12000 - 500 * getLevel().get());
        //timerDelay.set(200);
        return timerDelay;
    }


    /**
     * Get Timeline
     * @return timeline
     */
    public Timeline getTimeline() {return timeline;}

    /**
     * Get pieces stored in the queue
     * @return Queue pieces
     */
    public Queue<String> getPieces(){
        return this.pieces;
    }




}
