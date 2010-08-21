// Created by: Douglas Gardiner
// Creation Date: Fri Apr 03 17:07:31 CDT 2009
// Update Date: Sat Nov 22 18:05:23 CST 2008
//

/* Password and user name data class.
*/

public class Pass {

    private static String password;
    private static String username;
    private static final boolean debug = false;

    public static void setPassword(String newPassword, boolean decider) {

	if (decider) {
	    username = newPassword;
	} else {
	    password = newPassword;
	}
	if (debug) System.out.println("set user-" + username + 
	    "\nset pass-" + password);
    }
    public static String getPassword(boolean decider) {
	if (debug) System.out.println("get user-" + username + 
				      "\nget pass-" + password);
	if (decider) {
	    return username;
	} else {
	    return password;
	}
    }
}
