// Created by: Douglas Gardiner
// Creation Date: Fri Apr 03 17:06:00 CDT 2009
// Update Date: Sat Nov 22 18:05:23 CST 2008
//

/* This class just opens a dialog window for entry either
   password or username.
*/
// import debug.Debug;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class UserPasswordWindow {

    // variables
    private static boolean inUse = false;
    private static final boolean USER = true;
    private static final boolean PASSWORD = false;
    private static boolean whichIsIt = PASSWORD; 
    private static final boolean debug = false;
    JFrame frame = new JFrame();
    JLabel passwordLabel = new JLabel("Password");
    JLabel userNameLabel = new JLabel("Username");
    JTextField username = new JTextField(10);
    JPasswordField passwordEntry = new JPasswordField(10);
    JButton enter = new JButton("Enter");
    BorderLayout layout = new BorderLayout();
    JPanel background = new JPanel(layout);
    JPanel contents = new JPanel();
    static Debug debugger;

    // constructors

    public UserPasswordWindow(boolean decider, Debug passedframe) {

	this(decider);
	debugger = passedframe;
    }
    public UserPasswordWindow(boolean decider) {
	//	if (debugger == null) debugger = new Debug();
	inUse = true;
	contents.setLayout(new BoxLayout(contents, BoxLayout.X_AXIS));
	whichIsIt = decider;
	if (decider == USER) {
	    if (debug) debugger.update("Opening user window");
	    contents.add(userNameLabel);
	    contents.add(username);
	    username.addKeyListener(new KeyboardUpdater());
	} else {
	    if (debug) debugger.update("Opening password window");
	    contents.add(passwordLabel);
	    contents.add(passwordEntry);
	passwordEntry.addKeyListener(new KeyboardUpdater());
	}
	contents.add(enter);
	enter.addActionListener(new MouseUpdater());
	background.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
	background.add(contents);
	frame.getContentPane().add(background);
	frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	frame.pack();
	frame.setVisible(true);
    }
    // methods

    private void updateThePassword() {

	if (whichIsIt == USER) {
	    if (debug) debugger.update("updating username");
	    new Pass().setPassword(username.getText(), USER);
	    inUse = false;
	    frame.dispose();
	} else {
	    if (debug) debugger.update("updating password" );
	    new Pass().setPassword(new String(passwordEntry.getPassword()), PASSWORD);
	    inUse = false;
	    frame.dispose();
	}
    }
    public static boolean exists() {

	return inUse;
    }
    // inner classes

    public class KeyboardUpdater extends KeyAdapter {

	public void keyPressed(KeyEvent e) {

	    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
		updateThePassword();
	    }
	}
	
    }
    public class MouseUpdater implements ActionListener {

	public void actionPerformed(ActionEvent e) {

	    updateThePassword();
	}
    }
}
