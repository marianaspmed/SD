package pt.tecnico.bicloin.hub.domain;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Collection;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import pt.tecnico.bicloin.hub.Exceptions.NoSuchStationException;
import pt.tecnico.rec.QuorumFrontend;
import pt.tecnico.rec.grpc.Rec;
import pt.tecnico.rec.grpc.RecordServiceGrpc;
import pt.tecnico.rec.grpc.Rec.CleanRequest;
import pt.tecnico.rec.grpc.RecordServiceGrpc.RecordServiceBlockingStub;

import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;
import pt.ulisboa.tecnico.sdis.zk.ZKRecord;

public class HubNetwork {
    
	private Map<String, HubStation> stations = new HashMap<>();
    private QuorumFrontend frontend;
    
    public HubNetwork() {

    }
    
    public HubNetwork(String fileName, QuorumFrontend frontend) throws NumberFormatException, IOException {
        BufferedReader br = new BufferedReader(new FileReader(fileName));  
        String line = "";
        String name;
        String abrv;
        float lat;
        float lon;
        int n_docks;
        int n_bikes;
        int award;
        this.frontend = frontend;

        while ((line = br.readLine()) != null){
            String split_line[] = line.split(",");
            name = split_line[0];
            abrv = split_line[1];
            lat = Float.parseFloat(split_line[2]);
            lon = Float.parseFloat(split_line[3]);
            n_docks = Integer.parseInt(split_line[4]);
            n_bikes = Integer.parseInt(split_line[5]);
            award = Integer.parseInt(split_line[6]);

            HubStation station = new HubStation(name,abrv,lat,lon,n_docks,n_bikes,award, frontend);

            stations.put(abrv, station);
        }

        br.close();
    }

    public ArrayList<String> systemStatus(){

        ArrayList<String> servers = new ArrayList<String>();

        servers.add("Hub - UP");
        try {
            Collection<ZKRecord> listRecords = frontend.getServers("localhost","2181");
            for (ZKRecord record : listRecords){

                Rec.PingRequest pingRequest = Rec.PingRequest.newBuilder().build();
                servers.add("Rec - " + record.getURI() + " " + this.frontend.ping(pingRequest).getOutput());
            }
        } catch (ZKNamingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        
		
		return servers;
	}

    public void initRec(){
        CleanRequest cleanRequest = Rec.CleanRequest.newBuilder().build();
        this.frontend.clean(cleanRequest);
    }

    public HubStation getStationById(String abrv) throws NoSuchStationException{
        if (!stations.containsKey(abrv)) {
            throw new NoSuchStationException("ERROR station not found");
        }
        return stations.get(abrv);
    }

    public List<String> locateStation(float lat, float lon, int k){
    	Map<String, Float> distancesMap = new HashMap<>();
    	List<String> newList = new ArrayList<String>();
    
    	// create hash map with station ids and distance to the user
    	for(String abrv: stations.keySet()) {
    		HubStation station;
            try {
                station = this.getStationById(abrv);
                float d = Haversine.distanceFloat(lat, lon, station.getLat(), station.getLon());
                distancesMap.put(abrv, d);
            } catch (NoSuchStationException e) {
                
            }

    	}
    	
    	// sort hash map by value (distance)
    	List<Map.Entry<String, Float> > linkedlist= new LinkedList<Map.Entry<String, Float> >(distancesMap.entrySet());
         Collections.sort(linkedlist, new Comparator<Map.Entry<String, Float> >() {
             public int compare(Map.Entry<String, Float> o1, 
                                Map.Entry<String, Float> o2)
             {
                 return (o1.getValue()).compareTo(o2.getValue());
             }
         });
         //HashMap<String, Float> finalMap = new LinkedHashMap<String, Float>();
         for (Map.Entry<String, Float> elem : linkedlist) {
        	 //finalMap.put(elem.getKey(), elem.getValue());
        	 newList.add(elem.getKey());
         }
         
    	// gets k first stations closest to the user
    	int i = 0;
        List<String> returnList = new ArrayList<String>();

        for(String abrv: newList) {
            if(i < k) {
                returnList.add(abrv);
    		}
            i++;
    	}
    	
    	return returnList;
    }
}
