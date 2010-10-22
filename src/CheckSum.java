import java.io.FileInputStream;
import java.security.MessageDigest;
 
public class CheckSum {
 
	// class variables

	byte [] mdbytes = null;
	
	// constructors
	
	public CheckSum(String filename) {
		
		MessageDigest md = null;
		try {	
			md = MessageDigest.getInstance("SHA1");
	    	FileInputStream fis = new FileInputStream(filename);
			byte[] dataBytes = new byte[1024];
			int nread = 0;
			while ((nread = fis.read(dataBytes)) != -1) {
				md.update(dataBytes, 0, nread);
			}
			mdbytes = md.digest();
		} catch (Exception ioe) {
			
		}
	}
	// methods
	
	public String update() {
		
		// convert byte to hex and return
		
		StringBuffer buffer = new StringBuffer("");
		for (int i = 0; i < mdbytes.length; i++) {
			buffer.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
		}
		return buffer.toString();
	} 
}