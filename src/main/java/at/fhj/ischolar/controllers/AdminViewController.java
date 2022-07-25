package at.fhj.ischolar.controllers;

import at.fhj.ischolar.data.*;
import at.fhj.ischolar.helpers.LoginHelper;
import at.fhj.ischolar.helpers.SceneHelper;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller that manages the adim view. Provides controls to edit data for the IScholar.
 */
public class AdminViewController implements Initializable {
    @FXML
    public TableColumn<User.Role, String> tableUsersRoleCol1;
    @FXML
    public TableColumn<User, String> tableUsersNoteCol1;
    @FXML
    public TextField textUserLoginName;
    @FXML
    public TextField textUserPassword;
    @FXML
    public ChoiceBox<User.Role> selectUserRole;
    @FXML
    public PieChart pieChart;
    private SceneHelper sceneHelper = new SceneHelper();
    private LoginHelper loginHelper = new LoginHelper();
    @FXML
    private TableView<User> tableUsers;
    @FXML
    private TableColumn<User, String> tableUsersNameCol1;
    @FXML
    private TableColumn<User, String> tableUsersLoginNameCol1;
    @FXML
    private TableColumn<User, String> tableUsersPasswordCol1;
    @FXML
    private TextField textUserName;
    @FXML
    private Label labelUserInfo;
    @FXML
    private Label labelLoggedInUserName;
    @FXML
    private Label labelCourseInfo;
    @FXML
    private TextField textCourseName;
    @FXML
    private ChoiceBox<User> selectCourseUser;
    @FXML
    private TableView<Course> tableCourses;
    @FXML
    private TableColumn<Course, String> tableCoursesNameCol;
    @FXML
    private TableColumn<Course, String> tableCoursesUserCol;
    @FXML
    private Label labelRoomInfo;
    @FXML
    private TextField textRoomName;
    @FXML
    private TableView<Room> tableRooms;
    @FXML
    private TableColumn<Room, String> tableRoomsNameCol;

    private ObservableList<Course> courseData = FXCollections.observableArrayList();
    private ObservableList<Room> roomData = FXCollections.observableArrayList();
    private ObservableList<User> userData = FXCollections.observableArrayList();
    private ObservableList<User> userAdminAssistantData = FXCollections.observableArrayList();
    private ObservableList<CourseSchedule> courseSchedulesData = FXCollections.observableArrayList();
    private final DBDao db = new DBDao();

    /**
     * Loads the data for the course table.
     */
    private void loadCourseTableData() {
        try {
            courseData.setAll(db.getAllCourses());
            userAdminAssistantData.setAll(db.getAllAdminAssistantUsers());
            tableCourses.setEditable(true);
            tableCoursesNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
            tableCoursesUserCol.setCellValueFactory(new PropertyValueFactory<Course, String>("lecturer"));

            //To edit cell values for course name
            tableCoursesNameCol.setCellFactory(TextFieldTableCell.forTableColumn());
            tableCoursesNameCol.setOnEditCommit(
                    new EventHandler<CellEditEvent<Course, String>>() {
                        @Override
                        public void handle(CellEditEvent<Course, String> t) {
                            t.getTableView().getItems().get(t.getTablePosition().getRow()).setName(t.getNewValue());
                            db.updateCourse(t.getTableView().getItems().get(t.getTablePosition().getRow()));
                            reloadAllAdminViewData();
                        }
                    }
            );

            tableCourses.setItems(courseData);
            selectCourseUser.setItems(userAdminAssistantData);
        } catch (Exception ex) {
            System.err.println("Error while loading course table in adminController: " + ex.getMessage());
        }
    }

    /**
     * Loads the data for the room table.
     */
    private void loadRoomsTableData() {
        try {
            roomData.setAll(db.getAllRooms());
            tableRooms.setEditable(true);
            tableRoomsNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

            //To edit cell values for room name
            tableRoomsNameCol.setCellFactory(TextFieldTableCell.forTableColumn());
            tableRoomsNameCol.setOnEditCommit(
                    new EventHandler<CellEditEvent<Room, String>>() {
                        @Override
                        public void handle(CellEditEvent<Room, String> t) {
                            t.getTableView().getItems().get(t.getTablePosition().getRow()).setName(t.getNewValue());
                            db.updateRoom(t.getTableView().getItems().get(t.getTablePosition().getRow()));
                            reloadAllAdminViewData();
                        }
                    }
            );

            tableRooms.setItems(roomData);
        } catch (Exception ex) {
            System.err.println("Error while loading roomsdata table in adminController: " + ex.getMessage());
        }
    }

    /**
     * Loads the course schedule data
     */
    private void loadCourseScheduleData() {
        courseSchedulesData.setAll(db.getAllCourseSchedules());
    }

    /**
     * Loads the data for the users table an setups the handlers for editing the cell values.
     */
    private void loadUsersTableData() {
        try {
            userData.setAll(db.getAllUsers());
            tableUsers.setEditable(true);

            tableUsersNameCol1.setCellValueFactory(new PropertyValueFactory<>("name"));
            tableUsersLoginNameCol1.setCellValueFactory(new PropertyValueFactory<>("loginName"));
            tableUsersPasswordCol1.setCellValueFactory(new PropertyValueFactory<>("password"));
            tableUsersRoleCol1.setCellValueFactory(new PropertyValueFactory<>("role"));
            tableUsersNoteCol1.setCellValueFactory(new PropertyValueFactory<>("note"));

            //To edit cell values for user name
            tableUsersNameCol1.setCellFactory(TextFieldTableCell.forTableColumn());
            tableUsersNameCol1.setOnEditCommit(
                    new EventHandler<>() {
                        @Override
                        public void handle(CellEditEvent<User, String> t) {
                            t.getTableView().getItems().get(t.getTablePosition().getRow()).setName(t.getNewValue());
                            db.updateUser(t.getTableView().getItems().get(t.getTablePosition().getRow()));
                            reloadAllAdminViewData();
                        }
                    }
            );

            //To edit cell values for user loginname
            tableUsersLoginNameCol1.setCellFactory(TextFieldTableCell.forTableColumn());
            tableUsersLoginNameCol1.setOnEditCommit(
                    new EventHandler<>() {
                        @Override
                        public void handle(CellEditEvent<User, String> t) {
                            t.getTableView().getItems().get(t.getTablePosition().getRow()).setLoginName(t.getNewValue());
                            db.updateUser(t.getTableView().getItems().get(t.getTablePosition().getRow()));
                            reloadAllAdminViewData();
                        }
                    }
            );

            //To edit cell values for user password
            tableUsersPasswordCol1.setCellFactory(TextFieldTableCell.forTableColumn());
            tableUsersPasswordCol1.setOnEditCommit(
                    new EventHandler<>() {
                        @Override
                        public void handle(CellEditEvent<User, String> t) {
                            String pw = loginHelper.hashPW(t.getNewValue());
                            t.getTableView().getItems().get(t.getTablePosition().getRow()).setPassword(pw);
                            db.updateUser(t.getTableView().getItems().get(t.getTablePosition().getRow()));
                            reloadAllAdminViewData();
                        }
                    }
            );

            //To edit cell values for user notes
            tableUsersNoteCol1.setCellFactory(TextFieldTableCell.forTableColumn());
            tableUsersNoteCol1.setOnEditCommit(
                    new EventHandler<>() {
                        @Override
                        public void handle(CellEditEvent<User, String> t) {
                            t.getTableView().getItems().get(t.getTablePosition().getRow()).setNote(t.getNewValue());
                            db.updateUser(t.getTableView().getItems().get(t.getTablePosition().getRow()));
                            reloadAllAdminViewData();
                        }
                    }
            );

            tableUsers.setItems(userData);
            selectUserRole.setItems(FXCollections.observableArrayList(User.Role.values())); //sets data for the choiceBox
        } catch (Exception ex) {
            System.err.println("Error while loading userdata table in adminController: " + ex.getMessage());
        }

    }

    /**
     * Relods the current logged in user.
     */
    public void reloadLoggedInUser() {
        if (LoginHelper.getLoggedInUserId() != -1) {
            labelLoggedInUserName.setText(loginHelper.getCurrentLoggedInUser().getName());
        }
    }

    /**
     * Reloads all the admin view data.
     */
    public void reloadAllAdminViewData() {
        Platform.runLater(() -> {
            loadUsersTableData();
            reloadLoggedInUser();
            loadCourseTableData();
            loadRoomsTableData();
            loadCourseScheduleData();
            setupPieChart();
            System.out.println("Reload of all AdminView data complete!");
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        reloadAllAdminViewData();
        labelCourseInfo.setText("");
        labelRoomInfo.setText("");
        labelUserInfo.setText("");

        SceneHelper.getPrimaryStageObject().focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean onHidden, Boolean onShown) {
                System.out.println("Stage changed, reloading logged in user!");
                reloadLoggedInUser();
            }
        });
    }

    /**
     * Setups the piechart
     */
    private void setupPieChart() {
        ObservableList<PieChart.Data> pieChartData =
                FXCollections.observableArrayList(
                        new PieChart.Data("Users", userData.size()),
                        new PieChart.Data("Courses", courseData.size()),
                        new PieChart.Data("Rooms", roomData.size()),
                        new PieChart.Data("CS", courseSchedulesData.size())
                );

        pieChart.setData(pieChartData);
        pieChartData.forEach(data ->
                data.nameProperty().bind(
                        Bindings.concat(
                                data.pieValueProperty(), " ", data.getName()
                        )
                )
        );
    }

    /**
     * Adds a course.
     *
     * @param actionEvent
     */
    public void addCourse(ActionEvent actionEvent) {
        if (textCourseName.getText().isBlank()) {
            labelCourseInfo.setText("No course name given!");
        } else {
            try {
                db.updateCourse(new Course(textCourseName.getText(), selectCourseUser.getValue()));
                labelCourseInfo.setText("");
                textCourseName.setText("");
                reloadAllAdminViewData();
            } catch (Exception ex) {
                System.err.println("Error while adding course in adminController: " + ex.getMessage());
            }
        }
    }

    /**
     * Removes the selected course in the tableCourses
     *
     * @param actionEvent
     */
    public void removeSelectedCourse(ActionEvent actionEvent) {
        Course selectedCourse = tableCourses.getSelectionModel().getSelectedItem();
        try {
            db.deleteCourse(selectedCourse);
            reloadAllAdminViewData();
        } catch (Exception ex) {
            System.err.println("Error while removing course in adminController: " + ex.getMessage());
        }
    }

    public void changeSceneToOverview(ActionEvent actionEvent) {
        sceneHelper.changeSceneToMainView();
    }

    public void changeSceneToPlan(ActionEvent actionEvent) {
        sceneHelper.changeSceneToPlanView();
    }

    public void changeSceneToAdmin(ActionEvent actionEvent) {
        // sceneHelper.changeSceneToAdminView();
    }

    public void changeUser(ActionEvent actionEvent) {
        sceneHelper.showLoginWindow();
    }

    /**
     * Removes the selected Room from the tableRooms.
     *
     * @param actionEvent
     */
    public void removeSelectedRoom(ActionEvent actionEvent) {
        try {
            Room selectedRoom = tableRooms.getSelectionModel().getSelectedItem();
            db.deleteRoom(selectedRoom);
            reloadAllAdminViewData();
        } catch (Exception ex) {
            System.err.println("Error while deleting room in adminController: " + ex.getMessage());
        }
    }

    /**
     * Adds a room
     *
     * @param actionEvent
     */
    public void addRoom(ActionEvent actionEvent) {
        if (textRoomName.getText().isBlank()) {
            labelRoomInfo.setText("No room name given!");
        } else {
            try {
                db.updateRoom(new Room(textRoomName.getText()));
                labelRoomInfo.setText("");
                textRoomName.setText("");
                reloadAllAdminViewData();
            } catch (Exception ex) {
                System.err.println("Error while adding room in adminController: " + ex.getMessage());
            }
        }
    }

    /**
     * Adds a user.
     *
     * @param actionEvent
     */
    public void addUser(ActionEvent actionEvent) {
        if (textUserName.getText().isBlank() || textUserLoginName.getText().isBlank() || textUserPassword.getText().isBlank()) {
            labelUserInfo.setText("Not all values provided!");
        } else {
            try {
                String hashedPW = loginHelper.hashPW(textUserPassword.getText());
                db.updateUser(new User(textUserName.getText(), textUserLoginName.getText(), hashedPW, selectUserRole.getValue()));
                labelUserInfo.setText("");
                textUserName.setText("");
                textUserLoginName.setText("");
                textUserPassword.setText("");
                reloadAllAdminViewData();
            } catch (Exception ex) {
                System.err.println("Error while adding user in adminController: " + ex.getMessage());
            }
        }
    }

    /**
     * Removes the selected user from the tableUsers.
     *
     * @param actionEvent
     */
    public void removeSelectedUser(ActionEvent actionEvent) {
        try {
            User selectedUser = tableUsers.getSelectionModel().getSelectedItem();
            db.deleteUser(selectedUser);
            reloadAllAdminViewData();
        } catch (Exception ex) {
            System.err.println("Error while deleting user in adminController: " + ex.getMessage());
        }
    }
}
