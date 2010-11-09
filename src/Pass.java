// Created by: Douglas Gardiner
// Creation Date: Fri Apr 03 17:07:31 CDT 2009
// Update Date: Sun Oct 31 02:35:06 CDT 2010
//

/**
 * User information storage object.  Used to storage user name and password
 * @author Douglas Gardiner
 */

public class Pass {

    private static String password;
    private static String username;
    private static final boolean debug = false;

    /**
     * Used for updating user information boolean argument is to decide whether 
     * it is username or password.  True for password and False for username.
     * @param newInfo username / password based on boolean parameter
     * @param decider boolean true - username, false - password
     */

    public static void setInfo(final String newInfo, final boolean decider) {

        if (decider) {
            username = newInfo;
        } else {
            password = newInfo;
        }
        if (debug) {
            System.out.println("set user-" + username + "\nset pass-" 
            + password);
        }
    }
    /**
     * Retrieves username or password based on boolean argument.  True 
     * retrieves username, false returns password.
     * @param decider boolean toggle for username or password.
     * @return String password or username based on boolean sent as argument.
     */

    public static String getInfo(final boolean decider) {

        if (debug) {
            System.out.println("get user-" + username + "\nget pass-" 
            + password);
        }
        if (decider) {
            return username;
        } else {
            return password;
        }
    }
}
