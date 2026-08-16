import enums.ArtificialStat;
import enums.Sin;
import javafx.application.Application;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MachineDriver extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                MachineDriver.class.getResource("main.fxml")
        );

        Scene scene = new Scene(loader.load());
        stage.setTitle("Machine Simulator");
        stage.setScene(scene);
        stage.show();
    }

    static void main(String[] args) {
        launch();
    }
}
