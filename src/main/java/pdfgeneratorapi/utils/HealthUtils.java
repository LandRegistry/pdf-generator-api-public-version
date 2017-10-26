package pdfgeneratorapi.utils;

import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.Optional;

/**
 * Helper methods for health routes. 
 * 
 */
public class HealthUtils {

    public static List<List<String>> extractHeaders(Map<String, String> headers) {

        // Convert our headers map into a list of lists of strings, where each inner list 
		// contains two strings (being the header key and value respectively).
		// This matches Flask's output.

        // Load the headers into an optional
		return Optional.ofNullable(headers)
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

    }
}		
