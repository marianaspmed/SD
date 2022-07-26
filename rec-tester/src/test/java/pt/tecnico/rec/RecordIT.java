package pt.tecnico.rec;

import org.junit.jupiter.api.*;

import pt.tecnico.rec.grpc.Rec.CleanRequest;
import pt.tecnico.rec.grpc.Rec.PingRequest;
import pt.tecnico.rec.grpc.Rec.PingResponse;
import pt.tecnico.rec.grpc.Rec.ReadRequest;
import pt.tecnico.rec.grpc.Rec.WriteRequest;
import pt.tecnico.rec.grpc.Rec.WriteResponse;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.Properties;

public class RecordIT {

	private static final String TEST_PROP_FILE = "/test.properties";

	private static RecordFrontend frontend;
	private static QuorumFrontend old_frontend;
	protected static Properties testProps;

	// one-time initialization and clean-up
	@BeforeAll
	public static void oneTimeSetup () throws IOException, ZKNamingException {
		testProps = new Properties();

		try {
			testProps.load(RecordIT.class.getResourceAsStream(TEST_PROP_FILE));
			frontend = new RecordFrontend(testProps.getProperty("zoo.host"), testProps.getProperty("zoo.port"),1);
			old_frontend = new QuorumFrontend(testProps.getProperty("zoo.host"), testProps.getProperty("zoo.port"));



		}catch (IOException e) {
			final String msg = String.format("Could not load properties file {}", TEST_PROP_FILE);
			System.out.println(msg);
			throw e;
		}
	}

	@AfterAll
	public static void oneTimeTearDown() {

		//frontend.shutdownChannel();

	}


	@BeforeEach
	public void setUp() {

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

	@Test
	public void WriteOKTest() {

		WriteRequest request = WriteRequest.newBuilder().setName("balance").setValue("30").build();

		WriteResponse response = frontend.write(request);
		assertEquals("OK", response.getStatus());

	}

	@Test
	public void ReadOkTest() {

		WriteRequest request = WriteRequest.newBuilder().setName("balance").setValue("30").build();

		frontend.write(request);

		ReadRequest readRequest = ReadRequest.newBuilder().setName("balance").build();

		String result = frontend.read(readRequest).getValue();
		assertEquals("30", result);

	}

	@Test
	public void ReadNOkTest() {
		CleanRequest requestClean = CleanRequest.newBuilder().build();
		frontend.clean(requestClean);

		ReadRequest readRequest = ReadRequest.newBuilder().setName("balance").build();

		String result = frontend.read(readRequest).getValue();
		assertEquals("", result);
	}

}
