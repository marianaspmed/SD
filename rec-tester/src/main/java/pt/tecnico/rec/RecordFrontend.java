package pt.tecnico.rec;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.tecnico.rec.grpc.Rec.CleanRequest;
import pt.tecnico.rec.grpc.Rec.CleanResponse;
import pt.tecnico.rec.grpc.Rec.PingRequest;
import pt.tecnico.rec.grpc.Rec.PingResponse;
import pt.tecnico.rec.grpc.Rec.ReadRequest;
import pt.tecnico.rec.grpc.Rec.ReadResponse;
import pt.tecnico.rec.grpc.Rec.WriteRequest;
import pt.tecnico.rec.grpc.Rec.WriteResponse;
import pt.tecnico.rec.grpc.Rec.CleanRequest;
import pt.tecnico.rec.grpc.Rec.CleanResponse;
import pt.tecnico.rec.grpc.RecordServiceGrpc;
import pt.tecnico.rec.grpc.RecordServiceGrpc.RecordServiceBlockingStub;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;
import pt.ulisboa.tecnico.sdis.zk.ZKRecord;

public class RecordFrontend {

    final RecordServiceBlockingStub stub;
    final ManagedChannel channel;

    public RecordFrontend(String zooHost, String zooPort, int instance) throws ZKNamingException {

        final String path = "/grpc/bicloin/rec/" + instance;
    
        ZKNaming zkNaming = new ZKNaming(zooHost,zooPort);
        ZKRecord record = zkNaming.lookup(path);
        String target = record.getURI();

        final ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        
		RecordServiceBlockingStub stub = RecordServiceGrpc.newBlockingStub(channel);   

        this.channel = channel;
        this.stub = stub;
    }

    public PingResponse ping(PingRequest request){
        return this.stub.ping(request);
    }

    public WriteResponse write(WriteRequest request){
        return this.stub.write(request);
    }

    public ReadResponse read(ReadRequest request){
        return this.stub.read(request);
    }

    public CleanResponse clean(CleanRequest request){
        return this.stub.clean(request);
    }

    public void shutdownChannel() {
        
       this.channel.shutdownNow();
    }
    
}
