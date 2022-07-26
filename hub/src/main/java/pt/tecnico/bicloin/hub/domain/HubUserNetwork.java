package pt.tecnico.bicloin.hub.domain;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import pt.tecnico.rec.QuorumFrontend;
import pt.tecnico.rec.grpc.Rec.CleanRequest;
import pt.tecnico.rec.grpc.RecordServiceGrpc.RecordServiceBlockingStub;

public class HubUserNetwork {

	private Map<String, HubUser> users = new HashMap<>();
	
	public HubUserNetwork() {
        
    }
	
	public HubUserNetwork(String fileName, QuorumFrontend frontend) throws NumberFormatException, IOException {
        BufferedReader br = new BufferedReader(new FileReader(fileName));  
        String line = "";
        String username;
        String name;
        String phoneNumber;

        while ((line = br.readLine()) != null){
            String split_line[] = line.split(",");
            username = split_line[0];
            name = split_line[1];
            phoneNumber = split_line[2];

            HubUser user;
            try {
                user = new HubUser(username, name, phoneNumber, frontend);
                users.put(username, user);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            
        }

        br.close();
    }
	
	public HubUser getUserByUsername(String username){
        return users.get(username);
    }
}
