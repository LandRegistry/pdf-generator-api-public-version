package pdfgeneratorapi.views;

import static spark.Spark.get;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spark.Request;
import spark.Response;
import spark.Route;

import pdfgeneratorapi.utils.JsonTransformer;
import pdfgeneratorapi.views.logic.HealthWorker;
import pdfgeneratorapi.views.logic.CascadeHealthWorker;


public class General {
	private static final Logger logger = LoggerFactory.getLogger(General.class);

	/**
	* Add all routes, the methods that implement them and body transformer
	* classes here.
	*/
	public void registerRoutes() {
		get("/health", doHealth, JsonTransformer::render);
		get("/health/cascade/:str_depth", doHealth, JsonTransformer::render);
		logger.info("General routes registered");
	}

	/**
	* The health route.
	*/
	public Route doHealth = (Request request, Response response) -> {
		// Do the work and return the final body object from the GenericResponse
		if (request.params(":str_depth") == null) {
			return new HealthWorker()
				.process(null, request.params(), request.queryMap().toMap(), request.attribute("headers"))
				.getResult(request, response);
		} else {
			return new CascadeHealthWorker()
				.process(null, request.params(), request.queryMap().toMap(), request.attribute("headers"))
				.getResult(request, response);			
		}
	};
	
}