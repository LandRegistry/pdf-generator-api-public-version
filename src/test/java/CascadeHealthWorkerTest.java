import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.mashape.unirest.http.Headers;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;

import pdfgeneratorapi.Config;
import pdfgeneratorapi.utils.GenericResponse;
import pdfgeneratorapi.views.logic.CascadeHealthWorker;

/**
 * 
 * Looking for the version of this class that tests the database connection code too?
 * http://192.168.249.38/gadgets/gadget-sync-api/tree/master/src/test/java/CascadeHealthWorkerTest.java
 * You'll need to update the Worker and copy the HealthDAO itself as well.
 * 
 * The relevant changes are also commented // DB
 *
 */
@SuppressWarnings({ "unchecked"})
@RunWith(PowerMockRunner.class)
@PrepareForTest({Config.class, Unirest.class})
public class CascadeHealthWorkerTest extends EasyMockSupport {
	
	private static final String TEST_APP_NAME = "unittest-app-name";
	private static final String SERVICE_API_URL = "http://an-api/";

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
		environment.put("MAX_HEALTH_CASCADE", "6");
		TestUtils.setEnv(environment);
		
		// DB TestUtils.mockDataSource();
	}

	// DB private HealthDAO mockHealthDAO;
	private HttpResponse<String> mockResponse;
	private GetRequest mockRequest;
	private Headers mockHeaders;
	
	@Before
	public void initialize() {
		// To ensure dependencies are not tested unless we choose it
		Config.DEPENDENCIES.remove("DB2");
		Config.DEPENDENCIES.remove("AnAPI");
		
		// Create mocks we may want to use.
		// By default they do not expect any methods called on them.
		// A test will need to set expectations before calling replayAll().
		// DB mockHealthDAO = mock(HealthDAO.class);
		mockResponse = mock(HttpResponse.class);
		mockRequest = mock(GetRequest.class);
		mockHeaders = mock(Headers.class);
		
		PowerMock.mockStatic(Unirest.class); // Needs PrepareForTest annotation at top of class
	}
	
	@After
	public void after() {
		// Remove all expectations from mocks and put them back into record mode
		resetAll();
		PowerMock.resetAll();
	}
	
	/**
	 * Convenience method to replay PowerMock mocks (like UniRest) as well as the EasyMock ones
	 */
	@Override
	public void replayAll() {
		super.replayAll();
		PowerMock.replayAll();
	}
	
	/**
	 * Convenience method to verify PowerMock mocks (like UniRest) as well as the EasyMock ones
	 */
	@Override
	public void verifyAll() {
		super.verifyAll();
		PowerMock.verifyAll();
	}

	/**
	 * Tests our base set of return values when the route is called. No DBs or services.
	 */
	@Test
	public void testBasicBody() {
		// Not expecting any mocks to be called due to JDBC_URL set above
		// Switch mocks to replay mode
		replayAll();
		
		// DB CascadeHealthWorker theWorker = new CascadeHealthWorker(mockHealthDAO);
		CascadeHealthWorker theWorker = new CascadeHealthWorker();

		// Prepare input
		Map<String, String> headers = new HashMap<String, String>(0);
		
		Map<String, String> urlParams = new HashMap<String, String>(1);
		urlParams.put(":str_depth", "0");

		// Do it
		GenericResponse<Map<String, Object>> theResponse = theWorker.process(null, urlParams, null, headers);
		
		// Check what we want to check
		assertEquals("Incorrect overall status code", 200, theResponse.httpCode);
		assertEquals("Incorrect response content type", "application/json", theResponse.responseBodyContentType);
		
		assertEquals("Incorrect number of fields in response", 8, theResponse.responseBodyObject.size());
		
		assertEquals("Incorrect app name", TEST_APP_NAME, theResponse.responseBodyObject.get("app"));
		assertEquals("Incorrect commit", "LOCAL", theResponse.responseBodyObject.get("commit"));
		assertEquals("Incorrect status", "OK", theResponse.responseBodyObject.get("status"));
		assertEquals("Incorrect cascade depth", 0, theResponse.responseBodyObject.get("cascade_depth"));
		
		assertThat("Headers should be a list", theResponse.responseBodyObject.get("headers"), instanceOf(List.class));
		List<List<String>> outHeaders = (List<List<String>>)theResponse.responseBodyObject.get("headers");
		assertEquals("Incorrect number of reflected headers", 0, outHeaders.size());

		List<List<String>> services = (List<List<String>>)theResponse.responseBodyObject.get("services");
		assertEquals("Incorrect number of services", 0, services.size());
		
		List<Map<String, Object>> dbs = (List<Map<String, Object>>)theResponse.responseBodyObject.get("db");
		assertEquals("Incorrect number of databases", 0, dbs.size());
		
		// Check mocks were called with the right values and right number of times
		verifyAll();
	}
	
	/**
	 * Tests that if the requested depth is larger than the configured maximum, the expected error is returned
	 */
	@Test
	public void testDepthTooBig() {
		// Not expecting any mocks to be called due to JDBC_URL set above
		// Switch mocks to replay mode
		replayAll();
		
		// DB CascadeHealthWorker theWorker = new CascadeHealthWorker(mockHealthDAO);
		CascadeHealthWorker theWorker = new CascadeHealthWorker();

		// Prepare input
		Map<String, String> headers = new HashMap<String, String>(0);
		
		Map<String, String> urlParams = new HashMap<String, String>(1);
		urlParams.put(":str_depth", "7");

		// Do it
		GenericResponse<Map<String, Object>> theResponse = theWorker.process(null, urlParams, null, headers);
		
		// Check what we want to check
		assertEquals("Incorrect overall status code", 400, theResponse.httpCode);
		assertEquals("Incorrect response content type", "application/json", theResponse.responseBodyContentType);
		
		assertEquals("Incorrect number of body fields returned", 4, theResponse.responseBodyObject.size());
		assertEquals("Incorrect app name", TEST_APP_NAME, theResponse.responseBodyObject.get("app"));
		assertEquals("Incorrect status", "ERROR", theResponse.responseBodyObject.get("status"));
		assertEquals("Incorrect cascade depth", 7, theResponse.responseBodyObject.get("cascade_depth"));
		assertNotNull("No timestamp returned", theResponse.responseBodyObject.get("timestamp"));
		
		// Check mocks were called with the right values and right number of times
		verifyAll();
	}
	
	/**
	 * Tests that if a header is provided in the request it comes back in the response
	 */
	@Test
	public void testReflectedHeaders() {
		// Not expecting any mocks to be called due to JDBC_URL set above
		// Switch mocks to replay mode
		replayAll();
		
		// DB CascadeHealthWorker theWorker = new CascadeHealthWorker(mockHealthDAO);
		CascadeHealthWorker theWorker = new CascadeHealthWorker();

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
		
		// Check mocks were called with the right values and right number of times
		verifyAll();
	}
	
	/**
	 * Tests that if null header map is provided in the request, an empty list still comes back in the response
	 */
	@Test
	public void testNullHeaders() {
		// Not expecting any mocks to be called due to JDBC_URL set above
		// Switch mocks to replay mode
		replayAll();
		
		// DB CascadeHealthWorker theWorker = new CascadeHealthWorker(mockHealthDAO);
		CascadeHealthWorker theWorker = new CascadeHealthWorker();
		
		// Inputs
		Map<String, String> urlParams = new HashMap<String, String>(1);
		urlParams.put(":str_depth", "0");

		// Do the work
		GenericResponse<Map<String, Object>> theResponse = theWorker.process(null, urlParams, null, null);

		// Check what we want to check
		assertEquals("Incorrent number of reflected headers", 0, ((List<List<String>>) theResponse.responseBodyObject.get("headers")).size());		
		
		// Check mocks were called with the right values and right number of times
		verifyAll();
	}
	
	/**
	 * Tests that if a database is present, information about it comes back in the response
	 */
	// DB
	/*
	@Test
	public void testDB() {
		// Prepare the environment
		Config.DEPENDENCIES.put("DB2", "jdbc:db2");
				
		// Set up mocks and expectations
		DateTime dbTime = DateTime.now();
		EasyMock.expect(mockHealthDAO.getTimestamp(EasyMock.anyObject())).andReturn(dbTime);
		// Finished setting up mocks
		replayAll();
		
		// Create the worker
		CascadeHealthWorker theWorker = new CascadeHealthWorker(mockHealthDAO);
		
		// Get inputs ready
		Map<String, String> urlParams = new HashMap<String, String>(1);
		urlParams.put(":str_depth", "0");
		
		// Do it
		GenericResponse<Map<String, Object>> theResponse = theWorker.process(null, urlParams, null, null);
		
		// Check what we want to check
		List<Map<String, Object>> dbs = (List<Map<String, Object>>)theResponse.responseBodyObject.get("db");
		assertEquals("Database information not in response", 1, dbs.size());
		
		assertEquals("Incorrect number of database fields returned", 3, dbs.get(0).size());
		assertEquals("Incorrect DB name", "DB2", dbs.get(0).get("name"));
		assertEquals("Incorrect DB status", "OK", dbs.get(0).get("status"));
		assertEquals("Incorrect DB timestamp", dbTime.toString(), dbs.get(0).get("current_timestamp"));
		
		// Check mocks were called with the right values and right number of times
		verifyAll();
	}
	*/
	
	/**
	 * Tests that if a service is present, information about it comes back in the response.
	 * Base information only.
	 * 
	 * @throws UnirestException 
	 */
	@Test
	public void testServiceOK() throws UnirestException {
		// Prepare the environment
		Config.DEPENDENCIES.put("AnAPI", SERVICE_API_URL);
		
		// Set up mock expectations
		EasyMock.expect(Unirest.get(EasyMock.anyString())).andReturn(mockRequest);
		// Expect the rest of the unirest chain. Will test contents in other tests
		EasyMock.expect(mockRequest.header(EasyMock.anyString(), EasyMock.anyString())).andReturn(mockRequest);
		EasyMock.expect(mockRequest.asString()).andReturn(mockResponse);
		EasyMock.expect(mockResponse.getStatus()).andReturn(200);
		EasyMock.expect(mockResponse.getHeaders()).andReturn(mockHeaders);
		EasyMock.expect(mockHeaders.getFirst(EasyMock.anyString())).andReturn("myContentType");
		EasyMock.expect(mockResponse.getBody()).andReturn("{\"name\" : \"value\"}");
		// Not expecting mockHealthDAO to be called due to JDBC_URL set above
		
		// Finished setting up mocks
		replayAll();
		
		// Create the worker
		// DB CascadeHealthWorker theWorker = new CascadeHealthWorker(mockHealthDAO);
		CascadeHealthWorker theWorker = new CascadeHealthWorker();
		
		// Get inputs ready
		Map<String, String> urlParams = new HashMap<String, String>(1);
		urlParams.put(":str_depth", "1");
		Map<String, String> headers = new HashMap<String, String>(1);
		headers.put("X-Trace-ID", "123");
		
		// Do it
		GenericResponse<Map<String, Object>> theResponse = theWorker.process(null, urlParams, null, headers);
		
		// Check what we want to check
		assertEquals("Overall status code not correct", 200, theResponse.httpCode);
		
		List<Map<String, Object>> services = (List<Map<String, Object>>)theResponse.responseBodyObject.get("services");
		assertEquals("Service information not in response", 1, services.size());
		
		Map<String, Object> service = services.get(0);
		assertEquals("Incorrect number of service fields returned", 6, service.size());
		
		assertEquals("Service name not correct", "AnAPI", service.get("name"));
		assertEquals("Service type not correct", "http", service.get("type"));
		assertEquals("Service status code not correct", 200, service.get("status_code"));
		assertEquals("Service content type not correct", "myContentType", service.get("content_type"));
		assertEquals("Service status not correct", "OK", service.get("status"));
		Map<String, Object> serviceContent = (Map<String, Object>)service.get("content");
		assertEquals("Service response content not in response", 1, serviceContent.size());
		assertEquals("Service response content not correct", "value", serviceContent.get("name"));
		
		// Check mocks were called with the right values and right number of times
		verifyAll();
	}
	
	/**
	 * Tests that if a service is called and returns a 500 status code, it is reported as BAD with overall code 500
	 * 
	 * @throws UnirestException 
	 */
	@Test
	public void testServiceBad() throws UnirestException {
		// Prepare the environment
		Config.DEPENDENCIES.put("AnAPI", SERVICE_API_URL);
		
		// Set up mocks and expectations

		EasyMock.expect(Unirest.get(EasyMock.anyString())).andReturn(mockRequest);
		// Expect the rest of the unirest chain. Will test contents in other tests
		EasyMock.expect(mockRequest.header(EasyMock.anyString(), EasyMock.anyString())).andReturn(mockRequest);
		EasyMock.expect(mockRequest.asString()).andReturn(mockResponse);
		EasyMock.expect(mockResponse.getStatus()).andReturn(500);
		EasyMock.expect(mockResponse.getHeaders()).andReturn(mockHeaders);
		EasyMock.expect(mockHeaders.getFirst(EasyMock.anyString())).andReturn("myContentType");
		// Not expecting mockHealthDAO to be called due to JDBC_URL set above
		
		// Finished setting up mocks
		replayAll();
		
		// Create the worker
		// DB CascadeHealthWorker theWorker = new CascadeHealthWorker(mockHealthDAO);
		CascadeHealthWorker theWorker = new CascadeHealthWorker();
		
		// Get inputs ready
		Map<String, String> urlParams = new HashMap<String, String>(1);
		urlParams.put(":str_depth", "1");
		Map<String, String> headers = new HashMap<String, String>(1);
		headers.put("X-Trace-ID", "123");
		
		// Do it
		GenericResponse<Map<String, Object>> theResponse = theWorker.process(null, urlParams, null, headers);
		
		// Check what we want to check
		assertEquals("Overall status code not correct", 500, theResponse.httpCode);
		
		List<Map<String, Object>> services = (List<Map<String, Object>>)theResponse.responseBodyObject.get("services");
		assertEquals("Service information not in response", 1, services.size());
		
		Map<String, Object> service = services.get(0);
		assertEquals("Incorrect number of service fields returned", 5, service.size());
		assertEquals("Service name not correct", "AnAPI", service.get("name"));
		assertEquals("Service type not correct", "http", service.get("type"));
		assertEquals("Service status code not correct", 500, service.get("status_code"));
		assertEquals("Service content type not correct", "myContentType", service.get("content_type"));
		assertEquals("Service status not correct", "BAD", service.get("status"));
		assertNull("Service content returned", service.get("content"));
		
		// Check mocks were called with the right values and right number of times
		verifyAll();
	}
	
	/**
	 * Tests that if a service is called and returns an unexpected status code, it is reported as UNKNOWN with overall code 500
	 * 
	 * @throws UnirestException 
	 */
	@Test
	public void testServiceUnknown() throws UnirestException {
		// Prepare the environment
		Config.DEPENDENCIES.put("AnAPI", SERVICE_API_URL);

		EasyMock.expect(Unirest.get(EasyMock.anyString())).andReturn(mockRequest);
		// Expect the rest of the unirest chain. Will test contents in other tests
		EasyMock.expect(mockRequest.header(EasyMock.anyString(), EasyMock.anyString())).andReturn(mockRequest);
		EasyMock.expect(mockRequest.asString()).andReturn(mockResponse);
		EasyMock.expect(mockResponse.getStatus()).andReturn(400);
		EasyMock.expect(mockResponse.getHeaders()).andReturn(mockHeaders);
		EasyMock.expect(mockHeaders.getFirst(EasyMock.anyString())).andReturn("myContentType");
		// Not expecting mockHealthDAO to be called due to JDBC_URL set above
		
		// Finished setting up mocks
		replayAll();
		
		// Create the worker
		// DB CascadeHealthWorker theWorker = new CascadeHealthWorker(mockHealthDAO);
		CascadeHealthWorker theWorker = new CascadeHealthWorker();
		
		// Get inputs ready
		Map<String, String> urlParams = new HashMap<String, String>(1);
		urlParams.put(":str_depth", "1");
		Map<String, String> headers = new HashMap<String, String>(1);
		headers.put("X-Trace-ID", "123");
		
		// Do it
		GenericResponse<Map<String, Object>> theResponse = theWorker.process(null, urlParams, null, headers);
		
		// Check what we want to check
		assertEquals("Overall status code not correct", 500, theResponse.httpCode);
		
		List<Map<String, Object>> services = (List<Map<String, Object>>)theResponse.responseBodyObject.get("services");
		assertEquals("Service information not in response", 1, services.size());
		
		Map<String, Object> service = services.get(0);
		assertEquals("Incorrect number of service fields returned", 5, service.size());
		assertEquals("Service name not correct", "AnAPI", service.get("name"));
		assertEquals("Service type not correct", "http", service.get("type"));
		assertEquals("Service status code not correct", 400, service.get("status_code"));
		assertEquals("Service content type not correct", "myContentType", service.get("content_type"));
		assertEquals("Service status not correct", "UNKNOWN", service.get("status"));
		assertNull("Service content returned", service.get("content"));
		
		// Check mocks were called with the right values and right number of times
		verifyAll();
	}
	
	/**
	 * Tests that if a service is present but defined without a / at the end, it is still called correctly
	 * 
	 * @throws UnirestException 
	 */
	@Test
	public void testServiceWithNoSlash() throws UnirestException {
		// Prepare the environment
		// Remove the /
		Config.DEPENDENCIES.put("AnAPI", SERVICE_API_URL.substring(0, SERVICE_API_URL.length()-1));
		
		// Set up mocks and expectations
		Capture<String> capturedURLValue = EasyMock.newCapture();
		
		EasyMock.expect(Unirest.get(EasyMock.capture(capturedURLValue))).andReturn(mockRequest);
		// Expect the rest of the unirest chain.
		EasyMock.expect(mockRequest.header(EasyMock.anyString(), EasyMock.anyString())).andReturn(mockRequest);
		EasyMock.expect(mockRequest.asString()).andReturn(mockResponse);
		EasyMock.expect(mockResponse.getStatus()).andReturn(200);
		EasyMock.expect(mockResponse.getHeaders()).andReturn(mockHeaders);
		EasyMock.expect(mockHeaders.getFirst(EasyMock.anyString())).andReturn("myContentType");
		EasyMock.expect(mockResponse.getBody()).andReturn("{}");
		// Not expecting mockHealthDAO to be called due to JDBC_URL set above
		
		// Finished setting up mocks
		replayAll();
		
		// Create the worker
		// DB CascadeHealthWorker theWorker = new CascadeHealthWorker(mockHealthDAO);
		CascadeHealthWorker theWorker = new CascadeHealthWorker();
		
		// Get inputs ready
		Map<String, String> urlParams = new HashMap<String, String>(1);
		urlParams.put(":str_depth", "1");
		Map<String, String> headers = new HashMap<String, String>(1);
		headers.put("X-Trace-ID", "123");
		
		// Do it
		theWorker.process(null, urlParams, null, headers);
		
		// Check what we want to check
		assertEquals("URL value incorrect", "http://an-api/health/cascade/0", capturedURLValue.getValue());
		
		// Check mocks were called with the right values and right number of times
		verifyAll();
	}
	
	/**
	 * Tests that if a service is present, it is called with the depth correctly decremented
	 * 
	 * @throws UnirestException 
	 */
	@Test
	public void testServiceCalledWithCorrectDepth() throws UnirestException {
		// Prepare the environment
		Config.DEPENDENCIES.put("AnAPI", SERVICE_API_URL);
		
		// Set up mocks and expectations
		Capture<String> capturedURLValue = EasyMock.newCapture();
		
		EasyMock.expect(Unirest.get(EasyMock.capture(capturedURLValue))).andReturn(mockRequest);
		// Expect the rest of the unirest chain.
		EasyMock.expect(mockRequest.header(EasyMock.anyString(), EasyMock.anyString())).andReturn(mockRequest);
		EasyMock.expect(mockRequest.asString()).andReturn(mockResponse);
		EasyMock.expect(mockResponse.getStatus()).andReturn(200);
		EasyMock.expect(mockResponse.getHeaders()).andReturn(mockHeaders);
		EasyMock.expect(mockHeaders.getFirst(EasyMock.anyString())).andReturn("myContentType");
		EasyMock.expect(mockResponse.getBody()).andReturn("{}");
		// Not expecting mockHealthDAO to be called due to JDBC_URL set above
		
		// Finished setting up mocks
		replayAll();
		
		// Create the worker
		// DB CascadeHealthWorker theWorker = new CascadeHealthWorker(mockHealthDAO);
		CascadeHealthWorker theWorker = new CascadeHealthWorker();
		
		// Get inputs ready
		Map<String, String> urlParams = new HashMap<String, String>(1);
		urlParams.put(":str_depth", "5");
		Map<String, String> headers = new HashMap<String, String>(1);
		headers.put("X-Trace-ID", "123");
		
		// Do it
		theWorker.process(null, urlParams, null, headers);
		
		// Check what we want to check
		assertEquals("URL value incorrect", "http://an-api/health/cascade/4", capturedURLValue.getValue());
		
		// Check mocks were called with the right values and right number of times
		verifyAll();
	}
	
	/**
	 * Tests that if a service is present, it is not called if the requested depth is 0
	 * 
	 * @throws UnirestException 
	 */
	@Test
	public void testServiceNotCalledWith0Depth() throws UnirestException {
		// Prepare the environment
		Config.DEPENDENCIES.put("AnAPI", SERVICE_API_URL);
		
		// Set up mocks and expectations
		// Not expecting any mocks to be called
		// Finished setting up mocks
		replayAll();
		
		// Create the worker
		// DB CascadeHealthWorker theWorker = new CascadeHealthWorker(mockHealthDAO);
		CascadeHealthWorker theWorker = new CascadeHealthWorker();
		
		// Get inputs ready
		Map<String, String> urlParams = new HashMap<String, String>(1);
		urlParams.put(":str_depth", "0");
		Map<String, String> headers = new HashMap<String, String>(1);
		headers.put("X-Trace-ID", "123");
		
		// Do it
		GenericResponse<Map<String, Object>> theResponse = theWorker.process(null, urlParams, null, headers);
		
		// Check what we want to check
		List<List<String>> services = (List<List<String>>)theResponse.responseBodyObject.get("services");
		assertEquals("Incorrect number of services", 0, services.size());
		
		// Check mocks were called with the right values and right number of times
		verifyAll();
	}
		
	/**
	 * Tests that if a service is present, the trace id is pass through to it
	 * 
	 * @throws UnirestException 
	 */
	@Test
	public void testServiceCalledWithCorrectTraceId() throws UnirestException {
		// Prepare the environment
		Config.DEPENDENCIES.put("AnAPI", SERVICE_API_URL);
		
		// Set up mocks and expectations
		Capture<String> capturedHeaderValue = EasyMock.newCapture();
		
		EasyMock.expect(Unirest.get(EasyMock.anyString())).andReturn(mockRequest);
		// Expect the rest of the unirest chain.
		EasyMock.expect(mockRequest.header(EasyMock.eq("X-Trace-ID"), EasyMock.capture(capturedHeaderValue))).andReturn(mockRequest);
		EasyMock.expect(mockRequest.asString()).andReturn(mockResponse);
		EasyMock.expect(mockResponse.getStatus()).andReturn(200);
		EasyMock.expect(mockResponse.getHeaders()).andReturn(mockHeaders);
		EasyMock.expect(mockHeaders.getFirst(EasyMock.anyString())).andReturn("myContentType");
		EasyMock.expect(mockResponse.getBody()).andReturn("{}");
		// Not expecting mockHealthDAO to be called due to JDBC_URL set above
		
		// Finished setting up mocks
		replayAll();
		
		// Create the worker
		// DB CascadeHealthWorker theWorker = new CascadeHealthWorker(mockHealthDAO);
		CascadeHealthWorker theWorker = new CascadeHealthWorker();
		
		// Get inputs ready
		Map<String, String> urlParams = new HashMap<String, String>(1);
		urlParams.put(":str_depth", "1");
		Map<String, String> headers = new HashMap<String, String>(1);
		headers.put("X-Trace-ID", "123");
		
		// Do it
		theWorker.process(null, urlParams, null, headers);
		
		// Check what we want to check
		assertEquals("X-Trace-ID header value incorrect", "123", capturedHeaderValue.getValue());
		
		// Check mocks were called with the right values and right number of times
		verifyAll();
		
	}
	
}
