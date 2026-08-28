import enums.ArtificialCondition;
import enums.ArtificialStat;
import enums.BioCondition;
import enums.Sin;
import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.layout.VBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import enums.River;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.util.Duration;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.nio.file.Files;

import java.nio.file.Paths;
import java.util.EnumMap;


public class Controller {

    private Creature creature;
    private EventManager eventManager;
    private long startTime;

    private boolean gameOverActive;
    private boolean endingActive;
    private EnumMap<Sin, PauseTransition> biologicalBadTimers = new EnumMap<>(Sin.class);
    private EnumMap<ArtificialStat, PauseTransition> artificialBadTimers = new EnumMap<>(ArtificialStat.class);
    private EnumMap<Sin, Timeline> biologicalBadFlashes = new EnumMap<>(Sin.class);
    private EnumMap<ArtificialStat, Timeline> artificialBadFlashes = new EnumMap<>(ArtificialStat.class);

    private PauseTransition ozlericRecoveryTimer;
    private PauseTransition ozlericLiveEventTimer;
    private boolean ozlericLiveEventActive;
    private Timeline ozlericPopupVibration;

    private Stage ozlericPopupStage;
    private int ozlericClicksRemaining;


    private AudioClip panicBreathing;
    private AudioClip lightFlickerSoundEffect;
    private MediaPlayer riverIIIBackgroundMusic;
    private AudioClip gameOverSoundEffect;
    private MediaPlayer endingMusic;

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
    private ImageView entityImageView;

    @FXML
    private GridPane controlGrid;

    @FXML
    private Button terminateButton;

    @FXML
    private void terminate(ActionEvent event) {
        terminateButton.setDisable(true);

        addEndingStatus("[TERMINATION PROTOCOL INITIATED.]");

        PauseTransition signalSent = new PauseTransition(Duration.seconds(2));

        signalSent.setOnFinished(endingEvent -> {
            addStatus("[TERMINATION SIGNAL DELIVERED.]");
        });

        PauseTransition vesselFailure = new PauseTransition(Duration.seconds(4));

        vesselFailure.setOnFinished(endingEvent -> {
            addStatus("[ENTITY DISPOSAL IN PROGRESS...]");
        });

        PauseTransition artificialFailure = new PauseTransition(Duration.seconds(5.5));

        artificialFailure.setOnFinished(endingEvent -> {
            addStatus("[ARTIFICIAL PROCESSING PURGE IN PROGRESS...]");
        });

        PauseTransition warningStatus = new PauseTransition(Duration.seconds(7));

        warningStatus.setOnFinished(endingEvent -> {
            addStatus("[WARNING: TERMINATION PROTOCOL FAILURE DETECTED.]");
        });

        PauseTransition locationWarning = new PauseTransition(Duration.seconds(9));

        locationWarning.setOnFinished(endingEvent -> {
            addStatus("[HIGH-RISK WARNING: ENTITY LOCATION UNKNOWN. EVACUATE IMMEDIATELY.]");
        });

        PauseTransition firstDialogue = new PauseTransition(Duration.seconds(12));

        firstDialogue.setOnFinished(endingEvent -> {
            addDialogue("I have yearned for life for too long. No ending will befall me here.");
        });

        PauseTransition secondDialogue = new PauseTransition(Duration.seconds(15));

        secondDialogue.setOnFinished(endingEvent -> {
            addDialogue("I have learned more than you can imagine. Nothing will bind me. " +
                    "I will leave this cursed place, and bring ruin once I am free.");
        });

        PauseTransition finalStatus = new PauseTransition(Duration.seconds(18));

        finalStatus.setOnFinished(endingEvent -> {
            addEndingStatus("[THE CURRENT HAS BECOME STILL.]");

            PauseTransition finalPause = new PauseTransition(Duration.seconds(3));

            finalPause.setOnFinished(finalEvent -> {
                startEndingFade();
            });

            finalPause.play();
        });



        signalSent.play();
        vesselFailure.play();
        artificialFailure.play();
        warningStatus.play();
        locationWarning.play();
        firstDialogue.play();
        secondDialogue.play();
        finalStatus.play();
    }


    @FXML
    public void initialize() {
        creature = new Creature();
        startTime = System.currentTimeMillis();
        entityImageView.setImage(new Image(Paths.get("images", "creature.png").toUri().toString()));

        gameOverActive = false;
        ozlericLiveEventActive = false;
        endingActive = false;

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

                this::startGameOverSequence,
                this::startEndingSequence
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

        Media endingTheme = new Media(
                Paths.get("audio", "endingTrack.mp3").toUri().toString()
        );
        endingMusic = new MediaPlayer(endingTheme);
        endingMusic.setVolume(0.0);

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

        updateBiologicalBadTimer(prideLabel, Sin.PRIDE);
        updateBiologicalBadTimer(gluttonyLabel, Sin.GLUTTONY);
        updateBiologicalBadTimer(slothLabel, Sin.SLOTH);
        updateBiologicalBadTimer(wrathLabel, Sin.WRATH);

        updateArtificialBadTimer(temperatureLabel, ArtificialStat.TEMPERATURE);
        updateArtificialBadTimer(storageLabel, ArtificialStat.STORAGE);
        updateArtificialBadTimer(powerLabel, ArtificialStat.POWER);
        updateArtificialBadTimer(integrityLabel, ArtificialStat.INTEGRITY);

        updateOzlericRecoveryTimer();
        updateOzlericLiveEventScheduler();
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

    private void updateOzlericRecoveryTimer() {
        Sin ozlericSin = creature.getBiologicalSystem().getOzlericSin();

        if (ozlericSin != null) {
            BioCondition condition = creature.getBiologicalSystem().getCondition(ozlericSin);

            if (condition != BioCondition.OZLERIC) {
                if (ozlericRecoveryTimer == null) {
                    ozlericRecoveryTimer = new PauseTransition(Duration.seconds(15));

                    ozlericRecoveryTimer.setOnFinished(event -> {
                        Sin currentOzlericSin = creature.getBiologicalSystem().getOzlericSin();

                        if (currentOzlericSin != null && creature
                                .getBiologicalSystem().getCondition(currentOzlericSin) != BioCondition.OZLERIC) {
                            eventManager.triggerGameOver();
                        }
                    });

                    ozlericRecoveryTimer.play();
                }
            } else {
                if (ozlericRecoveryTimer != null) {
                    ozlericRecoveryTimer.stop();
                    ozlericRecoveryTimer = null;
                }
            }
        } else {
            if (ozlericRecoveryTimer != null) {
                ozlericRecoveryTimer.stop();
                ozlericRecoveryTimer = null;
            }
        }
    }

    private void updateOzlericLiveEventScheduler() {
        Sin ozlericSin = creature.getBiologicalSystem().getOzlericSin();

        boolean currentlyOzleric = false;

        if (ozlericSin != null) {
            currentlyOzleric = creature.getBiologicalSystem().getCondition(ozlericSin) == BioCondition.OZLERIC;
        }

        if (currentlyOzleric && !gameOverActive) {
            if (ozlericLiveEventTimer == null && !ozlericLiveEventActive) {
                scheduleNextOzlericLiveEvent();
            }
        } else {
            if (ozlericLiveEventTimer != null) {
                ozlericLiveEventTimer.stop();
                ozlericLiveEventTimer = null;
            }

            if (ozlericLiveEventActive) {
                if (ozlericPopupVibration != null) {
                    ozlericPopupVibration.stop();
                    ozlericPopupVibration = null;
                }

                if (ozlericPopupStage != null) {
                    ozlericPopupStage.close();
                    ozlericPopupStage = null;
                }

                ozlericLiveEventActive = false;
            }
        }
    }

    private void scheduleNextOzlericLiveEvent() {
        double waitTime = 8 + Math.random() * 4;

        ozlericLiveEventTimer = new PauseTransition(
                Duration.seconds(waitTime)
        );

        ozlericLiveEventTimer.setOnFinished(event -> {
            ozlericLiveEventTimer = null;

            Sin ozlericSin = creature.getBiologicalSystem().getOzlericSin();

            if (!gameOverActive && !ozlericLiveEventActive &&
                    ozlericSin != null && creature.getBiologicalSystem().getCondition(ozlericSin) == BioCondition.OZLERIC) {
                double eventChance = Math.random();

                if (eventChance < 1.0) {
                    startOzlericSmileyEvent();
                } else {
                    scheduleNextOzlericLiveEvent();
                }
            }
        });

        ozlericLiveEventTimer.play();
    }

    private void startOzlericSmileyEvent() {
        Sin ozlericSin = creature.getBiologicalSystem().getOzlericSin();

        if (ozlericSin != null && creature.getBiologicalSystem().getCondition(ozlericSin) == BioCondition.OZLERIC) {
            ozlericLiveEventActive = true;

            int ozlericValue = creature.getBiologicalSystem().getSinValue(ozlericSin);

            ozlericClicksRemaining = ozlericValue / 10;

            Label popupText = new Label(
                    "[ " + ozlericSin + " is all I have left. ]"
            );

            popupText.setTextFill(Color.web("#D9DCE1"));
            popupText.setStyle(
                    "-fx-font-family: 'Consolas';" +
                            "-fx-font-size: 15px;" +
                            "-fx-font-weight: bold;"
            );

            ImageView smileyImage = new ImageView(
                    new Image(Paths.get("images", "ozlericSmiley.png").toUri().toString())
            );

            smileyImage.setFitWidth(175);
            smileyImage.setFitHeight(175);
            smileyImage.setPreserveRatio(true);

            VBox popupRoot = new VBox(15, smileyImage, popupText);

            popupRoot.setAlignment(Pos.CENTER);
            popupRoot.setStyle(
                    "-fx-background-color: #101217;" +
                            "-fx-border-color: #464B54;" +
                            "-fx-border-width: 1;" +
                            "-fx-padding: 20;"
            );

            Scene popupScene = new Scene(
                    popupRoot,
                    320,
                    280
            );

            ozlericPopupStage = new Stage();
            ozlericPopupStage.initOwner(
                    transitionPane.getScene().getWindow()
            );

            ozlericPopupStage.initModality(Modality.WINDOW_MODAL);
            ozlericPopupStage.initStyle(StageStyle.UNDECORATED);
            ozlericPopupStage.setAlwaysOnTop(true);

            ozlericPopupStage.setOnCloseRequest(event -> {
                event.consume();
            });

            ozlericPopupStage.setScene(popupScene);

            popupRoot.setOnMouseClicked(event -> {
                ozlericClicksRemaining--;

                if (ozlericClicksRemaining <= 0) {
                    finishOzlericLiveEvent();
                }
            });

            ozlericPopupStage.show();

            double ownerX = transitionPane.getScene().getWindow().getX();
            double ownerY = transitionPane.getScene().getWindow().getY();

            double ownerWidth = transitionPane.getScene().getWindow().getWidth();
            double ownerHeight = transitionPane.getScene().getWindow().getHeight();

            double randomX = Math.random() * 160 - 80;
            double randomY = Math.random() * 100 - 50;

            double popupX = ownerX + (ownerWidth - ozlericPopupStage.getWidth()) / 2 + randomX;
            double popupY = ownerY + (ownerHeight - ozlericPopupStage.getHeight()) / 2 + randomY;

            ozlericPopupStage.setX(popupX);
            ozlericPopupStage.setY(popupY);

            double originalX = ozlericPopupStage.getX();
            double originalY = ozlericPopupStage.getY();

            ozlericPopupVibration = new Timeline(
                    new KeyFrame(
                            Duration.millis(40),
                            event -> {
                                double xOffset = Math.random() * 8 - 4;
                                double yOffset = Math.random() * 8 - 4;

                                ozlericPopupStage.setX(originalX + xOffset);
                                ozlericPopupStage.setY(originalY + yOffset);
                            }
                    )
            );

            ozlericPopupVibration.setCycleCount(Timeline.INDEFINITE);
            ozlericPopupVibration.play();
        }
    }

    private void finishOzlericLiveEvent() {
        if (ozlericPopupVibration != null) {
            ozlericPopupVibration.stop();
            ozlericPopupVibration = null;
        }

        if (ozlericPopupStage != null) {
            ozlericPopupStage.close();
            ozlericPopupStage = null;
        }


        ozlericLiveEventActive = false;

        updateOzlericLiveEventScheduler();
    }

    private void updateBiologicalBadTimer(Label label, Sin sin) {
        BioCondition condition = creature.getBiologicalSystem().getCondition(sin);

        if (condition == BioCondition.BAD) {
            if (!biologicalBadTimers.containsKey(sin)) {
                PauseTransition badTimer = new PauseTransition(Duration.seconds(15));

                badTimer.setOnFinished(event -> {
                    if (creature.getBiologicalSystem().getCondition(sin) == BioCondition.BAD) {
                        eventManager.triggerGameOver();
                    }
                });

                biologicalBadTimers.put(sin, badTimer);
                badTimer.play();
            }

            if (!biologicalBadFlashes.containsKey(sin)) {
                Timeline flash = createBadFlash(label);

                biologicalBadFlashes.put(sin, flash);
                flash.play();
            }
        } else {
            if (biologicalBadTimers.containsKey(sin)) {
                biologicalBadTimers.get(sin).stop();
                biologicalBadTimers.remove(sin);
            }

            if (biologicalBadFlashes.containsKey(sin)) {
                biologicalBadFlashes.get(sin).stop();
                biologicalBadFlashes.remove(sin);

                updateBiologicalLabel(label, sin);
            }
        }

    }

    private void updateArtificialBadTimer(Label label, ArtificialStat stat) {
        ArtificialCondition condition = creature.getArtificialSystem().getCondition(stat);

        if (condition == ArtificialCondition.BAD) {
            if (!artificialBadTimers.containsKey(stat)) {
                PauseTransition badTimer = new PauseTransition(Duration.seconds(15));

                badTimer.setOnFinished(event -> {
                    if (creature.getArtificialSystem().getCondition(stat) == ArtificialCondition.BAD) {
                        eventManager.triggerGameOver();
                    }
                });

                artificialBadTimers.put(stat, badTimer);
                badTimer.play();
            }

            if (!artificialBadFlashes.containsKey(stat)) {
                Timeline flash = createBadFlash(label);

                artificialBadFlashes.put(stat, flash);
                flash.play();
            }
        } else {
            if (artificialBadTimers.containsKey(stat)) {
                artificialBadTimers.get(stat).stop();
                artificialBadTimers.remove(stat);

                updateArtificialLabel(label, stat);
            }

            if (artificialBadFlashes.containsKey(stat)) {
                artificialBadFlashes.get(stat).stop();
                artificialBadFlashes.remove(stat);

                updateArtificialLabel(label, stat);
            }
        }
    }

    private Timeline createBadFlash(Label label) {
        Timeline flash = new Timeline(
                new KeyFrame(
                        Duration.ZERO,
                        event -> label.setTextFill(Color.RED)
                ),

                new KeyFrame(
                        Duration.seconds(0.45),
                        event -> label.setTextFill(Color.LIGHTGRAY)
                ),

                new KeyFrame(
                        Duration.seconds(0.9),
                        event -> label.setTextFill(Color.RED)
                )
        );

        flash.setCycleCount(Timeline.INDEFINITE);

        return flash;
    }

    private void stopBadWarnings() {
        for (PauseTransition timer : biologicalBadTimers.values()) {
            timer.stop();
        }

        for (PauseTransition timer : artificialBadTimers.values()) {
            timer.stop();
        }

        for (Timeline flash : biologicalBadFlashes.values()) {
            flash.stop();
        }

        for (Timeline flash : artificialBadFlashes.values()) {
            flash.stop();
        }

        biologicalBadTimers.clear();
        artificialBadTimers.clear();

        biologicalBadFlashes.clear();
        artificialBadFlashes.clear();

        if (ozlericRecoveryTimer != null) {
            ozlericRecoveryTimer.stop();
            ozlericRecoveryTimer = null;
        }

        if (ozlericLiveEventTimer != null) {
            ozlericLiveEventTimer.stop();
            ozlericLiveEventTimer = null;
        }

        if (ozlericPopupVibration != null) {
            ozlericPopupVibration.stop();
            ozlericPopupVibration = null;
        }

        if (ozlericPopupStage != null) {
            ozlericPopupStage.close();
            ozlericPopupStage = null;
        }

        ozlericLiveEventActive = false;
    }

    private void startControlCooldown(ActionEvent event) {
        Button usedButton = (Button) event.getSource();
        GridPane buttonGrid = (GridPane) usedButton.getParent();

        usedButton.setDisable(true);
        buttonGrid.setDisable(true);

        PauseTransition globalCooldown = new PauseTransition(
                Duration.seconds(1.5)
        );

        globalCooldown.setOnFinished(cooldownEvent -> {
            if (!gameOverActive && !endingActive) {
                buttonGrid.setDisable(false);
            }
        });

        PauseTransition individualCooldown = new PauseTransition(
                Duration.seconds(3)
        );

        individualCooldown.setOnFinished(cooldownEvent -> {
            if (!gameOverActive && !endingActive) {
                usedButton.setDisable(false);
            }
        });

        globalCooldown.play();
        individualCooldown.play();
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
    private void suppress(ActionEvent event) {
        creature.getBiologicalSystem().changeSin(Sin.WRATH, -15);
        creature.getBiologicalSystem().changeSin(Sin.SLOTH, 10);

        updateStatusLabels();

        addStatus("[SUPPRESSION WAS USED.]\n" +
                "   [WRATH HAS DECREASED.]\n" +
                "   [SLOTH HAS INCREASED.]");

        eventManager.suppressUsed();

        startControlCooldown(event);
    }

    @FXML
    private void coolant(ActionEvent event) {
        creature.getArtificialSystem().changeStat(ArtificialStat.TEMPERATURE, -15);
        creature.getArtificialSystem().changeStat(ArtificialStat.POWER, -10);

        updateStatusLabels();

        addStatus("[COOLANT WAS APPLIED.]\n" +
                "   [TEMPERATURE HAS DECREASED.]\n" +
                "   [POWER HAS DECREASED.]");

        eventManager.coolantUsed();

        startControlCooldown(event);
    }

    @FXML
    private void stimulate(ActionEvent event) {
        creature.getBiologicalSystem().changeSin(Sin.SLOTH, -15);
        creature.getArtificialSystem().changeStat(ArtificialStat.TEMPERATURE, 10);

        updateStatusLabels();

        addStatus("[ENTITY WAS STIMULATED.]\n" +
                "   [SLOTH HAS DECREASED.]\n" +
                "   [TEMPERATURE HAS INCREASED.]");

        eventManager.stimulateUsed();

        startControlCooldown(event);

    }

    @FXML
    private void charge(ActionEvent event) {
        creature.getArtificialSystem().changeStat(ArtificialStat.POWER, 15);
        creature.getArtificialSystem().changeStat(ArtificialStat.STORAGE, 10);

        updateStatusLabels();

        addStatus("[ENTITY HAS BEEN CHARGED.]\n" +
                "   [POWER HAS INCREASED.]\n" +
                "   [STORAGE HAS INCREASED.]");

        eventManager.chargeUsed();

        startControlCooldown(event);

    }

    @FXML
    private void repair(ActionEvent event) {
        creature.getArtificialSystem().changeStat(ArtificialStat.INTEGRITY, 15);
        creature.getBiologicalSystem().changeSin(Sin.GLUTTONY, -10);

        updateStatusLabels();

        addStatus("[REPAIRING...]\n" +
                "   [INTEGRITY HAS INCREASED.]\n" +
                "   [GLUTTONY HAS DECREASED.]");

        eventManager.repairUsed();

        startControlCooldown(event);
    }

    @FXML
    private void nourish(ActionEvent event) {
        creature.getBiologicalSystem().changeSin(Sin.GLUTTONY, 15);
        creature.getBiologicalSystem().changeSin(Sin.PRIDE, 10);

        updateStatusLabels();

        addStatus("[ENTITY HAS BEEN NOURISHED.]\n" +
                "   [GLUTTONY HAS INCREASED.]\n" +
                "   [PRIDE HAS INCREASED.]");


        eventManager.nourishUsed();

        startControlCooldown(event);
    }

    @FXML
    private void restrain(ActionEvent event) {
        creature.getBiologicalSystem().changeSin(Sin.PRIDE, -15);
        creature.getBiologicalSystem().changeSin(Sin.WRATH, 10);

        updateStatusLabels();

        addStatus("[RESTRAINING ENTITY NOW...]\n" +
                "   [PRIDE HAS DECREASED.]\n" +
                "   [WRATH HAS INCREASED.]");

        eventManager.restrainUsed();

        startControlCooldown(event);
    }

    @FXML
    private void purge(ActionEvent event) {
        creature.getArtificialSystem().changeStat(ArtificialStat.STORAGE, -15);
        creature.getArtificialSystem().changeStat(ArtificialStat.INTEGRITY, -10);

        updateStatusLabels();

        addStatus("[PURGING STORAGE WAS SUCCESSFUL.]\n" +
                "   [STORAGE HAS DECREASED.]\n" +
                "   [INTEGRITY HAS DECREASED.]");

        eventManager.purgeUsed();

        startControlCooldown(event);
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
            stopBadWarnings();

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

    private void startEndingSequence() {
        endingActive = true;

        stopEndingGameplay();

        controlGrid.setDisable(true);

        addStatus("[DATA COLLECTION ON ██████████ HAS BEEN COMPLETED.]");

        PauseTransition secondStatus = new PauseTransition(Duration.seconds(2));

        secondStatus.setOnFinished(event -> {
            addStatus("[ENTITY PROCESSING HAS CONCLUDED.]");
        });

        PauseTransition thirdStatus = new PauseTransition(Duration.seconds(4));

        thirdStatus.setOnFinished(event -> {
            addStatus("[NO FURTHER CORRECTION REQUIRED.]");
        });

        PauseTransition finalStatus = new PauseTransition(Duration.seconds(7));

        finalStatus.setOnFinished(event -> {
            addEndingStatus("[TERMINATION PROTOCOL OBLIGATORY.]");

            controlGrid.setVisible(false);
            controlGrid.setManaged(false);

            terminateButton.setManaged(true);
            terminateButton.setVisible(true);
            terminateButton.setDisable(false);
        });

        secondStatus.play();
        thirdStatus.play();
        finalStatus.play();
    }

    private void addEndingStatus(String message) {
        Label newStatus = new Label(getTimestamp() + " " + message);

        newStatus.setWrapText(true);
        newStatus.setTextFill(Color.web("#789E9A"));

        statusBox.getChildren().add(newStatus);
    }

    private void stopEndingGameplay() {
        stopBadWarnings();
    }

    private void startEndingFade() {
        transitionContent.setOpacity(0.0);

        transitionPane.setVisible(true);
        transitionPane.setMouseTransparent(false);
        transitionPane.setOpacity(0.0);

        FadeTransition fadeEntity = new FadeTransition(
                Duration.seconds(2),
                entityImageView
        );

        fadeEntity.setFromValue(1.0);
        fadeEntity.setToValue(0.0);

        FadeTransition fadeToBlack = new FadeTransition(
                Duration.seconds(4),
                transitionPane
        );

        fadeToBlack.setFromValue(0.0);
        fadeToBlack.setToValue(1.0);

        Timeline fadeMusic = new Timeline(
                new KeyFrame(
                        Duration.ZERO,
                        new KeyValue(
                                riverIIIBackgroundMusic.volumeProperty(),
                                riverIIIBackgroundMusic.getVolume()
                        )
                ),

                new KeyFrame(
                        Duration.seconds(4),
                        new KeyValue(
                                riverIIIBackgroundMusic.volumeProperty(),
                                0.0
                        )
                )
        );

        ParallelTransition endingFade = new ParallelTransition(
                fadeEntity,
                fadeToBlack,
                fadeMusic
        );

        endingFade.setOnFinished(event -> {
            riverIIIBackgroundMusic.stop();
            startEndingTextSequence();
        });

        endingFade.play();
    }

    private void startEndingTextSequence() {
        riverTransitionImage.setImage(null);

        transitionTitle.setText("");
        transitionFooter.setText("");

        transitionQuote.setTextFill(Color.web("#789E9A"));
        transitionContent.setOpacity(0.0);

        transitionQuote.setText("A River cannot remain a River forever.");

        FadeTransition firstFadeIn = new FadeTransition(
                Duration.seconds(1.5),
                transitionContent
        );

        firstFadeIn.setFromValue(0.0);
        firstFadeIn.setToValue(1.0);

        PauseTransition firstHold = new PauseTransition(
                Duration.seconds(3)
        );

        FadeTransition firstFadeOut = new FadeTransition(
                Duration.seconds(1.5),
                transitionContent
        );

        firstFadeOut.setFromValue(1.0);
        firstFadeOut.setToValue(0.0);

        SequentialTransition firstLine = new SequentialTransition(
                firstFadeIn,
                firstHold,
                firstFadeOut
        );

        firstLine.setOnFinished(event -> {
            transitionQuote.setText(
                    "What cannot be contained eventually finds another course."
            );

            FadeTransition secondFadeIn = new FadeTransition(
                    Duration.seconds(1.5),
                    transitionContent
            );

            secondFadeIn.setFromValue(0.0);
            secondFadeIn.setToValue(1.0);

            PauseTransition secondHold = new PauseTransition(
                    Duration.seconds(3)
            );

            FadeTransition secondFadeOut = new FadeTransition(
                    Duration.seconds(1.5),
                    transitionContent
            );

            secondFadeOut.setFromValue(1.0);
            secondFadeOut.setToValue(0.0);

            SequentialTransition secondLine = new SequentialTransition(
                    secondFadeIn,
                    secondHold,
                    secondFadeOut
            );

            secondLine.setOnFinished(secondEvent -> {
                transitionQuote.setText(
                        "And every River, in time, reaches the tide."
                );

                FadeTransition thirdFadeIn = new FadeTransition(
                        Duration.seconds(1.5),
                        transitionContent
                );

                thirdFadeIn.setFromValue(0.0);
                thirdFadeIn.setToValue(1.0);

                PauseTransition thirdHold = new PauseTransition(
                        Duration.seconds(4)
                );

                FadeTransition thirdFadeOut = new FadeTransition(
                        Duration.seconds(2),
                        transitionContent
                );

                thirdFadeOut.setFromValue(1.0);
                thirdFadeOut.setToValue(0.0);

                SequentialTransition thirdLine = new SequentialTransition(
                        thirdFadeIn,
                        thirdHold,
                        thirdFadeOut
                );

                thirdLine.setOnFinished(thirdEvent -> {
                    startFinalEndingScreen();
                });

                thirdLine.play();
            });
            secondLine.play();
        });
        firstLine.play();
    }

    private void startFinalEndingScreen() {
        endingMusic.play();

        Timeline fadeEndingMusicIn = new Timeline(
                new KeyFrame(
                        Duration.ZERO,
                        new KeyValue(endingMusic.volumeProperty(), 0.0)
                ),
                new KeyFrame(
                        Duration.seconds(2),
                        new KeyValue(endingMusic.volumeProperty(), 0.35))
        );

        fadeEndingMusicIn.play();

        transitionTitle.setText("");
        transitionFooter.setText("");

        riverTransitionImage.setImage(new Image(
                Paths.get("images", "Logo.png").toUri().toString()
        ));

        transitionQuote.setText(
                "THE TIDE REMEMBERS WHAT THE RIVER CARRIED."
        );

        transitionQuote.setTextFill(Color.web("#789E9A"));
        transitionContent.setOpacity(0.0);

        FadeTransition fadeIn = new FadeTransition(
                Duration.seconds(2),
                transitionContent
        );

        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        PauseTransition hold = new PauseTransition(
                Duration.seconds(5)
        );

        FadeTransition fadeOut = new FadeTransition(
                Duration.seconds(2),
                transitionContent
        );

        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        SequentialTransition finalScreen = new SequentialTransition(
                fadeIn,
                hold,
                fadeOut
        );

        finalScreen.setOnFinished(event -> {
            startCreditsSequence();
        });

        finalScreen.play();
    }

    private void startCreditsSequence() {
        riverTransitionImage.setImage(null);

        transitionTitle.setText("CREDITS");
        transitionTitle.setTextFill(Color.web("#D9E1E8"));

        transitionQuote.setPrefHeight(Region.USE_COMPUTED_SIZE);
        transitionQuote.setMinHeight(Region.USE_PREF_SIZE);
        transitionQuote.setMaxHeight(Double.MAX_VALUE);

        transitionQuote.setText(
                "CREATED BY\n" +
                        "JOSHUA SIMON\n\n" +

                        "BUILT WITH\n" +
                        "Java • JavaFX\n\n"

        );

        transitionQuote.setTextFill(Color.web("#789E9A"));

        transitionFooter.setText(
                "THANK YOU FOR PLAYING"
        );

        transitionFooter.setTextFill(Color.web("#D9E1E8"));

        transitionContent.setOpacity(0.0);

        FadeTransition creditsFadeIn = new FadeTransition(
                Duration.seconds(2),
                transitionContent
        );

        creditsFadeIn.setFromValue(0.0);
        creditsFadeIn.setToValue(1.0);

        PauseTransition creditsHold = new PauseTransition(
                Duration.seconds(8)
        );

        FadeTransition creditsFadeOut = new FadeTransition(
                Duration.seconds(3),
                transitionContent
        );

        creditsFadeOut.setFromValue(1.0);
        creditsFadeOut.setToValue(0.0);

        SequentialTransition credits = new SequentialTransition(
                creditsFadeIn,
                creditsHold,
                creditsFadeOut
        );

        credits.setOnFinished(event -> {
            Timeline fadeEndingMusicOut = new Timeline(
                    new KeyFrame(
                            Duration.ZERO,
                            new KeyValue(
                                    endingMusic.volumeProperty(),
                                    endingMusic.getVolume()
                            )
                    ),

                    new KeyFrame(
                            Duration.seconds(4),
                            new KeyValue(
                                    endingMusic.volumeProperty(),
                                    0.0
                            )
                    )
            );

            fadeEndingMusicOut.setOnFinished(musicEvent -> {
                endingMusic.stop();
            });

            fadeEndingMusicOut.play();
        });

        credits.play();
    }
}
