// Created by: Douglas Gardiner
// Creation Date: Sat Oct 23 08:26:48 CDT 2010
// Update Date: Fri Nov 12 20:01:56 CST 2010
//
import java.net.Socket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.ConnectException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;

/**
 * Performs transmit of file contents to host with the latest file.
 * @author Douglas Gardiner
 */

public class TransmitFile {

    // attributes

    private String filename = "switches.csv";
    private final int PORT = 10079;
    private InetAddress address;
    private boolean runTest = true;
    private JFrame frame;
    private boolean success = true;
    private DebugWindow debugger = null;
    private static final String EOF = "-1";

    // constructors

    /**
     * Creates TransmitFile object with reference to main GUI frame and the IP
     * address of the host requesting.
     * @param frame The reference for main GUI.
     * @param address The IP address of the host receiving the file 
     * transmission.
     * @param passedframe The reference the to debugging window for updates.
     */
    
    public TransmitFile(JFrame frame, InetAddress address, DebugWindow passedframe) {

        this(frame, address);
        debugger = passedframe;
    }
    /**
     * Creates TransmitFile object with reference to main GUI frame and the IP 
     * address of the host requesting.
     * @param frame The reference for main GUI.
     * @param address The IP address of the host receiving the file 
     * transmission.
     */

    public TransmitFile(JFrame frame, InetAddress address) {

        this.frame = frame;
        this.address = address;
    }
    // methods

    /**
     * Send the file out to the receiving host on port TCP 10079.
     * @return boolean the success or failure of the method to transmit the 
     * file.
     */
    
    public boolean sendFile() {

        BufferedReader reader = null;
        Socket socket = null;
        success = false;
        try {
            update("Start transmit");
            File newFile = new File(filename);
            reader = new BufferedReader(new FileReader(newFile));
            update("Address - " + address.toString() + "\tport " + PORT);
            socket = new Socket(address, PORT);
            //socket.setTcpNoDelay(false);    // turns on nagles
            if (debugger != null) {
                 if (socket.isBound()) {
                    update("output found port " + socket.getPort());
                 } else {
                    update("cannot bind outgoing " + socket.getPort());
                }
            }
            PrintWriter writer = new PrintWriter(new BufferedWriter(new 
                OutputStreamWriter(socket.getOutputStream())), true);
            String inputLine = null;
            while ((inputLine = reader.readLine()) != null) {
                writer.println(inputLine);
                update("Sending " + inputLine);
            }
            writer.println(EOF);
            writer.flush();
            writer.close();
            reader.close();
            socket.close();
            success = true;
		} catch (ConnectException ce) {
			update("unable to setup a port");
			ce.printStackTrace();
        } catch (SocketException cr) { 
            update("Transmit forced fail");
            if (debugger != null) cr.printStackTrace();
            success = false;
        } catch (SecurityException se) {
            JOptionPane.showMessageDialog(frame, "No permissions to read this file", "File Permissions", 
                JOptionPane.ERROR_MESSAGE);
            update("Transmit File failure");
            if (debugger != null) se.printStackTrace();
            success = false;
        } catch (IOException ioe) {
            	update("Transmit File failure");
                if (debugger != null) ioe.printStackTrace();
            success = false;
        } 
        return success;
    }
	/**
	 * updates the debug window in the GUI of the application.
	 * @param message string to send to DebugWindow.	  
  	 */

	private void update(String message) {
		
	 	if (debugger != null) {
			debugger.update(" --- TransmitFile: " + message);
		}
	}
}
