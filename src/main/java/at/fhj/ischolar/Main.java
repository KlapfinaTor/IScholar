package at.fhj.ischolar;

import at.fhj.ischolar.helpers.LoginHelper;
import at.fhj.ischolar.helpers.SceneHelper;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

/**
 * The Main Class for the IScholar application.
 */
public class Main extends Application {
    private final SceneHelper sceneHelper = new SceneHelper();

    @Override
    public void start(Stage primaryStage) throws Exception {
        SceneHelper.setPrimaryStageObject(primaryStage);

        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/main_view.fxml")));

        Scene scene = new Scene(root, 1000, 800);

        primaryStage.setScene(scene);
        primaryStage.setTitle("IScholar");
        primaryStage.show();
        primaryStage.setOnCloseRequest(e -> Platform.exit());

        if (LoginHelper.getLoggedInUserId() == -1) {
            sceneHelper.showLoginWindow();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
