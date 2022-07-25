package at.fhj.ischolar.data;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "course_schedule")
public class CourseSchedule {
    @Id
    @GeneratedValue
    private int id;

    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDate day;

    @ManyToOne(optional = false, cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    @JoinColumn(name = "COURSE_ID", referencedColumnName = "ID")
    private Course course;

    @ManyToOne(optional = false, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "ROOM_ID", referencedColumnName = "ID")
    private Room room;

    public CourseSchedule(LocalTime startTime, LocalTime endTime, LocalDate day, Course course, Room room) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.day = day;
        this.course = course;
        this.room = room;
    }

    public CourseSchedule() { }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public LocalDate getDay() {
        return day;
    }

    public void setDay(LocalDate day) {
        this.day = day;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }
}
