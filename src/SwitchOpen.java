// Created by: Douglas Gardiner
// Creation Date: Fri Apr 03 17:05:03 CDT 2009
// Update Date: Fri Nov 12 20:01:56 CST 2010
//
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.util.ArrayList;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Calendar;
import java.util.GregorianCalendar;
import net.sourceforge.napkinlaf.*;
import java.awt.datatransfer.*;
import java.awt.Toolkit;

/** The Class  is the heart of the application.  It does mac
 * address conversion, switch location, change of GUI for debug
 * mode and starts a new thread.  The thread is for background network
 * updates. This is used to by simply adding the tag number of the switch.  It 
 * requires  a few things.  The default file is switches.csv.  The file must 
 * also be in the same directory as the program. putty must also be in the 
 * directory if using windows otherwise ssh is used.
 * @author Douglas Gardiner
 */

public class SwitchOpen {

    // attributes

	private static final int ERROR = JOptionPane.ERROR_MESSAGE;
    private static final int INFO = JOptionPane.INFORMATION_MESSAGE;
	private static final boolean USER = true;
    private static final boolean PASSWORD = false;
	private static final int WARNING = JOptionPane.WARNING_MESSAGE;
    static FileUpdater backgroundService = null;
	private static boolean debug = false;
    static DebugWindow debugger = null;
	String directory = null;
	static JFrame frame;
    String importFile = null;
	JTextField inputTag;
    JTextField enterMacAddress = new JTextField("", 12); 
	private static final boolean isUpdateSvcRunning = true;
	static String switchFile = "switches.csv";
    JLabel outputText = new JLabel("");
    static ArrayList<String> switches = new ArrayList<String>();
    JCheckBoxMenuItem updating = new JCheckBoxMenuItem("Automatic");
	Pass userInfo = new Pass();

    // constructors

    /**
     * Creates the main GUI for the application, starts the network thread, 
     * if enabled and starts listening for input.
     */

    public SwitchOpen() {

        // set up frame and contents

        /* debug hack.  load napkin or give silly messages.  It's in debug 
           mode already so it will be verbose elsewhere  */

        String looknfeelName = "net.sourceforge.napkinlaf.NapkinLookAndFeel";
        if (debug) {
            if (debugger == null) {
                debugger = new DebugWindow();
            }
            try {
                UIManager.setLookAndFeel(looknfeelName);
            } catch (ClassNotFoundException es) {
                es.printStackTrace();
            } catch (InstantiationException et) {
                System.out.println("cannot initialize what I cannot find");
            } catch (IllegalAccessException er) {
                System.out.println("Now sure what to say anymore");
            } catch (UnsupportedLookAndFeelException ev) {
                System.out.println("cannot do " + looknfeelName);
                ev.printStackTrace();
            }
        } else {
            try {
                UIManager.setLookAndFeel(
                    UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ef) {
                // disregard as this is to be quiet,  It's a hack.  If it
                // doesn't load it doesn't matter
                ef.printStackTrace();
            }   
        }
        frame = new JFrame("Switch Finder");
        JLabel tagLabel = new JLabel("Tag number");
        inputTag = new JTextField(10);
        JButton logIn = new JButton("Log in");
        JMenuBar menuBar = new JMenuBar();
        JMenu files = new JMenu("File");
        JMenuItem importFileSelect = new JMenuItem("Import");
        JMenu user = new JMenu("User");
        JMenuItem username = new JMenuItem("User ID");
        JMenuItem aboutItem = new JMenuItem("About");
        JMenuItem helpItem = new JMenuItem("Help");
        JMenuItem passwordItem = new JMenuItem("Password");
        JMenu network = new JMenu("Updates");
        JMenu about = new JMenu("About");
        JMenu help = new JMenu("Help");
        BorderLayout layout = new BorderLayout();
        JPanel background = new JPanel(layout);
        JPanel contents = new JPanel();
        JLabel enterMacLabel = new JLabel("Enter mac:");
        contents.setLayout(new GridLayout(2, 3));

        // add listeners

        username.setActionCommand("user");
        username.addActionListener(new PasswordUpdater());
        passwordItem.setActionCommand("password");
        passwordItem.addActionListener(new PasswordUpdater());
        importFileSelect.addActionListener(new ImportListener());
        logIn.addActionListener(new RunTag());
        inputTag.addKeyListener(new EnterCheck());
        enterMacAddress.addKeyListener(new EnterCheck());
        aboutItem.addActionListener(new About());
        helpItem.addActionListener(new Help());
        updating.addActionListener(new UpdaterCheck());

        // add components

        contents.add(tagLabel);
        contents.add(inputTag);
        contents.add(logIn);
        contents.add(enterMacLabel);
        contents.add(enterMacAddress);
        contents.add(outputText);
        files.add(importFileSelect);
        network.add(updating);
        updating.setState(isUpdateSvcRunning);
        about.add(aboutItem);
        help.add(helpItem);
        user.add(username);
        user.add(passwordItem);
        menuBar.add(files);
        menuBar.add(user);
        menuBar.add(network);
        menuBar.add(about);
        menuBar.add(help);

        // tie frame stuff up

        if (debugger != null) {
            debugger.update("laf "  
            + UIManager.getLookAndFeel().getName());
        }
        background.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        background.add(contents);
        frame.setJMenuBar(menuBar);
        frame.getContentPane().add(background);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        SwingUtilities.updateComponentTreeUI(frame);
        frame.pack();
        frame.setVisible(true);
        readFile(switchFile);

		// popup menu stuff

		enterMacAddress.addMouseListener(new MouseClicker());
		inputTag.addMouseListener(new MouseClicker());
    }
    // methods

	/** Converts the MAC address from one setup to something that will work in
     *  Cisco's user tracker toolbar. Results on changed on the GUI.
     */
    
    private void convertMacAddrFormat() {

        StringBuffer buffer = new StringBuffer(enterMacAddress.getText());
        int len = buffer.length();
        boolean colons = false;
        boolean period = false;
        for (int i = 0; i < len; i++) {
            if (buffer.charAt(i) == Character.valueOf(':')) {
                colons = true;
            }
            if (buffer.charAt(i) == Character.valueOf('.')) {
                period = true;
            }
        }
        if (colons) {
            for (int i = 0; i < len; i++) {
                if (buffer.charAt(i) == ':') {
                    buffer.deleteCharAt(i);
                    buffer.insert(i, '-');
                }
            }
        }  // assumes using 0001.1111.2222 notation
        if (period) {
            for (int i = 0; i < len; i++) {
                if (buffer.charAt(i) == '.') {
                    buffer.deleteCharAt(i);
                    buffer.insert(i, '-');
                    buffer.insert(i - 2, '-');
                    if (i > 9) {
                        buffer.insert(i + 4, '-');
                    }               
                }
            }   
        }
        outputText.setText(enterMacAddress.getText());
        System.out.println(buffer.toString());
        enterMacAddress.setText(buffer.toString());
    }
	/**
     * Main() method no command line arguments are used
	 * @param Command line arguments.  Not used
     */
    
    public static void main(String[] args) {

        new SwitchOpen();
        if (debug) {
            if (debugger == null) {
                debugger = new DebugWindow();
            }
            backgroundService = new FileUpdater(frame, debugger);
        } else {
            backgroundService = new FileUpdater(frame);
        }
        Thread server = new Thread(backgroundService, "Server");
        if (isUpdateSvcRunning) {
            server.start();
            if (debugger != null) {
                debugger.update("Starting server --- ");
            }
        } else {
            if (debugger != null) {
                debugger.update("Server not started ---");
            }
        }
    }
    /**
     * Opens up the switch file for reading.
     * @param filename the String representation of the filename.
     */

    private void readFile(String selectedFile) {

        /* just opens the file and slurps up contents */

        File temp = null;
        try {
            // let local file handle this
        
            update("Using local file\nReading " + selectedFile);
            String buffer = null;
            temp = new File(selectedFile);
            directory = temp.getPath();
            BufferedReader reader = new BufferedReader(new FileReader(new 
                File(selectedFile)));
            while ((buffer = reader.readLine()) != null) {
				buffer = buffer.toLowerCase();
                if (!switches.contains(buffer)) {
                    switches.add(buffer);
                }
            } 
            reader.close();
            update("Finished reading " + selectedFile);
        } catch (FileNotFoundException fnfe) {
            update("File not there...trying network");
            temp.delete();
        } catch (IOException ex) {
            update("Something this way wicked comes...");
			String popupMsg = "Are you sure " + selectedFile;
			popupMsg += " is in " + directory + "?";
            JOptionPane.showMessageDialog(frame, popupMsg, "File problem", WARNING);
        }
    }
	/** Activates the search.  It ties the username, password, system 
     * type to figure out whether to use putty, or ssh, looks for the 
     * DNS name and opens a session.
     */
        
    private void openSSH_Session() {

        // determine the OS and use the suitable ssh command.

        update("Starting to open a switch");
        String testString = inputTag.getText().trim().toLowerCase();
        String command = null;
		update("OS Name is " + System.getProperty("os.name"));
        if (System.getProperty("os.name").startsWith("Windows")) {
            command = "putty ";
        } else {
            command = "xterm -e ssh ";
        }
        String commandLine = command; // should clean this
		if (command.startsWith("putty")) {
			if (userInfo.getInfo(USER) != null) {
				commandLine += "-l " + userInfo.getInfo(USER) + " ";
			}
			if (userInfo.getInfo(PASSWORD) != null) {
				commandLine += "-pw " + userInfo.getInfo(PASSWORD) + " ";
			}
		} else { // i didn't test for linux because only mac and windows used
			if (userInfo.getInfo(USER) != null) {
				commandLine += userInfo.getInfo(USER) + "@";
			}
		}
        if (testString.length() < 1) {
            return;
        }
        update("command line: " + commandLine);
        update("Looking for the switch based on " + testString);
 
       // check input for IP address

        Pattern ipAddress = Pattern.compile(
            "^[0-9]*\\.[0-9]*\\.[0-9]*\\.[0-9]*$");
        Matcher validateIp = ipAddress.matcher(testString);
        String validIp = null;
        if (validateIp.matches()) {
            update(testString + " is a valid IP");
            validIp = testString;
        } else {
            if (switches.isEmpty()) {
				update("first check for file failed");
				readFile(switchFile);
				update("read file again");
				if (switches.isEmpty()) {
					update("second check for file failed");
					String popupMsg = "Import a file because you have no data";
                	JOptionPane.showMessageDialog(frame, popupMsg, "No switch data", ERROR);
                	return;
				}
            } //
            for (int i = 0; i < switches.size(); i++) {
        
                // go through array and find item that has this text in it
        
                if ((switches.get(i).indexOf(testString)) >= 0) {
                   	validIp = switches.get(i);
                    update("Found " + validIp);
                }
            }               
        }   
        if (validIp == null || validIp.equals("")) {
			String popupMsg = testString + " is not found";
            JOptionPane.showMessageDialog(frame, popupMsg, "bad tag", ERROR);
            return;
        }
        inputTag.setText("");
        try {
            String switchNlogin = "";      
            switchNlogin += validIp;
            if (command.startsWith("xterm")) {
                switchNlogin += "\n";
            }
            update(commandLine + switchNlogin);
			commandLine += switchNlogin;
            Process child = Runtime.getRuntime().exec(commandLine);
       } catch (IOException e) {
            update("Something didn't work");
			String popupMsg = "Either putty is NOT in the directory\nor you have bigger issues";
            JOptionPane.showMessageDialog(frame, popupMsg, "Putty?", ERROR);
        } 
    }
    /**
     * Opens up the file for writing.  Used for imports.
     * @param filename String representation of the filename to write.
     */

    private static void writeFile(String filename) {

        /* writes a new switches file, used for imports of other files */

        try {
            SwitchOpen.update("Writing " + filename);
            BufferedWriter writer = new BufferedWriter(new 
            FileWriter(new File(filename)));
            for (int i = 0; i < switches.size(); i++) {
                writer.write(switches.get(i) + "\r\n");
                SwitchOpen.update("Wrote - " + switches.get(i));
            }
            writer.close();
            SwitchOpen.update("Finished writing " + filename);
        } catch (FileNotFoundException sf) {
            SwitchOpen.update(switchFile + " is in use");
			String popupMsg = filename + " is in use";
            JOptionPane.showMessageDialog(frame, popupMsg, "File problem", ERROR);
        } catch (IOException sd) {
			String popupMsg = filename + " is a bad boy";
            JOptionPane.showMessageDialog(frame, popupMsg, "File problem", ERROR);
        }
    }
	/**
	 * updates the debug window in the GUI of the application.
	 * @param message string to send to DebugWindow.	  
  	 */

    public static void update(String message) {
	
		if (debugger != null) {
			debugger.update(message);
		}
	}
    
    
    // inner classes
     
    /**
     * Listening class designed to bring up the about dialog box. 
     */

    public class About implements ActionListener {

        // methods

		@Override
        public void actionPerformed(ActionEvent ad) {
			
			Calendar fileStamp_ms = Calendar.getInstance();
			//GregorianCalendar fileStamp_ms = new GregorianCalendar();
			fileStamp_ms.setTimeInMillis(Broadcast.getFileDate());
			//fileStamp_ms.roll(Calendar.DAY_OF_WEEK, false);
			//fileStamp_ms.roll(Calendar.DAY_OF_WEEK, false);
			String message = "Version: 2.3\nCreation Date: 20 March 2009\n"
                + "Author: Douglas Gardiner\n" 
				+ getFileDate(fileStamp_ms);
            JOptionPane.showMessageDialog(frame, message, "about", INFO);
        }
		/**
		 * This creates the part of the message that has when the file was updated.
		 * @param tempTimeStamp_ms The timestamp as a Calendar object.
		 * @return updateMsg The date the file is updated minus the seconds.
		 */
		
		private String getFileDate(Calendar tempTimeStamp_ms) {
			
			String updateMsg = "Last Update: ";
			String months[] = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", 
				"Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
			String days[] = {"Sun","Mon", "Tues", "Wed", "Thurs", "Fri", "Sat"};
			updateMsg += days[tempTimeStamp_ms.get(Calendar.DAY_OF_WEEK) - 1];
			updateMsg += " " + tempTimeStamp_ms.get(Calendar.DAY_OF_MONTH);
			updateMsg += " " + months[tempTimeStamp_ms.get(Calendar.MONTH)];
			updateMsg += " " + tempTimeStamp_ms.get(Calendar.YEAR);
			return updateMsg;
		}
    }      
    /**
 	 * Performs copy to System clipboard.
     */	 

    public class Copier implements ActionListener {

		// attributes
		
		String text = null;

		// constructors
		
		public Copier(String text) {

		    this.text = text;
		}
		// methods
		
		@Override
		public void actionPerformed(ActionEvent ae) {

		    // copies selection to the clipboard

		    SwitchOpen.this.update("In copy action " + text);
		    StringSelection ss = new StringSelection(text);
		    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
	    
		}
    }
	/**
     * Allows enter key to activate mac address conversions or switch 
     * login attempts 
     */

    public class EnterCheck extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {

            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                convertMacAddrFormat();
                openSSH_Session();
            }
        }
    }
	/**
     * Listening class designed to bring up the help dialog box. 
     */

    public class Help implements ActionListener {

       	@Override
        public void actionPerformed(ActionEvent as) {

            String message = "Requirements\n\nPutty must be in the same "
                + "directory \nas this program.  A switch.csv file \nmust"
                + " also be in the same directory. It \ncan be created by"
                + " the import menu \nitem or from a network of others";
            JOptionPane.showMessageDialog(frame, message, "Help", INFO);
        }
    }
	/**
     * Listening class designed to bring up the import file dialog box. 
     */

    public class ImportListener implements ActionListener {

        public void actionPerformed(ActionEvent es) {
        
            // open file chooser
            JFileChooser newFile = new JFileChooser();
            newFile.showOpenDialog(frame);
            if (newFile.getSelectedFile() != null) {
                importFile = newFile.getSelectedFile().getAbsolutePath();
                readFile(importFile);
                writeFile(switchFile);
            }
        }
    }
	/**
     * Allows clicking enter key with the mouse to activate mac address 
     * conversions or switch login attempts 
     */

    public class MouseClicker extends MouseAdapter {

		@Override
		public void mousePressed(MouseEvent e) {

		    JTextField baseToChange = (JTextField) e.getSource();  
		    update("copy or paste: " + baseToChange.getText());
		    if (e.getButton() == MouseEvent.BUTTON3) {
				update("right click?");
				JMenuItem pMenuItem;
				JPopupMenu popup = new JPopupMenu();
				pMenuItem = new JMenuItem("copy");
				pMenuItem.addActionListener(new Copier(baseToChange.getText())); 
				popup.add(pMenuItem);
				pMenuItem = new JMenuItem("paste");
				pMenuItem.addActionListener(new Paster(baseToChange)); 
				popup.add(pMenuItem);
				popup.show(e.getComponent(), e.getX(), e.getY());
			}	
		}
	}
	/**
     * Updates UserAccountWindow based on the command associated with 
     * the menu Item in the GUI.
     */

    public class PasswordUpdater implements ActionListener {

		@Override
        public void actionPerformed(ActionEvent pu) {
        
            update("called " + pu.getActionCommand());
            if (pu.getActionCommand().equals("user")  
                && !UserAccountWindow.exists()) {
                new UserAccountWindow(USER);
            } else {
                if (!UserAccountWindow.exists()) {
                    new UserAccountWindow(PASSWORD);
                }
            }
        }
    }
	/**
	 * Performs pasting to System clipboard contents.
	 */
	
    public class Paster implements ActionListener {

		// attributes
		
		JTextField text = null;

		// constructors
		
		public Paster(JTextField text) {

		    this.text = text;
		}
		// methods 
		
		@Override
		public void actionPerformed(ActionEvent ae) {

		    update("In paste action");
		    	Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
			    try {
					if ( t!= null &&  t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
					    text.setText((String) t.getTransferData(DataFlavor.stringFlavor));
					}	
			    } catch (UnsupportedFlavorException e) {
					// do nothing
			    } catch (IOException e) {
		    }
		}
    }
	/**
      * Runs the runit and figureit function based on hitting enter. 
      */

    public class RunTag implements ActionListener {

		@Override
        public void actionPerformed(ActionEvent ev) {

            convertMacAddrFormat();
            openSSH_Session();
        }
    }   
 	/**
     * Listening class to toggle network updates on or off. 
     */

    public class UpdaterCheck implements ActionListener {

		@Override
        public void actionPerformed(ActionEvent es) {

            if (!backgroundService.getRun()) {
                backgroundService.setRun(true);
                Thread server = new Thread(backgroundService, "Server");
                server.start();
                update("Starting server --- ");
            }
            backgroundService.setRun(updating.getState());
        }
    }
}


