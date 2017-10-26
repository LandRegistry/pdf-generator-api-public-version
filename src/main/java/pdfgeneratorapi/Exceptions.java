package pdfgeneratorapi;

import static spark.Spark.exception;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonParseException;

import pdfgeneratorapi.utils.JsonTransformer;

public class Exceptions {

    private static final Logger logger = LoggerFactory.getLogger(Exceptions.class);

    public void registerExceptions() {
        // Application-raised errors
        exception(ApplicationException.class, (exception, request, response) -> {
            ApplicationException exc = (ApplicationException) exception;
            // If the app is throwing a 500 itself then it's log-worthy
            if (exc.httpCode == 500) {
            	logger.error("Application Exception: {}", exception.getMessage(), exception);
        	} else {
        		logger.debug("Application Exception: {}", exception.getMessage(), exception);
        	}
            response.type("application/json");
            response.status(exc.httpCode);
            response.body(buildExceptionBody(exc.getMessage(), exc.errorCode));
        });
        
        // If the incoming JSON could not be parsed (e.g. mismatched type in JSON vs target object
        // let's send back a 400 with explanation.
        exception(JsonParseException.class, (exception, request, response) -> {
        	logger.debug("JSON Parse Exception: {}", exception.getMessage(), exception);
            response.type("application/json");
            response.status(400);
            response.body(buildExceptionBody("Invalid input, caused by: " + exception.getMessage(), "XXX"));
        });

        // Emergency unexpected errors that have not been handled in the blocks above (null pointer etc)
        exception(Exception.class, (exception, request, response) -> {
            logger.error("Unhandled Exception: {}", exception.getClass().getName(), exception);
            response.type("application/json");
            response.status(500);
            response.body(buildExceptionBody("Unexpected error.", "XXX"));
        });
        
        logger.info("Exceptions registered");
    }
    
    private String buildExceptionBody(String errorMessage, String errorCode) {
    	// Just use a map as the json source, object is overkill for two fields
    	Map<String, String> result = new HashMap<String, String>(2);
    	result.put("error_message", errorMessage);
    	result.put("error_code", errorCode);
    	return JsonTransformer.render(result);
    }
}