package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Utility.Multimedia;
import uk.ac.soton.comp1206.event.CommunicationsListener;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

/**
 * The LobbyScene shows the current holding games and option to create a new game.
 * Players joined in the same game are allowed to communicate with each other.
 */
public class LobbyScene extends BaseScene{
    /**
     * Create a new scene, passing in the GameWindow the scene will be displayed in
     *
     * @param gameWindow the game window
     */

    private static final Logger logger = LogManager.getLogger(LobbyScene.class);

    /**
     * Timer for receiving lobby information
     */
    private Timer timer = new Timer();

    /**
     * Communicator
     */
    private Communicator communicator;

    /**
     * Communicator listener
     */
    private CommunicationsListener communicationsListener;

    /**
     * Vbox for channels received from server
     */
    private VBox channels = new VBox();

    /**
     * All game channels
     */
    private final ScrollPane scrollPane = new ScrollPane();

    /**
     * Textfield for enter new channel
     */
    private TextField textField;

    /**
     * Username/nickname of the player
     */
    private final SimpleStringProperty nickname = new SimpleStringProperty();

    /**
     * Textflow for messages
     */
    private final TextFlow messages = new TextFlow();

    private BorderPane mainPane;

    /**
     * Textfield to send messages
     */
    private TextField messageToSend;
    private boolean scrollToBottom = false;
    private ScrollPane messageScroller;

    /**
     * Boolean to store if this player is the host
     */
    private SimpleBooleanProperty isHost = new SimpleBooleanProperty();

    /**
     * Scene for lobby
     * @param gameWindow the gameWindow
     */
    public LobbyScene(GameWindow gameWindow) {

        super(gameWindow);




    }

    @Override
    public void initialise() {
        /**
         * Handle key pressed
         */
        scene.setOnKeyPressed((this::handleKey));

        /**
         * Handle scroll to bottom
         */
        scene.addPostLayoutPulseListener(this::jumpToBottom);



        communicator = gameWindow.getCommunicator();
        communicator.addListener(communicationsListener);

        //Request list from server
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                communicator.send("LIST");
            }
        },0,2000);
    }

    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var LobbyPane = new StackPane();
        LobbyPane.setMaxWidth(gameWindow.getWidth());
        LobbyPane.setMaxHeight(gameWindow.getHeight());
        LobbyPane.getStyleClass().add("lobby-background");
        root.getChildren().add(LobbyPane);

        mainPane = new BorderPane();
        LobbyPane.getChildren().add(mainPane);

        buildTitle(mainPane);

        buildLeftBox(mainPane);


        /**
         * Handle information received
         */
        setCommunicationsListener(this::receivedMessage);



    }


    /**
     * Handle key event
     * @param event
     */
    private void handleKey(KeyEvent event){
        //Return to menu
        if (event.getCode() == KeyCode.ESCAPE){
            logger.info("Return to Menu scene");
            Multimedia.stop();
            timer.cancel();
            gameWindow.startMenu();
        }

        else if (event.getCode() == KeyCode.ENTER){

            //For creating channel
            if(textField.focusedProperty().get()){
                if (textField == null || textField.getText() == null) {
                    logger.error("Invalid input");
                    return;
                }
                else{
                    String channelName = textField.getText();
                    communicator.send("CREATE " + channelName);
                    communicator.send("LIST");
                }
            }

            //For entering message
            else if (messageToSend.focusedProperty().get()){
                if (messageToSend == null || messageToSend.getText() == null){
                    logger.error("Invalid input");
                    return;
                }
                else{
                    String message = messageToSend.getText();
                    //For change name
                    if (message.contains("/nick")){
                        communicator.send("NICK " + message.replace("/nick",""));
                    }
                    //Send message
                    else{
                        communicator.send("MSG " + message);
                    }
                }
                messageToSend.clear();
            }




        }
    }


    /**
     * Build title
     * @param mainPane boarderPane
     */
    private void buildTitle(BorderPane mainPane){
        Text title = new Text();
        title.setText("Multi Player");
        title.getStyleClass().add("bigtitle");
        mainPane.setTop(title);
        BorderPane.setAlignment(title,Pos.CENTER);
    }

    /**
     * Build leftBox VBox container
     * @param mainPane boarderPane
     */
    private void buildLeftBox(BorderPane mainPane){
        /**
         * Left Vbox container
         */
        var currentGameBox = new VBox();
        currentGameBox.setAlignment(Pos.TOP_CENTER);
        currentGameBox.setPadding(new Insets(0,0,0,20));
        mainPane.setLeft(currentGameBox);


        var newChannelButton = new Button();
        currentGameBox.getChildren().add(newChannelButton);

        textField = new TextField();
        textField.getStyleClass().add("textField");
        currentGameBox.getChildren().add(textField);
        textField.setPadding(new Insets(10,0,10,0));
        textField.setPromptText("Enter new room name:");
        textField.setVisible(false);

        newChannelButton.setText("New Game");
        newChannelButton.setOnAction((event) -> {
            textField.setVisible(true);
            textField.requestFocus();
        });
        newChannelButton.getStyleClass().add("channelItem");



        var currentGameText = new Text("Current Games");
        currentGameText.getStyleClass().add("channelItem");
        currentGameBox.getChildren().add(currentGameText);

        scrollPane.getStyleClass().add("scroller");
        scrollPane.setFitToWidth(true);
        scrollPane.setContent(channels);
        currentGameBox.getChildren().add(scrollPane);


    }


    /**
     * Build message window
     */
    private void buildMessageWindow(String channelName){

        communicator.send("USERS");


        BorderPane chatWindow = new BorderPane();
        chatWindow.setBackground(Background.fill(Color.color(0,0,0,0.5)));
        chatWindow.setBorder(Border.stroke(Color.WHITE));

        //HBox to hold the name and change name instruction
        HBox nameBox = new HBox();
        //Name of the user.
        Text text = new Text();
        text.textProperty().bind(nickname);
        text.getStyleClass().add("myname");
        //Instruction to change nick name
        Text instruction = new Text("Type /nick [new name] to change nick name");
        instruction.getStyleClass().add("channelItem");
        //Region to separate nodes
        Region region = new Region();
        HBox.setHgrow(region,Priority.ALWAYS);
        Region region1 = new Region();
        HBox.setHgrow(region1, Priority.ALWAYS);
        nameBox.getChildren().addAll(text,region1,instruction);

        chatWindow.setTop(nameBox);

        messageScroller = new ScrollPane();
        messageScroller.getStyleClass().add("scroller");
        chatWindow.setCenter(messageScroller);

        messages.getStyleClass().add("messages");
        messageScroller.setContent(messages);

        //Container contains textField and two buttons to leave or start game
        VBox container = new VBox();
        chatWindow.setBottom(container);

        messageToSend = new TextField();
        messageToSend.getStyleClass().add("textField");
        messageToSend.setPromptText("Enter message to send");

        container.getChildren().add(messageToSend);

        Button startGameButton = new Button();
        startGameButton.setText("Start Game");
        startGameButton.getStyleClass().add("channelItem");
        startGameButton.visibleProperty().bind(isHost);
        startGameButton.setOnAction((event) -> {
            if(isHost.get()){
                //Start game
                communicator.send("START");
            }
            else if(!isHost.get()){
                logger.error("Not host");
            }
        });

        Button leaveGame = new Button();
        leaveGame.setText("Leave Game");
        leaveGame.getStyleClass().add("channelItem");
        leaveGame.setOnAction((event -> {
            //Leave current channel
            communicator.send("PART");
            //Reset messages
            chatWindow.setVisible(false);
            messages.getChildren().clear();
        }));




        HBox hBox = new HBox();
        hBox.getChildren().addAll(startGameButton,region,leaveGame);
        container.getChildren().add(hBox);





        mainPane.setCenter(chatWindow);
    }




    /**
     * Handle information received by communicator
     */
    private void receivedMessage(String message){

        //Channels information
        if (message.contains("CHANNELS")){
            //Clear all roms
            Platform.runLater(() -> channels.getChildren().clear());

            message = message.split("CHANNELS ")[1];

            //Only one channel situation
            if (!message.contains("\n")){
                message = message + "\n";
            }

            //Add all channels to arraylist
            for (String channelName : message.split("\n")){

                Platform.runLater(() -> {
                    Button button = new Button();
                    button.setText(channelName);
                    button.setAlignment(Pos.CENTER);

                    button.setOnAction((event -> {
                        communicator.send("JOIN " + channelName);
                    }));

                    button.getStyleClass().add("channelItem");

                    channels.setAlignment(Pos.CENTER);
                    channels.getChildren().add(button);
                });
            }

        }

        //Join channels
        else if(message.contains("JOIN")){
            Multimedia.playAudio("sounds/pling.wav");
            String channelName = message.split("JOIN ")[1];
            Platform.runLater(() -> {
                buildMessageWindow(channelName);
            });
        }

        //Error handling
        else if(message.contains("ERROR")){
            //Already in a channel but joins another
            if (message.contains("already in a channel")){
                //Generate an alert window
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Error occurred");
                    alert.setContentText("Already in channel.");

                    ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.OK_DONE);
                    alert.getButtonTypes().setAll(closeButton);

                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.get() == closeButton) {
                        alert.close();
                    }
                });
            }
            else if(message.contains("not host")){
                //Generate an alert window
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Error occurred");
                    alert.setContentText("You are not host");

                    ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.OK_DONE);
                    alert.getButtonTypes().setAll(closeButton);

                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.get() == closeButton) {
                        alert.close();
                    }
                });
            }
        }


        //Nick name change
        else if(message.contains("NICK")){
            //Change own name
            if(!message.contains(":")){
                String newName = message.split("NICK ")[1];
                Platform.runLater(() -> {
                    //Set nickName
                    nickname.set(newName);
                });
            }
            else{
                String oldName = message.split("NICK ")[1].split(":")[0];
                String newName = message.split("NICK ")[1].split(":")[1];


                for (Node node : messages.getChildren()){
                    if(node instanceof Text){
                        String name = ((Text) node).getText().split(":")[0];
                        String m = ((Text) node).getText().split(":")[1];
                        if (name.equals(oldName)){
                            ((Text) node).setText(newName + ":" + m);
                        }

                    }
                }
            }



        }

        //Modify host if is host
        else if(message.contains("HOST")){
            Platform.runLater( () -> {isHost.set(true);});
        }

        //Receive message
        else if(message.contains("MSG")){
            String newMessage = message.split("MSG ")[1].replace(":",": ");
            Platform.runLater(() -> {
                Text text = new Text(newMessage + "\n");
                messages.getChildren().add(text);
                //Scroll to bottom
                if(messageScroller.getVvalue() == 0.0f || messageScroller.getVvalue() > 0.9f) {
                    logger.info("Setting scrooltobottom");
                    scrollToBottom = true;
                }
            });

        }

        //Start game
        else if(message.contains("START")){
            Platform.runLater(() -> {
                timer.cancel();
                gameWindow.startMultiGame();
                communicator.send("PIECE");
                communicator.send("PIECE");
            }
            );

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
     * Move the scroller to bottom
     */
    private void jumpToBottom(){
        if (!scrollToBottom) return;
        messageScroller.setVvalue(1.0f);
        scrollToBottom = false;
    }

}
