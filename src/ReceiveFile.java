// Created by: Douglas Gardiner
// Creation Date: Sat Oct 23 08:30:35 CDT 2010
// Update Date: Sat Oct 23 08:31:01 CDT 2010
//

/**
 * Opens a serversocket for receipt of the file for an update.  File operations are contained within.
 * @author Douglas Gardiner
 */
import java.io.*;
import java.net.*;
import javax.swing.*;
import java.util.*;

/* This object is for receiving incoming files from a host that has
   a newer one.  Remember that there must be some sort of signature
*/

public class ReceiveFile {

    // class variables

    private String filename = "switches.csv";
    private static boolean debug = false;
    private boolean runTest = true;
    private int port = 10079;
    private JFrame frame;
    private static final int SEC_LENGTH = 1000;
    private boolean success = true;
    private Debug debugger = null;
    private static final String EOF = "-1";

    // constructors

    /**
     * Creates a ReceiveFile object with a reference to the frame for the main GUI and debug window for updates.
     * @param frame reference to the main GUI for updates.
     * @param passedframe reference for the debug window
     */

    public ReceiveFile(JFrame frame, Debug passedframe) {

        debug = true;
        debugger = passedframe;
        this.frame = frame;
    }
    /**
     * Creates a ReceiveFile object with a reference to the frame for main GUI updates. 
     * @param frame reference to the main GUI for updates.
     */

    public ReceiveFile(JFrame frame) {
    
        this.frame = frame;
    }
    // methods

    /**
     * Opens serversocket and copies contents from port 10079 to the file. 
     * @return boolean for success or failure to capture the information.
     */

    public boolean getFile() {
    
        // needs another ACL check to verify receive file is from ACL

        if (debug) debugger.update(" --- starting receive process --- ");
        ServerSocket socket = null;
        success = true;
        try {
            Checks securityStuff = new Checks();
            if (debug) debugger.update(" --- opening TCP socket --- ");
            socket = new ServerSocket(port);
            socket.setReuseAddress(true);
            if (debug) {
                if (socket.isBound()) debugger.update("bound to socket " + socket.getLocalPort());
                else debugger.update("Did not bind");
                debugger.update(socket.toString());
            }
            socket.setSoTimeout(SEC_LENGTH * 15); // 15 sec retry for listen so the rest of the app isn't hung
            Socket connection = socket.accept();
            InetAddress doubleCheck = connection.getInetAddress();
            if (securityStuff.exists() && securityStuff.inACL(doubleCheck)) {
                if (debug) debugger.update(" -- heard distant end");
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine = null;
                if (debug) debugger.update(" --- receiving file --- ");

                // stream accepted and now reading contents to file
            
                ArrayList <String> fileContents = new ArrayList<String>();
                while (!(inputLine = reader.readLine()).equals(EOF)) {
                    fileContents.add(inputLine);
                }
                reader.close();
                socket.close();
   
                // make sure not to create an empty file
            
                File newFile = new File(filename);
                if (newFile.exists() ) {
                    newFile.delete();
                } 
                newFile.createNewFile();
                BufferedWriter writer = new BufferedWriter(new FileWriter(newFile));
                for (int i = 0; i < fileContents.size() ; i++) {
                    writer.write(fileContents.get(i) + "\r\n");
                    if (debug) debugger.update(" ---- wrote " + fileContents.get(i));
                }
                if (debug) debugger.update(" --- closing file and TCP socket --- ");
                fileContents.clear();
                writer.flush();
                writer.close();         
                if (debug) {
                    debugger.update(Calendar.getInstance().getTime() + "\n --- Leaving receive file process ---");
                }
                JOptionPane.showMessageDialog(frame, "Update successful", "SwitchFinder Update", JOptionPane.INFORMATION_MESSAGE);
                success = true;
            } else {
                success = false;
            }
        } catch (FileNotFoundException fnfe) {
            JOptionPane.showMessageDialog(frame, "Something is blocking\nwrite permissions to " + filename, "File in Use", JOptionPane.ERROR_MESSAGE);
                if (debug) {
                    fnfe.printStackTrace();
                }
            success = false;
        } catch (SocketTimeoutException ste) {
            if (debug) debugger.update("Timeout occurred");
            success = false;
        } catch (BindException be) {
            if (! socket.isClosed()) socket.isClosed();
            success = false;
            if (debug) be.printStackTrace();
        } catch (SecurityException se) {
            JOptionPane.showMessageDialog(frame, "No permissions to write read this file", "File Permissions", JOptionPane.ERROR_MESSAGE);
            if (debug) {
                debugger.update("--- Receive File failure ---");
                se.printStackTrace();
            }
            success = false;
        } catch (IOException ioe) {
            if (debug) {
                debugger.update("--- Receive File failure ---");
                ioe.printStackTrace();
            }
            success = false;
        } finally {
            if (! socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException ioe) {
                    // do nothing as this is utterly useless.  If there isn't one go quiety
                }
            }
            // return success;
        }
        return success;
    }
}
