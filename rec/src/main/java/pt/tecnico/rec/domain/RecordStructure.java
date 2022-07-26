package pt.tecnico.rec.domain;

import java.util.HashMap;
import java.util.Map;

public class RecordStructure {
private Map<String, String> registry = new HashMap<>();
	
	public RecordStructure() {
        
    }
	
	public String getValueByName(String name){
		if (!registry.containsKey(name)){
			return "";
		}
        return registry.get(name);
    }
	
	//add or edit existing record
	public String addRecord(String name, String value) {
		registry.put(name, value);
		return "OK"; //
	}
	
	public String cleanRegistry() {
		registry.clear();
		return "OK";
	}
}
