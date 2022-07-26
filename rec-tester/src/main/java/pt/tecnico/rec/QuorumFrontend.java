package pt.tecnico.rec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.tecnico.rec.domain.ResponseCollector;
import pt.tecnico.rec.grpc.Rec;
import pt.tecnico.rec.grpc.RecordServiceGrpc;
import pt.tecnico.rec.grpc.Rec.CleanRequest;
import pt.tecnico.rec.grpc.Rec.CleanResponse;
import pt.tecnico.rec.grpc.Rec.PingRequest;
import pt.tecnico.rec.grpc.Rec.PingResponse;
import pt.tecnico.rec.grpc.Rec.ReadRequest;
import pt.tecnico.rec.grpc.Rec.ReadResponse;
import pt.tecnico.rec.grpc.Rec.WriteRequest;
import pt.tecnico.rec.grpc.Rec.WriteResponse;
import pt.tecnico.rec.grpc.RecordServiceGrpc.RecordServiceBlockingStub;
import pt.tecnico.rec.grpc.RecordServiceGrpc.RecordServiceStub;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;
import pt.ulisboa.tecnico.sdis.zk.ZKRecord;

public class QuorumFrontend {
    
    List<RecordServiceBlockingStub> stubs = new ArrayList<RecordServiceBlockingStub>();
    List<RecordServiceStub> stubs2 = new ArrayList<RecordServiceStub>();
    List<ManagedChannel> channels = new ArrayList<ManagedChannel>();
    

    public QuorumFrontend(String zooHost, String zooPort) throws ZKNamingException {

        ZKNaming zkNaming = new ZKNaming(zooHost,zooPort);
        final String path = "/grpc/bicloin/rec";
        Collection<ZKRecord> listRecords = zkNaming.listRecords(path);
        String target;
        ManagedChannel channel;
        RecordServiceBlockingStub stub;
        RecordServiceStub stub2;

        for (ZKRecord record : listRecords) {
            target = record.getURI();
            channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
            channels.add(channel);
            
            stub = RecordServiceGrpc.newBlockingStub(channel);
            stubs.add(stub);
            stub2 = RecordServiceGrpc.newStub(channel);
            stubs2.add(stub2);
            
            System.out.println("Created connection with rec server at target " + target);
        }
    }

    public PingResponse ping(PingRequest request){
        PingResponse response = null;
        ResponseCollector respCollector = new ResponseCollector<>();
        for (RecordServiceStub stub : this.stubs2){
            NewObserver<PingResponse> observer = new NewObserver<PingResponse>(respCollector);
            //response = stub.ping(request);
            //stub.ping(request, observer);
            respCollector = observer.getResponseCollector();
        }
        return response;
    }

    public WriteResponse write(WriteRequest request){
        WriteResponse response = null;
        ResponseCollector respCollector = new ResponseCollector<>();
        NewObserver<WriteResponse> observer = new NewObserver<WriteResponse>(respCollector);
        synchronized(this){
            for (RecordServiceStub stub : this.stubs2){
                
                //response = stub.write(request);
                stub.write(request, observer);
                respCollector = observer.getResponseCollector();
                System.out.println("Wrote record in rec " + stub);   
            }
            this.notifyAll();
        }
        response = observer.getWriteResponse();
        return response;
    }

    public ReadResponse read(ReadRequest request){
        ReadResponse response = null;
        ResponseCollector respCollector = new ResponseCollector<>();
        NewObserver<ReadResponse> observer = new NewObserver<ReadResponse>(respCollector);
        synchronized(this){
            for (RecordServiceStub stub : this.stubs2){
                stub.read(request, observer);    
                System.out.println("Getting read response from " + stub);            
            }
            this.notifyAll();
        }

        while(observer.getReadResponse() == null){}

        respCollector = observer.getResponseCollector();
        response = observer.getReadResponse();
        
        return response;
    }

    public CleanResponse clean(CleanRequest request){
        CleanResponse response = null;
        ResponseCollector respCollector = new ResponseCollector<>();
        for (RecordServiceStub stub : this.stubs2){
            //response = stub.clean(request);
            stub.clean(request, new NewObserver<CleanResponse>(respCollector));
        }
        return response;
    }

    public Collection<ZKRecord> getServers(String zooHost, String zooPort) throws ZKNamingException{
        ZKNaming zkNaming = new ZKNaming(zooHost,zooPort);
        final String path = "/grpc/bicloin/rec";
        Collection<ZKRecord> listRecords = zkNaming.listRecords(path);
        return listRecords;
    }

    public void shutdownChannel() {
        for (ManagedChannel channel : this.channels){
            channel.shutdownNow();
        }
    }
}