package pt.tecnico.bicloin.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.util.Scanner;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.tecnico.bicloin.hub.grpc.HubServiceGrpc;
import pt.tecnico.bicloin.hub.grpc.Hub.PingRequest;
import pt.tecnico.bicloin.hub.grpc.HubServiceGrpc.HubServiceBlockingStub;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;
import pt.ulisboa.tecnico.sdis.zk.ZKRecord;

public class AppMain {

	/** Set flag to true to print debug messages. 
	 * The flag can be set using the -Ddebug command line option. */
	private static final boolean DEBUG_FLAG = (System.getProperty("debug") != null);

	/** Helper method to print debug messages. */
	private static void debug(String debugMessage) {
		if (DEBUG_FLAG)
			System.err.println(debugMessage);
	}

	static ZKNaming zkNaming = null;
	
	public static void main(String[] args) {
		System.out.println(AppMain.class.getSimpleName());

		// receive and print arguments
		System.out.println(String.format("Received %d arguments", args.length));
		for (int i = 0; i < args.length; i++) {
			System.out.println(String.format("arg[%d] = %s", i, args[i]));
		}

		// check arguments
		if (args.length < 6) {
			System.err.println("Argument(s) missing!");
			System.err.printf("Usage: java %s host port%n", AppMain.class.getName());
			return;
		}

		final String zooHost = args[0];
		final String zooPort = args[1];
		final String name = args[2];
		final String phone_number = args[3];
		final float lat = Float.parseFloat(args[4]);
		final float lon = Float.parseFloat(args[5]);
		String filename = "";

		zkNaming = new ZKNaming(zooHost, zooPort);

		final String hub_path = "/grpc/bicloin/hub/1";
    
		ZKRecord record;
		try {
			record = zkNaming.lookup(hub_path);
			String target = record.getURI();
		
			debug("Target: " + target);
	
			if (args.length > 7) {
				if (args[6].equals("<")){
					filename = args[7];
				}
			}
				
			final ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
			HubServiceBlockingStub stub = HubServiceGrpc.newBlockingStub(channel);   

			App app = new App(name,phone_number,lat,lon,stub);

			System.out.println("Connected to hub at channel: " + stub.getChannel());

		
			try {
				startApp(app, filename);
			} catch (InterruptedException | FileNotFoundException e) {
				System.out.print("Error starting app");
			}
			
			// A Channel should be shutdown before stopping the process.
			channel.shutdown();
		} catch (ZKNamingException e1) {
		}
	}


	private static void startApp(App app, String filename) throws InterruptedException, FileNotFoundException {
		InputStream source;

		/*
		 * Using try with scanner - ensures the resource is closed in the end, even if
		 * there are exceptions.
		 */

		if (!filename.equals("")){
			source = new FileInputStream(filename);
		} else {
			source = System.in;
		}

		try (Scanner scanner = new Scanner(source)) {

			/* The app loop. */
			do {
				System.out.print("> ");

				String op = scanner.next();

				try {
					app.ping();
				} catch (StatusRuntimeException e) {
					System.out.println("Caught exception with description bla: " + e.getStatus().getDescription());
					System.out.println("Ping não dá");
					try {
						app.restartHub();
					} catch (ZKNamingException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}					
				}

				switch(op){
					case("move"):
						String arg1 = scanner.next();
						try{
							Float.parseFloat(arg1);
							System.out.println(app.move(Float.parseFloat(arg1),Float.parseFloat(scanner.next())));
						} catch(NumberFormatException e) {
							System.out.println(app.move(arg1));
						}
						break;
					case ("tag"):
						String arg2 = scanner.next();
						String arg3 = scanner.next();
						try{
							Float.parseFloat(arg2);
							Float.parseFloat(arg3);
							System.out.println(app.tag(Float.parseFloat(arg2), Float.parseFloat(arg3), scanner.next()));
						}catch(NumberFormatException e){
							System.out.println("Inputs inválidos");
							break;
						}						
						break;
					case("at"):
						System.out.println(app.getUsername() + " em " + app.at());
						break;
					case("scan"):
						for (String station : app.scan(scanner.nextInt())) {
							System.out.println(station);
						}
						break;
					case("balance"):
						System.out.println(app.getUsername() + " " + app.balance() + " BIC");
						break;
					case("top-up"):
						System.out.println(app.getUsername() + " " + app.topUp(scanner.nextInt()) + " BIC");
						break;
					case("info"):
						System.out.println(app.info(scanner.next()));
						break;
					case("bike-up"):
						System.out.println(app.bikeUp(scanner.next()));
						break;
					case("bike-down"):
						System.out.println(app.bikeDown(scanner.next()));
						break;
					case("exit"):
						System.exit(0);
						break;
					case("zzz"):
						Thread.sleep(scanner.nextInt());
						break;
					case("#"):
						break;
					case("ping"):
						System.out.println(app.ping());
						break;
					case("sys_status"):
						app.sysStatus();
						break;
					case("help"):
						app.help();
						break;
					default:
						System.out.println("Método " + op + " não disponível.");
				}

			} while(true);
		}
	}
}

