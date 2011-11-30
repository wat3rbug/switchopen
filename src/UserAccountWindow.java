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

    private static boolean inUse = false;
    private static final boolean USER = true;
    private static final boolean PASSWORD = false;
    private static boolean whichIsIt = PASSWORD; 
    JFrame frame = new JFrame();
    JLabel passwordLabel = new JLabel("Password");
    JLabel userNameLabel = new JLabel("Username");
    JTextField username = new JTextField(10);
    JPasswordField passwordEntry = new JPasswordField(10);
    JButton enter = new JButton("Enter");
    BorderLayout layout = new BorderLayout();
    JPanel background = new JPanel(layout);
    JPanel contents = new JPanel();
    private DebugWindow debugger;

    // constructors

    /**
     * Creates the user account window with a reference to the debug window.
     * @param decider boolean for which window: true for user / false for 
     * password.
     * @param passedframe the reference to the debug window for output.
     */

    public UserAccountWindow(boolean decider, DebugWindow passedframe) {
    
        new UserAccountWindow(decider);
        debugger = passedframe;
    }
    /**
     * Creates the user account window with a reference to the debug window.
     * @param decider boolean for which window: true for user / false for 
     * password.
     */

    public UserAccountWindow(boolean decider) {
    
        inUse = true;
        contents.setLayout(new BoxLayout(contents, BoxLayout.X_AXIS));
        whichIsIt = decider;
        if (decider == USER) {
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
			debugger.update(" --- UserAccountWindow: " + message);
		}
	}
	/**
     *  updates the password or username in the Pass storage object.
     */
    
    private void updateThePassword() {

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

		// methods
		
        @Override
       	public void keyPressed(KeyEvent e) {

            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                updateThePassword();
            }
        }
    }
    /**
     * Allows click of the enter key to update the password or username in 
     * the account window.
     */

    public class MouseUpdater implements ActionListener {

		// methods
		
        @Override
        public void actionPerformed(ActionEvent e) {

            updateThePassword();
        }
    }
}
