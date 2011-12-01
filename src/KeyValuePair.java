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
	
	private static ArrayList<String> names = new ArrayList<String>();
	private static ArrayList<String> addresses = new ArrayList<String>();
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
	
	public KeyValuePair(DebugWindow debug) {
		
		debugger = debug;
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
		String name = "";
		String address = "";
		do {
			line = buffer.substring(0, buffer.indexOf(", "));
			buffer = buffer.substring(buffer.indexOf(", " + 2));
			name = line.substring(0, line.indexOf("="));
			names.add(name);
			address = line.substring(line.indexOf("=") + 1);
			addresses.add(address);
			update("Constructor: host " + name + " address " + address);
		} while (buffer.indexOf(",") >= 0);
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
	
	public boolean containsValue(String name) {
		
		int index = addresses.indexOf(name);
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
	
	public String getKey(String address) {
		
		int index = addresses.indexOf(address);
		update("address given: " + address);
		if (index < 0) {
			update("name: not found");
			return "not found";
		} else {
			update("name: " + names.get(index));
			return names.get(index);
		}
	}
	/** 
	 * returns the IP address depending on what is sent.
	 * @param name  The key to use for returning a value.
	 */
	
	public String getValue(String name) {
		
		int index = names.indexOf(name);
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
		
		int index = names.indexOf(host);
		if ( index >= 0) {
			update("updated " + names.get(index) + " to " + ipAddress);
			addresses.set(index, ipAddress);
		} else {
			update("added " + host + " at " + ipAddress + " to KeyValuePair");
			names.add(host);
			addresses.add(ipAddress);
		}
	}
	/**
	 * Removes a key/value set from the simple hash-like table.
	 * @param address The address used to remove the host / address.
	 */
	
	public void remove(String address) {
		
		int index = addresses.indexOf(address);
		if (index < 0) {
			update(address + " wasn't found");
			return;
		} else {
			names.remove(index);
			addresses.remove(index);
		}
	}
	/**
	 * builds a simple output of the key value pair as String text.
	 * It updates the local address during output.
	 */
	
	public String toString() {
		
		// fix this cause empty is a failure
		if (names.isEmpty()) {
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
			localAddress = "0.0.0.0";
		}
		String output = "{";
		String testName = "";
		for (int i = 0; i < names.size(); i++) {
			
			// strip FQDN off for test
			if (names.get(i).indexOf(".") > 0) {
				testName = names.get(i).substring(0, names.get(i).indexOf("."));
			} else {
				testName = names.get(i);
			}
			// test to see if name matches local name
			if (testName.equals(hostName) || testName.equals(hostFQDN)) {
				addresses.set(i, localAddress);
			}
			output += addresses.get(i) + "=" + names.get(i);
			if (i + 1 < names.size()) {
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