package pdfgeneratorapi.views.logic;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pdfgeneratorapi.Config;
import pdfgeneratorapi.utils.GenericResponse;
import pdfgeneratorapi.utils.GenericWorker;

public class HealthWorker extends GenericWorker<Object, Map<String, Object>> {
	private static final Logger LOGGER = LoggerFactory.getLogger(HealthWorker.class);

	@Override
	public GenericResponse<Map<String, Object>> process(Object body, Map<String, String> urlParams, Map<String, String[]> queryParams, Map<String, String> headers) {
		LOGGER.debug("Health check - worker process start");
		// Create the response body
		// Can be a full blown object or just a map
		Map<String, Object> responseBody = new HashMap<>(4);
		responseBody.put("app", Config.APP_NAME);
		
		// Convert our headers map into a list of lists of strings, where each inner list 
		// contains two strings (being the header key and value respectively).
		// This matches Flask's output.
		List<List<String>> thefinalHeaders = 
				// Load the headers into an optional
				Optional.ofNullable(headers)
					// Apply a function to the value if it was given a not-null one
					.map(	
							// Stream through each entry in the headers
							theheaders -> theheaders.entrySet().stream()
								// and apply a function to each, specifically returning a list 
								// containing the key and value
								.map(entry -> Arrays.asList(entry.getKey(), entry.getValue()))
								// then collect the returned stream of lists into a list for returning
								// out of the top-level map method
								.collect(Collectors.toList())
						)
				// If the above function wasn't applied because the optional was given null, 
				// just return an empty list.
				.orElse(Collections.emptyList());

		responseBody.put("headers", thefinalHeaders);
		responseBody.put("commit", Config.COMMIT);
		responseBody.put("status", "OK");
		
		LOGGER.debug("Health check - worker process end");
		return new GenericResponse<>(200, "application/json", responseBody);
	}
}