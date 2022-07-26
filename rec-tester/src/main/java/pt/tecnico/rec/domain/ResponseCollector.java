package pt.tecnico.rec.domain;

import pt.tecnico.rec.grpc.Rec;
import pt.tecnico.rec.grpc.Rec.ReadResponse;
import pt.tecnico.rec.grpc.Rec.WriteResponse;

import java.util.HashMap;
import java.util.Map;

public class ResponseCollector<R> {
    private Map<Integer, R> registry = new HashMap<>();
    private Map<Integer, ReadResponse> read_registry = new HashMap<>();
    private Map<Integer, WriteResponse> write_registry = new HashMap<>();

    public ResponseCollector(){

    }

    public R getResponseByRequest(int tag){
        return registry.get(tag);
    }

    public ReadResponse getReadResponse(int tag){
        return read_registry.get(tag);
    }

    public WriteResponse getWriteResponse(int tag){
        return write_registry.get(tag);
    }

    //add or edit existing record
    public String addResponse(int tag, R response) {
        registry.put(tag, response);
        return "OK"; //
    }

    //add or edit existing record
    public String addWriteResponse(int tag, WriteResponse response) {
        
        write_registry.put(tag, response);
        return "OK"; //
    }

    //add or edit existing record
    public String addReadResponse(int tag, ReadResponse response) {
        read_registry.put(tag, response);
        return "OK"; //
    }

    public String cleanRegistry() {
        registry.clear();
        return "OK";
    }

}