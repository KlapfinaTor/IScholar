package at.fhj.ischolar.data;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * The Data Access Object for the IScholar Data.
 * Uses hibernate and the H2 database to store the data.
 */
public class DBDao {
    private final Level HIBERNATE_LOGLEVEL = Level.SEVERE;

    public DBDao() {
        java.util.logging.Logger.getLogger("org.hibernate").setLevel(HIBERNATE_LOGLEVEL);

        //Adds a default admin when no user is in the user table, PW:1234
        if (getAllUsers().size() == 0) {
            updateUser(new User("Default Admin", "admin", "$2b$10$h2ZTtVPHB8lmRUMak9WI9eqje9GaoTgEhecubZF4j992iAtyvhaeu", User.Role.Admin));
        }
    }

    /**
     * Gets a session from the session factory.
     *
     * @return A session
     * @throws HibernateException
     */
    public static Session getSession() throws HibernateException {
        //Default search path for config "hibernate.cfg.xml" is in resources
        SessionFactory sessionFactory = new Configuration().configure()
                .buildSessionFactory();
        return sessionFactory.openSession();
    }

    /**
     * Generic method to get all data in a table with the CriteriaBuilder
     *
     * @param type
     * @param session A session
     * @param <T>
     * @return
     */
    private static <T> List<T> loadAllData(Class<T> type, Session session) {
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<T> criteria = builder.createQuery(type);
        criteria.from(type);
        List<T> data = session.createQuery(criteria).getResultList();
        return data;
    }

    /**
     * Updates or creates the provided user.
     *
     * @param user User
     */
    public void updateUser(User user) {
        try (Session session = getSession()) {
            session.beginTransaction();
            session.saveOrUpdate(user);
            session.getTransaction().commit();
        } catch (Exception ex) {
            System.err.println("Error while updating user: " + ex.getMessage());
        }
    }

    /**
     * Gets all courses.
     *
     * @return
     */
    public ArrayList<Course> getAllCourses() {
        List<Course> allCourses = new ArrayList<Course>();

        try (Session session = getSession()) {
            session.beginTransaction();

            allCourses = loadAllData(Course.class, session);
            session.getTransaction().commit();
        } catch (Exception ex) {
            System.err.println("Error while getting all courses: " + ex.getMessage());
        }
        return (ArrayList<Course>) allCourses;
    }

    /**
     * Gets all the users with the role Admin or Assistant.
     *
     * @return A list of users
     */
    public ArrayList<User> getAllAdminAssistantUsers() {
        ArrayList<User> results = new ArrayList<User>();

        try (Session session = getSession()) {
            session.beginTransaction();

            String roleTypeAdmin = User.Role.Admin.toString();
            String roleTypeAssistant = User.Role.Assistant.toString();
            results.addAll(getSession().createQuery("from User where role=" + User.Role.valueOf(roleTypeAdmin).ordinal()).list());
            results.addAll(getSession().createQuery("from User where role=" + User.Role.valueOf(roleTypeAssistant).ordinal()).list());
            session.getTransaction().commit();
        } catch (Exception ex) {
            System.err.println("Error while getting all admin users: " + ex.getMessage());
        }
        return results;
    }

    /**
     * Gets all users.
     *
     * @return
     */
    public ArrayList<User> getAllUsers() {
        List<User> allUsers = new ArrayList<User>();

        try (Session session = getSession()) {
            session.beginTransaction();

            allUsers = loadAllData(User.class, session);
            session.getTransaction().commit();
        } catch (Exception ex) {
            System.err.println("Error while getting all users: " + ex.getMessage());
        }
        return (ArrayList<User>) allUsers;
    }

    /**
     * Gets user by login name.
     *
     * @param loginname
     * @return
     */
    public User getUserByLoginName(String loginname) {
        User user = null;
        try (Session session = getSession()) {
            session.beginTransaction();
            String searchValue = "%" + loginname + "%";
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<User> cr = cb.createQuery(User.class);
            Root<User> root = cr.from(User.class);
            cr.select(root).where(cb.like(root.get("loginName"), searchValue));

            Query<User> query = session.createQuery(cr);
            user = query.getSingleResult();

            session.getTransaction().commit();

        } catch (Exception ex) {
            System.out.println("Error while getting user by loginname: " + ex.getMessage());
        }
        return user;
    }

    /**
     * Gets all the rooms.
     *
     * @return a list with all rooms.
     */
    public ArrayList<Room> getAllRooms() {
        List<Room> allRooms = new ArrayList<Room>();

        try (Session session = getSession()) {
            session.beginTransaction();

            allRooms = loadAllData(Room.class, session);
            session.getTransaction().commit();
        } catch (Exception ex) {
            System.err.println("Error while getting all rooms: " + ex.getMessage());
        }
        return (ArrayList<Room>) allRooms;
    }

    /**
     * Updates or creates a courseschedule.
     *
     * @param csNew
     */
    public void updateCourseSchedule(CourseSchedule csNew) {
        try (Session session = getSession()) {
            session.beginTransaction();
            session.saveOrUpdate(csNew);
            session.getTransaction().commit();
        } catch (Exception ex) {
            System.err.println("Error while updating course schedule: " + ex.getMessage());
        }
    }

    /**
     * Gets all courseschedules.
     *
     * @return a list with all course schedules.
     */
    public ArrayList<CourseSchedule> getAllCourseSchedules() {
        List<CourseSchedule> allCS = new ArrayList<CourseSchedule>();

        try (Session session = getSession()) {
            session.beginTransaction();

            allCS = loadAllData(CourseSchedule.class, session);
            session.getTransaction().commit();
        } catch (Exception ex) {
            System.err.println("Error while getting all course schedules: " + ex.getMessage());
        }
        return (ArrayList<CourseSchedule>) allCS;
    }

    /**
     * Deletes a courseschedule.
     *
     * @param cs courseschedule to delete.
     */
    public void deleteCourseSchedule(CourseSchedule cs) {
        try (Session session = getSession()) {
            session.beginTransaction();
            session.delete(cs);
            session.getTransaction().commit();
        } catch (Exception ex) {
            System.err.println("Error while deleting course schedule: " + ex.getMessage());
        }
    }

    /**
     * gets a user by id
     *
     * @param id id of the user.
     * @return user.
     */
    public User getUserById(int id) {
        User user = null;
        try (Session session = getSession()) {
            session.beginTransaction();

            user = (User) session.get(User.class, id);
            session.getTransaction().commit();
        } catch (Exception ex) {
            System.out.println("Error while getting user by id: " + ex.getMessage());
        }
        return user;
    }

    /**
     * Updates or creates a course.
     *
     * @param course Course to update or create,
     */
    public void updateCourse(Course course) {
        try (Session session = getSession()) {
            session.beginTransaction();
            session.saveOrUpdate(course);
            session.getTransaction().commit();

        } catch (Exception ex) {
            System.err.println("Error while updating course: " + ex.getMessage());
        }
    }

    /**
     * Delete a course
     *
     * @param selectedCourse course to delete.
     */
    public void deleteCourse(Course selectedCourse) {
        try (Session session = getSession()) {
            session.beginTransaction();
            session.delete(selectedCourse);
            session.getTransaction().commit();
        } catch (Exception ex) {
            System.err.println("Error while deleting course: " + ex.getMessage());
        }
    }

    /**
     * Updates or creates a room.
     *
     * @param room Room to update or create.
     */
    public void updateRoom(Room room) {
        try (Session session = getSession()) {
            session.beginTransaction();
            session.saveOrUpdate(room);
            session.getTransaction().commit();
        } catch (Exception ex) {
            System.err.println("Error while updating room: " + ex.getMessage());
        }
    }

    /**
     * Delete a room.
     *
     * @param selectedRoom Room to delete.
     */
    public void deleteRoom(Room selectedRoom) {
        try (Session session = getSession()) {
            session.beginTransaction();
            session.delete(selectedRoom);
            session.getTransaction().commit();
        } catch (Exception ex) {
            System.err.println("Error while deleting room: " + ex.getMessage());
        }
    }

    /**
     * Delete a user.
     *
     * @param selectedUser User to delete.
     */
    public void deleteUser(User selectedUser) {
        try (Session session = getSession()) {
            session.beginTransaction();
            session.delete(selectedUser);
            session.getTransaction().commit();
        } catch (Exception ex) {
            System.err.println("Error while deleting user: " + ex.getMessage());
        }
    }

    /**
     * Gets alls courseschedules for a specific course.
     *
     * @param course Course
     * @return a list of courseschedules for the provided course.
     */
    public ArrayList<CourseSchedule> getCourseSchedulesForCourse(Course course) {
        List<CourseSchedule> results = new ArrayList<CourseSchedule>();

        try (Session session = getSession()) {
            session.beginTransaction();

            Criteria criteria = session.createCriteria(CourseSchedule.class);
            results = criteria.add(Restrictions.eq("course", course)).list();

            session.getTransaction().commit();
        } catch (Exception ex) {
            System.out.println("Error while getting course schedules for course: " + ex.getMessage());
        }
        return (ArrayList<CourseSchedule>) results;
    }
}

