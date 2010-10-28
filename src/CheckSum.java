// Created by: Douglas Gardiner
// Creation Date: Fri Oct 22 13:21:14 CDT 2010
// Update Date: Sat Oct 23 08:07:26 CDT 2010
//
//@author Douglas Gardiner

/** Used for getting a checksum hash of a file
 * @author Douglas Gardiner
 */

import java.io.FileInputStream;
import java.security.MessageDigest;
import java.lang.String;

 
public class CheckSum {
 
	// class variables
	
	// methods
	
/**
 * returns the String representation of the SHA1 hash of the filename as a String
 * @return String representation of the SHA1 hash in hexidecial form.
 * @param filename to be used
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
