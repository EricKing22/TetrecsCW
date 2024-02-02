package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.App;
import uk.ac.soton.comp1206.Utility.Multimedia;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;



/**
 * The main menu of the game. Provides a gateway to the rest of the game.
 */
public class MenuScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(MenuScene.class);

    /**
     * Create a new menu scene
     * @param gameWindow the Game Window this will be displayed in
     */
    public MenuScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Menu Scene");


    }

    /**
     * Build the menu layout
     */
    @Override
    public void build() {
        //logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var menuPane = new StackPane();
        menuPane.setMaxWidth(gameWindow.getWidth());
        menuPane.setMaxHeight(gameWindow.getHeight());
        menuPane.getStyleClass().add("menu-background");
        root.getChildren().add(menuPane);

        var mainPane = new BorderPane();
        menuPane.getChildren().add(mainPane);


        //Play menu music
        Multimedia.playBGM("music/menu.mp3");


        //Container of all elements on the menu
        VBox menu = new VBox();
        menu.setAlignment(Pos.CENTER);
        mainPane.setTop(menu);


        //Title
        Image image = new Image(getClass().getResource("/images/TetrECS.png").toExternalForm());
        var imageView = new ImageView(image);
        imageView.setFitWidth(600);
        imageView.setPreserveRatio(true);
        var imagePane = new BorderPane(imageView);
        imagePane.setPadding(new Insets(100));
        imagePane.setPrefSize(imageView.getFitWidth(), imageView.getFitHeight());
        menu.getChildren().add(imagePane);


        //Add buttons
        Button singlePlayer = new Button("Single Player");
        singlePlayer.setOnAction(this::startGame);
        singlePlayer.getStyleClass().add("menuItem");
        singlePlayer.setTextFill(Color.WHITE);
        //Turn yellow when mouse entered
        singlePlayer.setOnMouseEntered((mouseEvent) -> singlePlayer.setTextFill(Color.YELLOW));
        //Turn white when mouse left
        singlePlayer.setOnMouseExited((mouseEvent) -> singlePlayer.setTextFill(Color.WHITE));
        menu.getChildren().add(singlePlayer);

        Button multiPlayer = new Button("Multi Player");
        multiPlayer.setOnAction(this::startMulti);
        multiPlayer.getStyleClass().add("menuItem");
        multiPlayer.setTextFill(Color.WHITE);
        //Turn yellow when mouse entered
        multiPlayer.setOnMouseEntered((mouseEvent) -> multiPlayer.setTextFill(Color.YELLOW));
        //Turn white when mouse left
        multiPlayer.setOnMouseExited((mouseEvent) -> multiPlayer.setTextFill(Color.WHITE));
        menu.getChildren().add(multiPlayer);

        Button instruction = new Button("Instructions");
        instruction.setTextFill(Color.WHITE);
        instruction.setOnAction(this::startInstruction);
        instruction.getStyleClass().add("menuItem");
        //Turn yellow when mouse entered
        instruction.setOnMouseEntered((mouseEvent) -> instruction.setTextFill(Color.YELLOW));
        //Turn white when mouse left
        instruction.setOnMouseExited((mouseEvent) -> instruction.setTextFill(Color.WHITE));
        menu.getChildren().add(instruction);

        Button exit = new Button("Exit");
        exit.setTextFill(Color.WHITE);
        exit.setOnAction((event) -> {

            //Communicator exits
            gameWindow.getCommunicator().send("Quit");

            //Shutdown app
            App.getInstance().shutdown();




        });
        exit.getStyleClass().add("menuItem");
        //Turn yellow when mouse entered
        exit.setOnMouseEntered((mouseEvent) -> exit.setTextFill(Color.YELLOW));
        //Turn white when mouse left
        exit.setOnMouseExited((mouseEvent) -> exit.setTextFill(Color.WHITE));
        menu.getChildren().add(exit);





    }


    /**
     * Initialise the menu
     */
    @Override
    public void initialise() {
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == (KeyCode.M)){
                Multimedia.turnSilentMode();
            }
        });
    }


    /**
     * Handle when the Start Game button is pressed
     * @param event event
     */
    private void startGame(ActionEvent event) {
        Multimedia.stop();
        gameWindow.startChallenge();

    }

    /**
     * Handle when the Multi player button is pressed
     * @param event event
     */
    private void startMulti(ActionEvent event){
        Multimedia.stop();
        gameWindow.startMulti();

    }

    /**
     * Handle when the Instructions button is pressed
     * @param event event
     */
    private void startInstruction(ActionEvent event){
        gameWindow.startInstruction();
    }




}
