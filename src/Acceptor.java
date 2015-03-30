import java.util.*;

public class Acceptor extends Process {
	BallotNumber ballot_number = null;
	Set<PValue> accepted = new HashSet<PValue>();

	public Acceptor(Env env, ProcessId me, String logFolder) {
		super(logFolder, env, me, null);
		this.env = env;
		this.me = me;
		env.addProc(me, this);
	}

	public void body(){
		for (;;) {
			PaxosMessage msg = getNextMessage();

			if (msg instanceof P1aMessage) {
				P1aMessage m = (P1aMessage) msg;

				if (ballot_number == null ||
						ballot_number.compareTo(m.ballot_number) < 0) {
					ballot_number = m.ballot_number;
				}
				sendMessage(m.src, new P1bMessage(me, ballot_number, new HashSet<PValue>(accepted)));
			}
			else if (msg instanceof P2aMessage) {
				P2aMessage m = (P2aMessage) msg;

				if (ballot_number == null ||
						ballot_number.compareTo(m.ballot_number) <= 0) {
					ballot_number = m.ballot_number;
					
					// Search for the pValue, if it is already there, then go and update
					// the command field.
					boolean flag = true;
					for (PValue pv : accepted) {
						if (pv.ballot_number == ballot_number && pv.slot_number == m.slot_number) {
							Command pvCommand = pv.command;
							pvCommand.client = m.command.client;
							pvCommand.op = m.command.op;
							pvCommand.req_id = m.command.req_id;
							flag = false;
						}
					}
					if (flag) {
						accepted.add(new PValue(ballot_number, m.slot_number, m.command));
					}
				}
				sendMessage(m.src, new P2bMessage(me, ballot_number, m.slot_number));
			}
		}
	}
}
