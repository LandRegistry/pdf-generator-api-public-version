import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import pdfgeneratorapi.Config;
import pdfgeneratorapi.utils.GenericResponse;
import pdfgeneratorapi.views.logic.PdfGenerationWorker;
import pdfgeneratorapi.pdf_generation.AddressLines;
import pdfgeneratorapi.pdf_generation.PdfRenderer;
import pdfgeneratorapi.pdf_generation.Proprietors;
import pdfgeneratorapi.pdf_generation.TitleSummary;
import pdfgeneratorapi.ApplicationException;

import java.util.HashMap;
import java.util.Map;

import java.io.ByteArrayOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
// import org.joda.time.DateTime;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;



@SuppressWarnings({ "unchecked"})
@RunWith(PowerMockRunner.class)
@PrepareForTest({Config.class, PdfRenderer.class})

public class PdfGenerationWorkerTest extends EasyMockSupport {

	private static final String TEST_APP_NAME = "unittest-app-name";
	// private static final String SERVICE_API_URL = "http://an-api/";
	
	// private ByteArrayOutputStream mOutputStream;

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

	@Test
		public void testMissingTitleSummary() {
		// Create mock dependencies

			PowerMock.mockStatic(PdfRenderer.class);

			HttpServletResponse pdf_download = createMock(HttpServletResponse.class);

			// Done preparing, now put mock into "real" mode
			replayAll();

			// Prepare input
			Map<String, String> headers = new HashMap<String, String>(0);
			Map<String, Object> responseBodyObject = new HashMap<String, Object>();

			responseBodyObject.put("title_summary", "title_summary");
			responseBodyObject.put("pdf_download", pdf_download);

			PdfGenerationWorker theWorker = new PdfGenerationWorker();

			// Do it
			boolean exceptionthrown  = false;
			try {
				theWorker.process(responseBodyObject, null, null, headers);
			} catch (ApplicationException e) {
				exceptionthrown = true;
				assertEquals("Title information is incomplete", 400, e.httpCode);
			}
			assertTrue("ApplicationException not thrown", exceptionthrown);
			verifyAll();
		}


@Test
		public void testPdfRenderFail() throws Exception {
		// Create mock dependencies

			PowerMock.mockStatic(PdfRenderer.class);
			HttpServletResponse pdf_download = createMock(HttpServletResponse.class);;
			ApplicationException appException = createMock(ApplicationException.class);

			// title_summary object creation for pdf_render.main
			TitleSummary title_summary = new TitleSummary();
			Proprietors[] proprietor = {new Proprietors()};
			AddressLines[] addressLines = {new AddressLines()};
			addressLines[0].lines = new String[] { "One", "Two", "Three" };
			proprietor[0].name = "name";
			proprietor[0].addressLines = addressLines;
			title_summary.number = "DN123456";
			title_summary.tenure = "tenure";
			title_summary.is_caution_title = "true";
			title_summary.ppi_data = "ppi data";
			title_summary.address_lines = new String[] { "One", "Two", "Three" };
			title_summary.proprietors = proprietor;
			title_summary.lenders = null;
			title_summary.summary_heading = "summary heading";
			title_summary.proprietor_type_heading = "proprietor heading";
			title_summary.receipt = null;
			title_summary.last_changed_readable = "last changed date";

			EasyMock.expect(PdfRenderer.main(title_summary)).andThrow(appException);

			// Done preparing, now put mock into "real" mode
			replayAll();

			// Prepare input
			Map<String, String> headers = new HashMap<String, String>(0);
			Map<String, Object> responseBodyObject = new HashMap<String, Object>();

			responseBodyObject.put("title_summary", title_summary);
			responseBodyObject.put("pdf_download", pdf_download);

			PdfGenerationWorker theWorker = new PdfGenerationWorker();

			// Do it
			boolean exceptionthrown  = false;
			try {
				theWorker.process(responseBodyObject, null, null, headers);
			} catch (ApplicationException e) {
				exceptionthrown = true;
				assertEquals("PDF Rendering has failed", 500, e.httpCode);
			}
			assertTrue("ApplicationException not thrown", exceptionthrown);
			verifyAll();
		}

@Test
		public void testRenderPdfSuccess() throws Exception {
		// Create mock dependencies

			PowerMock.mockStatic(PdfRenderer.class);
			HttpServletResponse pdf_download = createMock(HttpServletResponse.class);
			ServletOutputStream outputPDF = createMock(ServletOutputStream.class);
			ByteArrayOutputStream pdfGeneration = createMock(ByteArrayOutputStream.class);

			// title_summary object creation for pdf_render.main
			TitleSummary title_summary = new TitleSummary();
			Proprietors[] proprietor = {new Proprietors()};
			AddressLines[] addressLines = {new AddressLines()};
			addressLines[0].lines = new String[] { "One", "Two", "Three" };
			proprietor[0].name = "name";
			proprietor[0].addressLines = addressLines;
			title_summary.number = "DN123456";
			title_summary.tenure = "tenure";
			title_summary.is_caution_title = "true";
			title_summary.ppi_data = "ppi data";
			title_summary.address_lines = new String[] { "One", "Two", "Three" };
			title_summary.proprietors = proprietor;
			title_summary.lenders = null;
			title_summary.summary_heading = "summary heading";
			title_summary.proprietor_type_heading = "proprietor heading";
			title_summary.receipt = null;
			title_summary.last_changed_readable = "last changed date";

			EasyMock.expect(PdfRenderer.main(title_summary)).andReturn(pdfGeneration);
			EasyMock.expect(pdf_download.getOutputStream()).andReturn(outputPDF);
			pdfGeneration.writeTo(outputPDF);
			outputPDF.flush();
			outputPDF.close();
			// Done preparing, now put mock into "real" mode
			replayAll();

			// Prepare input
			
			Map<String, String> headers = new HashMap<String, String>(0);
			Map<String, Object> responseBodyObject = new HashMap<String, Object>();

			responseBodyObject.put("title_summary", title_summary);
			responseBodyObject.put("pdf_download", pdf_download);

			PdfGenerationWorker theWorker = new PdfGenerationWorker();

			GenericResponse<Map<String, Object>> theResponse = theWorker.process(responseBodyObject, null, null, headers);

			assertEquals(200, theResponse.httpCode);

			verifyAll();
	}

@Test
		public void testPdfStreamingFailure() throws Exception {
		// Create mock dependencies

			PowerMock.mockStatic(PdfRenderer.class);
			HttpServletResponse pdf_download = createMock(HttpServletResponse.class);
			ByteArrayOutputStream pdfGeneration = createMock(ByteArrayOutputStream.class);
			ApplicationException appException = createMock(ApplicationException.class);

			// title_summary object creation for pdf_render.main
			TitleSummary title_summary = new TitleSummary();
			Proprietors[] proprietor = {new Proprietors()};
			AddressLines[] addressLines = {new AddressLines()};
			addressLines[0].lines = new String[] { "One", "Two", "Three" };
			proprietor[0].name = "name";
			proprietor[0].addressLines = addressLines;
			title_summary.number = "DN123456";
			title_summary.tenure = "tenure";
			title_summary.is_caution_title = "true";
			title_summary.ppi_data = "ppi data";
			title_summary.address_lines = new String[] { "One", "Two", "Three" };
			title_summary.proprietors = proprietor;
			title_summary.lenders = null;
			title_summary.summary_heading = "summary heading";
			title_summary.proprietor_type_heading = "proprietor heading";
			title_summary.receipt = null;
			title_summary.last_changed_readable = "last changed date";

			EasyMock.expect(PdfRenderer.main(title_summary)).andReturn(pdfGeneration);
			EasyMock.expect(pdf_download.getOutputStream()).andThrow(appException);
			// Done preparing, now put mock into "real" mode
			replayAll();

			// Prepare input
			
			Map<String, String> headers = new HashMap<String, String>(0);
			Map<String, Object> responseBodyObject = new HashMap<String, Object>();

			responseBodyObject.put("title_summary", title_summary);
			responseBodyObject.put("pdf_download", pdf_download);

			PdfGenerationWorker theWorker = new PdfGenerationWorker();

			// Do it
			boolean exceptionthrown  = false;
			try {
				theWorker.process(responseBodyObject, null, null, headers);
			} catch (ApplicationException e) {
				exceptionthrown = true;
				assertEquals("Streaming PDF has failed", 500, e.httpCode);
			}
			assertTrue("ApplicationException not thrown", exceptionthrown);
			verifyAll();
	}

}