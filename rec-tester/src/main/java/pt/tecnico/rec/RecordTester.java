package pt.tecnico.rec;

import pt.tecnico.rec.grpc.Rec.PingResponse;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

public class RecordTester {
	
	public static void main(String[] args) throws ZKNamingException {
		System.out.println(RecordTester.class.getSimpleName());
		
		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		//RecordFrontend frontend = new RecordFrontend(args[2], args[3], 1);
		QuorumFrontend q_frontend = new QuorumFrontend(args[2], args[3]);

		PingResponse response = q_frontend.ping(null);
		System.out.println(response);

		//frontend.shutdownChannel();
		q_frontend.shutdownChannel();

	}
	
}
