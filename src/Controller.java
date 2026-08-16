import enums.ArtificialStat;
import enums.Sin;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class Controller {

    private Creature creature;

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
    public void initialize() {
        creature = new Creature();

        prideLabel.setText("PRIDE - " + creature.getBiologicalSystem().getCondition(Sin.PRIDE));
        gluttonyLabel.setText("GLUTTONY - " + creature.getBiologicalSystem().getCondition(Sin.GLUTTONY));
        slothLabel.setText("SLOTH - " + creature.getBiologicalSystem().getCondition(Sin.SLOTH));
        wrathLabel.setText("WRATH - " + creature.getBiologicalSystem().getCondition(Sin.WRATH));

        temperatureLabel.setText("TEMPERATURE - " + creature.getArtificialSystem().getCondition(ArtificialStat.TEMPERATURE));
        storageLabel.setText("STORAGE - " + creature.getArtificialSystem().getCondition(ArtificialStat.STORAGE));
        powerLabel.setText("POWER - " + creature.getArtificialSystem().getCondition(ArtificialStat.POWER));
        integrityLabel.setText("INTEGRITY - " + creature.getArtificialSystem().getCondition(ArtificialStat.INTEGRITY));





    }
}
