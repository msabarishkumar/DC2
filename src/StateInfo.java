import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

public class StateInfo {
	
	public StateInfo(String filename) throws FileNotFoundException, IOException {
		Properties prop = new Properties();
		prop.load(new FileInputStream(filename));
		
		numClients = loadInt(prop, "NumClients");
		for (int i=0; i < numClients; i++) {
			new Client("client" + i);
		}
	}
	
	// Helper function.
	private int loadInt(Properties prop, String s) {
		return Integer.parseInt(prop.getProperty(s.trim()));
	}
	
	// Total number of clients;
	public int numClients;
	
	// Map from client name to the client object.
	public final HashMap<String,Client> clients = new HashMap<String, Client>();
}