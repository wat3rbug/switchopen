// Created by: Douglas Gardiner
// Creation Date: Fri Oct 22 13:21:14 CDT 2010
// Update Date: Sat Oct 23 08:07:26 CDT 2010
//
//@author Douglas Gardiner

/** Used for getting a checksum hash of a file
 * @author Douglas Gardiner
 */

import java.io.*;
import java.security.MessageDigest;
import java.util.*;
import java.net.*;

 
public class Checks {
 
	// class variables
	
	private boolean debug = false;
	private boolean aclPresent = true;
	private String hostFile = "hosts.txt";
    private ArrayList<String> hostnames = new ArrayList<String>();
	private Debug debugger = null;
	
	/* This method loads up the access list of hosts that this application will respond to.  It returns false if there is none.
		It is a security check with default deny.
	*/
	
	// constructors
	
	/**
	 *  Creates an object with a reference to the debugging window.
	 * @param passedframe Reference to the debugging window.
	 */
	public Checks(Debug passedframe) {
		
		debug = true;
		debugger = passedframe;
		new Checks();
	}
	/**
	 *  Creates an object to store the allowable hosts
	 */	
	
    public Checks() {
    
        /* this returns false if it cannot load the names.  
		 * The intent is to shut down the server if it doesn't have a ACL file 
		 */
		
        try{
            File hostFileHandle = new File(hostFile);
            BufferedReader reader = new BufferedReader(new FileReader(hostFileHandle));
            String inLine = null;
            while ((inLine = reader.readLine()) != null) {
                hostnames.add(inLine.toLowerCase());
                if (debug) debugger.update(inLine + " allowed");
            }
            if (hostnames.isEmpty()) aclPresent = false;
        } catch (IOException ioe) {
            if (debug) debugger.update(" --- ACL not updated, shutting down server");
            aclPresent = false;
        }
    }
	
	// methods
	
	/**
	 * checks to see if the the ACL file exists and was read.
	 * @return aclPresent true if things worked out / false if the process failed for any reason
	 */
	
	public boolean exists() {
	
		return aclPresent;
	}
	/**
	 * Checks to see if the address given, is in the access control list.
	 * @param remoteAddress Address of the machine that performed the broadcast or request.
	 * @return boolean result of the test, false if the host doesn't exist in the ACLand true if they are in the ACL. 
	 */
	
	public boolean inACL(InetAddress remoteAddress) {
		
		boolean inTheACL = false;
        for (int i = 0 ; i < hostnames.size(); i++) {
            if (remoteAddress.getHostName().toLowerCase().equals(hostnames.get(i))) inTheACL = true;
        }
		return inTheACL;
	}
	
/**
 * returns the String representation of the SHA1 hash of the filename as a String
 * @return buffer - String representation of the SHA1 hash in hexidecial form.
 * @param filename String representation of the filename to be used.
 */

	public static String update(String filename) {
		
		StringBuffer buffer = new StringBuffer("");
		try {	
			MessageDigest md = MessageDigest.getInstance("SHA1");
	    	FileInputStream fis = new FileInputStream(filename);
			byte[] dataBytes = new byte[1024];
			
			int nread = 0;
			while ((nread = fis.read(dataBytes)) != -1) {
				md.update(dataBytes, 0, nread);
			}
			byte[] mdbytes = md.digest();
			for (int i = 0; i < mdbytes.length; i++) {
				buffer.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
			}
			
		} catch (Exception ioe) {
			// do what? file failed send 0 out
			buffer= new StringBuffer("0");
		} 
		return buffer.toString();
	}
}
