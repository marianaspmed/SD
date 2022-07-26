package pt.tecnico.bicloin.app;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.protobuf.Descriptors.FieldDescriptor;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.tecnico.bicloin.hub.grpc.HubServiceGrpc;
import pt.tecnico.bicloin.hub.grpc.Hub.BalanceRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.SetLatRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.SetLonRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.SysStatusRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.GetLatRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.GetLonRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.GetDistanceRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.BikeDownRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.BikeUpRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.InfoStationRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.LocateStationRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.PingRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.TopUpRequest;
import pt.tecnico.bicloin.hub.grpc.HubServiceGrpc.HubServiceBlockingStub;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;
import pt.ulisboa.tecnico.sdis.zk.ZKRecord;

public class App {

    String username;
    String phone_number;
    Float lat;
    Float lon;
    HubServiceBlockingStub stub;
    Map<String,float[]> tags = new HashMap<String,float[]>();
    String url = "https://www.google.com/maps/place/";

    public App(String username, String phone_number, Float lat, Float lon, HubServiceBlockingStub stub){
        this.username = username;
        this.phone_number = phone_number;
        this.stub = stub;
        setUserLat(lat);
        setUserLon(lon);
    }

    public void setStub(HubServiceBlockingStub stub){
        this.stub = stub;
    }

    public String restartHub() throws ZKNamingException{

        final String hub_path = "/grpc/bicloin/hub/2";

        ZKNaming zkNaming = new ZKNaming("localhost", "2181");

        ZKRecord record = zkNaming.lookup(hub_path);
		String target = record.getURI();

        final ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
		HubServiceBlockingStub stub = HubServiceGrpc.newBlockingStub(channel);  

        this.stub = stub; 
        
        //create new hub

        return "Connected to new hub";
    } 
    

    public String ping(){
        
        PingRequest pingRequest = PingRequest.newBuilder().build();
        
        return this.stub.ping(pingRequest).getOutput();
    }

    public void sysStatus(){
        
        SysStatusRequest sysStatusRequest = SysStatusRequest.newBuilder().build();
        
        for (String result : this.stub.sysStatus(sysStatusRequest).getOutputList()){
            System.out.println(result);
        }
    }

    public void setUserLat(float lat){
        SetLatRequest setLatRequest = SetLatRequest.newBuilder().setUsername(this.username).setLat(lat).build();
        this.stub.setLat(setLatRequest);
    }

    public void setUserLon(float lon){
        SetLonRequest setLonRequest = SetLonRequest.newBuilder().setUsername(this.username).setLon(lon).build();
        this.stub.setLon(setLonRequest);
    }

    public float getUserLat(){
        GetLatRequest getLatRequest = GetLatRequest.newBuilder().setUsername(this.username).build();
        float userLat = this.stub.getLat(getLatRequest).getLat();
        return userLat;
    }

    public float getUserLon(){
        GetLonRequest getLonRequest = GetLonRequest.newBuilder().setUsername(this.username).build();
        float userLon = this.stub.getLon(getLonRequest).getLon();
        return userLon;
    }

    public String getUsername(){
        return this.username;
    }

    public int balance(){
        int balance;

        BalanceRequest balanceRequest = BalanceRequest.newBuilder().setUsername(this.username).build();

        balance = stub.balance(balanceRequest).getBalance();

        return balance;

    }

    public int topUp(int value){
        int balance;

        TopUpRequest topUpRequest = TopUpRequest.newBuilder().setUsername(this.username).setAmount(value).setPhoneNumber(this.phone_number).build();

        balance = stub.topUp(topUpRequest).getBalance();

        return balance;

    }

    public String tag(float lat, float lon, String tagName){

        if (lat != 0 && lon != 0) {
            float[] coord = new float[2];

            coord[0] = lat;
            coord[1] = lon;
            tags.put(tagName,coord);
    
            return "OK";
        } else {
            return "ERROR";
        }

    }

    public String at(){
        return url + getUserLat() + ", " + getUserLon();
    }

    public String move(String tagName){
        float[] coord = new float[2];

        if (!tags.containsKey(tagName)){
            return "Tag não existente";
        }

        coord = tags.get(tagName);
        setUserLat(coord[0]);
        setUserLon(coord[1]);

        return url + getUserLat() + ", " + getUserLon();
    }

    public String move(float lat, float lon){
        setUserLat(lat);
        setUserLon(lon);

        return this.username + " em " + url + lon + ", " + lat;
    }


    public ArrayList<String> scan(int n_stations){

        ArrayList<String> stations = new ArrayList<String>();
        String station_detail;
        LocateStationRequest locateStationRequest = LocateStationRequest.newBuilder().setK(n_stations).setLatitude(getUserLat()).setLongitude(getUserLon()).build();

        for (String abrv : this.stub.locateStation(locateStationRequest).getStationsIdsList()) {
            InfoStationRequest infoStationRequest = InfoStationRequest.newBuilder().setAbrv(abrv).build();
            GetDistanceRequest getDistanceRequest = GetDistanceRequest.newBuilder().setAbrv(abrv).setUserLat(this.getUserLat()).setUserLon(this.getUserLon()).build();

            station_detail = abrv + ", lat " + this.stub.infoStation(infoStationRequest).getLatitude() + ", " + this.stub.infoStation(infoStationRequest).getLongitude()
            + " long, " + this.stub.infoStation(infoStationRequest).getDocksCapacity() + " docas," + this.stub.infoStation(infoStationRequest).getAward() + "BIC prémio, " +
            this.stub.infoStation(infoStationRequest).getAvailableBikes() + " bicicletas, a " + this.stub.getDistance(getDistanceRequest).getDistance() + " metros";

            stations.add(station_detail);
        }

        return stations;
    }

    public String info(String abrv){

        InfoStationRequest infoStationRequest = InfoStationRequest.newBuilder().setAbrv(abrv).build();

        if (this.stub.infoStation(infoStationRequest).getName() == ""){
            return "Estação inexistente";
        }

        String station_detail = this.stub.infoStation(infoStationRequest).getName() + ", lat " + this.stub.infoStation(infoStationRequest).getLatitude() + ", " + this.stub.infoStation(infoStationRequest).getLongitude()
            + " long, " + this.stub.infoStation(infoStationRequest).getDocksCapacity() + " docas, " + this.stub.infoStation(infoStationRequest).getAward() + " BIC prémio, " +
            this.stub.infoStation(infoStationRequest).getAvailableBikes() + " bicicletas, " + this.stub.infoStation(infoStationRequest).getBikeUp() + " levantamentos, " +
            this.stub.infoStation(infoStationRequest).getBikeDown() + " devoluções, " + url + this.stub.infoStation(infoStationRequest).getLatitude() + ", " + this.stub.infoStation(infoStationRequest).getLongitude();

        return station_detail;
    }

    public String bikeUp(String abrv){
        
        BikeUpRequest bikeUpRequest = BikeUpRequest.newBuilder().setUsername(this.username).setLatitude(getUserLat()).setLongitude(getUserLon()).setAbrv(abrv).build();

        return this.stub.bikeUp(bikeUpRequest).getStatus();
    }

    public String bikeDown(String abrv){
        
        BikeDownRequest bikeDownRequest = BikeDownRequest.newBuilder().setUsername(this.username).setLatitude(getUserLat()).setLongitude(getUserLon()).setAbrv(abrv).build();

        return this.stub.bikeDown(bikeDownRequest).getStatus();
    }

    public void help(){

        System.out.println("Lista de comandos da aplicação Bicloin:");
        System.out.println("");
        System.out.println("balance - permite consultar o saldo de bicloins; retorna o saldo");
        System.out.println("top-up - recebe como input o valor incrementa o saldo de bicloins com o valor fornecido; retorna o saldo");
        System.out.println("tag - recebe como input os valores da latitude, longitude e nome da tag e cria uma tag com as coordenadas; retorna OK se correr com succeso, ERRO caso contrário");
        System.out.println("move - recebe como input os valores da nova latitude e nova longitude, ou da tag criada previamente e atualiza a posição; retorna a nova posição do utilizador");
        System.out.println("at - retorna a posição do utilizador");
        System.out.println("scan - recebe como input o número de estações; retorna as n estações mais próximas com detalhes de cada");
        System.out.println("info - recebe como input o id de uma estação; retorna os detalhes dessa estação");
        System.out.println("bike-up - recebe como input o id de uma estação e efectua o levantamento de uma bicicleta na estação; retorna OK se correr com succeso, ERRO caso contrário");
        System.out.println("bike-up - recebe como input o id de uma estação e efectua a devolução de uma bicicleta na estação; retorna OK se correr com succeso, ERRO caso contrário");
        System.out.println("ping - retorna UP se o servidor estiver operacional");
        System.out.println("sys_status - retorna o estado de todos os servidores");
        System.out.println("exit - sai da aplicação");
        
    }
}