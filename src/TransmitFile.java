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

    private static final String EOF = "-1";
	private static final int ERROR = JOptionPane.ERROR_MESSAGE;
	private final int PORT = 10079;
    private InetAddress destAddress;
    private DebugWindow debugger = null;
	private String filename = "switches.csv";
	private JFrame frame;
	private boolean runTest = true;
    private boolean success = true;
    
    // constructors

    /**
     * Creates TransmitFile object with reference to main GUI frame and the IP
     * address of the host requesting.
     * @param frame The reference for main GUI.
     * @param destAddress The IP address of the host receiving the file 
     * transmission.
     * @param passedframe The reference the to debugging window for updates.
     */
    
    public TransmitFile(JFrame frame, InetAddress destAddress, DebugWindow passedframe) {

        this(frame, destAddress);
        debugger = passedframe;
    }
    /**
     * Creates TransmitFile object with reference to main GUI frame and the IP 
     * address of the host requesting.
     * @param frame The reference for main GUI.
     * @param destAddress The IP address of the host receiving the file 
     * transmission.
     */

    public TransmitFile(JFrame frame, InetAddress destAddress) {

        this.frame = frame;
        this.destAddress = destAddress;
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
            update("Address - " + destAddress.getHostAddress() + "\tport " + PORT);
            socket = new Socket(destAddress, PORT);
            socket.setTcpNoDelay(false);    // turns on nagles
            if (socket.isBound()) {
                update("output found port " + socket.getPort());
            } else {
                update("cannot bind outgoing " + socket.getPort());
				throw new ConnectException();
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
			success = false;
        } catch (SocketException cr) { 
            update("Transmit forced fail");
            success = false;
        } catch (SecurityException se) {
			String popupMsg = "No permissions to read this file";
            JOptionPane.showMessageDialog(frame, popupMsg, "File Permissions", ERROR);
            update("Transmit File failure");
            success = false;
        } catch (IOException ioe) {
            update("Transmit File failure");
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
			debugger.update(message);
		}
	}
}
