package pt.tecnico.rec;

import io.grpc.stub.StreamObserver;
import pt.tecnico.rec.domain.RecordStructure;
import pt.tecnico.rec.grpc.Rec.PingRequest;
import pt.tecnico.rec.grpc.Rec.PingResponse;
import pt.tecnico.rec.grpc.Rec.ReadRequest;
import pt.tecnico.rec.grpc.Rec.ReadResponse;
import pt.tecnico.rec.grpc.Rec.WriteRequest;
import pt.tecnico.rec.grpc.Rec.WriteResponse;
import pt.tecnico.rec.grpc.Rec.CleanRequest;
import pt.tecnico.rec.grpc.Rec.CleanResponse;
import pt.tecnico.rec.grpc.RecordServiceGrpc.RecordServiceImplBase;



public class RecServiceImpl extends RecordServiceImplBase{
	
	RecordStructure registry = new RecordStructure();
    
    @Override
    public void ping(PingRequest request, StreamObserver<PingResponse> responseObserver) {
        String output = "UP";
        PingResponse response = PingResponse.newBuilder().setOutput(output).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void read(ReadRequest request, StreamObserver<ReadResponse> responseObserver) {
        
    	ReadResponse response = ReadResponse.newBuilder().setValue(registry.getValueByName(request.getName())).
		build();

        // Send a single response through the stream.
		responseObserver.onNext(response);
		// Notify the client that the operation has been completed.
		responseObserver.onCompleted();
        
    }
    
    @Override
    public void write(WriteRequest request, StreamObserver<WriteResponse> responseObserver) {
    	
    	WriteResponse response = WriteResponse.newBuilder().setStatus(registry.addRecord(request.getName(), request.getValue())).
    	build();
    	
    	// Send a single response through the stream.
    	responseObserver.onNext(response);
    	// Notify the client that the operation has been completed.
    	responseObserver.onCompleted();
    	
    }

    //@Override
    public void clean(CleanRequest request, StreamObserver<CleanResponse> responseObserver) {

        CleanResponse response = CleanResponse.newBuilder().setStatus(registry.cleanRegistry()).
                build();

        // Send a single response through the stream.
        responseObserver.onNext(response);
        // Notify the client that the operation has been completed.
        responseObserver.onCompleted();

    }
}