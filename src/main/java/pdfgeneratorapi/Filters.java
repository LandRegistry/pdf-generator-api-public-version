package pdfgeneratorapi;

import static java.util.UUID.randomUUID;
import static spark.Spark.after;
import static spark.Spark.before;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class Filters {
    private static final Logger logger = LoggerFactory.getLogger(Filters.class);

    public void registerFilters() {
        // Things that happen before the request is processed by the appropriate route code.
        before((request, response) -> {
            // Sets the transaction trace id into the request object if it has been provided in the HTTP header from the caller.
            // Generate a new one if it has not. We will use this in log messages.
            String traceid = request.headers("X-Trace-ID");  
            if (traceid == null) {
                traceid = randomUUID().toString();
                // We'll be inserting it into the headers further down.
            }
            // ******* \/ \/ READ THIS \/ \/ **********
            // Make sure to grab the traceid variable back out the headers and put it into the X-Trace-ID header
            // of any calls you make to other LR APIs (via Unirest or whatever)! 
            // *********************

            // For logging:
            MDC.put("traceid", traceid);
            
            // Get headers in a nicer format
            // Parse all received header keys/values
            Set<String> headerKeys = request.headers();
    		Map<String, String> headers = new HashMap<String, String>(headerKeys.size());
    		for (String headerKey : headerKeys) {
    			headers.put(headerKey, request.headers(headerKey));
    		}
    		// Insert (or re-insert the traceid, in case we had to generate a new one
    		headers.put("X-Trace-ID", traceid);
    		// and make them available
    		request.attribute("headers", headers);
        });

        // Things that happen after the request is processed by the appropriate route code.
        after((request, response) -> {
            // Add the API version (as in the interface spec, not the app) to the header. Semantic versioning applies - see the
            // API manual. A major version update will need to go in the URL. All changes should be documented though, for
            // reusing teams to take advantage of.
            response.header("X-API-Version", "1.0.0");
        });

        logger.info("Filters registered");
    }
}