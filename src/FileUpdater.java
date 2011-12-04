// Created by: Douglas Gardiner
// Creation Date: Sat Oct 23 07:57:08 CDT 2010
// Update Date: Fri Nov 12 20:01:56 CST 2010
//
import java.io.IOException;
import java.net.ServerSocket;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Calendar;
import javax.swing.JFrame;

/** Handles scheduling for the network operations.  It performs a 
 * broadcast every minute. It handles receiving UDP broadcasts and 
 * decides whether to respond to them or how.  If the host matches 
 * the access control list file, local to the machine, then it 
 * determines whether to receive the newer file or transmit its file 
 * because it is the latest.
 * @author Douglas Gardiner
 */

public class FileUpdater implements Runnable {

    // class variables

    private static final String FILE_NAME = "switches.csv";
	private static final int MAX_TRIES = 2;
	private static final int MIN_PER_HR = 60;
	private final int PORT = 10077;
 	private static final int SEC_PER_MIN = 60;
    private static final int SEC_LENGTH = 1000;
	private static final long TIMER_LEN = (long) SEC_PER_MIN * SEC_LENGTH;
	private Broadcast beacon = null;
	private DebugWindow debugger = null;
	private String fileDate = null;
   	private JFrame frame;
    private String localCRC = null; 
	private static boolean isRunning = true; 
	private long remoteDate = 0;
    private Checks securityChecks = null; 
    
    // constructors

    /**
     * Creates a scheduler with a reference to the GUI frame and a 
     * debug windowing object. Used for updating the GUI and the 
     * debug window.
	 * @param frame The reference to the JFrame. Used to allow menu updates. 
	 * @param passedframe The reference for the DebugWindow to allow updates
	 * in debug mode.
     */
    
    public FileUpdater(JFrame frame, DebugWindow passedframe) {

		this.frame = frame;
        debugger = passedframe;
		securityChecks = new Checks(debugger);
        setRun(securityChecks.exists());
        beacon = new Broadcast(securityChecks, debugger);
    }
    /**
    * Creates a scheduler with a reference to the GUI frame.  Used for 
    * updating the GUI.
	* @param frame The reference to the JFrame.  Used to allow menu updates.
    */

    public FileUpdater(JFrame frame) {

        this.frame = frame;
		securityChecks = new Checks();
        setRun(securityChecks.exists());
        beacon = new Broadcast(securityChecks);         
    }
    
    // methods

    /**
    * retrieves the running state of the scheduler.  Designed to be threadsafe.
    * @return boolean current state of scheduler.
    */

    public synchronized boolean getRun() {

        return isRunning;
    } 
    /**
     * The scheduling thread run method. Stopped by using the setRun() method.
     */
    
    public void run() {

        beacon.sendMessage();
        Calendar timer = Calendar.getInstance();
        long loopTimeStart = System.currentTimeMillis();
        InetAddress remoteAddress = null;

        // check to make sure we still want to run network updates
        
        while (this.getRun()) {
            
            update("Running server");
            // if timer 1 minutes then send beacon and reset timer
        
            if (System.currentTimeMillis() > loopTimeStart + TIMER_LEN) {
                beacon.sendMessage();
                loopTimeStart = System.currentTimeMillis();
                update("timer up, do broadcast");
            } 
            // listen for message

            localCRC = new Checks().getFileHash(FILE_NAME).trim();
            DatagramSocket receiver = null;
            ServerSocket socket = null;
            long diffInTime_ms = 0; 
            long limitToCheck_ms = 10 * SEC_LENGTH * SEC_PER_MIN;
            Calendar localTime = Calendar.getInstance();
            Calendar remoteTime = Calendar.getInstance();
            localTime.setTimeInMillis(beacon.getFileDate());
            String remoteCRC = null;
            String rawMessage = null;
            try {
                receiver = new DatagramSocket(PORT);
// check why we have this so small
                byte[] msgBytes = new byte[100];
                DatagramPacket message = new DatagramPacket(msgBytes, msgBytes.length);
// end check
                update("Server listening");
                receiver.setSoTimeout(SEC_LENGTH * 15);
                receiver.receive(message);
                update("Received message: " + message.getData());
                rawMessage = new String(message.getData());

				// remove hash from message and process it
				
				String incomingHash = ""; 
				if (rawMessage.indexOf("{}") >= 0 && rawMessage.indexOf("{") > 0 ) {
					incomingHash = rawMessage.substring(rawMessage.indexOf("{") + 1, rawMessage.indexOf("}"));
					rawMessage = rawMessage.substring(0, rawMessage.indexOf("{"));
					update("what is rawmessage after hash pulled" + rawMessage);
					securityChecks.processIncHash(incomingHash);
				}	// no hash - older versions
				
                // convert message to CRC and time stamp and who did it.

				if (rawMessage.indexOf(",") < 0) {
					update("wrong format for message");
				} else {
                	remoteDate = Long.parseLong(rawMessage.substring(0, rawMessage.indexOf(",")));
				}
                if (rawMessage.indexOf(",") > 0) {
                    remoteCRC = rawMessage.substring(rawMessage.indexOf(",") + 1).trim();    
                } else {
                    remoteCRC = "";
                }
                update("remoteCRC = " + remoteCRC + "\nlocalCRC  = " + localCRC);
                remoteTime.setTimeInMillis(remoteDate);
                diffInTime_ms = (remoteTime.getTimeInMillis() - localTime.getTimeInMillis()) / limitToCheck_ms;
                remoteAddress = message.getAddress();
                receiver.close();
            } catch (SocketTimeoutException ste) {
                update("nothing heard");
                if (!receiver.isClosed()) {
                    receiver.close();
                }
                update("closed listening socket");
                continue;
            } catch (SocketException se) {
                if (debugger != null) {
                    se.printStackTrace();
                }
            } catch (IOException ioe) {
                if (debugger != null) {
                    ioe.printStackTrace();
                }
            }   // end listen for response from broadcast
        
            // make sure host is in ACL and time is right

            boolean inTheACL = securityChecks.inACL(remoteAddress);
            boolean testReceive = false;
            update("local  file date = " + (beacon.getFileDate() - limitToCheck_ms));
  			update("remote file date = " + remoteDate);
			update("Difference in times = " + diffInTime_ms);
            if (inTheACL) { // are they in ACL?
                if (remoteCRC.compareTo(localCRC) != 0) { 
                    beacon.sendMessage(remoteAddress.getHostName());
                    update("CRC is different and " + remoteAddress.getHostName() + " is in the List");
					update("CRC check result is " + remoteCRC.compareTo(localCRC));
                    if (remoteDate == 0 || diffInTime_ms < 0) {

                    // local file is newer or doesnt exist so transmit this one

                        update("local is newer. Entering transmit file mode");
                        TransmitFile updateRemoteFile = null;
                        if (debugger != null) {
                            updateRemoteFile = new TransmitFile(frame, remoteAddress, debugger);
                        } else {
                            updateRemoteFile = new TransmitFile(frame, remoteAddress);
                        }
                        testReceive = false;
                        int tries = 0;
                        
                        // make 3 attempts and bail if it doesn't work
                        
                        while (!testReceive && tries++ <= MAX_TRIES) {
                            if ((testReceive = updateRemoteFile.sendFile())) {
                                update("Transmit failed...sending out another beacon to restart");
                                beacon.sendMessage();   
                            }
                            update("end server loop instructions");
                            try {
                                Thread.sleep(SEC_LENGTH * 5);
                            } catch (InterruptedException ie) {
                            /* do nothing because we are waiting to do 
                                things anyway */
                            }
                        }
                    }
                    if (beacon.getFileDate() == 0 || diffInTime_ms > 0) {

                    // local file is older so get ready and receive file.
            
                        update("local is older. Entering receive file mode");
                        ReceiveFile updateLocalFile = null;
                        if (debugger != null) {
                            updateLocalFile = new ReceiveFile(frame, debugger);
                        } else {
                            updateLocalFile = new ReceiveFile(frame);
                        }
                        int tries = 0;
                        while (!testReceive && tries++ < MAX_TRIES) {
                            if ((testReceive = updateLocalFile.getFile())) {
                                update("Receive failed...sending out another beacon to restart");
                                beacon.sendMessage();
                            }
                        }
                    }
                } else {
                    update("in ACL but CRC is the same");
                }
            } else { // not in ACL
                update(remoteAddress.getHostName() + " is NOT in the List");
            }
        } // end while running is true   
        update("shutting down server");     
    }
	/**
    * Sets the running state of the scheduler.  Used to start and stop the 
    * service. Designed to be threadsafe.
    */

    public synchronized void setRun(boolean state) {
    
        isRunning = state;
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


