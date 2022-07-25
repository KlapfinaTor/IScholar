package at.fhj.ischolar.controllers;

import at.fhj.ischolar.data.*;
import at.fhj.ischolar.helpers.LoginHelper;
import at.fhj.ischolar.helpers.SceneHelper;
import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * Controller that manages the PlanView. Displays a control to create new course schedules.
 */
public class PlanViewController implements Initializable {
    private final String STYLE_BORDER_RED = "-fx-text-box-border: #B22222; -fx-focus-color: #B22222;";
    @FXML
    public TextField textStartTime;
    @FXML
    public TextField textEndTime;
    @FXML
    public TextArea areaNote;
    @FXML
    public Button btnCreateSchedule;
    @FXML
    public DatePicker datePickerSchedule;
    @FXML
    public Label labelWarningSchedule;
    @FXML
    public ChoiceBox<Course> choiceCourse;
    @FXML
    public ChoiceBox<Room> choiceRoom;
    @FXML
    public TableView<CourseSchedule> tableScheduledCourses;
    @FXML
    public TableColumn<Course, String> tableCSCourseName;
    @FXML
    public TableColumn<LocalDate, String> tableCSDay;
    @FXML
    public TableColumn<LocalTime, String> tableCSStartTime;
    @FXML
    public TableColumn<LocalTime, String> tableCSEndTime;
    @FXML
    public TableColumn<Room, String> tableCSRoomName;
    @FXML
    public TableColumn tableCSLecturerNote;
    @FXML
    public GridPane gridPaneCalendar;
    @FXML
    public Button btnRemoveCS;
    @FXML
    public Label labelLecturerName;
    @FXML
    public Button btnToAdmin;
    @FXML
    public Button btnToPlan;
    @FXML
    public Button btnToOverview;
    private SceneHelper sceneHelper = new SceneHelper();
    private LoginHelper loginHelper = new LoginHelper();
    private CalendarView calendarView;
    private Calendar myCourses;
    @FXML
    private Label labelLoggedInUserName;
    private ObservableList<Course> courseData = FXCollections.observableArrayList();
    private ObservableList<Room> roomData = FXCollections.observableArrayList();
    private ObservableList<User> userAdminAssistantData = FXCollections.observableArrayList();
    private ObservableList<CourseSchedule> courseScheduleData = FXCollections.observableArrayList();
    private DBDao db = new DBDao();

    /**
     * Loads the data for the tables and choice boxes.
     */
    private void loadData() {
        try {
            courseData.setAll(db.getAllCourses());
            courseScheduleData.setAll(db.getAllCourseSchedules());
            roomData.setAll(db.getAllRooms());
            userAdminAssistantData.setAll(db.getAllAdminAssistantUsers());

            choiceCourse.setItems(courseData);
            choiceRoom.setItems(roomData);

            tableScheduledCourses.setEditable(false);
            tableScheduledCourses.setItems(courseScheduleData);
        } catch (Exception ex) {
            System.err.println("Error while loading plan view data: " + ex.getMessage());
        }
    }

    /**
     * Setups the course schedule table.
     */
    private void setupTable() {
        tableCSCourseName.setCellValueFactory(new PropertyValueFactory<>("course"));
        tableCSDay.setCellValueFactory(new PropertyValueFactory<>("day"));
        tableCSStartTime.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        tableCSEndTime.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        tableCSRoomName.setCellValueFactory(new PropertyValueFactory<>("room"));
        // tableCSLecturerNote.setCellValueFactory(new PropertyValueFactory<Course, String>("lecturer")); //TODO get note from course->lecturer
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadData();
        setupTable();
        reloadLoggedInUser();
        loadCalendar();
        textStartTime.setText("12:00");
        textEndTime.setText("13:00");
        labelWarningSchedule.setText("");
        //areaNote.setEditable(false);
        areaNote.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(final ObservableValue<? extends String> observable, final String oldValue, final String newValue) {
                User lecturerOfCurrentSelectedCourse = choiceCourse.getValue().getLecturer();
                try {
                    lecturerOfCurrentSelectedCourse.setNote(areaNote.getText());
                    db.updateUser(lecturerOfCurrentSelectedCourse);
                } catch (Exception ex) {
                    System.out.println("Error while updating user note in plan view: " + ex.getMessage());
                }
            }
        });
    }

    /**
     * Reloads all the plan view data.
     */
    public void reloadAllPlanViewData() {
        Platform.runLater(() -> {
            loadData();
            setupTable();
            reloadLoggedInUser();
            loadCalendar();
            labelWarningSchedule.setText("");
            System.out.println("Reload of all PlanView data complete!");
        });
    }

    /**
     * Checks if the Time input is valid. Example: 08:10(HH:mm)
     *
     * @param enteredTime A string with the entered time
     * @return True if time is valid.
     */
    public boolean isTimeInputValid(String enteredTime) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("H:mm");
            LocalTime startTime = LocalTime.parse(textStartTime.getText(), formatter);

        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    /**
     * Checks if the time is within the bounds 8:00-23:00
     *
     * @param time The time to check
     * @return True if within bounds
     */
    private boolean isTimeWithinBounds(LocalTime time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("H:mm");
        LocalTime min = LocalTime.parse("8:00", formatter);
        LocalTime max = LocalTime.parse("23:00", formatter);

        return !time.isAfter(max) && !time.isBefore(min);
    }

    /**
     * Resets the course plan view style
     */
    private void resetCoursePlanViewStyle() {
        textStartTime.setStyle("");
        textEndTime.setStyle("");
    }


    /**
     * Creates a new schedule. Calls various plausibility checks.
     *
     * @param actionEvent Button event.
     */
    public void createSchedule(ActionEvent actionEvent) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("H:mm");
        resetCoursePlanViewStyle();

        if (isTimeInputValid(textStartTime.getText()) && isTimeInputValid(textEndTime.getText())) {
            LocalTime startTime = LocalTime.parse(textStartTime.getText(), formatter);
            LocalTime endTime = LocalTime.parse(textEndTime.getText(), formatter);

            if (isTimeWithinBounds(startTime) && isTimeWithinBounds(endTime) && startTime.isBefore(endTime)) {
                if (datePickerSchedule.getValue() != null && choiceCourse.getValue() != null && choiceRoom.getValue() != null) {
                    if (isRoomAvailable(choiceRoom.getValue(), datePickerSchedule.getValue(), startTime, endTime)) {
                        CourseSchedule csNew = new CourseSchedule(startTime, endTime, datePickerSchedule.getValue(), choiceCourse.getValue(), choiceRoom.getValue());
                        db.updateCourseSchedule(csNew);

                        User lecturerOfCurrentSelectedCourse = choiceCourse.getValue().getLecturer();
                        lecturerOfCurrentSelectedCourse.setNote(areaNote.getText());
                        db.updateUser(lecturerOfCurrentSelectedCourse);

                        reloadAllPlanViewData();
                    } else {
                        labelWarningSchedule.setText("Room is already booked!");
                    }
                } else {
                    labelWarningSchedule.setText("Missing required information!");
                }
            } else {
                labelWarningSchedule.setText("Time must be between 8:00-23:00");
                if (!isTimeWithinBounds(startTime)) {
                    textStartTime.setStyle(STYLE_BORDER_RED);
                }
                if (!isTimeWithinBounds(endTime)) {
                    textEndTime.setStyle(STYLE_BORDER_RED);
                }
            }
        } else {
            labelWarningSchedule.setText("No valid time input entered!!");
        }
    }

    /**
     * Checks if the room is available at the given time (day, start, endtime)
     *
     * @param room      Room
     * @param day       Day
     * @param startTime The start time
     * @param endTime   The end time
     * @return true if room is available at the provided time.
     */
    private boolean isRoomAvailable(Room room, LocalDate day, LocalTime startTime, LocalTime endTime) {
        ArrayList<CourseSchedule> csList = db.getAllCourseSchedules();
        boolean available = true;

        for (CourseSchedule cs : csList) {
            if (cs.getDay().isEqual(day)) {
                if (cs.getRoom().equals(room)) {

                    //checks if the start and or endtime is between a already schedules course
                    if (startTime.isAfter(cs.getStartTime()) && startTime.isBefore(cs.getEndTime()) ||
                            endTime.isBefore(cs.getEndTime()) && endTime.isAfter(cs.getStartTime()) ||
                            startTime.isBefore(cs.getStartTime()) && endTime.isAfter(cs.getEndTime()) ||
                            startTime.equals(cs.getStartTime()) || endTime.equals(cs.getEndTime()) ||
                            startTime.equals(endTime) || endTime.equals(startTime)) {
                        available = false;
                        break;
                    }
                }
            }
        }
        return available;
    }

    /**
     * Adds calendar entries for every scheduled course.
     */
    private void addCalendarEntries() {
        try {
            ArrayList<CourseSchedule> csList = db.getAllCourseSchedules();
            for (CourseSchedule cs : csList) {
                String title = String.format("%s, %s, [%s]", cs.getCourse().getName(), cs.getCourse().getLecturer().getName(), cs.getRoom().getName());
                Entry<String> entry = new Entry<>(title);
                //entry.setInterval(cs.getDay());
                entry.changeStartDate(cs.getDay());
                entry.changeEndDate(cs.getDay());
                entry.changeStartTime(cs.getStartTime());
                entry.changeEndTime(cs.getEndTime());

                myCourses.addEntry(entry);
            }
        } catch (Exception ex) {
            System.err.println("Error while adding calendar entries!" + ex.getMessage());
        }
    }

    /**
     * Loads the calendarFX calendar.
     */
    private void loadCalendar() {
        calendarView = new CalendarView();
        myCourses = new Calendar("My Courses");
        CalendarSource myCalendarSource = new CalendarSource("Source");
        myCourses.setReadOnly(true);
        myCourses.setStyle(Calendar.Style.STYLE1);
        calendarView.setShowSourceTray(false);
        calendarView.setShowAddCalendarButton(false);
        calendarView.setEntryFactory(param -> null); //disables the creation of entries

        addCalendarEntries();

        myCalendarSource.getCalendars().addAll(myCourses);
        calendarView.getCalendarSources().addAll(myCalendarSource);
        calendarView.setRequestedTime(LocalTime.now());

        Thread updateTimeThread = new Thread("Calendar: Update Time Thread") {
            @Override
            public void run() {
                while (true) {
                    Platform.runLater(() -> {
                        calendarView.setToday(LocalDate.now());
                        calendarView.setTime(LocalTime.now());
                    });
                    try {
                        // update every 10 seconds
                        sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        updateTimeThread.setPriority(Thread.MIN_PRIORITY);
        updateTimeThread.setDaemon(true);
        updateTimeThread.start();

        calendarView.showDayPage();
        gridPaneCalendar.getChildren().add(calendarView);
    }

    /**
     * Reloads the logged in User ans sets the allowed Menu items accordingly.
     */
    public void reloadLoggedInUser() {
        if (LoginHelper.getLoggedInUserId() != -1) {
            labelLoggedInUserName.setText(loginHelper.getCurrentLoggedInUser().getName());
        }

        if (loginHelper.getCurrentLoggedInUser().getRole().equals(User.Role.Student)) {
            btnToAdmin.setVisible(false);
            btnToPlan.setVisible(false);
        }
        if (loginHelper.getCurrentLoggedInUser().getRole().equals(User.Role.Assistant)) {
            btnToAdmin.setVisible(false);
            btnToPlan.setVisible(true);
        }
        if (loginHelper.getCurrentLoggedInUser().getRole().equals(User.Role.Admin)) {
            btnToAdmin.setVisible(true);
            btnToPlan.setVisible(true);
        }
    }


    public void changeUser(ActionEvent actionEvent) {
        sceneHelper.showLoginWindow();
    }

    public void changeSceneToOverview(ActionEvent actionEvent) {
        sceneHelper.changeSceneToMainView();
    }

    public void changeSceneToPlan(ActionEvent actionEvent) {
        //sceneHelper.changeSceneToPlanView();
    }

    public void changeSceneToAdmin(ActionEvent actionEvent) {
        if (loginHelper.getCurrentLoggedInUser().getRole().equals(User.Role.Admin)) {
            sceneHelper.changeSceneToAdminView();
        }
    }

    /**
     * Sets the he lecturer notes area text according to the selected course.
     *
     * @param actionEvent Button event.
     */
    public void choiceCourseChanged(ActionEvent actionEvent) {
        labelLecturerName.setText(choiceCourse.getValue().getLecturer().getName());
        areaNote.setText(choiceCourse.getValue().getLecturer().getNote());
    }

    /**
     * shows the current selected day in the calendar view
     *
     * @param actionEvent
     */
    public void dayPickerChanged(ActionEvent actionEvent) {
        calendarView.showDate(datePickerSchedule.getValue());
    }

    /**
     * Removes the current selected schedule in the tableScheduledCourses.
     *
     * @param actionEvent
     */
    public void removeSelectedSchedule(ActionEvent actionEvent) {
        CourseSchedule selectedCS = tableScheduledCourses.getSelectionModel().getSelectedItem();
        db.deleteCourseSchedule(selectedCS);
        reloadAllPlanViewData();
    }
}
