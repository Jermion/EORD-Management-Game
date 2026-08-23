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

import java.io.IOException;
import java.nio.file.Files;

import java.nio.file.Paths;


public class Controller {

    private Creature creature;
    private EventManager eventManager;
    private long startTime;

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
    public void initialize() {
        creature = new Creature();
        startTime = System.currentTimeMillis();
        eventManager = new EventManager(
                creature,
                // the 'this::' allows EventManager to use these methods
                this::updateStatusLabels,
                this::addDialogue,
                this::addStatus,
                this::addEventStatus,
                this::addNoActionStatus,
                this::startRiverTransition
                );

        updateStatusLabels();

        loadFonts();

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

        FadeTransition fadeFromBlack = new FadeTransition(
                Duration.seconds(1.5),
                transitionPane
        );

        fadeFromBlack.setFromValue(1.0);
        fadeFromBlack.setToValue(0.0);

        SequentialTransition introTransition = new SequentialTransition(
                fadeContentIn,
                holdTransition,
                fadeContentOut,
                fadeFromBlack
        );

        introTransition.setOnFinished(event -> {
            transitionPane.setVisible(false);

            eventManager.start();
        });

        introTransition.play();


    }

    private void startRiverTransition(River nextRiver) {
        riverTransitionImage.setImage(null);
        transitionFooter.setText("");
        transitionQuote.setTextFill(Color.WHITE);
        switch (nextRiver) {
            case RIVER_I -> {
                transitionTitle.setText("RIVER I");
                transitionTitle.setTextFill(Color.WHITE);
                transitionQuote.setText("");
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
                transitionTitle.setTextFill(Color.WHITE);
                transitionQuote.setText("");
            }
        }

        transitionPane.setVisible(true);
        transitionPane.setOpacity(0.0);
        transitionContent.setOpacity(0.0);

        FadeTransition fadeToBlack = new FadeTransition(
                Duration.seconds(1),
                transitionPane
        );

        fadeToBlack.setFromValue(0.0);
        fadeToBlack.setToValue(1.0);

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
                    // Nothing There
                }
                case RIVER_II -> {
                    addRiverDialogue("The current feels different. Do you dare fight against it?");

                    addRiverMessage(
                            "...And the echos of self rely on no being...\n\n" +
                                    "The River remembers what the vessel has forgotten.\n\n" +
                                    "What gazes inward will eventually gaze back..."
                    );
                }
                case RIVER_III -> {
                    // Nothing rn
                }
            }
            eventManager.finishRiverTransition(nextRiver);
        });

        transition.play();
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
}
