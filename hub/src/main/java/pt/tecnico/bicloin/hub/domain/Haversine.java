package pt.tecnico.bicloin.hub.domain;

public class Haversine {
	private static final int EARTH_RADIUS = 6371;
	// lat1 & lon1 = start coordinates
	// lat2 & lon2 = end coordinates
	public static float distanceFloat(float lat1, float lon1, float lat2, float lon2) {
		
		float dLat = (float) Math.toRadians((lat2 - lat1));
		float dLon = (float) Math.toRadians((lon2 - lon1));
		
		lat1 = (float) Math.toRadians(lat1);
		lat2 = (float) Math.toRadians(lat2);
		
		float a = (float) (haversin(dLat) + Math.cos(lat1) * Math.cos(lat2) * haversin(dLon));
		float c = (float) (2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)));

		float distance = (float) (EARTH_RADIUS * c * 1000);
		return Math.round(distance); // DISTANCE from kms to meters
	}
	
	public static double distanceDouble(double lat1, double lon1, double lat2, double lon2) {
		
		double dLat = Math.toRadians((lat2 - lat1));
		double dLon = Math.toRadians((lon2 - lon1));
		
		lat1 = Math.toRadians(lat1);
		lat2 = Math.toRadians(lat2);
		
		double a = (haversin(dLat) + Math.cos(lat1) * Math.cos(lat2) * haversin(dLon));
		double c = (2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)));
		
		return EARTH_RADIUS * c * 0.001; // DISTANCE from kms to meters
	}
	
	public static double haversin(double val) {
		return Math.pow(Math.sin(val/2), 2);
	}

}
