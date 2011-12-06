// Created by: Douglas Gardiner
// Creation Date: Fri Oct 22 13:21:14 CDT 2010
// Update Date: Fri Nov 12 20:01:56 CST 2010
//
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.net.InetAddress;
import java.io.IOException;
import java.net.UnknownHostException;
 
/** 
 * Used for getting a checksum hash of a file.  This provides general
 * security checks for the SwitchFinder application.
 * @author Douglas Gardiner
 */

public class Checks {
 
    // class variables
    
    private boolean aclPresent = true;
    private DebugWindow debugger = null;
	private static final String UNKNOWN_IP_ADDR = "0.0.0.0";
	private String hostFile = "hosts.txt";
    private static KeyValuePair hostInfo = null;
	private ArrayList<String> hostnames = new ArrayList<String>();
	private static final int MAX_BYTE = 1024;
    
    // constructors
    
    /**
     *  Creates an ACL object with a reference to the debugging window.
     * @param passedframe Reference to the debugging window.
     */

    public Checks(DebugWindow passedframe) {
        
        debugger = passedframe;
		hostInfo = new KeyValuePair(debugger);
        finishConstructor();
    }
    /**
     *  Creates an ACL object to store the allowable hosts.
     */ 
    
    public Checks() {
	
		hostInfo = new KeyValuePair();
		finishConstructor();
	}    
    // methods
    
    /**
     * checks to see if the the ACL file exists and was read.
     * @return aclPresent true if things worked out / false if the process 
     * failed for any reason
     */
    
    public boolean exists() {
    
        return aclPresent;
    }  
	/** 
	 * Provides functions for both styles of constructors since
	 * constructor overloading was not working as intended.
	 */
	
	private void finishConstructor() {
    
        /* this returns false if it cannot load the names.  
         * The intent is to shut down the server if it doesn't have a ACL 
		 * file. 
         */
        
        try {
            File hostFileHandle = new File(hostFile);
            BufferedReader reader = new BufferedReader(new
 				FileReader(hostFileHandle));
            String inLine = null;
			String ipAddress = null;
            while ((inLine = reader.readLine()) != null) {
                hostnames.add(inLine.toLowerCase());
				try {					
					ipAddress = InetAddress.getByName(inLine).getHostAddress();
				} catch (UnknownHostException uhe) {
					// disregard if not found
				}
				// failsafe if no address given
				if (ipAddress.isEmpty() || ipAddress.equals("")) {
					ipAddress = UNKNOWN_IP_ADDR;
				}				
				// if name is not an address and address is known
				// update hostInfo
				hostInfo.put(inLine, ipAddress);
				update(inLine + " allowed");
            }
            if (hostnames.isEmpty()) {
                aclPresent = false;
            }
            reader.close();
        } catch (IOException ioe) {
            update("ACL not updated, shutting down server");
            aclPresent = false;
        }
    }
    /**
     * returns the String representation of the SHA1 hash of the filename 
     * as a String.
     * @return buffer - String representation of the SHA1 hash in hexidecial
     * form.
     * @param filename String representation of the filename to be used.
     */

    public static String getFileHash(String filename) {
        
        StringBuffer buffer = new StringBuffer("");
        try {   
            MessageDigest md = MessageDigest.getInstance("SHA1");
            FileInputStream fis = new FileInputStream(filename);
            byte[] dataBytes = new byte[MAX_BYTE];          
            int bytesRead = 0;
            while ((bytesRead = fis.read(dataBytes)) != -1) {
                md.update(dataBytes, 0, bytesRead);
            }
            byte[] mdbytes = md.digest();
            for (int i = 0; i < mdbytes.length; i++) {
                buffer.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            fis.close();
        } catch (Exception ioe) {
            // do what? file failed send 0 out
            buffer = new StringBuffer("0");
        } 
        return buffer.toString();
    }
	/**
     * Checks to see if the address given, is in the access control list.
     * @param remoteAddress Address of the remote host performed the 
     * broadcast or request.
     * @return result of the test, false if the host doesn't exist
     * in the ACLand true if they are in the ACL. 
     */
    
    public boolean inACL(InetAddress remoteAddress) {
        
        boolean inTheACL = false;
		String remoteName = remoteAddress.getHostName().toLowerCase();
		String remoteFQDN = remoteAddress.getCanonicalHostName().toLowerCase();
		String address = remoteAddress.getHostAddress();
        if (hostnames.indexOf(remoteName) >= 0 || hostnames.indexOf(remoteFQDN) >= 0) {
			inTheACL = true;
		}
		String testName = hostInfo.getKey(address);
		if (hostnames.indexOf(testName) >= 0) {
			inTheACL = true;
		}
        return inTheACL;
    }
	/**
	 * Takes the String of the message received - date and file hash is already 
	 * stripped.  This method updates the hash if there is anything.
	 * @param hashMessage The hash string of hosts and addresses minus enclosing
	 * brackets.
	 */
	
	public void processIncHash(String hashMessage) {
		
		String buffer = hashMessage;
		String hostName = "";
		String address = "";
		update("raw data " + hashMessage);
		while (buffer.indexOf("=") > 0) {
			address = buffer.substring(0, buffer.indexOf("="));
			update("address: " + address);
			buffer = buffer.substring(buffer.indexOf("=") + 1);
			if (buffer.indexOf(", ") >= 0) {
				hostName = buffer.substring(0, buffer.indexOf(", "));
				buffer = buffer.substring(buffer.indexOf(", ") + 2);
			} else {
				hostName = buffer;
				buffer = "";
			}
			// strip of FQDN
			if (hostName.indexOf(".") > 0) {
				hostName = hostName.substring(0, hostName.indexOf("."));
			}
			update("host: " + hostName);
			hostInfo.put(hostName, address);
			address = "";	
		}
	}
	/**
	 * returns the string of a hash from the KeyValuePair
	 */
	
	public String getHostHashStr() {
		
		return hostInfo.toString();
	}
	/**
	 * updates the debug window in the GUI of the application.
	 * @param message string to send to DebugWindow.	  
  	 */

	private void update(String message){
	
		if (debugger != null) {
			debugger.update(message);
		}
	}
}
