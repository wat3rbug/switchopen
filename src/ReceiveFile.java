// Created by: Douglas Gardiner
// Creation Date: Sat Oct 23 08:30:35 CDT 2010
// Update Date: Fri Nov 12 20:01:56 CST 2010
//
import java.io.IOException;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.BindException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Opens a serversocket for receipt of the file for an update.  
 * File operations are contained within.
 * @author Douglas Gardiner
 */

public class ReceiveFile {

    // attributes

    private String filename = "switches.csv";
    private boolean runTest = true;
    private final int PORT = 10079;
    private JFrame frame;
    private static final int SEC_LENGTH = 1000;
    private boolean success = true;
    private DebugWindow debugger = null;
    private static final String EOF = "-1";

    // constructors

    /**
     * Creates a ReceiveFile object with a reference to the frame 
     * for the main GUI and debug window for updates.
     * @param frame reference to the main GUI for updates.
     * @param passedframe reference for the debug window
     */

    public ReceiveFile(final JFrame frame, final DebugWindow passedframe) {

        debugger = passedframe;
        this.frame = frame;
    }
    /**
     * Creates a ReceiveFile object with a reference to the frame 
     * for main GUI updates. 
     * @param frame reference to the main GUI for updates.
     */

    public ReceiveFile(final JFrame frame) {
    
        this.frame = frame;
    }
    // methods

    /**
     * Opens serversocket and copies contents from port 10079 to the file. 
     * @return boolean for success or failure to capture the information.
     */

    public final boolean getFile() {
    
        // needs another ACL check to verify receive file is from ACL

        update("starting receive process");
        ServerSocket socket = null;
        success = true;
        try {
            Checks securityStuff = new Checks();
            update("opening TCP socket");
            socket = new ServerSocket(PORT);
            socket.setReuseAddress(true);
            if (debugger != null) {
                if (socket.isBound()) {
                    update("bound to socket " + socket.getLocalPort());
                } else {
                    update("Did not bind");
                }
                update(socket.toString());
            }
            socket.setSoTimeout(SEC_LENGTH * 15); 

            // 15 sec retry for listen so the rest of the app isn't hung
            
            Socket connection = socket.accept();
            InetAddress doubleCheck = connection.getInetAddress();
            if (securityStuff.exists() && securityStuff.inACL(doubleCheck)) {
                update("heard distant end");
                BufferedReader reader = new BufferedReader(new 
                    InputStreamReader(connection.getInputStream()));
                String inputLine = null;
                update("receiving file");

                // stream accepted and now reading contents to file
            
                ArrayList<String> fileContents = new ArrayList<String>();
                while (!(inputLine = reader.readLine()).equals(EOF)) {
                    fileContents.add(inputLine);
                }
                reader.close();
                socket.close();
   
                // make sure not to create an empty file
            
                File newFile = new File(filename);
                if (newFile.exists()) {
                    newFile.delete();
                } 
                newFile.createNewFile();
                BufferedWriter writer = new BufferedWriter(new FileWriter(newFile));
                for (int i = 0; i < fileContents.size(); i++) {
                    writer.write(fileContents.get(i) + "\r\n");
                    update("wrote " + fileContents.get(i));
                }
                update("closing file and TCP socket");
                fileContents.clear();
                writer.flush();
                writer.close();         
                update(Calendar.getInstance().getTime() + "\n --- Leaving receive file process ---");
                JOptionPane.showMessageDialog(frame, "Update successful from " + doubleCheck.getHostName(), 
                    "SwitchFinder Update", JOptionPane.INFORMATION_MESSAGE);
                success = true;
            } else {
                success = false;
            }
        } catch (FileNotFoundException fnfe) {
            JOptionPane.showMessageDialog(frame, "Something is blocking\nwrite permissions to " + filename, "File in Use", 
                    JOptionPane.ERROR_MESSAGE);
                if (debugger != null) {
                    fnfe.printStackTrace();
                }
            success = false;
        } catch (SocketTimeoutException ste) {
            update("Timeout occurred");
            success = false;
        } catch (BindException be) {
            if (!socket.isClosed()) {
                socket.isClosed();
            }
            success = false;
            if (debugger != null) {
                be.printStackTrace();
            }
        } catch (SecurityException se) {
            JOptionPane.showMessageDialog(frame, "No permissions to write read" 
                + " this file", "File Permissions", JOptionPane.ERROR_MESSAGE);
            update("Receive File failure");
            if (debugger != null) se.printStackTrace();
            success = false;
        } catch (IOException ioe) {
            update("Receive File failure");
            if (debugger !=  null) ioe.printStackTrace();
            success = false;
        } finally {
            if (!socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException ioe) {
                    // just die quietly
                }
            }
            // return success;
        }
        return success;
    }
	/**
	 * updates the debug window in the GUI of the application.
	 * @param message string to send to DebugWindow.	  
  	 */

	private void update(String message) {
		
	 	if (debugger != null) {
			debugger.update(" --- ReceiveFile: " + message);
		}
	}
}
