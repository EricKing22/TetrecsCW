package uk.ac.soton.comp1206.component;

import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.FillTransition;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;

/**
 * The Visual User Interface component representing a single block in the grid.
 *
 * Extends Canvas and is responsible for drawing itself.
 *
 * Displays an empty square (when the value is 0) or a coloured square depending on value.
 *
 * The GameBlock value should be bound to a corresponding block in the Grid model.
 */
public class GameBlock extends Canvas {

    private static final Logger logger = LogManager.getLogger(GameBlock.class);

    /**
     * The set of colours for different pieces
     */
    public static final Color[] COLOURS = {
            Color.TRANSPARENT,
            Color.DEEPPINK,
            Color.RED,
            Color.ORANGE,
            Color.YELLOW,
            Color.YELLOWGREEN,
            Color.LIME,
            Color.GREEN,
            Color.DARKGREEN,
            Color.DARKTURQUOISE,
            Color.DEEPSKYBLUE,
            Color.AQUA,
            Color.AQUAMARINE,
            Color.BLUE,
            Color.MEDIUMPURPLE,
            Color.PURPLE
    };

    private final GameBoard gameBoard;

    private final double width;
    private final double height;

    private boolean hoover = false;

    /**
     * The column this block exists as in the grid
     */
    private final int x;

    /**
     * The row this block exists as in the grid
     */
    private final int y;

    /**
     * The value of this block (0 = empty, otherwise specifies the colour to render as)
     */
    private final IntegerProperty value = new SimpleIntegerProperty(0);


    private BigDecimal opacity;
    private GraphicsContext gc;

    /**
     * Create a new single Game Block
     * @param gameBoard the board this block belongs to
     * @param x the column the block exists in
     * @param y the row the block exists in
     * @param width the width of the canvas to render
     * @param height the height of the canvas to render
     */
    public GameBlock(GameBoard gameBoard, int x, int y, double width, double height) {
        this.gameBoard = gameBoard;
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;

        //A canvas needs a fixed width and height
        setWidth(width);
        setHeight(height);

        //Do an initial paint
        paint();

        //When the value property is updated, call the internal updateValue method
        value.addListener(this::updateValue);

        //Mouse monitoring for hoover effect only effective for main gameBoard
        if (!(gameBoard instanceof PieceBoard)){
            //When the mouse enters the game block
            setOnMouseEntered((e) -> {
                hooverOn();
                //Hide hoover effect for current coordinate (keyboard support)
                gameBoard.getBlock(gameBoard.getCurrentCoordinate().getX(),gameBoard.getCurrentCoordinate().getY()).hooverOff();
            });

            //When the mouse leaves the game block
            setOnMouseExited((e) -> {
                hooverOff();
            });
        }

    }

    /**
     * When the value of this block is updated,
     * @param observable what was updated
     * @param oldValue the old value
     * @param newValue the new value
     */
    private void updateValue(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        paint();
    }

    /**
     * Handle painting of the block canvas
     */
    public void paint() {
        //If the block is empty, paint as empty
        if(value.get() == 0) {
            paintEmpty();
        } else {
            //If the block is not empty, paint with the colour represented by the value
            paintColor(COLOURS[value.get()]);
        }

        //Add hoover effect
        if(hoover && !(gameBoard instanceof PieceBoard)){
            gc.setFill(Color.color(1,1,1,0.5));
            gc.fillRect(0,0,width,height);
        }
    }

    /**
     * Paint this canvas empty
     */
    private void paintEmpty() {
        gc = getGraphicsContext2D();
        //Clear
        gc.clearRect(0,0,width,height);

        //Fill
        gc.setFill(Color.color(0,0,0,0.5));
        gc.fillRect(0,0, width, height);

        //Border
        gc.setStroke(Color.WHITE);
        gc.strokeRect(0,0,width,height);
    }

    /**
     * Paint this canvas with the given colour
     * @param colour the colour to paint
     */
    private void paintColor(Paint colour) {
        gc = getGraphicsContext2D();
        //Clear
        gc.clearRect(0,0,width,height);

        //Colour fill
        gc.setFill(colour);
        gc.fillRect(0,0, width, height);
        //Border
        gc.setStroke(Color.BLACK);
        gc.strokeRect(0,0,width,height);

        //Color in the highlights
        gc.setFill(Color.color(1,1,1,0.5));
        gc.fillRect(0, 0, width, 3);
        gc.fillRect(0,0, 3, height);

        //Color in the shadows
        gc.setFill(Color.color(0,0,0,0.5));
        gc.fillRect(width-3, 0, width, height);
        gc.fillRect(0,height-3, width, height);
    }

    /**
     * Get the column of this block
     * @return column number
     */
    public int getX() {
        return x;
    }

    /**
     * Get the row of this block
     * @return row number
     */
    public int getY() {
        return y;
    }

    /**
     * Get the current value held by this block, representing it's colour
     * @return value
     */
    public int getValue() {
        return this.value.get();
    }

    /**
     * Bind the value of this block to another property. Used to link the visual block to a corresponding block in the Grid.
     * @param input property to bind the value to
     */
    public void bind(ObservableValue<? extends Number> input) {
        value.bind(input);
    }

    /**
     * Switch hoover on
     */
    public void hooverOn(){
        hoover = true;
        paint();
    }

    /**
     * Switch hoover off
     */
    public void hooverOff(){
        hoover = false;
        paint();
    }

    /**
     * Draw circle at the center
     */
    public void drawCircle(){
        gc = getGraphicsContext2D();
        gc.setFill(Color.color(1,1,1,0.7));
        gc.fillOval(width/4,height/4,width/2,height/2);
    }

    /**
     * Block fades out
     */
    public void fadeOut(){
        gc = getGraphicsContext2D();
        opacity = new BigDecimal("1.0");
        AnimationTimer timer = new AnimationTimer() {

            @Override
            public void handle(long now) {
                //Clear the previous color
                gc.clearRect(0,0,width,height);
                //Repaint the bottom
                paint();
                //Fill the rectangle with semi-transparent green from 1.0 to 0.0
                gc.setFill(Color.color(1,1,0, opacity.doubleValue()));
                gc.fillRect(0,0,width,height);

                //If reaches transparent, then stops timer
                if (opacity.doubleValue() == 0.0){
                    //logger.info("Stopping timer");
                    this.stop();
                }

                //Set new opacity
                opacity = opacity.subtract(new BigDecimal("0.05"));



            }
        };


        //logger.info("Starting block fade out timer");
        timer.start();





    }



}
