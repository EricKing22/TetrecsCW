package uk.ac.soton.comp1206.scene;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Utility.Multimedia;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * The InstructionScene shows basic instructions of this game.
 */
public class InstructionScene extends BaseScene{

    private static final Logger logger = LogManager.getLogger(InstructionScene.class);
    /**
     * Create a new scene, passing in the GameWindow the scene will be displayed in
     *
     * @param gameWindow the game window
     */
    public InstructionScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Instruction Scene");

    }

    /**
     * Initialise the scene and show instruction image
     */
    @Override
    public void initialise() {

        //Return to menu page
        scene.setOnKeyPressed(this::handleKey);

    }

    /**
     * Build the instruction layout
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());


        var instructionPane = new StackPane();
        instructionPane.setMaxWidth(gameWindow.getWidth());
        instructionPane.setMaxHeight(gameWindow.getHeight());
        instructionPane.getStyleClass().add("instruction-background");
        root.getChildren().add(instructionPane);

        var mainPane = new BorderPane();
        instructionPane.getChildren().add(mainPane);

        //Vertical container for text instruction
        VBox instructionText = new VBox();
        instructionText.setAlignment(Pos.CENTER);
        mainPane.setTop(instructionText);
        //Title
        Label instructionTitle = new Label("Instructions");
        instructionTitle.getStyleClass().add("instructionmodesub");
        instructionTitle.setTextAlignment(TextAlignment.CENTER);
        instructionText.getChildren().add(instructionTitle);
        //Word instructions
        Text instruction = new Text();
//        instruction.setTextAlignment(TextAlignment.CENTER);
//        instruction.setText("");
//        instructionText.getChildren().add(instruction);




        //Vertical container for instruction image
        VBox instructionImage = new VBox();
        instructionImage.setAlignment(Pos.CENTER);
        mainPane.setCenter(instructionImage);


        //Load image
        Image image = new Image(getClass().getResource("/images/Instructions.png").toExternalForm());
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(600);
        imageView.setPreserveRatio(true);
        instructionImage.getChildren().add(imageView);

        //Horizontal container for examples images of game pieces, stored in a vertical container with a label
        VBox examplePieces = new VBox();
        examplePieces.setAlignment(Pos.CENTER);
        mainPane.setBottom(examplePieces);

        //Label for the pieces
        Label exampleTitle = new Label("Game Pieces");
        exampleTitle.getStyleClass().add("instructionmodesub");
        exampleTitle.setTextAlignment(TextAlignment.CENTER);
        examplePieces.getChildren().add(exampleTitle);

        HBox pieces1= new HBox();
        examplePieces.getChildren().add(pieces1);

        for (int i = 0; i < GamePiece.PIECES; i++){
            var board = new GameBoard(3,3,50,50);
            board.getGrid().playPiece(GamePiece.createPiece(i),1,1);
            var region1 = new Region();
            var region2 = new Region();
            HBox.setHgrow(region1,Priority.ALWAYS);
            HBox.setHgrow(region2,Priority.ALWAYS);
            pieces1.getChildren().addAll(region1,board,region2);
        }



    }





    /**
     * Handle what happens when a key is pressed
     * @param event key pressed
     */
    public void handleKey(KeyEvent event){
        if(event.getCode() == KeyCode.ESCAPE){
            logger.info("Return to Menu scene");
            Multimedia.stop();
            gameWindow.startMenu();
        }

    }

}
