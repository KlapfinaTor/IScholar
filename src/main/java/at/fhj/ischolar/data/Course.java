package at.fhj.ischolar.data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Course {
    @Column(name = "COURSE_NAME")
    String name;

    @Id
    @GeneratedValue
    private int id;

    @ManyToOne(optional = false, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "USER_ID", referencedColumnName = "ID")
    private User lecturer;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "USERS_COURSES",
            joinColumns = @JoinColumn(name = "COURSE_ID", referencedColumnName = "ID"),
            inverseJoinColumns = @JoinColumn(name = "USER_ID", referencedColumnName = "ID")
    )
    private List<User> enrolledUsers = new ArrayList<>();

    public Course(String name) {
        this.name = name;
    }


    public Course(String name, User lecturer) {
        this.name = name;
        this.lecturer = lecturer;
    }

    public Course(int id, String name, User lecturer) {
        this.id = id;
        this.name = name;
        this.lecturer = lecturer;
    }

    public Course() {
    }

    public List<User> getEnrolledUsers() {
        return enrolledUsers;
    }

    public void addEnrolledUser(User newUser) {
        enrolledUsers.add(newUser);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User getLecturer() {
        return lecturer;
    }

    public void setLecturer(User lecturer) {
        this.lecturer = lecturer;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    //This is used by the choicebox
    @Override
    public String toString() {
        return getName();
    }

    public void removeEnrolledUser(User user) {
        enrolledUsers.remove(user);
    }
}
