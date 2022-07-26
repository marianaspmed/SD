package pt.tecnico.rec;

import io.grpc.stub.StreamObserver;
import pt.tecnico.rec.domain.ResponseCollector;
import pt.tecnico.rec.grpc.Rec;
import pt.tecnico.rec.grpc.Rec.ReadResponse;
import pt.tecnico.rec.grpc.Rec.WriteResponse;

public class NewObserver<R> implements StreamObserver<R> {
    ResponseCollector respCollector = new ResponseCollector<R>();
    int index;

    public NewObserver(ResponseCollector responseCollector) {
        this.respCollector = responseCollector;
        this.index = 0;
    }

    public ResponseCollector getResponseCollector() {
        return this.respCollector;
    }

    /*public R getResponse() {
        return (R) this.respCollector.getResponseByRequest(index);
    }*/

    public ReadResponse getReadResponse() {
        for (int i = index; i >= 0; i--){
            if(this.respCollector.getReadResponse(i) != null){
                return this.respCollector.getReadResponse(i);
            }
        }
        return null;
    }

    public WriteResponse getWriteResponse() {
        return this.respCollector.getWriteResponse(index);
    }

    @Override
    public void onNext(R r) {
        // add tag to name and response to value of hashmap
        respCollector.addResponse(index, r.toString());

        if(r instanceof ReadResponse) {
            respCollector.addReadResponse(index, (ReadResponse) r);
        } else if(r instanceof WriteResponse) {
            respCollector.addWriteResponse(index, (WriteResponse) r);
        }
        index++;
        //System.out.println("Received response: " + r);
    }

    @Override
    public void onError(Throwable throwable) {
        System.out.println("Received error: " + throwable);
    }

    @Override
    public void onCompleted() {
        System.out.println("Request completed");
    }
}
