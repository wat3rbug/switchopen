import java.io.FileInputStream;
import java.security.MessageDigest;
 
public class CheckSum {
 
	// class variables
	
	
	// methods
	
	public static String CheckSum(String filename) {
		
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
			// do what?
		}
		return buffer.toString();
	}
}
