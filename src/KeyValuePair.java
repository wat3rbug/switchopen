import java.util.ArrayList;
import java.net.InetAddress;
import java.net.UnknownHostException;

/** 
 * A generic hash-like data structure.  I needed a hash
 * that does two way look ups and this is the quickest I coulddo.
 * @author Douglas Gardiner
 */

public class KeyValuePair {
	
	// attributes
	
	private static ArrayList<String> addresses = new ArrayList<String>();
	private static ArrayList<String> hostnames = new ArrayList<String>();
	private static final String NEXT = ", ";
	private static final String DEFAULT_IP = "0.0.0.0";
	private DebugWindow debugger = null;
	
	// constructors
	
	/**
	 * Constructor with an empty set.
	 */
	
	public KeyValuePair() {
		
	}	
	/**
	 * Constructor with empty set and reference to the debug window.
	 * @param debug The reference to the DebugWindow.
	 */
	
	public KeyValuePair(DebugWindow debugRef) {
		
		debugger = debugRef;
		update("Constructor");
	}
	/**
	 * Constructor that allows for adding with a long hash-like
	 * toString() output as input.  Format is {key=value, ...}
	 * @param input the hash-like input of {key=value, key2=value2, ...}
	 */
	
	public KeyValuePair(String input) {
		
		String buffer = input.substring(1, input.indexOf("}") - 1);
		String line = "";
		String tmpName = "";
		String tmpAddress = "";
		do {
			line = buffer.substring(0, buffer.indexOf(NEXT));
			buffer = buffer.substring(buffer.indexOf(NEXT + NEXT.length()));
			tmpName = line.substring(0, line.indexOf("="));
			hostnames.add(tmpName);
			tmpAddress = line.substring(line.indexOf("=") + 1);
			addresses.add(tmpAddress);
			update("Constructor: host " + tmpName + " address " + tmpAddress);
		} while (buffer.indexOf(NEXT) >= 0);
	}
	/**
	 * Constructor that allows for adding with a long hash-like
	 * toString() output as input.  Format is {key=value, ...}
	 * @param debug The reference to the DebugWindow.
	 * @param input the hash-like input of {key=value, key2=value2, ...}
	 */
	
	public KeyValuePair(DebugWindow debug, String input) {
		
		debugger = debug;
		new KeyValuePair(input);
	}
	// methods
	
	/**
	 * returns true if the keypair has something like the string
	 * sent to it.
	 * @param name The string to test if there is an entry for it.
	 */
	
	public boolean containsValue(String passedHostName) {
		
		int index = addresses.indexOf(passedHostName);
		if (index < 0) {
			return false;
		} else {
			return true;
		}
	}
	/**
	 * returns the name based on the address sent.
	 * @param address Address associated with the hostname.
	 */
	
	public String getKey(String passedAddress) {
		
		int index = addresses.indexOf(passedAddress);
		if (index < 0) {
			return "not found";
		} else {
			return hostnames.get(index);
		}
	}
	/** 
	 * returns the IP address depending on what is sent.
	 * @param name  The key to use for returning a value.
	 */
	
	public String getValue(String passedHostName) {
		
		int index = hostnames.indexOf(passedHostName);
		if (index < 0) {
			return "not found";
		} else {
			return addresses.get(index);
		}
	}	
	/**
	 * adds to the array if there isn't something already in
	 * otherwise it just updates it.
	 * @param name is the host name for the key of the key/value pair.
	 * @address is the IP address for the host for the key/value pair.
	 */
	
	public void put(String host, String ipAddress) {
		
		int index = hostnames.indexOf(host);
		if ( index >= 0 && !ipAddress.equals(DEFAULT_IP)) {
			addresses.set(index, ipAddress);
		} else {
			hostnames.add(host);
			addresses.add(ipAddress);
		}
	}
	/**
	 * Removes a key/value set from the simple hash-like table.
	 * @param address The address used to remove the host / address.
	 */
	
	public void remove(String passedAddress) {
		
		int index = addresses.indexOf(passedAddress);
		if (index < 0) {
			update(passedAddress + " wasn't found");
			return;
		} else {
			hostnames.remove(index);
			addresses.remove(index);
		}
	}
	/**
	 * builds a simple output of the key value pair as String text.
	 * It updates the local address during output.
	 */
	
	public String toString() {
		
		// fix this cause empty is a failure
		if (hostnames.isEmpty()) {
			return "{}";
		}
		String hostName = "";
		String hostFQDN = "";
		String localAddress = "";
		try {
			hostName = InetAddress.getLocalHost().getHostName();
			hostFQDN = InetAddress.getLocalHost().getCanonicalHostName();
			localAddress = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException uhe) {
			// disregard if not found
		}
		// failsafe if no address given
		if (localAddress.isEmpty() || localAddress.equals("")) {
			localAddress = DEFAULT_IP;
		}
		String output = "{";
		String testName = "";
		for (int i = 0; i < hostnames.size(); i++) {
			
			// strip FQDN off for test
			if (hostnames.get(i).indexOf(".") > 0) {
				testName = hostnames.get(i).substring(0, hostnames.get(i).indexOf("."));
			} else {
				testName = hostnames.get(i);
			}
			// test to see if name matches local name
			if (testName.equals(hostName) || testName.equals(hostFQDN)) {
				addresses.set(i, localAddress);
			}
			output += addresses.get(i) + "=" + hostnames.get(i);
			if (i + 1 < hostnames.size()) {
				output += ", ";
			}
		}
		return output + "}";
	}
	/**
	 * Updates the debug window with the message sent to it.
	 * @param message The message to display at the debug window.
	 */
	
	private void update(String message) {
	
		if (debugger != null) {
			debugger.update(message);
		}
	}
}