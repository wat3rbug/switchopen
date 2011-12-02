// Created by: Douglas Gardiner
// Creation Date: Fri Apr 03 17:07:31 CDT 2009
// Update Date: Fri Nov 12 20:01:56 CST 2010
//
/**
 * User information storage object.  Used to storage user name and password
 * @author Douglas Gardiner
 */

public class Pass {

	// attributes
	
    private static String password;
    private static String username;
    
	// methods
	
	/**
     * Retrieves username or password based on boolean argument.  True 
     * retrieves username, false returns password.
     * @param trueForUserName boolean toggle for username or password.
     * @return String password or username based on boolean sent as argument.
     */

    public static String getInfo(final boolean trueForUserName) {

        if (trueForUserName) {
            return username;
        } else {
            return password;
        }
    }
    /**
     * Used for updating user information boolean argument is to decide whether 
     * it is username or password.  True for password and False for username.
     * @param newInfo username / password based on boolean parameter
     * @param trueForUserName boolean true - username, false - password
     */

    public static void setInfo(final String newInfo, final boolean trueForUserName) {

        if (trueForUserName) {
            username = newInfo;
        } else {
            password = newInfo;
        }
    }
    
}
