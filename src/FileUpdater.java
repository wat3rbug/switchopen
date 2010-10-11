import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;

public class FileUpdater implements Runnable {

    // class variables

    private static boolean isRunning = true; 
    private static final boolean debug = false;
    private static final int MIN = 5;
    private static final int SEC_LENGTH = 1000;
    private String fileDate = null;
    private String filename = "switches.csv";
    private int port = 10077;
    private static final long TIMER_LEN = (long)MIN * 60  * SEC_LENGTH;
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
                hostnames.add(inLine);
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

        if (debug) debugger.update(" -- FileUpdater --\nInitialize server");
        beacon.sendMessage();
        Calendar timer = Calendar.getInstance();
        long loopTimeStart = System.currentTimeMillis();
        InetAddress remoteAddress = null;
        while (this.getRun()) {
            
            // if timer 5 minutes then send beacon and reset timer
        
            if (System.currentTimeMillis() > loopTimeStart + TIMER_LEN) {
                beacon.sendMessage();
                loopTimeStart = System.currentTimeMillis();
                if (debug) debugger.update("timer up, do broadcast");
            } 
            // listen for message
        
            DatagramSocket receiver = null;
            ServerSocket socket = null;
            try {
                receiver = new DatagramSocket(port);
                byte[] msgBytes = new byte[100];
                DatagramPacket message = new DatagramPacket(msgBytes, msgBytes.length);
                if (debug) debugger.update("Server listening");
                receiver.setSoTimeout(SEC_LENGTH * 15);
                receiver.receive(message);
                if (debug) debugger.update("Received\n ---- message - " + message.getData());
                remoteDate = Long.parseLong(new String(message.getData()).trim());
                if (debug) debugger.update(" ----- " + remoteDate);
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
                if (remoteAddress.getHostName().equals(hostnames.get(i))) inTheACL = true;
            }
            long adjustedRemoteDate = remoteDate + TIMER_LEN *24;
            if (debug) debugger.update(" -- FileUpdater --\nlocal file date = " + beacon.getFileDate() + 
                "\nremote file date = " + (adjustedRemoteDate));
            if (inTheACL) {
                debugger.update (" ---- " + remoteAddress.getHostName() +  " is in the List");
                if ((adjustedRemoteDate) > beacon.getFileDate()) {
            
                    // local file is older
            
                    if (debug) {
                        debugger.update("local is older\n --- Entering receive file mode --- ");
                    }
                    ReceiveFile updateLocalFile = null;
            boolean testReceive = false;
                    if (debug) {
                        updateLocalFile = new ReceiveFile(frame, debugger);
                    } else {
                        updateLocalFile = new ReceiveFile(frame);
                    }
                    if (!updateLocalFile.getFile()) {
                        if (debug) {
                            debugger.update(" ---- Receive failed...sending out another beacon to reestablish");
                        }
                        beacon.sendMessage();
                    }
                }
                if ((adjustedRemoteDate) < beacon.getFileDate()) { 
            
                    // local file is newer
            
                    if (debug) {
                        debugger.update("local is newer\n --- Entering transmit file mode --- ");
                    }
                    TransmitFile updateRemoteFile = null;
                    if (debug) {
                        updateRemoteFile = new TransmitFile(frame, remoteAddress, debugger);
                    } else {
                        updateRemoteFile = new TransmitFile(frame, remoteAddress);
                    }
                    if (!updateRemoteFile.sendFile()) {
                        if (debug) {
                            debugger.update(" ---- Transmit failed...sending out another beacon to reestablish");
                        }
                        beacon.sendMessage();   
                    }
                    if (debug) {
                        debugger.update(" -- FileUpdater --\n -- end server loop instructions -- ");
                    }
                }   
            } else { // not in ACL
                if (debug) {
                    debugger.update (" ---- " + remoteAddress.getHostName() +  " is NOT in the List");
                }
            }
        } // end while running is true           
    }
}


