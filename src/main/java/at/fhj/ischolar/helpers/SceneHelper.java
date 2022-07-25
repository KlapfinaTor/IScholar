package at.fhj.ischolar.helpers;

import at.fhj.ischolar.data.User;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * Helper class that keeps track of the primary and secondary scene.
 */
public class SceneHelper {
    private static Stage primaryStageObject; //Holds the primary stage (window)
    private static Stage secondaryStageObject; //Holds the secondary stage(window) for the login window.
    private final String pathAdminView = "/views/admin_view.fxml";
    private final String pathMainView = "/views/main_view.fxml";
    private final String pathPlanView = "/views/plan_view.fxml";
    private LoginHelper loginHelper = new LoginHelper();

    public static Stage getPrimaryStageObject() {
        return primaryStageObject;
    }

    public static void setPrimaryStageObject(Stage primaryStageObject) {
        SceneHelper.primaryStageObject = primaryStageObject;
    }

    public static Stage getSecondaryStageObject() {
        return secondaryStageObject;
    }

    /**
     * Loads a scene by the fxml file.
     *
     * @param fxml The fxml for that scene
     */
    private void loadScene(String fxml) {
        try {
            double currentWindowWidth = primaryStageObject.getWidth() - 16;
            double currentWindowHeight = primaryStageObject.getHeight() - 39;

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();

            primaryStageObject.setScene(new Scene(root, currentWindowWidth, currentWindowHeight));
            primaryStageObject.setTitle("IScholar");
            primaryStageObject.show();
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
    }

    /**
     * Checks if the current logged in user has the Role Admin or Assistant
     *
     * @return true if admin or assistant
     */
    private boolean isLoggedInUserAdminOrAssis() {
        return (loginHelper.getCurrentLoggedInUser().getRole().equals(User.Role.Admin) ||
                loginHelper.getCurrentLoggedInUser().getRole().equals(User.Role.Assistant));
    }

    /**
     * Changes the scene to the Admin View, checks if current logged in user has the correct role.
     */
    public void changeSceneToAdminView() {
        if (isLoggedInUserAdminOrAssis()) {
            loadScene(pathAdminView);
        }
    }

    /**
     * Changes the scene to the Main View.
     */
    public void changeSceneToMainView() {
        loadScene(pathMainView);
    }

    /**
     * Changes the scene to the Plan View, checks if current logged in user has the correct role.
     */
    public void changeSceneToPlanView() {
        if (isLoggedInUserAdminOrAssis()) {
            loadScene(pathPlanView);
        }
    }

    /**
     * Shows the login window.
     */
    public void showLoginWindow() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/login_view.fxml"));

            Scene scene = new Scene(root);
            Stage loginStage = new Stage();
            secondaryStageObject = loginStage;
            loginStage.initModality(Modality.WINDOW_MODAL);
            loginStage.initOwner(primaryStageObject.getScene().getWindow());
            loginStage.setScene(scene);
            loginStage.setTitle("IScholar - Login");
            loginStage.show();
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
    }
}
