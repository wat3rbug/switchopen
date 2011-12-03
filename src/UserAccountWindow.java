// Created by: Douglas Gardiner
// Creation Date: Fri Apr 03 17:06:00 CDT 2009
// Update Date: Fri Nov 12 20:01:56 CST 2010
//
import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.event.*;

/** This class just opens a dialog window for entry either
 * password or username.
 * @author Douglas Gardiner
 */

public class UserAccountWindow {

    // variables

    private static final boolean USER = true;
    private static final boolean PASSWORD = false;
    JPanel contents = new JPanel();
    private DebugWindow debugger;
	JButton enter = new JButton("Enter");
	JFrame frame = new JFrame();
	private static boolean inUse = false;
	BorderLayout layout = new BorderLayout();
	JPanel background = new JPanel(layout);
	JPasswordField passwordEntry = new JPasswordField(10);
	JLabel passwordLabel = new JLabel("Password");
    JLabel userNameLabel = new JLabel("Username");
    JTextField username = new JTextField(10);
	private static boolean whichIsIt = PASSWORD; 
    
    // constructors

    /**
     * Creates the user account window with a reference to the debug window.
     * @param isUser boolean for which window: true for user / false for 
     * password.
     * @param passedframe the reference to the debug window for output.
     */

    public UserAccountWindow(boolean isUser, DebugWindow passedframe) {
    
        new UserAccountWindow(isUser);
        debugger = passedframe;
    }
    /**
     * Creates the user account window with a reference to the debug window.
     * @param isUser boolean for which window: true for user / false for 
     * password.
     */

    public UserAccountWindow(boolean isUser) {
    
        inUse = true;
        contents.setLayout(new BoxLayout(contents, BoxLayout.X_AXIS));
        whichIsIt = isUser;
		
		// decide whether to have user or password window and setup
		
        if (isUser == USER) {
            update("Opening user window");
            contents.add(userNameLabel);
            contents.add(username);
            username.addKeyListener(new KeyboardUpdater());
        } else {
            update("Opening password window");
            contents.add(passwordLabel);
            contents.add(passwordEntry);
            passwordEntry.addKeyListener(new KeyboardUpdater());
        }
		// common elements
		
        contents.add(enter);
        enter.addActionListener(new MouseUpdater());
        background.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        background.add(contents);
        frame.getContentPane().add(background);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
    // methods

    /**
     * Used to make sure a duplicate window isn't created for user name or 
     * password.
     * @return true if there is a window open, false if not.
     */

    public static boolean exists() {

        return inUse;
    }
	/**
	 * updates the debug window in the GUI of the application.
	 * @param message string to send to DebugWindow.	  
  	 */
	
	private void update(String message) {
		
	 	if (debugger != null) {
			debugger.update(message);
		}
	}
	/**
     *  updates the password or username in the Pass storage object.
     */
    
    private void updateUsrOrPasswd() {

        if (whichIsIt == USER) {
            update("updating username");
            Pass.setInfo(username.getText(), USER);
            inUse = false;
            frame.dispose();
        } else {
            update("updating password");
            Pass.setInfo(new String(passwordEntry.getPassword()), PASSWORD);
            inUse = false;
            frame.dispose();
        }
    }
    // inner classes

    /**
     * Allows enter key to update the password or username in the account 
     * window.
     */

    public class KeyboardUpdater extends KeyAdapter {

        @Override
       	public void keyPressed(KeyEvent e) {

            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                updateUsrOrPasswd();
            }
        }
    }
    /**
     * Allows click of the enter key to update the password or username in 
     * the account window.
     */

    public class MouseUpdater implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {

            updateUsrOrPasswd();
        }
    }
}
