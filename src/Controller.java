import enums.ArtificialCondition;
import enums.ArtificialStat;
import enums.BioCondition;
import enums.Sin;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.layout.VBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import enums.River;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.scene.text.Font;
import javafx.util.Duration;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.IOException;
import java.nio.file.Files;

import java.nio.file.Paths;


public class Controller {

    private Creature creature;
    private EventManager eventManager;
    private long startTime;
    private boolean gameOverActive;

    private AudioClip panicBreathing;
    private AudioClip lightFlickerSoundEffect;
    private MediaPlayer riverIIIBackgroundMusic;
    private AudioClip gameOverSoundEffect;

    private String[] gameOverMessages = {
            "Your sins have outlived the flesh they condemned.",
            "May your soul carry the memory of what your hands failed to save.",
            "Carry this failure with you. Let it weigh upon every life that follows."
    };

    @FXML
    private Label prideLabel;

    @FXML
    private Label gluttonyLabel;

    @FXML
    private Label slothLabel;

    @FXML
    private Label wrathLabel;

    @FXML
    private Label temperatureLabel;

    @FXML
    private Label storageLabel;

    @FXML
    private Label powerLabel;

    @FXML
    private Label integrityLabel;

    @FXML
    private VBox dialogueBox;

    @FXML
    private ScrollPane dialogueScrollPane;

    @FXML
    private VBox statusBox;

    @FXML
    private ScrollPane statusScrollPane;

    @FXML
    private StackPane transitionPane;

    @FXML
    private VBox transitionContent;

    @FXML
    private ImageView riverTransitionImage;

    @FXML
    private Label transitionTitle;

    @FXML
    private Label transitionQuote;

    @FXML
    private Label transitionFooter;

    @FXML
    private StackPane gameOverPane;

    @FXML
    private VBox gameOverContent;

    @FXML
    private ImageView gameOverImage;

    @FXML
    private Label gameOverText;


    @FXML
    public void initialize() {
        creature = new Creature();
        startTime = System.currentTimeMillis();
        gameOverActive = false;
        eventManager = new EventManager(
                creature,
                // the 'this::' allows EventManager to use these methods
                this::updateStatusLabels,

                this::addDialogue,
                this::addChatDialogue,
                this::addNoActionDialogue,

                this::addStatus,
                this::addEventStatus,
                this::addNoActionStatus,

                this::startRiverTransition,
                this::startPanicDim,
                this::startLightFlicker,

                this::startGameOverSequence
                );

        updateStatusLabels();

        loadFonts();

        panicBreathing = new AudioClip(
                Paths.get("audio", "panicbreathing.wav").toUri().toString()
        );
        panicBreathing.setVolume(0.45);
        panicBreathing.setCycleCount(AudioClip.INDEFINITE);

        lightFlickerSoundEffect = new AudioClip(
                Paths.get("audio", "lightflicker.wav").toUri().toString()
        );
        lightFlickerSoundEffect.setVolume(0.75);
        lightFlickerSoundEffect.setCycleCount(AudioClip.INDEFINITE);

        Media riverIIIMusic = new Media(
                Paths.get("audio", "river3soundtrack.mp3").toUri().toString()
        );
        riverIIIBackgroundMusic = new MediaPlayer(riverIIIMusic);
        riverIIIBackgroundMusic.setVolume(0.35);
        riverIIIBackgroundMusic.setCycleCount(MediaPlayer.INDEFINITE);

        gameOverSoundEffect = new AudioClip(
                Paths.get("audio", "gameover.wav").toUri().toString()
        );
        gameOverSoundEffect.setVolume(0.8);

        dialogueBox.heightProperty().addListener((observableValue, oldValue, newValue) -> {
            dialogueScrollPane.setVvalue(1.0);
        });

        statusBox.heightProperty().addListener((observableValue, oldValue, newValue) -> {
            statusScrollPane.setVvalue(1.0);
        });

        startIntroSequence();



    }

    private String getTimestamp() {
        long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;

        long hours = elapsedSeconds / 3600;
        long minutes = (elapsedSeconds % 3600) / 60;
        long seconds = elapsedSeconds % 60;

        return String.format(
                "[%d:%02d:%02d]", hours, minutes, seconds
        );
    }

    private void addStatus(String message) {
        Label newStatus = new Label(getTimestamp() + " " + message);
        newStatus.setWrapText(true);
        statusBox.getChildren().add(newStatus);
    }

    private void addEventStatus(String message, int actionCount) {
        Label newStatus = new Label(getTimestamp() + " " + message);

        newStatus.setWrapText(true);
        switch (actionCount) {
            case 1 -> newStatus.setTextFill(Color.MAROON);
            case 2 -> newStatus.setTextFill(Color.PURPLE);
            case 3 -> newStatus.setTextFill(Color.ORANGERED);
            default -> newStatus.setTextFill(Color.RED);
        }

        statusBox.getChildren().add(newStatus);
    }

    private void addNoActionStatus(String message) {
        Label newStatus = new Label(getTimestamp() + " " + message);

        newStatus.setWrapText(true);
        newStatus.setTextFill(Color.YELLOW);

        statusBox.getChildren().add(newStatus);
    }

    private void updateStatusLabels() {
        updateBiologicalLabel(prideLabel, Sin.PRIDE);
        updateBiologicalLabel(gluttonyLabel, Sin.GLUTTONY);
        updateBiologicalLabel(slothLabel, Sin.SLOTH);
        updateBiologicalLabel(wrathLabel, Sin.WRATH);

        updateArtificialLabel(temperatureLabel, ArtificialStat.TEMPERATURE);
        updateArtificialLabel(storageLabel, ArtificialStat.STORAGE);
        updateArtificialLabel(powerLabel, ArtificialStat.POWER);
        updateArtificialLabel(integrityLabel, ArtificialStat.INTEGRITY);
    }

    private void updateBiologicalLabel(Label label, Sin sin) {
        BioCondition condition = creature.getBiologicalSystem().getCondition(sin);

        switch (condition) {
            case GOOD -> label.setTextFill(Color.GREEN);
            case OK -> label.setTextFill(Color.GOLDENROD);
            case BAD -> label.setTextFill(Color.RED);
            case OZLERIC -> label.setTextFill(Color.BLUE);
        }
    }

    private void updateArtificialLabel(Label label, ArtificialStat stat) {
        ArtificialCondition condition = creature.getArtificialSystem().getCondition(stat);

        switch (condition) {
            case GOOD -> label.setTextFill(Color.GREEN);
            case OK -> label.setTextFill(Color.GOLDENROD);
            case BAD -> label.setTextFill(Color.RED);
        }
    }

    private void addDialogue(String message) {
        Label newMessage = new Label("???: " + message);
        newMessage.setWrapText(true);
        newMessage.setTextFill(Color.WHITE);

        dialogueBox.getChildren().add(newMessage);
    }

    private void addChatDialogue(String message) {
        Label newMessage = new Label("???: " + message);
        newMessage.setWrapText(true);
        newMessage.setTextFill(Color.web("#9AA7B3"));

        dialogueBox.getChildren().add(newMessage);
    }

    private void addNoActionDialogue(String message) {
        Label newMessage = new Label("???: " + message);
        newMessage.setWrapText(true);
        newMessage.setTextFill(Color.web("#C9A55C"));

        dialogueBox.getChildren().add(newMessage);
    }

    @FXML
    private void suppress() {
        creature.getBiologicalSystem().changeSin(Sin.WRATH, -15);
        creature.getBiologicalSystem().changeSin(Sin.SLOTH, 10);

        updateStatusLabels();

        addStatus("[SUPPRESSION WAS USED.]\n" +
                "   [WRATH HAS DECREASED.]\n" +
                "   [SLOTH HAS INCREASED.]");

        eventManager.suppressUsed();
    }

    @FXML
    private void coolant() {
        creature.getArtificialSystem().changeStat(ArtificialStat.TEMPERATURE, -15);
        creature.getArtificialSystem().changeStat(ArtificialStat.POWER, -10);

        updateStatusLabels();

        addStatus("[COOLANT WAS APPLIED.]\n" +
                "   [TEMPERATURE HAS DECREASED.]\n" +
                "   [POWER HAS DECREASED.]");

        eventManager.coolantUsed();
    }

    @FXML
    private void stimulate() {
        creature.getBiologicalSystem().changeSin(Sin.SLOTH, -15);
        creature.getArtificialSystem().changeStat(ArtificialStat.TEMPERATURE, 10);

        updateStatusLabels();

        addStatus("[ENTITY WAS STIMULATED.]\n" +
                "   [SLOTH HAS DECREASED.]\n" +
                "   [TEMPERATURE HAS INCREASED.]");

        eventManager.stimulateUsed();

    }

    @FXML
    private void charge() {
        creature.getArtificialSystem().changeStat(ArtificialStat.POWER, 15);
        creature.getArtificialSystem().changeStat(ArtificialStat.STORAGE, 10);

        updateStatusLabels();

        addStatus("[ENTITY HAS BEEN CHARGED.]\n" +
                "   [POWER HAS INCREASED.]\n" +
                "   [STORAGE HAS INCREASED.]");

        eventManager.chargeUsed();

    }

    @FXML
    private void repair() {
        creature.getArtificialSystem().changeStat(ArtificialStat.INTEGRITY, 15);
        creature.getBiologicalSystem().changeSin(Sin.GLUTTONY, -10);

        updateStatusLabels();

        addStatus("[REPAIRING...]\n" +
                "   [INTEGRITY HAS INCREASED.]\n" +
                "   [GLUTTONY HAS DECREASED.]");

        eventManager.repairUsed();
    }

    @FXML
    private void nourish() {
        creature.getBiologicalSystem().changeSin(Sin.GLUTTONY, 15);
        creature.getBiologicalSystem().changeSin(Sin.PRIDE, 10);

        updateStatusLabels();

        addStatus("[ENTITY HAS BEEN NOURISHED.]\n" +
                "   [GLUTTONY HAS INCREASED.]\n" +
                "   [PRIDE HAS INCREASED.]");


        eventManager.nourishUsed();
    }

    @FXML
    private void restrain() {
        creature.getBiologicalSystem().changeSin(Sin.PRIDE, -15);
        creature.getBiologicalSystem().changeSin(Sin.WRATH, 10);

        updateStatusLabels();

        addStatus("[RESTRAINING ENTITY NOW...]\n" +
                "   [PRIDE HAS DECREASED.]\n" +
                "   [WRATH HAS INCREASED.]");

        eventManager.restrainUsed();
    }

    @FXML
    private void purge() {
        creature.getArtificialSystem().changeStat(ArtificialStat.STORAGE, -15);
        creature.getArtificialSystem().changeStat(ArtificialStat.INTEGRITY, -10);

        updateStatusLabels();

        addStatus("[PURGING STORAGE WAS SUCCESSFUL.]\n" +
                "   [STORAGE HAS DECREASED.]\n" +
                "   [INTEGRITY HAS DECREASED.]");

        eventManager.purgeUsed();
    }

    private void startIntroSequence() {
        transitionTitle.setText("SESSION INITIALIZING");
        transitionTitle.setTextFill(Color.web("#D9E1E8"));


        transitionQuote.setText(
                "Stability is not the absence of suffering.\n" +
                        "It is the balance that survives it.\n\n" +
                        "A thing that should not live has begun to breathe."
        );

        transitionFooter.setText(
                "OBSERVE CAREFULLY"
        );
        transitionFooter.setTextFill(Color.web("#A61B1B"));

        riverTransitionImage.setImage(new Image(
                Paths.get("images", "Logo.png").toUri().toString()
        ));

        transitionPane.setVisible(true);
        transitionPane.setOpacity(1.0);
        transitionContent.setOpacity(0.0);

        FadeTransition fadeContentIn = new FadeTransition(
                Duration.seconds(2),
                transitionContent
        );

        fadeContentIn.setFromValue(0.0);
        fadeContentIn.setToValue(1.0);

        PauseTransition holdTransition = new PauseTransition(Duration.seconds(5));

        FadeTransition fadeContentOut = new FadeTransition(
                Duration.seconds(1.5),
                transitionContent
        );

        fadeContentOut.setFromValue(1.0);
        fadeContentOut.setToValue(0.0);


        SequentialTransition introTransition = new SequentialTransition(
                fadeContentIn,
                holdTransition,
                fadeContentOut
        );

        introTransition.setOnFinished(event -> {
            startRiverTransition(River.RIVER_I);
        });

        introTransition.play();


    }

    private void startRiverTransition(River nextRiver) {
        transitionPane.setMouseTransparent(false);

        riverTransitionImage.setImage(null);
        transitionFooter.setText("");
        transitionQuote.setTextFill(Color.WHITE);
        switch (nextRiver) {
            case RIVER_I -> {
                transitionTitle.setText("RIVER I");
                transitionTitle.setTextFill(Color.web("#C7D6E0"));
                transitionQuote.setText(
                        "At first, the River flowed gently through the earth,\n" +
                                "carrying life toward places it had yet to know."
                );
            }
            case RIVER_II -> {
                transitionTitle.setText("RIVER II");
                transitionTitle.setTextFill(Color.web("#A61B1B"));
                transitionQuote.setText(
                        "The rivers widen to give way for the rushing water,\n" +
                                "so that life's existence may prosper."
                );
            }
            case RIVER_III -> {
                transitionTitle.setText("RIVER III");
                transitionTitle.setTextFill(Color.web("#B83232"));
                transitionQuote.setText("Yet once the river grew, it began to consume everything in its path,\n" +
                        "leaving nothing but ruin and sorrow in its wake.");
            }
        }

        transitionPane.setVisible(true);
        transitionPane.setOpacity(0.0);
        transitionContent.setOpacity(0.0);

        FadeTransition fadeToBlack;

        if (nextRiver == River.RIVER_I) {
            transitionPane.setOpacity(1.0);

            fadeToBlack = new FadeTransition(
                    Duration.seconds(0),
                    transitionPane
            );

            fadeToBlack.setFromValue(1.0);
            fadeToBlack.setToValue(1.0);
        } else {
            transitionPane.setOpacity(0.0);

            fadeToBlack = new FadeTransition(
                    Duration.seconds(1),
                    transitionPane
            );

            fadeToBlack.setFromValue(0.0);
            fadeToBlack.setToValue(1.0);
        }

        fadeToBlack.setOnFinished(event -> {
            if (nextRiver == River.RIVER_III) {
                riverIIIBackgroundMusic.play();
            }
        });

        FadeTransition fadeContentIn = new FadeTransition(
                Duration.seconds(1.5),
                transitionContent
        );

        fadeContentIn.setFromValue(0.0);
        fadeContentIn.setToValue(1.0);

        PauseTransition holdTransition = new PauseTransition(
                Duration.seconds(4)
        );

        FadeTransition fadeContentOut = new FadeTransition(
                Duration.seconds(1),
                transitionContent
        );

        fadeContentOut.setFromValue(1.0);
        fadeContentOut.setToValue(0.0);

        FadeTransition fadeFromBlack = new FadeTransition(
                Duration.seconds(1),
                transitionPane
        );

        fadeFromBlack.setFromValue(1.0);
        fadeFromBlack.setToValue(0.0);

        SequentialTransition transition = new SequentialTransition(
                 fadeToBlack, fadeContentIn, holdTransition, fadeContentOut, fadeFromBlack
        );

        transition.setOnFinished(event -> {
            transitionPane.setVisible(false);

            switch (nextRiver) {
                case RIVER_I -> {
                    addRiverDialogue("The current is gentle here. For now, I will follow where it leads.");

                    addRiverMessage(
                            "...From still waters, the first reflection stirs...\n" +
                                    "The vessel knows not yet the shape it carries.\n" +
                                    "What stirs beneath the surface has not yet recognized itself..."
                    );
                }
                case RIVER_II -> {
                    addRiverDialogue("The current feels different. Do you dare fight against it?");

                    addRiverMessage(
                            "...And the echos of self rely on no being...\n" +
                                    "The River remembers what the vessel has forgotten.\n" +
                                    "What gazes inward will eventually gaze back..."
                    );
                }
                case RIVER_III -> {
                    addRiverDialogue("The current has carried me far enough. I shall carve my own River into the dust.");

                    addRiverMessage(
                            "...Alas, the River swells beyond the vessel...\n" +
                                    "Its endless sorrow seeps into the earth, poisoning all that takes root.\n" +
                                    "What learned to gaze inward has begun to search for the door...\n" +
                                    "May the River leave none behind to mourn."
                    );


                }
            }
            eventManager.finishRiverTransition(nextRiver);
        });

        transition.play();
    }

    private void startGameOverSequence() {
        if (!gameOverActive) {
            gameOverActive = true;

            if (panicBreathing.isPlaying()) {
                panicBreathing.stop();
            }

            if (lightFlickerSoundEffect.isPlaying()) {
                lightFlickerSoundEffect.stop();
            }

            if (riverIIIBackgroundMusic != null) {
                riverIIIBackgroundMusic.stop();
            }

            gameOverSoundEffect.play();

            int messageChoice = (int) (Math.random() * gameOverMessages.length);

            gameOverText.setText(gameOverMessages[messageChoice]);

            gameOverImage.setImage(new Image(
                    Paths.get("images", "gameOverImage.png").toUri().toString()
            ));

            gameOverPane.setMouseTransparent(false);
            gameOverPane.setVisible(true);
            gameOverPane.setOpacity(0.0);
            gameOverContent.setOpacity(0.0);

            FadeTransition fadeToBlack = new FadeTransition(
                    Duration.seconds(2),
                    gameOverPane
            );

            fadeToBlack.setFromValue(0.0);
            fadeToBlack.setToValue(1.0);

            FadeTransition fadeContentIn = new FadeTransition(
                    Duration.seconds(1.5),
                    gameOverContent
            );

            fadeContentIn.setFromValue(0.0);
            fadeContentIn.setToValue(1.0);

            SequentialTransition gameOverTransition = new SequentialTransition(
                    fadeToBlack,
                    fadeContentIn
            );

            gameOverTransition.play();
        }
    }

    private void loadFonts() {
        try {
            Font quoteFont = Font.loadFont(
                    Files.newInputStream(
                            Paths.get("fonts", "AritaBuriKR-SemiBold.ttf")
                    ),
                    34
            );

            Font titleFont = Font.loadFont(
                    Files.newInputStream(
                            Paths.get("fonts", "norwester.ttf")
                    ),
                    28
            );

            Font footerFont = Font.loadFont(
                    Files.newInputStream(
                            Paths.get("fonts", "norwester.ttf")
                    ),
                    20
            );

            transitionQuote.setFont(quoteFont);
            transitionTitle.setFont(titleFont);
            transitionFooter.setFont(footerFont);

            gameOverText.setFont(quoteFont);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addRiverDialogue(String message) {
        Label newMessage = new Label("???: " + message);

        newMessage.setWrapText(true);
        newMessage.setTextFill(Color.web("#B83232"));

        dialogueBox.getChildren().add(newMessage);
    }

    private void addRiverMessage(String message) {
        Label newStatus = new Label(message);

        newStatus.setWrapText(true);
        newStatus.setTextFill(Color.web("#B83232"));
        statusBox.getChildren().add(newStatus);
    }

    private void startPanicDim() {
        transitionContent.setOpacity(0.0);

        transitionPane.setVisible(true);
        transitionPane.setMouseTransparent(true);
        transitionPane.setOpacity(0.0);

        panicBreathing.play();

        var root = transitionPane.getScene().getRoot();
        transitionPane.getScene().setFill(Color.BLACK);

        GaussianBlur blur = new GaussianBlur(0);
        root.setEffect(blur);

        FadeTransition dimScreen = new FadeTransition(
                Duration.seconds(1.5),
                transitionPane
        );

        dimScreen.setFromValue(0.0);
        dimScreen.setToValue(0.65);

        Timeline blurScreen = new Timeline(
                new KeyFrame(
                        Duration.ZERO,
                        new KeyValue(blur.radiusProperty(), 0)
                ),

                new KeyFrame(
                        Duration.seconds(1.5),
                        new KeyValue(blur.radiusProperty(), 14)
                )
        );

        ParallelTransition loseFocus = new ParallelTransition(
                dimScreen,
                blurScreen
        );


        PauseTransition holdDim = new PauseTransition(
                Duration.seconds(3)
        );


        FadeTransition restoreScreen = new FadeTransition(
                Duration.seconds(1.5),
                transitionPane
        );

        restoreScreen.setFromValue(0.65);
        restoreScreen.setToValue(0.0);

        Timeline restoreFocus = new Timeline(
                new KeyFrame(
                        Duration.ZERO,
                        new KeyValue(blur.radiusProperty(), 14)
                ),

                new KeyFrame(
                        Duration.seconds(1.5),
                        new KeyValue(blur.radiusProperty(), 0)
                )
        );

        ParallelTransition regainFocus = new ParallelTransition(
                restoreScreen,
                restoreFocus
        );

        SequentialTransition panicTransition = new SequentialTransition(
                loseFocus,
                holdDim,
                regainFocus
        );

        panicTransition.setOnFinished(event -> {
            panicBreathing.stop();

            root.setEffect(null);

            transitionPane.setVisible(false);
            transitionPane.setMouseTransparent(false);

            eventManager.liveEventFinished();
        });

        panicTransition.play();
    }

    private void startLightFlicker() {
        transitionContent.setOpacity(0.0);

        transitionPane.setVisible(true);
        transitionPane.setMouseTransparent(true);
        transitionPane.setOpacity(0.0);

        lightFlickerSoundEffect.play();

        FadeTransition darkFlash1 = new FadeTransition(
                Duration.seconds(0.08),
                transitionPane
        );
        darkFlash1.setFromValue(0.0);
        darkFlash1.setToValue(1.0);

        PauseTransition holdBlack1 = new PauseTransition(
                Duration.seconds(1.0)
        );

        FadeTransition recover1 = new FadeTransition(
                Duration.seconds(0.35),
                transitionPane
        );
        recover1.setFromValue(1.0);
        recover1.setToValue(0.0);

        PauseTransition pause1 = new PauseTransition(
                Duration.seconds(0.8)
        );


        FadeTransition darkFlash2 = new FadeTransition(
                Duration.seconds(0.06),
                transitionPane
        );
        darkFlash2.setFromValue(0.0);
        darkFlash2.setToValue(1.0);

        PauseTransition holdBlack2 = new PauseTransition(
                Duration.seconds(0.6)
        );

        FadeTransition recover2 = new FadeTransition(
                Duration.seconds(0.30),
                transitionPane
        );
        recover2.setFromValue(1.0);
        recover2.setToValue(0.0);

        PauseTransition pause2 = new PauseTransition(
                Duration.seconds(1.2)
        );


        FadeTransition darkFlash3 = new FadeTransition(
                Duration.seconds(0.10),
                transitionPane
        );
        darkFlash3.setFromValue(0.0);
        darkFlash3.setToValue(1.0);

        PauseTransition holdBlack3 = new PauseTransition(
                Duration.seconds(1.2)
        );

        FadeTransition recover3 = new FadeTransition(
                Duration.seconds(0.40),
                transitionPane
        );
        recover3.setFromValue(1.0);
        recover3.setToValue(0.0);

        PauseTransition pause3 = new PauseTransition(
                Duration.seconds(0.7)
        );


        FadeTransition darkFlash4 = new FadeTransition(
                Duration.seconds(0.07),
                transitionPane
        );
        darkFlash4.setFromValue(0.0);
        darkFlash4.setToValue(1.0);

        PauseTransition holdBlack4 = new PauseTransition(
                Duration.seconds(0.8)
        );

        FadeTransition recover4 = new FadeTransition(
                Duration.seconds(0.35),
                transitionPane
        );
        recover4.setFromValue(1.0);
        recover4.setToValue(0.0);


        SequentialTransition flicker = new SequentialTransition(
                darkFlash1,
                holdBlack1,
                recover1,
                pause1,

                darkFlash2,
                holdBlack2,
                recover2,
                pause2,

                darkFlash3,
                holdBlack3,
                recover3,
                pause3,

                darkFlash4,
                holdBlack4,
                recover4
        );


        flicker.setOnFinished(event -> {
            lightFlickerSoundEffect.stop();

            transitionPane.setVisible(false);
            transitionPane.setMouseTransparent(false);

            eventManager.liveEventFinished();
        });


        flicker.play();
    }
}
