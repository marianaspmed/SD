package pt.tecnico.bicloin.hub.domain;

import pt.tecnico.bicloin.hub.Exceptions.*;
import pt.tecnico.rec.QuorumFrontend;
import pt.tecnico.rec.grpc.RecordServiceGrpc;
import pt.tecnico.rec.grpc.Rec.ReadRequest;
import pt.tecnico.rec.grpc.Rec.WriteRequest;
import pt.tecnico.rec.grpc.RecordServiceGrpc.RecordServiceBlockingStub;

public class HubStation {

    private String name;
    private String abrv;
    private float lat;
    private float lon;
    private int n_docks;
    private int award;
    QuorumFrontend frontend;

    public HubStation(String name, String abrv, float lat, float lon, int n_docks, int n_bikes, int award, QuorumFrontend frontend) {
        this.name = name;
        this.abrv = abrv;
        this.lat = lat;
        this.lon = lon;
        this.n_docks = n_docks;
        this.frontend = frontend;

        setnBikes(n_bikes);
        this.award = award;
        setBikesUp(0);
        setBikesDown(0);
    }

    public String getName() {
        return this.name;
    }

    public String getAbrv() {
        return this.abrv;
    }

    public float getLat() {
        return this.lat;
    }

    public float getLon() {
        return this.lon;
    }

    public int getDocks() {
        return this.n_docks;
    }

    public int getAward() {
        return this.award;
    }

    public int getnBikes() {
        String name = "stations/" + this.getAbrv() + "/n_bikes";
        ReadRequest readRequest = ReadRequest.newBuilder().setName(name).build();
        int n_bikesValue = Integer.parseInt(this.frontend.read(readRequest).getValue());
        return n_bikesValue;
    }

    public void setnBikes(int n_bikesValue) {
        String name = "stations/" + this.getAbrv() + "/n_bikes";
        WriteRequest writeRequest = WriteRequest.newBuilder().setName(name).setValue("" + n_bikesValue).build();
        this.frontend.write(writeRequest);
    }

    public int getBikesUp() {
        String name = "stations/" + this.getAbrv() + "/n_bikes_up";
        ReadRequest readRequest = ReadRequest.newBuilder().setName(name).build();
        String response = this.frontend.read(readRequest).getValue();
        int n_bikes_upValue = 0;
        if(response != ""){
            n_bikes_upValue = Integer.parseInt(response);
        }
        return n_bikes_upValue;
    }

    public void setBikesUp(int n_bikes_upValue) {
        String name = "stations/" + this.getAbrv() + "/n_bikes_up";
        WriteRequest writeRequest = WriteRequest.newBuilder().setName(name).setValue("" + n_bikes_upValue).build();
        this.frontend.write(writeRequest);
    }

    public int getBikesDown() {
        String name = "stations/" + this.getAbrv() + "/n_bikes_down";
        ReadRequest readRequest = ReadRequest.newBuilder().setName(name).build();
        int n_bikes_downValue = 0;
        String response = this.frontend.read(readRequest).getValue();
         if(response != ""){
            n_bikes_downValue = Integer.parseInt(response);
        }
        return n_bikes_downValue;
        
    }

    public void setBikesDown(int n_bikes_downValue) {
        String name = "stations/" + this.getAbrv() + "/n_bikes_down";
        WriteRequest writeRequest = WriteRequest.newBuilder().setName(name).setValue("" + n_bikes_downValue).build();
        this.frontend.write(writeRequest);
    }

    public String bike_up(float userLat, float userLon, HubUser user) throws UserHasBikeException, NotEnoughBalanceException, TooFarException {
        float d;
        d = Haversine.distanceFloat(userLat, userLon, this.lat, this.lon);
        if (user.getHasBike()) {
            throw new UserHasBikeException("ERRO utilizador ja se encontra com uma bicicleta");
        } else if (user.getBalance() < 10) {
            throw new NotEnoughBalanceException("ERRO utilizador nao tem BiCloins suficientes");
        } else if (d > 200) {
            user.setHasBike(false);
            throw new TooFarException("ERRO fora de alcance");
            //return "ERRO fora de alcance";

        } else {
            int userBalance = user.getBalance();
            userBalance -= 10;
            user.setBalance(userBalance);

            //writing n_bikes_up new value to rec
            int n_bikes_upValue = this.getBikesUp();
            n_bikes_upValue++;
            String nameBikeUp = "stations/" + this.getAbrv() + "/n_bikes_up";
            WriteRequest writeRequest = WriteRequest.newBuilder().setName(nameBikeUp).setValue("" + n_bikes_upValue).build();
            this.frontend.write(writeRequest);

            //writing n_bikes new value to rec
            int n_bikes_Value = this.getnBikes();
            n_bikes_Value--;
            String nameNBikes = "stations/" + this.getAbrv() + "/n_bikes";
            WriteRequest writeRequest2 = WriteRequest.newBuilder().setName(nameNBikes).setValue("" + n_bikes_Value).build();
            this.frontend.write(writeRequest2);

            user.setHasBike(true);
            return "OK";
        }
    }

    public String bike_down(float userLat, float userLon, HubUser user) throws FullStationException, NoBikeException {
        if (this.getnBikes() == this.n_docks) {
            throw new FullStationException("ERRO estacao cheia, nao pode devolver bicicleta");
        } else if (!user.getHasBike()) {
            throw new NoBikeException("ERRO utilizador nao levantou uma bicicleta");
        } else {
            float d = Haversine.distanceFloat(userLat, userLon, this.lat, this.lon);
            if (d < 200) {
                user.setHasBike(false);
                int balance = user.getBalance();
                balance += this.getAward();
                user.setBalance(balance);
                //writing n_bikes_down new value to rec
                int n_bikes_downValue = this.getBikesUp();
                n_bikes_downValue++;
                String nameBikeDown = "stations/" + this.getAbrv() + "/n_bikes_down";
                WriteRequest writeRequest = WriteRequest.newBuilder().setName(nameBikeDown).setValue("" + n_bikes_downValue).build();
                this.frontend.write(writeRequest);

                //writing n_bikes new value to rec
                int n_bikes_Value = this.getnBikes();
                n_bikes_Value++;
                String nameNBikes = "stations/" + this.getAbrv() + "/n_bikes";
                WriteRequest writeRequest2 = WriteRequest.newBuilder().setName(nameNBikes).setValue("" + n_bikes_Value).build();
                this.frontend.write(writeRequest2);

                return "OK";
            } else {
                return "ERRO fora de alcance";
            }
        }

    }

    public float getDistance(float userLat, float userLon) {
        float d = Haversine.distanceFloat(userLat, userLon, this.lat, this.lon); //in kms
        return d;
    }
}
