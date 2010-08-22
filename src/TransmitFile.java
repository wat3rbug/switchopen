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

    // constructors

    public TransmitFile(JFrame frame, InetAddress address, Debug passedframe) {

        this(frame, address);
        debugger = passedframe;
    }
    public TransmitFile(JFrame frame, InetAddress address) {

        this.frame = frame;
        this.address = address;
        if (debug && debugger == null) debugger = new Debug();
        if (debug) debugger.update(" --- TransmitFile constructor address " + address.toString());
    }
    // methods

    public boolean sendFile() {

        /* What am I supposed to return */
        
        /* This does the heavy lifting */

        BufferedReader reader =null;
        Socket socket = null;
        success = true;
        try {
            if (debug) debugger.update(" -- TransmitFile --");
            File newFile = new File(filename);
            reader = new BufferedReader(new FileReader(newFile));
            if (debug) debugger.update("Address - "+ address.toString() + "\tport " + port);
            socket = new Socket(address, port);
            DataOutputStream writer = new DataOutputStream(socket.getOutputStream());
            String inputLine = null;
            while ((inputLine = reader.readLine()) != null ) {
                writer.writeBytes(inputLine + ",\r\n");
                if (debug) debugger.update(" --- Sending " + inputLine);
            }
            writer.flush();
            writer.close();
            reader.close();
            socket.close();
        } catch (SecurityException se) {
            JOptionPane.showMessageDialog(frame, "No permissions to read this file", "File Permissions", JOptionPane.ERROR_MESSAGE);
            if (debug) {
                debugger.update("--- Transmit File failure ---");
                se.printStackTrace();
            }
            success = false;
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(frame, "Generic failure", "Port Dead", JOptionPane.ERROR_MESSAGE);
            if (debug) {
                debugger.update("--- Transmit File failure ---");
                ioe.printStackTrace();
            }
            success = false;
        } finally {
            // return success;
        }
        return success;
    }
}