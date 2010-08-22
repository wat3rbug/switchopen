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
    private static final boolean debug = true;
    private boolean runTest = true;
    private int port = 10079;
    private JFrame frame;
    private static final int SEC_LENGTH = 1000;
    private boolean success = true;
    private Debug debugger = null;

    // constructors

    public ReceiveFile(JFrame frame, Debug passedframe) {

        debugger = passedframe;
        this.frame = frame;
    }
    public ReceiveFile(JFrame frame) {
    
        if (debug && debugger == null) {
            debugger = new Debug();
        }
        this.frame = frame;
    }
    // methods

    public boolean getFile() {

        /* What am I supposed to return */
        
        /* This does the heavy lifting */
        // add timeout so it can retry
        if (debug) debugger.update(" --- starting receive process --- ");
        ServerSocket socket = null;
        success = true;
        try {
            if (debug) debugger.update(" --- opening TCP socket --- ");
            socket = new ServerSocket(port);
            socket.setSoTimeout(SEC_LENGTH * 15);
            Socket connection = socket.accept();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine = null;
            if (debug) debugger.update(" --- receiving file --- ");
            ArrayList <String> fileContents = new ArrayList<String>();
            while ((inputLine = reader.readLine()) != null) {
                fileContents.add(inputLine);
            }
            reader.close();
            socket.close();
            // do file stuff after everything read from th e net

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
                debugger.update(" --- Leaving receive file process ---");
                JOptionPane.showMessageDialog(frame, "Successfully received and updated local file", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (FileNotFoundException fnfe) {
            JOptionPane.showMessageDialog(frame, "Something is blocking\nwrite permissions to " + filename, "File in Use", JOptionPane.ERROR_MESSAGE);
            if (debug) {
                fnfe.printStackTrace();
            }
            success = false;
        } catch (SocketTimeoutException ste) {
            JOptionPane.showMessageDialog(frame, "Lost connection to host", "Lost Host", JOptionPane.ERROR_MESSAGE);
            if (debug) ste.printStackTrace();
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
            JOptionPane.showMessageDialog(frame, "Generic failure", "Port Dead", JOptionPane.ERROR_MESSAGE);
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
