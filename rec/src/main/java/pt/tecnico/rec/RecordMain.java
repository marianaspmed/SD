package pt.tecnico.rec;

import java.io.IOException;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

public class RecordMain {
	
	public static void main(String[] args) throws IOException, InterruptedException {
		System.out.println(RecordMain.class.getSimpleName());
		
		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		
		}

		ZKNaming zkNaming = null;
		
		final String zookeeperServer = args[0];
		final String zookeeperPort = args[1];
		final String currentServer = args[2];
		final String currentPort = args[3];
		
		final int instance = Integer.parseInt(args[4]);
		final String path = "/grpc/bicloin/rec/" + instance;
		
		final int port = Integer.parseInt(args[3]);
		final BindableService impl = new RecServiceImpl();
	
		try {

			zkNaming = new ZKNaming(zookeeperServer, zookeeperPort);

			zkNaming.rebind(path,currentServer,currentPort);

			// Create a new server to listen on port
			Server server = ServerBuilder.forPort(port).addService(impl).build();

			// Start the server
			server.start();
		
			// Server threads are running in the background.
			System.out.println("Server started at port " + port);
		
			// Do not exit the main thread. Wait until server is terminated.
			server.awaitTermination();		
		
		} catch (ZKNamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (zkNaming != null) {
				// remove
				try {
					zkNaming.unbind(path,currentServer,currentPort);
				} catch (ZKNamingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}


		
	
		

	}	
}
