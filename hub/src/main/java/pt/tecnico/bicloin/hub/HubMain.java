package pt.tecnico.bicloin.hub;

import java.io.IOException;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import pt.tecnico.bicloin.hub.domain.HubUserNetwork;
import pt.tecnico.bicloin.hub.domain.HubNetwork;
import pt.tecnico.rec.QuorumFrontend;
import pt.tecnico.rec.grpc.Rec;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;
import pt.tecnico.rec.grpc.Rec.CleanRequest;

public class HubMain {

	static HubUserNetwork userNet;
	static HubNetwork stationNet;
	static BindableService impl;

	public static void main(String[] args) throws IOException, InterruptedException {
		System.out.println(HubMain.class.getSimpleName());
		
		//mvn compile exec:java -Dexec.args="arg0 arg1 arg2 arg2 arg3 arg4"
		
		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		final String zookeeperServer = args[0];
		final String zookeeperPort = args[1];
		final String currentServer = args[2];
		final String currentPort = args[3];
		final int instance = Integer.parseInt(args[4]);
		final String usersFile = args[5];
		final String stationsFile = args[6];
		
		final int port = Integer.parseInt(args[3]);
		final String path = "/grpc/bicloin/hub/";
		
		
		ZKNaming zkNaming = null;

		
		try {
			QuorumFrontend frontend = new QuorumFrontend(zookeeperServer, zookeeperPort);
			
			zkNaming = new ZKNaming(zookeeperServer, zookeeperPort);

			zkNaming.rebind(path+1,currentServer,currentPort);

			String currentPort2 = String.valueOf(Integer.parseInt(currentPort) + 1);
			zkNaming.rebind(path+2,currentServer,currentPort2);

			if(args.length > 7) {
				final String initRec = args[7];
				CleanRequest cleanRequest = Rec.CleanRequest.newBuilder().build();
				frontend.clean(cleanRequest);
			}

			//to send the stub
			
			final HubUserNetwork userNet = new HubUserNetwork(usersFile, frontend);
			final HubNetwork stationNet = new HubNetwork(stationsFile, frontend);

			final BindableService impl = new HubServiceImpl(stationNet, userNet);

			// Create a new server to listen on port
			Server server1 = ServerBuilder.forPort(port).addService(impl).build();
			Server server2 = ServerBuilder.forPort(port+1).addService(impl).build();

			// Start the server
			server1.start();
			server2.start();

			// Server threads are running in the background.
			System.out.println("Server started at " + server1.getPort());
			System.out.println("Backup server started at " + server2.getPort());
		
			// Do not exit the main thread. Wait until server is terminated.
			server1.awaitTermination();
			server2.awaitTermination();

		} catch (ZKNamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		} finally {
			if (zkNaming != null) {
				// remove
				try {
					zkNaming.unbind(path+1,currentServer,currentPort);
					zkNaming.unbind(path+2,currentServer,currentPort);
				} catch (ZKNamingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}	
	}
	
}
