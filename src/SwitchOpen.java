// Created by: Douglas Gardiner
// Creation Date: Fri Apr 03 17:05:03 CDT 2009
// Update Date: Sat Nov 22 18:05:23 CST 2008
//

/* This simple little application just expedites opening
switches.  It assumes you're using putty and on a windows
box.
*/
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;
import java.io.*;
import net.sourceforge.napkinlaf.*;

/* This is used to by simply adding the tag number of the switch.  It requires 
   a few things.  The default file is switches.cvs.  The file must also be 
   in the same directory as the program. putty must also be the ssh to use.
*/

/* TODO: See about searching for the putty file or ssh depending on the 
   OS 
*/

public class SwitchOpen {

    // variables

    private static final boolean USER = true;
    private static final boolean PASSWORD = false;
    private static boolean debug = true;
    static JFrame frame;
    JTextField inputTag;
    static String switchFile = "switches.csv";
    String importFile = null;
    Pass password = new Pass();
    JTextField inputText = new JTextField("",12); 
    JLabel outputText = new JLabel("");
    static ArrayList <String> switches = new ArrayList<String>();
    String directory = null;
    static Debug debugger = null;


    // constructors

    public SwitchOpen() {

        // set up frame and contents

        /* debug hack.  load napkin or give silly messages.  It's in debug 
        mode already so it will be verbose elsewhere 
        */
        String destination = "net.sourceforge.napkinlaf.NapkinLookAndFeel";
        if (debug) {
            if (debugger == null) debugger = new Debug();
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
            // move debugger window below

        } else {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
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
        JMenu about = new JMenu("About");
        JMenu help = new JMenu("Help");
        BorderLayout layout = new BorderLayout();
        JPanel background = new JPanel(layout);
        JPanel contents = new JPanel();
        JLabel enterText = new JLabel("Enter mac:");
        contents.setLayout(new GridLayout(2,3));

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

        // add components

        contents.add(tagLabel);
        contents.add(inputTag);
        contents.add(logIn);
        contents.add(enterText);
        contents.add(inputText);
        contents.add(outputText);
        files.add(importFileSelect);
        about.add(aboutItem);
        help.add(helpItem);
        user.add(username);
        user.add(passwordItem);
        menuBar.add(files);
        menuBar.add(user);
        menuBar.add(about);
        menuBar.add(help);

        // tie frame stuff up

        if (debug) debugger.update("laf " + UIManager.getLookAndFeel().getName());
        background.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        background.add(contents);
        frame.setJMenuBar(menuBar);
        frame.getContentPane().add(background);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        SwingUtilities.updateComponentTreeUI(frame);
        frame.pack();
        frame.setVisible(true);
        readFile(switchFile);
    }
    // methods

    private void readFile(String filename) {

        /* just opens the file and slurps up contents */
        try {
            // let local file handle this
        
            if (debug) debugger.update("Using local file\nReading "+ filename);
            String buffer = null;
            File temp = new File(filename);
            directory = temp.getPath();
            BufferedReader reader = new BufferedReader(new FileReader(new File(filename)));
            while ((buffer = reader.readLine())!= null) {
                if (! switches.contains(buffer)) {
                    switches.add(buffer);
                }
            } 
            reader.close();
            if (debug) debugger.update("Finished reading " + filename);
        } catch (FileNotFoundException fnfe) {
            if (debug) debugger.update("File not there...trying network");
            JOptionPane.showMessageDialog(frame, "Are you sure " + filename + " is in " + directory +"?\ntrying for network update", 
                "File problem", JOptionPane.WARNING_MESSAGE);
        } catch (IOException ex) {
            if (debug) ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Are you sure " + filename + " is in " + directory + "?", "File problem", 
                JOptionPane.WARNING_MESSAGE);
        }
    }
    private static void writeFile(String filename) {

        /* writes a new switches file, used for imports of other files */

        try {
            if (debug) debugger.update("Writing "+ filename);
            BufferedWriter writer = new BufferedWriter(new 
            FileWriter(new File(filename)));
            for (int i = 0 ; i < switches.size(); i++) {
            writer.write(switches.get(i) + "\r\n");
            if (debug) debugger.update("Wrote - " + switches.get(i));
        }
        writer.close();
        if (debug) debugger.update("Finished writing " + filename);
        } catch (FileNotFoundException sf) {
            if (debug) {
                debugger.update(switchFile + " is not found");
                sf.printStackTrace();
            } 
            JOptionPane.showMessageDialog(frame, filename + " is in use", "File problem", JOptionPane.ERROR_MESSAGE);
        } catch (IOException sd) {
            if (debug) sd.printStackTrace();
            JOptionPane.showMessageDialog(frame, filename + " is a bad boy", "File problem", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public static void writeToFile(String filename, ArrayList<String> listing) {
    
        switches = listing;
        writeFile(filename);
    }

    public static void main(String[] args) {

        new SwitchOpen();
        FileUpdater backgroundService = null;
        if (debug){
            if (debugger == null) debugger = new Debug();
            backgroundService = new FileUpdater(frame, debugger);
        } else {
            backgroundService = new FileUpdater(frame);
        }
        Thread server = new Thread(backgroundService, "Server");
        if (backgroundService.getRun()) {
            server.start();
            if (debug) debugger.update(" --- Starting server --- ");
        } else if (debug) debugger.update(" --- Server not started ---");
    }

    private void figureIt() {

        StringBuffer buffer = new StringBuffer(inputText.getText());
        int len = buffer.length();
        boolean colons = false;
        boolean period = false;
        for (int i =0; i < len ; i++ ) {
            if (buffer.charAt(i) == Character.valueOf(':')) colons = true;
            if (buffer.charAt(i) == Character.valueOf('.')) period = true;
        }
        if (colons) {
            for (int i = 0; i < len ; i ++ ) {
                if ((buffer.charAt(i))== ':') {
                    buffer.deleteCharAt(i);
                    buffer.insert(i, '-');
                }
            }
        }  // assumes using 0001.1111.2222 notation
        if (period) {
            for (int i =0 ; i < len; i++) {
                if((buffer.charAt(i))=='.') {
                    buffer.deleteCharAt(i);
                    buffer.insert( i ,'-');
                    buffer.insert(i-2, '-');
                    if (i > 9) {
                        buffer.insert(i +4,'-');
                    }               
                }
            }   
        }
        outputText.setText(inputText.getText());
        System.out.println(buffer.toString());
        inputText.setText(buffer.toString());
    }

    private void runIt() {

        // if array is empty open dialog box saying import a file

        if (debug) debugger.update("Starting to open a switch");
        String testString = inputTag.getText();
        String command = "putty ";
        String commandLine;
        if (password.getPassword(PASSWORD) != null) {
            commandLine = command + "-pw " + password.getPassword(PASSWORD) + " ";
        } else {
            commandLine = command;
        }
        if (testString.length() < 1 ) return;
        if (debug) {
            debugger.update("command line: "+commandLine);
            debugger.update("Looking for the switch based on " + testString);
        }
        if (switches.size() < 1 ) {
            JOptionPane.showMessageDialog(frame, "Import a file because you have no data", "No switch data", 
                JOptionPane.ERROR_MESSAGE);
        }
        for (int i = 0; i < switches.size(); i ++) {
        
            // go through array and find item that has this text in it
        
            if ((switches.get(i).indexOf(testString)) >= 0) {

                // run a system command to use putty using the 
                // string from array
                if (debug) debugger.update("Found " + switches.get(i));
                try {
                    String destination ="";
                    if (password.getPassword(USER) != null) {
                        destination = password.getPassword(USER) + "@";
                    }
                    destination = destination + switches.get(i);
                    if (debug) debugger.update(commandLine + destination);
                    Process child = Runtime.getRuntime().exec(commandLine + destination);
                } catch (IOException e) {
                    if (debug) {
                        debugger.update("Something didn't work");
                        e.printStackTrace();
                    }
                    JOptionPane.showMessageDialog(frame, "Either putty is in " + directory +" or you have bigger issues", 
                        "Putty?", JOptionPane.ERROR_MESSAGE);
                    debugger.update("Make sure putty is in the " + directory + 
                        "directory");
                } // end catch block
                inputTag.setText("");
                return;
            } // end if switch found
        } // end for loop for db
        JOptionPane.showMessageDialog(frame, testString + " is not found", "bad tag", JOptionPane.ERROR_MESSAGE);
        return;
    }
    // inner classes

    public class PasswordUpdater implements ActionListener {

        /* used to reading when password is ready to be read */

        public void actionPerformed(ActionEvent pu) {
        
            if (debug) debugger.update("called "+pu.getActionCommand());
            if (pu.getActionCommand().equals("user") && !UserAccountWindow.exists()) {
                new UserAccountWindow(USER);
            } else if (!UserAccountWindow.exists()) {
                new UserAccountWindow(PASSWORD);
            }
        }
    }

    public class Help implements ActionListener {

        /* brings up the help dialog box */

        public void actionPerformed(ActionEvent as) {

            String message = "Requirements\n\nPutty must be in the same directory as\n" +
                "this program.  A switch.csv file must \nalso be in the same directory. It can \n" +
                "be created by the import menu item";
            JOptionPane.showMessageDialog(frame, message, "Help", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public class About implements ActionListener {

        /* Displays the version number and author */

        public void actionPerformed(ActionEvent ad) {

            String message = "Version: 1.3\nCreation Date: 20 March 2009\n"
                + "Author: Douglas Gardiner\n\nPlans: use excel format,"
                + " search\nfor putty or ssh depending on\nthe OS";
            JOptionPane.showMessageDialog(frame, message, 
                "about", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public class ImportListener implements ActionListener {

        /* This class is for import contents from a new file */

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

    public class EnterCheck extends KeyAdapter {

        /* Used to run the runit function based on hitting enter */

        public void keyPressed(KeyEvent e) {

            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                figureIt();
                runIt();
            }
        }
    }

    public class RunTag implements ActionListener {

        /* Used for starting the database poll and run */

        public void actionPerformed(ActionEvent ev) {

            runIt();
        }
    }   
}

