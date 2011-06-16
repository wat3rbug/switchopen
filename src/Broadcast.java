// Created by: Douglas Gardiner
// Creation Date: Sat Oct 23 07:55:04 CDT 2010
// Update Date: Fri Nov 12 20:01:56 CST 2010
//

/* The File takes care of broadcast task.  It calls the
    checksum object to get the CRC of the switch file.  It also
    gets the time stamp and then broadcasts in in UDP on port
    10077.
*/

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.SocketException;

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
    private static File switchFile;
    private static long fileDate = 0;
    private DatagramSocket broadcastSocket;
    private static final int port = 10077;
    private Debug debugger = null;
    private String addressTxt = "gardiner-doug.ndc.nasa.gov"; 
	String workingAddress = addressTxt;
	private Checks hostList;
	
    // constructors

    /**
     * Creates a Broadcast object with references to the debug 
     * windowing object.
     */

    public Broadcast(Debug passedframe) {

        debugger = passedframe;
		hostList = new Checks(passedframe);
        finishConstructor();
    }
    /**
     * Creates a standard Broadcast object with debugging not 
     * available.
     */

    public Broadcast() {
    
		hostList = new Checks();
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
        updateDebug("File date is " + fileDate);
    }
    /**
     * Creates the UDP datagram packet with the hash and 
     * timestamp of the file.  It opens UDP port 10077 in 
     * broadcast mode.  Then it broadcasts the message out.
     */

	public void sendMessage(String tempAddr) {
		
		if (tempAddr == null || tempAddr.equals("")) {
			workingAddress = addressTxt;
		} else {
			// workingAddress = tempAddr;
			workingAddress = hostList.next();
		}
		sendMessage();
	}
    
    public void sendMessage() {

		boolean retry = true;
		int countOfRetries = 0;
		do {
        	try {
	            updateDebug("Start broadcast");
	            broadcastSocket = new DatagramSocket();
	            broadcastSocket.setBroadcast(true);
	            DatagramPacket message;
	            InetAddress address = InetAddress.getByName(workingAddress);
	            broadcastSocket.connect(address, port);
	            updateDebug("Opened port to " + address.getHostAddress() + " at " + workingAddress);
	            String rawMessage = (new Long(this.getFileDate()).toString()) + "," + this.getFileCRC();
	            byte[] sendBuff = (rawMessage.getBytes());
	            message = new DatagramPacket(sendBuff, sendBuff.length);
	            broadcastSocket.send(message);
	            updateDebug("Sent message: " + this.getFileDate());
	            broadcastSocket.disconnect();
	            broadcastSocket.close();
				retry = false;
	        } catch (UnknownHostException uhe) {
	            /* change to cycle through ACL hosts */
	        	workingAddress = hostList.next();
				if (countOfRetries >= hostList.numberOfHosts()) {
					retry = false;
					updateDebug("Broadcast failure. No hosts available");
				}
				countOfRetries++;
	            updateDebug("Broadcast failure " + workingAddress + " not available");
	        } catch (SocketException se) {
	            updateDebug("Broadcast failure");
                se.printStackTrace();
				retry = false;
	        } catch (IOException ioe) {     
	            if (debugger != null) {
	                updateDebug("Broadcast failure");
	                if (ioe.getMessage().matches("No route to host")) {
	                    updateDebug("Check cable or make sure wireless is turned on");
						retry = false;
	                } else {
	                    ioe.printStackTrace();
	                }
	            }  
				retry = false;
	        }
		} while (retry);
        updateDebug("End ");
    }
    /**
     * Gets the last modified date for a file.
     * @return long time in milliseconds for the last modified 
     * for the file.
     */
    
    public static long getFileDate() {

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
	private void updateDebug(String message){
	
		if (debugger != null) {
			debugger.update(" --- Broadcast: " + message);
		}
	}
}
