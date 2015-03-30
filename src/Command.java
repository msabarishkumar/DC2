public class Command {
	//ProcessId client;
	IncomingSocket client;
	int req_id;
	Object op;
	
	public Command(Command c) {
		this.client = c.client;
		this.req_id = c.req_id;
		this.op = c.op;		
	}
	public Command(IncomingSocket client, int req_id, Object op){
		this.client = client;
		this.req_id = req_id;
		this.op = op;		
	}
	
	public boolean equals(Object o) {
		Command other = (Command) o;
		return client == other.client && req_id == other.req_id && op.equals(other.op);
	}

	public String toString() {
		return "Command(" + client + ", " + req_id + ", " + op + ")";
	}
}
	
