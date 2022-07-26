package pt.tecnico.bicloin.hub.domain;

import pt.tecnico.bicloin.hub.Exceptions.InvalidDepositValueException;
import pt.tecnico.bicloin.hub.HubMain;
import pt.tecnico.rec.grpc.Rec.ReadRequest;
import pt.tecnico.rec.grpc.Rec.ReadResponse;
import pt.tecnico.rec.grpc.Rec.WriteRequest;
import pt.tecnico.rec.QuorumFrontend;
import pt.tecnico.rec.grpc.RecordServiceGrpc;
import pt.tecnico.rec.grpc.RecordServiceGrpc.RecordServiceBlockingStub;

public class HubUser {
	private String username;
    private String name;
    private String phoneNumber;
    private boolean hasBike;
    QuorumFrontend frontend;

    public HubUser(String username, String name, String phoneNumber, QuorumFrontend frontend) throws InterruptedException {
        this.username = username;
        this.name = name;
        this.phoneNumber = phoneNumber;
		this.frontend = frontend;
        setBalance(0);
        setHasBike(false);
    }
    
    public String getUserName() {
    	return this.username;
    }
    
    public String getName() {
    	return this.name;
    }
    
    public String getPhoneNumber() {
    	return this.phoneNumber;
    }
    
    public boolean getHasBike() {
    	String name = "usernames/" + this.getUserName() + "/hasBike";

    	
    	ReadRequest readRequest = ReadRequest.newBuilder().setName(name).build();
    	boolean hasBikeValue = Boolean.parseBoolean(this.frontend.read(readRequest).getValue());
    	return hasBikeValue;
    }
    
    public void setHasBike(boolean hasBikeValue) {
    	String name = "usernames/" + this.getUserName() + "/hasBike";
    	WriteRequest writeRequest = WriteRequest.newBuilder().setName(name).setValue("" + hasBikeValue).build();
    	this.frontend.write(writeRequest);
    }
    
    public int getBalance() {
    	String name = "usernames/" + this.getUserName() + "/balance";
    	ReadRequest readRequest = ReadRequest.newBuilder().setName(name).build();
		
    	int balanceValue = Integer.parseInt(this.frontend.read(readRequest).getValue());
		
    	return balanceValue;
    }
    
    public void setBalance(int balanceValue) {
    	String name = "usernames/" + this.getUserName() + "/balance";
		WriteRequest writeRequest = WriteRequest.newBuilder().setName(name).setValue("" + balanceValue).build();
		this.frontend.write(writeRequest);

	}
    
    public float getLat() {
    	String name = "usernames/" + this.getUserName() + "/lat";
    	
    	ReadRequest readRequest = ReadRequest.newBuilder().setName(name).build();
    	float latValue = Float.parseFloat(this.frontend.read(readRequest).getValue());
    	return latValue;
    }
    
    public String setLat(float latValue) {
    	String name = "usernames/" + this.getUserName() + "/lat";
    	WriteRequest writeRequest = WriteRequest.newBuilder().setName(name).setValue("" + latValue).build();
    	this.frontend.write(writeRequest);
    	return "OK";
    }
    
    public float getLon() {
    	String name = "usernames/" + this.getUserName() + "/lon";
    	
    	ReadRequest readRequest = ReadRequest.newBuilder().setName(name).build();
    	float lonValue = Float.parseFloat(this.frontend.read(readRequest).getValue());
    	return lonValue;
    }
    
    public String setLon(float lonValue) {
    	String name = "usernames/" + this.getUserName() + "/lon";
    	WriteRequest writeRequest = WriteRequest.newBuilder().setName(name).setValue("" + lonValue).build();
    	this.frontend.write(writeRequest);
		return "OK";
    }
    
    
    public int topUpBalance(int amount) throws InvalidDepositValueException {
    	if(amount < 1 | amount > 20) {
			throw new InvalidDepositValueException("ERRO valor de deposito deve ser de 1 a 20");
		}

    	String name = "usernames/" + this.getUserName() + "/balance";
    	
    	ReadRequest readRequest = ReadRequest.newBuilder().setName(name).build();
    	int balanceValue = Integer.parseInt(this.frontend.read(readRequest).getValue());
    	
        if (amount <= 20) {
            balanceValue += amount * 10; //BIC conversion from euro
        }
            	
    	WriteRequest writeRequest = WriteRequest.newBuilder().setName(name).setValue("" + balanceValue).build();
    	this.frontend.write(writeRequest);
    	return balanceValue;
    }
    
}
