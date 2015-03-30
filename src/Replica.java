import java.util.*;

public class Replica extends Process {
	public StateInfo appState;
	ProcessId[] leaders;
	int slot_num = 1;
	Map<Integer /* slot number */, Command> proposals = new HashMap<Integer, Command>();
	Map<Integer /* slot number */, Command> decisions = new HashMap<Integer, Command>();

	// Testing variables.
	// myLeader variable can be used for testing the network partition case.
	Integer myLeader;
	
	public Replica(Env env, StateInfo appState, ProcessId me, ProcessId[] leaders, String logFolder,
			Integer myLeader) {
		super(logFolder, env, me, null);
		this.appState = appState;
		this.leaders = leaders;
		
		this.myLeader = myLeader;
		env.addProc(me, this);
	}

	void propose(Command c) {
		Message msg = (Message)c.op;

		if (msg.type == MessageType.INQUIRY) {
			// TODO: brining up a new machine after allClear command.
		} else if (!decisions.containsValue(c)) {
			// TODO
		}
	}

	void perform(Command c){
		// TODO: process the decisions
	}

	public void body(){
		for (;;) {
			PaxosMessage msg = getNextMessage();

			if (msg instanceof RequestMessage) {
				RequestMessage m = (RequestMessage) msg;				
				propose(m.command);
			} else if (msg instanceof DecisionMessage) {				
				DecisionMessage m = (DecisionMessage) msg;
				if (decisions.containsKey(m.slot_number)) {
					Command x = decisions.get(m.slot_number);
					x.client = m.command.client;
					x.op = m.command.op;
					x.req_id = m.command.req_id;
				} else {
					decisions.put(m.slot_number, m.command);
				}
				
				
				for (;;) {
					Command c = decisions.get(slot_num);
					if (c == null) {
						break;
					}
					if (c.client == null) {
						break;
					}
					
					Command c2 = proposals.get(slot_num);
					if (c2 != null && !c2.equals(c)) {
						propose(c2);
					}
					perform(c);
				}
			}
			else {
				System.err.println("Replica: unknown msg type");
			}
		}
	}
}
