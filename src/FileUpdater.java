// Created by: Douglas Gardiner
// Creation Date: Sat Oct 23 07:57:08 CDT 2010
// Update Date: Sat Oct 23 08:09:22 CDT 2010
//

/** Handles scheduling for the network operations.  It performs a broadcast every minute.
 * It handles receiving UDP broadcasts and decides whether to respond to them or how.  If the host
 * matches the access control list file, local to the machine, then it determines whether to receive the newer
 * file or transmit its file because it is the latest.
 * @author Douglas Gardiner
 * @implements Runnable
 */

import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;

public class FileUpdater implements Runnable {

    // class variables

    private static boolean isRunning = true; 
    private static final boolean debug = false;
    private static final int SEC_PER_MIN = 60;
    private static final int MIN_PER_HOUR = 60;
    private static final int HOUR_PER_DAY = 24;
    private static final int SEC_LENGTH = 1000;
    private String fileDate = null;
    private String filename = "switches.csv";
    private int port = 10077;
    private static final long TIMER_LEN = (long)SEC_PER_MIN  * SEC_LENGTH;
    private JFrame frame;
    private long remoteDate = 0;
    private Broadcast beacon = null;
    private Debug debugger = null;
    private String hostFile = "hosts.txt";
    private ArrayList<String> hostnames = new ArrayList<String>();
	private String localCRC = null; 
    
    // constructors

	/**
	 * Creates a scheduler with a reference to the GUI frame and a debug windowing object.  
	 * Used for updating the GUI and the debug window.
	 */
	
    public FileUpdater(JFrame frame, Debug passedframe) {

        debugger = passedframe;
        setRun(getHosts());
        beacon = new Broadcast(passedframe);
    }
	/**
 	* Creates a scheduler with a reference to the GUI frame.  Used for updating the GUI.
 	*/

    public FileUpdater(JFrame frame) {

        this.frame = frame;
        setRun(getHosts());
        if (debug) {
            beacon = new Broadcast((debugger = new Debug()));
        }else {
            beacon = new Broadcast();       
        }   
    }
    
    // methods

    // threadsafe methods here

	/**
 	*  Sets the running state of the scheduler.  Used to start and stop the service.
	*  Designed to be threadsafe.
 	*/

    public synchronized void setRun(boolean state) {
    
        isRunning = state;
    }
	/**
 	* retrieves the running state of the scheduler.  Designed to be threadsafe.
	* @return boolean current state of scheduler.
 	*/

    public synchronized boolean getRun() {

        return isRunning;
    } 
	// NON threadsafe methods
	
	/* This method loads up the access list of hosts that this application will respond to.  It returns false if there is none.
		It is a security check with default deny.
	*/
	/**
	 *  updates the access control list based on the local machine file.  Returns the state of operations
	 * @return boolean for the success of the file read.  Default deny is used.
	 */	
    private boolean getHosts() {
    
        /* this returns false if it cannot load the names.  The intent is to shut down the server if it doesn't have a ACL file */
        try{
            File hostFileHandle = new File(hostFile);
            BufferedReader reader = new BufferedReader(new FileReader(hostFileHandle));
            String inLine = null;
            while ((inLine = reader.readLine()) != null) {
                hostnames.add(inLine.toLowerCase());
                if (debug) debugger.update(inLine + " allowed");
            }
            if (hostnames.isEmpty()) return false;
        } catch (IOException ioe) {
            if (debug) debugger.update(" --- ACL not updated, shutting down server");
            return false;
        }
        return true;
    }
	/* This is the scheduler.  It keeps track of broadcasting, when to receive and when to transmit the file. */
	
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
            
            if (debug) debugger.update(" -- FileUpdater --\nRunning server");

            // if timer 1 minutes then send beacon and reset timer
        
            if (System.currentTimeMillis() > loopTimeStart + TIMER_LEN) {
                beacon.sendMessage();
                loopTimeStart = System.currentTimeMillis();
                if (debug) debugger.update("timer up, do broadcast");
            } 
            // listen for message

        	localCRC = new CheckSum().update(filename).trim();
            DatagramSocket receiver = null;
            ServerSocket socket = null;
            long diffInTime = 0; 
			long limitToCheck = SEC_LENGTH * SEC_PER_MIN;
			Calendar localTime = Calendar.getInstance();
			Calendar remoteTime = Calendar.getInstance();
			localTime.setTimeInMillis(beacon.getFileDate());
			String remoteCRC = null;
			String rawMessage = null;
            try {
                receiver = new DatagramSocket(port);
                byte[] msgBytes = new byte[100];
                DatagramPacket message = new DatagramPacket(msgBytes, msgBytes.length);
                if (debug) debugger.update("Server listening");
                receiver.setSoTimeout(SEC_LENGTH * 15);
                receiver.receive(message);
                if (debug) debugger.update("Received\n ---- message - " + message.getData());

				// convert message to CRC and time stamp and who did it.

				rawMessage = new String(message.getData());
				remoteDate = Long.parseLong(rawMessage.substring(0, rawMessage.indexOf(",")));
				if (rawMessage.indexOf(",") > 0) {
					remoteCRC = rawMessage.substring(rawMessage.indexOf(",") +1).trim();	
				} else {
					remoteCRC = "";
				}
				if (debug) debugger.update("remoteCRC = " + remoteCRC +"\nlocalCRC  = " + localCRC);
				remoteTime.setTimeInMillis(remoteDate);
				diffInTime = (remoteTime.getTimeInMillis() - localTime.getTimeInMillis()) / limitToCheck;
                beacon.sendMessage();
                remoteAddress = message.getAddress();
                receiver.close();
            } catch (SocketTimeoutException ste) {
                if (debug) debugger.update("nothing heard");
                if (! receiver.isClosed()) receiver.close();
                if (debug) debugger.update("closed listening socket");
                continue;
            } catch (SocketException se) {
                if (debug) se.printStackTrace();
            } catch (IOException ioe) {
                if (debug) ioe.printStackTrace();
            }   // end listen for response from broadcast
        
            // make sure host is in ACL and time is right

            boolean inTheACL = false;
            for (int i = 0 ; i < hostnames.size(); i++) {
                if (remoteAddress.getHostName().toLowerCase().equals(hostnames.get(i))) inTheACL = true;
            }
			boolean testReceive = false;
            if (debug) debugger.update(" -- FileUpdater --\nlocal  file date = " + (beacon.getFileDate() - limitToCheck) + 
                "\nremote file date = " + (remoteDate) + "\nDifference in times " + diffInTime + "\n");
            if (inTheACL ) { // are they in ACL?
				if (remoteCRC.compareTo(localCRC) != 0) { // are the files different?
					if (debug) {
						debugger.update(" -- CRC is different\n ---- " + remoteAddress.getHostName() +  " is in the List");
						debugger.update(" -- CRC check result is " + remoteCRC.compareTo(localCRC));
					}
					if (remoteDate == 0 || diffInTime < 0 ) {

    				// local file is newer or doesnt exist so transmit this one

    					if (debug) {
        					debugger.update("local is newer\n --- Entering transmit file mode --- ");
    					}
    					TransmitFile updateRemoteFile = null;
    					if (debug) {
        					updateRemoteFile = new TransmitFile(frame, remoteAddress, debugger);
    					} else {
        					updateRemoteFile = new TransmitFile(frame, remoteAddress);
    					}
						testReceive = false;
						int cycle = 0;
						
						// make 3 attempts and bail if it doesn't work
						
						while (!testReceive && cycle++ < 3) {
    						if ((testReceive = updateRemoteFile.sendFile())) {
        						if (debug) {
            						debugger.update(" ---- Transmit failed...sending out another beacon to reestablish");
        						}
        						beacon.sendMessage();   
    						}
    							if (debug) {
        							debugger.update(" -- FileUpdater --\n -- end server loop instructions -- ");
    							}
								try {
									Thread.sleep(SEC_LENGTH * 5);
								} catch (InterruptedException ie) {
			 					// do nothing because we are waiting to do things anyway
								}
							}
						}
                	if (beacon.getFileDate() == 0 || diffInTime > 0  ) {

                    // local file is older so get ready and receive file.
            
                    	if (debug) {
                        	debugger.update("local is older\n --- Entering receive file mode --- ");
                    	}
                    	ReceiveFile updateLocalFile = null;
                    	if (debug) {
                        	updateLocalFile = new ReceiveFile(frame, debugger);
                    	} else {
                        	updateLocalFile = new ReceiveFile(frame);
                    	}
						int cycle = 0;
						while (!testReceive && cycle++ < 2) {
                    		if ((testReceive = updateLocalFile.getFile())) {
                        		if (debug) {
                            		debugger.update(" ---- Receive failed...sending out another beacon to reestablish");
                        		}
                        		beacon.sendMessage();
							}
                    	}
                	}
				} else {
					if (debug) debugger.update(" -- in ACL but CRC is the same");
				}
            } else { // not in ACL
                if (debug) {
                    debugger.update (" ---- " + remoteAddress.getHostName() +  " is NOT in the List");
                }
            }
        } // end while running is true   
        if (debug) debugger.update(" --- shutting down server");        
    }
}


