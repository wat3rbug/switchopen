// Created by: Douglas Gardiner
// Creation Date: Fri Apr 03 17:05:03 CDT 2009
// Update Date: Fri Nov 12 20:01:56 CST 2010
//

/** The Class  is the heart of the application.  It does mac
 * address conversion, switch location, change of GUI for debug
 * mode and starts a new thread.  The thread is for background network
 * updates. This is used to by simply adding the tag number of the switch.  It 
 * requires  a few things.  The default file is switches.csv.  The file must 
 * also be in the same directory as the program. putty must also be in the 
 * directory if using windows otherwise ssh is used.
 * @author Douglas Gardiner
 */

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
import net.sourceforge.napkinlaf.*;
import java.awt.datatransfer.*;
import java.awt.Toolkit;

public class SwitchOpen {

    // variables

	static JFrame frame;
    JTextField inputTag;
	static String switchFile = "switches.csv";
    String importFile = null;
    Pass userInfo = new Pass();
    JTextField inputText = new JTextField("", 12); 
    JLabel outputText = new JLabel("");
    static ArrayList<String> switches = new ArrayList<String>();
    String directory = null;
    static DebugWindow debugger = null;
	static FileUpdater backgroundService = null;
    JCheckBoxMenuItem updating = new JCheckBoxMenuItem("Automatic");

    private static boolean debug = false;
    private static final boolean USER = true;
    private static final boolean PASSWORD = false;  
    private static final boolean runNetwork = true;
    
    // constructors

    /**
     * Creates the main GUI for the application, starts the network thread, 
     * if enabled and starts listening for input.
     */

    public SwitchOpen() {

        // set up frame and contents

        /* debug hack.  load napkin or give silly messages.  It's in debug 
           mode already so it will be verbose elsewhere  */

        String destination = "net.sourceforge.napkinlaf.NapkinLookAndFeel";
        if (debug) {
            if (debugger == null) {
                debugger = new DebugWindow();
            }
            try {
                UIManager.setLookAndFeel(destination);
            } catch (ClassNotFoundException es) {
                es.printStackTrace();
            } catch (InstantiationException et) {
                System.out.println("cannot initialize what I cannot find");
            } catch (IllegalAccessException er) {
                System.out.println("Now sure what to say anymore");
            } catch (UnsupportedLookAndFeelException ev) {
                System.out.println("cannot do " + destination);
                ev.printStackTrace();
            }
        } else {
            try {
                UIManager.setLookAndFeel(
                    UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ef) {
                // disregard as this is to be quiet,  It's a hack.  If it
                // doesn't load so much the better
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
        JLabel enterText = new JLabel("Enter mac:");
        contents.setLayout(new GridLayout(2, 3));

        // add listeners

        username.setActionCommand("user");
        username.addActionListener(new PasswordUpdater());
        passwordItem.setActionCommand("password");
        passwordItem.addActionListener(new PasswordUpdater());
        importFileSelect.addActionListener(new ImportListener());
        logIn.addActionListener(new RunTag());
        inputTag.addKeyListener(new EnterCheck());
        inputText.addKeyListener(new EnterCheck());
        aboutItem.addActionListener(new About());
        helpItem.addActionListener(new Help());
        updating.addActionListener(new UpdaterCheck());

        // add components

        contents.add(tagLabel);
        contents.add(inputTag);
        contents.add(logIn);
        contents.add(enterText);
        contents.add(inputText);
        contents.add(outputText);
        files.add(importFileSelect);
        network.add(updating);
        updating.setState(runNetwork);
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

	
	inputText.addMouseListener(new MouseClicker());
	inputTag.addMouseListener(new MouseClicker());


    }
    // methods

	/**
     * Main() method no command line arguments are used
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
        if (runNetwork) {
            server.start();
            if (debugger != null) {
                debugger.update(" --- Starting server --- ");
            }
        } else {
            if (debugger != null) {
                debugger.update(" --- Server not started ---");
            }
        }
    }
    /**
     * Opens up the switch file for reading.
     * @param filename the String representation of the filename.
     */

    private void readFile(String filename) {

        /* just opens the file and slurps up contents */

        File temp = null;
        try {
            // let local file handle this
        
            if (debugger != null) {
                debugger.update("Using local file\nReading " + filename);
            }
            String buffer = null;
            temp = new File(filename);
            directory = temp.getAbsolutePath();
            BufferedReader reader = new BufferedReader(new FileReader(new 
                File(filename)));
            while ((buffer = reader.readLine()) != null) {
				if (buffer.indexOf(",") >= 0) {
					buffer = buffer.substring(0, buffer.indexOf(","));
				}
                if (!switches.contains(buffer)) {
                    switches.add(buffer);
                }
            } 
            reader.close();
            if (debugger != null) {
                debugger.update("Finished reading " + filename);
            }
        } catch (FileNotFoundException fnfe) {
            if (debugger != null) {
                debugger.update("File not there...trying network");
            }
            temp.delete();
        } catch (IOException ex) {
            if (debugger != null) {
                ex.printStackTrace();
            }
            JOptionPane.showMessageDialog(frame, "Are you sure " + filename 
                + " is in " + directory + "?", "File problem", 
                JOptionPane.WARNING_MESSAGE);
        }
    }
    /**
     * Opens up the file for writing.  Used for imports.
     * @param filename String representation of the filename to write.
     */

    private static void writeFile(String filename) {

        /* writes a new switches file, used for imports of other files */

        try {
            if (debugger != null) {
                debugger.update("Writing " + filename);
            }
            BufferedWriter writer = new BufferedWriter(new 
            FileWriter(new File(filename)));
            for (int i = 0; i < switches.size(); i++) {
                writer.write(switches.get(i) + "\r\n");
                if (debugger != null) {
                    debugger.update("Wrote - " + switches.get(i));
                }
            }
            writer.close();
            if (debugger != null) {
                debugger.update("Finished writing " + filename);
            }
        } catch (FileNotFoundException sf) {
            if (debugger != null) {
                debugger.update(switchFile + " is not found");
                sf.printStackTrace();
            } 
            JOptionPane.showMessageDialog(frame, filename + " is in use", 
                "File problem", JOptionPane.ERROR_MESSAGE);
        } catch (IOException sd) {
            if (debugger != null) {
                sd.printStackTrace();
            }
            JOptionPane.showMessageDialog(frame, filename + " is a bad boy", 
                "File problem", JOptionPane.ERROR_MESSAGE);
        }
    }
    /* The start of everything */
    
    
    /** Converts the MAC address from one setup to something that will work in
     *  Cisco's user tracker toolbar. Results on changed on the GUI.
     */
    
    private void figureIt() {

        StringBuffer buffer = new StringBuffer(inputText.getText());
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
        outputText.setText(inputText.getText());
        System.out.println(buffer.toString());
        inputText.setText(buffer.toString());
    }
    /** Activates the search.  It ties the username, password, system 
     * type to figure out whether to use putty, or ssh, looks for the 
     * DNS name and opens a session.
     */
        
    private void runIt() {

        // if array is empty open dialog box saying import a file

        if (debugger != null) {
            debugger.update("Starting to open a switch");
        }
        String testString = inputTag.getText().trim();
        String command = null;
        if (System.getProperty("os.name").startsWith("Windows")) {
            command = "putty ";
        } else {
            command = "xterm -e ssh ";
        }
        String commandLine;
        if (userInfo.getInfo(PASSWORD) != null  
            && command.startsWith("putty")) {
            commandLine = command + "-pw " + userInfo.getInfo(PASSWORD) + " ";
        } else {
            commandLine = command;
        }
        if (testString.length() < 1) {
            return;
        }
        if (debugger != null) {
            debugger.update("command line: " + commandLine);
            debugger.update("Looking for the switch based on " + testString);
        }
        
        Pattern ipAddress = Pattern.compile(
            "^[0-9]*\\.[0-9]*\\.[0-9]*\\.[0-9]*$");
        Matcher validateIp = ipAddress.matcher(testString);
        String validIp = null;
        if (validateIp.matches()) {
            if (debugger != null) {
                debugger.update(testString + " is a valid IP");
            }
            validIp = testString;
        } else {
             if (switches.size() < 1) {
					if (debugger != null) {
		                debugger.update("first check for file failed");
		            }
					readFile(switchFile);
					if (debugger != null) {
		                debugger.update("read file again");
		            }
					if (switches.size() < 1) {
						if (debugger != null) {
			                debugger.update("second check for file failed");
			            }
	                	JOptionPane.showMessageDialog(frame, "Import a file because you have no data", "No switch data", 
	                    	JOptionPane.ERROR_MESSAGE);
	                	return;
					}
	            }
			readFile(switchFile);
            for (int i = 0; i < switches.size(); i++) {
        
                // go through array and find item that has this text in it
        
                if ((switches.get(i).indexOf(testString)) >= 0) {

                    // run a system command to use putty using the 
                    // string from array
                    validIp = switches.get(i);
                    if (debug) {
                        debugger.update("Found " + validIp);
                    }
                }
            }               
        }   
        if (validIp == null || validIp.equals("")) {
            JOptionPane.showMessageDialog(frame, testString + " is not found", 
                "bad tag", JOptionPane.ERROR_MESSAGE);
            return;
        }
        inputTag.setText("");
        try {
            String destination = "";      
            if (userInfo.getInfo(USER) != null) {
                    destination = userInfo.getInfo(USER) + "@";
            }
            destination = destination + validIp;
            if (command.startsWith("xterm")) {
                destination += " ";
            }
            if (debugger != null) {
                debugger.update(commandLine + destination);
            }
            Process child = Runtime.getRuntime().exec(commandLine  
                + destination);
       } catch (IOException e) {
            if (debugger != null) {
                  debugger.update("Something didn't work");
                  e.printStackTrace();
            }
            JOptionPane.showMessageDialog(frame, "Either putty is not in "  
                + directory + " or you have bigger issues", 
                "Putty?", JOptionPane.ERROR_MESSAGE);
            if (debugger != null) {
                debugger.update("Make sure putty is in the " + directory  
                    + "directory");
                }
        } // end catch block
    }
    // inner classes

    /**
     * Listening class designed for password and username updates. 
     */

    public class PasswordUpdater implements ActionListener {

        /* used for reading when password is ready to be read */

        /**
         * Updates UserAccountWindow based on the command associated with 
         * the menu Item in the GUI.
         */

        public void actionPerformed(ActionEvent pu) {
        
            if (debugger != null) {
                debugger.update("called " + pu.getActionCommand());
            }
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
     * Listening class designed to bring up the help dialog box. 
     */

    public class Help implements ActionListener {

        /** 
         * Brings up the help dialog box 
         */

        public void actionPerformed(ActionEvent as) {

            String message = "Requirements\n\nPutty must be in the same "
                + "directory \nas this program.  A switch.csv file \nmust"
                + " also be in the same directory. It \ncan be created by"
                + " the import menu \nitem or from a network of others";
            JOptionPane.showMessageDialog(frame, message, "Help", 
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    /**
     * Listening class designed to bring up the about dialog box. 
     */

    public class About implements ActionListener {

        /**
         * Displays the version number and author 
         */

        public void actionPerformed(ActionEvent ad) {

			String months[] = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
			String days[] = {"Mon", "Tues", "Wed", "Thurs", "Fri", "Sat"};
			Calendar fileStamp = Calendar.getInstance();
			fileStamp.setTimeInMillis(Broadcast.getFileDate());
			String fileDate = "Last Update: " + days[fileStamp.get(Calendar.DAY_OF_WEEK)] 
				+ " " + fileStamp.get(Calendar.DAY_OF_MONTH) + " "
				+ months[fileStamp.get(Calendar.MONTH)] + " "
				+ fileStamp.get(Calendar.YEAR);
            String message = "Version: 2.1.1\nCreation Date: 20 March 2009\n"
                + "Author: Douglas Gardiner\n" 
				+ fileDate;
            JOptionPane.showMessageDialog(frame, message, 
                "about", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    /**
     * Listening class designed to bring up the import file dialog box. 
     */

    public class ImportListener implements ActionListener {

        /**
         * Used for select the file to import and then makes a call to read 
         * the file contents and then write to the default file.
         */

        public void actionPerformed(ActionEvent es) {
        
            // open file chooser
            JFileChooser newFile = new JFileChooser();
            newFile.showOpenDialog(frame);
            if (newFile.getSelectedFile() != null) {
                importFile = newFile.getSelectedFile().getAbsolutePath();
                // reads contents from selected file and puts them in new file

                readFile(importFile);
                writeFile(switchFile);
            }
        }
    }
    /**
     * Listening class to toggle network updates on or off. 
     */

    public class UpdaterCheck implements ActionListener {

        /**
         * Toggles network updates on or off.
         */

        public void actionPerformed(ActionEvent es) {
        
            if (!backgroundService.getRun()) {
                backgroundService.setRun(true);
                Thread server = new Thread(backgroundService, "Server");
                server.start();
                if (debugger != null) {
                    debugger.update(" --- Starting server --- ");
                }
            }
            backgroundService.setRun(updating.getState());
        }
    }
    /**
     * Allows enter key to activate mac address conversions or switch 
     * login attempts 
     */

    public class EnterCheck extends KeyAdapter {

        /**
         * Runs the runit and figureit function based on hitting enter. 
         */

        public void keyPressed(KeyEvent e) {

            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                figureIt();
                runIt();
            }
        }
    }
    /**
     * Allows clicking enter key with the mouse to activate mac address 
     * conversions or switch login attempts 
     */

    public class RunTag implements ActionListener {

        /**
         * Runs the runit and figureit function based on hitting enter. 
         */

        public void actionPerformed(ActionEvent ev) {

            figureIt();
            runIt();
        }
    }   
    /**
     * Allows right click to cut and paste information.
     */

    public class MouseClicker extends MouseAdapter {

		public void mousePressed(MouseEvent e) {

		    JTextField baseToChange = (JTextField) e.getSource();  
		    if (debugger != null) {
				debugger.update("copy or paste: " + baseToChange.getText());
		    }
		    if (e.getButton() == MouseEvent.BUTTON3) {
				if (debugger != null) {
			    	debugger.update("right click?");
				}
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
    public class Copier implements ActionListener {

		String text = null;

		public Copier(String text) {

		    this.text = text;
		}
		public void actionPerformed(ActionEvent ae) {

		    // copies selection to the clipboard

		    if (debugger != null) debugger.update("In copy action " + text);
		    StringSelection ss = new StringSelection(text);
		    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
	    
		}
    }
    public class Paster implements ActionListener {

		JTextField text = null;

		public Paster(JTextField text) {

		    this.text = text;
		}
		public void actionPerformed(ActionEvent ae) {

		    if (debugger != null) debugger.update("In paste action");
		    	Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
			    try {
					if ( t!= null &&  t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
					    text.setText((String) t.getTransferData(DataFlavor.stringFlavor));
					}	
			    } catch (UnsupportedFlavorException e) {
			    } catch (IOException e) {
		    }
		}
    }
}


