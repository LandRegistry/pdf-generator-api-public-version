package pdfgeneratorapi.views;

import static spark.Spark.post;
import static spark.Spark.get;
import static spark.Spark.path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spark.Request;
import spark.Response;
import spark.Route;

import pdfgeneratorapi.views.logic.PdfGenerationWorker;
import pdfgeneratorapi.pdf_generation.AddressLines;
import pdfgeneratorapi.pdf_generation.Proprietors;
import pdfgeneratorapi.pdf_generation.Lenders;
import pdfgeneratorapi.pdf_generation.LenderAddressLines;
import pdfgeneratorapi.pdf_generation.TitleSummary;
import pdfgeneratorapi.pdf_generation.ProprietorsDeserializer;
import pdfgeneratorapi.pdf_generation.AddressLinesDeserializer;
import pdfgeneratorapi.pdf_generation.LendersDeserializer;
import pdfgeneratorapi.pdf_generation.LenderAddressLinesDeserializer;

import javax.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class GeneratePdf {
	private static final Logger logger = LoggerFactory.getLogger(General.class);
	public void registerRoutes() {
    	// Version 1 routes
		path("/v1", () -> {
            get("/pdf", renderPdf);
			post("/pdf", renderPdf);
			logger.info("pdf render routes registered");
		});
}

	/**
	* This is the route that the frontend hits with the json required to populate the title summary
	* If extra information is needed to go on the title summary that is title related, it needs to 
	* be sent from the digital-register-frontend.
	*/

	public Route renderPdf = (Request request, Response response) -> {

		logger.info("STARTED: renderPdf");

		String requestBody = request.body();

		final GsonBuilder gsonBuilder = new GsonBuilder();

		// These deserialisers are needed to allow the addresses for the Proprietors and Lenders to be
		// captured correctly
		gsonBuilder.registerTypeAdapter(Proprietors.class, new ProprietorsDeserializer());
		gsonBuilder.registerTypeAdapter(AddressLines.class, new AddressLinesDeserializer());
		gsonBuilder.registerTypeAdapter(Lenders.class, new LendersDeserializer());
		gsonBuilder.registerTypeAdapter(LenderAddressLines.class, new LenderAddressLinesDeserializer());

		Gson gson = gsonBuilder.create();

		// Parse JSON to Java
		final TitleSummary title_summary = gson.fromJson(requestBody, TitleSummary.class);

        HttpServletResponse pdf_download = response.raw();

        Map<String, Object> responseBodyObject = new HashMap<String, Object>();

		responseBodyObject.put("title_summary", title_summary);
		responseBodyObject.put("pdf_download", pdf_download);

		return new PdfGenerationWorker()
			.process(responseBodyObject, null, null, request.attribute("headers"))
			.getResult(request, response);
        
	};

}