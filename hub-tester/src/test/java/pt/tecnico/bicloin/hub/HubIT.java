package pt.tecnico.bicloin.hub;

import org.junit.jupiter.api.*;

import io.grpc.ManagedChannel;
import pt.tecnico.bicloin.hub.grpc.Hub.BalanceRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.BikeDownRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.BikeUpRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.InfoStationRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.InitRecRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.LocateStationRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.PingRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.PingResponse;
import pt.tecnico.bicloin.hub.grpc.Hub.SetBalanceRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.SetHasBikeRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.SetLatRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.SetLonRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.SysStatusRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.SysStatusResponse;
import pt.tecnico.bicloin.hub.grpc.Hub.TopUpRequest;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class HubIT {
	
	private static final String TEST_PROP_FILE = "/test.properties";
	protected static Properties testProps;
	protected static HubFrontend frontend;
	
	
	// one-time initialization and clean-up
	@BeforeAll
	public static void oneTimeSetUp() throws IOException, NumberFormatException, ZKNamingException{
		testProps = new Properties();
		
		try {
			testProps.load(HubIT.class.getResourceAsStream(TEST_PROP_FILE));
			System.out.println("Test properties:");
			System.out.println(testProps);
			
			frontend = new HubFrontend(testProps.getProperty("server.host"), testProps.getProperty("server.port"),1,
			testProps.getProperty("user.username"),testProps.getProperty("user.phonenumber"),Float.parseFloat(testProps.getProperty("user.lat")),
			Float.parseFloat(testProps.getProperty("user.lon")));



		}catch (IOException e) {
			final String msg = String.format("Could not load properties file {}", TEST_PROP_FILE);
			System.out.println(msg);
			throw e;
		}
		
	}
	
	@AfterAll
	public static void oneTimeTearDown() {
		frontend.shutdownChannel();
	}
	
	// initialization and clean-up for each test
	
	@BeforeEach
	public void setUp() {

		SetBalanceRequest request = SetBalanceRequest.newBuilder().setUsername(frontend.getUsername()).setValue(0).build();
		frontend.setBalance(request);
		SetHasBikeRequest hasBikerequest = SetHasBikeRequest.newBuilder().setUsername(frontend.getUsername()).build();
		frontend.setHasBike(hasBikerequest);
	}
	
	@AfterEach
	public void tearDown() {
		
	}
	
    @Test
	public void pingOKTest() {
		
		PingRequest request = PingRequest.newBuilder().build();

		PingResponse response = frontend.ping(request);
		assertEquals("UP", response.getOutput());
	}	

   /* @Test
	public void sysStatusOKTest() {
		
		SysStatusRequest request = SysStatusRequest.newBuilder().build();

		SysStatusResponse response = frontend.sysStatus(request);
		assertEquals("Hub - UP", response.getOutput(0));
		assertEquals("Rec - UP", response.getOutput(1));
	}	
	*/
	
	@Test
	public void getBalanceZeroSuccess() {

		BalanceRequest request = BalanceRequest.newBuilder().setUsername(frontend.getUsername()).build();
		int balance = frontend.balance(request).getBalance();
		assertEquals(0,balance);
		
	}

	@Test
	public void topUpSuccess() {

		TopUpRequest topUpRequest = TopUpRequest.newBuilder().setPhoneNumber(frontend.getPhoneNumber()).setUsername(frontend.username).setAmount(10).build();
		frontend.topUp(topUpRequest);

		BalanceRequest request = BalanceRequest.newBuilder().setUsername(frontend.getUsername()).build();
		int balance = frontend.balance(request).getBalance();
		assertEquals(100,balance);
		
	}

	@Test
	public void topUpMoreThanMax() {

		TopUpRequest topUpRequest = TopUpRequest.newBuilder().setPhoneNumber(frontend.getPhoneNumber()).setUsername(frontend.username).setAmount(25).build();
		frontend.topUp(topUpRequest);

		BalanceRequest request = BalanceRequest.newBuilder().setUsername(frontend.getUsername()).build();
		int balance = frontend.balance(request).getBalance();
		assertEquals(0,balance);
	}

	@Test
	public void infoStationSuccess() {

		InfoStationRequest infoStationRequest = InfoStationRequest.newBuilder().setAbrv("istt").build();
		String stationName = frontend.infoStation(infoStationRequest).getName();
		float lat = frontend.infoStation(infoStationRequest).getLatitude();
		float lon = frontend.infoStation(infoStationRequest).getLongitude();
		int nDocks = frontend.infoStation(infoStationRequest).getDocksCapacity();
		int nBikes = frontend.infoStation(infoStationRequest).getAvailableBikes();
		int award = frontend.infoStation(infoStationRequest).getAward();

		assertEquals("IST Taguspark",stationName);
		assertEquals("38.7372", String.format("%.4f",lat));
		assertEquals("-9.3023",String.format("%.4f",lon));
		assertEquals(20,nDocks);
		assertEquals(12,nBikes);
		assertEquals(4,award);
		
	}

	@Test
	public void infoStationNonExistent() {

		InfoStationRequest infoStationRequest = InfoStationRequest.newBuilder().setAbrv("abcd").build();
		String stationName = frontend.infoStation(infoStationRequest).getName();
		float lat = frontend.infoStation(infoStationRequest).getLatitude();
		float lon = frontend.infoStation(infoStationRequest).getLongitude();
		int nDocks = frontend.infoStation(infoStationRequest).getDocksCapacity();
		int nBikes = frontend.infoStation(infoStationRequest).getAvailableBikes();
		int award = frontend.infoStation(infoStationRequest).getAward();

		assertEquals("",stationName);
		assertEquals(0, lat);
		assertEquals(0,lon);
		assertEquals(0,nDocks);
		assertEquals(0,nBikes);
		assertEquals(0,award);
	}

	@Test
	public void locateTwoStations() {

		LocateStationRequest locateStationRequest = LocateStationRequest.newBuilder().setK(2).setLatitude(frontend.getLatitude()).setLongitude(frontend.getLongitude()).build();
		List<String> list = new ArrayList<String>();

		list = frontend.locateStation(locateStationRequest).getStationsIdsList();

		assertEquals(2,list.size());
	}

	@Test
	public void locateZeroStations() {

		LocateStationRequest locateStationRequest = LocateStationRequest.newBuilder().setK(0).setLatitude((float) 10.234).setLongitude((float) -5.123).build();
		List<String> list = new ArrayList<String>();

		list = frontend.locateStation(locateStationRequest).getStationsIdsList();

		assertEquals(0,list.size());
	}

	@Test
	public void setLatitude() {

		SetLatRequest setLatRequest = SetLatRequest.newBuilder().setLat((float) 38.7633).setUsername(frontend.getUsername()).build();
		
		String status = frontend.setLat(setLatRequest).getStatus();
		assertEquals("OK",status);
	}

	@Test
	public void setLongitude() {

		SetLonRequest setLonRequest = SetLonRequest.newBuilder().setLon((float) -9.0950).setUsername(frontend.getUsername()).build();
		
		String status = frontend.setLon(setLonRequest).getStatus();
		assertEquals("OK",status);
	}

	@Test
	public void bikeUpSucess() {

		TopUpRequest topUpRequest = TopUpRequest.newBuilder().setPhoneNumber(frontend.getPhoneNumber()).setUsername(frontend.username).setAmount(10).build();
		frontend.topUp(topUpRequest);

		BikeUpRequest bikeUpRequest = BikeUpRequest.newBuilder().setAbrv("ocea").setLatitude((float) 38.7633).setLongitude((float) -9.0950).setUsername(frontend.getUsername()).build();
		
		String status = frontend.bikeUp(bikeUpRequest).getStatus();

		assertEquals("OK",status);
	}

	@Test
	public void bikeUpFailNotEnoughMoney() {

		BikeUpRequest bikeUpRequest = BikeUpRequest.newBuilder().setAbrv("ocea").setLatitude((float) 38.7633).setLongitude((float) -9.0950).setUsername(frontend.getUsername()).build();
		String status = frontend.bikeUp(bikeUpRequest).getStatus();

		assertEquals("ERRO utilizador nao tem BiCloins suficientes",status);
	}

	@Test
	public void bikeUpFailAlreadyHasBike() {


		TopUpRequest topUpRequest = TopUpRequest.newBuilder().setPhoneNumber(frontend.getPhoneNumber()).setUsername(frontend.username).setAmount(10).build();
		frontend.topUp(topUpRequest);

		BikeUpRequest bikeUpRequest = BikeUpRequest.newBuilder().setAbrv("ocea").setLatitude((float) 38.7633).setLongitude((float) -9.0950).setUsername(frontend.getUsername()).build();
		String status = frontend.bikeUp(bikeUpRequest).getStatus();

		assertEquals("OK",status);
		
		status = frontend.bikeUp(bikeUpRequest).getStatus();
		assertEquals("ERRO utilizador ja se encontra com uma bicicleta",status);
	}

	@Test
	public void bikeUpFailOutOfRange() {


		TopUpRequest topUpRequest = TopUpRequest.newBuilder().setPhoneNumber(frontend.getPhoneNumber()).setUsername(frontend.username).setAmount(10).build();
		frontend.topUp(topUpRequest);

		BikeUpRequest bikeUpRequest = BikeUpRequest.newBuilder().setUsername(frontend.getUsername()).setAbrv("ista").setLatitude(frontend.getLatitude()).setLongitude(frontend.getLongitude()).build();
		String status = frontend.bikeUp(bikeUpRequest).getStatus();

		assertEquals("ERRO fora de alcance",status);
	}

	@Test
	public void bikeDownSucess() {

		TopUpRequest topUpRequest = TopUpRequest.newBuilder().setPhoneNumber(frontend.getPhoneNumber()).setUsername(frontend.username).setAmount(10).build();
		frontend.topUp(topUpRequest);

		BikeUpRequest bikeUpRequest = BikeUpRequest.newBuilder().setAbrv("ocea").setLatitude((float) 38.7633).setLongitude((float) -9.0950).setUsername(frontend.getUsername()).build();
		
		frontend.bikeUp(bikeUpRequest);

		BikeDownRequest bikeDownRequest = BikeDownRequest.newBuilder().setAbrv("ocea").setLatitude((float) 38.7633).setLongitude((float) -9.0950).setUsername(frontend.getUsername()).build();
		
		String status = frontend.bikeDown(bikeDownRequest).getStatus();

		assertEquals("OK",status);
	}

	@Test
	public void bikeDownFailHasNoBike() {

		BikeDownRequest bikeDownRequest = BikeDownRequest.newBuilder().setAbrv("ocea").setLatitude((float) 38.7633).setLongitude((float) -9.0950).setUsername(frontend.getUsername()).build();
		String status = frontend.bikeDown(bikeDownRequest).getStatus();

		assertEquals("ERRO utilizador nao levantou uma bicicleta",status);
	}

	@Test
	public void bikeDownFailOutOfRange() {

		TopUpRequest topUpRequest = TopUpRequest.newBuilder().setPhoneNumber(frontend.getPhoneNumber()).setUsername(frontend.username).setAmount(10).build();
		frontend.topUp(topUpRequest);

		BikeUpRequest bikeUpRequest = BikeUpRequest.newBuilder().setAbrv("ocea").setLatitude((float) 38.7633).setLongitude((float) -9.0950).setUsername(frontend.getUsername()).build();
		
		frontend.bikeUp(bikeUpRequest);

		BikeDownRequest bikeDownRequest = BikeDownRequest.newBuilder().setUsername(frontend.getUsername()).setAbrv("ista").setLatitude(frontend.getLatitude()).setLongitude(frontend.getLongitude()).build();
		String status = frontend.bikeDown(bikeDownRequest).getStatus();

		assertEquals("ERRO fora de alcance",status);
	}

}
