package uk.ac.soton.comp1206.Utility;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Multimedia is a static class that plays sound and music.
 */
public class Multimedia {
    private static final Logger logger = LogManager.getLogger(Multimedia.class);

    public static SimpleBooleanProperty silentMode = new SimpleBooleanProperty(false);

    private static MediaPlayer audioPlayer;
    private static MediaPlayer musicPlayer;

    /**
     * Play an audio file
     * @param file filename to play from resources
     */
    public static void playAudio(String file) {

        if (silentMode.get()) return;

        String toPlay = Multimedia.class.getResource("/" + file).toExternalForm();
        //logger.info("Playing audio: " + toPlay);

        try {
            Media play = new Media(toPlay);
            audioPlayer = new MediaPlayer(play);
            audioPlayer.play();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Unable to play audio file, disabling audio");
        }
    }

    /**
     * Play an background music file
     * @param file filename to play from resources
     */
    public static void playBGM(String file) {
        if (silentMode.get()) return;

        String toPlay = Multimedia.class.getResource("/" + file).toExternalForm();
        //logger.info("Playing music: " + toPlay);

        try {
            Media play = new Media(toPlay);
            musicPlayer = new MediaPlayer(play);
            //Set music play repeatedly in loop
            musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            musicPlayer.play();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Unable to play music file, disabling music");
        }
    }

    /**
     * Stop playing music
     */
    public static void stop(){
        try{
            musicPlayer.stop();
            logger.info("Stopping music");
        }catch (Exception e){

        }
    }

    /**
     * Switch silent mode
     */
    public static void turnSilentMode(){
        logger.info("Switching mode");
        var current = silentMode.get();
        silentMode.set(!current);

        if(silentMode.get()){
            musicPlayer.pause();
        }
        else{
            musicPlayer.play();
        }


    }

}
