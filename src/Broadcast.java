// Created by: Douglas Gardiner
// Creation Date: Sat Oct 23 07:55:04 CDT 2010
// Update Date: Sun Oct 31 02:26:21 CDT 2010
//

/* The File takes care of broadcast task.  It calls the
    checksum object to get the CRC of the switch file.  It also
    gets the time stamp and then broadcasts in in UDP on port
    10077.
*/

import java.io.*;
import java.net.*;


/**
 * This object is for sending broadcast beacons only.  Beacons are are UDP
 * packets on port 10077.  They incorporate a SHA1 hash of the file and 
 * the timestamp for the last time it was modified.  A future version will
 * be done to use non-blocking I/O.  Note: This will be refactored because 
 * during initial development other operations were being performed for 
 * the file information, which have since been removed.
 * @author Douglas Gardiner
 */

public class Broadcast {

    
    // class variables

    private String filename = "switches.csv";
    File switchFile;
    private static long fileDate = 0;
    public DatagramSocket broadcastSocket;
    private static int port = 10077;
    private boolean debug = false;
    private Debug debugger = null;
    private String addressTxt = "255.255.255.255"; 

    // constructors

    /**
     * Creates a Broadcast object with references to the debug 
     * windowing object.
     */

    public Broadcast(Debug passedframe) {

        debugger = passedframe;
        debug = true;
        finishConstructor();
    }
    /**
     * Creates a standard Broadcast object with debugging not 
     * available.
     */

    public Broadcast() {
    
        finishConstructor();
    }
    // methods 

    /**
     * Common method for the two constructors to used for 
     * common tasks during the creation of the broadcast 
     * object.
     */

    private void finishConstructor() {
     
        switchFile = new File(filename);
        fileDate = switchFile.lastModified();
        if (debug) debugger.update(" -- Broadcast --\n "
            + "--- File date is " + fileDate);
    }
    /**
     * Creates the UDP datagram packet with the hash and 
     * timestamp of the file.  It opens UDP port 10077 in 
     * broadcast mode.  Then it broadcasts the message out.
     */
    
    public void sendMessage() {

        try {
            if (debug) debugger.update(" --- Start broadcast --- ");
            broadcastSocket = new DatagramSocket();
            broadcastSocket.setBroadcast(true);
            DatagramPacket message;
            InetAddress address = InetAddress.getByName(addressTxt);
            broadcastSocket.connect(address, port);
            if (debug) debugger.update("Opened port");
            String rawMessage = (new Long(this.getFileDate()).toString()) 
                + "," + this.getFileCRC();
            byte[] sendBuff = (rawMessage.getBytes());
            message = new DatagramPacket(sendBuff, sendBuff.length);
            broadcastSocket.send(message);
            if (debug)  debugger.update("Sent message: " + this.getFileDate());
            broadcastSocket.disconnect();
            broadcastSocket.close();
        } catch (UnknownHostException uhe) {
            
        /* Useless stuff here except debug trace */
            if (debug) {
                debugger.update("Broadcast failure");
                uhe.printStackTrace();
            }
        } catch (SocketException se) {
            if (debug) {
                debugger.update("Broadcast failure");
                se.printStackTrace();
            }
        } catch (IOException ioe) {     
            if (debug) {
                debugger.update("Broadcast failure");
                if (ioe.getMessage().matches("No route to host")) {
                    debugger.update("Check cable or make sure wireless "
                        + "is turned on");
                } else ioe.printStackTrace();
            }  
        }
        if (debug) debugger.update(" -- Broadcast --\n --- End "
            + "broadcast --- ");
    }
    /**
     * Gets the last modified date for a file.
     * @return long time in milliseconds for the last modified 
     * for the file.
     */
    
    public long getFileDate() {

    // method used to get the timestamp for a file, if it exists

        if (switchFile.exists()) {
            fileDate = switchFile.lastModified();
        } else {
            fileDate = 0;
        }
        return fileDate;
    }   
    /**
     * Gets the SHA1 hash of the file.
     * @return String hex form of file hash
     */

    public String getFileCRC() {
        
        return new Checks().update(filename);
    }
}
