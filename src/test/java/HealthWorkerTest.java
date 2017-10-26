import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import pdfgeneratorapi.Config;
import pdfgeneratorapi.utils.GenericResponse;
import pdfgeneratorapi.views.logic.HealthWorker;

@SuppressWarnings({ "unchecked"})
@RunWith(PowerMockRunner.class)
@PrepareForTest(Config.class)
public class HealthWorkerTest {
	
	private static final String TEST_APP_NAME = "unittest-app-name";

	@BeforeClass
	public static void statics() throws Exception {
		// Prepare the environment for Config. Just need to do these once as they only get read once
		
		Map<String, String> environment = new HashMap<String, String>(8);
		// Health
		environment.put("APP_NAME", TEST_APP_NAME);
		environment.put("COMMIT", "LOCAL");
		// Health+Spark
		environment.put("PORT", "8080");
		// Database
		environment.put("JDBC_URL", "X");
		environment.put("JDBC_USER", "X");
		environment.put("JDBC_PASSWORD", "X");
		environment.put("JDBC_POOL_MAX", "0");
		environment.put("MAX_HEALTH_CASCADE", "0");
		TestUtils.setEnv(environment);
		
		// DB Uncomment if app is DB-enabled
		// TestUtils.mockDataSource();
	}

	/**
	 * Tests our base set of return values
	 */
	@Test
	public void testBasicBody() {
		HealthWorker theWorker = new HealthWorker();

		// Prepare input
		Map<String, String> headers = new HashMap<String, String>(0);
		
		Map<String, String> urlParams = new HashMap<String, String>(1);
		urlParams.put(":str_depth", "0");

		// Do it
		GenericResponse<Map<String, Object>> theResponse = theWorker.process(null, urlParams, null, headers);
		
		// Check OK response
		assertEquals(200, theResponse.httpCode);
		assertEquals("application/json", theResponse.responseBodyContentType);
		
		// Check what we want to check
		assertNotNull("No response body", theResponse.responseBodyObject);
		assertEquals("Incorrect number of fields in response", 4, theResponse.responseBodyObject.size());
		
		assertThat("Headers should be a list", theResponse.responseBodyObject.get("headers"), instanceOf(List.class));
		List<List<String>> outHeaders = (List<List<String>>)theResponse.responseBodyObject.get("headers");
		assertEquals("Incorrect number of reflected headers", 0, outHeaders.size());
		
		assertEquals("Incorrect app name", TEST_APP_NAME, theResponse.responseBodyObject.get("app"));
		assertEquals("Incorrect commit", "LOCAL", theResponse.responseBodyObject.get("commit"));
		assertEquals("Incorrect status", "OK", theResponse.responseBodyObject.get("status"));
	}
	
	/**
	 * Tests that if a header is provided in the request it comes back in the response
	 */
	@Test
	public void testReflectedHeaders() {
		HealthWorker theWorker = new HealthWorker();

		// Prepare input
		Map<String, String> headers = new HashMap<String, String>(1);
		headers.put("MyHeader", "MyValue");
		
		Map<String, String> urlParams = new HashMap<String, String>(1);
		urlParams.put(":str_depth", "0");

		// Do it
		GenericResponse<Map<String, Object>> theResponse = theWorker.process(null, urlParams, null, headers);

		// Check what we want to check
		assertThat("Headers should be a list", theResponse.responseBodyObject.get("headers"), instanceOf(List.class));
		List<List<String>> outHeaders = (List<List<String>>)theResponse.responseBodyObject.get("headers");
		assertEquals("Incorrect number of reflected headers", 1, outHeaders.size());
		assertEquals("Incorrect reflected header key", "MyHeader", outHeaders.get(0).get(0));
		assertEquals("Incorrect reflected header value", "MyValue", outHeaders.get(0).get(1));
	}
	
	/**
	 * Tests that if null header map is provided in the request, an empty list still comes back in the response
	 */
	@Test
	public void testNullHeaders() {
		HealthWorker theWorker = new HealthWorker();
		Map<String, String> urlParams = new HashMap<String, String>(1);
		urlParams.put(":str_depth", "0");

		GenericResponse<Map<String, Object>> theResponse = theWorker.process(null, urlParams, null, null);

		// Check what we want to check
		assertEquals("Incorrent number of reflected headers", 0, ((List<List<String>>) theResponse.responseBodyObject.get("headers")).size());		

	}
}
