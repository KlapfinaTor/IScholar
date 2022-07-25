package at.fhj.ischolar.data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue
    private int id;

    @Column(name = "FIRST_NAME")
    private String name;

    @Column(name = "LOGIN_NAME")
    private String loginName;

    @Column(name = "PASSWORD")
    private String password;

    @Column(name = "ROLE")
    private Role role;

    @Column(name = "NOTE")
    private String note;

    @OneToMany(mappedBy = "lecturer")
    private List<Course> lecturedCourses;

    @ManyToMany(mappedBy = "enrolledUsers", fetch = FetchType.EAGER)
    private List<Course> enrolledCourses;


    public User(String name, String loginName, String password, Role role) {
        this.name = name;
        this.loginName = loginName;
        this.password = password;
        this.role = role;
        this.enrolledCourses = new ArrayList<>();
    }

    public User(String name, String loginName, String password, Role role, String note) {
        this.name = name;
        this.loginName = loginName;
        this.password = password;
        this.role = role;
        this.enrolledCourses = new ArrayList<>();
        this.note = note;
    }

    public User() {

    }

    public void addCourse(Course course) {
        enrolledCourses.add(course);
    }

    public void removeCourse(Course course) {
        enrolledCourses.remove(course);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getPassword() {
        return password;
    }
    public String getCensoredPassword() {
        return "*******";
    }


    public void setPassword(String password) {
        this.password = password;
    }

    @Enumerated(EnumType.STRING)
    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public List<Course> getAllEnrolledCourses() {
        return enrolledCourses;
    }

    public enum Role {
        Admin,
        Assistant,
        Student
    }

    //This is used by the choicebox
    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
