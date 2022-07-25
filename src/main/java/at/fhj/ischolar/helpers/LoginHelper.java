package at.fhj.ischolar.helpers;

import at.fhj.ischolar.data.DBDao;
import at.fhj.ischolar.data.User;
import com.password4j.Hash;
import com.password4j.Password;

/**
 * Helper class that helps to manage the logged in user and provides password hashing functionality.
 */
public class LoginHelper {
    private static DBDao db = new DBDao();
    private static int loggedInUserId = -1;

    public static int getLoggedInUserId() {
        return loggedInUserId;
    }

    public static void setLoggedInUserId(int loggedInUser) {
        LoginHelper.loggedInUserId = loggedInUser;
    }

    /**
     * Hashes the plaintext password
     *
     * @param plaintextPW Plaintext password
     * @return Hashed password
     */
    public String hashPW(String plaintextPW) {
        Hash hash = Password.hash(plaintextPW).withBCrypt();
        return hash.getResult();
    }

    /**
     * Checks if the entered password is matching with the hashed password
     *
     * @param rawPW    The password in plaintext
     * @param pwHashed A password Hash
     * @return Return true is PW is matching
     */
    public boolean isPWMatching(String rawPW, String pwHashed) {
        return Password.check(rawPW, pwHashed).withBCrypt();
    }

    /**
     * Gets the current logged in user
     *
     * @return Logged in User
     */
    public User getCurrentLoggedInUser() {
        User result = null;
        try {
            result = db.getUserById(LoginHelper.getLoggedInUserId());
        } catch (Exception ex) {
            System.out.println("Error while getting current logged in user in mainviewcontroller: " + ex.getMessage());
        }
        return result;
    }
}
