package pdfgeneratorapi.views.logic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pdfgeneratorapi.pdf_generation.PdfRenderer;
import pdfgeneratorapi.pdf_generation.TitleSummary;
import pdfgeneratorapi.ApplicationException;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.HashMap;

import javax.servlet.http.HttpServletResponse;

import pdfgeneratorapi.Config;
import pdfgeneratorapi.utils.GenericResponse;
import pdfgeneratorapi.utils.GenericWorker;

public class PdfGenerationWorker extends GenericWorker<Map<String, Object>, Map<String, Object>> {
	private static final Logger logger = LoggerFactory.getLogger(PdfGenerationWorker.class);

	/**
	* PDF Generator route.
	*/

	public GenericResponse<Map<String, Object>> process(Map<String, Object> responseBodyObject, Map<String, String> urlParams, Map<String, String[]> queryParams, Map<String, String> headers) {
		logger.info("STARTED: generatePdf Generic Response");

		Map<String, Object> responseBody = new HashMap<>(4);
		responseBody.put("app", Config.APP_NAME);

		TitleSummary title_summary = new TitleSummary();

		HttpServletResponse pdf_download = (HttpServletResponse)responseBodyObject.get("pdf_download");

		try {
			title_summary = (TitleSummary)responseBodyObject.get("title_summary");
		} catch (Exception e) {
			throw new ApplicationException("No title summary in request", "NOTITLE", 400, e);
		}

		// Render the pdf with the TitleSummary object
		if (title_summary.getTitleNumber().isEmpty()){
			throw new ApplicationException("Title information is incomplete", "NOTITLEINFO", 400);
		}

		ByteArrayOutputStream pdfOutputStream;

		try {
			// Generate the pdf and prepare to stream it
			pdfOutputStream = PdfRenderer.main(title_summary);
		} catch (Exception e) {
			throw new ApplicationException("PDF failed to generate", "GEN", 500, e);
		}
		
		try {
			OutputStream outputPDF = pdf_download.getOutputStream();
			pdfOutputStream.writeTo(outputPDF);
        	outputPDF.flush();
        	outputPDF.close();
		} catch (Exception e) {
			throw new ApplicationException("PDF failed to stream", "STREAM", 500, e);
		}
		responseBody.put("headers", "stuff");
		responseBody.put("commit", Config.COMMIT);
		responseBody.put("status", "OK");
		
		return new GenericResponse<>(200, "application/json", responseBody);

	};

}