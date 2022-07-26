package pt.tecnico.bicloin.hub;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.tecnico.bicloin.hub.grpc.Hub.BalanceRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.BalanceResponse;
import pt.tecnico.bicloin.hub.grpc.Hub.BikeDownRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.BikeDownResponse;
import pt.tecnico.bicloin.hub.grpc.Hub.BikeUpRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.BikeUpResponse;
import pt.tecnico.bicloin.hub.grpc.Hub.InfoStationRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.InfoStationResponse;
import pt.tecnico.bicloin.hub.grpc.Hub.InitRecRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.InitRecResponse;
import pt.tecnico.bicloin.hub.grpc.Hub.LocateStationRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.LocateStationResponse;
import pt.tecnico.bicloin.hub.grpc.Hub.PingRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.PingResponse;
import pt.tecnico.bicloin.hub.grpc.Hub.SetBalanceRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.SetBalanceResponse;
import pt.tecnico.bicloin.hub.grpc.Hub.SetHasBikeRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.SetHasBikeResponse;
import pt.tecnico.bicloin.hub.grpc.Hub.SetLatRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.SetLatResponse;
import pt.tecnico.bicloin.hub.grpc.Hub.SetLonRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.SetLonResponse;
import pt.tecnico.bicloin.hub.grpc.Hub.SysStatusRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.SysStatusResponse;
import pt.tecnico.bicloin.hub.grpc.Hub.TopUpRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.TopUpResponse;
import pt.tecnico.bicloin.hub.grpc.HubServiceGrpc;
import pt.tecnico.bicloin.hub.grpc.HubServiceGrpc.HubServiceBlockingStub;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;
import pt.ulisboa.tecnico.sdis.zk.ZKRecord;

public class HubFrontend {

    HubServiceBlockingStub stub;
    ManagedChannel channel;
    final String username;
    final String phone_number;
    final float lat;
    final float lon;

    public HubFrontend(String zooHost, String zooPort, int instance, String username, String phone_number,float lat,float lon){
        final String path = "/grpc/bicloin/hub/" + instance;
    
        ZKNaming zkNaming = new ZKNaming(zooHost,zooPort);
        System.out.println(path);

        ZKRecord record;
        this.username = username;
        this.phone_number = phone_number;
        this.lat = lat;
        this.lon = lon;
        try {
            record = zkNaming.lookup(path);
            String target = record.getURI();
        
            final ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
                
            HubServiceBlockingStub stub = HubServiceGrpc.newBlockingStub(channel);   

            this.channel = channel;
            this.stub = stub;   
        } catch (ZKNamingException e) {
        }
        
    }

    public String getUsername(){
        return this.username;
    }

    public String getPhoneNumber(){
        return this.phone_number;
    }

    public float getLatitude(){
        return this.lat;
    }

    public float getLongitude(){
        return this.lon;
    }

    public PingResponse ping(PingRequest request){
        return this.stub.ping(request);
    }

    public SysStatusResponse sysStatus(SysStatusRequest request){
        return this.stub.sysStatus(request);
    }

    public BalanceResponse balance(BalanceRequest request) {
        return this.stub.balance(request);
    }

    public TopUpResponse topUp(TopUpRequest request) {
        return this.stub.topUp(request);
    }

    public InfoStationResponse infoStation(InfoStationRequest request) {
        return this.stub.infoStation(request);
    }

    public LocateStationResponse locateStation(LocateStationRequest request) {
        return this.stub.locateStation(request);
    }

    public BikeUpResponse bikeUp(BikeUpRequest request) {
        return this.stub.bikeUp(request);
    }

    public BikeDownResponse bikeDown(BikeDownRequest request) {
        return this.stub.bikeDown(request);
    }

    public SetLatResponse setLat(SetLatRequest request) {
        return this.stub.setLat(request);
    }

    public SetLonResponse setLon(SetLonRequest request) {
        return this.stub.setLon(request);
    }

    public InitRecResponse initRec(InitRecRequest request) {
        return this.stub.initRec(request);
    }

    public SetBalanceResponse setBalance(SetBalanceRequest request) {
        return this.stub.setBalance(request);
    }

    public SetHasBikeResponse setHasBike(SetHasBikeRequest request) {
        return this.stub.setHasBike(request);
    }

    public void shutdownChannel() {
        
       this.channel.shutdownNow();
    }
    
}
