package at.fhj.ischolar.controllers;

import at.fhj.ischolar.data.Course;
import at.fhj.ischolar.data.CourseSchedule;
import at.fhj.ischolar.data.DBDao;
import at.fhj.ischolar.data.User;
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
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller that manages the MainView. The MainView displays a calendar with the courses the current logged in user is enrolled.
 */
public class MainViewController implements Initializable {
    private static DBDao db = new DBDao();
    @FXML
    public TableView<Course> tableEnrolledCourses;
    @FXML
    public ComboBox<Course> choiceEnrollCourse;
    @FXML
    public TableColumn<Course, String> tableColumnCourseName;
    @FXML
    public Label labelWarningEnroll;
    @FXML
    public Button btnToAdmin;
    @FXML
    public Button btnToPlan;
    @FXML
    private GridPane gridPaneCalendar;
    @FXML
    private Label labelLoggedInUserName;
    private SceneHelper sceneHelper = new SceneHelper();
    private LoginHelper loginHelper = new LoginHelper();
    private ObservableList<Course> courseData = FXCollections.observableArrayList();
    private ObservableList<Course> enrolledCourseData = FXCollections.observableArrayList();

    private CalendarView calendarView;
    private Calendar myCourses;

    /**
     * Loads the fxCalendar.
     */
    private void loadCalendar() {
        try{
            calendarView = new CalendarView();
            myCourses = new Calendar("My Courses");
            CalendarSource myCalendarSource = new CalendarSource("Source");
            myCourses.setReadOnly(true);
            myCourses.setStyle(Calendar.Style.STYLE1);
            calendarView.setShowSourceTray(false);
            calendarView.setShowAddCalendarButton(false);
            calendarView.setEntryFactory(param -> null); //disables the creation of entries
            //calendarView.

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

            calendarView.showWeekPage();
            gridPaneCalendar.getChildren().add(calendarView);
        }catch (Exception ex){
            System.err.println("Error while creating calendarfx caelender: "+ex.getMessage());
        }
    }

    /**
     * Creates seperate calendar entries for each course schedule which the user is enrolled.
     */
    private void addCalendarEntries() {
        try {
            ArrayList<CourseSchedule> csList = getAllEnrolledCoursesByUser(db.getUserById(loginHelper.getCurrentLoggedInUser().getId()));

            for (CourseSchedule cs : csList) {
                String title = String.format("%s, %s, [%s]", cs.getCourse().getName(), cs.getCourse().getLecturer().getName(), cs.getRoom().getName());
                Entry<String> entry = new Entry<>(title);
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
     * Gets all the Course Schedules for the enrolled courses from a user
     * @param user The user
     * @return A list of course schedules which the user is enrolled.
     */
    private ArrayList<CourseSchedule> getAllEnrolledCoursesByUser(User user) {
        ArrayList<CourseSchedule> enrolledCourses = new ArrayList<CourseSchedule>();
        try {
            List<Course> courses = user.getAllEnrolledCourses();
            ArrayList<CourseSchedule> allCS = db.getAllCourseSchedules();

            for (CourseSchedule cs : allCS) {
                for (Course course : courses) {
                    if (cs.getCourse().getId() == course.getId()) {
                        enrolledCourses.add(cs);
                    }
                }
            }
        } catch (Exception ex) {
            System.err.println("Error while getting all enrolled courses in mainview controller: " + ex.getMessage());
        }
        return enrolledCourses;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (loginHelper.getCurrentLoggedInUser() != null) {
            reloadAllMainViewData();
        }
        SceneHelper.getPrimaryStageObject().focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean onHidden, Boolean onShown) {
                reloadLoggedInUser();
                reloadAllMainViewData();
            }
        });
    }

    /**
     * Reloads all the mainview data.
     */
    public void reloadAllMainViewData() {
        if(loginHelper.getCurrentLoggedInUser() != null){
            Platform.runLater(() -> {
                reloadLoggedInUser();
                loadData();
                setupTable();
                loadCalendar();
                System.out.println("Reload of all MainView data complete!");
            });
        }
    }

    /**
     * Setups the enrolled Courses Table
     */
    public void setupTable() {
        tableColumnCourseName.setCellValueFactory(new PropertyValueFactory<>("name"));
    }

    /**
     * Loads all the needed data for the course table.
     */
    public void loadData() {
        try {
            courseData.setAll(db.getAllCourses());
            choiceEnrollCourse.setItems(courseData);
            enrolledCourseData.setAll(db.getUserById(loginHelper.getCurrentLoggedInUser().getId()).getAllEnrolledCourses());

            tableEnrolledCourses.setEditable(false);
            tableEnrolledCourses.setItems(enrolledCourseData);
        } catch (Exception ex) {
            System.err.println("Error while loading main view data: " + ex.getMessage());
        }
    }

    /**
     * Reloads the logged in User ans sets the allowed Menu items accordingly.
     */
    public void reloadLoggedInUser() {
        if (LoginHelper.getLoggedInUserId() != -1 || loginHelper.getCurrentLoggedInUser() != null) {
            labelLoggedInUserName.setText(loginHelper.getCurrentLoggedInUser().getName());

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
    }

    /**
     * Shows the login window.
     * @param actionEvent
     */
    public void changeUser(ActionEvent actionEvent) {
        sceneHelper.showLoginWindow();
    }

    public void changeSceneToOverview(ActionEvent actionEvent) {
        // sceneHelper.changeSceneToMainView();
    }

    public void changeSceneToPlan(ActionEvent actionEvent) {
        if (loginHelper.getCurrentLoggedInUser().getRole().equals(User.Role.Admin) || loginHelper.getCurrentLoggedInUser().getRole().equals(User.Role.Assistant)) {
            sceneHelper.changeSceneToPlanView();
        }
    }

    public void changeSceneToAdmin(ActionEvent actionEvent) {
        if (loginHelper.getCurrentLoggedInUser().getRole().equals(User.Role.Admin)) {
            sceneHelper.changeSceneToAdminView();
        }
    }

    /**
     * Checks if the current logged in user is already enrolled to a course
     * @param course The course that gets checked
     * @return True if already enrolled.
     */
    private boolean isUserEnrolledToCourse(Course course) {
        User user = db.getUserById(loginHelper.getCurrentLoggedInUser().getId());
        return (course.getEnrolledUsers().contains(user));
    }

    /**
     * Trys to enroll into a course. Checks if
     * @param actionEvent Event from the enroll button
     */
    public void enrollIntoCourse(ActionEvent actionEvent) {
        try {
            if (loginHelper.getCurrentLoggedInUser() != null && choiceEnrollCourse.getValue() != null) {
                if (isUserEnrolledToCourse(choiceEnrollCourse.getValue())) {
                    labelWarningEnroll.setText("Already enrolled into course!");
                } else {
                    Course course = choiceEnrollCourse.getValue();
                    if (isCourseNotOverlappingWithOtherCourses(course)) {
                        User user = loginHelper.getCurrentLoggedInUser();
                        course.addEnrolledUser(user);
                        user.addCourse(course);

                        db.updateCourse(course);
                        db.updateUser(user);
                        reloadAllMainViewData();
                        labelWarningEnroll.setText("");
                    }
                }
            }
        } catch (Exception ex) {
            System.err.println("Error while enrolling into course: " + ex.getMessage());
        }
    }

    /**
     * Checks if a course is overlapping with the already scheduled courses
     * @param course The course that gets checked
     * @return True if not overlapping
     */
    private boolean isCourseNotOverlappingWithOtherCourses(Course course) {
        boolean available = true;
        try{
            ArrayList<CourseSchedule> allEnrolledCourses = getAllEnrolledCoursesByUser(loginHelper.getCurrentLoggedInUser());
            ArrayList<CourseSchedule> schedulesForNewCourse = db.getCourseSchedulesForCourse(course);

            for (CourseSchedule csFromAll : allEnrolledCourses) {
                for (CourseSchedule csNew : schedulesForNewCourse) {
                    if (csFromAll.getDay().equals(csNew.getDay())) {
                        if (csNew.getStartTime().isAfter(csFromAll.getStartTime()) && csNew.getStartTime().isBefore(csFromAll.getEndTime()) ||
                                csNew.getEndTime().isBefore(csFromAll.getEndTime()) && csNew.getEndTime().isAfter(csFromAll.getStartTime()) ||
                                csNew.getStartTime().isBefore(csFromAll.getStartTime()) && csNew.getEndTime().isAfter(csFromAll.getEndTime()) ||
                                csNew.getStartTime().equals(csFromAll.getStartTime()) || csNew.getEndTime().equals(csFromAll.getEndTime()) ||
                                csNew.getStartTime().equals(csNew.getEndTime()) || csNew.getEndTime().equals(csNew.getStartTime())) {
                            available = false;
                            labelWarningEnroll.setText(String.format("Course conflict with %s \non %s at %s-%s",
                                    csFromAll.getCourse().getName(), csFromAll.getDay(), csFromAll.getStartTime(), csFromAll.getEndTime()));
                            break;
                        }
                    }
                }
            }
        }catch (Exception ex){
            System.err.println("Error while checking for course overlapping: "+ex.getMessage());
        }
        return available;
    }

    /**
     * Try to opt out from the current selected course in the enrolled course table.
     * @param actionEvent Event from the opt out button
     */
    public void optOutFromSelectedCourse(ActionEvent actionEvent) {
        try {
            if (loginHelper.getCurrentLoggedInUser() != null && tableEnrolledCourses.getSelectionModel().getSelectedItem() != null) {
                User user = loginHelper.getCurrentLoggedInUser();
                Course selectedCourse = tableEnrolledCourses.getSelectionModel().getSelectedItem();
                selectedCourse.removeEnrolledUser(user);
                user.removeCourse(selectedCourse);

                db.updateCourse(selectedCourse);
                db.updateUser(user);
                reloadAllMainViewData();
            }
        } catch (Exception ex) {
            System.out.println("Error while enrolling into course: " + ex.getMessage());
        }
    }
}

