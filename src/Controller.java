import enums.ArtificialCondition;
import enums.ArtificialStat;
import enums.BioCondition;
import enums.Sin;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.layout.VBox;
import javafx.scene.control.ScrollPane;


public class Controller {

    private Creature creature;
    private EventManager eventManager;

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
    public void initialize() {
        creature = new Creature();
        eventManager = new EventManager(
                creature,
                // the 'this::' allows EventManager to use these methods
                this::updateStatusLabels,
                this::addDialogue
                );

        updateStatusLabels();

        dialogueBox.heightProperty().addListener((observableValue, oldValue, newValue) -> {
            dialogueScrollPane.setVvalue(1.0);
        });

        eventManager.start();
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
        eventManager.suppressUsed();
    }

    @FXML
    private void coolant() {
        creature.getArtificialSystem().changeStat(ArtificialStat.TEMPERATURE, -15);
        creature.getArtificialSystem().changeStat(ArtificialStat.POWER, -10);

        updateStatusLabels();
        eventManager.coolantUsed();
    }

    @FXML
    private void stimulate() {
        creature.getBiologicalSystem().changeSin(Sin.SLOTH, -15);
        creature.getArtificialSystem().changeStat(ArtificialStat.TEMPERATURE, 10);

        updateStatusLabels();
    }

    @FXML
    private void charge() {
        creature.getArtificialSystem().changeStat(ArtificialStat.POWER, 15);
        creature.getArtificialSystem().changeStat(ArtificialStat.STORAGE, 10);

        updateStatusLabels();
    }

    @FXML
    private void repair() {
        creature.getArtificialSystem().changeStat(ArtificialStat.INTEGRITY, 15);
        creature.getBiologicalSystem().changeSin(Sin.GLUTTONY, -10);

        updateStatusLabels();
    }

    @FXML
    private void nourish() {
        creature.getBiologicalSystem().changeSin(Sin.GLUTTONY, 15);
        creature.getBiologicalSystem().changeSin(Sin.PRIDE, 10);

        updateStatusLabels();
    }

    @FXML
    private void restrain() {
        creature.getBiologicalSystem().changeSin(Sin.PRIDE, -15);
        creature.getBiologicalSystem().changeSin(Sin.WRATH, 10);

        updateStatusLabels();
    }

    @FXML
    private void purge() {
        creature.getArtificialSystem().changeStat(ArtificialStat.STORAGE, -15);
        creature.getArtificialSystem().changeStat(ArtificialStat.INTEGRITY, -10);

        updateStatusLabels();
    }

}
