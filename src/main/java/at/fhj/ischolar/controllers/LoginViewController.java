package at.fhj.ischolar.controllers;

import at.fhj.ischolar.data.DBDao;
import at.fhj.ischolar.data.User;
import at.fhj.ischolar.helpers.LoginHelper;
import at.fhj.ischolar.helpers.SceneHelper;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;


public class LoginViewController implements Initializable {
    @FXML
    private TextField userNameField;
    @FXML
    private TextField passwordField;
    @FXML
    private Label labelMessage;

    private LoginHelper loginHelper;
    private SceneHelper sceneHelper;
    private DBDao db = new DBDao();


    public void ok(ActionEvent actionEvent) {
        try{
            String username = userNameField.getText().trim();
            String password = passwordField.getText().trim();
            //ArrayList<User> allUsers = db.getAllUsers();
            User foundUser = db.getUserByLoginName(username);

            if (foundUser != null) {
                if (loginHelper.isPWMatching(password, foundUser.getPassword())) {
                    LoginHelper.setLoggedInUserId(foundUser.getId());
                    SceneHelper.getSecondaryStageObject().close();
                } else {
                    labelMessage.setText("Wrong password. Try again!");
                }
            } else {
                labelMessage.setText("Wrong username. Try again!");
            }

            if (SceneHelper.getPrimaryStageObject() == null) {
                sceneHelper.changeSceneToMainView();
                SceneHelper.getPrimaryStageObject().show();
            }
        }catch (Exception ex){
            System.out.println("Error while verifing login in login controller: "+ex.getMessage());
        }
    }

    public void cancel(ActionEvent actionEvent) {
        if (LoginHelper.getLoggedInUserId() == -1) {
            //close the programm when you abort the initial login screen
            SceneHelper.getPrimaryStageObject().close();
        }
        SceneHelper.getSecondaryStageObject().close();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loginHelper = new LoginHelper();
        sceneHelper = new SceneHelper();
    }


}
