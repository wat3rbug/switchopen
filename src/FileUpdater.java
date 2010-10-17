import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;

public class FileUpdater implements Runnable {

    // class variables

    private static boolean isRunning = true; 
    private static final boolean debug = true;
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
    
    // constructors

    public FileUpdater(JFrame frame, Debug passedframe) {

        debugger = passedframe;
        setRun(getHosts());
        beacon = new Broadcast(passedframe);
    }
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

    public synchronized void setRun(boolean state) {
    
        isRunning = state;
    }
    public synchronized boolean getRun() {

        return isRunning;
    } 
    private boolean getHosts() {
    
        /* this returns false if it cannot load the names.  The intent is to shut down the server if it doesn't have a config file */
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
    public void run() {

        beacon.sendMessage();
        Calendar timer = Calendar.getInstance();
        long loopTimeStart = System.currentTimeMillis();
        InetAddress remoteAddress = null;
        while (this.getRun()) {
            
            if (debug) debugger.update(" -- FileUpdater --\nRunning server");

            // if timer 5 minutes then send beacon and reset timer
        
            if (System.currentTimeMillis() > loopTimeStart + TIMER_LEN) {
                beacon.sendMessage();
                loopTimeStart = System.currentTimeMillis();
                if (debug) debugger.update("timer up, do broadcast");
            } 
            // listen for message
        
            DatagramSocket receiver = null;
            ServerSocket socket = null;
// TS spot
            long diffInTime = 0; 
//
            try {
                receiver = new DatagramSocket(port);
                byte[] msgBytes = new byte[100];
                DatagramPacket message = new DatagramPacket(msgBytes, msgBytes.length);
                if (debug) debugger.update("Server listening");
                receiver.setSoTimeout(SEC_LENGTH * 15);
                receiver.receive(message);
                if (debug) debugger.update("Received\n ---- message - " + message.getData());
                remoteDate = Long.parseLong(new String(message.getData()).trim());
// TS spot
                diffInTime = (beacon.getFileDate() - remoteDate) / SEC_LENGTH;
//
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
            // long adjustedRemoteDate = remoteDate + (TIMER_LEN * HOUR * 1);
            if (debug) debugger.update(" -- FileUpdater --\nlocal  file date = " + beacon.getFileDate() + 
                "\nremote file date = " + (remoteDate) + "\nDifference in times " + diffInTime + "\n");
            if (inTheACL) {
                debugger.update (" ---- " + remoteAddress.getHostName() +  " is in the List");
// TS spot
			if (remoteDate == 0 || diffInTime > 2) {
//
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
				while (!testReceive) {
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
// TS spot
                if (beacon.getFileDate() == 0 || diffInTime < -2) {
//
                    // local file is older
            
                    if (debug) {
                        debugger.update("local is older\n --- Entering receive file mode --- ");
                    }
                    ReceiveFile updateLocalFile = null;
                    if (debug) {
                        updateLocalFile = new ReceiveFile(frame, debugger);
                    } else {
                        updateLocalFile = new ReceiveFile(frame);
                    }
					while (!testReceive) {
                    	if ((testReceive = updateLocalFile.getFile())) {
                        	if (debug) {
                            	debugger.update(" ---- Receive failed...sending out another beacon to reestablish");
                        	}
                        	beacon.sendMessage();
						}
                    }
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


