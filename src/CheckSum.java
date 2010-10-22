import java.io.*;
import java.security.MessageDigest;
 
public class CheckSum {
 
	// class variables

	byte [] mdbytes = null;
	
	// constructors
	
	public CheckSum(String filename) {
		
		try {
			MessageDigest md = MessageDigest.getInstance("SHA1");
	    	FileInputStream fis = new FileInputStream(filename);
			byte[] dataBytes = new byte[1024];
			int nread = 0;
			while ((nread = fis.read(dataBytes)) != -1) {
				md.update(dataBytes, 0, read);
			}
			mdbytes = md.digest();
		} catch (Exception e) {
			// ignore til I figure out what to do
		}
	}
	// methods
	
	public String update() {
		
		// convert byte to hex and return
		
		StringBuffer buffer = new StringBuffer("");
		for (int i = 0; i <mdbytes.length; i++) {
			sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).subString(1));
		}
	} 
	return sb.toString();
}