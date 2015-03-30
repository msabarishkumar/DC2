import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.*;

public class Env {
	static Map<ProcessId, Process> procs = new HashMap<ProcessId, Process>();
	public static int nAcceptors = 3, nReplicas = 3, nLeaders = 2, nRequests = 10;
	public static ProcessId[] acceptors;
	public static ProcessId[] replicas;
	public static ProcessId[] leaders;
	
	public static String logFolder;
	public static Properties prop = new Properties();
	
	public static ArrayList<IncomingSocket> socketList = new ArrayList<IncomingSocket>();
			
    synchronized void sendMessage(ProcessId dst, PaxosMessage msg){
        Process p = procs.get(dst);
        if (p != null) {
                p.deliver(msg);
        }
    }
    
	synchronized void addProc(ProcessId pid, Process proc){
		procs.put(pid, proc);
		proc.start();
	}

	synchronized void removeProc(ProcessId pid){
		procs.remove(pid);
	}

	void run(String[] args) throws FileNotFoundException, IOException{
		acceptors = new ProcessId[nAcceptors];
		replicas = new ProcessId[nReplicas];
		leaders = new ProcessId[nLeaders];

		for (int i = 0; i < nAcceptors; i++) {
			acceptors[i] = new ProcessId("acceptor_" + i);
			new Acceptor(this, acceptors[i], logFolder);
		}
		
	
		String replicaLeaderString = prop.getProperty("replicaLeader");
		HashMap<Integer, Integer> replicaLeaderMap = new HashMap<Integer, Integer>();
		if(replicaLeaderString!=null) {
			String[] xx = replicaLeaderString.split(",");
			for (String x : xx) {
				String[] replicaLeader = x.split("-");
				replicaLeaderMap.put(Integer.parseInt(replicaLeader[0]), Integer.parseInt(replicaLeader[1]));
			}
		}
		for (int i = 0; i < nReplicas; i++) {
			replicas[i] = new ProcessId("replica_" + i);
			StateInfo appState = new StateInfo(args[0]);
			Integer myLeader = null;
			myLeader = replicaLeaderMap.get(i);
			new Replica(this, appState, replicas[i], leaders, logFolder, myLeader);
		}
		
		String pingPong = prop.getProperty("PingPong");
		if (pingPong == null) {
			pingPong = "false";
		}
		String failureDetection = prop.getProperty("failureDetection");
		if (failureDetection == null) {
			failureDetection = "false";
		}
		
		for (int i = 0; i < nLeaders; i++) {
			leaders[i] = new ProcessId("leader_" + i);
			new Leader(this, leaders[i], acceptors, replicas, leaders, logFolder, pingPong, failureDetection);
		}
	}

	private static int loadInt(Properties prop, String s) {
		return Integer.parseInt(prop.getProperty(s.trim()));
	}
	
	public static void main(String[] args) throws FileNotFoundException, IOException{
		
		prop.load(new FileInputStream(args[0]));
		
		nAcceptors = loadInt(prop, "Acceptors");
		nReplicas = loadInt(prop, "Replicas");
		nLeaders = loadInt(prop, "Leaders");
		logFolder = prop.getProperty("LOG_FOLDER");
		
		new Env().run(args);
		
		// Start server on the given port.
		final int port = Integer.parseInt(args[1]);
		ServerSocket serverSock;
		try {
			serverSock = new ServerSocket(port);
			System.out.println("Request handler started.");
		} catch (IOException e) {
			String errStr = String.format(
					"Server can't open server port %d", port);
			System.out.println(errStr);
			throw new Error(errStr);
		}
		
		Replica[] replicaProcess = new Replica[nReplicas];
		for (int i = 0; i < nReplicas; i++) {
			replicaProcess[i] = (Replica)procs.get(replicas[i]);
		}

		try {
			while(true) {
				System.out.println("Still waiting..\n");
				IncomingSocket incomingSock = new IncomingSocket(
						serverSock.accept(), replicaProcess);
				socketList.add(incomingSock);
				incomingSock.start();
				System.out.println("New incoming connection accepted from " + incomingSock.sock.getInetAddress());
			}
		} catch (IOException e) {
		} finally {
			try {
				serverSock.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
