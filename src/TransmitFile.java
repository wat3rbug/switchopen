// Created by: Douglas Gardiner
// Creation Date: Sat Oct 23 08:26:48 CDT 2010
// Update Date: Sat Oct 23 08:27:33 CDT 2010
//

/**
 * Performs transmit of file contents to host with the latest file.
 * @author Douglas Gardiner
 */

import java.net.*;
import javax.swing.*;
import java.io.*;

public class TransmitFile {

    // class variables

    private String filename = "switches.csv";
    private int port = 10079;
    private InetAddress address;
    private boolean debug = false;
    private boolean runTest = true;
    private JFrame frame;
    private boolean success = true;
    private Debug debugger = null;
    private static final String EOF = "-1";

    // constructors

    /**
     * Creates TransmitFile object with reference to main GUI frame and the IP address of the host requesting.
     * @param frame The reference for main GUI.
     * @param address The IP address of the host receiving the file transmission.
     * @param passedframe The reference the to debugging window for updates.
     */
    
    public TransmitFile(JFrame frame, InetAddress address, Debug passedframe) {

        this(frame, address);
        debug = true;
        debugger = passedframe;
    }
    /**
     * Creates TransmitFile object with reference to main GUI frame and the IP address of the host requesting.
     * @param frame The reference for main GUI.
     * @param address The IP address of the host receiving the file transmission.
     */

    public TransmitFile(JFrame frame, InetAddress address) {

        this.frame = frame;
        this.address = address;
    }
    // methods

    /**
     * Send the file out to the receiving host on port TCP 10079.
     * @return boolean the success or failure of the method to transmit the file.
     */
    
    public boolean sendFile() {

        BufferedReader reader =null;
        Socket socket = null;
        success = false;
        try {
            if (debug) debugger.update(" -- TransmitFile --");
            File newFile = new File(filename);
            reader = new BufferedReader(new FileReader(newFile));
            if (debug) debugger.update("Address - "+ address.toString() + "\tport " + port);
            socket = new Socket(address, port);
            socket.setTcpNoDelay(false);    // turns off nagles
            if (debug) {
                 if (socket.isBound()) {
                    debugger.update("ouput found port " + socket.getPort());
                 } else {
                    debugger.update("cannot bind outgoing " + socket.getPort());
                }
            }
            PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
            String inputLine = null;
            while ((inputLine = reader.readLine()) != null ) {
                writer.println(inputLine);
                if (debug) debugger.update(" --- Sending " + inputLine);
            }
            writer.println(EOF);
            writer.flush();
            writer.close();
            reader.close();
            socket.close();
            success = true;
        } catch (SocketException cr) { 
            if (debug) {
                 debugger.update("--- Transmit forced fail ---");
                  cr.printStackTrace();
                success = false;
            }
        } catch (SecurityException se) {
            JOptionPane.showMessageDialog(frame, "No permissions to read this file", "File Permissions", JOptionPane.ERROR_MESSAGE);
            if (debug) {
                debugger.update("--- Transmit File failure ---");
                se.printStackTrace();
            }
            success = false;
        } catch (IOException ioe) {
            if (debug) {
                debugger.update("--- Transmit File failure ---");
                ioe.printStackTrace();
            }
            success = false;
        } 
        return success;
    }
}
