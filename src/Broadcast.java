import java.io.*;
import java.net.*;

/* This object is for sending broadcast beacons only.  This
   will need to be modified so that it will work with the non-blocking IO
   libraries */

public class Broadcast {

    /* This is to automatically send out a broadcast of the file date */

    // class variables

    private String filename = "switches.csv";
	File switchFile;
    private static long fileDate = 0;
    public DatagramSocket broadcastSocket;
    private static int port = 10077;
    private boolean debug = false;
    private Debug debugger = null;
    private String addressTxt = "255.255.255.255"; // can restrict this to just subnet broadcast

    // constructors

    public Broadcast(Debug passedframe) {

        debugger = passedframe;
        finishConstructor();
    }
    public Broadcast() {
    
        if (debug && debugger == null) debugger = new Debug();
        finishConstructor();
    }
    // methods 

    private void finishConstructor() {
    
        switchFile = new File(filename);
        fileDate = switchFile.lastModified();
        if (debug) debugger.update(" -- Broadcast --\n --- File date is " + fileDate);
    }

    public void sendMessage() {

        try {
            if (debug) debugger.update(" --- Start broadcast --- ");
            broadcastSocket = new DatagramSocket();
            broadcastSocket.setBroadcast(true);
            DatagramPacket message;
            InetAddress address = InetAddress.getByName(addressTxt);
            broadcastSocket.connect(address, port);
            if (debug) debugger.update("Opened port");
// ts
			String rawMessage = (new Long(this.getFileDate()).toString()) + "," + this.getFileCRC();
//
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
                if (ioe.getMessage().matches("No route to host")) debugger.update("Check cable or make sure wireless is turned on");
                else ioe.printStackTrace();
            }  
        }
        if (debug) debugger.update(" -- Broadcast --\n --- End broadcast --- ");
    }

    public long getFileDate() {
	
	// method used to get the timestamp for a file, if it exists

		if (switchFile.exists()) {
			fileDate = switchFile.lastModified();
		} else {
			fileDate = 0;
		}
        return fileDate;
    }   
	public String getFileCRC() {
		
		CheckSum crc = new CheckSum(filename);
		return crc.update();
	}
}
