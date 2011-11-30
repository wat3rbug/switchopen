import java.util.ArrayList;

/* I will make this more generic when I import it to the library */

public class KeyValuePair {
	
	// attributes
	
	private static ArrayList<String> names = new ArrayList<String>();
	private static ArrayList<String> addresses = new ArrayList<String>();
	private DebugWindow debugger;
	
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
			updateDebug("Constructor: host " + name + " address " + address);
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
	 * returns the name based on the address sent.
	 * @param address Address associated with the hostname.
	 */
	
	public String getKey(String address) {
		
		int index = addresses.indexOf(address);
		if (index < 0) {
			return "not found";
		} else {
			return names.get(index);
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
			addresses.set(index, ipAddress);
		} else {
			names.add(host);
			addresses.add(ipAddress);
		}
	}
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
	 * Removes a key/value set from the simple hash-like table.
	 * @param address The address used to remove the host / address.
	 */
	
	public void remove(String address) {
		
		int index = addresses.indexOf(address);
		if (index < 0) {
			updateDebug(address + " wasn't found");
			return;
		} else {
			names.remove(index);
			addresses.remove(index);
		}
	}
	/**
	 * builds a simple output of the key value pair as String text.
	 */
	
	public String toString() {
		
		// fix this cause empty is a failure
		if (names.isEmpty()) {
			return "empty";
		}
		String output = "{";
		for (int i = 0; i < names.size(); i++) {
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
	
	private void updateDebug(String message) {
	
		if (debugger != null) {
			debugger.update(message);
		}
	}
}