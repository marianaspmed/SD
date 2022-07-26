package pt.tecnico.bicloin.hub;

import pt.tecnico.bicloin.hub.grpc.Hub.PingResponse;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

public class HubTester {
	
	public static void main(String[] args) throws ZKNamingException {
		System.out.println(HubTester.class.getSimpleName());
		
		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}
		HubFrontend frontend = new HubFrontend(args[2], args[3], 1, null, null, 0, 0);

		PingResponse response = frontend.ping(null);
		System.out.println(response);

		frontend.shutdownChannel();
	}	
}
