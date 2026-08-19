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
    public void initialize() {
        creature = new Creature();
        startTime = System.currentTimeMillis();
        eventManager = new EventManager(
                creature,
                // the 'this::' allows EventManager to use these methods
                this::updateStatusLabels,
                this::addDialogue,
                this::addStatus,
                this::addEventStatus
                );

        updateStatusLabels();

        dialogueBox.heightProperty().addListener((observableValue, oldValue, newValue) -> {
            dialogueScrollPane.setVvalue(1.0);
        });

        statusBox.heightProperty().addListener((observableValue, oldValue, newValue) -> {
            statusScrollPane.setVvalue(1.0);
        });

        eventManager.start();
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

    private void addEventStatus(String message) {
        Label newStatus = new Label(getTimestamp() + " " + message);

        newStatus.setWrapText(true);
        newStatus.setTextFill(Color.MAROON);

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

    }

    @FXML
    private void restrain() {
        creature.getBiologicalSystem().changeSin(Sin.PRIDE, -15);
        creature.getBiologicalSystem().changeSin(Sin.WRATH, 10);

        updateStatusLabels();

        addStatus("[RESTRAINING ENTITY NOW...]\n" +
                "   [PRIDE HAS DECREASED.]\n" +
                "   [WRATH HAS INCREASED.]");

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

}
